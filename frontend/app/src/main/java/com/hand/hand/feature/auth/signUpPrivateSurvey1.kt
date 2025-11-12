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

class SignUpPrivateSurveyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpPrivateSurveyScreen()
        }
    }
}

@Composable
fun SignUpPrivateSurveyScreen(
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

            Spacer(Modifier.height(80.dp))
            Text(
                text = "지난 2주간을 기준으로\n다음 문항에 해당되는\n빈도를 선택해 주세요.",
                fontSize = (screenWidthDp * 0.08f).sp, // 반응형 폰트 크기
                fontWeight = FontWeight.Bold,
                fontFamily = BrandFontFamily,
                color = Color(0xFF4F3422),
                textAlign = TextAlign.Center,
                lineHeight = (screenWidthDp * 0.11f).sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val intent = Intent(context,  SignUpPrivateSurvey2Activity::class.java)
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
                        text = "시작하기",
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

