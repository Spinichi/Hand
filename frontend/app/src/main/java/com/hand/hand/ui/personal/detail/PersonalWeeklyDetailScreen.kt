// file: com/hand/hand/ui/personal/detail/PersonalWeeklyDetailScreen.kt
package com.hand.hand.ui.personal.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.nav.NavBar
import com.hand.hand.ui.model.PersonalReportSource
import com.hand.hand.ui.theme.*
import java.util.*

private val PurpleACA6E9 = Color(0xFFACA6E9)  // 상단 배경
private val SheetF7F4F2  = Color(0xFFF7F4F2)  // 시트 배경
private val GuideDash    = Color(0xFFE1D4CD)  // 점선 가이드

@Composable
fun PersonalWeeklyDetailScreen(
    year: Int,
    month: Int,           // 1..12
    weekIndex: Int,       // 1..4 (샘플 기준)
    onBack: () -> Unit = {},
    sheetTopOffset: Dp = 174.dp,
    sheetCorner: Dp = 28.dp,
    badgeOuterSize: Dp = 104.dp,
    badgeEmojiSize: Dp = 75.dp,
    // 네브바 콜백
    onHomeClick: () -> Unit = {},
    onDiaryClick: () -> Unit = {},
    onDocumentClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCareClick: () -> Unit = {}
) {
    // ── 데이터 조회 (월간→주간)
    val monthly = remember(year, month) { PersonalReportSource.reportOrNull(year, month) }
    val week = remember(monthly, weekIndex) { monthly?.weeks?.firstOrNull { it.weekIndex == weekIndex } }
    val weekTitle = "${month}월 ${weekIndex}주차"
    val day01 = week?.dailyScores ?: List(7) { null }
    val day01f: List<Float?> = remember(day01) { day01.map { it?.coerceIn(0,100)?.div(100f) } }
    val avgScore = week?.avgScore ?: 0
    val weeklySummary = week?.weeklySummary ?: ""
    val weeklyAdvice  = week?.weeklyAdvice  ?: ""   // ← alias (weeklySummary) 지원

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val sheetTop = statusTop + sheetTopOffset

    // 네브바 높이(기존 비율)
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val navBarHeight = screenHeightDp * 0.12f

    val sheetScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleACA6E9)
    ) {
        /* ───────── 헤더: 뒤로가기 · 제목 · 점수칩 ───────── */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusTop + 12.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Transparent,
                shape = CircleShape,
                onClick = onBack,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp
            ) {
                Image(
                    painter = painterResource(R.drawable.back_white_btn),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(47.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = weekTitle, // ← 이름 대신 "월·주차"
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
            Spacer(Modifier.width(10.dp))
            ScoreChip(score = avgScore)
        }

        /* ───────── 시트(흰 배경) ───────── */
        Surface(
            color = SheetF7F4F2,
            shape = RoundedCornerShape(topStart = sheetCorner, topEnd = sheetCorner),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = sheetTop)
                .align(Alignment.TopStart)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(sheetScroll) // 시트 전체 스크롤
                    .padding(start = 20.dp, end = 20.dp, top = 86.dp, bottom = 18.dp)
            ) {
                // 1) 감정 경향 그래프
                SectionTitle("감정 경향 그래프")
                Spacer(Modifier.height(14.dp))
                GraphGridWithRoundedCurve(
                    height = 220.dp,
                    values01 = day01f,
                    thickness = 10.dp,
                    smoothness = 1.0f
                )
                Spacer(Modifier.height(10.dp))
                WeekLabels()

                Spacer(Modifier.height(28.dp))

                // 2) 주간 요약 (display)
                SectionTitle("주간 요약")
                Spacer(Modifier.height(10.dp))
                DisplayBubbleCard(
                    text = weeklySummary,
                    minHeight = 96.dp
                )

                Spacer(Modifier.height(18.dp))

                // 3) 감정 개선 조언 (display)
                SectionTitle("감정 개선 조언")
                Spacer(Modifier.height(10.dp))
                DisplayBubbleCard(
                    text = weeklyAdvice,
                    minHeight = 96.dp
                )

                // 네브바 여유
                Spacer(Modifier.height(navBarHeight + 24.dp))
            }
        }

        /* ───────── 이모지 배지(주간 평균 점수 기준) ───────── */
        val iconRes = scoreToDialogIcon(avgScore)
        Surface(
            color = SheetF7F4F2,
            shape = CircleShape,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            modifier = Modifier
                .size(badgeOuterSize)
                .align(Alignment.TopCenter)
                .offset(y = sheetTop - (badgeOuterSize / 2))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(badgeEmojiSize)
                )
            }
        }

        /* ───────── 하단 네브바(오버레이) ───────── */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            NavBar(
                onHomeClick = onHomeClick,
                onDiaryClick = onDiaryClick,
                onDocumentClick = onDocumentClick,
                onProfileClick = onProfileClick,
                onCareClick = onCareClick
            )
        }
    }
}

