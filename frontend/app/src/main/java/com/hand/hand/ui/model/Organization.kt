// file: com/hand/hand/ui/model/Organization.kt
package com.hand.hand.ui.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.hand.hand.R

/**
 * 그룹(조직) 표시용 공용 모델
 * averageScore: 0 ~ 100 (정수/실수 모두 허용)
 */
data class Organization(
    val id: String,
    val name: String,
    val memberCount: Int,
    val averageScore: Float   // 0 ~ 100
)

/**
 * 다이얼로그 카드에 쓰일 무드 아이콘/배경색 + 라벨
 *
 * 단계 정의 (0~100):
 *  1단계 = 80~100 (Great)
 *  2단계 = 60~79  (Happy)
 *  3단계 = 40~59  (Okay)
 *  4단계 = 20~39  (Down)
 *  5단계 = 0~19   (Sad)
 *
 * 이모지 (다이얼로그 전용):
 *  ic_great, ic_happy, ic_okay, ic_down, ic_sad
 *
 * 배경색: 기존 디자인 팔레트 유지(긍정/보통/부정 3계열)
 */
data class OrgMoodUi(
    @DrawableRes val moodIconRes: Int,
    val moodBg: Color,
    val moodLabel: String // great / happy / okay / down / sad
)

/** 0~100 점수를 5단계 라벨로 변환 */
private fun scoreToLabel(score: Float): String {
    val s = score.coerceIn(0f, 100f)
    return when {
        s >= 80f -> "great"
        s >= 60f -> "happy"
        s >= 40f -> "okay"
        s >= 20f -> "down"
        else     -> "sad"
    }
}

fun Organization.toOrgMoodUi(): OrgMoodUi {
    return when (val label = scoreToLabel(averageScore)) {
        "great" -> OrgMoodUi(
            moodIconRes = R.drawable.ic_great,
            moodBg = Color(0xFFFFF1C7), // 기존 '좋음' 계열
            moodLabel = label
        )
        "happy" -> OrgMoodUi(
            moodIconRes = R.drawable.ic_happy,
            moodBg = Color(0xFFFFF1C7), // 긍정 계열 유지
            moodLabel = label
        )
        "okay" -> OrgMoodUi(
            moodIconRes = R.drawable.ic_okay,
            moodBg = Color(0xFFE7DBFF), // 중간(보라톤) 유지
            moodLabel = label
        )
        "down" -> OrgMoodUi(
            moodIconRes = R.drawable.ic_down,
            moodBg = Color(0xFFFFE1CC), // 하락/주의 계열
            moodLabel = label
        )
        else -> OrgMoodUi(
            moodIconRes = R.drawable.ic_sad,
            moodBg = Color(0xFFFFE1CC), // 우울/부정 계열
            moodLabel = "sad"
        )
    }
}

/**
 * 헤더용 작은 이모지
 *  ic_mini_great, ic_mini_happy, ic_mini_okay, ic_mini_down, ic_mini_sad
 */
@DrawableRes
fun moodLabelToHeaderIcon(label: String): Int = when (label.lowercase()) {
    "great" -> R.drawable.ic_mini_great
    "happy" -> R.drawable.ic_mini_happy
    "okay"  -> R.drawable.ic_mini_okay
    "down"  -> R.drawable.ic_mini_down
    "sad"   -> R.drawable.ic_mini_sad
    else    -> R.drawable.ic_mini_okay
}
