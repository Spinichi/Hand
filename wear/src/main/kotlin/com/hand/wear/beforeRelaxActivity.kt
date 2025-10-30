package com.hand.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import ui.theme.HandTheme

class BeforeRelaxActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandTheme {
                BeforeRelaxScreen()
            }
        }
    }
}

@Composable
fun BeforeRelaxScreen() {
    Scaffold {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F4F2)),
            contentAlignment = Alignment.Center
        ) {
            val screenHeight = this.maxHeight
            val screenWidth = this.maxWidth

            // 반응형 원 리스트 (비율로 지정)
            val circles = listOf(
                CircleInfoFraction(0.7f, -0.25f, -0.15f),
                CircleInfoFraction(0.35f, 0.3f, 0.1f),
                CircleInfoFraction(0.2f, -0.05f, 0.35f)
            )



            // 원 그리기
            circles.forEach { circle ->
                Box(
                    modifier = Modifier
                        .size(screenHeight * circle.sizeFraction) // 화면 높이 기준 반응형
                        .offset(
                            x = screenWidth * circle.offsetXFraction,
                            y = screenHeight * circle.offsetYFraction
                        )
                        .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
                )
            }
            Box(
                modifier = Modifier
                    .size(screenHeight * 0.2f)
                    .offset(x = screenWidth * 0.3f, y = screenHeight * 0.1f)
                    .background(Color(0xFFF7F4F2), shape = androidx.compose.foundation.shape.CircleShape)
            )

            // Column UI (기존과 동일)
            Column(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(screenHeight * 0.09f))

                Text(
                    text = "잠시 쉬어볼까요?",
                    color = Color(0xFF4F3422),
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight.value * 0.09).sp,
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
                    Button(
                        onClick = { },
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

                    Button(
                        onClick = { },
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

// 화면 비율 기준 원 정보
data class CircleInfoFraction(
    val sizeFraction: Float, // 화면 높이에 대한 크기 비율
    val offsetXFraction: Float, // 화면 폭에 대한 X 위치 비율
    val offsetYFraction: Float  // 화면 높이에 대한 Y 위치 비율
)




@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun BeforeRelaxScreenPreview() {
    BeforeRelaxScreen()
}
