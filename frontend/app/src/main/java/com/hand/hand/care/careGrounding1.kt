// careGrounding1.kt

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.Locale

class CareGrounding1Activity : ComponentActivity() {

    companion object {
        var groundingSessionId: Long? = null
        var beforeStressLevel: Int? = null
        var beforeStressTimestamp: Long? = null
    }

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
            CareGrounding1Screen(
                onBackClick = { finish() },
                onStartClick = {
                    // TODO: 다음 화면으로 이동 (CareGrounding2Activity 구현 시)
                    Toast.makeText(this, "착지 연습 시작", Toast.LENGTH_SHORT).show()
                },
                tts = tts,
                ttsReady = ttsReady
            )
        }
    }

    override fun onPause() {
        super.onPause()
        tts?.stop()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// TTS 확장 함수
fun TextToSpeech?.readGroundingText() {
    val text = "착지법은 땅에 발을 딛고 있는 것을 느끼면서 지금 여기로 돌아오는 거예요. 발바닥을 바닥에 붙이고, 발이 땅에 닿아있는 느낌에 집중하세요."
    this?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "Grounding1TTS")
}

@Composable
fun CareGrounding1Screen(
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    tts: TextToSpeech?,
    ttsReady: Boolean
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val headerHeight = screenHeight * 0.25f

    // 페이지 들어오고 TTS 준비되면 자동으로 한 번 읽기
    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            android.util.Log.d("CareGrounding1", "TTS ready, speaking text.")
            tts.readGroundingText()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        CareHeader2(
            titleText = "착지 연습",
            subtitleTags = listOf(
                TagWithIcon("지금 여기", R.drawable.stress_icon),
                TagWithIcon("발바닥 집중", R.drawable.home_icon),
                TagWithIcon("현실 감각", R.drawable.stress_icon)
            ),
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = headerHeight,
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.05f,
                    bottom = 80.dp
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "착지법은 땅에 발을 딛고 있는 것을 \n 느끼면서 '지금 여기'로 \n 돌아오는 거예요.",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.035f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.04f))

            Text(
                text = "발바닥을 바닥에 붙이고, \n 발이 땅에 닿아있는 \n 느낌에 집중하세요.",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (screenHeight * 0.035f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )
        }

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
                        text = "시작하기",
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
