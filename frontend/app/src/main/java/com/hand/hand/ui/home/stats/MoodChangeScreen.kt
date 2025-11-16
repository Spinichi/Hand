// file: com/hand/hand/ui/home/stats/MoodChangeScreen.kt
package com.hand.hand.ui.home.stats

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily
import androidx.compose.ui.graphics.nativeCanvas

// ----- 공통 색(디자인 유지) -----
private val Brown80 = Color(0xFF4B2E1E)
private val MoodGreen = Color(0xFF9AB067)
private val TitleWhite = Color(0xFFFEFDFD)
private val CardWhite = Color(0xFFFFFFFF)
private val BadgeBrown = Color(0xFF4F3422)
private val LineGray = Color(0xFFD9D9D9)
private val CurveColor = Color(0xFF9AB067)

// ===== 원본 TeamAiDocument 스타일 그래프 (24h + 0h/12h/24h 레이블) =====
@Composable
fun StressLineChart(
    scores: List<Int>, // 24개
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4F3422),
    pointColor: Color = Color(0xFF815EFF),
    gridColor: Color = Color(0xFFE1D4CD),
) {
    Canvas(modifier = modifier) {
        if (scores.isEmpty()) return@Canvas

        val widthPerPoint = size.width / (scores.size - 1).coerceAtLeast(1)
        val heightScale = size.height / 100f

        // 가로 그리드
        val gridValues = listOf(0, 20, 40, 60, 80, 100)
        gridValues.forEach { value ->
            val y = size.height - (value * heightScale)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 4f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // 곡선
        val path = Path()
        scores.forEachIndexed { index, score ->
            val x = index * widthPerPoint
            val y = size.height - (score * heightScale)
            if (index == 0) path.moveTo(x, y)
            else {
                val prevX = (index - 1) * widthPerPoint
                val prevY = size.height - (scores[index - 1] * heightScale)
                val cpx1 = prevX + widthPerPoint / 2
                val cpy1 = prevY
                val cpx2 = prevX + widthPerPoint / 2
                val cpy2 = y
                path.cubicTo(cpx1, cpy1, cpx2, cpy2, x, y)
            }
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 8f))

        // 최고점 표시
        val maxScore = scores.maxOrNull() ?: 0
        scores.forEachIndexed { index, score ->
            if (score == maxScore) {
                val x = index * widthPerPoint
                val y = size.height - (score * heightScale)
                drawCircle(color = pointColor, radius = 12f, center = Offset(x, y))
            }
        }

        // x축 레이블 0h, 12h, 24h만 표시
        val labelIndices = listOf(0, 12, 23)
        val labelTexts = listOf("0h", "12h", "24h")
        labelIndices.forEachIndexed { i, index ->
            if (index < scores.size) {
                val x = index * widthPerPoint
                val canvas = drawContext.canvas
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#867E7A")
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = size.height * 0.09f
                    isFakeBoldText = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                canvas.nativeCanvas.drawText(labelTexts[i], x, size.height + size.height * 0.15f, paint)
            }
        }
    }
}

