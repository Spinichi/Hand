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
import androidx.compose.ui.platform.LocalContext
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.round
import kotlin.math.ceil
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.core.content.res.ResourcesCompat
import android.graphics.Typeface
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
    scores: List<Int>,
    avgScores: List<Int> = emptyList(),
    maxScores: List<Int> = emptyList(),
    minScores: List<Int> = emptyList(),
    frequencyScores: List<Int> = emptyList(),
    frequencyMax: Int? = null,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4F3422),
    pointColor: Color = Color(0xFF815EFF),
    gridColor: Color = Color(0xFFE1D4CD),
) {
    val density = LocalDensity.current
    val baseTextPx = with(density) { 14.sp.toPx() }
    // BrandFontFamily가 없다면 기본 폰트로 대체하세요
    val brandTypeface = Typeface.DEFAULT_BOLD

    var hoveredIndex by remember { mutableStateOf(-1) }
    var hoveredKind by remember { mutableStateOf("frequency") }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    hoveredIndex = -1
                }
            }
            .pointerInput(scores, frequencyScores, maxScores, minScores) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.first().position
                        val sizeWidth = size.width
                        val sizeHeight = size.height

                        if (event.changes.first().pressed) {
                            val topPaddingPx = sizeHeight * 0.08f
                            val bottomPaddingPx = sizeHeight * 0.16f
                            val drawableHeight = sizeHeight - topPaddingPx - bottomPaddingPx

                            val maxScoreValue = listOf(scores, maxScores).flatMap { it }.maxOrNull() ?: 0
                            val dynamicMaxY = 100.coerceAtLeast(maxScoreValue)

                            // 그리기 로직과 동일하게: 100 이하면 100, 초과하면 그 값 자체 사용
                            val gridTopValue = if (dynamicMaxY <= 100) 100 else dynamicMaxY

                            val heightScale = drawableHeight / gridTopValue.toFloat()
                            // ---------------------------------------------------------

                            val baseList = when {
                                scores.isNotEmpty() -> scores
                                frequencyScores.isNotEmpty() -> frequencyScores
                                avgScores.isNotEmpty() -> avgScores
                                maxScores.isNotEmpty() -> maxScores
                                minScores.isNotEmpty() -> minScores
                                else -> emptyList()
                            }

                            if (baseList.isEmpty()) {
                                hoveredIndex = -1
                                continue
                            }

                            // ✅ [수정 1] 터치 계산: 24시간 기준 (24등분)
                            val widthPerPoint = sizeWidth / 24f

                            var bestIndex = -1
                            var bestKind = "frequency"
                            var bestDist = Float.MAX_VALUE

                            if (frequencyScores.isNotEmpty()) {
                                val maxFreqLocal = frequencyMax?.toFloat()
                                    ?: frequencyScores.maxOrNull()?.toFloat()
                                    ?: 1f
                                val safeMax = if (maxFreqLocal == 0f) 1f else maxFreqLocal

                                frequencyScores.forEachIndexed { i, freq ->
                                    if (freq > 0) {
                                        val x = i * widthPerPoint
                                        val relativeScore = (freq / safeMax) * 100f
                                        val y = topPaddingPx + (drawableHeight - relativeScore * heightScale)

                                        val dx = pos.x - x
                                        val dy = pos.y - y
                                        val dist = dx * dx + dy * dy
                                        if (dist < bestDist) {
                                            bestDist = dist
                                            bestIndex = i
                                            bestKind = "frequency"
                                        }
                                    }
                                }
                            }

                            fun checkList(list: List<Int>, kind: String) {
                                if (list.isEmpty()) return
                                list.forEachIndexed { i, value ->
                                    if (value < 0) return@forEachIndexed
                                    val x = i * widthPerPoint
                                    val y = topPaddingPx + (drawableHeight - (value * heightScale))
                                    val dx = pos.x - x
                                    val dy = pos.y - y
                                    val dist = dx * dx + dy * dy
                                    if (dist < bestDist) {
                                        bestDist = dist
                                        bestIndex = i
                                        bestKind = kind
                                    }
                                }
                            }
                            checkList(maxScores, "max")
                            checkList(avgScores, "avg")
                            checkList(minScores, "min")

                            val thresholdPx = with(density) { 24.dp.toPx() }
                            if (bestIndex >= 0 && bestDist <= thresholdPx * thresholdPx) {

                                // ✅ [수정 완료] 터치된 종류(bestKind)에 맞는 데이터가 0보다 큰지 확인
                                val shouldSelect = when (bestKind) {
                                    "frequency" -> (frequencyScores.getOrNull(bestIndex) ?: 0) > 0
                                    "max" -> (maxScores.getOrNull(bestIndex) ?: 0) > 0
                                    "avg" -> (avgScores.getOrNull(bestIndex) ?: 0) > 0
                                    "min" -> (minScores.getOrNull(bestIndex) ?: 0) > 0
                                    else -> false
                                }

                                if (shouldSelect) {
                                    hoveredIndex = bestIndex
                                    hoveredKind = bestKind
                                } else {
                                    hoveredIndex = -1
                                }
                            } else {
                                hoveredIndex = -1
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizeWidth = size.width
            val topPadding = size.height * 0.08f
            val bottomPadding = size.height * 0.16f
            val drawableHeight = size.height - topPadding - bottomPadding

            val maxScoreValue = listOf(scores, maxScores).flatMap { it }.maxOrNull() ?: 0
            val dynamicMaxY = 100.coerceAtLeast(maxScoreValue)
            val gridTopValue = if (dynamicMaxY % 20 == 0) dynamicMaxY else (dynamicMaxY / 20 + 1) * 20
            val heightScale = drawableHeight / gridTopValue.toFloat()

            val baseList = when {
                scores.isNotEmpty() -> scores
                frequencyScores.isNotEmpty() -> frequencyScores
                avgScores.isNotEmpty() -> avgScores
                maxScores.isNotEmpty() -> maxScores
                minScores.isNotEmpty() -> minScores
                else -> emptyList()
            }
            if (baseList.isEmpty()) return@Canvas

            // ✅ [수정 2] 그리기 간격: 24시간 기준 (24등분)
            val widthPerPoint = size.width / 24f


            // -----------------
// 그리드 (항상 5등분으로 통일)
// -----------------
// 100점일 때: 0, 20, 40, 60, 80, 100
// 150점일 때: 0, 30, 60, 90, 120, 150
            val gridSteps = 5
            val gridValues = (0..gridSteps).map { (gridTopValue / gridSteps) * it }

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

            // frequency 점 그리기
            if (frequencyScores.isNotEmpty()) {
                val safeMaxFreq = frequencyMax?.toFloat()
                    ?: frequencyScores.maxOrNull()?.toFloat()
                    ?: 1f
                val safeMax = if (safeMaxFreq == 0f) 1f else safeMaxFreq

                val startColor = Color(0xFFC2B1FF)
                val endColor = Color(0xFFA187FF)

                frequencyScores.forEachIndexed { index, frequency ->
                    if (frequency > 0) {
                        val x = index * widthPerPoint
                        val relativeScore = (frequency / safeMax) * 100f
                        val y = topPadding + (drawableHeight - relativeScore * heightScale)

                        val fraction = (frequency / safeMax).coerceIn(0f, 1f)
                        val currentColor = lerp(startColor, endColor, fraction)
                        val radius = 6f + (fraction * 14f)

                        drawCircle(color = currentColor, radius = radius, center = Offset(x, y))
                    }
                }
            }

            // 곡선 그리기
            fun drawCurve(values: List<Int>, color: Color) {
                if (values.size < 2) return
                val path = Path()

                // 마지막 데이터 인덱스 (23시)
                val lastIndex = values.size - 1

                values.forEachIndexed { index, score ->
                    val x = index * widthPerPoint
                    val y = topPadding + (drawableHeight - score * heightScale)

                    if (index == 0) path.moveTo(x, y)
                    else {
                        val prevX = (index - 1) * widthPerPoint
                        val prevY = topPadding + (drawableHeight - values[index - 1] * heightScale)
                        path.cubicTo(prevX + widthPerPoint / 2, prevY, prevX + widthPerPoint / 2, y, x, y)
                    }

                    // ✅ [추가] 마지막 점(23시)을 찍은 후, 24시(그래프 끝)의 0점 위치로 연결
                    if (index == lastIndex) {
                        val endX = size.width // 24h 위치 (widthPerPoint * 24)
                        val zeroY = topPadding + drawableHeight // 점수 0일 때의 Y좌표 (바닥)

                        // 부드럽게 떨어지도록 곡선 사용 (또는 직선을 원하면 path.lineTo 사용)
                        val controlX = x + widthPerPoint / 2
                        path.cubicTo(controlX, y, controlX, zeroY, endX, zeroY)
                    }
                }
                drawPath(path = path, color = color, style = Stroke(width = 12f))
            }
            if (minScores.isNotEmpty()) drawCurve(minScores, Color(0xFF007BFF))
            if (avgScores.isNotEmpty()) drawCurve(avgScores, Color(0xFF000000))
            if (maxScores.isNotEmpty()) drawCurve(maxScores, Color(0xFF4F3422))

            // 최고값 점 강조
            if (maxScores.isNotEmpty()) {
                val maxScore = maxScores.maxOrNull() ?: 0
                if (maxScore > 0) {
                    maxScores.forEachIndexed { index, score ->
                        if (score == maxScore) {
                            val x = index * widthPerPoint
                            val y = topPadding + (drawableHeight - score * heightScale)
                            drawCircle(color = Color(0xFFEF8834), radius = 20f, center = Offset(x, y))
                        }
                    }
                }
            }

            // -----------------------
            // ✅ [수정 3] X축 라벨: 24h 포함, 24등분 매핑
            // -----------------------
            run {
                val scaleFactor = (size.height / 240f).coerceIn(0.8f, 1.25f)
                val textSizePx = baseTextPx * scaleFactor

                // 0, 4, 8, 12, 16, 20, 24 까지 포함
                val labelHours = listOf(0, 4, 8, 12, 16, 20, 24)
                // 24시간 기준으로 비율 계산
                val labelFractions = labelHours.map { it / 24f }
                val labelTexts = labelHours.map { "${it}h" }

                val labelY = topPadding + drawableHeight + (bottomPadding * 0.25f)

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#867E7A")
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = textSizePx
                    isAntiAlias = true
                    isFakeBoldText = false
                    typeface = brandTypeface
                }

                val fm = paint.fontMetrics
                val baselineY = labelY - fm.ascent

                labelFractions.forEachIndexed { i, fraction ->
                    val x = fraction * size.width
                    drawContext.canvas.nativeCanvas.drawText(labelTexts[i], x, baselineY, paint)
                }
            }

            // 툴팁 그리기
            val currentFreq = frequencyScores.getOrNull(hoveredIndex) ?: 0

// ✅ [수정] 빈도수(currentFreq)가 0보다 클 때만 툴팁을 그립니다.
            val shouldShow = if (hoveredIndex >= 0) {
                when (hoveredKind) {
                    "frequency" -> (frequencyScores.getOrNull(hoveredIndex) ?: 0) > 0
                    "max" -> (maxScores.getOrNull(hoveredIndex) ?: 0) > 0
                    "avg" -> (avgScores.getOrNull(hoveredIndex) ?: 0) > 0
                    "min" -> (minScores.getOrNull(hoveredIndex) ?: 0) > 0
                    else -> false
                }
            } else {
                false
            }

            if (shouldShow) {
                val tooltipText = when (hoveredKind) {
                    "frequency" -> {
                        val v = frequencyScores.getOrNull(hoveredIndex) ?: 0
                        "${hoveredIndex}시\n스트레스 빈도: ${v}회"
                    }
                    "max" -> {
                        val v = maxScores.getOrNull(hoveredIndex) ?: 0
                        "${hoveredIndex}시\n스트레스 최고점: ${v}점"
                    }
                    "avg" -> {
                        val v = avgScores.getOrNull(hoveredIndex) ?: 0
                        "${hoveredIndex}시\n평균: ${v}점"
                    }
                    "min" -> {
                        val v = minScores.getOrNull(hoveredIndex) ?: 0
                        "${hoveredIndex}시\n최저: ${v}점"
                    }
                    else -> "${hoveredIndex}시"
                }

                val pointX = hoveredIndex * widthPerPoint
                val pointY = when (hoveredKind) {
                    "frequency" -> {
                        val freq = frequencyScores.getOrNull(hoveredIndex) ?: 0
                        val safeMaxFreq = frequencyMax?.toFloat()
                            ?: frequencyScores.maxOrNull()?.toFloat()
                            ?: 1f
                        val safeMax = if (safeMaxFreq == 0f) 1f else safeMaxFreq

                        val relativeScore = (freq / safeMax) * 100f
                        topPadding + (drawableHeight - relativeScore * heightScale)
                    }
                    else -> topPadding + (drawableHeight - (((when (hoveredKind) {
                        "max" -> maxScores.getOrNull(hoveredIndex) ?: 0
                        "avg" -> avgScores.getOrNull(hoveredIndex) ?: 0
                        "min" -> minScores.getOrNull(hoveredIndex) ?: 0
                        else -> 0
                    })) * heightScale))
                }

                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 32f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val textBounds = android.graphics.Rect()
                textPaint.getTextBounds(tooltipText, 0, tooltipText.length, textBounds)
                val boxWidth = textBounds.width() + 60f
                val boxHeight = textBounds.height() + 50f

                var boxLeft = pointX - boxWidth / 2
                var boxTop = pointY - boxHeight - 20f

                if (boxLeft < 0) boxLeft = 0f
                if (boxLeft + boxWidth > size.width) boxLeft = size.width - boxWidth
                if (boxTop < 0) boxTop = pointY + 20f

                drawContext.canvas.nativeCanvas.drawRoundRect(
                    boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight,
                    16f, 16f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#4F3422")
                        style = android.graphics.Paint.Style.FILL
                        setShadowLayer(12f, 0f, 6f, android.graphics.Color.parseColor("#80000000"))
                    }
                )

                val lines = tooltipText.split("\n")
                val lineHeight = textPaint.descent() - textPaint.ascent()
                val totalTextHeight = lineHeight * lines.size
                var currentY = boxTop + (boxHeight - totalTextHeight) / 2 - textPaint.ascent()

                lines.forEach { line ->
                    drawContext.canvas.nativeCanvas.drawText(line, boxLeft + boxWidth / 2, currentY, textPaint)
                    currentY += lineHeight
                }
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
//                Spacer(Modifier.height(smallSpacer))
            }

//            item { Spacer(modifier = Modifier.height(chartTopSpacer)) }

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
                    val frequencyList = MutableList(24) { 0 }

                    data.hourlyStats.forEach { stat ->
                        val h = stat.hour
                        if (h in 0..23) {
                            val max = stat.maxStress?.roundToInt() ?: 0
                            val min = stat.minStress?.roundToInt() ?: 0
                            val avg = stat.avgStress?.roundToInt() ?: 0

                            maxList[h] = max
                            minList[h] = min
                            avgList[h] = avg
                            frequencyList[h] = stat.measurementCount

                        }
                    }

                    maxScoresState = maxList
                    minScoresState = minList
                    avgScoresState = avgList
                    frequencyStressState = frequencyList

                    // 최다 스트레스 시점 및 횟수
                    moodChangeTimeState = data.peakFrequencyHour ?: 0
                    maxStressState = data.peakFrequencyCount ?: 0

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
        val extraGap = (configuration.screenHeightDp.dp * 0.3f).coerceIn(40.dp, 60.dp)
//        val extraGap = 30.dp;
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
