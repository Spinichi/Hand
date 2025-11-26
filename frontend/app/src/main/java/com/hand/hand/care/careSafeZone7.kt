package com.hand.hand.care

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.Locale

class CareSafeZone7Activity : ComponentActivity() {

    // TTS 객체 & 준비 여부 상태
    private var tts: TextToSpeech? = null
    private val ttsInitialized = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.KOREAN)
                if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    ttsInitialized.value = true
                } else {
                    Toast.makeText(this, "한국어 TTS를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "TTS 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            val ttsReady by ttsInitialized
            CareSafeZone7Screen(
                onBackClick = { finish() },
                onStartClick = {
                    startActivity(Intent(this, CareSafeZone8Activity::class.java))
                },
                tts = tts,
                ttsReady = ttsReady
            )
        }
    }

    override fun onPause() {
        super.onPause()
        tts?.stop()   // 다른 화면으로 넘어가는 순간 TTS 끊기
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// 이 화면 문장을 읽어주는 TTS 확장 함수
fun TextToSpeech?.readSafeZone7Text() {
    val ttsText =
        "마음 속에 쉴 수 있는 공간이 생겼습니다. " +
                "이제 스트레스를 줄이는 연습을 해 볼까요? " +
                "불편감 점수를 50점 이하로 만드는 사건을 떠올려 보세요. " +
                "그곳의 장면, 소리, 향기, 신체 감각에 집중해 봅시다."
    this?.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null, "SafeZone7TTS")
}

@Composable
fun CareSafeZone7Screen(
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    tts: TextToSpeech?,
    ttsReady: Boolean
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val headerHeight = screenHeight * 0.25f

    var discomfortScore by remember { mutableStateOf(TextFieldValue("")) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 페이지 들어오고 TTS 준비되면 자동으로 한 번 읽기
    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            android.util.Log.d("CareSafeZone7", "TTS ready, speaking SafeZone7 text.")
            tts.readSafeZone7Text()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        // 헤더
        CareHeader2(
            titleText = "안전지대 연습",
            subtitleText = "마무리", // 아이콘 없이 텍스트만
            onBackClick = onBackClick
        )

        // 본문 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = headerHeight,
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.05f,
                    bottom = 80.dp
                )
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            // Level 이미지
            Image(
                painter = painterResource(id = R.drawable.safe_zone_level_5),
                contentDescription = "Safe Zone Level",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 안내 텍스트 1
            Text(
                text = "마음 속에 쉴 수 있는 공간이 생겼습니다. \n" +
                        "이제 스트레스를 줄이는 연습을 해볼까요?",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (screenHeight * 0.023f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.025f))

            // 안내 텍스트 2
            Text(
                text = "불편감 점수를 50점 이하로 만드는 \n사건을 떠올려 보세요",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (screenHeight * 0.023f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.025f))

            // 안내 텍스트 3
            Text(
                text = "그곳의 장면, 소리, 향기, \n신체 감각에 집중해 봅시다",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.03f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.025f))

            Image(
                painter = painterResource(id = R.drawable.safe_mind_icon),
                contentDescription = "Safe Mind Icon",
                modifier = Modifier
                    .height(screenHeight * 0.15f)   // 화면 높이에 비례
                    .aspectRatio(1f)                // 정사각형 유지
                    .align(Alignment.CenterHorizontally)
            )
        }

        // 하단 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.05f
                )
        ) {
            val buttonHeight = screenHeight * 0.065f
            val arrowHeight = buttonHeight * 0.4f
            val arrowWidth = arrowHeight * (24f / 24f)

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F3422),
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "다음",
                        fontSize = (screenHeight * 0.022f).value.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Image(
                        painter = painterResource(id = R.drawable.arrow_right_white),
                        contentDescription = "Arrow Right",
                        modifier = Modifier
                            .height(arrowHeight)
                            .width(arrowWidth)
                    )
                }
            }
        }
    }
}
