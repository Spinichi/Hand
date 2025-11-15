// careSafeZone1.kt

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
import com.hand.hand.api.Relief.ReliefManager
import com.hand.hand.ui.theme.BrandFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CareSafeZone1Activity : ComponentActivity() {

    companion object {
        var safeZoneSessionId: Long? = null
        var beforeStressLevel: Int? = null
        var beforeStressTimestamp: Long? = null
    }

    //  TTS + 상태
    private var tts: TextToSpeech? = null
    private val ttsInitialized = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TTS 초기화 (람다 콜백 방식)
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
            CareSafeZone1Screen(
                onBackClick = { finish() },
                onStartClick = { startSafeZoneSession() },
                tts = tts,
                ttsReady = ttsReady
            )
        }
    }

    private fun startSafeZoneSession() {
        beforeStressLevel =
            com.hand.hand.wear.WearListenerForegroundService.getLatestStressLevel()
        beforeStressTimestamp =
            com.hand.hand.wear.WearListenerForegroundService.getLatestStressTimestamp()
        android.util.Log.d(
            "CareSafeZone1",
            "📊 Before stress level: $beforeStressLevel (timestamp: $beforeStressTimestamp)"
        )

        val startedAt = nowIsoUtc()

        ReliefManager.startReliefSession(
            interventionId = 2,
            triggerType = "MANUAL",
            anomalyDetectionId = null,
            gestureCode = "SAFE_ZONE",
            startedAt = startedAt,
            onSuccess = { res ->
                val sessionId = res.data?.sessionId
                safeZoneSessionId = sessionId

                val intent = Intent(this, CareSafeZone2Activity::class.java).apply {
                    putExtra("sessionId", sessionId ?: -1L)
                }
                startActivity(intent)
            },
            onFailure = { e ->
                e.printStackTrace()
                Toast.makeText(this, "세션 시작에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun nowIsoUtc(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(Date())
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

// TTS 확장 함수
fun TextToSpeech?.readSafeZoneText() {
    val text = "안전지대는 편안하고 안정되는 장소입니다. 불편함이 느껴진다면 다른 장소를 떠올리세요."
    this?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SafeZone1TTS")
}

@Composable
fun CareSafeZone1Screen(
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
            android.util.Log.d("CareSafeZone1", "TTS ready, speaking text.")
            tts.readSafeZoneText()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        CareHeader2(
            titleText = "안전지대 연습",
            subtitleTags = listOf(
                TagWithIcon("불편감", R.drawable.stress_icon),
                TagWithIcon("스트레스", R.drawable.stress_icon),
                TagWithIcon("공간 이미지", R.drawable.home_icon)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))
            Image(
                painter = painterResource(id = R.drawable.safe_zone_level),
                contentDescription = "Safe Zone Level",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "안전지대는 편안하고 \n 안정되는 장소입니다. \n \n 불편함이 느껴진다면 \n 다른 장소를 떠올리세요",
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
