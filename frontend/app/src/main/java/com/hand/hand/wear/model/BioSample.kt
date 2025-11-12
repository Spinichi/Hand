package com.hand.hand.wear.model

/**
 * 센서 샘플 1개 (1초치 데이터)
 * Wear 앱으로부터 수신하는 데이터 구조
 * 백엔드 API 필드명과 통일
 */
data class BioSample(
    val timestampMs: Long,              // 샘플 시각
    val heartRate: Float?,              // 심박수 (bpm)
    val hrvSdnn: Double?,               // HRV SDNN (ms)
    val hrvRmssd: Double?,              // HRV RMSSD (ms)
    val objectTemp: Float?,             // 피부 온도 (°C)
    val ambientTemp: Float?,            // 주변 온도 (°C)
    val accelX: Float?,                 // 가속도 X (m/s²)
    val accelY: Float?,                 // 가속도 Y (m/s²)
    val accelZ: Float?,                 // 가속도 Z (m/s²)
    val movementIntensity: Float?,      // 움직임 강도 (0~1)
    val stressIndex: Double?,           // 스트레스 지수 (0~100)
    val stressLevel: Int?,              // 스트레스 레벨 (1~5)
    val totalSteps: Long?,              // 누적 걸음 수
    val stepsPerMinute: Int?,           // 분당 걸음 수 (SPM)
    val isAnomaly: Boolean              // 이상치 여부 (최근 10개 대표값 중 레벨 4+ 가 5개 이상)
)

/**
 * 10초치 샘플 묶음 (Wear 앱에서 전송되는 배치)
 */
data class BioSampleBatch(
    val deviceId: String,               // 워치 ID
    val batchTimestamp: Long,           // 배치 생성 시각
    val samples: List<BioSample>        // 10개 샘플
)