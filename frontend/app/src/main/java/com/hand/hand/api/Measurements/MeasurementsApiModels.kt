package com.hand.hand.api.Measurements

import com.hand.hand.wear.model.BioSample
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
            // 서버 시간 검증을 통과하기 위해 현재 시각보다 2초 전으로 설정
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("Asia/Seoul")
            }
            val now = System.currentTimeMillis()
            val sampleTime = sample.timestampMs

            // 워치 시간이 현재보다 미래거나 너무 가까우면 현재-2초 사용
            val actualTime = if (sampleTime > now - 2000) {
                now - 2000
            } else {
                sampleTime
            }

            val measuredAt = formatter.format(Date(actualTime))

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
