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
import androidx.compose.foundation.border
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.hand.hand.AiDocument.PrivateAiDocumentHomeActivity
import com.hand.hand.api.Diary.DiaryItem
import com.hand.hand.api.Diary.DiaryManager
import com.hand.hand.care.CareActivity
import com.hand.hand.ui.home.BottomTab
import com.hand.hand.ui.home.CurvedBottomNavBar
import com.hand.hand.ui.home.HomeActivity
import com.hand.hand.ui.mypage.MyPageActivity
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.common.LoadingDialog
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

    // ✅ lifecycle owner
    val lifecycleOwner = LocalLifecycleOwner.current

    // ✅ 공통 일기 목록 조회 함수
    val fetchDiaryList: () -> Unit = {
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

    // ✅ 처음 진입 + 달이 바뀔 때마다 호출
    LaunchedEffect(calendar) {
        fetchDiaryList()
    }

    // ✅ 화면으로 다시 돌아올 때(ON_RESUME)마다 재조회
    DisposableEffect(lifecycleOwner, calendar) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchDiaryList()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val scoreMap: Map<String, Float> = diaryList.associateBy(
        { it.sessionDate },
        { it.depressionScore ?: -1f }
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
                onClickHome = {
                    val intent = Intent(context, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(intent)
                },
                onClickWrite = { /* 현재 페이지 */ },
                onClickDiary = {
                    context.startActivity(
                        Intent(
                            context,
                            PrivateAiDocumentHomeActivity::class.java
                        )
                    )
                },
                onClickProfile = {
                    context.startActivity(Intent(context, MyPageActivity::class.java))
                },
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
                        // 오늘이면서 아직 작성 안 한 경우 등 → 새 일기 작성
                        val intent = Intent(context, DiaryWriteActivity::class.java)
                        intent.putExtra("selectedDate", selectedDateStr)
                        context.startActivity(intent)
                    } else {
                        // status에 따라 분기
                        when (diaryItem.status) {
                            "IN_PROGRESS" -> {
                                // 작성 중 → 작성 화면으로 이동 (이어서 작성)
                                val intent = Intent(context, DiaryWriteActivity::class.java)
                                intent.putExtra("selectedDate", selectedDateStr)
                                Log.i("DiaryHome", "→ 작성 중인 다이어리, Write 화면으로 이동")
                                context.startActivity(intent)
                            }
                            "COMPLETED" -> {
                                // 완료 → 상세 보기
                                val intent = Intent(context, DiaryDetailActivity::class.java)
                                intent.putExtra("sessionId", diaryItem.sessionId.toLong())
                                Log.i("DiaryHome", "→ 완료된 다이어리, 상세 화면으로 이동 (sessionId = ${diaryItem.sessionId})")
                                context.startActivity(intent)
                            }
                            else -> {
                                // 기본값 (혹시 모를 상태) → 상세 보기
                                val intent = Intent(context, DiaryDetailActivity::class.java)
                                intent.putExtra("sessionId", diaryItem.sessionId.toLong())
                                Log.i("DiaryHome", "→ 알 수 없는 상태(${diaryItem.status}), 상세 화면으로 이동")
                                context.startActivity(intent)
                            }
                        }
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

    // 로딩 다이얼로그
    if (isLoading) {
        LoadingDialog(message = "다이어리 불러오는 중...")
    }
}

// ===================== 히스토리 카드 =====================

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
                val intScore = score.toInt()
                val boxColor = when (intScore) {
                    in 0..19 -> Color(0xFFC2B1FF)
                    in 20..39 -> Color(0xFFED7E1C)
                    in 40..59 -> Color(0xFFC0A091)
                    in 60..79 -> Color(0xFFFFCE5C)
                    in 80..100 -> Color(0xFF9BB167)
                    else -> Color.Gray
                }

                Box(
                    modifier = Modifier
                        .background(boxColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (intScore >= 0) "$intScore 점" else "-",
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

// ===================== 캘린더 =====================

@Composable
fun DiaryCalendar2(
    calendar: Calendar,
    scoreMap: Map<String, Float> = emptyMap(),  // 날짜별 점수
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

                        val todayCal = Calendar.getInstance()

                        // 오늘인지 체크
                        val isToday = thisDate.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                                thisDate.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                                thisDate.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH)

                        val isFuture = thisDate.after(todayCal)

                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                        val thisDateStr = sdf.format(thisDate.time)

                        val score = scoreMap[thisDateStr]          // null이면 일기 없음
                        val hasDiary = score != null
                        val intScore = score?.toInt() ?: -1

                        val baseCircleColor = when (100 - intScore) {
                            in 0..19 -> Color(0xFF9BB167)
                            in 20..39 -> Color(0xFFFFCE5C)
                            in 40..59 -> Color(0xFFC0A091)
                            in 60..79 -> Color(0xFFED7E1C)
                            in 80..100 -> Color(0xFFC2B1FF)
                            else -> Color.White                  // 일기 없음
                        }

                        val circleColor = if (isFuture) {
                            Color(0xFFE0E0E0)                    // 미래는 회색
                        } else {
                            baseCircleColor
                        }

                        // ✅ 클릭 가능 조건
                        val clickableEnabled = when {
                            isFuture -> false                    // 미래 X
                            isToday -> true                      // 오늘은 일기 없어도 O
                            else -> hasDiary                     // 과거는 일기 있는 날만 O
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(circleColor, CircleShape)
                                .then(
                                    if (isToday) Modifier.border(
                                        width = 2.dp,
                                        color = Color(0xFF4F3422), // 은은한 진한 브라운
                                        shape = CircleShape
                                    ) else Modifier
                                )
                                .clickable(enabled = clickableEnabled) {
                                    onDateClick(date.toInt())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = when {
                                    isFuture -> Color(0xFFB0A8A4)
                                    !hasDiary && !isToday -> Color(0xFFB0A8A4)   // 일기 없는 과거
                                    else -> Color(0xFF4F3422)
                                }
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

// ===================== 감정 범례 =====================

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
