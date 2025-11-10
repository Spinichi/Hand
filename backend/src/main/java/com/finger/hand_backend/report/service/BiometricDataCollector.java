package com.finger.hand_backend.report.service;

import com.finger.hand_backend.anomaly.AnomalyDetection;
import com.finger.hand_backend.anomaly.AnomalyDetectionRepository;
import com.finger.hand_backend.baseline.Baseline;
import com.finger.hand_backend.baseline.BaselineRepository;
import com.finger.hand_backend.measurement.Measurement;
import com.finger.hand_backend.measurement.MeasurementRepository;
import com.finger.hand_backend.user.entity.IndividualUser;
import com.finger.hand_backend.user.repository.IndividualUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 생체 데이터 수집기
 * - 보고서 생성을 위한 생체 데이터 수집
 * - Baseline, 이상치 개수, 사용자 기본 정보만 수집
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BiometricDataCollector {

    private final MeasurementRepository measurementRepository;
    private final BaselineRepository baselineRepository;
    private final AnomalyDetectionRepository anomalyDetectionRepository;
    private final IndividualUserRepository individualUserRepository;

    /**
     * 생체 데이터 수집 (주간/월간 공통)
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜 (포함)
     * @param endDate   종료 날짜 (포함)
     * @return 생체 데이터 수집 결과
     */
    public BiometricDataResult collectBiometricData(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Collecting biometric data for user {} from {} to {}", userId, startDate, endDate);

        // 1. 사용자 베이스라인 조회
        Map<String, Object> userBaseline = getUserBaseline(userId);

        // 2. 이상치 데이터 조회 (측정 데이터 포함)
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        List<Map<String, Object>> anomalies = getAnomalyDetails(userId, start, end);

        // 3. 사용자 기본 정보 조회 (AI 분석 참고용)
        Map<String, Object> userInfo = getUserInfo(userId);

        return BiometricDataResult.builder()
                .userBaseline(userBaseline)
                .anomalies(anomalies)
                .userInfo(userInfo)
                .build();
    }

    /**
     * 사용자 베이스라인 조회
     */
    private Map<String, Object> getUserBaseline(Long userId) {
        Optional<Baseline> baselineOpt = baselineRepository.findByUserIdAndIsActiveTrue(userId);

        if (baselineOpt.isEmpty()) {
            log.warn("No active baseline found for user {}", userId);
            return new HashMap<>();
        }

        Baseline baseline = baselineOpt.get();
        Map<String, Object> result = new HashMap<>();

        result.put("version", baseline.getVersion());
        result.put("measurementCount", baseline.getMeasurementCount());
        result.put("dataStartDate", baseline.getDataStartDate());
        result.put("dataEndDate", baseline.getDataEndDate());

        // HRV SDNN
        Map<String, Object> hrvSdnn = new HashMap<>();
        hrvSdnn.put("mean", baseline.getHrvSdnnMean());
        hrvSdnn.put("std", baseline.getHrvSdnnStd());
        result.put("hrvSdnn", hrvSdnn);

        // HRV RMSSD
        Map<String, Object> hrvRmssd = new HashMap<>();
        hrvRmssd.put("mean", baseline.getHrvRmssdMean());
        hrvRmssd.put("std", baseline.getHrvRmssdStd());
        result.put("hrvRmssd", hrvRmssd);

        // 심박수
        Map<String, Object> heartRate = new HashMap<>();
        heartRate.put("mean", baseline.getHeartRateMean());
        heartRate.put("std", baseline.getHeartRateStd());
        result.put("heartRate", heartRate);

        // 체온
        Map<String, Object> objectTemp = new HashMap<>();
        objectTemp.put("mean", baseline.getObjectTempMean());
        objectTemp.put("std", baseline.getObjectTempStd());
        result.put("objectTemp", objectTemp);

        return result;
    }

    /**
     * 사용자 기본 정보 조회
     */
    private Map<String, Object> getUserInfo(Long userId) {
        Optional<IndividualUser> userOpt = individualUserRepository.findByUserId(userId);

        if (userOpt.isEmpty()) {
            log.warn("No individual user info found for user {}", userId);
            return new HashMap<>();
        }

        IndividualUser user = userOpt.get();
        Map<String, Object> result = new HashMap<>();

        result.put("age", user.getAge());
        result.put("gender", user.getGender().name());
        result.put("job", user.getJob());
        result.put("height", user.getHeight());
        result.put("weight", user.getWeight());
        result.put("disease", user.getDisease());

        return result;
    }

    /**
     * 이상치 상세 정보 조회
     * - 각 이상치 발생 시점의 스트레스 지수, 레벨, 심박수 등 포함
     */
    private List<Map<String, Object>> getAnomalyDetails(Long userId, LocalDateTime start, LocalDateTime end) {
        List<AnomalyDetection> anomalies = anomalyDetectionRepository
                .findByUserIdAndCreatedAtBetween(userId, start, end);

        if (anomalies.isEmpty()) {
            log.debug("No anomalies found for user {} from {} to {}", userId, start, end);
            return new ArrayList<>();
        }

        // 측정 ID 목록 추출
        List<Long> measurementIds = anomalies.stream()
                .map(AnomalyDetection::getMeasurementId)
                .collect(Collectors.toList());

        // 측정 데이터 일괄 조회
        Map<Long, Measurement> measurementMap = measurementRepository.findAllById(measurementIds)
                .stream()
                .collect(Collectors.toMap(Measurement::getId, m -> m));

        // 이상치 상세 정보 구성
        List<Map<String, Object>> anomalyDetails = new ArrayList<>();
        for (AnomalyDetection anomaly : anomalies) {
            Measurement measurement = measurementMap.get(anomaly.getMeasurementId());
            if (measurement == null) {
                log.warn("Measurement {} not found for anomaly {}", anomaly.getMeasurementId(), anomaly.getId());
                continue;
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put("detectedAt", anomaly.getCreatedAt());
            detail.put("measurementId", anomaly.getMeasurementId());
            detail.put("stressIndex", measurement.getStressIndex());
            detail.put("stressLevel", measurement.getStressLevel());
            detail.put("heartRate", measurement.getHeartRate());
            detail.put("hrvSdnn", measurement.getHrvSdnn());
            detail.put("hrvRmssd", measurement.getHrvRmssd());

            anomalyDetails.add(detail);
        }

        return anomalyDetails;
    }

    /**
     * 생체 데이터 수집 결과
     */
    @lombok.Data
    @lombok.Builder
    public static class BiometricDataResult {
        private Map<String, Object> userBaseline;
        private List<Map<String, Object>> anomalies;
        private Map<String, Object> userInfo;
    }
}
