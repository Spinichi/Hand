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
        val (ts, hr, ibi, temp, _, accelX, accelY, accelZ, _, lastStepAt, spm) = sample

        // HRV 지표 계산(IBI가 충분할 때만)
        val sdnn  = ibi?.let { if (it.size >= 2) HrvCalculator.sdnn(it) else null }
        val rmssd = ibi?.let { if (it.size >= 2) HrvCalculator.rmssd(it) else null }

        // ⭐ 가속도 크기 계산 (X, Y, Z로부터 벡터 크기)
        val accelMagnitude = if (accelX != null && accelY != null && accelZ != null) {
            kotlin.math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
        } else null

        // 각 항목을 Z -> 0 ~ 100 점수로 환산
        val sSdnn  = zToScore(sdnn?.let  { z(it, baseline.hrvSdnnMean, baseline.hrvSdnnStd) })
        val sRmssd = zToScore(rmssd?.let { z(it, baseline.hrvRmssdMean, baseline.hrvRmssdStd) })
        val sHr    = zToScore(hr?.let    { z(it, baseline.hrMean,      baseline.hrStd) })
        val sTemp  = zToScore(temp?.let  { z(it, baseline.objTempMean,  baseline.objTempStd) })

        // ⭐ 가속도 점수: 높은 움직임 = 높은 스트레스 (간단한 매핑, Baseline 없이 절대값 기준)
        // 가속도 크기가 클수록 스트레스 증가 (0~20 m/s² 범위를 0~100으로 정규화)
        val sAccel = accelMagnitude?.let {
            val normalized = (it / 20.0) * 100.0  // 20 m/s²를 최대값으로 가정
            min(100.0, max(0.0, normalized))
        } ?: 0.0

        // 가중 합산(논문/설계 비율): HRV 0.6, HR 0.25, Temp 0.10, Accel 0.05
        var index = (sSdnn * 0.30) + (sRmssd * 0.30) + (sHr * 0.25) + (sTemp * 0.10) + (sAccel * 0.05)

        // ⭐ SPM 기반 활동 보정 (SPM >= 100이면 활동 중으로 간주)
        index = StressLevelMapper.applyActivityDiscountBySpm(index, spm)

        // 0~100으로 클램프
        index = min(100.0, max(0.0, index))

        // 지수 -> 레벨
        val level = StressLevelMapper.levelFromIndex(index)
        // 결과 반환
        return Result(index, level, sdnn, rmssd)
    }
}
