package com.hand.hand.ui.model

import java.util.Calendar
import java.util.Date

/** 감정 변화 히스토리 1줄 데이터 */
data class MoodChangeRecord(
    val year: Int,
    val month: Int, // 1~12
    val day: Int,   // 1~31
    val count: Int, // N회
    val score: Int  // 0~100
)

/** 오늘 yyyy, M, d 구하기 (desugaring 없이 Calendar 사용) */
private fun todayYMD(now: Date = Date()): Triple<Int, Int, Int> {
    val cal = Calendar.getInstance().apply { time = now }
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1 // 0-based → 1~12
    val d = cal.get(Calendar.DAY_OF_MONTH)
    return Triple(y, m, d)
}

/** 리스트에 오늘 건을 moodChangeCount로 반영(있으면 교체, 없으면 추가) */
fun List<MoodChangeRecord>.withTodayCount(
    todayCount: Int,
    now: Date = Date(),
    defaultScoreIfNew: Int = 0 // 새로 추가될 때 점수 없으면 0으로
): List<MoodChangeRecord> {
    val (y, m, d) = todayYMD(now)
    var replaced = false
    val updated = map {
        if (it.year == y && it.month == m && it.day == d) {
            replaced = true
            it.copy(count = todayCount) // 점수는 기존값 유지
        } else it
    }
    return if (replaced) updated
    else updated + MoodChangeRecord(y, m, d, todayCount, defaultScoreIfNew)
}

/** 오늘 항목을 목록에서 제외 */
fun List<MoodChangeRecord>.excludeToday(now: Date = Date()): List<MoodChangeRecord> {
    val (y, m, d) = todayYMD(now)
    return filterNot { it.year == y && it.month == m && it.day == d }
}

/** 최신(연-월-일) 내림차순 정렬 */
fun List<MoodChangeRecord>.sortedDescByYMD(): List<MoodChangeRecord> =
    sortedWith(
        compareByDescending<MoodChangeRecord> { it.year }
            .thenByDescending { it.month }
            .thenByDescending { it.day }
    )

/**
 * 화면에서 바로 쓰기 좋은 파이프라인:
 *   1) 더미/백 데이터 불러오기
 *   2) 오늘 카운트 반영
 *   3) 오늘 항목은 목록에서 제외
 *   4) 날짜 내림차순 정렬
 */
object MoodChangeSource {
    fun sample(): List<MoodChangeRecord> = listOf(
        MoodChangeRecord(2025, 10, 28, 2, 60),
        MoodChangeRecord(2025, 10, 27, 5, 15),
        MoodChangeRecord(2025, 10, 26, 3, 82),
        MoodChangeRecord(2025, 10,  1, 3,  1),
        MoodChangeRecord(2025, 10, 30, 3, 30),
        MoodChangeRecord(2025, 10, 29, 3, 29),
        MoodChangeRecord(2025, 10, 20, 3, 20),
        MoodChangeRecord(2025, 10, 19, 3, 19),
    )

    /**
     * UI에서 쓰는 “히스토리 리스트” 제공 메서드.
     * 지금은 sample()을 쓰지만, 나중에 백 연동 시 여기만 API로 교체하면 됨.
     */
    fun historyForScreen(todayCount: Int, now: Date = Date()): List<MoodChangeRecord> {
        return sample()
            .withTodayCount(todayCount = todayCount, now = now, defaultScoreIfNew = 0)
            .excludeToday(now = now)
            .sortedDescByYMD()
    }
}
