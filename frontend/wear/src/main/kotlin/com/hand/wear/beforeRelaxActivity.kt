package com.hand.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import com.hand.hand.R
import com.hand.wear.components.BackgroundCircles
import ui.theme.HandTheme


class BeforeRelaxActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandTheme {
                BeforeRelaxScreen(
                    onCancel = {
                        // ❌ X 버튼 눌렀을 때: WearHomeActivity 로 이동
                        startActivity(Intent(this, WearHomeActivity::class.java))
                        finish() // 현재 화면 종료
                    },
                    onConfirm = {
                        // ✅ Check 버튼 눌렀을 때: CareEx1Activity 로 이동
                        startActivity(Intent(this, CareEx1Activity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun BeforeRelaxScreen(
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
                Spacer(modifier = Modifier.height(screenHeight * 0.09f))

                Text(
                    text = "잠시 쉬어볼까요?",
                    color = Color(0xFF4F3422),
                    style = MaterialTheme.typography.title1.copy(fontSize = (screenHeight.value * 0.09).sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                Image(
                    painter = painterResource(id = R.drawable.heart_beat),
                    contentDescription = "Heart Beat Image",
                    modifier = Modifier.size(screenHeight * 0.4f)
                )

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
