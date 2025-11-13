package com.hand.hand.feature.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.hand.hand.care.CareActivity
import com.hand.hand.diary.DiaryHomeActivity
import com.hand.hand.ui.common.BrandWaveHeader
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.theme.Green60

import android.widget.Toast
import com.hand.hand.api.Login.LoginManager
import com.hand.hand.api.Login.LoginResponse


class SignInActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SignInScreen()
        }
    }
}

@Composable
fun SignInScreen(
    onClickLogin: (String, String) -> Unit = { _, _ -> },
    onClickSignUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val horizontalPadding: Dp = (screenWidthDp * 0.05f).dp

    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }

    // api 로딩/에러 상태
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }


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
                // 로고 이미지
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
            text = "HAND 로그인",
            fontSize = (screenWidthDp * 0.07f).sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = BrandFontFamily,
            color = Color(0xFF4F3422),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally) // 변경됨
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

        // 로그인 버튼
        // 로그인 버튼
        Button(
            onClick = {
                // 1) 기본 유효성 검사
                if (id.isBlank() || pw.isBlank()) {
                    Toast.makeText(context, "아이디(이메일)와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // 2) 로딩 상태 ON
                loading = true
                errorMsg = null

                // 3) 로그인 API 호출
                LoginManager.login(
                    email = id,
                    password = pw,
                    onSuccess = { res: LoginResponse ->
                        loading = false
                        if (res.success) {
                            Toast.makeText(context, res.message ?: "로그인 성공", Toast.LENGTH_SHORT).show()

                            // 로그인 성공 → 기존처럼 화면 이동
                            val intent = Intent(context, SignInTypeActivity::class.java)
                            context.startActivity(intent)
                            // (필요하면) 현재 액티비티 종료: (context as? Activity)?.finish()
                        } else {
                            errorMsg = res.message ?: "로그인 실패"
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { e ->
                        loading = false
                        errorMsg = e.message ?: "네트워크 오류"
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        // Log 찍고 싶으면:
                        // Log.e("SignIn", "login error", e)
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
            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("로그인 중...", fontFamily = BrandFontFamily)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "로그인",
                        color = Color.White,
                        fontSize = (screenWidthDp * 0.04f).sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = BrandFontFamily
                    )

                    Spacer(modifier = Modifier.width(4.dp)) // 텍스트와 이미지 사이 간격

                    Image(
                        painter = painterResource(id = R.drawable.login_btn), // login_btn.png
                        contentDescription = "로그인 버튼 이미지",
                        modifier = Modifier
                            .size((screenWidthDp * 0.05f).dp) // 적절한 크기 설정
                    )
                }
            }
        }


        Spacer(Modifier.height(20.dp))

        // 회원가입 링크
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "아이디가 없으신가요? ",
                color = Color(0xFF7B6A5D),
                fontSize = (screenWidthDp * 0.035f).sp,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "회원가입",
                color = Color(0xFFE67E22),
                fontSize = (screenWidthDp * 0.035f).sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    val intent = Intent(context, SignUpActivity::class.java)
                    context.startActivity(intent)
                },
                fontFamily = BrandFontFamily
            )
        }

        Spacer(Modifier.height(32.dp))
        Spacer(Modifier.height(32.dp))

        // ── 임시 AI 문서 홈 이동 버튼 ──



    }
}
