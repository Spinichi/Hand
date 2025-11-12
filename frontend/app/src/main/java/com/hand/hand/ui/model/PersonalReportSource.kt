package com.hand.hand.ui.model

import kotlin.math.roundToInt

data class WeeklyReport(
    val weekIndex: Int,                // 1..4
    val dailyScores: List<Int?>,       // 월~일 7개, null = 미기록
    val weeklySummary: String,
    val weeklyAdvice: String
) {
    val avgScore: Int by lazy {
        val v = dailyScores.filterNotNull().map { it.coerceIn(0, 100) }
        if (v.isEmpty()) 0 else v.average().roundToInt()
    }
    val hasData: Boolean by lazy { dailyScores.any { it != null } }
}

data class MonthlyReport(
    val year: Int,
    val month: Int,                    // 1..12
    val weeks: List<WeeklyReport>,
    val monthlySummary: String,
    val monthlyAdvice: String
) {
    val monthAvg: Int by lazy {
        val all = weeks.flatMap { it.dailyScores }.filterNotNull().map { it.coerceIn(0, 100) }
        if (all.isEmpty()) 0 else all.average().roundToInt()
    }
    /** 한 주라도 데이터가 있으면 true */
    val hasData: Boolean by lazy { weeks.any { it.hasData } }
}

object PersonalReportSource {

    // 샘플: 2025-10만 예시 데이터
    private val sample: Map<String, MonthlyReport> = mapOf(
        "2025-10" to MonthlyReport(
            year = 2025, month = 10,
            weeks = listOf(
                WeeklyReport(
                    1, listOf(82, 75, 90, 65, 70, null, 76),
                    weeklySummary = "업무 스트레스가 있었지만 주중 후반에 안정세.",
                    weeklyAdvice  = "수면 루틴 고정, 짧은 산책 10분 유지."
                ),
                WeeklyReport(
                    2, listOf(68, 60, null, 55, 62, 58, 64),
                    weeklySummary = "컨디션 기복이 있었음.",
                    weeklyAdvice  = "카페인 늦은 시간 섭취 줄이기."
                ),
                WeeklyReport(
                    3, listOf(40, 45, 52, 48, 50, null, 46),
                    weeklySummary = "중립 상태가 많았고 회복 필요.",
                    weeklyAdvice  = "호흡 4-7-8과 가벼운 스트레칭."
                ),
                WeeklyReport(
                    4, listOf(72, 80, 78, 82, 75, 70, 76),
                    weeklySummary = "대체로 양호, 주 후반 집중 잘 됨.",
                    weeklyAdvice  = "집중 블록 유지, 중간 휴식 타이머 활용."
                )
            ),
            monthlySummary = "10월 전반은 변동성이 있었지만 후반부 회복세.",
            monthlyAdvice  = "주간 루틴 고정 + 저강도 운동 주 3회 권장."
        )
    )

    /** 해당 (year, month)에 데이터가 있으면 MonthlyReport, 없으면 null */
    fun reportOrNull(year: Int, month: Int): MonthlyReport? =
        sample["%04d-%02d".format(year, month)]

    /** 해당 (year, month)에 데이터가 있는지 여부 */
    fun hasMonthly(year: Int, month: Int): Boolean =
        reportOrNull(year, month)?.hasData == true
}
