package com.hand.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Scaffold
import com.hand.wear.components.BackgroundCircles

class CareEx2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareEx2Screen()
        }
    }
}

@Composable
fun CareEx2Screen() {
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
                    context = LocalContext.current,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            // 중앙 텍스트 (임시)
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
                    text = "배를 이용해 숨을 \n 쉬는 것이 중요합니다.",
                    color = Color(0xFF4F3422),
                    fontSize = (screenHeight.value * 0.1).sp,
                    lineHeight = (screenHeight.value * 0.12).sp,
                    fontFamily = KyonggiFont,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    style = androidx.compose.ui.text.TextStyle(
                        lineBreak = LineBreak.Simple // ✅ 단어 단위 줄바꿈, 중간 끊김 방지
                    )
                )

            }
            val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

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
                        // ✅ 상위에서 가져온 context 사용
                        val intent = Intent(context, Care1Activity::class.java)
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
