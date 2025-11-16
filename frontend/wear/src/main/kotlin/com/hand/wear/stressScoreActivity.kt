package com.hand.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import ui.theme.HandTheme
import com.hand.wear.components.BackgroundCircles
import com.hand.hand.R
import androidx.lifecycle.lifecycleScope
import com.mim.watch.services.WearMessageSender
import kotlinx.coroutines.launch


class StressScoreActivity : ComponentActivity() {

    private lateinit var messageSender: WearMessageSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⭐ Intent에서 스트레스 점수 받기 (기본값 54.0)
        val stressScore = intent.getDoubleExtra("stressScore", 54.0)

        // ⭐ MessageSender 초기화
        messageSender = WearMessageSender(applicationContext)

        setContent {
            HandTheme {
                StressScoreScreen(
                    stressScore = stressScore,
                    onCancel = {
                        // ❌ X 버튼 눌렀을 때: WearHomeActivity 로 이동
                        startActivity(Intent(this, WearHomeActivity::class.java))
                        finish() // 현재 화면 종료
                    },
                    onConfirm = {
                        // ✅ Check 버튼 눌렀을 때: relief START 이벤트 전송 후 CareEx1Activity로 이동
                        lifecycleScope.launch {
                            messageSender.sendReliefStartEvent(
                                interventionId = 1L,  // 호흡법
                                triggerType = "MANUAL",
                                gestureCode = "breathing"
                            )
                        }

                        val intent = Intent(this, CareEx1Activity::class.java)
                        intent.putExtra("triggerType", "MANUAL")
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun StressScoreScreen(
    stressScore: Double = 54.0,
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    Scaffold {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F4F2)),
            contentAlignment = Alignment.Center
        ) {
            val screenHeight = this.maxHeight
            val screenWidth = this.maxWidth

            BackgroundCircles(screenWidth = screenWidth, screenHeight = screenHeight)


            Box(
                modifier = Modifier
                    .size(screenHeight * 0.2f)
                    .offset(x = screenWidth * 0.3f, y = screenHeight * 0.1f)
                    .background(Color(0xFFF7F4F2), shape = CircleShape)
            )

            Column(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(screenHeight * 0.0f))

                // 🔴 화면 중앙에 큰 숫자와 작은 단위 표시
                Row(
                    verticalAlignment = Alignment.Bottom, // 아래 기준 맞춤
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${stressScore.toInt()}",
                        color = Color(0xFF4F3422),
                        fontSize = (screenHeight.value * 0.3).sp, // 큰 숫자
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.width(4.dp)) // 숫자와 단위 사이 간격

                    Text(
                        text = "점",
                        color = Color(0xFF4F3422),
                        fontSize = (screenHeight.value * 0.08).sp, // 작은 단위
                        textAlign = TextAlign.Center,
                        fontFamily = KyonggiFont5,
                        modifier = Modifier.padding(bottom = screenHeight * 0.08f) // 아래 맞춤
                    )
                }


                Spacer(modifier = Modifier.height(screenHeight * 0.04f))

                Text(
                    text = "잠시 쉬어볼까요?",
                    color = Color(0xFF4F3422),
                    style = MaterialTheme.typography.title1.copy(fontSize = (screenHeight.value * 0.09).sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ❌ X 버튼
                    Button(
                        onClick = { onCancel() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        modifier = Modifier.size(screenHeight * 0.18f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.x_btn),
                            contentDescription = "X button",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(screenWidth * 0.2f))

                    // ✅ 체크 버튼
                    Button(
                        onClick = { onConfirm() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        modifier = Modifier.size(screenHeight * 0.18f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.check_btn),
                            contentDescription = "Check button",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

//// 화면 비율 기준 원 정보
//data class CircleInfoFraction(
//    val sizeFraction: Float,
//    val offsetXFraction: Float,
//    val offsetYFraction: Float
//)
