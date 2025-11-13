package com.hand.hand.api.Measurements

import com.hand.hand.wear.model.BioSample
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 측정 데이터 저장 요청 DTO
 */
data class MeasurementRequest(
    val heartRate: Double?,
    val objectTemp: Double?,
    val ambientTemp: Double?,
    val accelX: Double?,
    val accelY: Double?,
    val accelZ: Double?,
    val hrvSdnn: Double?,
    val hrvRmssd: Double?,
    val movementIntensity: Double?,
    val stressIndex: Double?,
    val stressLevel: Int?,
    val isAnomaly: Boolean,
    val totalSteps: Int?,
    val stepsPerMinute: Double?,
    val measuredAt: String  // ISO-8601 형식: "2025-01-13T10:30:00"
) {
    companion object {
        /**
         * BioSample을 MeasurementRequest로 변환
         */
        fun from(sample: BioSample): MeasurementRequest {
            // timestampMs를 ISO-8601 문자열로 변환 (yyyy-MM-dd'T'HH:mm:ss)
            // 워치 시간이 미래일 수 있으므로 현재 시각과 비교해서 더 작은 값 사용
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val sampleTime = Instant.ofEpochMilli(sample.timestampMs)
            val now = Instant.now()
            val actualTime = if (sampleTime.isAfter(now)) now else sampleTime

            val measuredAt = actualTime
                .atZone(ZoneId.systemDefault())
                .format(formatter)

            return MeasurementRequest(
                heartRate = sample.heartRate?.toDouble(),
                objectTemp = sample.objectTemp?.toDouble(),
                ambientTemp = sample.ambientTemp?.toDouble(),
                accelX = sample.accelX?.toDouble(),
                accelY = sample.accelY?.toDouble(),
                accelZ = sample.accelZ?.toDouble(),
                hrvSdnn = sample.hrvSdnn,
                hrvRmssd = sample.hrvRmssd,
                movementIntensity = sample.movementIntensity?.toDouble(),
                stressIndex = sample.stressIndex,
                stressLevel = sample.stressLevel,
                isAnomaly = sample.isAnomaly,
                totalSteps = sample.totalSteps?.toInt(),
                stepsPerMinute = sample.stepsPerMinute?.toDouble(),
                measuredAt = measuredAt
            )
        }
    }
}

/**
 * 측정 데이터 저장 응답 DTO
 */
data class MeasurementCreateResponse(
    val id: Long,
    val stressIndex: Double?,
    val stressLevel: Int?,
    val isAnomaly: Boolean
)

/**
 * 공통 응답 래퍼
 */
data class MeasurementResponse(
    val success: Boolean,
    val data: MeasurementCreateResponse?,
    val message: String?
)
