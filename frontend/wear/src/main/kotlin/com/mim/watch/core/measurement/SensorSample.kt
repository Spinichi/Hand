package com.mim.watch.core.measurement

/**
 * SensorCollector가 매 주기마다 만들어 services로 넘길 1회 샘플.
 * userId 같은 건 필요 없음(개인 워치에서만 동작).
 */
data class  SensorSample(
    val timestampMs: Long,      // 샘플 시각(밀리초), 보정/최근걸음 판정에 사용
    val heartRateBpm: Double?,      // 심박수, 없을 수 있으므로 null 허용
    val ibiMsList: List<Double>?,   // IBI(ms) 목록, (HRV 계산용 IBI(ms)
    val objectTempC: Double?,       // 피부/객체 온도(섭씨) , null 이면 점수 계산에서 제외
    val accelMagnitude: Double?,    // 가속도 세기 0~1 정규화 or g 단위 힘 단계는 가중치 0
    val accelX: Double?,            // 가속도 X (m/s²)
    val accelY: Double?,            // 가속도 Y (m/s²)
    val accelZ: Double?,            // 가속도 Z (m/s²)
    val totalSteps: Long?,          // 누적 걸음 수(옵션)
    val lastStepAtMs: Long?,        // 마지막 걸음 발생 시각(활동 보정용)
    val stepsPerMinute: Int?        // 분당 걸음 수 (SPM)
)
