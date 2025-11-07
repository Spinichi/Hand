package com.hand.hand.ui.model

/**
 * 점수(0..100) -> 단계(1..5), 라벨 텍스트를 공통으로 계산해 주는 유틸.
 * 단계 정의
 * 1단계 = 80~100 (Great)
 * 2단계 = 60~79  (Happy)
 * 3단계 = 40~59  (Okay)
 * 4단계 = 20~39  (Down)
 * 5단계 = 0~19   (Sad)
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

/** 점수 → MoodInfo */
fun moodFromScore(scoreRaw: Int): MoodInfo {
    val s = scoreRaw.coerceIn(0, 100)
    val type = when {
        s >= 80 -> MoodType.GREAT
        s >= 60 -> MoodType.HAPPY
        s >= 40 -> MoodType.OKAY
        s >= 20 -> MoodType.DOWN
        else    -> MoodType.SAD
    }
    return MoodInfo(type)
}

/** 캡션: "n단계 (Label)" 형태 */
fun moodCaption(scoreRaw: Int): String {
    val m = moodFromScore(scoreRaw)
    return "${m.level}단계 (${m.label})"
}