@Composable
private fun MoodChangeHistorySection(
    horizontalPadding: Dp,
    maxListHeight: Dp,
    scores: List<Int>,
    screenHeight: Dp,
    moodChangeCount: Int
) {
    // 반응형 기준값들
    val smallSpacer = screenHeight * 0.0125f      // 기존 12.dp 정도
    val chartTopSpacer = screenHeight * 0.02f     // 기존 screenHeight * 0.02f 유지 (반응형)
    val betweenChartAndCard = screenHeight * 0.04f // 기존 screenHeight * 0.04f
    val betweenCards = screenHeight * 0.0125f     // 기존 12.dp
    val cardVerticalPadding = screenHeight * 0.015f
    val innerHorizontalPadding = horizontalPadding // 원래 주신 horizontalPadding 사용

    // 폰트 크기 (원래 25.sp 정도였던 값들을 반응형으로 대체)
    val bigTitleFont = (screenHeight * 0.025f).value.sp   // 약 25.sp에 대응
    val cardTitleFont = (screenHeight * 0.0205f).value.sp // 섹션 타이틀(오늘의 스트레스 변화)
    val countFont = (screenHeight * 0.0205f).value.sp    // 작은 흰 카드 안의 텍스트
    val bigCardTextFont = (screenHeight * 0.0205f).value.sp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = innerHorizontalPadding)
    ) {
        Text(
            text = "오늘의 스트레스 변화",
            color = Brown80,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = cardTitleFont
        )

        Spacer(Modifier.height(smallSpacer))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxListHeight),
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f),
            userScrollEnabled = true,
            contentPadding = PaddingValues(bottom = screenHeight * 0.02f)
        ) {
            item { Spacer(modifier = Modifier.height(chartTopSpacer)) }

            // 감정 변화 그래프
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    StressLineChart(
                        scores = scores,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.20f) // 기존과 비슷한 비율 유지
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(betweenChartAndCard)) }

            // 감정 변화 큰 카드
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.075f), // 기존 56.dp에 대응하는 반응형 높이
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F4F2)),
                    shape = RoundedCornerShape(screenHeight * 0.025f), // 기존 20.dp-ish
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = innerHorizontalPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "감정 변화",
                            color = Brown80,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = bigCardTextFont
                        )

                        Card(
                            modifier = Modifier
                                .size(width = screenHeight * 0.095f, height = screenHeight * 0.055f), // 76x41 대응
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(screenHeight * 0.02f),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${moodChangeCount}회",
                                    color = Brown80,
                                    fontFamily = BrandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = countFont,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // 스트레스 최고점 큰 카드 + 작은 카드 (반응형)
            item {
                val maxScore = scores.maxOrNull() ?: 0
                val maxIndices = scores.mapIndexedNotNull { index, score -> if (score == maxScore) index else null }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F4F2)),
                    shape = RoundedCornerShape(screenHeight * 0.025f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = innerHorizontalPadding, vertical = cardVerticalPadding),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // 큰 카드 제목 (가운데)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "스트레스 최고점",
                                color = Brown80,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = bigTitleFont,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(smallSpacer))

                        // 작은 카드들 (동일 최고점이 여러개면 여러개 생성)
                        maxIndices.forEachIndexed { idx, index ->
                            val hourText = "${index}시"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(screenHeight * 0.055f), // 기존 41.dp 정도에 대응
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(screenHeight * 0.02f),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = innerHorizontalPadding * 0.5f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${maxScore}점 - ",
                                        color = Brown80,
                                        fontFamily = BrandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = bigCardTextFont,
                                    )
                                    Spacer(modifier = Modifier.width(screenHeight * 0.01f))
                                    Text(
                                        text = hourText,
                                        color = Brown80,
                                        fontFamily = BrandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = bigCardTextFont,
                                    )
                                }
                            }

                            // 작은 카드 간 간격(반응형)
                            if (idx != maxIndices.lastIndex) {
                                Spacer(modifier = Modifier.height(betweenCards))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(screenHeight * 0.08f)) }
        }
    }
}


// ===== 메인 스크린 =====
@Composable
fun MoodChangeScreen(
    onBack: () -> Unit = {},
    moodChangeCount: Int = 0,
    scores: List<Int> = List(24) { (0..100).random() } // 기본 24개
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val headerHeight: Dp = screenHeight * 0.20f
    val backSize: Dp = screenHeight * 0.06f
    val paddStart: Dp = screenWidth * 0.07f
    val paddTop: Dp = screenHeight * 0.05f
    val titleStartGap: Dp = 16.dp
    val titleSp = 24.sp
    val crestFromTop: Dp = 310.dp
    val arcHeight: Dp = 70.dp
    val sheetCorner: Dp = 28.dp
    val badgeSize: Dp = 56.dp
    val badgeIconSize: Dp = 24.dp
    val apexInsidePx = with(density) { arcHeight.toPx() * 0.25f }
    val apexInsideDp = with(density) { apexInsidePx.toDp() }
    val badgeTopOffset: Dp = crestFromTop + apexInsideDp - (badgeSize / 2)
    val historyTopGap = 98.dp
    val maxListHeight = (screenHeight - (crestFromTop + historyTopGap) - 48.dp).coerceAtLeast(140.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MoodGreen)
    ) {
        // 상단 헤더 (Box 방식으로 뒤로가기 절대 위치, 타이틀은 버튼 옆으로)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            // 뒤로가기 버튼을 절대 위치로 배치 (DiaryHeader 방식과 동일하게)
            Image(
                painter = painterResource(R.drawable.back_white_btn),
                contentDescription = "back",
                modifier = Modifier
                    .padding(start = paddStart, top = paddTop) // 위치 튜닝은 이 값만 조정하면 됨
                    .size(backSize) // 크기도 동일 변수로 통제
                    .align(Alignment.TopStart)
                    .clickable { onBack() }
            )

            // 타이틀을 뒤로가기 버튼 옆으로 배치
            Text(
                text = "오늘 감정 변화",
                color = TitleWhite,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = titleSp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = paddStart + backSize + 18.dp, // back 버튼 바로 옆
                        top = paddTop + (backSize / 4) // 세로로도 버튼과 자연스럽게 정렬
                    )
            )
        }

        val extraGap = (configuration.screenHeightDp.dp * 0.03f).coerceIn(70.dp, 80.dp)
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
        }

        // 하단 시트
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
                    cubicTo(w * 0.25f, 0f, w * 0.75f, 0f, w, ah)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path = path, color = CardWhite, style = Fill)
            }
        }

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

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = crestFromTop + historyTopGap)
                .fillMaxWidth()
        ) {
            MoodChangeHistorySection(
                horizontalPadding = paddStart,
                maxListHeight = maxListHeight,
                scores = scores,
                screenHeight = screenHeight,
                moodChangeCount = moodChangeCount
            )
        }
    }
}
