package com.hand.wear

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Scaffold
import com.hand.wear.components.BackgroundCircles
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.foundation.interaction.MutableInteractionSource
import java.util.Locale

class CareEx1Activity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TTS 초기화
        tts = TextToSpeech(this, this)

        setContent {
            CareEx1Screen(tts)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.KOREAN
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// ✅ 폰트 정의
val KyonggiFont = FontFamily(Font(R.font.kyonggi_medium))

@Composable
fun CareEx1Screen(tts: TextToSpeech?) {
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

            // ✅ 상단 버튼
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                TopButtonImage(
                    screenHeight = screenHeight,
                    context = LocalContext.current,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // ✅ 중앙 텍스트
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                val displayText = "가슴과 배에 손을 두고 배가 움직이는 걸 \n 느껴보세요."

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

                // ✅ 화면이 보이면 자동으로 TTS 실행
                LaunchedEffect(Unit) {
                    tts?.speak(displayText, TextToSpeech.QUEUE_FLUSH, null, "CareEx1TTS")
                }
            }

            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = screenHeight * 0.05f)
                    .size(screenHeight * 0.12f)
                    .background(Color.Gray)
                    .clickable(
                        indication = null,
                        interactionSource = interactionSource
                    ) {
                        val intent = Intent(context, CareEx2Activity::class.java)
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "< Next",
                    color = Color.White,
                    fontSize = (screenHeight.value * 0.05).sp
                )
            }
        }
    }
}
