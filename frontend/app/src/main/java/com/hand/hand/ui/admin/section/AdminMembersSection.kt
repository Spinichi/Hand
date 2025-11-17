package com.hand.hand.ui.admin.sections

import com.hand.hand.ui.model.Organization
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.AiDocument.TeamAiDocumentActivity
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.Calendar

/* ---------- 데이터 모델 & 무드 정의 ---------- */

enum class Mood { GREAT, HAPPY, OKAY, DOWN, SAD }

data class GroupMember(
    val id: String,
    val name: String,
    val avgScore: Int,
    val note: String? = null
)

fun scoreToMood(score: Int): Mood {
    val s = score.toFloat().coerceIn(0f, 100f)
    return when {
        s >= 80f -> Mood.GREAT // 80~100
        s >= 60f -> Mood.HAPPY // 60~79
        s >= 40f -> Mood.OKAY  // 40~59
        s >= 20f -> Mood.DOWN  // 20~39
        else     -> Mood.SAD   // 0~19
    }
}

/* ---------- 섹션 컴포저블 ---------- */

@Composable
fun AdminMembersSection(
    horizontalPadding: Dp = 0.dp,
    members: List<GroupMember>,
    searchQuery: String,
    selectedMood: Mood?,
    onSelectMood: (Mood?) -> Unit,
    onMemberClick: (GroupMember) -> Unit, // ★ 부모에게 받은 클릭 이벤트를 사용할 것입니다.
    org: Organization
) {
    val context = LocalContext.current
    val Brown80 = Color(0xFF4B2E1E)
    val GrayBG = Color(0xFFF3EFEA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "그룹원 정보",
                color = Brown80,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BrandFontFamily,
                modifier = Modifier.weight(1f)
            )
            MoodTagRowCompact(
                selected = selectedMood,
                onSelect = onSelectMood
            )
        }

        val filtered = remember(members, selectedMood, searchQuery) {
            members
                .asSequence()
                .filter { m -> selectedMood == null || scoreToMood(m.avgScore) == selectedMood }
                .filter { m ->
                    val q = searchQuery.trim()
                    q.isEmpty() || m.name.contains(q, ignoreCase = true)
                }
                .sortedBy { it.avgScore } // <- 여기서 낮은 점수 순으로 정렬
                .toList()
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            filtered.forEach { member ->
                // ★ FIX: 하드코딩된 startActivity 대신, 부모에게 받은 onMemberClick을 호출합니다.
                MemberCard(member = member, onClick = { onMemberClick(member) })
            }

            if (filtered.isEmpty()) {
                Surface(
                    color = GrayBG,
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "해당 조건의 그룹원이 없어요",
                            color = Brown80.copy(alpha = 0.7f),
                            fontFamily = BrandFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/* ---------- 태그(이모지) 행 ---------- */
@Composable
private fun MoodTagRowCompact(
    selected: Mood?,
    onSelect: (Mood?) -> Unit
) {
    val pillShape = RoundedCornerShape(999.dp)

    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .clip(pillShape),
        color = Color.White,
        tonalElevation = 0.dp,
        shape = pillShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Mood.entries.forEach { mood ->
                val isSelected = selected == mood
                val iconRes = iconResFor(mood, isSelected)
                val selectedBg = selectedPillBgFor(mood)

                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) selectedBg else Color.Transparent)
                        .clickable { onSelect(if (isSelected) null else mood) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/** 선택 여부에 따라 일반/다크브라운 아이콘 매핑 */
@Composable
private fun iconResFor(mood: Mood, selected: Boolean): Int = when (mood) {
    Mood.SAD -> if (selected) R.drawable.ic_mini_sad_darkbrown else R.drawable.ic_mini_sad
    Mood.DOWN -> if (selected) R.drawable.ic_mini_down_darkbrown else R.drawable.ic_mini_down
    Mood.OKAY -> if (selected) R.drawable.ic_mini_okay_darkbrown else R.drawable.ic_mini_okay
    Mood.HAPPY -> if (selected) R.drawable.ic_mini_happy_darkbrown else R.drawable.ic_mini_happy
    Mood.GREAT -> if (selected) R.drawable.ic_mini_great_darkbrown else R.drawable.ic_mini_great
}

/** 무드별 선택 캡슐 배경색 */
private fun selectedPillBgFor(mood: Mood): Color = when (mood) {
    Mood.GREAT -> Color(0xFF9BB167)
    Mood.HAPPY -> Color(0xFFFFCE5C)
    Mood.OKAY -> Color(0xFFC0A091)
    Mood.DOWN -> Color(0xFFED7E1C)
    Mood.SAD -> Color(0xFFC2B1FF)
}

/* ---------- 점수 칩 색(배경/글자) ---------- */
private fun chipBgForScore(score: Int): Color = when {
    score >= 80 -> Color(0xFFF2F4EB)
    score >= 60 -> Color(0xFFFFF4E0)
    score >= 40 -> Color(0xFFF5F5F5)
    score >= 20 -> Color(0xFFFFEEE2)
    else -> Color(0xFFF6F1FF)
}

private fun chipTextColorForScore(score: Int): Color = when {
    score >= 80 -> Color(0xFF9BB167)
    score >= 60 -> Color(0xFFFFCE5C)
    score >= 40 -> Color(0xFF736B66)
    score >= 20 -> Color(0xFFED7E1C)
    else -> Color(0xFF8978E3)
}

/* ---------- MemberCard ---------- */
private object MemberEmojiDimens {
    val Icon = 44.dp
}

@Composable
fun MemberCard(
    member: GroupMember,
    onClick: () -> Unit
) {
    val Brown80 = Color(0xFF4B2E1E)

    // ✅ 수정: member.mood 대신 member.avgScore로 Mood 결정
    val mood = scoreToMood(member.avgScore)

    val moodIcon = when (mood) {
        Mood.GREAT -> R.drawable.ic_great
        Mood.HAPPY -> R.drawable.ic_happy
        Mood.OKAY -> R.drawable.ic_okay
        Mood.DOWN -> R.drawable.ic_down
        Mood.SAD -> R.drawable.ic_sad
    }

    val chipBg = chipBgForScore(member.avgScore)
    val chipText = chipTextColorForScore(member.avgScore)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(moodIcon),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(MemberEmojiDimens.Icon)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    color = Brown80,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BrandFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Surface(
                    color = chipBg,
                    contentColor = chipText,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "평균 ${member.avgScore} 점",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BrandFontFamily,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }

                if (!member.note.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = member.note,
                        color = Brown80.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = BrandFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.chevron_right),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}