/* ───────── 공통 컴포넌트 (디자인 동일) ───────── */

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Brown80,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = BrandFontFamily
    )
}

@Composable
private fun DisplayBubbleCard(
    text: String,
    minHeight: Dp = 0.dp,
    corner: Dp = 30.dp,
) {
    Surface(
        color = White,
        shape = RoundedCornerShape(corner),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        Text(
            text = if (text.isEmpty()) " " else text,
            color = Brown80,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun WeekLabels() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach {
            Text(
                text = it,
                color = Color(0xFF867E7A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BrandFontFamily
            )
        }
    }
}

@Composable
private fun GraphGridWithRoundedCurve(
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
    values01: List<Float?>,
    thickness: Dp = 10.dp,
    smoothness: Float = 1.0f
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 6.dp)
    ) {
        val w = size.width
        val h = size.height

        val dash = PathEffect.dashPathEffect(floatArrayOf(14f, 16f), 0f)
        val rows = 5
        repeat(rows) { i ->
            val y = h * (i + 1) / (rows + 1)
            drawLine(
                color = GuideDash,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 3f,
                pathEffect = dash
            )
        }

        val n = values01.size
        if (n < 2) return@Canvas

        val pts = buildList {
            values01.forEachIndexed { i, v ->
                if (v != null) add(
                    Offset(
                        x = w * i / (n - 1).coerceAtLeast(1),
                        y = h * (1f - v.coerceIn(0f, 1f))
                    )
                )
            }
        }
        if (pts.size < 2) return@Canvas

        val ext = listOf(pts.first()) + pts + listOf(pts.last())
        val path = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until ext.size - 2) {
                val p0 = ext[i - 1]; val p1 = ext[i]; val p2 = ext[i + 1]; val p3 = ext[i + 2]
                val c1 = Offset(p1.x + (p2.x - p0.x) / 6f * smoothness,
                    p1.y + (p2.y - p0.y) / 6f * smoothness)
                val c2 = Offset(p2.x - (p3.x - p1.x) / 6f * smoothness,
                    p2.y - (p3.y - p1.y) / 6f * smoothness)
                cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
            }
        }

        drawPath(
            path = path,
            color = Brown80,
            style = Stroke(
                width = thickness.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
private fun ScoreChip(score: Int, modifier: Modifier = Modifier) {
    Surface(
        color = chipBgForScore(score),
        contentColor = chipTextColorForScore(score),
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier.height(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "평균 ${score} 점",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 16.sp
            )
        }
    }
}

private fun chipBgForScore(score: Int): Color = when {
    score >= 80 -> Color(0xFFF2F4EB)
    score >= 60 -> Color(0xFFFFF4E0)
    score >= 40 -> Color(0xFFF5F5F5)
    score >= 20 -> Color(0xFFFFEEE2)
    else        -> Color(0xFFF6F1FF)
}
private fun chipTextColorForScore(score: Int): Color = when {
    score >= 80 -> Color(0xFF9BB167)
    score >= 60 -> Color(0xFFFFCE5C)
    score >= 40 -> Color(0xFF736B66)
    score >= 20 -> Color(0xFFED7E1C)
    else        -> Color(0xFF8978E3)
}

private fun scoreToDialogIcon(score: Int): Int {
    val s = score.coerceIn(0, 100)
    return when (s) {
        in 80..100 -> R.drawable.ic_great
        in 60..79  -> R.drawable.ic_happy
        in 40..59  -> R.drawable.ic_okay
        in 20..39  -> R.drawable.ic_down
        else       -> R.drawable.ic_sad
    }
}
