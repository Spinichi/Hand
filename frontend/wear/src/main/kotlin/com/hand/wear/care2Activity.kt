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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import java.util.Locale

class Care2Activity : ComponentActivity() {

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
                    ttsInitialized.value = true
                }
            }
        }

        setContent {
            val ttsReady by ttsInitialized
            Care2Screen(tts, ttsReady) {
                // ✅ 모든 카운트 끝나면 다음 화면으로 이동
                startActivity(Intent(this, Care3Activity::class.java))
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// ✅ 폰트 정의
val KyonggiFont2 = FontFamily(Font(R.font.kyonggi_medium))

// ✅ 순차적으로 TTS 발화하는 함수
suspend fun TextToSpeech?.speakCountSequence(
    words: List<String>,
    delayMillis: Long = 1000L
) {
    words.forEachIndexed { index, word ->
        this?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_$index")
        delay(delayMillis)
    }
}

// ✅ Composable
@Composable
fun Care2Screen(tts: TextToSpeech?, ttsReady: Boolean, onFinish: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                TopButtonImage(
                    screenHeight = screenHeight,
                    context = context
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                Image(
                    painter = painterResource(id = R.drawable.care_icon),
                    contentDescription = "Care Icon",
                    modifier = Modifier.size(screenHeight * 0.38f)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                val displayText = "하나, 둘, 셋, 넷"
                Text(
                    text = displayText,
                    color = Color(0xFF4F3422),
                    fontSize = (screenHeight.value * 0.08).sp,
                    fontFamily = KyonggiFont2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                // ✅ 순차 발화 실행
                LaunchedEffect(ttsReady) {
                    if (ttsReady) {
                        val audioManager =
                            context.getSystemService(android.content.Context.AUDIO_SERVICE) as? AudioManager
                        val isMuted =
                            (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0) == 0

                        if (!isMuted) {
                            // 하나 → 둘 → 셋 → 넷 (1초 간격)
                            tts?.speakCountSequence(listOf("하나", "둘", "셋", "넷"))
                        }

                        // 총 4초 후 다음 화면으로 이동
                        delay(4000L)
                        onFinish()
                    }
                }
            }
        }
    }
}
