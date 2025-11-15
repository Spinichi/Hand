package com.mim.watch.core.measurement

object StressLevelMapper {

    // 0 ~ 100 지수 1~5 레벨로 단순 구간화
    fun levelFromIndex(index: Double): Int = when {
        index < 21 -> 1
        index < 41 -> 2
        index < 61 -> 3
        index < 81 -> 4
        else -> 5
    }

    /**
     * 활동 보정: 최근 10초 내 걸음이 있으면 일시적 상승으로 보고 지수 0.4배
     *  - measuredAtMs: 샘플 시각
     *  - lastStepAtMs: 마지막 걸음 시각(없으면 null)
     */
    fun applyActivityDiscount(
        stressIndex: Double,
        measuredAtMs: Long,
        lastStepAtMs: Long?
    ): Double {
        val within10s = lastStepAtMs?.let { measuredAtMs - it <= 10_000 } == true
        return if (within10s) stressIndex * 0.4 else stressIndex
    }

    /**
     * ⭐ SPM 기반 활동 보정: SPM >= 100이면 운동 중으로 간주하여 지수 0.4배
     *  - stressIndex: 계산된 스트레스 지수
     *  - stepsPerMinute: 분당 걸음 수 (SPM)
     */
    fun applyActivityDiscountBySpm(
        stressIndex: Double,
        stepsPerMinute: Int?
    ): Double {
        val isExercising = (stepsPerMinute ?: 0) >= 100
        return if (isExercising) stressIndex * 0.4 else stressIndex
    }

    /**
     * ⭐ 가속도 기반 활동 보정: 움직임이 크면 운동/활동 중으로 간주하여 지수 0.4배
     *  - stressIndex: 계산된 스트레스 지수
     *  - accelMagnitude: 가속도 벡터 크기 (센서 raw 값, mg 단위)
     *  - threshold: 활동으로 간주할 가속도 임계값 (기본 5000.0 = 약 5g)
     *    - 정지/앉아있기: ~1000-2000 (1-2g, 중력 포함)
     *    - 걷기: ~2000-3000
     *    - 빠른 걷기/계단: ~3000-5000
     *    - 달리기/격렬한 운동: 5000+ (5g 이상)
     */
    fun applyActivityDiscountByAccel(
        stressIndex: Double,
        accelMagnitude: Double?,
        threshold: Double = 5000.0
    ): Double {
        val isActive = (accelMagnitude ?: 0.0) >= threshold
        return if (isActive) stressIndex * 0.4 else stressIndex
    }
}
