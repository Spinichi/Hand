package com.hand.hand.diary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hand.hand.R
import com.hand.hand.api.GMS.GmsSttManager
import com.hand.hand.api.Write.WriteManager
import com.hand.hand.ui.theme.BrandFontFamily
import android.speech.tts.TextToSpeech
import java.util.Locale

class DiaryWriteActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔸 TTS 초기화
        tts = TextToSpeech(this, this)

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
                onBackClick = { finish() },
                onSpeak = { text -> speak(text) }   // 🔸 여기서 TTS 호출 람다 내려줌
            )
        }
    }

    // 🔸 TTS 초기화 콜백
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.KOREAN)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            isTtsReady = false
        }
    }

    // 🔸 실제로 읽어주는 함수
    private fun speak(text: String) {
        if (!isTtsReady) {   // ❗ 여기 ! 붙는게 맞음
            Log.d("DiaryTTS", "TTS 아직 준비 안됨")
            return
        }
        if (text.isBlank()) return

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "DIARY_QUESTION"
        )
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}   // 🔸 여기서 Activity 클래스 끝!! (이 괄호가 빠져 있었음)

// ======================= 여기부터는 예전처럼 top-level 함수 =======================

/* 공백 기준 줄바꿈 */
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

/* 대화 끝내기 버튼 */
@Composable
fun EndConversationButton(
    modifier: Modifier = Modifier,
    questionCount: Int,
    onClick: () -> Unit
) {
    val isEnabled = questionCount >= 3
    val alpha = if (isEnabled) 1f else 0.5f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = alpha))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "대화 끝내기",
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = Color(0xFFEF8834).copy(alpha = alpha)
        )
    }
}

