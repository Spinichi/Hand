package com.finger.hand_backend.measurement;

import com.finger.hand_backend.measurement.dto.DailyAnomalyResponse;
import com.finger.hand_backend.measurement.dto.MeasurementRequest;
import com.finger.hand_backend.measurement.dto.MeasurementResponse;
import com.finger.hand_backend.measurement.dto.WeeklyAnomalyResponse;
import com.finger.hand_backend.relief.ReliefAfterBackfill;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Measurement Service
 * - 측정 데이터 저장 및 조회
 * - 워치에서 계산된 데이터를 수신하여 저장
 */
@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final ReliefAfterBackfill reliefAfterBackfill;

    /**
     * 측정 데이터 저장
     * - 워치에서 계산된 모든 값을 수신하여 그대로 저장
     *
     * @param userId  사용자 ID
     * @param request 측정 데이터 요청
     * @return 저장된 측정 데이터
     */
    @Transactional
    public Measurement save(Long userId, MeasurementRequest request) {
        // 워치에서 계산된 값 그대로 저장
        Measurement measurement = Measurement.builder()
                .userId(userId)
                .heartRate(request.getHeartRate())
                .hrvSdnn(request.getHrvSdnn())
                .hrvRmssd(request.getHrvRmssd())
                .objectTemp(request.getObjectTemp())
                .ambientTemp(request.getAmbientTemp())
                .accelX(request.getAccelX())
                .accelY(request.getAccelY())
                .accelZ(request.getAccelZ())
                .movementIntensity(request.getMovementIntensity())
                .stressIndex(request.getStressIndex())
                .stressLevel(request.getStressLevel())
                .isAnomaly(request.getIsAnomaly())
                .totalSteps(request.getTotalSteps())
                .stepsPerMinute(request.getStepsPerMinute())
                .measuredAt(request.getMeasuredAt())
                .build();

        Measurement saved = measurementRepository.save(measurement);

        // Relief 세션 연동
        reliefAfterBackfill.onNewMeasurement(saved.getUserId(), saved);

        return saved;
    }

    /**
     * 사용자별 측정 데이터 페이징 조회
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 측정 데이터 페이지
     */
    @Transactional(readOnly = true)
    public Page<Measurement> getMyMeasurements(Long userId, Pageable pageable) {
        return measurementRepository.findByUserId(userId, pageable);
    }

    /**
     * 특정 기간 측정 데이터 조회
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 측정 데이터 리스트
     */
    @Transactional(readOnly = true)
    public List<Measurement> getMeasurementsByDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return measurementRepository.findByUserIdAndMeasuredAtBetween(userId, start, end);
    }

    /**
     * 특정 측정 데이터 조회
     *
     * @param userId        사용자 ID
     * @param measurementId 측정 데이터 ID
     * @return 측정 데이터
     */
    @Transactional(readOnly = true)
    public Measurement getMeasurement(Long userId, Long measurementId) {
        Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new IllegalArgumentException("MEASUREMENT_NOT_FOUND"));

        // 본인 데이터인지 확인
        if (!measurement.getUserId().equals(userId)) {
            throw new IllegalArgumentException("NOT_YOUR_MEASUREMENT");
        }

        return measurement;
    }

    /**
     * 측정 데이터 삭제
     *
     * @param userId        사용자 ID
     * @param measurementId 측정 데이터 ID
     */
    @Transactional
    public void deleteMeasurement(Long userId, Long measurementId) {
        Measurement measurement = getMeasurement(userId, measurementId);
        measurementRepository.delete(measurement);
    }

    /**
     * 특정 기간 측정 데이터 개수 조회
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 측정 데이터 개수
     */
    @Transactional(readOnly = true)
    public long countMeasurements(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return measurementRepository.countByUserIdAndMeasuredAtBetween(userId, start, end);
    }

    /**
     * 특정 날짜의 이상치 조회
     *
     * @param userId 사용자 ID
     * @param date   조회 날짜
     * @return 해당 날짜의 이상치 데이터
     */
    @Transactional(readOnly = true)
    public DailyAnomalyResponse getDailyAnomalies(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Measurement> anomalies = measurementRepository
                .findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(userId, startOfDay, endOfDay);

        List<MeasurementResponse> anomalyResponses = anomalies.stream()
                .map(MeasurementResponse::from)
                .collect(Collectors.toList());

        return DailyAnomalyResponse.builder()
                .date(date)
                .anomalyCount(anomalies.size())
                .anomalies(anomalyResponses)
                .build();
    }

    /**
     * 최근 일주일간 이상치 조회 (날짜별 그룹화)
     *
     * @param userId 사용자 ID
     * @return 일주일간 날짜별 이상치 데이터
     */
    @Transactional(readOnly = true)
    public WeeklyAnomalyResponse getWeeklyAnomalies(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // 오늘 포함 7일

        List<DailyAnomalyResponse> dailyAnomalies = new ArrayList<>();
        int totalCount = 0;

        // 각 날짜별로 이상치 개수만 조회 (상세 데이터 제외)
        for (LocalDate date = sevenDaysAgo; !date.isAfter(today); date = date.plusDays(1)) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            List<Measurement> anomalies = measurementRepository
                    .findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(userId, startOfDay, endOfDay);

            int count = anomalies.size();
            totalCount += count;

            // anomalies 리스트는 빈 리스트로 설정 (데이터 경량화)
            DailyAnomalyResponse dailyAnomaly = DailyAnomalyResponse.builder()
                    .date(date)
                    .anomalyCount(count)
                    .anomalies(List.of()) // 빈 리스트
                    .build();

            dailyAnomalies.add(dailyAnomaly);
        }

        return WeeklyAnomalyResponse.builder()
                .startDate(sevenDaysAgo)
                .endDate(today)
                .totalAnomalyCount(totalCount)
                .dailyAnomalies(dailyAnomalies)
                .build();
    }

    /**
     * 가장 최근 측정 데이터 조회
     * 홈 화면 표시용
     *
     * @param userId 사용자 ID
     * @return 가장 최근 측정 데이터 (없으면 null)
     */
    @Transactional(readOnly = true)
    public Measurement getLatestMeasurement(Long userId) {
        return measurementRepository.findTopByUserIdOrderByMeasuredAtDesc(userId)
                .orElse(null);
    }
}
