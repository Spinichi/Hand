package com.hand.wear

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Scaffold
import com.hand.hand.R
import com.hand.wear.components.BackgroundCircles
import java.util.Locale

class Care3Activity : ComponentActivity() {

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
                    // ✅ 발화 완료 리스너
                    tts?.setOnUtteranceProgressListener(object :
                        android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}

                        override fun onDone(utteranceId: String?) {
                            runOnUiThread {
                                // ✅ 음성 끝나면 Care3Activity로 이동
                                startActivity(Intent(this@Care3Activity, Care4Activity::class.java))
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
            Care3Screen(tts, ttsReady)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// ✅ 폰트 정의
val KyonggiFont3 = FontFamily(Font(R.font.kyonggi_medium))

// ✅ TTS 확장 함수
fun TextToSpeech?.readText3(text: String) {
    this?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "Care2TTS")
}

// ✅ Composable
@Composable
fun Care3Screen(tts: TextToSpeech?, ttsReady: Boolean) {
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

            // ✅ 배경 원
            BackgroundCircles(screenWidth = screenWidth, screenHeight = screenHeight)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = screenHeight * 0.02f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // ✅ 상단 버튼
                TopButtonImage(
                    screenHeight = screenHeight,
                    context = context
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                // ✅ 중앙 아이콘
                Image(
                    painter = painterResource(id = R.drawable.care_icon),
                    contentDescription = "Care Icon",
                    modifier = Modifier.size(screenHeight * 0.38f)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                // ✅ 텍스트
                val displayText = "이제 3초간 \n 숨을 멈춥니다"
                Text(
                    text = displayText,
                    color = Color(0xFF4F3422),
                    fontSize = (screenHeight.value * 0.08).sp,
                    fontFamily = KyonggiFont3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                // ✅ TTS 실행 또는 타이머
                val audioManager =
                    context.getSystemService(android.content.Context.AUDIO_SERVICE) as? AudioManager
                val isMuted =
                    (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0) == 0

                LaunchedEffect(ttsReady) {
                    if (ttsReady) {
                        if (!isMuted) {
                            // 음소거 아닐 때: TTS 실행 (onDone 콜백으로 자동 넘어감)
                            tts?.readText3(displayText)
                        } else {
                            // 음소거일 때: 6초 후 강제 이동
                            kotlinx.coroutines.delay(6000L)
                            context.startActivity(Intent(context, Care4Activity::class.java))
                            (context as? ComponentActivity)?.finish()
                        }
                    }
                }
            }
        }
    }
}
