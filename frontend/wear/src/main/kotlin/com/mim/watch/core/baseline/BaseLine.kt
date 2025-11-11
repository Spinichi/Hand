
package com.mim.watch.core.baseline

/**
 * 개인 기준선(평균·표준편차) 모델.
 * 워치 로컬(Room)에 저장·로드하여 사용.
 */
data class BaseLine(
    val hrvSdnnMean: Double,    // SDNN 평균
    val hrvSdnnStd: Double,     // SDNN 표준편차
    val hrvRmssdMean: Double,   // RMSSD 평균
    val hrvRmssdStd: Double,    // RMSSD 표준편차
    val hrMean: Double,         // HR 평균
    val hrStd: Double,          // HR 표준편차
    val objTempMean: Double,    // 피부온 평균
    val objTempStd: Double,     // 피부온 표준편차
    val version: Int = 1,       // 스키마/산출 방식 버전
    val isActive: Boolean = true    // 여러 버전을 둘 때 활성 플래그
)
