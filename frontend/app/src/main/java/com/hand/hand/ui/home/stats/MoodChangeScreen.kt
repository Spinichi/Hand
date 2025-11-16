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
import com.hand.hand.api.Measurements.StressTodayManager
import com.hand.hand.api.Measurements.StressTodayData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


// ----- 공통 색(디자인 유지) -----
private val Brown80 = Color(0xFF4B2E1E)
private val MoodGreen = Color(0xFF9AB067)
private val TitleWhite = Color(0xFFFEFDFD)
private val CardWhite = Color(0xFFFFFFFF)
private val BadgeBrown = Color(0xFF4F3422)
private val LineGray = Color(0xFFD9D9D9)
private val CurveColor = Color(0xFF9AB067)

// ===== 3개 라인(최저/평균/최고) 그래프 =====
@Composable
fun StressLineChart(
    scores: List<Int>,                // 기준 리스트 (없어도 됨)
    avgScores: List<Int> = emptyList(),  // 평균값(검정)
    maxScores: List<Int> = emptyList(),  // 최고값(빨강)
    minScores: List<Int> = emptyList(),  // 최저값(파랑)
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4F3422),
    pointColor: Color = Color(0xFF815EFF),
    gridColor: Color = Color(0xFFE1D4CD),
) {
    Canvas(modifier = modifier) {
        // 기준 리스트(길이 계산용)
        val baseList = when {
            scores.isNotEmpty() -> scores
            avgScores.isNotEmpty() -> avgScores
            maxScores.isNotEmpty() -> maxScores
            minScores.isNotEmpty() -> minScores
            else -> emptyList()
        }
        if (baseList.isEmpty()) return@Canvas

        val widthPerPoint = size.width / (baseList.size - 1).coerceAtLeast(1)
        // 위/아래 여백 조금 두고 그릴 영역 높이 계산
        val topPadding = size.height * 0.08f         // 위쪽 8% 여백
        val bottomPadding = size.height * 0.16f      // 아래 16%는 x축/범례용
        val drawableHeight = size.height - topPadding - bottomPadding

        val heightScale = size.height / 100f

        // ===== 가로 그리드 =====
        val gridValues = listOf(0, 20, 40, 60, 80, 100)
        gridValues.forEach { value ->
            val y = topPadding + (drawableHeight - value * heightScale)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 4f,
                pathEffect = androidx.compose.ui.graphics.PathEffect
                    .dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // ===== 공통 곡선 함수 =====
        fun drawCurve(values: List<Int>, color: Color) {

            if (values.size < 2) return
            val path = Path()
            values.forEachIndexed { index, score ->
                val x = index * widthPerPoint
                val y = topPadding + (drawableHeight - score * heightScale)
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (index - 1) * widthPerPoint
                    val prevY = topPadding + (drawableHeight - values[index - 1] * heightScale)
                    val cpx1 = prevX + widthPerPoint / 2
                    val cpy1 = prevY
                    val cpx2 = prevX + widthPerPoint / 2
                    val cpy2 = y
                    path.cubicTo(cpx1, cpy1, cpx2, cpy2, x, y)
                }
            }
            drawPath(path = path, color = color, style = Stroke(width = 8f))
        }

        // 최저(파랑) → 평균(검정) → 최고(빨강) 순서로 그리기
        if (minScores.isNotEmpty()) drawCurve(minScores, Color(0xFF007BFF)) // 파랑
        if (avgScores.isNotEmpty()) drawCurve(avgScores, Color(0xFF000000)) // 검정
        if (maxScores.isNotEmpty()) drawCurve(maxScores, Color(0xFFFF3B30)) // 빨강

        // 최고값 빨간 점(최고 곡선 기준)
        if (maxScores.isNotEmpty()) {
            val maxScore = maxScores.maxOrNull() ?: 0
            maxScores.forEachIndexed { index, score ->
                if (score == maxScore) {
                    val x = index * widthPerPoint
                    val y = topPadding + (drawableHeight - score * heightScale)
                    drawCircle(
                        color = Color(0xFFFF3B30),
                        radius = 12f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        // ===== x축 레이블 (0h / 12h / 24h) =====
        val labelIndices = listOf(0, 12, baseList.size - 1)
        val labelTexts = listOf("0h", "12h", "24h")
        labelIndices.forEachIndexed { i, index ->
            if (index in baseList.indices) {
                val x = index * widthPerPoint
                val canvas = drawContext.canvas
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#867E7A")
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = size.height * 0.09f
                    isFakeBoldText = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val labelY = size.height - bottomPadding / 2f
                canvas.nativeCanvas.drawText(
                    labelTexts[i],
                    x,
                    labelY,
                    paint
                )
            }
        }
    }
}




@Composable
private fun MoodChangeHistorySection(
    horizontalPadding: Dp,
    maxListHeight: Dp,
    scores: List<Int>,
    avgScores: List<Int>,   // 평균
    maxScores: List<Int>,   // 최고
    minScores: List<Int>,   // 최저
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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxListHeight),
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f),
            userScrollEnabled = true,
            contentPadding = PaddingValues(bottom = screenHeight * 0.02f)
        ) {
            item {
                Text(
                    text = "오늘의 스트레스 변화",
                    color = Brown80,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = cardTitleFont
                )
                Spacer(Modifier.height(smallSpacer))
            }

            item { Spacer(modifier = Modifier.height(chartTopSpacer)) }

            // 감정 변화 그래프
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 그래프 자체
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        StressLineChart(
                            scores = scores,
                            avgScores = avgScores,
                            maxScores = maxScores,
                            minScores = minScores,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight * 0.20f)
                        )
                    }

                    // 그래프 아래 작은 레전드
                    Spacer(modifier = Modifier.height(screenHeight * 0.008f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = horizontalPadding),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val legendFont = (screenHeight * 0.015f).value  // 화면 비율에 맞는 작은 글씨

                        LineLegendItem(
                            color = Color(0xFFFF3B30),      // 빨강: 최고
                            text = "스트레스 최고 점수",
                            fontSize = legendFont
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LineLegendItem(
                            color = Color(0xFF000000),      // 검정: 평균
                            text = "스트레스 평균 점수",
                            fontSize = legendFont
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LineLegendItem(
                            color = Color(0xFF007BFF),      // 파랑: 최저
                            text = "스트레스 최저 점수",
                            fontSize = legendFont
                        )
                    }
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
                                .size(
                                    width = screenHeight * 0.095f,
                                    height = screenHeight * 0.055f
                                ), // 76x41 대응
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
                // maxScores가 비어있으면 기존 scores 기준으로 표시
                val sourceForMax = if (maxScores.isNotEmpty()) maxScores else scores
                val maxScore = sourceForMax.maxOrNull() ?: 0
                val maxIndices = sourceForMax.mapIndexedNotNull { index, score ->
                    if (score == maxScore) index else null
                }

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
                            .padding(
                                horizontal = innerHorizontalPadding,
                                vertical = cardVerticalPadding
                            ),
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

// 메인 스크린

@Composable
private fun LineLegendItem(
    color: Color,
    text: String,
    fontSize: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = Brown80,
            fontFamily = BrandFontFamily,
            fontSize = fontSize.sp
        )
    }
}



// 오늘 날짜 만드는 함수
private fun todayIsoDate(): String {
    // "2025-11-16" 같은 형식
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}
@Composable
fun MoodChangeScreen(
    onBack: () -> Unit = {},
    moodChangeCount: Int = 0,
    scores: List<Int> = List(24) { (0..100).random() } // 초기값(백업용)
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
    val crestFromTop: Dp = screenHeight * 0.28f
    val arcHeight: Dp = 70.dp
    val sheetCorner: Dp = 28.dp
    val badgeSize: Dp = 56.dp
    val badgeIconSize: Dp = 24.dp
    val apexFromTopPx = with(density) { arcHeight.toPx() * 0.25f }
    val apexFromTopDp = with(density) { apexFromTopPx.toDp() }
    val badgeTopOffset: Dp = crestFromTop + apexFromTopDp - (badgeSize / 2)
    val historyTopGap = screenHeight * 0.10f
    val maxListHeight =
        (screenHeight - (crestFromTop + historyTopGap) - 48.dp).coerceAtLeast(140.dp)

    // ==== 상태: API로 채울 값들 ====
    var maxScoresState by remember { mutableStateOf(List(24) { 0 }) }
    var minScoresState by remember { mutableStateOf(List(24) { 0 }) }
    var avgScoresState by remember { mutableStateOf(List(24) { 0 }) }
    var moodChangeCountState by remember { mutableStateOf(moodChangeCount) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ==== API 호출 (오늘 날짜) ====
    LaunchedEffect(Unit) {
        val today = todayIsoDate() // "2025-11-16" 형식

        withContext(Dispatchers.IO) {
            StressTodayManager.getTodayStress(
                date = today,
                onSuccess = { data: StressTodayData ->
                    // 시간(0~23시)에 맞게 배열 채우기
                    val maxList = MutableList(24) { -1 }
                    val minList = MutableList(24) { -1 }
                    val avgList = MutableList(24) { -1 }

                    data.hourlyStats.forEach { stat ->
                        val h = stat.hour
                        if (h in 0..23) {
                            val max = stat.maxStress?.roundToInt() ?: 0
                            val min = stat.minStress?.roundToInt() ?: 0
                            val avg = stat.avgStress?.roundToInt() ?: 0

                            maxList[h] = max
                            minList[h] = min
                            avgList[h] = avg

                        }
                    }

                    maxScoresState = maxList
                    minScoresState = minList
                    avgScoresState = avgList

                    // 감정 변화 횟수: 일단 anomalyCount 사용 (원하면 measurementCount 기준으로 바꿔도 됨)
                    moodChangeCountState = data.anomalyCount

                    isLoading = false
                    errorMessage = null
                },
                onFailure = { t ->
                    errorMessage = t.message
                    isLoading = false
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MoodGreen)
    ) {
        // 상단 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            Image(
                painter = painterResource(R.drawable.back_white_btn),
                contentDescription = "back",
                modifier = Modifier
                    .padding(start = paddStart, top = paddTop)
                    .size(backSize)
                    .align(Alignment.TopStart)
                    .clickable { onBack() }
            )

            Text(
                text = "오늘 감정 변화",
                color = TitleWhite,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = titleSp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = paddStart + backSize + 18.dp,
                        top = paddTop + (backSize / 4)
                    )
            )
        }
//        추후 화면 로직에 따라 반영 예정
//        val extraGap = (configuration.screenHeightDp.dp * 0.3f).coerceIn(40.dp, 60.dp)
        val extraGap = 30.dp;
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = paddTop + backSize + extraGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${moodChangeCountState}회",
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
            // 로딩/에러 처리 간단히
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Brown80)
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "데이터를 불러오지 못했어요.\n${errorMessage}",
                        color = Brown80,
                        textAlign = TextAlign.Center,
                        fontFamily = BrandFontFamily
                    )
                }
            } else {
                MoodChangeHistorySection(
                    horizontalPadding = paddStart,
                    maxListHeight = maxListHeight,
                    scores = scores, // 길이 기준용 백업
                    avgScores = avgScoresState,
                    maxScores = maxScoresState,
                    minScores = minScoresState,
                    screenHeight = screenHeight,
                    moodChangeCount = moodChangeCountState
                )
            }
        }
    }
}
