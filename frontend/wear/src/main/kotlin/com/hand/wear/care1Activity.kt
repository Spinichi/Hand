package com.hand.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.remember

class Care1Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Care1Screen()
        }
    }
}

// ✅ 커스텀 폰트 정의
val KyonggiFont1 = FontFamily(Font(R.font.kyonggi_medium))

@Composable
fun Care1Screen() {
    Scaffold {
        val context = LocalContext.current
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = screenHeight * 0.02f), // 화면 상단 여유
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // ✅ 상단 버튼
                TopButtonImage(
                    screenHeight = screenHeight,
                    context = LocalContext.current
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                // ✅ 중앙 아이콘
                Image(
                    painter = painterResource(id = R.drawable.care_icon),
                    contentDescription = "Care Icon",
                    modifier = Modifier
                        .size(screenHeight * 0.38f) // 반응형 크기
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                // ✅ 텍스트
                Text(
                    text = "4초간 천천히 코로 \n 숨을 들이마십니다",
                    color = Color(0xFF4F3422),
                    fontSize = (screenHeight.value * 0.08).sp,
                    fontFamily = KyonggiFont1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = screenHeight * 0.05f)
                        .size(screenHeight * 0.12f)
                        .background(Color.Gray)
                        .clickable(
                            indication = null, // 클릭 시 시각적 효과 제거
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            val intent = Intent(context, Care2Activity::class.java)
                            context.startActivity(intent)
                        }
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Next",
                        color = Color.White,
                        fontSize = (screenHeight.value * 0.05).sp
                    )
                }
            }
        }
    }
}
