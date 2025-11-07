package com.hand.hand.diary

import android.os.Bundle
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
import androidx.compose.runtime.Composable
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
import com.hand.hand.nav.NavBar
import com.hand.hand.ui.theme.BrandFontFamily

class DiaryDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedDate = intent.getStringExtra("selectedDate") ?: "날짜 없음"

        setContent {
            DiaryDetailScreen(
                selectedDate = selectedDate,
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun DiaryDetailScreen(selectedDate: String, onBackClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStart: Dp = screenWidth * 0.07f
    val backButtonPaddingTop: Dp = screenHeight * 0.05f
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
            text = selectedDate,
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

        // 중앙 제목 + 감정 태그
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.14f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "아줌마가 날 밀고\n자기가 앉음",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.035f).value.sp,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = (screenHeight * 0.04f).value.sp
            )
            Spacer(modifier = Modifier.height(screenHeight * 0.015f))
            Box(
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "화남",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // 화남 아래 Row (sad 아이콘과 수정/삭제 분리)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = screenHeight * 0.3f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.diary_sad_icon),
                contentDescription = "Sad Icon",
                modifier = Modifier
                    .size(screenHeight * 0.11f)
                    .clickable { }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 55.dp)
                    .offset(y = (-screenHeight * 0.01f)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.diary_rewrite_btn),
                    contentDescription = "Rewrite Button",
                    modifier = Modifier
                        .size(screenHeight * 0.07f)
                        .clickable { }
                )
                Image(
                    painter = painterResource(id = R.drawable.diary_delete_btn),
                    contentDescription = "Delete Button",
                    modifier = Modifier
                        .size(screenHeight * 0.07f)
                        .clickable { }
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
                    .padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )

            // 감정 분석 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.15f)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                val backgroundColors = listOf(
                    Color(0xFFE5EAD7),
                    Color(0xFFFFF2D3),
                    Color(0xFFFDE3CD),
                    Color(0xFFEDE5E1),
                    Color(0xFFEDE8FF),
                    Color(0xFFEAE4DC)
                )
                val barMaxHeight = screenHeight * 0.15f
                val barWidth = screenWidth * 0.1f

                // 🔹 배경 막대
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (color in backgroundColors) {
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .fillMaxHeight()
                                .background(color = color, shape = RoundedCornerShape(50.dp))
                        )
                    }
                }

                // 🔹 실제 값 막대 (상대 비율)
                val barValues = listOf(0.2053f, 0.1368f, 0.0867f, 0.0289f, 0.2550f, 0.2873f)
                val maxBarValue = barValues.maxOrNull() ?: 1f
                val dataColors = listOf(
                    Color(0xFF9BB167),
                    Color(0xFFFFCE5C),
                    Color(0xFFED7E1C),
                    Color(0xFFC0A091),
                    Color(0xFFC2B1FF),
                    Color(0xFF928D86)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (i in barValues.indices) {
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(barMaxHeight * (barValues[i] / maxBarValue)) // 상대 비율 적용
                                .background(
                                    color = dataColors[i],
                                    shape = RoundedCornerShape(100.dp)
                                )
                        )
                    }
                }
            }

            val emotionItems = listOf(
                Pair(Color(0xFF9BB167), "기쁨"),
                Pair(Color(0xFFFFCE5C), "당황"),
                Pair(Color(0xFFED7E1C), "분노"),
                Pair(Color(0xFFC0A091), "불안"),
                Pair(Color(0xFFC2B1FF), "상처"),
                Pair(Color(0xFF928D86), "슬픔")
            )

// 🔹 반응형 거리 값
            val circleTextSpacing = screenWidth * 0.01f   // 원과 텍스트 사이 간격
            val groupSpacing = screenWidth * 0.07f        // 감정 그룹 간 간격
            val circleSize = screenHeight * 0.012f        // 원 크기 (조정 가능)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = screenHeight * 0.02f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                emotionItems.forEachIndexed { index, (color, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(circleSize)
                                .background(color = color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(circleTextSpacing))
                        Text(
                            text = label,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenHeight * 0.018f).value.sp,
                            color = Color(0xFF867E7A)
                        )
                    }

                    // 🔹 각 감정 그룹 사이 간격
                    if (index != emotionItems.lastIndex) {
                        Spacer(modifier = Modifier.width(groupSpacing))
                    }
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
                    .padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "오늘 버스를 탔는데, 내가 자리에 앉으려는 순간\n" +
                            "아줌마 한 분이 갑자기 나를 밀치고 먼저 앉았다.\n" +
                            "순간 너무 놀라고 기분이 좀 나빴다.\n" +
                            "나도 힘들었는데, 그냥 아무 말도 못 하고 서 있었다.\n" +
                            "조금 억울했지만, “괜찮아, 그냥 넘기자” 하고 마음을 다잡았다.\n" +
                            "다음엔 이런 상황에서도 침착하게 말할 수 있으면 좋겠다.",
                    fontFamily = BrandFontFamily,
                    fontSize = (screenHeight * 0.018f).value.sp,
                    color = Color(0xFF4F3422),
                    lineHeight = (screenHeight * 0.025f).value.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 🔹 화면 하단 고정 NavBar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            NavBar()
        }
    }
}
