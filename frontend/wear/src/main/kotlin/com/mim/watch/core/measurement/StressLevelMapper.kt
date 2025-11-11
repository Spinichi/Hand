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
}