@Composable
fun DiaryWriteScreen(
    selectedDate: String,
    onBackClick: () -> Unit,
    onSpeak: (String) -> Unit      // 🔸 여기 새 파라미터 추가!!
) {

    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStart: Dp = screenWidth * 0.07f
    val backButtonPaddingTop: Dp = screenHeight * 0.05f

    var isRecording by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    var questions by remember { mutableStateOf<List<String>>(emptyList()) }
    var sessionId by remember { mutableStateOf<Long?>(null) }
    var questionNumber by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        WriteManager.startDiary(
            onSuccess = { res ->
                val data = res.data
                if (res.success && data != null) {
                    sessionId = data.sessionId
                    questionNumber = data.questionNumber
                    questions = listOf(data.questionText)

                    // 🔸 첫 질문을 바로 읽어주기
                    onSpeak(data.questionText)
                } else {
                    questions = listOf("질문을 불러오지 못했어요.")
                }
            },
            onFailure = {
                questions = listOf("질문을 불러오는 중 오류가 발생했어요.")
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {

        /* 🔶 헤더 */
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

        /* 🔙 뒤로가기 버튼 (즉시 뒤로가기) */
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "Back Button",
            modifier = Modifier
                .padding(start = backButtonPaddingStart, top = backButtonPaddingTop)
                .size(backButtonSize)
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
        )

        /* 📅 날짜 */
        androidx.compose.material3.Text(
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

        /* 제목 */
        androidx.compose.material3.Text(
            text = "감정 대화하기",
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (screenHeight * 0.022f).value.sp,
            color = Color(0xFF4F3422),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.07f,
                    top = screenHeight * 0.18f
                )
        )

        /* 🟢 질문 리스트 + 세로 스크롤 */
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.07f,
                    top = screenHeight * 0.22f,
                    bottom = screenHeight * 0.15f
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f)
        ) {
            questions.forEachIndexed { index, question ->
                val isLast = index == questions.lastIndex

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Image(
                        painter = painterResource(
                            id = if (isLast) R.drawable.diary_question
                            else R.drawable.diary_question_check
                        ),
                        contentDescription = "Question Icon",
                        modifier = Modifier.size(screenHeight * 0.06f)
                    )

                    Spacer(modifier = Modifier.width(screenWidth * 0.03f))

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
                        androidx.compose.material3.Text(
                            text = autoWrapText(question, 20),
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = (screenHeight * 0.018f).value.sp,
                            lineHeight = (screenHeight * 0.03f).value.sp,
                            color = Color(0xFF4F3422).copy(alpha = if (isLast) 1f else 0.5f),
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        /* 하단 이미지 */
        Image(
            painter = painterResource(id = R.drawable.diary_write_bottom),
            contentDescription = "Bottom Decoration",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        /* 녹음 버튼 */
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
                .padding(bottom = screenHeight * 0.11f)
                .size(screenHeight * 0.09f)
                .clickable {
                    if (isSending) return@clickable

                    if (!isRecording) {
                        isRecording = true
                        RecordManager.startRecording(context)
                    } else {
                        isRecording = false
                        val audioFile = RecordManager.stopRecording()
                            ?: return@clickable.also {
                                Toast.makeText(context, "녹음 실패", Toast.LENGTH_SHORT).show()
                            }

                        isSending = true

                        GmsSttManager.requestStt(
                            audioFile = audioFile,
                            onSuccess = { text ->
                                val currentSessionId = sessionId ?: return@requestStt
                                if (text.isBlank()) {
                                    Toast.makeText(context, "인식 실패", Toast.LENGTH_SHORT).show()
                                    isSending = false
                                    return@requestStt
                                }

                                WriteManager.sendAnswer(
                                    sessionId = currentSessionId,
                                    answerText = text,
                                    onSuccess = { res ->
                                        isSending = false
                                        if (res.success && res.data != null) {
                                            sessionId = res.data.sessionId
                                            questionNumber = res.data.questionNumber
                                            questions = questions + res.data.questionText

                                            // 🔸 새 질문도 TTS로 읽기
                                            onSpeak(res.data.questionText)
                                        }
                                    },
                                    onFailure = {
                                        isSending = false
                                        Toast.makeText(context, "오류", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onFailure = {
                                isSending = false
                                Toast.makeText(context, "STT 실패", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
        )

        /* 대화 끝내기 버튼 (가운데) */
        EndConversationButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = screenHeight * 0.04f),
            questionCount = questions.size,
            onClick = { showExitDialog = true }
        )

        /* 종료 모달 */
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
                    androidx.compose.material3.Text(
                        text = "다이어리 작성을\n완료 하시겠습니까?",
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.035f).value.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = (screenHeight * 0.05f).value.sp,
                        color = Color(0xFF4F3422)
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
                                .clickable { showExitDialog = false }
                        )

                        Image(
                            painter = painterResource(id = R.drawable.diary_write_check),
                            contentDescription = "Confirm Button",
                            modifier = Modifier
                                .size(screenHeight * 0.07f)
                                .clickable {
                                    showExitDialog = false

                                    val currentSessionId = sessionId
                                    if (currentSessionId == null) {
                                        Toast.makeText(
                                            context,
                                            "세션 정보가 없어요. 다시 시도해 주세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onBackClick()
                                    } else {
                                        WriteManager.completeDiary(
                                            sessionId = currentSessionId,
                                            onSuccess = { res ->
                                                if (res.success && res.data != null) {
                                                    Log.d("DiaryWrite", "다이어리 완료 성공: ${res.data}")

                                                    // ✅ 원하면 여기서 완료 요약을 읽어줄 수도 있어요
                                                    // onSpeak(res.data.shortSummary)

                                                    Toast.makeText(
                                                        context,
                                                        "다이어리가 완료되었어요.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    onBackClick()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        res.message ?: "완료 처리에 실패했어요.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            onFailure = { e ->
                                                Log.e("DiaryWrite", "다이어리 완료 실패", e)
                                                Toast.makeText(
                                                    context,
                                                    "완료 요청 중 오류가 발생했어요.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                }
                        )

                    }
                }
            }
        }
    }
}
