package com.finger.hand_backend.risk;

import com.finger.hand_backend.measurement.Measurement;
import com.finger.hand_backend.measurement.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 일일 위험 점수 서비스
 * - 다이어리 우울점수 + 측정 데이터 → 종합 위험 점수
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyRiskScoreService {

    private final DailyRiskScoreRepository riskScoreRepository;
    private final MeasurementRepository measurementRepository;

    /**
     * 일일 위험 점수 계산 및 저장
     *
     * @param userId          사용자 ID
     * @param date            날짜
     * @param depressionScore 다이어리 우울점수 (0-100)
     * @return 저장된 DailyRiskScore
     */
    @Transactional
    public DailyRiskScore calculateAndSave(Long userId, LocalDate date, Double depressionScore) {
        log.info("일일 위험 점수 계산 - userId: {}, date: {}", userId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // 1. diary_component (AI 우울점수)
        Double diaryComponent = depressionScore; // 0-100

        // 2. measurement_component 계산
        Double measurementComponent = calculateMeasurementComponent(userId, startOfDay, endOfDay);

        // 3. 하루 총 측정 횟수
        Integer measurementCount = (int) measurementRepository
                .countByUserIdAndMeasuredAtBetween(userId, startOfDay, endOfDay);

        // 4. 하루 이상치 감지 횟수 (워치에서 탐지된 isAnomaly=true)
        List<Measurement> anomalies = measurementRepository
                .findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(userId, startOfDay, endOfDay);
        Integer anomalyCount = anomalies.size();

        // 5. 최종 risk_score 계산 (wellness score - 높을수록 좋음)
        // diary: 높을수록 좋음 (AI 우울점수지만 기획 변경으로 wellness 의미)
        // measurement: 높을수록 안좋음 (스트레스/이상치) → 반전 필요
        Double wellnessMeasurement = 100.0 - measurementComponent;  // 반전: 높을수록 좋음
        Double riskScore = (diaryComponent * 0.5) + (wellnessMeasurement * 0.5);  // 높을수록 건강함

        // 6. 저장 또는 업데이트
        DailyRiskScore riskScoreEntity = riskScoreRepository
                .findByUserIdAndScoreDate(userId, date)
                .orElse(DailyRiskScore.builder()
                        .userId(userId)
                        .scoreDate(date)
                        .build());

        riskScoreEntity.updateScores(diaryComponent, measurementComponent, null, riskScore);
        riskScoreEntity.updateStats(measurementCount, anomalyCount);

        DailyRiskScore saved = riskScoreRepository.save(riskScoreEntity);

        log.info("일일 위험 점수 저장 완료 - riskScore: {}, diary: {}, measurement: {}",
                riskScore, diaryComponent, measurementComponent);

        return saved;
    }

    /**
     * 다이어리 없이 하루 점수 계산 (측정 데이터만 사용)
     * - 스케줄러에서 호출
     * - diary_component = null
     * - risk_score = 100 - measurement_component (wellness 개념)
     *
     * @param userId 사용자 ID
     * @param date   날짜
     * @return 저장된 DailyRiskScore
     */
    @Transactional
    public DailyRiskScore calculateWithoutDiary(Long userId, LocalDate date) {
        log.info("다이어리 없이 일일 위험 점수 계산 - userId: {}, date: {}", userId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // 1. diary_component = null (다이어리 없음)
        Double diaryComponent = null;

        // 2. measurement_component 계산
        Double measurementComponent = calculateMeasurementComponent(userId, startOfDay, endOfDay);

        // 3. 하루 총 측정 횟수
        Integer measurementCount = (int) measurementRepository
                .countByUserIdAndMeasuredAtBetween(userId, startOfDay, endOfDay);

        // 4. 하루 이상치 감지 횟수
        List<Measurement> anomalies = measurementRepository
                .findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(userId, startOfDay, endOfDay);
        Integer anomalyCount = anomalies.size();

        // 5. risk_score = wellness score (측정 데이터만 사용)
        // measurement는 높을수록 안좋음 → 반전시켜 wellness로
        Double riskScore = 100.0 - measurementComponent;  // 높을수록 건강함

        // 6. 저장 또는 업데이트
        DailyRiskScore riskScoreEntity = riskScoreRepository
                .findByUserIdAndScoreDate(userId, date)
                .orElse(DailyRiskScore.builder()
                        .userId(userId)
                        .scoreDate(date)
                        .build());

        riskScoreEntity.updateScores(diaryComponent, measurementComponent, null, riskScore);
        riskScoreEntity.updateStats(measurementCount, anomalyCount);

        DailyRiskScore saved = riskScoreRepository.save(riskScoreEntity);

        log.info("다이어리 없이 점수 저장 완료 - riskScore: {}, measurementComponent: {}",
                riskScore, measurementComponent);

        return saved;
    }

    /**
     * measurement_component 계산
     * = f(anomalyCount, avgStressIndex)
     */
    private Double calculateMeasurementComponent(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        // 1. 하루 동안 워치에서 이상치로 탐지된 측정 데이터 조회 (isAnomaly=true)
        List<Measurement> anomalies = measurementRepository
                .findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(userId, startOfDay, endOfDay);

        if (anomalies.isEmpty()) {
            return 0.0; // 이상치 없음
        }

        Integer anomalyCount = anomalies.size();

        // 2. 이상치 측정 데이터의 stressIndex 평균 계산
        Double avgStressIndex = anomalies.stream()
                .mapToDouble(Measurement::getStressIndex)
                .average()
                .orElse(0.0);

        // 3. measurement_component 계산
        // 이상치 1개당 2점, 평균 스트레스 지수의 0.4배
        // 정상인 기준: 이상치 10개 + 스트레스 80 = 20 + 32 = 52점 (보통)
        Double measurementComponent = (anomalyCount * 2.0) + (avgStressIndex * 0.4);

        // 4. 최대 100으로 제한
        measurementComponent = Math.min(measurementComponent, 100.0);

        log.debug("measurement_component 계산 - anomalyCount: {}, avgStressIndex: {}, result: {}",
                anomalyCount, avgStressIndex, measurementComponent);

        return measurementComponent;
    }

    /**
     * 일일 위험 점수 조회
     */
    @Transactional(readOnly = true)
    public DailyRiskScore getRiskScore(Long userId, LocalDate date) {
        return riskScoreRepository.findByUserIdAndScoreDate(userId, date)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜의 위험 점수가 없습니다"));
    }

    /**
     * 기간별 위험 점수 조회
     */
    @Transactional(readOnly = true)
    public List<DailyRiskScore> getRiskScores(Long userId, LocalDate startDate, LocalDate endDate) {
        return riskScoreRepository.findByUserIdAndScoreDateBetweenOrderByScoreDateAsc(
                userId, startDate, endDate);
    }

    /**
     * 최근 30일 위험 점수 조회
     */
    @Transactional(readOnly = true)
    public List<DailyRiskScore> getRecentRiskScores(Long userId) {
        return riskScoreRepository.findTop30ByUserIdOrderByScoreDateDesc(userId);
    }

    /**
     * 일주일간(오늘 기준) 평균 위험 점수 계산
     *
     * @param userId 사용자 ID
     * @return 일주일 평균 riskScore (0-100), 데이터 없으면 null
     */
    @Transactional(readOnly = true)
    public Double getWeeklyAverageRiskScore(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // 오늘 포함 7일

        List<DailyRiskScore> weeklyScores = riskScoreRepository
                .findByUserIdAndScoreDateBetweenOrderByScoreDateAsc(userId, sevenDaysAgo, today);

        if (weeklyScores.isEmpty()) {
            return null; // 데이터 없음
        }

        // riskScore 평균 계산 (null 제외)
        return weeklyScores.stream()
                .map(DailyRiskScore::getRiskScore)
                .filter(score -> score != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    /**
     * 오늘 위험 점수 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 오늘 점수 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean hasTodayScore(Long userId) {
        LocalDate today = LocalDate.now();
        return riskScoreRepository.findByUserIdAndScoreDate(userId, today).isPresent();
    }

    /**
     * 오늘 위험 점수 조회
     *
     * @param userId 사용자 ID
     * @return 오늘의 DailyRiskScore, 없으면 null
     */
    @Transactional(readOnly = true)
    public DailyRiskScore getTodayScore(Long userId) {
        LocalDate today = LocalDate.now();
        return riskScoreRepository.findByUserIdAndScoreDate(userId, today).orElse(null);
    }
}
