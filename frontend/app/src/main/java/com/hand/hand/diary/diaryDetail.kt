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
import androidx.compose.foundation.shape.CircleShape
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

        val sessionId = intent.getLongExtra("sessionId", -1L)
        Log.i("DiaryDetail", "📌 전달받은 sessionId = $sessionId")

        if (sessionId == -1L) {
            Log.i("DiaryDetail", "❌ sessionId 전달 실패 — 화면 종료")
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
                        Text(
                            text = "데이터를 불러올 수 없습니다.",
                            fontSize = 18.sp,
                            color = Color.Red
                        )
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
        // 헤더 배경 이미지
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

        // 뒤로가기 버튼
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
            text = diaryDetail.sessionDate ?: "",
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (screenHeight.value * 0.03f).sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = backButtonPaddingStart + backButtonSize + 18.dp,
                    top = backButtonPaddingTop + (backButtonSize / 4)
                )
        )

        // 중앙 제목 + 감정 태그
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
                fontSize = (screenHeight.value * 0.035f).sp,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = (screenHeight.value * 0.04f).sp
            )
            Spacer(modifier = Modifier.height(screenHeight * 0.015f))

            val mainEmotion = diaryDetail.emotions?.let { emotions ->
                val map = mapOf(
                    "기쁨" to (emotions.joy ?: 0.0),
                    "당황" to (emotions.embarrassment ?: 0.0),
                    "분노" to (emotions.anger ?: 0.0),
                    "불안" to (emotions.anxiety ?: 0.0),
                    "상처" to (emotions.hurt ?: 0.0),
                    "슬픔" to (emotions.sadness ?: 0.0)
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
                    fontSize = (screenHeight.value * 0.025f).sp,
                    color = Color.Black
                )
            }
        }

        // 본문 영역 (스크롤)
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
                fontSize = (screenHeight.value * 0.02f).sp,
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
            } ?: List(6) { 0.0 }

            val emotionColors = listOf(
                Color(0xFF9BB167),
                Color(0xFFFFCE5C),
                Color(0xFFED7E1C),
                Color(0xFFC0A091),
                Color(0xFF815EFF),
                Color(0xFF797876)
            )

            val safeValues = emotionValues.map { it ?: 0.0 }
            val maxValue = (safeValues.maxOrNull() ?: 1.0).toFloat()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.15f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                safeValues.forEachIndexed { index, value ->
                    Box(
                        modifier = Modifier
                            .width(screenWidth * 0.1f)
                            .height(screenHeight * 0.15f * (value.toFloat() / maxValue))
                            .background(
                                color = emotionColors[index],
                                shape = RoundedCornerShape(100.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 감정 범례
            EmotionLegend2()

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            // 감정 다이어리
            Text(
                text = "감정 다이어리",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight.value * 0.02f).sp,
                color = Color(0xFF4F3422),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(30.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = diaryDetail.longSummary ?: "",
                    fontFamily = BrandFontFamily,
                    fontSize = (screenHeight.value * 0.018f).sp,
                    color = Color(0xFF4F3422),
                    lineHeight = (screenHeight.value * 0.025f).sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 감정 조언
            Text(
                text = "감정 조언",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight.value * 0.02f).sp,
                color = Color(0xFF4F3422),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(30.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = diaryDetail.emotionalAdvice ?: "",
                    fontFamily = BrandFontFamily,
                    fontSize = (screenHeight.value * 0.018f).sp,
                    color = Color(0xFF4F3422),
                    lineHeight = (screenHeight.value * 0.025f).sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 수정/삭제 버튼 (독립 배치, 반응형)
        val buttonWidth = 130.dp
        val buttonHeight = 70.dp
        val buttonOffsetY = screenHeight * 0.31f

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = buttonOffsetY),
            horizontalArrangement = Arrangement.spacedBy(100.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.diary_rewrite_btn),
                contentDescription = "Diary Rewrite",
                modifier = Modifier
                    .size(width = buttonWidth, height = buttonHeight)
                    .clickable { Log.i("DiaryDetailScreen", "수정 버튼 클릭") }
            )

            Image(
                painter = painterResource(id = R.drawable.diary_delete_btn),
                contentDescription = "Diary Delete",
                modifier = Modifier
                    .size(width = buttonWidth, height = buttonHeight)
                    .clickable { Log.i("DiaryDetailScreen", "삭제 버튼 클릭") }
            )
        }

        // 우울 점수 아이콘 (상단 중앙)
        DepressionIcon(
            depressionScore = diaryDetail.depressionScore?.toInt(),
            modifier = Modifier
                .size(screenHeight * 0.12f)
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.3f)
        )

        // NavBar
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavBar()
        }
    }
}

@Composable
fun DepressionIcon(
    depressionScore: Int?,
    modifier: Modifier = Modifier,
) {
    val score = depressionScore ?: -1
    val diaryScore = 100 - score

    val imageRes = when (diaryScore.coerceIn(0, 100)) {
        in 0..19 -> R.drawable.diary_sad_icon
        in 20..39 -> R.drawable.diary_down_icon
        in 40..59 -> R.drawable.diary_okay_icon
        in 60..79 -> R.drawable.diary_happy_icon
        else -> R.drawable.diary_great_icon
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Depression Icon",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun EmotionLegend2() {
    val emotions = listOf(
        Pair(Color(0xFF9BB167), "기쁨"),
        Pair(Color(0xFFFFCE5C), "당황"),
        Pair(Color(0xFFED7E1C), "분노"),
        Pair(Color(0xFFC0A091), "불안"),
        Pair(Color(0xFF815EFF), "상처"),
        Pair(Color(0xFF797876), "슬픔")
    )

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val circleSize = screenWidth * 0.03f
    val textSize = (screenWidth.value / 27).sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.00f),
        horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.046f) // 항목 간 간격
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
