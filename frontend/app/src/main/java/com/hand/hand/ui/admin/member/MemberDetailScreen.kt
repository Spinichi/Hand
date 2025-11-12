package com.hand.hand.ui.admin.member

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.hand.hand.nav.NavBar   // ← 네브바 import

private val PurpleACA6E9 = Color(0xFFACA6E9)  // 상단 배경
private val SheetF7F4F2  = Color(0xFFF7F4F2)  // 시트 배경
private val GuideDash    = Color(0xFFE1D4CD)  // 점선 가이드
private val Placeholder  = Brown80.copy(alpha = 0.45f)

@Composable
fun MemberDetailScreen(
    orgId: String,
    memberId: String,
    onBack: () -> Unit = {},
    sheetTopOffset: Dp = 174.dp,
    sheetCorner: Dp = 28.dp,
    badgeOuterSize: Dp = 104.dp,
    badgeEmojiSize: Dp = 75.dp,
    // 네브바 콜백(필요 없으면 기본값으로 둠)
    onHomeClick: () -> Unit = {},
    onDiaryClick: () -> Unit = {},
    onDocumentClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCareClick: () -> Unit = {}
) {
    // 데이터
    val members = com.hand.hand.ui.model.OrgSource.members(orgId)
    val me = members.firstOrNull { it.id == memberId } ?: members.first()
    val name = me.name
    val avgScore = me.avgScore
    val week01 = com.hand.hand.ui.model.OrgSource.memberWeekScores01(orgId, memberId) // List<Float?>
    val adviceText = com.hand.hand.ui.model.OrgSource.memberAdvice(orgId, memberId)
    val initialSpecial = com.hand.hand.ui.model.OrgSource.memberSpecialNote(orgId, memberId)
    var specialNote by rememberSaveable { mutableStateOf(initialSpecial) }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val sheetTop = statusTop + sheetTopOffset

    // 네브바 높이 계산(네브바 구현과 동일한 비율 사용: screenHeight * 0.12f)
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val navBarHeight = screenHeightDp * 0.12f

    val sheetScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleACA6E9)
    ) {
        /* ───────── 헤더: 뒤로가기 · 이름 · 점수칩 ───────── */
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
                text = name,
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
                    .verticalScroll(sheetScroll)           // 시트 전체 스크롤
                    .padding(start = 20.dp, end = 20.dp, top = 86.dp, bottom = 18.dp)
                    .imePadding()
            ) {
                // 제목
                SectionTitle("감정 경향 그래프")
                Spacer(Modifier.height(14.dp))

                // 그리드 + 곡선
                GraphGridWithRoundedCurve(
                    height = 220.dp,
                    values01 = week01,
                    thickness = 10.dp,
                    smoothness = 1.0f
                )

                Spacer(Modifier.height(10.dp))
                WeekLabels()

                Spacer(Modifier.height(28.dp))

                // 감정 개선 조언
                SectionTitle("감정 개선 조언")
                Spacer(Modifier.height(10.dp))
                AdviceBubbleCard(
                    text = adviceText,
                    innerPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    minHeight = 96.dp,
                    textSizeSp = 16,
                    lineHeightSp = 24,
                    maxLines = Int.MAX_VALUE
                )

                Spacer(Modifier.height(18.dp))

                // 특이사항
                SectionTitle("특이사항")
                Spacer(Modifier.height(12.dp))
                NoteInputCard(
                    value = specialNote,
                    onValueChange = { specialNote = it },
                    placeholder = "특이사항을 입력하세요",
                    minHeight = 96.dp,
                    innerPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterStart,
                    textSizeSp = 16
                )

                // ⬇︎ 네브바가 가리지 않도록 여유 공간 추가
                Spacer(Modifier.height(navBarHeight + 24.dp))
            }
        }

        /* ───────── 이모지 배지(오버랩) ───────── */
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
                .align(Alignment.BottomCenter)   // 화면 하단 고정
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

/* ───────── 섹션 공통 타이틀 ───────── */
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

/* ───────── 요일 라벨 ───────── */
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

/* ───────── 감정 개선 조언 말풍선 카드 ───────── */
@Composable
private fun AdviceBubbleCard(
    text: String,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 20.dp),
    minHeight: Dp = 0.dp,
    corner: Dp = 30.dp,
    textSizeSp: Int = 16,
    lineHeightSp: Int = 28,
    maxLines: Int = Int.MAX_VALUE
) {
    Surface(
        color = White,
        shape = RoundedCornerShape(corner),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        Text(
            text = if (text.isEmpty()) " " else text,
            color = Brown80,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = textSizeSp.sp,
            lineHeight = lineHeightSp.sp,
            maxLines = maxLines,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/* ───────── 특이사항 입력 카드 ───────── */
@Composable
private fun NoteInputCard(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
    verticalAlignment: Alignment = Alignment.TopStart,
    textSizeSp: Int = 18
) {
    Surface(
        color = White,
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = minHeight)
                .padding(innerPadding),
            contentAlignment = verticalAlignment
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Brown80,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = textSizeSp.sp,
                    lineHeight = (textSizeSp + 8).sp
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Placeholder,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = textSizeSp.sp
                        )
                    }
                    inner()
                }
            )
        }
    }
}

/* ───────────────── 그래프 (그리드 + Catmull-Rom) ───────────────── */
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

/* ───────── 점수 칩 ───────── */
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

/* ───────── 칩 색상 규칙 ───────── */
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

/* ───────── 점수→이모지 ───────── */
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
