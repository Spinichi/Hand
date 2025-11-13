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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

// CareEx1Activity
class CareEx1Activity : ComponentActivity() {

    private var tts: TextToSpeech? = null
    private val ttsInitialized = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TTS 초기화
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
                    // TTS 발화 완료 리스너
                    tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}

                        override fun onDone(utteranceId: String?) {
                            runOnUiThread {
                                startActivity(Intent(this@CareEx1Activity, CareEx2Activity::class.java))
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
            CareEx1Screen(tts, ttsReady)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// 폰트 정의
val KyonggiFont = FontFamily(Font(R.font.kyonggi_medium))

// TTS 확장 함수
fun TextToSpeech?.readText(text: String) {
    this?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CareEx1TTS")
}

// Composable
@Composable
fun CareEx1Screen(tts: TextToSpeech?, ttsReady: Boolean) {
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

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                TopButtonImage(
                    screenHeight = screenHeight,
                    context = context,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // 중앙 텍스트
            val displayText = "가슴과 배에 손을 두고 \n 배가 움직이는걸 \n 느껴보세요."
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
                    fontFamily = KyonggiFont,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    style = androidx.compose.ui.text.TextStyle(
                        lineBreak = LineBreak.Simple
                    )
                )
            }

            // TTS 실행 또는 타이머
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val isMuted = (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0) == 0

            LaunchedEffect(ttsReady) {
                if (ttsReady) {
                    if (!isMuted) {
                        // 음소거 아닐 때: TTS 실행 (onDone 콜백으로 자동 넘어감)
                        tts?.readText(displayText)
                    } else {
                        // 음소거일 때: 6초 후 강제 이동
                        kotlinx.coroutines.delay(6000L)
                        context.startActivity(Intent(context, CareEx2Activity::class.java))
                        (context as? ComponentActivity)?.finish()
                    }
                }
            }

        }
    }
}
