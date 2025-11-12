package com.hand.hand.diary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.AiDocument.PrivateAiDocumentHomeActivity
import com.hand.hand.care.CareActivity
import com.hand.hand.ui.home.BottomTab
import com.hand.hand.ui.home.CurvedBottomNavBar
import com.hand.hand.ui.home.HomeActivity
import com.hand.hand.ui.theme.BrandFontFamily
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
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current

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
                    context.startActivity(Intent(context, HomeActivity::class.java))
                },
                onClickWrite = {
                    context.startActivity(Intent(context, DiaryHomeActivity::class.java))
                },
                onClickDiary = {
                    context.startActivity(Intent(context, PrivateAiDocumentHomeActivity::class.java))
                },
                onClickProfile = { /* TODO: 프로필 페이지 */ },
                onClickCenter = {
                    context.startActivity(Intent(context, CareActivity::class.java))
                }
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

            // 🔹 달력
            DiaryCalendar(
                calendar = calendar,
                onDateClick = { day ->
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH) + 1
                    val selectedDate = "${year}년 ${month}월 ${day}일"

                    val intent = if (year == 2025 && month == 11 && day == 1) {
                        Intent(context, DiaryWriteActivity::class.java)
                    } else {
                        Intent(context, DiaryDetailActivity::class.java)
                    }
                    intent.putExtra("selectedDate", selectedDate)
                    context.startActivity(intent)
                }
            )

            // 🔹 감정 상태 표시 줄
            EmotionLegend()

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            // 🔹 감정 다이어리 히스토리
            Text(
                text = "감정 다이어리 히스토리",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color(0xFF4F3422)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 🔹 예시 히스토리 박스 5개
            repeat(5) { index ->
                DiaryHistoryBox(day = 30 - index)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DiaryHistoryBox(day: Int) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.1f)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(
                color = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(screenHeight * 0.07f)
                    .background(
                        color = Color(0xFFF7F4F2),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${day}일",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "아줌마가 날 밀고 자기가 앉음",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.02f).value.sp,
                    color = Color(0xFF4F3422)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFA694F5),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "화남",
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
    val circleTextGap = screenWidth * 0.015f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.04f)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        emotions.forEach { (color, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .background(color = color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(circleTextGap))
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
