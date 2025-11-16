package com.hand.hand.ui.model

/**
 * 스트레스 지수(0..100) -> 단계(1..5), 라벨 텍스트를 공통으로 계산해 주는 유틸.
 * ⭐ 스트레스 지수가 낮을수록 좋은 상태 (0=매우 편안, 100=고스트레스)
 *
 * 단계 정의 (스트레스 지수 기준 - 낮을수록 좋음)
 * 1단계 = 0~20   (Great) - 매우 편안
 * 2단계 = 21~40  (Happy) - 편안
 * 3단계 = 41~60  (Okay)  - 보통
 * 4단계 = 61~80  (Down)  - 스트레스
 * 5단계 = 81~100 (Sad)   - 고스트레스
 */

enum class MoodType(val level: Int, val label: String) {
    GREAT(1, "Great"),
    HAPPY(2, "Happy"),
    OKAY (3, "Okay"),
    DOWN (4, "Down"),
    SAD  (5, "Sad");
}

data class MoodInfo(
    val type: MoodType,
    val level: Int = type.level,     // 1..5
    val label: String = type.label   // "Great" 등
)

/** 스트레스 지수 → MoodInfo (낮을수록 좋음) */
fun moodFromScore(scoreRaw: Int): MoodInfo {
    val s = scoreRaw.coerceIn(0, 100)
    val type = when {
        s <= 20 -> MoodType.GREAT   // 0~20: 매우 편안
        s <= 40 -> MoodType.HAPPY   // 21~40: 편안
        s <= 60 -> MoodType.OKAY    // 41~60: 보통
        s <= 80 -> MoodType.DOWN    // 61~80: 스트레스
        else    -> MoodType.SAD     // 81~100: 고스트레스
    }
    return MoodInfo(type)
}

/** 캡션: "n단계 (Label)" 형태 */
fun moodCaption(scoreRaw: Int): String {
    val m = moodFromScore(scoreRaw)
    return "${m.level}단계 (${m.label})"
}
