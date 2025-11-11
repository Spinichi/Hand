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
import com.hand.hand.ui.model.PersonalReportSource
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.*

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

    val isDisplayMonth = calendar.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
            (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) < today.get(Calendar.MONTH))

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
                MonthlyReportCard(calendar, today, screenHeight, screenWidth)
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

            // ── 주간 보고서 카드 ──
            WeeklyReportCards(calendar, today, screenHeight, screenWidth)

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))
        }
    }
}

// MonthlyReportCard, WeeklyReportCards는 기존 코드 그대로 사용


@Composable
fun MonthlyReportCard(calendar: Calendar, today: Calendar, screenHeight: Dp, screenWidth: Dp) {
    val context = LocalContext.current
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)

    val report = PersonalReportSource.reportOrNull(year, month + 1)

    Card(
        shape = RoundedCornerShape(screenHeight * 0.03f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.05f, vertical = screenHeight * 0.005f)
            .clickable {
                val intent = Intent(context, PrivateAiDocumentActivity::class.java)
                intent.putExtra("YEAR", year)
                intent.putExtra("MONTH", month + 1)
                intent.putExtra("WEEK", 0)
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
                    .size(width = screenWidth * 0.2f, height = screenHeight * 0.07f)
                    .background(
                        color = Color(0xFFF7F4F2),
                        shape = RoundedCornerShape(screenHeight * 0.02f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${month + 1}월",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )
            }

            Spacer(modifier = Modifier.width(screenWidth * 0.03f))

            Text(
                text = report?.let { "AI 분석 감정 보고서" } ?: "데이터가 없습니다",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.025f).value.sp,
                color = Color(0xFF4F3422)
            )
        }
    }
}

@Composable
fun WeeklyReportCards(calendar: Calendar, today: Calendar, screenHeight: Dp, screenWidth: Dp) {
    val context = LocalContext.current
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val report = PersonalReportSource.reportOrNull(year, month + 1)

    if (calendar.get(Calendar.YEAR) > today.get(Calendar.YEAR) ||
        (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) > today.get(Calendar.MONTH))
    ) return

    val lastDisplayDay = if (year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH)) {
        today.get(Calendar.DAY_OF_MONTH) - 1
    } else {
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val weekList = mutableListOf<Int>()
    val firstDay = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    var week = 1
    for (day in 1..lastDisplayDay) {
        firstDay.set(Calendar.DAY_OF_MONTH, day)
        if (firstDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && day != 1) {
            week++
        }
        if (!weekList.contains(week)) weekList.add(week)
    }

    weekList.forEach { w ->
        val hasData = report?.weeks?.getOrNull(w - 1)?.hasData == true

        Card(
            shape = RoundedCornerShape(screenHeight * 0.03f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.05f, vertical = screenHeight * 0.005f)
                .clickable {
                    val intent = Intent(context, PrivateAiDocumentActivity::class.java)
                    intent.putExtra("YEAR", year)
                    intent.putExtra("MONTH", month + 1)
                    intent.putExtra("WEEK", w)
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
                        text = "${w}주차",
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.025f).value.sp,
                        color = Color(0xFF4F3422)
                    )
                }

                Spacer(modifier = Modifier.width(screenWidth * 0.03f))

                Text(
                    text = if (hasData) "AI 분석 감정 보고서" else "데이터가 없습니다",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.022f).value.sp,
                    color = Color(0xFF4F3422)
                )
            }
        }
    }
}

