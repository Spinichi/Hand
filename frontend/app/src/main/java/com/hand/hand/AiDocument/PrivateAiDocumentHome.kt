// PrivateAiDocumentHome.kt

package com.hand.hand.AiDocument

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.care.CareActivity
import com.hand.hand.diary.DiaryHomeActivity
import com.hand.hand.ui.home.*
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.*

import androidx.compose.runtime.LaunchedEffect
import com.hand.hand.api.Report.ReportManager
import com.hand.hand.api.Report.WeeklyReportItem
import com.hand.hand.api.Report.MonthlyReportItem
import android.util.Log

class PrivateAiDocumentHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrivateAiDocumentHomeScreen()
        }
    }
}

@Composable
fun PrivateAiDocumentHomeScreen() {
    val context = LocalContext.current
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val today = Calendar.getInstance()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // 현재 헤더에 보이는 연/월
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonthIndex = calendar.get(Calendar.MONTH) // 0~11

    val isDisplayMonth = currentYear < today.get(Calendar.YEAR) ||
            (currentYear == today.get(Calendar.YEAR) &&
                    currentMonthIndex <= today.get(Calendar.MONTH))


    // 주간 리포트 상태
    var weeklyReports by remember { mutableStateOf<List<WeeklyReportItem>>(emptyList()) }
    var isWeeklyLoading by remember { mutableStateOf(false) }
    var weeklyError by remember { mutableStateOf<String?>(null) }

    // 월간 리포트 상태
    var monthlyReports by remember { mutableStateOf<List<MonthlyReportItem>>(emptyList()) }
    var isMonthlyLoading by remember { mutableStateOf(false) }
    var monthlyError by remember { mutableStateOf<String?>(null) }

    // 달이 바뀔 때마다 주간 다시 요청
    LaunchedEffect(currentYear, currentMonthIndex) {
        isWeeklyLoading = true
        weeklyError = null

        ReportManager.fetchWeeklyReports(
            page = 0,
            size = 20,
            onSuccess = { list ->
                weeklyReports = list
                isWeeklyLoading = false
            },
            onFailure = { t ->
                Log.e("PrivateAiDocument", "주간 리포트 불러오기 실패", t)
                weeklyError = t.message
                isWeeklyLoading = false
            }
        )
    }

    // 달이 바뀔 때마다 월간 다시 요청
    LaunchedEffect(currentYear, currentMonthIndex) {
        Log.d("PrivateAiDocument", "월간 리포트 요청: year=$currentYear, month=$currentMonthIndex")
        isMonthlyLoading = true
        monthlyError = null

        ReportManager.fetchMonthlyReports(
            page = 0,
            size = 20,
            onSuccess = { list ->
                monthlyReports = list
                isMonthlyLoading = false
            },
            onFailure = { t ->
                Log.e("PrivateAiDocument", "월간 리포트 불러오기 실패", t)
                monthlyError = t.message
                isMonthlyLoading = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            CurvedBottomNavBar(
                selectedTab = BottomTab.Diary,
                onClickHome = {
                    context.startActivity(Intent(context, HomeActivity::class.java))
                },
                onClickWrite = {
                    context.startActivity(Intent(context, DiaryHomeActivity::class.java))
                },
                onClickDiary = {
                    context.startActivity(Intent(context, PrivateAiDocumentHomeActivity::class.java))
                },
                onClickProfile = {
                    // 추후 프로필 페이지
                },
                onClickCenter = {
                    context.startActivity(Intent(context, CareActivity::class.java))
                }
            )
        },
        containerColor = Color(0xFFF7F4F2)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // ── AI 문서 헤더 ──
            AiDocumentHeader(
                subtitleText = "AI 분석 감정 보고서",
                calendar = calendar,
                onMonthChange = { newCal -> calendar = newCal },
                onBackToHome = {
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            // ── 월간 감정 보고서 텍스트 ──
            Text(
                text = "월간 감정 보고서",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = screenWidth * 0.05f),
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.025f).value.sp,
                color = Color(0xFF4F3422)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            // ── 월간 보고서 카드 ──
            if (isDisplayMonth) {
                when {
                    // 월간 로딩 상태
                    isMonthlyLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = screenWidth * 0.05f)
                                .size(32.dp),
                            color = Color(0xFF4F3422)
                        )
                    }

                    // 에러 상태
                    monthlyError != null -> {
                        Text(
                            text = "월간보고서를 불러오지 못했습니다",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = screenWidth * 0.05f),
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = (screenHeight * 0.02f).value.sp,
                            color = Color.Red
                        )
                    }

                    // 정상 데이터 있을 때
                    else -> {
                        MonthlyReportCard(
                            calendar = calendar,
                            today = today,
                            screenHeight = screenHeight,
                            screenWidth = screenWidth,
                            monthlyReports = monthlyReports
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            // ── 주간 감정 보고서 텍스트 ──
            Text(
                text = "주간 감정 보고서",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = screenWidth * 0.05f),
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.025f).value.sp,
                color = Color(0xFF4F3422)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            // ── 주간 보고서: 로딩 / 에러 / 성공 ──
            when {
                isWeeklyLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = screenWidth * 0.05f)
                            .size(32.dp),
                        color = Color(0xFF4F3422)
                    )
                }

                weeklyError != null -> {
                    Text(
                        text = "주간 보고서를 불러오지 못했습니다",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenWidth * 0.05f),
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = (screenHeight * 0.02f).value.sp,
                        color = Color.Red
                    )
                }

                else -> {
                    WeeklyReportCards(
                        calendar = calendar,
                        today = today,
                        screenHeight = screenHeight,
                        screenWidth = screenWidth,
                        weeklyReports = weeklyReports
                    )
                }
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))
        }
    }
}

