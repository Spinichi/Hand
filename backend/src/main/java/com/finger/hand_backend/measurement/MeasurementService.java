package com.finger.hand_backend.measurement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finger.hand_backend.baseline.Baseline;
import com.finger.hand_backend.baseline.BaselineRepository;
import com.finger.hand_backend.measurement.dto.MeasurementRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Measurement Service
 * - 측정 데이터 저장 및 조회
 * - HRV 계산 (SDNN, RMSSD)
 * - 스트레스 지수 계산 (1-100)
 */
@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final BaselineRepository baselineRepository;
    private final ObjectMapper objectMapper;

    /**
     * 측정 데이터 저장
     * - IBI 배열로부터 HRV 계산
     * - 스트레스 지수 계산 (Baseline 있으면 개인화, 없으면 고정 임계값)
     *
     * @param userId  사용자 ID
     * @param request 측정 데이터 요청
     * @return 저장된 측정 데이터
     */
    @Transactional
    public Measurement save(Long userId, MeasurementRequest request) {
        // 1. IBI 배열로부터 HRV 계산
        List<Integer> ibiArray = request.getIbiArray();
        double hrvSdnn = calculateHrvSdnn(ibiArray);
        double hrvRmssd = calculateHrvRmssd(ibiArray);

        // 2. 활동 감지 (걸음 데이터가 있는 경우)
        String activityState = determineActivityState(request.getLastStepAt(), request.getMeasuredAt());
        double stepsPerMinute = calculateStepsPerMinute(userId, request.getTotalSteps(), request.getMeasuredAt());
        String measurementQuality = determineMeasurementQuality(activityState);

        // 3. 움직임 강도 계산 (가속도계 데이터가 있는 경우)
        Double movementIntensity = null;
        if (request.getAccelX() != null && request.getAccelY() != null && request.getAccelZ() != null) {
            movementIntensity = Math.sqrt(
                Math.pow(request.getAccelX(), 2) +
                Math.pow(request.getAccelY(), 2) +
                Math.pow(request.getAccelZ(), 2)
            );
        }

        // 4. Baseline 조회 (있으면 사용, 없으면 simple 계산)
        Optional<Baseline> baselineOpt = baselineRepository.findByUserIdAndIsActiveTrue(userId);

        // 5. 스트레스 지수 계산
        int stressIndex;
        int stressLevel;

        if (baselineOpt.isPresent()) {
            // Baseline 기반 개인화 계산
            Baseline baseline = baselineOpt.get();
            stressIndex = calculateBaselineStressIndex(
                hrvSdnn, hrvRmssd, request.getHeartRate(),
                request.getObjectTemp(), movementIntensity,
                activityState, baseline
            );
            stressLevel = getStressLevelFromBaseline(stressIndex, baseline);
        } else {
            // 고정 임계값 기반 계산 (Bootstrap)
            stressIndex = calculateSimpleStressIndex(
                hrvSdnn, hrvRmssd, request.getHeartRate(),
                request.getObjectTemp(), movementIntensity,
                activityState
            );
            stressLevel = getStressLevel(stressIndex);
        }

        // 6. IBI 배열을 JSON 문자열로 변환
        String ibiJson;
        try {
            ibiJson = objectMapper.writeValueAsString(ibiArray);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("IBI 배열 JSON 변환 실패", e);
        }

        // 7. Measurement 엔티티 생성 및 저장
        Measurement measurement = Measurement.builder()
                .userId(userId)
                .heartRate(request.getHeartRate())
                .ibiArray(ibiJson)
                .hrvSdnn(hrvSdnn)
                .hrvRmssd(hrvRmssd)
                .objectTemp(request.getObjectTemp())
                .ambientTemp(request.getAmbientTemp())
                .accelX(request.getAccelX())
                .accelY(request.getAccelY())
                .accelZ(request.getAccelZ())
                .stressIndex(stressIndex)
                .stressLevel(stressLevel)
                .totalSteps(request.getTotalSteps())
                .lastStepAt(request.getLastStepAt())
                .stepsPerMinute(stepsPerMinute)
                .activityState(activityState)
                .measurementQuality(measurementQuality)
                .measuredAt(request.getMeasuredAt())
                .build();

        return measurementRepository.save(measurement);
    }

    /**
     * HRV SDNN 계산
     * SDNN = IBI 배열의 표준편차
     *
     * @param ibiArray IBI 배열 (ms 단위)
     * @return HRV SDNN (ms)
     */
    private double calculateHrvSdnn(List<Integer> ibiArray) {
        if (ibiArray == null || ibiArray.size() < 5) {
            return 0.0;
        }

        // 평균 계산
        double mean = ibiArray.stream()
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);

        // 분산 계산
        double variance = ibiArray.stream()
                .mapToDouble(ibi -> Math.pow(ibi - mean, 2))
                .average()
                .orElse(0.0);

        // 표준편차 (SDNN)
        return Math.sqrt(variance);
    }

    /**
     * HRV RMSSD 계산
     * RMSSD = 연속된 IBI 차이의 제곱평균제곱근
     *
     * @param ibiArray IBI 배열 (ms 단위)
     * @return HRV RMSSD (ms)
     */
    private double calculateHrvRmssd(List<Integer> ibiArray) {
        if (ibiArray == null || ibiArray.size() < 5) {
            return 0.0;
        }

        // 연속된 IBI 차이 계산
        double sumSquaredDiffs = 0.0;
        for (int i = 0; i < ibiArray.size() - 1; i++) {
            int diff = ibiArray.get(i + 1) - ibiArray.get(i);
            sumSquaredDiffs += Math.pow(diff, 2);
        }

        // 제곱평균제곱근
        double meanSquaredDiff = sumSquaredDiffs / (ibiArray.size() - 1);
        return Math.sqrt(meanSquaredDiff);
    }

    /**
     * 간단한 스트레스 지수 계산 (Bootstrap용 - Baseline 없을 때)
     *
     * WESAD 검증 가중치 적용:
     * - SDNN: 30%, RMSSD: 30%, HR: 25%, Temp: 10%, Acc: 5%
     * - 활동 보정: WALKING 시 점수 × 0.4 (운동 영향 제거)
     *
     * @param hrvSdnn          HRV SDNN (ms)
     * @param hrvRmssd         HRV RMSSD (ms)
     * @param heartRate        심박수 (bpm)
     * @param objectTemp       피부 온도 (°C)
     * @param movementIntensity 움직임 강도
     * @param activityState    활동 상태 (STATIC | WALKING)
     * @return 스트레스 지수 (1-100)
     */
    private int calculateSimpleStressIndex(double hrvSdnn, double hrvRmssd, int heartRate,
                                           Double objectTemp, Double movementIntensity,
                                           String activityState) {
        // WESAD 데이터셋 기반 기준값
        final double BASELINE_SDNN = 285.6;
        final double STRESS_SDNN = 326.2;
        final double BASELINE_RMSSD = 372.5;
        final double STRESS_RMSSD = 411.5;
        final int BASELINE_HR = 70;
        final double BASELINE_TEMP = 33.5;
        final double BASELINE_MOVEMENT = 850.0;

        // 1. SDNN 점수 (0-100) - 30%
        double sdnnScore = 0.0;
        if (hrvSdnn < BASELINE_SDNN) {
            sdnnScore = (hrvSdnn / BASELINE_SDNN) * 30;
        } else if (hrvSdnn < STRESS_SDNN) {
            sdnnScore = 30 + ((hrvSdnn - BASELINE_SDNN) / (STRESS_SDNN - BASELINE_SDNN)) * 40;
        } else {
            sdnnScore = 70 + Math.min(((hrvSdnn - STRESS_SDNN) / 50) * 30, 30);
        }

        // 2. RMSSD 점수 (0-100) - 30%
        double rmssdScore = 0.0;
        if (hrvRmssd < BASELINE_RMSSD) {
            rmssdScore = (hrvRmssd / BASELINE_RMSSD) * 30;
        } else if (hrvRmssd < STRESS_RMSSD) {
            rmssdScore = 30 + ((hrvRmssd - BASELINE_RMSSD) / (STRESS_RMSSD - BASELINE_RMSSD)) * 40;
        } else {
            rmssdScore = 70 + Math.min(((hrvRmssd - STRESS_RMSSD) / 50) * 30, 30);
        }

        // 3. 심박수 점수 (0-100) - 25%
        double hrScore = 0.0;
        if (heartRate <= BASELINE_HR) {
            hrScore = 0;
        } else {
            hrScore = Math.min(((heartRate - BASELINE_HR) / 30.0) * 100, 100);
        }

        // 4. 체온 점수 (0-100) - 10%
        double tempScore = 0.0;
        if (objectTemp != null) {
            if (objectTemp <= BASELINE_TEMP) {
                tempScore = 0;
            } else {
                tempScore = Math.min(((objectTemp - BASELINE_TEMP) / 2.0) * 100, 100);
            }
        }

        // 5. 움직임 점수 (0-100) - 5%
        double accScore = 0.0;
        if (movementIntensity != null) {
            if (movementIntensity >= BASELINE_MOVEMENT) {
                accScore = Math.min(((movementIntensity - BASELINE_MOVEMENT) / 200.0) * 100, 100);
            }
        }

        // 가중 평균 (WESAD 가중치)
        double stressIndex = (sdnnScore * 0.30) +
                             (rmssdScore * 0.30) +
                             (hrScore * 0.25) +
                             (tempScore * 0.10) +
                             (accScore * 0.05);

        // 활동 보정: WALKING 시 60% 할인 (운동 영향 제거)
        if ("WALKING".equals(activityState)) {
            stressIndex *= 0.4;
        }

        // 1-100 범위로 제한
        return Math.max(1, Math.min(100, (int) Math.round(stressIndex)));
    }

    /**
     * 스트레스 지수를 5단계로 분류 (고정 임계값)
     *
     * @param stressIndex 스트레스 지수 (1-100)
     * @return 스트레스 단계 (1-5)
     */
    private int getStressLevel(int stressIndex) {
        if (stressIndex <= 20) return 1;  // 매우 편안
        if (stressIndex <= 40) return 2;  // 편안
        if (stressIndex <= 60) return 3;  // 보통
        if (stressIndex <= 80) return 4;  // 스트레스
        return 5;  // 고스트레스
    }

    /**
     * Z-score를 0-100 점수로 변환
     * - Z-score: 표준편차를 기준으로 한 상대적 위치
     * - -1σ 이하: 0점 (매우 편안)
     * - -1σ~0: 0-30점 (편안)
     * - 0~+1σ: 30-70점 (보통-스트레스)
     * - +1σ 이상: 70-100점 (고스트레스)
     *
     * @param zScore Z-score 값
     * @return 0-100 점수
     */
    private double convertZScoreToScore(double zScore) {
        if (zScore <= -1.0) {
            return 0;
        } else if (zScore <= 0) {
            return ((zScore + 1.0) / 1.0) * 30;
        } else if (zScore <= 1.0) {
            return 30 + (zScore / 1.0) * 40;
        } else {
            return 70 + Math.min(((zScore - 1.0) / 1.0) * 30, 30);
        }
    }

    /**
     * Baseline 기반 스트레스 지수 계산 (개인화)
     * - 사용자의 Baseline과 비교하여 스트레스 계산
     * - WESAD 검증 가중치: SDNN 30%, RMSSD 30%, HR 25%, Temp 10%, Acc 5%
     * - 활동 보정: WALKING 시 점수 × 0.4
     *
     * @param hrvSdnn          HRV SDNN (ms)
     * @param hrvRmssd         HRV RMSSD (ms)
     * @param heartRate        심박수 (bpm)
     * @param objectTemp       피부 온도 (°C)
     * @param movementIntensity 움직임 강도
     * @param activityState    활동 상태 (STATIC | WALKING)
     * @param baseline         사용자 Baseline
     * @return 스트레스 지수 (1-100)
     */
    private int calculateBaselineStressIndex(double hrvSdnn, double hrvRmssd, int heartRate,
                                             Double objectTemp, Double movementIntensity,
                                             String activityState, Baseline baseline) {
        // 1. SDNN 점수 (0-100) - 30%
        double sdnnZScore = (hrvSdnn - baseline.getHrvSdnnMean()) / baseline.getHrvSdnnStd();
        double sdnnScore = convertZScoreToScore(sdnnZScore);

        // 2. RMSSD 점수 (0-100) - 30%
        double rmssdZScore = (hrvRmssd - baseline.getHrvRmssdMean()) / baseline.getHrvRmssdStd();
        double rmssdScore = convertZScoreToScore(rmssdZScore);

        // 3. 심박수 점수 (0-100) - 25%
        double hrZScore = (heartRate - baseline.getHeartRateMean()) / baseline.getHeartRateStd();
        // 심박수는 높을수록 스트레스 (음수 Z-score는 0점 처리)
        double hrScore = (hrZScore <= 0) ? 0 : convertZScoreToScore(hrZScore);

        // 4. 체온 점수 (0-100) - 10%
        double tempScore = 0.0;
        if (objectTemp != null && baseline.getObjectTempMean() != null && baseline.getObjectTempStd() != null) {
            double tempZScore = (objectTemp - baseline.getObjectTempMean()) / baseline.getObjectTempStd();
            // 체온도 높을수록 스트레스 (음수 Z-score는 0점 처리)
            tempScore = (tempZScore <= 0) ? 0 : convertZScoreToScore(tempZScore);
        }

        // 5. 움직임 점수 (0-100) - 5%
        double accScore = 0.0;
        if (movementIntensity != null) {
            // 움직임은 고정 기준값 사용 (Baseline에 없음)
            final double BASELINE_MOVEMENT = 850.0;
            if (movementIntensity >= BASELINE_MOVEMENT) {
                accScore = Math.min(((movementIntensity - BASELINE_MOVEMENT) / 200.0) * 100, 100);
            }
        }

        // 가중 평균 (WESAD 가중치)
        double stressIndex = (sdnnScore * 0.30) +
                             (rmssdScore * 0.30) +
                             (hrScore * 0.25) +
                             (tempScore * 0.10) +
                             (accScore * 0.05);

        // 활동 보정: WALKING 시 60% 할인 (운동 영향 제거)
        if ("WALKING".equals(activityState)) {
            stressIndex *= 0.4;
        }

        // 1-100 범위로 제한
        return Math.max(1, Math.min(100, (int) Math.round(stressIndex)));
    }

    /**
     * Baseline 기반 스트레스 단계 분류
     * - Baseline의 임계값 사용
     *
     * @param stressIndex 스트레스 지수 (1-100)
     * @param baseline    사용자 Baseline
     * @return 스트레스 단계 (1-5)
     */
    private int getStressLevelFromBaseline(int stressIndex, Baseline baseline) {
        if (stressIndex <= baseline.getStressThresholdLow()) {
            return 1;  // 매우 편안
        } else if (stressIndex <= baseline.getStressThresholdMedium()) {
            return 2;  // 편안
        } else if (stressIndex <= baseline.getStressThresholdHigh()) {
            return 3;  // 보통
        } else if (stressIndex <= baseline.getStressThresholdHigh() + 15) {
            return 4;  // 스트레스
        } else {
            return 5;  // 고스트레스
        }
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

    // ========== 활동 감지 메서드 ==========

    /**
     * 활동 상태 판별
     * - STATIC: 최근 걸음이 10초 이상 없음 (신뢰도 높음)
     * - WALKING: 최근 걸음이 10초 이내 (신뢰도 낮음)
     *
     * @param lastStepAt  최근 걸음 시간
     * @param measuredAt  측정 시간
     * @return 활동 상태 (STATIC | WALKING)
     */
    private String determineActivityState(LocalDateTime lastStepAt, LocalDateTime measuredAt) {
        if (lastStepAt == null) {
            return "STATIC";
        }

        long secondsSinceLastStep = Duration.between(lastStepAt, measuredAt).getSeconds();
        return (secondsSinceLastStep < 10) ? "WALKING" : "STATIC";
    }

    /**
     * 분당 걸음수 계산
     * - 이전 측정과 비교하여 계산
     *
     * @param userId     사용자 ID
     * @param totalSteps 현재 누적 걸음수
     * @param measuredAt 현재 측정 시간
     * @return 분당 걸음수 (이전 데이터 없으면 0.0)
     */
    private double calculateStepsPerMinute(Long userId, Integer totalSteps, LocalDateTime measuredAt) {
        if (totalSteps == null) {
            return 0.0;
        }

        // 이전 측정 데이터 조회
        Optional<Measurement> previousOpt = measurementRepository
                .findTop1ByUserIdAndTotalStepsIsNotNullOrderByMeasuredAtDesc(userId);

        if (previousOpt.isEmpty()) {
            return 0.0;  // 이전 데이터 없음
        }

        Measurement previous = previousOpt.get();
        int stepsDiff = totalSteps - previous.getTotalSteps();
        long minutesDiff = Duration.between(previous.getMeasuredAt(), measuredAt).toMinutes();

        if (minutesDiff <= 0 || stepsDiff < 0) {
            return 0.0;  // 시간 역전 또는 재부팅 (걸음수 초기화)
        }

        return (double) stepsDiff / minutesDiff;
    }

    /**
     * 측정 품질 판별
     * - HIGH: STATIC 상태 (스트레스 측정 신뢰도 높음)
     * - LOW: WALKING 상태 (운동 영향으로 신뢰도 낮음)
     *
     * @param activityState 활동 상태
     * @return 측정 품질 (HIGH | LOW)
     */
    private String determineMeasurementQuality(String activityState) {
        return "STATIC".equals(activityState) ? "HIGH" : "LOW";
    }
}
