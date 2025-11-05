package com.finger.hand_backend.anomaly;

import com.finger.hand_backend.anomaly.dto.AnomalyDetectionResponse;
import com.finger.hand_backend.measurement.Measurement;
import com.finger.hand_backend.measurement.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AnomalyDetection Service
 * - 이상치 자동 탐지 (stress_level >= 4)
 * - 이상치 이력 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnomalyDetectionService {

    private final AnomalyDetectionRepository anomalyDetectionRepository;
    private final MeasurementRepository measurementRepository;

    /**
     * 이상치 탐지 및 저장
     * - stress_level >= 4 일 때 자동 생성
     * - 중복 방지: 최근 5분 이내 이상치가 있으면 생성하지 않음
     *
     * @param measurement 측정 데이터
     */
    @Transactional
    public void detectAnomaly(Measurement measurement) {
        // 1. stress_level >= 4 체크
        if (measurement.getStressLevel() < 4) {
            return;  // 이상치 아님
        }

        // 2. 중복 방지: 최근 5분 이내 이상치 존재 여부 확인
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        boolean recentAnomalyExists = anomalyDetectionRepository
                .existsByUserIdAndCreatedAtAfter(measurement.getUserId(), fiveMinutesAgo);

        if (recentAnomalyExists) {
            log.debug("최근 5분 이내 이상치가 이미 존재하여 생성하지 않음. userId={}, stressLevel={}",
                    measurement.getUserId(), measurement.getStressLevel());
            return;  // 중복 생성 방지
        }

        // 3. 이상치 생성 및 저장
        AnomalyDetection anomaly = AnomalyDetection.builder()
                .userId(measurement.getUserId())
                .measurementId(measurement.getId())
                .build();

        anomalyDetectionRepository.save(anomaly);

        log.info("이상치 탐지 및 저장 완료. userId={}, stressLevel={}, stressIndex={}",
                measurement.getUserId(), measurement.getStressLevel(), measurement.getStressIndex());
    }

    /**
     * 내 이상치 이력 조회 (페이징)
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 이상치 페이지
     */
    public Page<AnomalyDetectionResponse> getMyAnomalies(Long userId, Pageable pageable) {
        Page<AnomalyDetection> anomalyPage = anomalyDetectionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        // AnomalyDetection + Measurement 조인하여 Response 생성
        return anomalyPage.map(anomaly -> {
            Measurement measurement = measurementRepository.findById(anomaly.getMeasurementId())
                    .orElseThrow(() -> new IllegalStateException(
                            "측정 데이터를 찾을 수 없습니다. id=" + anomaly.getMeasurementId()));
            return AnomalyDetectionResponse.from(anomaly, measurement);
        });
    }

    /**
     * 특정 이상치 조회
     *
     * @param userId    사용자 ID
     * @param anomalyId 이상치 ID
     * @return 이상치 응답
     */
    public AnomalyDetectionResponse getAnomalyById(Long userId, Long anomalyId) {
        AnomalyDetection anomaly = anomalyDetectionRepository.findById(anomalyId)
                .orElseThrow(() -> new IllegalArgumentException("이상치를 찾을 수 없습니다. id=" + anomalyId));

        // 권한 체크
        if (!anomaly.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        Measurement measurement = measurementRepository.findById(anomaly.getMeasurementId())
                .orElseThrow(() -> new IllegalStateException(
                        "측정 데이터를 찾을 수 없습니다. id=" + anomaly.getMeasurementId()));

        return AnomalyDetectionResponse.from(anomaly, measurement);
    }

    /**
     * 특정 기간 이상치 조회
     *
     * @param userId 사용자 ID
     * @param start  시작 시각
     * @param end    종료 시각
     * @return 이상치 리스트
     */
    public List<AnomalyDetectionResponse> getAnomaliesByDateRange(
            Long userId, LocalDateTime start, LocalDateTime end) {
        List<AnomalyDetection> anomalies = anomalyDetectionRepository
                .findByUserIdAndCreatedAtBetween(userId, start, end);

        return anomalies.stream()
                .map(anomaly -> {
                    Measurement measurement = measurementRepository.findById(anomaly.getMeasurementId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "측정 데이터를 찾을 수 없습니다. id=" + anomaly.getMeasurementId()));
                    return AnomalyDetectionResponse.from(anomaly, measurement);
                })
                .toList();
    }

    /**
     * 특정 기간 이상치 개수 조회
     *
     * @param userId 사용자 ID
     * @param start  시작 시각
     * @param end    종료 시각
     * @return 개수
     */
    public long countAnomalies(Long userId, LocalDateTime start, LocalDateTime end) {
        return anomalyDetectionRepository.countByUserIdAndCreatedAtBetween(userId, start, end);
    }

    /**
     * 이상치 삭제
     *
     * @param userId    사용자 ID
     * @param anomalyId 이상치 ID
     */
    @Transactional
    public void deleteAnomaly(Long userId, Long anomalyId) {
        AnomalyDetection anomaly = anomalyDetectionRepository.findById(anomalyId)
                .orElseThrow(() -> new IllegalArgumentException("이상치를 찾을 수 없습니다. id=" + anomalyId));

        // 권한 체크
        if (!anomaly.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        anomalyDetectionRepository.delete(anomaly);
    }
}
