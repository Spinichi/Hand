package com.hand.hand.AiDocument

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.api.Report.ReportManager
import com.hand.hand.api.Report.WeeklyReportDetail
import com.hand.hand.api.Report.MonthlyReportDetail
import com.hand.hand.ui.model.MonthlyReport
import com.hand.hand.ui.model.PersonalReportSource
import com.hand.hand.ui.model.WeeklyReport
import com.hand.hand.ui.theme.BrandFontFamily
import androidx.compose.foundation.Canvas

import androidx.compose.ui.graphics.nativeCanvas
import java.util.Calendar

class PrivateAiDocumentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val year = intent.getIntExtra("YEAR", 0)
        val month = intent.getIntExtra("MONTH", 0)
        val week = intent.getIntExtra("WEEK", 0)
        // 주간/월간 공통으로 쓰는 리포트 id
        val reportId = intent.getLongExtra("REPORT_ID", -1L)

        val selectedDate = if (week == 0) "${year}년 ${month}월"
        else "${year}년 ${month}월 ${week}주차"

        setContent {
            PrivateAiDocumentScreen(
                selectedDate = selectedDate,
                week = week,
                year = year,
                month = month,
                reportId = reportId,   // 🔹 여기!
                onBackClick = {
                    startActivity(Intent(this, PrivateAiDocumentHomeActivity::class.java))
                    finish()
                }
            )
        }
    }
}

// 월의 실제 주 수 계산 (지금은 안 써도 됨)
fun getWeeksInMonth(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    return ((daysInMonth + firstDayOfMonth - 2) / 7) + 1
}

