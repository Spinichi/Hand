package com.hand.wear

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Scaffold
import com.hand.wear.components.BackgroundCircles
import java.util.Locale
import com.hand.hand.R

// CareEx2Activity
class CareEx2Activity : ComponentActivity() {

    private var tts: TextToSpeech? = null
    private val ttsInitialized = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.KOREAN)

                tts?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )

                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    // ✅ 발화 완료 후 다음 화면으로 자동 이동
                    tts?.setOnUtteranceProgressListener(object :
                        android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}

                        override fun onDone(utteranceId: String?) {
                            runOnUiThread {
                                startActivity(Intent(this@CareEx2Activity, Care1Activity::class.java))
                            }
                        }

                        override fun onError(utteranceId: String?) {}
                    })
                    ttsInitialized.value = true
                }
            }
        }

        setContent {
            val ttsReady by ttsInitialized
            CareEx2Screen(tts, ttsReady)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// 폰트 정의
val KyonggiFont0 = FontFamily(Font(R.font.kyonggi_medium))

// ✅ TTS 확장 함수
fun TextToSpeech?.readTextEx2(text: String) {
    this?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CareEx2TTS")
}

// ✅ Composable
@Composable
fun CareEx2Screen(tts: TextToSpeech?, ttsReady: Boolean) {
    val context = LocalContext.current
    Scaffold {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F4F2)),
            contentAlignment = Alignment.Center
        ) {
            val screenHeight = this.maxHeight
            val screenWidth = this.maxWidth

            // 배경 원
            BackgroundCircles(screenWidth = screenWidth, screenHeight = screenHeight)

            Box(modifier = Modifier.fillMaxSize()) {
                TopButtonImage(
                    screenHeight = screenHeight,
                    context = context,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // ✅ 중앙 텍스트
            val displayText = "배를 이용해 숨을 \n 쉬는 것이 중요합니다."
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(screenHeight * 0.05f))
                Text(
                    text = displayText,
                    color = Color(0xFF4F3422),
                    fontSize = (screenHeight.value * 0.1).sp,
                    lineHeight = (screenHeight.value * 0.12).sp,
                    fontFamily = KyonggiFont0,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    style = androidx.compose.ui.text.TextStyle(
                        lineBreak = LineBreak.Simple
                    )
                )
            }

            // ✅ TTS 실행 (볼륨 0일 땐 실행 안 함)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val isMuted = (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0) == 0

            LaunchedEffect(ttsReady) {
                if (ttsReady && !isMuted) {
                    tts?.readTextEx2(displayText)
                }
            }
        }
    }
}
