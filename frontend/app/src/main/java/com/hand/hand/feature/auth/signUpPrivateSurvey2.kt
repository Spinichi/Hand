package com.hand.hand.feature.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hand.hand.R
import com.hand.hand.ui.common.BrandWaveHeader
import com.hand.hand.ui.theme.BrandFontFamily

class SignUpPrivateSurvey2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpPrivateSurvey2Screen()
        }
    }
}

@Composable
fun SignUpPrivateSurvey2Screen(
    onClickLogin: (String, String) -> Unit = { _, _ -> },
    onClickSignUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val horizontalPadding: Dp = (screenWidthDp * 0.05f).dp

    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var disease by remember { mutableStateOf("") }   // ✅ 질병 입력용
    var address by remember { mutableStateOf("") }   // ✅ 거주지 입력용
    var isAlarmEnabled by remember { mutableStateOf(false) }
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }


    val edgeY = (screenHeightDp * 0.08f).dp
    val centerY = (screenHeightDp * 0.25f).dp
    val overhang = (screenWidthDp * 0.06f).dp
    val headerHeight = centerY * 0.9f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        val scrollState = rememberScrollState()

        // ── 헤더 (고정) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeightDp.dp * 0.28f) // 헤더 높이
                .zIndex(1f) // 항상 맨 위
        ) {
            BrandWaveHeader(
                fillColor = Color(0xFF9BB168),
                edgeY = edgeY,
                centerY = centerY,
                overhang = overhang,
                height = headerHeight
            ) {
                Image(
                    painter = painterResource(R.drawable.image_14),
                    contentDescription = "로고",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-screenHeightDp * 0.04f).dp)
                        .size(width = screenWidthDp.dp * 0.3f, height = screenHeightDp.dp * 0.05f)
                )
            }
        }

        // ── 본문 (스크롤) ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding)
                .padding(top = screenHeightDp.dp * 0.23f), // 헤더 높이만큼 아래에서 시작
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "개인 등록",
                fontSize = (screenWidthDp * 0.07f).sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = BrandFontFamily,
                color = Color(0xFF4F3422),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Image(
                painter = painterResource(id = R.drawable.signup_private_level1),
                contentDescription = "레벨 안내 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeightDp * 0.05f).dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(16.dp))

// ── 단계 표시 (1 of 3) ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width((screenWidthDp * 0.15f).dp) // 네모 가로 크기, 반응형
                    .height((screenWidthDp * 0.08f).dp) // 네모 세로 크기, 반응형
                    .background(
                        color = Color(0xFFE8DDD9), // 배경색
                        shape = RoundedCornerShape(100.dp) // 둥근 네모
                    )
            ) {
                Text(
                    text = "1 of 3",
                    color = Color(0xFF926247), // 텍스트 색
                    fontSize = (screenWidthDp * 0.04f).sp, // 반응형 폰트 크기
                    fontWeight = FontWeight.Bold,
                    fontFamily = BrandFontFamily,
                    textAlign = TextAlign.Center
                )
            }


            Spacer(Modifier.height(50.dp))
            Text(
                text = "예전에는 재밌거나 즐거웠던\n활동에서 흥미나 즐거움이\n많이 줄었다고 느끼나요?",
                fontSize = (screenWidthDp * 0.08f).sp, // 반응형 폰트 크기
                fontWeight = FontWeight.Bold,
                fontFamily = BrandFontFamily,
                color = Color(0xFF4F3422),
                textAlign = TextAlign.Center,
                lineHeight = (screenWidthDp * 0.11f).sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            // ── 빈도 선택 옵션 ──
            // ── 빈도 선택 옵션 2x2 ──
            val options = listOf("해당 없음", "며칠 동안", "절반 이상", "거의 매일")
            var selectedFrequency by remember { mutableStateOf("") }

// 2x2 그리드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { option ->
                            val isSelected = selectedFrequency == option
                            Surface(
                                color = if (isSelected) Color(0xFF9BB168) else Color.White,
                                shape = RoundedCornerShape(100.dp),
                                tonalElevation = 2.dp,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height((LocalConfiguration.current.screenHeightDp * 0.08f).dp) // 버튼 크기 키움
                                    .clickable { selectedFrequency = option }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = option,
                                        color = if (isSelected) Color.White else Color(0xFF4F3422),
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = BrandFontFamily,
                                        fontSize = (LocalConfiguration.current.screenWidthDp * 0.05f).sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(context,  SignUpPrivateSurvey3Activity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenWidthDp * 0.14f).dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A2E1F),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "다음",
                        color = Color.White,
                        fontSize = (screenWidthDp * 0.04f).sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = BrandFontFamily
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Image(
                        painter = painterResource(id = R.drawable.login_btn),
                        contentDescription = "로그인 버튼 이미지",
                        modifier = Modifier.size((screenWidthDp * 0.05f).dp)
                    )
                }
            }
        }
    }
}

