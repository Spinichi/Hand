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
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.offset

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
    scores: List<Int>,                // 기준 리스트 (없어도됨)
    avgScores: List<Int> = emptyList(),  // 평균값(검정)
    maxScores: List<Int> = emptyList(),  // 최고값(빨강)
    minScores: List<Int> = emptyList(),  // 최저값(파랑)
    frequencyScores: List<Int> = emptyList(), // ✅ 1. 스트레스 빈도 데이터 파라미터 추가
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4F3422),
    pointColor: Color = Color(0xFF815EFF),
    gridColor: Color = Color(0xFFE1D4CD),
) {


    Canvas(modifier = modifier) {
        // 기준 리스트(길이 계산용)
        val baseList = when {
            scores.isNotEmpty() -> scores
            frequencyScores.isNotEmpty() -> frequencyScores // ✅ 빈도 데이터도 길이 계산의 기준으로 추가
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

        // Y축 계산을 위한 스케일 (0-100점 기준)
        val maxScoreValue = listOf(scores, avgScores, maxScores, minScores)
            .flatMap { it }
            .maxOrNull() ?: 0

        val maxFrequencyAsScore = (frequencyScores.maxOrNull() ?: 0) * 4

        // 기본 100과 실제 데이터의 최댓값 중 더 큰 값을 선택
        val dynamicMaxY = 100.coerceAtLeast(maxScoreValue).coerceAtLeast(maxFrequencyAsScore)

        // 20 단위로 그리드 최댓값 올림 (e.g., 135 -> 140)
        val gridTopValue = if (dynamicMaxY % 20 == 0) {
            dynamicMaxY // 🎯 20으로 나누어 나머지가 0이면, 원래 값 그대로 사용 (e.g., 100 -> 100)
        } else {
            (dynamicMaxY / 20 + 1) * 20 // 🎯 나머지가 있으면, 기존 방식대로 올림 처리 (e.g., 135 -> 140)
        }

        // ===== 가로 그리드 =====
        // ✅ 2. Y축 계산을 위한 '동적' 스케일 계산
        val heightScale = drawableHeight / gridTopValue.toFloat()

        // ✅ 3. ===== 동적 가로 그리드 생성 및 그리기 =====
        val gridValues = List((gridTopValue / 20) + 1) { it * 20 } // e.g., [0, 20, ..., 120, 140]

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


        // ===== 스트레스 빈도 점(Dot) 그리기 (Y축 계산 로직은 동일) =====
        if (frequencyScores.isNotEmpty()) {
            val maxFrequencyValue = frequencyScores.maxOrNull()?.toFloat() ?: 1f
            val startColor = Color(0xFFC2B1FF)
            val endColor = Color(0xFFA187FF)

            frequencyScores.forEachIndexed { index, frequency ->
                if (frequency > 0) {
                    val x = index * widthPerPoint
                    // 빈도(0~25)를 점수(0~100)로 변환한 값을 사용. heightScale은 이미 동적으로 계산됨.
                    val y = topPadding + (drawableHeight - (frequency * 4f) * heightScale)

                    val fraction = (frequency / maxFrequencyValue).coerceIn(0f, 1f)
                    val currentColor = lerp(startColor, endColor, fraction)
                    val radius = 6f + (fraction * 14f)

                    drawCircle(color = currentColor, radius = radius, center = Offset(x, y))
                }
            }
        }


        // ===== 공통 곡선 함수 (Y축 계산 로직은 동일) =====
        fun drawCurve(values: List<Int>, color: Color) {
            if (values.size < 2) return
            val path = Path()
            values.forEachIndexed { index, score ->
                val x = index * widthPerPoint
                // score 값 사용. heightScale은 이미 동적으로 계산됨.
                val y = topPadding + (drawableHeight - score * heightScale)

                if (index == 0) path.moveTo(x, y)
                else {
                    val prevX = (index - 1) * widthPerPoint
                    val prevY = topPadding + (drawableHeight - values[index - 1] * heightScale)
                    path.cubicTo(prevX + widthPerPoint / 2, prevY, prevX + widthPerPoint / 2, y, x, y)
                }
            }
            drawPath(path = path, color = color, style = Stroke(width = 12f))
        }

        // 최저(파랑) → 평균(검정) → 최고(빨강) 순서로 그리기
        if (minScores.isNotEmpty()) drawCurve(minScores, Color(0xFF007BFF)) // 파랑
        if (avgScores.isNotEmpty()) drawCurve(avgScores, Color(0xFF000000)) // 검정
        if (maxScores.isNotEmpty()) drawCurve(maxScores, Color(0xFF4F3422)) // 빨강

        // 최고값 빨간 점(최고 곡선 기준)
        if (maxScores.isNotEmpty()) {
            val maxScore = maxScores.maxOrNull() ?: 0
            if (maxScore > 0){
                maxScores.forEachIndexed { index, score ->
                    if (score == maxScore) {
                        val x = index * widthPerPoint
                        val y = topPadding + (drawableHeight - score * heightScale)
                        drawCircle(
                            color = Color(0xFFEF8834),
                            radius = 20f,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        // (X축 레이블 그리는 부분은 기존 코드와 동일)
        val totalPoints = baseList.size
        val hours = listOf(0, 4, 8, 12, 16, 20, 24)
        val labelIndices = hours.map { (it * (totalPoints - 1) / 24).coerceIn(0, totalPoints - 1) }
        val labelTexts = hours.map { "${it}h" }

        labelIndices.forEachIndexed { i, index ->
            val x = index * widthPerPoint
            val canvas = drawContext.canvas
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#867E7A")
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = size.height * 0.09f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val labelY = size.height - bottomPadding / 4
            canvas.nativeCanvas.drawText(labelTexts[i], x, labelY, paint)
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
    frequencyStress: List<Int>, // ✅ 빈도 데이터
    screenHeight: Dp,
    moodChangeCount: Int,
    moodChangeTime : Int, // 최다 스트레스 시점
    maxStress: Int
) {
    // 반응형 기준값들
    val smallSpacer = screenHeight * 0.0125f      // 기존 12.dp 정도
    val chartTopSpacer = screenHeight * 0.02f     // 기존 screenHeight * 0.02f 유지 (반응형)
    val betweenChartAndCard = screenHeight * 0.0f // 기존 screenHeight * 0.04f
    val betweenCards = screenHeight * 0.0125f     // 기존 12.dp
    val cardVerticalPadding = screenHeight * 0.015f
    val innerHorizontalPadding = horizontalPadding // 원래 주신 horizontalPadding 사용
    val padincard = screenHeight * 0.015f

    // 폰트 크기 (원래 25.sp 정도였던 값들을 반응형으로 대체)
    val bigTitleFont = (screenHeight * 0.03f).value.sp   // 약 25.sp에 대응
    val cardTitleFont = (screenHeight * 0.0205f).value.sp // 섹션 타이틀(오늘의 스트레스 변화)
    val countFont = (screenHeight * 0.0205f).value.sp    // 작은 흰 카드 안의 텍스트
    val bigCardTextFont = (screenHeight * 0.03f).value.sp

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
                            maxScores = maxScores,
                            frequencyScores = frequencyStress, // ✅ 실제 빈도 데이터를 여기에 전달
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight * 0.20f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padincard),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(screenHeight * 0.03f, Alignment.CenterHorizontally)
                    // 각 아이템 사이 3% screenHeight 패딩, 전체 Row 가운데 정렬
                ) {
                    // 첫 번째 원 + 텍스트
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(screenHeight * 0.01f)
                                .background(color = Color(0xFFC2B1FF), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(screenHeight * 0.01f))
                        Text(
                            text = "스트레스 빈도",
                            color = Brown80,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenHeight * 0.016f).value.sp,
                            maxLines = 1
                        )
                    }

                    // 두 번째 원 + 텍스트
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(screenHeight * 0.01f)
                                .background(color = Color(0xFFEF8834), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(screenHeight * 0.01f))
                        Text(
                            text = "스트레스 최고점",
                            color = Brown80,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenHeight * 0.016f).value.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.0125f)) // 원과 카드 사이 간격
            }
            // 감정 변화 큰 카드
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.14f), // 두 줄 배치 가능하도록 높이 확장, 비율 유지
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F4F2)),
                    shape = RoundedCornerShape(screenHeight * 0.025f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = padincard),
                        verticalArrangement = Arrangement.SpaceEvenly // 두 세트 간 공간 균등
                    ) {
                        // 첫 번째 텍스트 + 카드
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = screenHeight * 0.015f),
                                text = "최다 스트레스 시점",
                                color = Brown80,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp
                            )

                            Card(
                                modifier = Modifier
                                    .height(screenHeight * 0.055f)
                                    .width(screenHeight * 0.155f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(screenHeight * 0.02f),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${moodChangeTime}시 - ${moodChangeTime + 1}시",
                                        color = Brown80,
                                        fontFamily = BrandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 23.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // 두 번째 텍스트 + 카드 (디자인 동일)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = screenHeight * 0.015f),
                                text = "최다",
                                color = Brown80,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp
                            )

                            Card(
                                modifier = Modifier
                                    .height(screenHeight * 0.055f)
                                    .width(screenHeight * 0.155f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(screenHeight * 0.02f),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${maxStress} 회",
                                        color = Brown80,
                                        fontFamily = BrandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 23.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
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
                                horizontal = padincard,
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
                        if (maxScore > 0) {
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

private val mockFrequencyStress = listOf(0, 0, 3, 5, 0, 8, 12, 15, 0, 0, 30, 22, 0, 0, 25, 18, 0, 0, 10, 0, 0, 0, 0, 0)

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
    var frequencyStressState by remember { mutableStateOf(List(24) { 0 }) }
    var minScoresState by remember { mutableStateOf(List(24) { 0 }) }
    var avgScoresState by remember { mutableStateOf(List(24) { 0 }) }
    var moodChangeCountState by remember { mutableStateOf(moodChangeCount) }
    var moodChangeTimeState by remember { mutableStateOf(0) }
    var maxStressState by remember { mutableStateOf(0) }
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

//                    frequencyStressState = data.frequencyStress ?: List(24) { 0 }
                    frequencyStressState = mockFrequencyStress

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
                text = "오늘 스트레스 변화",
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
                    moodChangeCount = moodChangeCountState,
                    moodChangeTime = moodChangeTimeState,
                    maxStress = maxStressState,
                    frequencyStress = frequencyStressState
                )
            }
        }
    }
}
