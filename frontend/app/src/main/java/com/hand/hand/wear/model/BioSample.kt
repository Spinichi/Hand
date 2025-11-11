package com.hand.hand.wear.model

/**
 * 센서 샘플 1개 (1초치 데이터)
 * Wear 앱으로부터 수신하는 데이터 구조
 */
data class BioSample(
    val timestampMs: Long,              // 샘플 시각
    val heartRateBpm: Float?,           // 심박수
    val ibiMsList: List<Float>?,        // IBI 배열
    val skinTempC: Float?,              // 피부 온도
    val movementIndex: Float?,          // 움직임 지수 (0~1)
    val totalSteps: Long?,              // 누적 걸음 수
    val stressIndex: Double?,           // 스트레스 지수 (0~100)
    val stressLevel: Int?               // 스트레스 레벨 (1~5)
)

/**
 * 10초치 샘플 묶음 (Wear 앱에서 전송되는 배치)
 */
data class BioSampleBatch(
    val deviceId: String,               // 워치 ID
    val batchTimestamp: Long,           // 배치 생성 시각
    val samples: List<BioSample>        // 10개 샘플
)