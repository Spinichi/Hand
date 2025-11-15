package com.hand.hand.diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.hand.hand.AiDocument.PrivateAiDocumentHomeActivity
import com.hand.hand.api.Diary.DiaryItem
import com.hand.hand.api.Diary.DiaryManager
import com.hand.hand.care.CareActivity
import com.hand.hand.ui.home.BottomTab
import com.hand.hand.ui.home.CurvedBottomNavBar
import com.hand.hand.ui.home.HomeActivity
import com.hand.hand.ui.theme.BrandFontFamily
import java.text.SimpleDateFormat
import java.util.*

class DiaryHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiaryHomeScreen(onBackClick = { finish() })
        }
    }
}

@Composable
fun DiaryHomeScreen(onBackClick: () -> Unit) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var diaryList by remember { mutableStateOf<List<DiaryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        val startDate = "%04d-%02d-01".format(year, month)
        val endDate = "%04d-%02d-%02d".format(
            year,
            month,
            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        )

        isLoading = true
        errorMessage = null

        DiaryManager.getMyDiaryList(
            startDate = startDate,
            endDate = endDate,
            page = 0,
            size = 30,
            onSuccess = {
                diaryList = it
                isLoading = false
            },
            onFailure = {
                isLoading = false
                errorMessage = it.message
            }
        )
    }

    val scoreMap = diaryList.associateBy(
        { it.sessionDate },
        { it.depressionScore ?: -1 }
    )

    Scaffold(
        containerColor = Color(0xFFF7F4F2),
        topBar = {
            DiaryHeader(
                subtitleText = "감정 다이어리",
                onBackClick = onBackClick,
                calendar = calendar,
                onMonthChange = { calendar = it }
            )
        },
        bottomBar = {
            CurvedBottomNavBar(
                selectedTab = BottomTab.Write,
                onClickHome = { context.startActivity(Intent(context, HomeActivity::class.java)) },
                onClickWrite = { /* 현재 페이지 */ },
                onClickDiary = { context.startActivity(Intent(context, PrivateAiDocumentHomeActivity::class.java)) },
                onClickProfile = { /* TODO */ },
                onClickCenter = { context.startActivity(Intent(context, CareActivity::class.java)) }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            DiaryCalendar2(
                calendar = calendar,
                scoreMap = scoreMap,
                onDateClick = { day ->
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH) + 1
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                    val selectedDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month - 1)
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                    val selectedDateStr = sdf.format(selectedDate.time)

                    val diaryItem = diaryList.find { it.sessionDate == selectedDateStr }
                    Log.i("DiaryHome", "clickedDate = $selectedDateStr, diaryItem = $diaryItem")

                    if (diaryItem == null) {
                        val intent = Intent(context, DiaryWriteActivity::class.java)
                        intent.putExtra("selectedDate", selectedDateStr)
                        context.startActivity(intent)
                    } else {
                        val intent = Intent(context, DiaryDetailActivity::class.java)
                        intent.putExtra("sessionId", diaryItem.sessionId)
                        Log.i("DiaryHome", "→ 전달 sessionId = ${diaryItem.sessionId}")
                        context.startActivity(intent)
                    }
                }
            )


            EmotionLegend()

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            Text(
                text = "감정 다이어리 히스토리",
                modifier = Modifier.padding(start = 20.dp),
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color(0xFF4F3422)
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> Text(
                    text = "불러오는 중...",
                    modifier = Modifier.padding(20.dp),
                    color = Color.Gray
                )

                errorMessage != null -> Text(
                    text = "오류 발생: $errorMessage",
                    modifier = Modifier.padding(20.dp),
                    color = Color.Red
                )

                diaryList.isEmpty() -> Text(
                    text = "이달의 데이터가 없습니다.",
                    modifier = Modifier.padding(20.dp),
                    color = Color.Gray
                )

                else -> diaryList.forEach { item -> DiaryHistoryBox(item) }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// DiaryHistoryBox, DiaryCalendar2, EmotionLegend는 기존 코드 그대로 사용


@Composable
fun DiaryHistoryBox(item: DiaryItem) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // 날짜에서 "일(day)"만 추출
    val dayText = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        val date = sdf.parse(item.sessionDate)
        val cal = Calendar.getInstance().apply { time = date!! }
        "${cal.get(Calendar.DAY_OF_MONTH)}일"
    } catch (e: Exception) {
        item.sessionDate
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.1f)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 날짜 박스
            Box(
                modifier = Modifier
                    .size(screenHeight * 0.07f)
                    .background(Color(0xFFF7F4F2), shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayText,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = item.shortSummary ?: "내용 없음",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.02f).value.sp,
                    color = Color(0xFF4F3422)
                )

                Spacer(modifier = Modifier.height(6.dp))

                val score = item.depressionScore ?: -1
                val boxColor = when (score) {
                    in 0..19 -> Color(0xFFC2B1FF)
                    in 20..39 -> Color(0xFFED7E1C)
                    in 40..59 -> Color(0xFFC0A091)
                    in 60..79 -> Color(0xFF9BB167)
                    in 80..100 -> Color(0xFFFFCE5C)
                    else -> Color.Gray
                }

                Box(
                    modifier = Modifier
                        .background(boxColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (score >= 0) "$score 점" else "-",
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (screenHeight * 0.018f).value.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DiaryCalendar2(
    calendar: Calendar,
    scoreMap: Map<String, Int> = emptyMap(),
    onDateClick: (Int) -> Unit = {}
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val currentCalendar = calendar.clone() as Calendar

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
                            in 0..19 -> Color(0xFFC2B1FF)
                            in 20..39 -> Color(0xFFED7E1C)
                            in 40..59 -> Color(0xFFC0A091)
                            in 60..79 -> Color(0xFF9BB167)
                            in 80..100 -> Color(0xFFFFCE5C)
                            else -> Color.White
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(circleColor.copy(alpha = alpha), shape = CircleShape)
                                .clickable(enabled = !isFuture) { onDateClick(date.toInt()) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF4F3422)
                            )
                        }
                    } else {
                        Box(modifier = Modifier.size(cellSize)) {}
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun EmotionLegend() {
    val emotions = listOf(
        Pair(Color(0xFF9BB167), "great"),
        Pair(Color(0xFFFFCE5C), "happy"),
        Pair(Color(0xFFC0A091), "okay"),
        Pair(Color(0xFFED7E1C), "down"),
        Pair(Color(0xFFC2B1FF), "sad")
    )

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val circleSize = screenWidth * 0.03f
    val textSize = (screenWidth.value / 27).sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.04f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        emotions.forEach { (color, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(screenWidth * 0.015f))
                Text(
                    text = label,
                    color = Color(0xFF867E7A),
                    fontSize = textSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
