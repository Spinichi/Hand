package com.hand.hand.diary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily

class DiaryWriteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedDate = intent.getStringExtra("selectedDate") ?: "날짜 없음"

        setContent {
            DiaryWriteScreen(
                selectedDate = selectedDate,
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun DiaryWriteScreen(selectedDate: String, onBackClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStart: Dp = screenWidth * 0.07f
    val backButtonPaddingTop: Dp = screenHeight * 0.05f

    // 🎙 녹음 상태
    var isRecording by remember { mutableStateOf(false) }

    // ⚫ 다이어리 완료 모달 상태
    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        // 🔶 헤더 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.15f)
                .background(
                    color = Color(0xFFEF8834),
                    shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                )
                .align(Alignment.TopCenter)
        )

        // 🔹 뒤로가기 버튼
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "Back Button",
            modifier = Modifier
                .padding(start = backButtonPaddingStart, top = backButtonPaddingTop)
                .size(backButtonSize)
                .align(Alignment.TopStart)
                .clickable {
                    showExitDialog = true // ✅ 모달 열기
                }
        )

        // 🔹 날짜 텍스트
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

        // 🟠 본문 텍스트
        Text(
            text = "감정 대화하기",
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (screenHeight * 0.022f).value.sp,
            color = Color(0xFF4F3422),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.07f,
                    top = screenHeight * 0.18f
                )
        )

        // 🟡 하단 고정 이미지 (배경)
        Image(
            painter = painterResource(id = R.drawable.diary_write_bottom),
            contentDescription = "Bottom Decoration",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        // 🔴 하단 녹음 버튼 (토글 기능)
        Image(
            painter = painterResource(
                id = if (isRecording)
                    R.drawable.diary_write_record_stop
                else
                    R.drawable.diary_write_record_btn
            ),
            contentDescription = "Record Button",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = screenHeight * 0.02f)
                .size(screenHeight * 0.09f)
                .clickable {
                    isRecording = !isRecording
                },
            contentScale = ContentScale.Fit
        )

        // ⚫ 다이어리 완료 모달
        if (showExitDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)) // ✅ 검정색 + 투명도 80%
                    .clickable(enabled = false) {} // 외부 클릭 방지
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(vertical = screenHeight * 0.03f, horizontal = screenWidth * 0.08f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "다이어리 작성을 완료 하시겠습니까?",
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.022f).value.sp,
                        color = Color(0xFF4F3422),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.1f)
                    ) {
                        Text(
                            text = "아니오",
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenHeight * 0.02f).value.sp,
                            color = Color(0xFFEF8834),
                            modifier = Modifier
                                .clickable { showExitDialog = false } // 닫기
                        )
                        Text(
                            text = "예",
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenHeight * 0.02f).value.sp,
                            color = Color(0xFF4F3422),
                            modifier = Modifier
                                .clickable {
                                    showExitDialog = false
                                    onBackClick() // ✅ Activity 종료
                                }
                        )
                    }
                }
            }
        }
    }
}
