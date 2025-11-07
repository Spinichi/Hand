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

class CareSafeZone4Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareSafeZone4Screen(
                onBackClick = { finish() },
                onStartClick = {
                    startActivity(Intent(this, CareSafeZone5Activity::class.java))
                }
            )
        }
    }
}

@Composable
fun CareSafeZone4Screen(onBackClick: () -> Unit, onStartClick: () -> Unit) {
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
            subtitleText = "구체적으로 떠올리기", // 아이콘 없이 텍스트만
            onBackClick = onBackClick
        )

        // 본문
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

            Image(
                painter = painterResource(id = R.drawable.safe_zone_level_2),
                contentDescription = "Safe Zone Level",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.1f)
            )

            val fieldHeight = screenHeight * 0.065f // ✅ 높이 늘림 (기존 0.055f → 0.065f)

            val questionList = listOf(
                Pair("보입니까?", "무엇이 보이나요?"),
                Pair("들립니까?", "무엇이 들리나요?"),
                Pair("냄새가 납니까?", "어떤 냄새가 나나요?"),
                Pair("감정이 느껴집니까?", "어떤 감정이 느껴집니까?"),
                Pair("감각이 느껴집니까?", "어떤 감각이 느껴집니까?")
            )

            questionList.forEach { (question, placeholder) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.check_brown_icon),
                        contentDescription = "Check Icon",
                        modifier = Modifier.size(screenHeight * 0.025f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "안전지대를 떠올리면 어떤 ",
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (screenHeight * 0.02f).value.sp,
                        color = Color(0xFF4F3422),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = question,
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.02f).value.sp,
                        color = Color(0xFF4F3422),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = discomfortScore,
                    onValueChange = { discomfortScore = it },
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = Color(0xFF736B66),
                            fontSize = (screenHeight * 0.015f).value.sp,
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
                        .height(fieldHeight)
                        .focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // 하단 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = 16.dp,
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
