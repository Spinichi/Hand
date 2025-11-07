package com.hand.hand.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.*

@Composable
fun DiaryCalendar(
    calendar: Calendar,
    onDateClick: (Int) -> Unit = {} // ✅ 날짜 클릭 콜백 추가
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val currentCalendar = calendar.clone() as Calendar

    // 🔹 이번 달의 첫 날 / 마지막 날짜 계산
    currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // 🔹 날짜 리스트 구성
    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
    val dates = (0 until totalCells).map { dayIndex ->
        val date = dayIndex - firstDayOfWeek + 1
        if (date in 1..daysInMonth) date.toString() else ""
    }

    // 🔹 오늘 날짜 계산
    val today = Calendar.getInstance()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cellSize: Dp = screenWidth / 9  // 반응형 크기 조정

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔸 요일 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    color = Color(0xFF867E7A),
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔸 날짜 Grid
        for (week in dates.chunked(7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { date ->
                    if (date.isNotEmpty()) {
                        val thisDate = calendar.clone() as Calendar
                        thisDate.set(Calendar.DAY_OF_MONTH, date.toInt())

                        // ✅ 미래 날짜 판별
                        val isFuture = thisDate.after(today)
                        val alpha = if (isFuture) 0.5f else 1f

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(
                                    color = Color.White.copy(alpha = alpha),
                                    shape = CircleShape
                                )
                                .clickable(enabled = !isFuture) { // ✅ 클릭 가능 조건 추가
                                    onDateClick(date.toInt())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF4F3422).copy(alpha = alpha)
                            )
                        }
                    } else {
                        // ✅ 빈 칸 (공백 유지용)
                        Box(
                            modifier = Modifier.size(cellSize),
                            contentAlignment = Alignment.Center
                        ) {}
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
