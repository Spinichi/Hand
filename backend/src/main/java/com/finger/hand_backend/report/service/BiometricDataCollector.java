package com.finger.hand_backend.report.service;

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
 * - Baseline, 이상치(워치에서 탐지), 사용자 기본 정보 수집
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BiometricDataCollector {

    private final MeasurementRepository measurementRepository;
    private final BaselineRepository baselineRepository;
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
     * - 데이터가 없으면 기본값으로 채워서 반환 (FastAPI 422 에러 방지)
     */
    private Map<String, Object> getUserBaseline(Long userId) {
        Optional<Baseline> baselineOpt = baselineRepository.findByUserIdAndIsActiveTrue(userId);

        if (baselineOpt.isEmpty()) {
            log.warn("No active baseline found for user {}, returning default values", userId);
            return getDefaultBaseline();
        }

        Baseline baseline = baselineOpt.get();
        Map<String, Object> result = new HashMap<>();

        result.put("version", baseline.getVersion() != null ? baseline.getVersion() : 0);
        result.put("measurementCount", baseline.getMeasurementCount() != null ? baseline.getMeasurementCount() : 0);
        result.put("dataStartDate", baseline.getDataStartDate() != null ? baseline.getDataStartDate().toString() : "");
        result.put("dataEndDate", baseline.getDataEndDate() != null ? baseline.getDataEndDate().toString() : "");

        // HRV SDNN (FastAPI 스펙: min, max, avg)
        Map<String, Object> hrvSdnn = new HashMap<>();
        hrvSdnn.put("min", baseline.getHrvSdnnMin() != null ? baseline.getHrvSdnnMin() : 0.0);
        hrvSdnn.put("max", baseline.getHrvSdnnMax() != null ? baseline.getHrvSdnnMax() : 0.0);
        hrvSdnn.put("avg", baseline.getHrvSdnnMean() != null ? baseline.getHrvSdnnMean() : 0.0);
        result.put("hrvSdnn", hrvSdnn);

        // HRV RMSSD (FastAPI 스펙: min, max, avg)
        Map<String, Object> hrvRmssd = new HashMap<>();
        hrvRmssd.put("min", baseline.getHrvRmssdMin() != null ? baseline.getHrvRmssdMin() : 0.0);
        hrvRmssd.put("max", baseline.getHrvRmssdMax() != null ? baseline.getHrvRmssdMax() : 0.0);
        hrvRmssd.put("avg", baseline.getHrvRmssdMean() != null ? baseline.getHrvRmssdMean() : 0.0);
        result.put("hrvRmssd", hrvRmssd);

        // 심박수 (FastAPI 스펙: min, max, avg)
        Map<String, Object> heartRate = new HashMap<>();
        heartRate.put("min", baseline.getHeartRateMin() != null ? baseline.getHeartRateMin() : 0.0);
        heartRate.put("max", baseline.getHeartRateMax() != null ? baseline.getHeartRateMax() : 0.0);
        heartRate.put("avg", baseline.getHeartRateMean() != null ? baseline.getHeartRateMean() : 0.0);
        result.put("heartRate", heartRate);

        // 체온 (FastAPI 스펙: min, max, avg)
        Map<String, Object> objectTemp = new HashMap<>();
        objectTemp.put("min", baseline.getObjectTempMin() != null ? baseline.getObjectTempMin() : 0.0);
        objectTemp.put("max", baseline.getObjectTempMax() != null ? baseline.getObjectTempMax() : 0.0);
        objectTemp.put("avg", baseline.getObjectTempMean() != null ? baseline.getObjectTempMean() : 0.0);
        result.put("objectTemp", objectTemp);

        return result;
    }

    /**
     * 기본 Baseline 값 반환 (데이터가 없을 때)
     */
    private Map<String, Object> getDefaultBaseline() {
        Map<String, Object> result = new HashMap<>();
        result.put("version", 0);
        result.put("measurementCount", 0);
        result.put("dataStartDate", "");
        result.put("dataEndDate", "");

        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("min", 0.0);
        defaultStats.put("max", 0.0);
        defaultStats.put("avg", 0.0);

        result.put("hrvSdnn", new HashMap<>(defaultStats));
        result.put("hrvRmssd", new HashMap<>(defaultStats));
        result.put("heartRate", new HashMap<>(defaultStats));
        result.put("objectTemp", new HashMap<>(defaultStats));

        return result;
    }

    /**
     * 사용자 기본 정보 조회
     * - 데이터가 없거나 null이면 기본값으로 채워서 반환 (FastAPI 422 에러 방지)
     */
    private Map<String, Object> getUserInfo(Long userId) {
        Optional<IndividualUser> userOpt = individualUserRepository.findByUserId(userId);

        if (userOpt.isEmpty()) {
            log.warn("No individual user info found for user {}, returning default values", userId);
            return getDefaultUserInfo();
        }

        IndividualUser user = userOpt.get();
        Map<String, Object> result = new HashMap<>();

        result.put("age", user.getAge() != null ? user.getAge() : 0);
        result.put("gender", user.getGender() != null ? user.getGender().name() : "");
        result.put("job", user.getJob() != null ? user.getJob() : "");
        result.put("height", user.getHeight() != null ? user.getHeight() : 0);
        result.put("weight", user.getWeight() != null ? user.getWeight() : 0);
        result.put("disease", user.getDisease() != null ? user.getDisease() : "");
        result.put("residenceType", user.getResidenceType() != null ? user.getResidenceType() : "");

        return result;
    }

    /**
     * 기본 사용자 정보 반환 (데이터가 없을 때)
     */
    private Map<String, Object> getDefaultUserInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("age", 0);
        result.put("gender", "");
        result.put("job", "");
        result.put("height", 0);
        result.put("weight", 0);
        result.put("disease", "");
        result.put("residenceType", "");
        return result;
    }

    /**
     * 이상치 상세 정보 조회
     * - 워치에서 이상치로 탐지된 측정 데이터 조회 (isAnomaly=true)
     */
    private List<Map<String, Object>> getAnomalyDetails(Long userId, LocalDateTime start, LocalDateTime end) {
        // Measurement 테이블에서 isAnomaly=true인 데이터 직접 조회
        List<Measurement> anomalies = measurementRepository
                .findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(userId, start, end);

        if (anomalies.isEmpty()) {
            log.debug("No anomalies found for user {} from {} to {}", userId, start, end);
            return new ArrayList<>();
        }

        // 이상치 상세 정보 구성 (FastAPI 스펙에 맞춤)
        List<Map<String, Object>> anomalyDetails = new ArrayList<>();
        for (Measurement measurement : anomalies) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("detectedAt", measurement.getMeasuredAt().toString());  // measuredAt -> detectedAt (String)
            detail.put("measurementId", measurement.getId());  // 측정 ID 추가
            detail.put("stressIndex", measurement.getStressIndex());
            detail.put("stressLevel", measurement.getStressLevel());
            detail.put("heartRate", measurement.getHeartRate());
            detail.put("hrvSdnn", measurement.getHrvSdnn());
            detail.put("hrvRmssd", measurement.getHrvRmssd());
            // objectTemp는 FastAPI 스펙에 없으므로 제거

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
