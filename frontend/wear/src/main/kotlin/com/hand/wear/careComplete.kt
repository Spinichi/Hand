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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import com.hand.hand.R
import androidx.lifecycle.lifecycleScope
import com.mim.watch.services.WearMessageSender
import kotlinx.coroutines.launch
import android.util.Log

class CareComplete : ComponentActivity() {

    private lateinit var messageSender: WearMessageSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⭐ MessageSender 초기화
        messageSender = WearMessageSender(applicationContext)

        setContent {
            CareCompleteScreen(
                onConfirm1 = {
                    // ✅ 완화법 완료: 종료 이벤트 전송 후 홈으로 이동
                    lifecycleScope.launch {
                        // sessionId는 폰에서 관리하고 있으므로 null로 전송 (폰에서 currentSessionId 사용)
                        messageSender.sendReliefEndEvent(
                            sessionId = 0L,  // 폰에서 currentSessionId 사용
                            userRating = null  // 별점 없음 (추후 확장 가능)
                        )
                        Log.d("CareComplete", "Relief END event sent")
                    }

                    startActivity(Intent(this, WearHomeActivity::class.java))
                    finish()
                }
            )
        }
    }
}

// ✅ 커스텀 폰트 정의
val KyonggiFont7 = FontFamily(Font(R.font.kyonggi_medium))

@Composable
fun CareCompleteScreen(onConfirm1: () -> Unit) {
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

            BackgroundCircles(screenWidth = screenWidth, screenHeight = screenHeight)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = screenHeight * 0.08f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // ✅ 상단 버튼
                Button(
                    onClick = { onConfirm1() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                    modifier = Modifier.size(screenHeight * 0.18f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.check_btn),
                        contentDescription = "Check button",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                // ✅ 중앙 아이콘
                Image(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = "Care Icon",
                    modifier = Modifier.size(screenHeight * 0.38f)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                // ✅ 텍스트
                Text(
                    text = "조금 더 평온해진 \n 하루예요",
                    color = Color(0xFF4F3422),
                    fontSize = (screenHeight.value * 0.08).sp,
                    fontFamily = KyonggiFont7,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
            }
        }
    }
}

