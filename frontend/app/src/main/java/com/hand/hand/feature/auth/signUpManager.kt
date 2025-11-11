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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.common.BrandWaveHeader
import com.hand.hand.ui.theme.BrandFontFamily

class SignUpManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpManagerScreen()
        }
    }
}

@Composable
fun SignUpManagerScreen(
    onClickLogin: (String, String) -> Unit = { _, _ -> },
    onClickSignUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val horizontalPadding: Dp = (screenWidthDp * 0.05f).dp

    var id by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
            .padding(horizontal = horizontalPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── BrandWaveHeader ──
        val edgeY = (screenHeightDp * 0.08f).dp
        val centerY = (screenHeightDp * 0.25f).dp
        val overhang = (screenWidthDp * 0.06f).dp
        val headerHeight = centerY * 0.9f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(centerY)
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

        // ── 제목 ──
        Text(
            text = "관리자 등록",
            fontSize = (screenWidthDp * 0.07f).sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = BrandFontFamily,
            color = Color(0xFF4F3422),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(20.dp))

        // ── 그룹 이름 ──
        Text(
            text = "그룹 이름",
            fontSize = (screenWidthDp * 0.03f).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4F3422),
            modifier = Modifier.fillMaxWidth(),
            fontFamily = BrandFontFamily
        )

        OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            placeholder = { Text("그룹 이름을 입력하세요.", fontFamily = BrandFontFamily) },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.signup_team_name),
                    contentDescription = "아이디 아이콘",
                    modifier = Modifier.size((screenWidthDp * 0.06f).dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9BB168),
                unfocusedBorderColor = Color(0xFFBFD19B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFF9BB168)
            )
        )

        Spacer(Modifier.height(16.dp))

        // ── 그룹 유형 ──
        Text(
            text = "그룹 유형",
            fontSize = (screenWidthDp * 0.03f).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4F3422),
            modifier = Modifier.fillMaxWidth(),
            fontFamily = BrandFontFamily
        )

        Spacer(Modifier.height(12.dp))

        // 그룹 유형 (2줄 구성)
        val optionsRow1 = listOf("공공안전기관", "의료기관", "교육기관")
        val optionsRow2 = listOf("기업", "기타단체")

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(optionsRow1, optionsRow2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowOptions.forEach { option ->
                        val isSelected = selectedType == option
                        Surface(
                            color = if (isSelected) Color(0xFF9BB168) else Color.White,
                            shape = RoundedCornerShape(100.dp),
                            tonalElevation = 2.dp,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height((screenHeightDp * 0.065f).dp)
                                .clickable { selectedType = option }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = option,
                                    color = if (isSelected) Color.White else Color(0xFF4F3422),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = BrandFontFamily,
                                    fontSize = (screenWidthDp * 0.035f).sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── 등록하기 버튼 ──
        Button(
            onClick = {
                // ✅ SignInTypeActivity로 이동
                val intent = Intent(context, SignInTypeActivity::class.java)
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
                    text = "등록하기",
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
