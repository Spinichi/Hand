package com.hand.hand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.* // remember, mutableStateOf 등을 사용하기 위해 import
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.hand.hand.feature.auth.LoginScreen // LoginScreen import
import com.hand.hand.ui.common.BrandWaveHeader
import com.hand.hand.ui.home.HomeScreen // HomeScreen import
import com.hand.hand.ui.theme.Green60
//import com.hand.hand.ui.theme.HandTheme // 전체 테마 적용

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
//            HandTheme { // 앱 전체에 일관된 테마 적용
                // --- 1. 로그인 상태를 저장할 변수 생성 ---
                // remember { mutableStateOf(false) } : 'false'로 초기화된 상태 변수 생성.
                // 이 값이 바뀌면 UI가 자동으로 업데이트됩니다.
                var isLoggedIn by remember { mutableStateOf(false) }

                // --- 2. 상태(isLoggedIn)에 따라 다른 화면을 보여줌 ---
                if (isLoggedIn) {
                    // 로그인이 된 상태이므로 HomeScreen을 보여줌
                    HomeScreen()
                } else {
                    // 로그인이 안 된 상태이므로 로그인 관련 화면을 보여줌
                    // 기존의 로그인 화면 코드를 그대로 사용합니다.
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .windowInsetsTopHeight(WindowInsets.statusBars)
                                    .background(Green60)
                            )
                            BrandWaveHeader(
                                fillColor = Green60,
                                edgeY = 30.dp,
                                centerY = 161.dp,
                                overhang = 24.dp,
                                height = 161.dp
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.image_14),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(top = 18.dp)
                                        .size(width = 111.dp, height = 40.dp)
                                )
                            }

                            // --- 3. LoginScreen의 onClickLogin 람다에서 상태 변경 ---
                            LoginScreen(
                                // LoginScreen에서 로그인 버튼이 눌리면 이 람다 함수가 실행됨
                                onClickLogin = { id, pw ->
                                    // TODO: 실제로는 여기서 id, pw로 서버에 로그인 요청을 보냅니다.
                                    // 지금은 "로그인했다 치고" 상태를 true로 바꿔줍니다.
                                    isLoggedIn = true
                                },
                                onClickSignUp = {
                                    // TODO: 네비게이션 → 회원가입 화면으로 이동
                                }
                            )
                        }
                    }
                }
            }
        }
    }
//}
