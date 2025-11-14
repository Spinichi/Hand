// file: com/hand/hand/ui/model/Organization.kt
package com.hand.hand.ui.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.hand.hand.R
import com.hand.hand.ui.admin.sections.Mood // Mood enum 사용을 위해 import
import kotlinx.parcelize.Parcelize

/**
 * 그룹(조직) 표시용 공용 모델
 * averageScore: 0 ~ 100 (정수/실수 모두 허용)
 */
@Parcelize
data class Organization(
    val id: String,
    val name: String,
    val memberCount: Int,
    val averageScore: Float   // 0 ~ 100
) : Parcelable

/**
 * 다이얼로그 카드에 쓰일 무드 아이콘/배경색 + 라벨
// ... (OrgMoodUi 데이터 클래스 유지)
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

// -----------------------------------------------------------
// ✅ 추가: AdminHomeActivity.kt에서 Mood enum 타입 불일치 에러를 해결하기 위한 함수
//          이 함수를 AdminHomeActivity에서 'moodFromScore' 대신 사용합니다.
// -----------------------------------------------------------

/** 0~100 점수를 5단계 Mood enum으로 변환 */
fun scoreToMood(score: Int): Mood {
    val s = score.toFloat().coerceIn(0f, 100f)
    return when {
        s >= 80f -> Mood.GREAT
        s >= 60f -> Mood.HAPPY
        s >= 40f -> Mood.OKAY
        s >= 20f -> Mood.DOWN
        else     -> Mood.SAD
    }
}

// -----------------------------------------------------------
// ✅ 변경: 기존 'moodFromScore'와 같은 역할을 하며, 라벨만 반환하는 함수 (Organization.kt에 정의)
//          AdminHomeActivity.kt에서 moodFromScore(점수).label 대신 이 함수를 사용하도록 유도
// -----------------------------------------------------------

/**
 * AdminHomeActivity.kt에서 moodFromScore(score).label 대신 사용할 함수입니다.
 * 점수(Int)를 받아 라벨 문자열을 반환합니다.
 * @param avgScore 0~100 사이의 점수
 */
fun moodLabelFromScore(avgScore: Int): String {
    return scoreToLabel(avgScore.toFloat())
}

fun Organization.toOrgMoodUi(): OrgMoodUi {
// ... (기존 로직 유지)
    return when (val label = scoreToLabel(averageScore)) {
        "great" -> OrgMoodUi(
            moodIconRes = R.drawable.ic_great,
            moodBg = Color(0xFFFFF1C7),
            moodLabel = label
        )
        "happy" -> OrgMoodUi(
            moodIconRes = R.drawable.ic_happy,
            moodBg = Color(0xFFFFF1C7),
            moodLabel = label
        )
        "okay" -> OrgMoodUi(
            moodIconRes = R.drawable.ic_okay,
            moodBg = Color(0xFFE7DBFF),
            moodLabel = label
        )
        "down" -> OrgMoodUi(
            moodIconRes = R.drawable.ic_down,
            moodBg = Color(0xFFFFE1CC),
            moodLabel = label
        )
        else -> OrgMoodUi(
            moodIconRes = R.drawable.ic_sad,
            moodBg = Color(0xFFFFE1CC),
            moodLabel = "sad"
        )
    }
}

/**
 * 헤더용 작은 이모지
 * ic_mini_great, ic_mini_happy, ic_mini_okay, ic_mini_down, ic_mini_sad
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