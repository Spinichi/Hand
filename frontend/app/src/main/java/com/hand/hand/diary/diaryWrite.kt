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
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * ✅ 공백 기준으로 자연스럽게 줄바꿈하는 함수
 * @param text 줄바꿈 처리할 문자열
 * @param maxCharPerLine 한 줄당 최대 문자 수
 */
fun autoWrapText(text: String, maxCharPerLine: Int): String {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""

    for (word in words) {
        // 현재 줄에 단어를 추가했을 때 최대 글자 수를 넘으면 줄바꿈
        if ((currentLine + word).length > maxCharPerLine) {
            lines.add(currentLine.trim())
            currentLine = ""
        }
        currentLine += "$word "
    }

    if (currentLine.isNotEmpty()) lines.add(currentLine.trim())

    // 줄바꿈으로 연결해서 반환
    return lines.joinToString("\n")
}

@Composable
fun DiaryWriteScreen(selectedDate: String, onBackClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStart: Dp = screenWidth * 0.07f
    val backButtonPaddingTop: Dp = screenHeight * 0.05f

    var isRecording by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // ✅ 여러 질문 관리
    var questions by remember {
        mutableStateOf(
            listOf(
                "오늘 있었던 일 중에 기억에 남는 순간이 있나요?",
                "그때 어떤 감정이 들었나요?",
                "그 감정은 왜 그렇게 느꼈던 걸까요?"
            )
        )
    }

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
                .clickable { showExitDialog = true }
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

        // 🟠 본문 제목
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

        // 🟢 감정 질문 리스트
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.07f,
                    top = screenHeight * 0.22f,
                    bottom = screenHeight * 0.15f
                ),
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f)
        ) {
            questions.forEachIndexed { index, question ->
                val isLast = index == questions.lastIndex

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 🔹 아이콘: 마지막 질문만 주황색
                    Image(
                        painter = painterResource(
                            id = if (isLast)
                                R.drawable.diary_question
                            else
                                R.drawable.diary_question_check
                        ),
                        contentDescription = "Question Icon",
                        modifier = Modifier.size(screenHeight * 0.06f)
                    )

                    Spacer(modifier = Modifier.width(screenWidth * 0.03f))

                    // 🔸 질문 텍스트 박스
                    Box(
                        modifier = Modifier
                            .width(screenWidth * 0.7f)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White)
                            .padding(
                                vertical = screenHeight * 0.015f,
                                horizontal = screenWidth * 0.07f
                            )
                    ) {
                        Text(
                            text = autoWrapText(question, 20),
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = (screenHeight * 0.018f).value.sp,
                            color = Color(0xFF4F3422).copy(alpha = if (isLast) 1f else 0.5f),
                            lineHeight = (screenHeight * 0.03f).value.sp,
                            softWrap = true,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 🟡 하단 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.diary_write_bottom),
            contentDescription = "Bottom Decoration",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        // 🔴 하단 녹음 버튼 (토글)
        // 🔴 하단 녹음 버튼 (토글)
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
                    // ✅ 녹음 종료 시에만 새로운 질문 추가
                    if (isRecording) {
                        questions = questions + "새로운 질문이 도착했어요!"
                    }

                    // 🔁 녹음 상태 토글
                    isRecording = !isRecording
                },
            contentScale = ContentScale.Fit
        )


        // ⚪ 모달 표시
        if (showExitDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFFF7F4F2))
                        .padding(
                            vertical = screenHeight * 0.05f,
                            horizontal = screenWidth * 0.1f
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "다이어리 작성을\n완료 하시겠습니까?",
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.035f).value.sp,
                        lineHeight = (screenHeight * 0.05f).value.sp,
                        color = Color(0xFF4F3422),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.035f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.diary_write_x),
                            contentDescription = "Cancel Button",
                            modifier = Modifier
                                .size(screenHeight * 0.07f)
                                .clickable { showExitDialog = false },
                            contentScale = ContentScale.Fit
                        )

                        Image(
                            painter = painterResource(id = R.drawable.diary_write_check),
                            contentDescription = "Confirm Button",
                            modifier = Modifier
                                .size(screenHeight * 0.07f)
                                .clickable {
                                    showExitDialog = false
                                    onBackClick()
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

