package com.hand.hand.diary

import android.content.Intent
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.ui.theme.BrandFontFamily
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DiaryCalendar(
    calendar: Calendar,
    scoreMap: Map<String, Int> = emptyMap(), // 날짜별 depressionScore Map
    sessionIdMap: Map<String, Long> = emptyMap(), // 날짜별 sessionId Map
    onDateClick: (Int) -> Unit = {}
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val currentCalendar = calendar.clone() as Calendar
    val context = LocalContext.current

    currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
    val dates = (0 until totalCells).map { dayIndex ->
        val date = dayIndex - firstDayOfWeek + 1
        if (date in 1..daysInMonth) date.toString() else ""
    }

    val today = Calendar.getInstance()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cellSize: Dp = screenWidth / 9

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

                        val isFuture = thisDate.after(today)
                        val alpha = if (isFuture) 0.5f else 1f

                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                        val thisDateStr = sdf.format(thisDate.time)

                        val score = scoreMap[thisDateStr] ?: -1
                        val circleColor = when (score) {
                            in 0..19 -> Color(0xFFFFCE5C)
                            in 20..39 -> Color(0xFF9BB167)
                            in 40..59 -> Color(0xFFC0A091)
                            in 60..79 -> Color(0xFFED7E1C)
                            in 80..100 -> Color(0xFFC2B1FF)
                            else -> Color.White
                        }

                        val finalCircleColor = if (isFuture) {
                            Color(0xFFE0E0E0) // ← 미래는 무조건 회색
                        } else {
                            circleColor
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(
                                    color = finalCircleColor,
                                    shape = CircleShape
                                )
                                .clickable(enabled = !isFuture) {

                                    Log.i("DiaryCalendar", "clickedDate = [$thisDateStr]")
                                    Log.i("DiaryCalendar", "scoreMap keys = ${scoreMap.keys}")
                                    Log.i("DiaryCalendar", "sessionIdMap = $sessionIdMap")

                                    val sessionId = sessionIdMap[thisDateStr] ?: -1L
                                    Log.i("DiaryCalendar", "→ 선택 sessionId = $sessionId")

                                    if (sessionId == -1L) {
                                        val intent = Intent(context, DiaryWriteActivity::class.java)
                                        intent.putExtra("selectedDate", thisDateStr)
                                        context.startActivity(intent)
                                    } else {
                                        val intent = Intent(context, DiaryDetailActivity::class.java)
                                        intent.putExtra("sessionId", sessionId)
                                        context.startActivity(intent)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(
                                text = date,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF4F3422)
                            )
                        }
                    } else {
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
