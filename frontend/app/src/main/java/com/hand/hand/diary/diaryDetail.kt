package com.hand.hand.diary

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.api.Diary.DiaryDetailResponse
import com.hand.hand.api.Diary.DiaryManager
import com.hand.hand.nav.NavBar
import com.hand.hand.ui.theme.BrandFontFamily

class DiaryDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionId = intent.extras?.get("sessionId")?.toString()?.toLongOrNull() ?: -1L
        Log.i("DiaryDetail", "📌 전달받은 sessionId = $sessionId")

        if (sessionId == -1L) {
            Log.e("DiaryDetail", "❌ sessionId 전달 실패 — 화면 종료")
            finish()
            return
        }

        setContent {
            var diaryDetail by remember { mutableStateOf<DiaryDetailResponse?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                Log.i("DiaryDetail", "📡 getDiaryDetail API 호출 시작 (sessionId=$sessionId)")

                DiaryManager.getDiaryDetail(
                    sessionId = sessionId,
                    onSuccess = { response ->
                        Log.i("DiaryDetail", "✅ API 응답 성공: $response")
                        diaryDetail = response
                        isLoading = false
                    },
                    onFailure = { t ->
                        Log.e("DiaryDetail", "❌ API 응답 실패: ${t.message}")
                        isLoading = false
                        t.printStackTrace()
                    }
                )
            }

            diaryDetail?.let {
                DiaryDetailScreen(
                    diaryDetail = it,
                    onBackClick = { finish() }
                )
            } ?: run {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "로딩 중...", fontSize = 18.sp)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "데이터를 불러올 수 없습니다.", fontSize = 18.sp, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryDetailScreen(diaryDetail: DiaryDetailResponse, onBackClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val backButtonSize = screenHeight * 0.06f
    val backButtonPaddingStart = screenWidth * 0.07f
    val backButtonPaddingTop = screenHeight * 0.05f
    val navBarHeight = screenHeight * 0.12f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {

        // 🔶 헤더 배경 이미지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.6f)
                .offset(y = (-screenHeight * 0.25f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_circle_orange),
                contentDescription = "Background Circle",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 🔹 뒤로가기 버튼
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "Back Button",
            modifier = Modifier
                .padding(start = backButtonPaddingStart, top = backButtonPaddingTop)
                .size(backButtonSize)
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
        )

        // 날짜 텍스트
        Text(
            text = diaryDetail.sessionDate,
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

        // 🔹 중앙 제목 + 감정 태그
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.14f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = diaryDetail.shortSummary ?: "",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.035f).value.sp,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = (screenHeight * 0.04f).value.sp
            )
            Spacer(modifier = Modifier.height(screenHeight * 0.015f))

            // 🔥 감정 null 방어
            val mainEmotion = diaryDetail.emotions?.let { emotions ->
                val map = mapOf(
                    "기쁨" to emotions.joy,
                    "당황" to emotions.embarrassment,
                    "분노" to emotions.anger,
                    "불안" to emotions.anxiety,
                    "상처" to emotions.hurt,
                    "슬픔" to emotions.sadness
                )
                map.maxByOrNull { it.value }?.key ?: "감정 없음"
            } ?: "감정 없음"

            Box(
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = mainEmotion,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color.Black
                )
            }
        }

        // 🔸 본문 영역 (스크롤)
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = screenHeight * 0.41f,
                    bottom = navBarHeight + 40.dp
                )
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {

            // 감정 분석
            Text(
                text = "감정 분석",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color(0xFF4F3422),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            val emotionValues = diaryDetail.emotions?.let { emotions ->
                listOf(
                    emotions.joy,
                    emotions.embarrassment,
                    emotions.anger,
                    emotions.anxiety,
                    emotions.hurt,
                    emotions.sadness
                )
            } ?: listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

            val emotionColors = listOf(
                Color(0xFF9BB167),
                Color(0xFFFFCE5C),
                Color(0xFFED7E1C),
                Color(0xFFC0A091),
                Color(0xFFC2B1FF),
                Color(0xFF928D86)
            )

            val maxValue = (emotionValues.maxOrNull() ?: 1.0).toFloat()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.15f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                emotionValues.forEachIndexed { index, value ->
                    Box(
                        modifier = Modifier
                            .width(screenWidth * 0.1f)
                            .height(screenHeight * 0.15f * (value / maxValue).toFloat())
                            .background(color = emotionColors[index], shape = RoundedCornerShape(100.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            // 감정 다이어리
            Text(
                text = "감정 다이어리",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color(0xFF4F3422),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(color = Color.White, shape = RoundedCornerShape(30.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = diaryDetail.longSummary ?: "",
                    fontFamily = BrandFontFamily,
                    fontSize = (screenHeight * 0.018f).value.sp,
                    color = Color(0xFF4F3422),
                    lineHeight = (screenHeight * 0.025f).value.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 감정 조언
            Text(
                text = "감정 조언",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color(0xFF4F3422),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(color = Color.White, shape = RoundedCornerShape(30.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = diaryDetail.emotionalAdvice ?: "",
                    fontFamily = BrandFontFamily,
                    fontSize = (screenHeight * 0.018f).value.sp,
                    color = Color(0xFF4F3422),
                    lineHeight = (screenHeight * 0.025f).value.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 🔹 화면 하단 NavBar
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavBar()
        }
    }
}

// 확장 함수
@Composable
fun Double.toDp(): Dp = (this * LocalConfiguration.current.screenHeightDp).dp
