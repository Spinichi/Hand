package com.hand.wear


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Scaffold
import com.hand.wear.components.BackgroundCircles

class Care4Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Care4Screen()
        }
    }
}

// ✅ 커스텀 폰트 정의
val KyonggiFont4 = FontFamily(Font(R.font.kyonggi_medium))

@Composable
fun Care4Screen() {
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
                    text = "하나, 둘, 셋",
                    color = Color(0xFF4F3422),
                    fontSize = (screenHeight.value * 0.08).sp,
                    fontFamily = KyonggiFont4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                )
            }


            // ✅ 하단 Next 버튼 (임시, 이전 페이지로 돌아가기)
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
                            val intent = Intent(context, Care5Activity::class.java)
                            context.startActivity(intent)
                        }
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Prev",
                        color = Color.White,
                        fontSize = (screenHeight.value * 0.05).sp
                    )
                }
            }
        }
    }
}
