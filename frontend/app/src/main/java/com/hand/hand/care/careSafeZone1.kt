// careSafeZone1.kt

package com.hand.hand.care

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily

import com.hand.hand.api.Relief.ReliefManager
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CareSafeZone1Activity : ComponentActivity() {

    companion object {
        // 앱 실행 중 어디서든 접근 가능한 세션 ID 저장소
        var safeZoneSessionId: Long? = null

        // ⭐ 완화법 시작 시점의 스트레스 점수 저장
        var beforeStressLevel: Int? = null
        var beforeStressTimestamp: Long? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareSafeZone1Screen(
                onBackClick = { finish() },
                onStartClick = {
                    startSafeZoneSession()
//                    startActivity(Intent(this, CareSafeZone2Activity::class.java))
                }
            )
        }
    }
    private fun startSafeZoneSession() {
        // 1) 토큰 가져오기 (예시: SharedPreferences에 저장해둔 경우)
//        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
//        val token = prefs.getString("accessToken", null)
//
//        if (token == null) {
//            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
//            return
//        }

        // 2) ⭐ 완화법 시작 시점의 스트레스 점수 저장
        beforeStressLevel = com.hand.hand.wear.WearListenerForegroundService.getLatestStressLevel()
        beforeStressTimestamp = com.hand.hand.wear.WearListenerForegroundService.getLatestStressTimestamp()
        android.util.Log.d("CareSafeZone1", "📊 Before stress level: $beforeStressLevel (timestamp: $beforeStressTimestamp)")

        // 3) 현재 시간을 ISO 형식으로 만들기
        val startedAt = nowIsoUtc()

        // 4) ReliefManager로 API 호출
        ReliefManager.startReliefSession(
//            token = token,
            interventionId = 2,          // ✅ 안전지대 연습의 DB id
            triggerType = "MANUAL",  // 수동으로 실행
            anomalyDetectionId = null,
            gestureCode = "SAFE_ZONE",
            startedAt = startedAt,
            onSuccess = { res ->
                val sessionId = res.data?.sessionId
                // 세션 id 잘 받았는지 확인
                // Log.d("Care", "safe zone sessionId = $sessionId")

                safeZoneSessionId = sessionId
                // 4) 다음 화면으로 이동 + 필요하면 sessionId도 같이 넘기기
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

    // KST 현재시간을 "yyyy-MM-dd'T'HH:mm:ss" 형태로
    private fun nowIsoUtc(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(Date())
    }
}

@Composable
fun CareSafeZone1Screen(onBackClick: () -> Unit, onStartClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val headerHeight = screenHeight * 0.25f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        // 헤더
        CareHeader2(
            titleText = "안전지대 연습",
            subtitleTags = listOf(
                TagWithIcon("불편감", R.drawable.stress_icon),
                TagWithIcon("스트레스", R.drawable.stress_icon),
                TagWithIcon("공간 이미지", R.drawable.home_icon)
            ),
            onBackClick = onBackClick
        )

        // 본문 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = headerHeight, // 헤더 바로 아래 시작
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.05f,
                    bottom = 80.dp // 버튼 공간 확보
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 이미지
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))
            Image(
                painter = painterResource(id = R.drawable.safe_zone_level),
                contentDescription = "Safe Zone Level",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 안내 텍스트
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

        // 하단 고정 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = screenWidth * 0.05f, end = screenWidth * 0.05f)
        ) {
            val buttonHeight = screenHeight * 0.065f
            val arrowHeight = buttonHeight * 0.4f
            val arrowWidth = arrowHeight * (24f / 24f) // 원본 비율 유지

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
