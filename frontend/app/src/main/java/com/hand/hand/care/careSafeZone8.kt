// careSafeZone8.kt

package com.hand.hand.care

import android.content.Intent
import android.os.Bundle
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
import com.hand.hand.R
import com.hand.hand.api.Relief.ReliefManager
import com.hand.hand.ui.theme.BrandFontFamily
import androidx.compose.foundation.layout.imePadding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.hand.hand.care.CareSafeZone1Activity

class CareSafeZone8Activity : ComponentActivity() {

//    private var sessionId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  앞 단계에서 넘겨준 sessionId 받기
//        sessionId = intent.getLongExtra("sessionId", -1L)

        setContent {
            CareSafeZone8Screen(
                onBackClick = { finish() },
                onSubmit = { score ->
                    endSafeZoneSession(score)
                }
            )
        }
    }

    // ✅ 세션 종료 + 점수 전송
    private fun endSafeZoneSession(userRating: Int) {

        val sessionId = CareSafeZone1Activity.safeZoneSessionId
        if (sessionId == null || sessionId <= 0L) {
            Toast.makeText(this, "세션 ID가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val endedAt = nowIsoUtc()

        ReliefManager.endSession(
            sessionId = sessionId,
            endedAt = endedAt,
            userRating = userRating,
            onSuccess = {
                // 성공하면 CareActivity로 이동
                startActivity(Intent(this, CareActivity::class.java))
                finish()
            },
            onFailure = { e ->
                e.printStackTrace()
                Toast.makeText(this, "세션 종료에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // UTC 현재시간을 "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" 형태로
    private fun nowIsoUtc(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}

@Composable
fun CareSafeZone8Screen(
    onBackClick: () -> Unit,
    onSubmit: (Int) -> Unit               // ✅ 입력완료 시 점수(Int)를 넘겨주는 콜백
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val headerHeight = screenHeight * 0.25f

    var discomfortScore by remember { mutableStateOf(TextFieldValue("")) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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
                painter = painterResource(id = R.drawable.safe_zone_level_5),
                contentDescription = "Safe Zone Level",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "수고하셨습니다!",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (screenHeight * 0.035f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(screenHeight * 0.025f))
            Text(
                text = "완화법을 적용한 후, \n 현재 당신의 불편감 점수는 \n 몇 점인가요?",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (screenHeight * 0.035f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "불편감 점수" 텍스트 왼쪽 정렬
            Text(
                text = "불편감 점수",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color(0xFF4F3422),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 입력 박스
            TextField(
                value = discomfortScore,
                onValueChange = { discomfortScore = it },
                placeholder = {
                    Text(
                        text = "불편감 점수를 입력하세요",
                        color = Color(0xFF736B66),
                        fontSize = (screenHeight * 0.02f).value.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BrandFontFamily
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF4F3422),
                    unfocusedTextColor = Color(0xFF4F3422),
                    disabledTextColor = Color.Gray,
                    errorTextColor = Color.Red,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    errorContainerColor = Color.White,
                    cursorColor = Color(0xFF4F3422),
                    errorCursorColor = Color.Red,
                    selectionColors = null,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = Color(0xFF736B66),
                    unfocusedPlaceholderColor = Color(0xFF736B66)
                ),
                shape = RoundedCornerShape(100.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.065f)
                    .focusRequester(focusRequester)
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
                onClick = {
                    val scoreText = discomfortScore.text.trim()
                    val scoreInt = scoreText.toIntOrNull()

                    if (scoreInt != null) {
                        onSubmit(scoreInt)   // ✅ Activity로 점수 전달 → endSession 호출
                    }
                    // 숫자 아니면 지금은 무시 (원하면 나중에 에러 처리 추가)
                },
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
                        text = "입력완료",
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
