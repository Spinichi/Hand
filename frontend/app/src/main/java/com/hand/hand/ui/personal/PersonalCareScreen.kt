// file: com/hand/hand/ui/personal/PersonalCareScreen.kt
package com.hand.hand.ui.personal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.nav.NavBar
import com.hand.hand.ui.model.MonthlyReport
import com.hand.hand.ui.model.PersonalReportSource
import com.hand.hand.ui.model.WeeklyReport
import com.hand.hand.ui.personal.detail.PersonalWeeklyDetailActivity
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.Calendar

private val SheetF7F4F2 = Color(0xFFF7F4F2)
private val CardWhite   = Color(0xFFFFFFFF)
private val ChipBg      = Color(0xFFF7F4F2) // 내부 칩 배경

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalCareScreen(
    onHomeClick: () -> Unit = {},
    onDiaryClick: () -> Unit = {},
    onDocumentClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCareClick: () -> Unit = {},
) {
    var currentCal by remember { mutableStateOf(Calendar.getInstance()) }
    val context = LocalContext.current

    // 월/연
    val currentYear  = currentCal.get(Calendar.YEAR)
    val currentMonth = currentCal.get(Calendar.MONTH) + 1

    // 월별 원본 데이터 (없으면 null)
    val reportRaw: MonthlyReport? by remember(currentCal.timeInMillis) {
        mutableStateOf(PersonalReportSource.reportOrNull(currentYear, currentMonth))
    }

    // 주간 중 실제 데이터 있는 주만 필터
    val weeksWithData: List<WeeklyReport> = remember(reportRaw) {
        reportRaw?.weeks?.filter { it.hasData } ?: emptyList()
    }
    val hasMonthlyData = reportRaw?.hasData == true

    Scaffold(
        containerColor = SheetF7F4F2,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            PersonalTopBar(
                subtitleText = "AI 분석 감정 보고서",
                onBackClick = onHomeClick,
                calendar = currentCal,
                onMonthChange = { newCal ->
                    if (!isBlockedFutureMonthPublic(newCal)) {
                        currentCal = newCal
                    }
                },
                titlePaddingTop = 0.03f
            )
        },
        bottomBar = {
            NavBar(
                onHomeClick = onHomeClick,
                onDiaryClick = onDiaryClick,
                onDocumentClick = onDocumentClick,
                onProfileClick = onProfileClick,
                onCareClick = onCareClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── 월간 카드: 실제 데이터 있을 때만 표시
            if (hasMonthlyData && reportRaw != null) {
                item {
                    SectionTitle("월간 감정 보고서")
                    Spacer(Modifier.height(10.dp))
                    MonthlyCard(
                        month = reportRaw!!.month,
                        title = "AI 분석 감정 보고서"
                    )
                }
            }

            // ── 주간 카드: 데이터 있는 주만 표시
            if (weeksWithData.isNotEmpty()) {
                item { SectionTitle("주간 감정 보고서") }
                items(weeksWithData, key = { it.weekIndex }) { wk ->
                    WeeklyCard(
                        weekIndex = wk.weekIndex,
                        title = "AI 분석 감정 보고서",
                        modifier = Modifier.clickable(
                            // 디자인 변화 없게 (리플 제거)
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            context.startActivity(
                                PersonalWeeklyDetailActivity.intent(
                                    context = context,
                                    year = currentYear,
                                    month = currentMonth,
                                    weekIndex = wk.weekIndex
                                )
                            )
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/* =================== UI 컴포넌트 (디자인 고정 사양) =================== */

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF736B66),
        fontFamily = BrandFontFamily,
        fontWeight = FontWeight.Bold,    // 요구: Bold만 사용
        fontSize = 20.sp
    )
}

/** 월간 카드: 바깥 카드(FFFFFF, radius 25) + 내부 칩(F7F4F2, radius 15, 최소높이 68dp) */
@Composable
private fun MonthlyCard(month: Int, title: String) {
    Surface(
        color = CardWhite,
        contentColor = Color.Unspecified,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ChipBg,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 68.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${month}월",
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF736B66)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF3F362F)
            )
        }
    }
}

/** 주간 카드: 바깥 카드(FFFFFF, radius 25) + 내부 칩(F7F4F2, radius 15, 최소높이 44dp) */
@Composable
private fun WeeklyCard(
    weekIndex: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CardWhite,
        contentColor = Color.Unspecified,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth() // ← 전달된 modifier 적용 (디자인 변화 없음)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ChipBg,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${weekIndex}주차",
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF736B66)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF3F362F)
            )
        }
    }
}

/* ─── 미래 월 차단(화면 측 가드) ─── */
private fun Calendar.startOfMonth(): Calendar =
    (clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

private fun isBlockedFutureMonthPublic(target: Calendar): Boolean {
    val nextMonthStart = Calendar.getInstance()
        .startOfMonth().apply { add(Calendar.MONTH, 1) }
    val candidate = target.startOfMonth()
    return !candidate.before(nextMonthStart)
}