@Composable
fun PrivateAiDocumentScreen(
    selectedDate: String,
    week: Int,
    year: Int,
    month: Int,
    reportId: Long,              // 🔹 weeklyReportId → reportId 로 통일
    onBackClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStart: Dp = screenWidth * 0.07f
    val backButtonPaddingTop: Dp = screenHeight * 0.05f

    val bottomBoxHeight: Dp = screenHeight * 0.8f
    val bottomBoxRadius: Dp = 30.dp

    val imageWidth: Dp = screenWidth * 0.25f
    val imageHeight: Dp = screenHeight * 0.15f

    // 월간 더미 데이터 (그래프/점수용)
    val monthlyReport: MonthlyReport? = PersonalReportSource.reportOrNull(year, month)

    // 주간 상세 리포트 상태
    var weeklyDetail by remember { mutableStateOf<WeeklyReportDetail?>(null) }
    var isWeeklyDetailLoading by remember { mutableStateOf(false) }
    var weeklyDetailError by remember { mutableStateOf<String?>(null) }

    // 월간 상세 리포트 상태
    var monthlyDetail by remember { mutableStateOf<MonthlyReportDetail?>(null) }
    var isMonthlyDetailLoading by remember { mutableStateOf(false) }
    var monthlyDetailError by remember { mutableStateOf<String?>(null) }

    // 주간 상세 리포트 API 호출 (week > 0 && id가 있을 때만)
    LaunchedEffect(reportId, week) {
        if (week > 0 && reportId > 0L) {
            isWeeklyDetailLoading = true
            weeklyDetailError = null

            ReportManager.fetchWeeklyReportDetail(
                reportId = reportId,
                onSuccess = { detail ->
                    weeklyDetail = detail
                    isWeeklyDetailLoading = false
                },
                onFailure = { t ->
                    weeklyDetailError = t.message
                    isWeeklyDetailLoading = false
                }
            )
        }
    }

    // 월간 상세 리포트 API 호출 (week == 0 && id가 있을 때만)
    LaunchedEffect(reportId, week) {
        if (week == 0 && reportId > 0L) {
            isMonthlyDetailLoading = true
            monthlyDetailError = null

            ReportManager.fetchMonthlyReportDetail(
                reportId = reportId,
                onSuccess = { detail ->
                    monthlyDetail = detail
                    isMonthlyDetailLoading = false
                },
                onFailure = { t ->
                    monthlyDetailError = t.message
                    isMonthlyDetailLoading = false
                }
            )
        }
    }

    // ─────────────────────────────────────
    // 그래프 / 요약 / 조언에 쓸 값 결정
    // ─────────────────────────────────────
    val scores: List<Int>
    val xLabels: List<String>
    val summaryText: String
    val adviceText: String
    val avgScore: Int

    if (week > 0) {
        // ── 주간 모드 ──
        when {
            weeklyDetail != null -> {
                val d = weeklyDetail!!

                // dailyDiaries에서 각 날짜의 depressionScore 추출
                val dailyScores = d.dailyDiaries?.mapNotNull { diary ->
                    (diary["depressionScore"] as? Number)?.toInt()
                } ?: emptyList()

                if (dailyScores.isNotEmpty()) {
                    // 실제 일별 점수 사용
                    scores = dailyScores
                    // 날짜 개수만큼 레이블 생성 (Mon, Tue, Wed, ...)
                    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    xLabels = List(dailyScores.size) { index -> dayLabels[index % 7] }
                } else {
                    // 데이터가 없으면 평균값으로 표시
                    val avgAsInt = d.averageDepressionScore.toInt()
                    scores = List(7) { avgAsInt }
                    xLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                }

                summaryText = d.report ?: "주간 요약이 없지롱."
                adviceText = d.emotionalAdvice ?: "감정 개선 조언이 없지롱."
                avgScore = d.averageDepressionScore.toInt()
            }

            isWeeklyDetailLoading -> {
                xLabels = emptyList()
                scores = emptyList()
                summaryText = "주간 리포트 못불러옴~"
                adviceText = ""
                avgScore = 0
            }

            weeklyDetailError != null -> {
                xLabels = emptyList()
                scores = emptyList()
                summaryText = "주간 리포트 못불러옴~"
                adviceText = weeklyDetailError ?: ""
                avgScore = 0
            }

            else -> {
                xLabels = emptyList()
                scores = emptyList()
                summaryText = "데이터 없다고!!"
                adviceText = "데이터 없다고!!"
                avgScore = 0
            }
        }
    } else {
        // ── 월간 모드 (week == 0) ──
        when {
            isMonthlyDetailLoading -> {
                xLabels = emptyList()
                scores = emptyList()
                summaryText = "월간 리포트를 불러오는 중이다.....기달 ㅠ"
                adviceText = ""
                avgScore = 0
            }

            monthlyDetailError != null -> {
                xLabels = emptyList()
                scores = emptyList()
                summaryText = "월간 리포트를 불러오지 못했다닼."
                adviceText = monthlyDetailError ?: ""
                avgScore = 0
            }

            monthlyDetail != null -> {
                val d = monthlyDetail!!

                // dailyDiaries에서 일별 데이터 추출 후 주별로 평균 계산
                val dailyData = d.dailyDiaries?.mapNotNull { diary ->
                    val dateStr = diary["date"] as? String
                    val score = (diary["depressionScore"] as? Number)?.toDouble()
                    if (dateStr != null && score != null) {
                        // "2025-11-10" → 일자(10) 추출
                        val day = dateStr.split("-").lastOrNull()?.toIntOrNull() ?: 0
                        Pair(day, score)
                    } else null
                } ?: emptyList()

                if (dailyData.isNotEmpty()) {
                    // 주별로 그룹화 (1-7일=1주, 8-14일=2주, ...)
                    val weeklyScores = dailyData
                        .groupBy { (day, _) -> (day - 1) / 7 + 1 }  // 1주, 2주, 3주, ...
                        .toSortedMap()
                        .mapValues { (_, pairs) -> pairs.map { it.second }.average().toInt() }

                    scores = weeklyScores.values.toList()
                    xLabels = weeklyScores.keys.map { "${it}주" }
                    avgScore = scores.average().toInt()
                } else {
                    // 데이터가 없으면 빈 그래프
                    xLabels = emptyList()
                    scores = emptyList()
                    avgScore = 0
                }

                summaryText = d.report ?: "월간 요약 없다 엌ㅋ."
                adviceText = d.emotionalAdvice ?: "감정 개선 조언 없다 엌ㅋ.."
            }

            monthlyReport != null -> {
                // 월간 상세 API는 아직 없지만, 더미 데이터는 있을 때
                val weeksInMonth = monthlyReport.weeks.size
                xLabels = List(weeksInMonth) { "${it + 1}주" }
                scores = monthlyReport.weeks.map { it.avgScore }
                summaryText = monthlyReport.monthlySummary
                adviceText = monthlyReport.monthlyAdvice
                avgScore = monthlyReport.monthAvg
            }

            else -> {
                xLabels = emptyList()
                scores = emptyList()
                summaryText = "데이터가 없다고..."
                adviceText = "데이터가 없다고..."
                avgScore = 0
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFACA6E9))
    ) {
        // 백버튼
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "Back Button",
            modifier = Modifier
                .padding(start = backButtonPaddingStart, top = backButtonPaddingTop)
                .size(backButtonSize)
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
        )

        // 날짜
        Text(
            text = selectedDate,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (screenHeight * 0.03f).value.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = backButtonPaddingStart + backButtonSize + 18.dp,
                    top = backButtonPaddingTop + (backButtonSize / 4)
                )
        )

        // 하단 흰색 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomBoxHeight)
                .align(Alignment.BottomCenter)
                .background(
                    color = Color(0xFFF7F4F2),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = bottomBoxRadius,
                        topEnd = bottomBoxRadius
                    )
                )
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = imageHeight / 2)
            ) {

                // 감정 경향 그래프
                Text(
                    text = "감정 경향 그래프",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = screenWidth * 0.05f),
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EmotionLineChart(
                        scores = scores,
                        xLabels = xLabels,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(screenHeight * 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                // 요약
                Text(
                    text = if (week > 0) "주간 요약" else "월간 요약",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = screenWidth * 0.05f),
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.015f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = screenWidth * 0.05f)
                        .background(
                            color = Color.White,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = summaryText,
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.023f).value.sp,
                        color = Color(0xFF4F3422),
                        lineHeight = (screenHeight * 0.03f).value.sp,
                    )
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                // 감정 개선 조언
                Text(
                    text = "감정 개선 조언",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = screenWidth * 0.05f),
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.015f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = screenWidth * 0.05f)
                        .background(
                            color = Color.White,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = adviceText,
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.023f).value.sp,
                        color = Color(0xFF4F3422),
                        lineHeight = (screenHeight * 0.03f).value.sp,
                    )
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.08f))
            }
        }

        // 점수 이미지
        Image(
            painter = painterResource(
                id = when (avgScore) {
                    in 0..19 -> R.drawable.ai_document_sad
                    in 20..39 -> R.drawable.ai_document_down
                    in 40..59 -> R.drawable.ai_document_okay
                    in 60..79 -> R.drawable.ai_document_happy
                    in 80..100 -> R.drawable.ai_document_great
                    else -> R.drawable.ai_document_okay
                }
            ),
            contentDescription = "Score Icon",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = imageWidth, height = imageHeight)
                .offset(y = imageHeight * 0.8f)
        )
    }
}

