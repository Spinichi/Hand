package com.hand.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import ui.theme.HandTheme
import com.hand.hand.R

class WearHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandTheme {
                WearHomeScreen(score = 85) // score 값 넣기}

            }
        }
    }

    @Composable
    fun WearHomeScreen(score: Int) {
        val context = LocalContext.current

        // score에 따른 배경색과 이미지 선택
        val (bgColor, imageRes) = when (score) {
            in 0..19 -> Color(0xFFA694F5) to R.drawable.mood_depressed
            in 20..39 -> Color(0xFFED7E1C) to R.drawable.mood_sad
            in 40..59 -> Color(0xFF926247) to R.drawable.mood_neutral
            in 60..79 -> Color(0xFFFFCE5C) to R.drawable.mood_happy
            in 80..100 -> Color(0xFF9BB167) to R.drawable.mood_overjoyed
            else -> Color.Gray to R.drawable.mood_neutral // 범위를 벗어날 경우 기본값
        }

        Scaffold {
            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor), // 선택된 배경색 적용
                contentAlignment = Alignment.Center
            ) {
                val screenWidth = this@BoxWithConstraints.maxWidth
                val screenHeight = this@BoxWithConstraints.maxHeight

                val imageSize = screenHeight * 0.6f
                val spacing = screenHeight * 0.05f
                val buttonHeight = screenHeight * 0.15f
                val buttonWidth = screenWidth * 0.6f
                val fontSize = (buttonHeight.value * 0.5).sp
                val iconSize = buttonHeight * 0.5f


                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 이미지
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "HAND 이미지",
                        modifier = Modifier.size(imageSize)
                    )

                    Spacer(modifier = Modifier.height(spacing))

                    // 버튼
                    Button(
                        onClick = {
                            context.startActivity(Intent(context, BeforeRelaxActivity::class.java))
                        },
                        modifier = Modifier
                            .height(buttonHeight)
                            .width(buttonWidth),
                        colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4F3422)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "긴장 풀기",
                                color = Color.White,
                                fontSize = fontSize,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.arrow_right),
                                contentDescription = "화살표 아이콘",
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    }
                }
            }
        }
    }
}
