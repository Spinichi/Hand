package com.hand.hand.care

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.imePadding

class CareSafeZone6Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareSafeZone6Screen(
                onBackClick = { finish() },
                onStartClick = {
                    startActivity(Intent(this, CareSafeZone7Activity::class.java))
                }
            )
        }
    }
}

@Composable
fun CareSafeZone6Screen(onBackClick: () -> Unit, onStartClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val headerHeight = screenHeight * 0.25f

    var discomfortScore by remember { mutableStateOf(TextFieldValue("")) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        // 헤더
        CareHeader2(
            titleText = "안전지대 연습",
            subtitleText = "이름 붙이기", // 아이콘 없이 텍스트만
            onBackClick = onBackClick
        )

        // 본문 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = headerHeight,
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.05f,
                    bottom = 80.dp
                )
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            // Level 이미지
            Image(
                painter = painterResource(id = R.drawable.safe_zone_level_4),
                contentDescription = "Safe Zone Level",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 안내 텍스트
            Text(
                text = "당신의 안전지대에 뭐라고 이름을 \n 붙이면 잘 연상할 수 있을까요?",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (screenHeight * 0.023f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            Text(
                text = "간단한 이름일수록 쉽게 떠올릴 수 있습니다",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.023f).value.sp,
                color = Color(0xFF4F3422),
                lineHeight = (screenHeight * 0.05f).value.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "안전지대 이름",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color(0xFF4F3422),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 입력 박스
            TextField(
                value = discomfortScore,
                onValueChange = { discomfortScore = it },
                placeholder = {
                    Text(
                        text = "안전지대에 붙일 이름을 입력하세요",
                        color = Color(0xFF736B66),
                        fontSize = (screenHeight * 0.02f).value.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BrandFontFamily
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF4F3422),
                    unfocusedTextColor = Color(0xFF4F3422),
                    disabledTextColor = Color.Gray,
                    errorTextColor = Color.Red,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    errorContainerColor = Color.White,
                    cursorColor = Color(0xFF4F3422),
                    errorCursorColor = Color.Red,
                    selectionColors = null,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = Color(0xFF736B66),
                    unfocusedPlaceholderColor = Color(0xFF736B66)
                ),
                shape = RoundedCornerShape(100.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.065f)
                    .focusRequester(focusRequester)
            )
        }


        // 하단 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.05f
                )
        ) {
            val buttonHeight = screenHeight * 0.065f
            val arrowHeight = buttonHeight * 0.4f
            val arrowWidth = arrowHeight * (24f / 24f)

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F3422),
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "다음",
                        fontSize = (screenHeight * 0.022f).value.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Image(
                        painter = painterResource(id = R.drawable.arrow_right_white),
                        contentDescription = "Arrow Right",
                        modifier = Modifier
                            .height(arrowHeight)
                            .width(arrowWidth)
                    )
                }
            }
        }
    }
}