@Composable
fun EmotionLineChart(
    scores: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4F3422),
    pointColor: Color = Color(0xFF815EFF),
    gridColor: Color = Color(0xFFE1D4CD),
    xLabels: List<String> = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
) {
    Canvas(modifier = modifier) {
        if (scores.isEmpty()) return@Canvas

        val widthPerPoint = size.width / (scores.size - 1).coerceAtLeast(1)
        val heightScale = size.height / 100f

        // 배경 점선
        val gridValues = listOf(0, 20, 40, 60, 80, 100)
        gridValues.forEach { value ->
            val y = size.height - (value * heightScale)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 4f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(10f, 10f),
                    0f
                )
            )
        }

        val path = Path()

        // 곡선 계산
        scores.forEachIndexed { index, score ->
            val x = index * widthPerPoint
            val y = size.height - (score * heightScale)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (index - 1) * widthPerPoint
                val prevY = size.height - (scores[index - 1] * heightScale)
                val cpx1 = prevX + widthPerPoint / 2
                val cpy1 = prevY
                val cpx2 = prevX + widthPerPoint / 2
                val cpy2 = y
                path.cubicTo(cpx1, cpy1, cpx2, cpy2, x, y)
            }
        }

        // 곡선 그리기
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 8f)
        )

        // 점 그리기
        scores.forEachIndexed { index, score ->
            val x = index * widthPerPoint
            val y = size.height - (score * heightScale)
            drawCircle(
                color = pointColor,
                radius = 12f,
                center = Offset(x, y)
            )
        }

        // X축 레이블
        xLabels.forEachIndexed { index, label ->
            if (index < scores.size) {
                val x = index * widthPerPoint
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#867E7A")
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = size.height * 0.09f
                        isFakeBoldText = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    drawText(label, x, size.height + size.height * 0.15f, paint)
                }
            }
        }
    }
}
