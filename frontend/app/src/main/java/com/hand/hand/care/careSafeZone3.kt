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

class CareSafeZone3Activity : ComponentActivity() {

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
            CareSafeZone3Screen(
                onBackClick = { finish() },
                onStartClick = {
                    startActivity(Intent(this, CareSafeZone4Activity::class.java))
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

// SafeZone3 안내문을 읽어주는 확장 함수
fun TextToSpeech?.readSafeZone3Text() {
    val ttsText =
        "아무한테도 방해받지 않고 편안하고 안전하게 느껴지는 장소를 떠올리세요. " +
                "잠시 숨을 고르며 그 장소를 떠올려 봅니다. " +
                "가보았던 장소도 좋고 상상의 장소도 좋습니다. " +
                "고요하고 안전하게 느껴지는 곳을 생각하세요. " +
                "바닷가나 편안한 방일 수도 있습니다."
    this?.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null, "SafeZone3TTS")
}

@Composable
fun CareSafeZone3Screen(
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
            android.util.Log.d("CareSafeZone3", "TTS ready, speaking SafeZone3 text.")
            tts.readSafeZone3Text()
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
            subtitleText = "안전지대 정하기", // 아이콘 없이 텍스트만
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
                .imePadding(), // 키보드에 맞춰 padding 적용
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))
            Image(
                painter = painterResource(id = R.drawable.safe_zone_level_1),
                contentDescription = "Safe Zone Level",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "아무한테도 방해받지 않고 편안하고 \n" +
                        "안전하게 느껴지는 장소를 떠올리세요. \n\n" +
                        "가보았던 장소도 좋고 상상의 장소도 \n" +
                        "좋습니다. 고요하고 안전하게 느껴지는 곳을 \n" +
                        "생각하세요. 바닷가나 편안한 방일 수도 \n" +
                        "있습니다.",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (screenHeight * 0.023f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )
        }

        // 하단 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = screenWidth * 0.05f, end = screenWidth * 0.05f)
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
