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

    // HRV용 역방향 점수 매핑 (높을수록 낮은 점수)
    private fun zToScoreInverted(z: Double?): Double {
        if (z == null) return 0.0
        return 100.0 - zToScore(z)  // 점수 반전
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

        // ⭐ 가속도 크기 계산 (활동 감지용)
        val accelMagnitude = if (accelX != null && accelY != null && accelZ != null) {
            kotlin.math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
        } else null

        // 각 항목을 Z -> 0 ~ 100 점수로 환산
        // ⭐ HRV(SDNN/RMSSD)는 높을수록 스트레스 낮음 → 점수 반전 사용
        val sSdnn  = zToScoreInverted(sdnn?.let  { z(it, baseline.hrvSdnnMean, baseline.hrvSdnnStd) })
        val sRmssd = zToScoreInverted(rmssd?.let { z(it, baseline.hrvRmssdMean, baseline.hrvRmssdStd) })
        val sHr    = zToScore(hr?.let    { z(it, baseline.hrMean,      baseline.hrStd) })
        val sTemp  = zToScore(temp?.let  { z(it, baseline.objTempMean,  baseline.objTempStd) })

        // 가중 합산: HRV 60%, HR 30%, Temp 10%
        // (가속도는 활동 감지용으로만 사용, 스트레스 지수에 직접 반영하지 않음)
        var index = (sSdnn * 0.30) + (sRmssd * 0.30) + (sHr * 0.30) + (sTemp * 0.10)

        // ⭐ SPM 기반 활동 보정 (SPM >= 100이면 운동 중으로 간주)
        index = StressLevelMapper.applyActivityDiscountBySpm(index, spm)

        // ⭐ 가속도 기반 활동 보정 (제자리 운동/활동 감지)
        index = StressLevelMapper.applyActivityDiscountByAccel(index, accelMagnitude)

        // 0~100으로 클램프
        index = min(100.0, max(0.0, index))

        // 지수 -> 레벨
        val level = StressLevelMapper.levelFromIndex(index)
        // 결과 반환
        return Result(index, level, sdnn, rmssd)
    }
}
