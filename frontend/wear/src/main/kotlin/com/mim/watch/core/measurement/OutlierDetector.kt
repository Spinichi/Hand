package com.mim.watch.core.measurement
// 이상치(엣지값) 필터

object OutlierDetector {
    // 심박수가 사람의 생리 범위를 벗어나면 null 처리(계산에서 제외)
    fun sanitizeHeartRate(hr: Double?): Double? =
        hr?.takeIf { it in 30.0..220.0 }
    // 온도도 마찬가지(피부온도 대략범위). 벗어나면 null
    fun sanitizeTemp(t: Double?): Double? =
        t?.takeIf { it in 30.0..40.0 }
}
