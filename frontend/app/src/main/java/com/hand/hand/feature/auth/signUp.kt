package com.hand.hand.feature.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import com.hand.hand.api.SignUp.SignUpManager
import com.hand.hand.api.SignUp.SignupRequest
import com.hand.hand.api.SignUp.SignupResponse
import com.hand.hand.ui.common.BrandWaveHeader
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.theme.Green60

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpScreen()
        }
    }
}

@Composable
fun SignUpScreen() {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val horizontalPadding: Dp = (screenWidthDp * 0.05f).dp

    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var showPw by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
            .padding(horizontal = horizontalPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // BrandWaveHeader
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

        Text(
            text = "회원가입",
            fontSize = (screenWidthDp * 0.07f).sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = BrandFontFamily,
            color = Color(0xFF4F3422),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))

        // 아이디 입력
        Text(
            text = "아이디",
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
            placeholder = { Text("아이디를 입력하세요.", fontFamily = BrandFontFamily) },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.login_id_icon),
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
                focusedBorderColor = Green60,
                unfocusedBorderColor = Color(0xFFBFD19B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Green60
            )
        )

        Spacer(Modifier.height(16.dp))

        // 비밀번호 입력
        Text(
            text = "비밀번호",
            fontSize = (screenWidthDp * 0.03f).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4F3422),
            modifier = Modifier.fillMaxWidth(),
            fontFamily = BrandFontFamily
        )

        OutlinedTextField(
            value = pw,
            onValueChange = { pw = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            placeholder = { Text("비밀번호를 입력하세요.", fontFamily = BrandFontFamily) },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.login_pw_icon),
                    contentDescription = "비밀번호 아이콘",
                    modifier = Modifier.size((screenWidthDp * 0.06f).dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { showPw = !showPw }) {
                    Icon(
                        imageVector = if (showPw) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "비밀번호 보기/숨기기",
                        tint = Color(0xFF9D9D9D)
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green60,
                unfocusedBorderColor = Color(0xFFBFD19B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Green60
            )
        )

        Spacer(Modifier.height(24.dp))

        // 회원가입 버튼
        Button(
            onClick = {
                // Retrofit 회원가입 API 호출
                val signupRequest = SignupRequest(email = id, password = pw)
                SignUpManager.signup(
                    signupRequest,
                    onSuccess = { response: SignupResponse ->
                        Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        // 로그인 화면으로 이동
                        val intent = Intent(context, SignInActivity::class.java)
                        context.startActivity(intent)
                    },
                    onFailure = { throwable ->
                        Toast.makeText(context, "회원가입 실패: ${throwable.message}", Toast.LENGTH_SHORT).show()
                    }
                )
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
                    text = "회원가입",
                    color = Color.White,
                    fontSize = (screenWidthDp * 0.04f).sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = BrandFontFamily
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.login_btn),
                    contentDescription = "회원가입 버튼 이미지",
                    modifier = Modifier.size((screenWidthDp * 0.05f).dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 로그인 링크
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "아이디가 있으신가요? ",
                color = Color(0xFF7B6A5D),
                fontSize = (screenWidthDp * 0.035f).sp,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "로그인",
                color = Color(0xFFE67E22),
                fontSize = (screenWidthDp * 0.035f).sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    val intent = Intent(context, SignInActivity::class.java)
                    context.startActivity(intent)
                },
                fontFamily = BrandFontFamily
            )
        }
    }
}