// ─────────────────────────────────────────────
// 월간 카드
// ─────────────────────────────────────────────

@Composable
fun MonthlyReportCard(
    calendar: Calendar,
    today: Calendar,
    screenHeight: Dp,
    screenWidth: Dp,
    monthlyReports: List<MonthlyReportItem>
) {
    val context = LocalContext.current
    val year = calendar.get(Calendar.YEAR)
    val monthIndex = calendar.get(Calendar.MONTH)   // 0~11
    val month = monthIndex + 1                      // 1~12

    // 한 달에 하나만: 현재 연/월과 같은 첫 번째 리포트
    val report = monthlyReports.firstOrNull {
        it.year == year && it.month == month
    }

    Card(
        shape = RoundedCornerShape(screenHeight * 0.03f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.05f, vertical = screenHeight * 0.005f)
            .clickable {
                report?.let { monthly ->
                    val intent = Intent(context, PrivateAiDocumentActivity::class.java)
                    intent.putExtra("YEAR", year)
                    intent.putExtra("MONTH", month)
                    intent.putExtra("WEEK", 0)                 // 월간 모드 표시
                    intent.putExtra("REPORT_ID", monthly.id)    // 월간 보고서 id
                    context.startActivity(intent)
                }
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.03f, vertical = screenHeight * 0.01f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(width = screenWidth * 0.2f, height = screenHeight * 0.07f)
                    .background(
                        color = Color(0xFFF7F4F2),
                        shape = RoundedCornerShape(screenHeight * 0.02f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${month}월",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )
            }

            Spacer(modifier = Modifier.width(screenWidth * 0.03f))

            Text(
                text = report?.let { "AI 분석 감정 보고서" } ?: "보고서 데이터가 없습니다",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.025f).value.sp,
                color = Color(0xFF4F3422)
            )
        }
    }
}


// 주간 카드 (기존과 같고, 데이터만 API 기반)


@Composable
fun WeeklyReportCards(
    calendar: Calendar,
    today: Calendar,
    screenHeight: Dp,
    screenWidth: Dp,
    weeklyReports: List<WeeklyReportItem>
) {
    val context = LocalContext.current
    val year = calendar.get(Calendar.YEAR)
    val monthIndex = calendar.get(Calendar.MONTH)    // 0~11
    val month = monthIndex + 1                       // 1~12

    // 미래 달이면 그냥 안 보이게 처리
    if (calendar.get(Calendar.YEAR) > today.get(Calendar.YEAR) ||
        (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) > today.get(Calendar.MONTH))
    ) {
        return
    }

    // 이 달에 해당하는 주간 리포트만 추출
    val reportsForMonth = weeklyReports.filter { report ->
        try {
            val parts = report.weekStartDate.split("-")  // "2025-11-11"
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            y == year && m == month
        } catch (_: Exception) {
            false
        }
    }.sortedBy { it.weekNumber }   // 연 기준 주차로 정렬

    if (reportsForMonth.isEmpty()) {
        Text(
            text = "리포트 데이터 없음",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.05f),
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (screenHeight * 0.02f).value.sp,
            color = Color(0xFF4F3422)
        )
        return
    }

    // 월 기준 주차로 다시 번호 매기기
    reportsForMonth.forEachIndexed { index, report ->
        val hasData = report.diaryCount > 0 && report.status != "EMPTY"

        // 1주차, 2주차, 3주차…
        val displayWeek = index + 1

        Card(
            shape = RoundedCornerShape(screenHeight * 0.03f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.05f, vertical = screenHeight * 0.005f)
                .clickable {
                    val intent = Intent(context, PrivateAiDocumentActivity::class.java)
                    intent.putExtra("YEAR", report.year)
                    intent.putExtra("MONTH", month)
                    intent.putExtra("REPORT_ID", report.id)   // 상세는 id로 조회
                    intent.putExtra("WEEK", displayWeek)      // 화면에 보여줄 주차는 월 기준
                    context.startActivity(intent)
                },
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenWidth * 0.03f, vertical = screenHeight * 0.01f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(width = screenWidth * 0.2f, height = screenHeight * 0.045f)
                        .background(
                            color = Color(0xFFF7F4F2),
                            shape = RoundedCornerShape(screenHeight * 0.02f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${displayWeek}주차",   // 여기도 월 기준 주차
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.025f).value.sp,
                        color = Color(0xFF4F3422)
                    )
                }

                Spacer(modifier = Modifier.width(screenWidth * 0.03f))

                Text(
                    text = if (hasData) "AI 분석 감정 보고서" else "감정 보고서가 발행되지 않았습니다",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.022f).value.sp,
                    color = Color(0xFF4F3422)
                )
            }
        }
    }
}
