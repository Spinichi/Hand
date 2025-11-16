package com.hand.hand.feature.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.feature.onboarding.OnboardingActivity
import com.hand.hand.ui.theme.BrandFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreen(
                onSplashFinished = {
                    startActivity(Intent(this, OnboardingActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // 애니메이션 상태
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // 아이콘 스케일 애니메이션 (펄스 효과)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // 페이드인 애니메이션
    var alpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // 페이드인
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(800)
        ) { value, _ ->
            alpha = value
        }

        // 3초 대기
        delay(3000)

        // 페이드아웃
        animate(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = tween(500)
        ) { value, _ ->
            alpha = value
        }

        // 종료
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2)) // 앱 기본 배경색
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 스마일 아이콘 (diary_happy_icon 사용)
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // 뒷배경 원형
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            color = Color(0xFFFFF7E6), // 연한 노란색 파스텔
                            shape = CircleShape
                        )
                )

                // 아이콘
                Image(
                    painter = painterResource(R.drawable.ic_splash_hand),
                    contentDescription = "Logo",
                    modifier = Modifier.size(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 앱 이름
            Text(
                text = "HAND",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF867E7A), // Brown60
                fontFamily = BrandFontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 서브 텍스트
            Text(
                text = "마음을 보듬는 순간",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA89F9B), // Brown40
                fontFamily = BrandFontFamily
            )
        }
    }
}