package com.hand.hand.api.Anomaly

/**
 * 측정 데이터 응답 DTO
 */
data class MeasurementResponse(
    val id: Long,
    val heartRate: Double?,
    val hrvSdnn: Double?,
    val hrvRmssd: Double?,
    val objectTemp: Double?,
    val ambientTemp: Double?,
    val accelX: Double?,
    val accelY: Double?,
    val accelZ: Double?,
    val movementIntensity: Double?,
    val stressIndex: Double?,
    val stressLevel: Int?,
    val isAnomaly: Boolean,
    val totalSteps: Int?,
    val stepsPerMinute: Double?,
    val measuredAt: String,  // LocalDateTime -> String
    val createdAt: String?   // LocalDateTime -> String
)

/**
 * 일일 이상치 응답 DTO
 */
data class DailyAnomalyResponse(
    val date: String,  // LocalDate -> String (yyyy-MM-dd)
    val anomalyCount: Int,
    val anomalies: List<MeasurementResponse>
)

/**
 * 주간 이상치 응답 DTO
 */
data class WeeklyAnomalyResponse(
    val startDate: String,  // LocalDate -> String
    val endDate: String,    // LocalDate -> String
    val totalAnomalyCount: Int,
    val dailyAnomalies: List<DailyAnomalyResponse>
)

/**
 * API 공통 응답 래퍼
 */
data class AnomalyApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
