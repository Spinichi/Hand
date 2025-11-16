package com.finger.hand_backend.measurement;

import com.finger.hand_backend.measurement.dto.*;
import com.finger.hand_backend.relief.ReliefAfterBackfill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Measurement Service
 * - 측정 데이터 저장 및 조회
 * - 워치에서 계산된 데이터를 수신하여 저장
 * - 스트레스 통계 조회
 */
@Slf4j
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

    // ===== 스트레스 관련 메서드 =====

    /**
     * 오늘의 스트레스 변화 조회
     * - 이상치 횟수
     * - 시간대별 통계 (최고/최저/평균)
     * - 최고점/최저점 시각
     */
    @Transactional(readOnly = true)
    public TodayStressResponse getTodayStress(Long userId, LocalDate date) {
        log.info("Getting today stress for user {} on {}", userId, date);

        // 1. 오늘 하루 범위 계산 (00:00:00 ~ 23:59:59)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 2. 오늘의 모든 측정 데이터 조회
        List<Measurement> measurements = measurementRepository
                .findByUserIdAndMeasuredAtBetween(userId, startOfDay, endOfDay);

        log.debug("Found {} measurements for date {}", measurements.size(), date);

        // 3. 이상치 횟수 계산
        long anomalyCount = measurements.stream()
                .filter(Measurement::getIsAnomaly)
                .count();

        // 4. 시간대별 통계 계산 (0시~23시)
        List<HourlyStatsDto> hourlyStats = calculateHourlyStats(measurements);

        // 5. 최고점 찾기 (여러 개 가능)
        List<StressPointDto> peakStress = findPeakStress(measurements);

        // 6. 최저점 찾기 (여러 개 가능)
        List<StressPointDto> lowestStress = findLowestStress(measurements);

        return new TodayStressResponse(
                date,
                (int) anomalyCount,
                hourlyStats,
                peakStress,
                lowestStress
        );
    }

    /**
     * 시간대별 통계 계산 (0시~23시, 총 24개)
     */
    private List<HourlyStatsDto> calculateHourlyStats(List<Measurement> measurements) {
        // 시간대별로 그룹화
        Map<Integer, List<Measurement>> hourlyGrouped = measurements.stream()
                .collect(Collectors.groupingBy(m -> m.getMeasuredAt().getHour()));

        // 0시~23시까지 24개 통계 생성
        List<HourlyStatsDto> hourlyStats = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            List<Measurement> hourMeasurements = hourlyGrouped.getOrDefault(hour, Collections.emptyList());

            if (hourMeasurements.isEmpty()) {
                // 해당 시간대에 측정 데이터가 없으면 null 값
                hourlyStats.add(new HourlyStatsDto(hour, null, null, null, 0));
            } else {
                // 최고/최저/평균 계산
                DoubleSummaryStatistics stats = hourMeasurements.stream()
                        .filter(m -> m.getStressIndex() != null)
                        .mapToDouble(Measurement::getStressIndex)
                        .summaryStatistics();

                hourlyStats.add(new HourlyStatsDto(
                        hour,
                        stats.getMax(),
                        stats.getMin(),
                        stats.getAverage(),
                        hourMeasurements.size()
                ));
            }
        }

        return hourlyStats;
    }

    /**
     * 최고점 찾기 (stressIndex가 최대인 모든 측정값)
     */
    private List<StressPointDto> findPeakStress(List<Measurement> measurements) {
        if (measurements.isEmpty()) {
            return Collections.emptyList();
        }

        // 최댓값 찾기
        double maxStress = measurements.stream()
                .filter(m -> m.getStressIndex() != null)
                .mapToDouble(Measurement::getStressIndex)
                .max()
                .orElse(0.0);

        // 최댓값과 같은 모든 측정값 반환
        return measurements.stream()
                .filter(m -> m.getStressIndex() != null && m.getStressIndex() == maxStress)
                .map(m -> new StressPointDto(m.getStressIndex(), m.getMeasuredAt()))
                .collect(Collectors.toList());
    }

    /**
     * 최저점 찾기 (stressIndex가 최소인 모든 측정값)
     */
    private List<StressPointDto> findLowestStress(List<Measurement> measurements) {
        if (measurements.isEmpty()) {
            return Collections.emptyList();
        }

        // 최솟값 찾기
        double minStress = measurements.stream()
                .filter(m -> m.getStressIndex() != null)
                .mapToDouble(Measurement::getStressIndex)
                .min()
                .orElse(0.0);

        // 최솟값과 같은 모든 측정값 반환
        return measurements.stream()
                .filter(m -> m.getStressIndex() != null && m.getStressIndex() == minStress)
                .map(m -> new StressPointDto(m.getStressIndex(), m.getMeasuredAt()))
                .collect(Collectors.toList());
    }

}