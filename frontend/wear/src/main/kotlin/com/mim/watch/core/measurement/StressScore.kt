package com.mim.watch.core.measurement

import com.mim.watch.core.baseline.BaseLine // 스트레스 지수 핵심 계싼기
import kotlin.math.max
import kotlin.math.min

object StressScore {        // 유틸 싱글턴

    // Z-Score 계산: (관측값-평균)/표준편차: null/0 std 방지
    private fun z(current: Double?, mean: Double, std: Double): Double? {
        if (current == null || std <= 0.0) return null
        return (current - mean) / std
    }

    // Z-Score -> 0~100 점수로 구간 매핑(연속)
    private fun zToScore(z: Double?): Double {
        if (z == null) return 0.0       // 데이터 없으면 가중합에 0으로 기여
        return when {
            z <= -1.0 -> 0.0
            z <= 0.0  -> (z + 1.0) * 30.0
            z <= 1.0  -> 30.0 + z * 40.0
            z <= 2.0  -> 70.0 + (z - 1.0) * 30.0
            else      -> 100.0
        }
    }

    // 최종 결과 묶음(지수/레벨/HRV 상세)
    data class Result(
        val stressIndex: Double, // 0~100
        val stressLevel: Int,    // 1~5
        val sdnn: Double?,
        val rmssd: Double?
    )

    // 상세 + BaseLine -> 지수/레벨 계산
    fun score(sample: SensorSample, baseline: BaseLine): Result {
        val (ts, hr, ibi, temp, _, _, lastStepAt) = sample  // 필요한 값만 구조분해

        // HRV 지표 계산(IBI가 충분할 때만)
        val sdnn  = ibi?.let { if (it.size >= 2) HrvCalculator.sdnn(it) else null }
        val rmssd = ibi?.let { if (it.size >= 2) HrvCalculator.rmssd(it) else null }

        // 각 항목을 Z -> 0 ~ 100 점수로 환산
        val sSdnn  = zToScore(sdnn?.let  { z(it, baseline.hrvSdnnMean, baseline.hrvSdnnStd) })
        val sRmssd = zToScore(rmssd?.let { z(it, baseline.hrvRmssdMean, baseline.hrvRmssdStd) })
        val sHr    = zToScore(hr?.let    { z(it, baseline.hrMean,      baseline.hrStd) })
        val sTemp  = zToScore(temp?.let  { z(it, baseline.objTempMean,  baseline.objTempStd) })
        val sAccel = 0.0 // 필요 시 가속도 점수 추가

        // 가중 합산(논문/설계 비율): HRV 0.6, HR 0.25, Temp 0.10, Accel 0.05
        var index = (sSdnn * 0.30) + (sRmssd * 0.30) + (sHr * 0.25) + (sTemp * 0.10) + (sAccel * 0.05)
        // 최근 걸음 할인(운동성 반영)
        index = StressLevelMapper.applyActivityDiscount(index, ts, lastStepAt)
        // 0~100으로 클램프
        index = min(100.0, max(0.0, index))

        // 지수 -> 레벨
        val level = StressLevelMapper.levelFromIndex(index)
        // 결과 반환
        return Result(index, level, sdnn, rmssd)
    }
}
