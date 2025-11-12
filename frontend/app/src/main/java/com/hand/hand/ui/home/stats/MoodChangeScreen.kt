// file: com/hand/hand/ui/home/stats/MoodChangeScreen.kt
package com.hand.hand.ui.home.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.model.MoodChangeRecord
import com.hand.hand.ui.model.MoodChangeSource
import com.hand.hand.ui.model.withTodayCount
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.Calendar
import java.util.Date

// ----- 공통 색(디자인 유지) -----
private val Brown80   = Color(0xFF4B2E1E)
private val MonthGray = Color(0xFFB0ADA9)
private val CardBig   = Color(0xFFF7F4F2)   // 큰 카드 배경
private val CardSmall = Color(0xFFFFFFFF)   // 작은 카드 배경

private val MoodGreen  = Color(0xFF9AB067) // 헤더/배경
private val TitleWhite = Color(0xFFFEFDFD) // 제목 & 본문 흰색
private val CardWhite  = Color(0xFFFFFFFF) // 시트(카드) 내부
private val BadgeBrown = Color(0xFF4F3422) // 중앙 배지

// ----- 점수 색 규칙 -----
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

// ===== 우측 원형 점수 인디케이터 =====
@Composable
private fun ScoreDonut(
    score: Int,
    size: Dp = 64.dp,
    stroke: Dp = 10.dp
) {
    val bg = chipBgForScore(score)
    val fg = chipTextColorForScore(score)
    val sweep = (score.coerceIn(0, 100) / 100f) * 360f
    val density = LocalDensity.current

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = with(density) { stroke.toPx() }
            drawArc(
                color = bg,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                color = fg,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${score}",
                color = Brown80,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            )
            Text(
                text = "점",
                color = Brown80,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}

// ===== 히스토리 1행 카드 =====
@Composable
private fun MoodHistoryItem(
    record: MoodChangeRecord,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            // Figma drop shadow 대략 매칭: Y=2, Blur=4, Black 10%
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x1A000000),
                spotColor   = Color(0x1A000000)
            )
            .fillMaxWidth()
            .background(CardBig, shape = RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⬅ 날짜 작은 카드 (흰색, radius 15)
        Column(
            modifier = Modifier
                .width(56.dp)
                .height(56.dp)
                .background(CardSmall, shape = RoundedCornerShape(15.dp))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 월: Bold 10sp, MonthGray
            Text(
                text = "${record.month}월",
                color = MonthGray,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(2.dp))
            // 일: 20sp, Brown80
            Text(
                text = "${record.day}일",
                color = Brown80,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        // 중앙: "N회" 32sp, Brown80
        Text(
            text = "${record.count}회",
            color = Brown80,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            modifier = Modifier.weight(1f)
        )

        // ➡ 우측: 점수 도넛
        ScoreDonut(score = record.score, size = 63.dp, stroke = 10.dp)
    }
}

// ===== 섹션 전체 =====
@Composable
private fun MoodChangeHistorySection(
    records: List<MoodChangeRecord>,
    horizontalPadding: Dp,
    maxListHeight: Dp
) {
    // 최신(연-월-일) 내림차순
    val sorted = remember(records) {
        records.sortedWith(
            compareByDescending<MoodChangeRecord> { it.year }
                .thenByDescending { it.month }
                .thenByDescending { it.day }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    ) {
        Text(
            text = "감정 변화 히스토리",
            color = Brown80,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxListHeight),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = true,
            contentPadding = PaddingValues(bottom = 20.dp) // 하단 여유(그림자 공간)
        ) {
            items(sorted) { rec ->
                MoodHistoryItem(record = rec)
            }
        }
    }
}

// ===== 오늘 날짜 구하기(이 파일 내부 전용) =====
private fun todayYMD(now: Date = Date()): Triple<Int, Int, Int> {
    val cal = Calendar.getInstance().apply { time = now }
    return Triple(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}

// ===== 메인 스크린(디자인 유지) =====
@Composable
fun MoodChangeScreen(
    onBack: () -> Unit = {},
    moodChangeCount: Int = 2,
) {
    val cfg = LocalConfiguration.current
    val density = LocalDensity.current

    val screenH = cfg.screenHeightDp.dp
    val screenW = cfg.screenWidthDp.dp

    // 헤더(디자인 유지)
    val headerHeight: Dp = screenH * 0.20f
    val backSize: Dp = screenH * 0.06f
    val paddStart: Dp = screenW * 0.07f
    val paddTop: Dp = screenH * 0.05f
    val titleStartGap: Dp = 16.dp
    val titleSp = 24.sp

    // 시트(아치) 파라미터 (확정값 유지)
    val crestFromTop: Dp = 310.dp
    val arcHeight: Dp = 70.dp
    val sheetCorner: Dp = 28.dp

    // 중앙 배지
    val badgeSize: Dp = 56.dp
    val badgeIconSize: Dp = 24.dp

    // 아치 정점 보정
    val apexInsidePx = with(density) { arcHeight.toPx() * 0.25f }
    val apexInsideDp = with(density) { apexInsidePx.toDp() }

    // 배지 top 오프셋
    val badgeTopOffset: Dp = crestFromTop + apexInsideDp - (badgeSize / 2)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MoodGreen)
    ) {
        // ── 상단 헤더 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .padding(start = paddStart, top = paddTop, end = paddStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Transparent,
                shape = CircleShape,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp
            ) {
                Image(
                    painter = painterResource(R.drawable.back_white_btn),
                    contentDescription = "back",
                    modifier = Modifier
                        .size(backSize)
                        .clickable { onBack() }
                )
            }
            Spacer(modifier = Modifier.width(titleStartGap))
            Text(
                text = "감정 변화 횟수",
                color = TitleWhite,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = titleSp
            )
        }

        // ── 중앙 타이포 ──
        val extraGap = (cfg.screenHeightDp.dp * 0.03f).coerceIn(70.dp, 80.dp)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = paddTop + backSize + extraGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${moodChangeCount}회",
                color = TitleWhite,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "오늘 감정 변화",
                color = TitleWhite,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }

        // ── 하단 흰 시트: Card + Canvas(상단 아치) ──
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = crestFromTop),
            shape = RoundedCornerShape(topStart = sheetCorner, topEnd = sheetCorner),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val ah = with(density) { arcHeight.toPx() }

                val path = Path().apply {
                    moveTo(0f, ah)
                    cubicTo(
                        w * 0.25f, 0f,
                        w * 0.75f, 0f,
                        w, ah
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path = path, color = CardWhite, style = Fill)
            }
        }

        // ── 중앙 배지 ──
        Box(
            modifier = Modifier
                .size(badgeSize)
                .align(Alignment.TopCenter)
                .offset(y = badgeTopOffset)
                .clip(CircleShape)
                .background(BadgeBrown),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mini_chart),
                contentDescription = "chart",
                modifier = Modifier.size(badgeIconSize)
            )
        }

        // ── 감정 변화 히스토리 섹션 ──
        val rawHistory = MoodChangeSource.sample() // 백 연동 전 더미

        // 오늘 카운트는 데이터에 반영하되…
        val historyWithToday: List<MoodChangeRecord> = remember(moodChangeCount) {
            rawHistory.withTodayCount(todayCount = moodChangeCount, defaultScoreIfNew = 0)
        }

        // …목록에서는 오늘을 숨김
        val (ty, tm, td) = todayYMD()
        val history: List<MoodChangeRecord> = remember(historyWithToday) {
            historyWithToday.filterNot { it.year == ty && it.month == tm && it.day == td }
        }

        val historyTopGap = 98.dp
        val maxListHeight =
            (screenH - (crestFromTop + historyTopGap) - 48.dp).coerceAtLeast(140.dp)

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = crestFromTop + historyTopGap)
                .fillMaxWidth()
        ) {
            MoodChangeHistorySection(
                records = history,
                horizontalPadding = paddStart,
                maxListHeight = maxListHeight
            )
        }
    }
}
