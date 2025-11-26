// careBodyScan1.kt - 바디스캔

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

class CareBodyScan1Activity : ComponentActivity() {

    companion object {
        var bodyScanSessionId: Long? = null
        var beforeStressLevel: Int? = null
        var beforeStressTimestamp: Long? = null
    }

    private var tts: TextToSpeech? = null
    private val ttsInitialized = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            CareBodyScan1Screen(
                onBackClick = { finish() },
                onStartClick = { startBodyScanSession() },
                tts = tts,
                ttsReady = ttsReady
            )
        }
    }

    private fun startBodyScanSession() {
        beforeStressLevel =
            com.hand.hand.wear.WearListenerForegroundService.getLatestStressLevel()
        beforeStressTimestamp =
            com.hand.hand.wear.WearListenerForegroundService.getLatestStressTimestamp()

        val startedAt = nowIsoUtc()

        ReliefManager.startReliefSession(
            interventionId = 6,
            triggerType = "MANUAL",
            anomalyDetectionId = null,
            gestureCode = "BODY_SCAN",
            startedAt = startedAt,
            onSuccess = { res ->
                val sessionId = res.data?.sessionId
                bodyScanSessionId = sessionId
                Toast.makeText(this, "바디스캔을 시작합니다.", Toast.LENGTH_SHORT).show()
                // TODO: 다음 화면으로 이동 (구현 필요)
                // startActivity(Intent(this, CareBodyScan2Activity::class.java))
                finish()
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
        tts?.stop()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

fun TextToSpeech?.readBodyScanText() {
    val text = "바디스캔은 몸의 각 부위를 차례로 느끼며 긴장을 이완하는 명상법입니다. 편안한 곳에 앉거나 누워주세요."
    this?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "BodyScan1TTS")
}

@Composable
fun CareBodyScan1Screen(
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    tts: TextToSpeech?,
    ttsReady: Boolean
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val headerHeight = screenHeight * 0.25f

    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            tts.readBodyScanText()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        CareHeader2(
            titleText = "바디스캔",
            subtitleTags = listOf(
                TagWithIcon("이완", R.drawable.home_icon),
                TagWithIcon("명상", R.drawable.stress_icon)
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
                contentDescription = "Body Scan Level",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "바디스캔은 몸의 각 부위를 \n 차례로 느끼며 긴장을 \n 이완하는 명상법입니다. \n \n 편안한 곳에 앉거나 누워주세요",
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
