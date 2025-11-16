package com.hand.hand.feature.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.feature.auth.SignInActivity
import com.hand.hand.ui.theme.BrandFontFamily
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OnboardingScreen(
                onFinish = {
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
            )
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val backgroundColor: Color
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = "실시간 스트레스 모니터링",
            description = "워치와 연동하여\n나의 스트레스를 실시간으로 확인하세요",
            imageRes = R.drawable.ic_onboarding_stress,
            backgroundColor = Color(0xFFE8F5E0) // 연한 초록
        ),
        OnboardingPage(
            title = "AI 마음 케어",
            description = "당신만을 위한\n맞춤 완화법을 제공합니다",
            imageRes = R.drawable.ic_onboarding_ai_care,
            backgroundColor = Color(0xFFFFF7E6) // 연한 노랑
        ),
        OnboardingPage(
            title = "감정 다이어리",
            description = "오늘 하루 나의 마음을\n기록하고 돌아보세요",
            imageRes = R.drawable.ic_onboarding_diary,
            backgroundColor = Color(0xFFF0EBFF) // 연한 보라
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 페이저
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(pages[pageIndex])
            }

            // 인디케이터 + 버튼
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 페이지 인디케이터
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) {
                                        Color(0xFF867E7A)
                                    } else {
                                        Color(0xFFD9D9D9)
                                    }
                                )
                        )
                    }
                }

                // 버튼
                if (pagerState.currentPage == pages.size - 1) {
                    // 마지막 페이지: 시작하기 버튼
                    Button(
                        onClick = onFinish,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF867E7A)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(52.dp)
                    ) {
                        Text(
                            "시작하기",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily
                        )
                    }
                } else {
                    // 다음 페이지로
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 건너뛰기
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF867E7A)
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                "건너뛰기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = BrandFontFamily
                            )
                        }

                        // 다음
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF867E7A)
                            ),
                            modifier = Modifier
                                .width(120.dp)
                                .height(48.dp)
                        ) {
                            Text(
                                "다음",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BrandFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 아이콘 배경
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(page.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(page.imageRes),
                contentDescription = null,
                modifier = Modifier.size(720.dp)
                    .padding(top = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 제목
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4F3422),
            fontFamily = BrandFontFamily,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 설명
        Text(
            text = page.description,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF867E7A),
            fontFamily = BrandFontFamily,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}