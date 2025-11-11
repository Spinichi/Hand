package com.mim.watch.core.measurement

import kotlin.math.pow
import kotlin.math.sqrt

object HrvCalculator {
    /** SDNN: IBI(ms) 표준편차 */
    fun sdnn(ibi: List<Double>): Double {
        if (ibi.size < 2) return Double.NaN // 데이터가 모자라면 NoN 반환
        val mean = ibi.average()    // 평균 IBI
        val varSum = ibi.sumOf { (it - mean).pow(2) } // 분산 합계
        return sqrt(varSum / (ibi.size - 1))    // 표준편차 (표본)
    }

    /** RMSSD: 연속 IBI 차이 제곱 평균의 제곱근 */
    fun rmssd(ibi: List<Double>): Double {
        if (ibi.size < 2) return Double.NaN     // 데이터 부족 가드
        val diffs = ibi.zip(ibi.drop(1)) { a, b -> (b - a) }  // 연속 차이들
        val meanSq = diffs.map { it * it }.average()     // 제곱 평균
        return sqrt(meanSq) // 제곱근 → RMSSD
    }
}
