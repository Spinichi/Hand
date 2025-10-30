package com.example.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Scaffold

class BeforeRelaxActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeforeRelaxScreen()
        }
    }
}

@Composable
fun BeforeRelaxScreen() {
    Scaffold {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F4F2))
        ) {
            // BoxWithConstraints 스코프 활용
            val screenWidth = this.maxWidth
            val screenHeight = this.maxHeight

            // 흰색 원 3개, 위치와 크기 반응형
            WhiteCircle(xOffset = screenWidth * 0.0f, yOffset = screenHeight * 0.0f, size = screenHeight * 0.7f)
            WhiteCircle(xOffset = screenWidth * 0.5f, yOffset = screenHeight * 0.3f, size = screenHeight * 0.15f)
            WhiteCircle(xOffset = screenWidth * 0.3f, yOffset = screenHeight * 0.6f, size = screenHeight * 0.2f)
        }
    }
}

@Composable
fun WhiteCircle(xOffset: Dp, yOffset: Dp, size: Dp) {
    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(size)
            .background(Color.White, shape = CircleShape)
    )
}
