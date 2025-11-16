package com.hand.hand.diary

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.api.GMS.GmsSttManager
import com.hand.hand.api.Write.DiaryAnswerResponse
import com.hand.hand.api.Write.DiaryStartResponse
import com.hand.hand.api.Write.WriteManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


import com.hand.hand.ui.theme.BrandFontFamily

class DiaryWriteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1001
            )
        }

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
 * 공백 기준 줄바꿈
 */
fun autoWrapText(text: String, maxCharPerLine: Int): String {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""

    for (word in words) {
        if ((currentLine + word).length > maxCharPerLine) {
            lines.add(currentLine.trim())
            currentLine = ""
        }
        currentLine += "$word "
    }

    if (currentLine.isNotEmpty()) lines.add(currentLine.trim())
    return lines.joinToString("\n")
}

@Composable
fun DiaryWriteScreen(selectedDate: String, onBackClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val context = LocalContext.current

    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStart: Dp = screenWidth * 0.07f
    val backButtonPaddingTop: Dp = screenHeight * 0.05f

    var isRecording by remember { mutableStateOf(false) }   // 녹음 중 여부
    var isSending by remember { mutableStateOf(false) }     // STT + answer 전송 중 여부
    var showExitDialog by remember { mutableStateOf(false) }

    // 백엔드 질문 리스트
    var questions by remember { mutableStateOf<List<String>>(emptyList()) }

    // 세션 / 질문 번호
    var sessionId by remember { mutableStateOf<Long?>(null) }
    var questionNumber by remember { mutableStateOf(0) }

    // 다이어리 세션 시작
    LaunchedEffect(Unit) {
        WriteManager.startDiary(
            onSuccess = { res: DiaryStartResponse ->
                Log.d("DiaryWrite", "다이어리 시작 성공: $res")

                val data = res.data
                if (res.success && data != null) {
                    sessionId = data.sessionId
                    questionNumber = data.questionNumber
                    questions = listOf(data.questionText)
                } else {
                    questions = listOf("질문을 불러오지 못했어요.")
                }
            },
            onFailure = { t ->
                Log.e("DiaryWrite", "다이어리 시작 실패", t)
                questions = listOf("질문을 불러오는 중 오류가 발생했어요.")
            }
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
                    // 아이콘
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

                    // 질문 텍스트 박스
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

        // 🔴 하단 녹음 버튼 (마이크 ↔ 체크)
        Image(
            painter = painterResource(
                id = if (isRecording)
                    R.drawable.diary_write_record_stop   // 체크 아이콘 (녹음 중)
                else
                    R.drawable.diary_write_record_btn     // 마이크 아이콘 (대기)
            ),
            contentDescription = "Record Button",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = screenHeight * 0.02f)
                .size(screenHeight * 0.09f)
                .clickable {

                    // STT + answer 전송 중이면 클릭 막기
                    if (isSending) {
                        Log.d("DiaryWrite", "지금 전송 중이라 클릭 무시")
                        return@clickable
                    }

                    if (!isRecording) {
                        // 1차 클릭: 녹음 시작
                        isRecording = true
                        Log.d("DiaryWrite", "🎙 녹음 시작")
                        RecordManager.startRecording(context)

                    } else {
                        // 2차 클릭: 녹음 종료 + GMS STT + answer POST
                        isRecording = false
                        Log.d("DiaryWrite", "🎙 녹음 종료, STT 요청 준비")

                        val audioFile = RecordManager.stopRecording()
                        if (audioFile == null) {
                            Log.e("DiaryWriteDebug", "[REC FAIL] 녹음 파일 null")
                            Toast.makeText(context, "녹음에 실패했어요. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }

                        isSending = true

                        GmsSttManager.requestStt(
                            audioFile = audioFile,
                            onSuccess = { text ->
                                Log.d("DiaryWrite", "GMS STT 결과: '$text'")

                                val currentSessionId = sessionId
                                if (currentSessionId == null || text.isBlank()) {
                                    Log.e(
                                        "DiaryWrite",
                                        "STT 이후 sessionId 없거나 text 비어있음: sessionId=$currentSessionId, text='$text'"
                                    )
                                    Toast.makeText(context, "음성을 인식하지 못했어요.", Toast.LENGTH_SHORT).show()
                                    isSending = false
                                    return@requestStt
                                }

                                // 👉 답변 POST
                                WriteManager.sendAnswer(
                                    sessionId = currentSessionId,
                                    answerText = text,
                                    onSuccess = { res: DiaryAnswerResponse ->
                                        Log.d("DiaryWrite", "answer 성공: $res")
                                        isSending = false

                                        if (res.success && res.data != null) {
                                            sessionId = res.data.sessionId
                                            questionNumber = res.data.questionNumber
                                            questions = questions + res.data.questionText

                                            if (res.data.canFinish) {
                                                Log.d("DiaryWrite", "이제 다이어리 종료 가능")
                                            }
                                        } else {
                                            Toast.makeText(context, "답변 전송에 실패했어요.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onFailure = { t ->
                                        Log.e("DiaryWrite", "answer 실패", t)
                                        Toast.makeText(context, "답변 전송에 실패했어요.", Toast.LENGTH_SHORT).show()
                                        isSending = false
                                    }
                                )
                            },
                            onFailure = { t ->
                                Log.e("DiaryWrite", "GMS STT 요청 실패", t)
                                Toast.makeText(context, "음성 인식에 실패했어요.", Toast.LENGTH_SHORT).show()
                                isSending = false
                            }
                        )
                    }
                },
            contentScale = ContentScale.Fit
        )

        // ⚪ 나가기 모달
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
