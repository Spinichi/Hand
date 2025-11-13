package com.finger.hand_backend.baseline;

import com.finger.hand_backend.measurement.Measurement;
import com.finger.hand_backend.measurement.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Baseline Service
 * - 편안한 상태 측정 데이터로부터 개인화된 Baseline 계산
 * - 버전 관리 및 활성화/비활성화
 */
@Service
@RequiredArgsConstructor
public class BaselineService {

    private final BaselineRepository baselineRepository;
    private final MeasurementRepository measurementRepository;

    /**
     * Baseline 계산 및 생성
     * - 최근 N일간 stress_level ≤ 2 (편안한 상태) 측정 데이터 수집
     * - 통계 계산 (평균, 표준편차)
     * - 스트레스 임계값 계산
     *
     * @param userId 사용자 ID
     * @param days   수집 기간 (기본 3일)
     * @return 생성된 Baseline
     */
    @Transactional
    public Baseline calculateAndSave(Long userId, Integer days) {
        if (days == null || days < 1) {
            days = 3;  // 기본값
        }

        // 1. 최근 N일간 편안한 상태 측정 데이터 조회
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Measurement> measurements = measurementRepository
                .findByUserIdAndMeasuredAtBetweenAndStressLevelLessThanEqual(
                        userId, startDate, endDate, 2
                );

        if (measurements.isEmpty()) {
            throw new IllegalStateException("INSUFFICIENT_DATA: 편안한 상태의 측정 데이터가 부족합니다");
        }

        if (measurements.size() < 10) {
            throw new IllegalStateException(
                    String.format("INSUFFICIENT_DATA: 최소 10개 이상의 데이터가 필요합니다 (현재: %d개)",
                            measurements.size())
            );
        }

        // 2. 통계 계산
        double hrvSdnnMean = calculateMean(measurements, Measurement::getHrvSdnn);
        double hrvSdnnStd = calculateStdDev(measurements, Measurement::getHrvSdnn, hrvSdnnMean);

        double hrvRmssdMean = calculateMean(measurements, Measurement::getHrvRmssd);
        double hrvRmssdStd = calculateStdDev(measurements, Measurement::getHrvRmssd, hrvRmssdMean);

        double heartRateMean = calculateMean(measurements, m -> m.getHeartRate().doubleValue());
        double heartRateStd = calculateStdDev(measurements, m -> m.getHeartRate().doubleValue(), heartRateMean);

        double objectTempMean = calculateMean(measurements, Measurement::getObjectTemp);
        double objectTempStd = calculateStdDev(measurements, Measurement::getObjectTemp, objectTempMean);

        // 3. 버전 관리
        Integer nextVersion = baselineRepository.findMaxVersionByUserId(userId) + 1;

        // 4. 기존 활성 Baseline 비활성화
        baselineRepository.deactivateAllByUserId(userId);

        // 5. 새 Baseline 생성 및 저장
        Baseline baseline = Baseline.builder()
                .userId(userId)
                .version(nextVersion)
                .isActive(true)
                .hrvSdnnMean(hrvSdnnMean)
                .hrvSdnnStd(hrvSdnnStd)
                .hrvRmssdMean(hrvRmssdMean)
                .hrvRmssdStd(hrvRmssdStd)
                .heartRateMean(heartRateMean)
                .heartRateStd(heartRateStd)
                .objectTempMean(objectTempMean)
                .objectTempStd(objectTempStd)
                .measurementCount(measurements.size())
                .dataStartDate(measurements.get(0).getMeasuredAt().toLocalDate())
                .dataEndDate(measurements.get(measurements.size() - 1).getMeasuredAt().toLocalDate())
                .build();

        return baselineRepository.save(baseline);
    }

    /**
     * 활성 Baseline 조회
     *
     * @param userId 사용자 ID
     * @return 활성 Baseline
     */
    @Transactional(readOnly = true)
    public Baseline getActiveBaseline(Long userId) {
        return baselineRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("BASELINE_NOT_FOUND: 활성 Baseline이 없습니다"));
    }

    /**
     * Baseline 이력 조회 (페이징)
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return Baseline 페이지
     */
    @Transactional(readOnly = true)
    public Page<Baseline> getBaselineHistory(Long userId, Pageable pageable) {
        return baselineRepository.findByUserIdOrderByVersionDesc(userId, pageable);
    }

    /**
     * 특정 버전 Baseline 조회
     *
     * @param userId  사용자 ID
     * @param version 버전 번호
     * @return Baseline
     */
    @Transactional(readOnly = true)
    public Baseline getBaselineByVersion(Long userId, Integer version) {
        return baselineRepository.findByUserIdAndVersion(userId, version)
                .orElseThrow(() -> new IllegalArgumentException("BASELINE_NOT_FOUND"));
    }

    /**
     * Baseline 활성화
     * - 기존 활성 Baseline 비활성화
     * - 지정한 버전 활성화
     *
     * @param userId  사용자 ID
     * @param version 활성화할 버전
     * @return 활성화된 Baseline
     */
    @Transactional
    public Baseline activateBaseline(Long userId, Integer version) {
        Baseline baseline = getBaselineByVersion(userId, version);

        // 모든 Baseline 비활성화
        baselineRepository.deactivateAllByUserId(userId);

        // 지정한 버전 활성화
        baseline.activate();
        return baselineRepository.save(baseline);
    }

    /**
     * Baseline 삭제
     * - 활성 Baseline은 삭제 불가
     *
     * @param userId  사용자 ID
     * @param version 삭제할 버전
     */
    @Transactional
    public void deleteBaseline(Long userId, Integer version) {
        Baseline baseline = getBaselineByVersion(userId, version);

        if (baseline.getIsActive()) {
            throw new IllegalStateException("CANNOT_DELETE_ACTIVE: 활성 Baseline은 삭제할 수 없습니다");
        }

        baselineRepository.delete(baseline);
    }

    /**
     * Baseline 업데이트 (재계산)
     * - 새 데이터로 Baseline 재계산
     * - 새 버전으로 저장
     *
     * @param userId 사용자 ID
     * @param days   수집 기간
     * @return 업데이트된 Baseline
     */
    @Transactional
    public Baseline updateBaseline(Long userId, Integer days) {
        // 기존 활성 Baseline이 있는지 확인
        baselineRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("BASELINE_NOT_FOUND"));

        // 재계산 (내부적으로 버전 증가 및 활성화 처리)
        return calculateAndSave(userId, days);
    }

    // ========== 통계 계산 헬퍼 메서드 ==========

    /**
     * 평균 계산
     */
    private double calculateMean(List<Measurement> measurements,
                                  java.util.function.Function<Measurement, Double> extractor) {
        OptionalDouble average = measurements.stream()
                .map(extractor)
                .filter(val -> val != null && val > 0)
                .mapToDouble(Double::doubleValue)
                .average();

        return average.orElse(0.0);
    }

    /**
     * 표준편차 계산
     */
    private double calculateStdDev(List<Measurement> measurements,
                                    java.util.function.Function<Measurement, Double> extractor,
                                    double mean) {
        double variance = measurements.stream()
                .map(extractor)
                .filter(val -> val != null && val > 0)
                .mapToDouble(val -> Math.pow(val - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }
}
