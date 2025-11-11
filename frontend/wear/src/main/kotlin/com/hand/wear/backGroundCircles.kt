package com.hand.wear.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.hand.hand.R

// 화면 크기에 맞춰 반응형으로 그려주는 백그라운드 원 컴포넌트
@Composable
fun BackgroundCircles(
    screenWidth: Dp,
    screenHeight: Dp
) {
    val circles = listOf(
        CircleInfoFraction(0.7f, -0.25f, -0.15f, Color.White),
        CircleInfoFraction(0.35f, 0.3f, 0.1f, Color.White),
        CircleInfoFraction(0.2f, -0.05f, 0.35f, Color.White),
        CircleInfoFraction(0.2f, 0.3f, 0.1f, Color(0xFFF7F4F2)) // 마지막 원: 배경색
    )

    circles.forEach { circle ->
        Box(
            modifier = Modifier
                .size(screenHeight * circle.sizeFraction)
                .offset(
                    x = screenWidth * circle.offsetXFraction,
                    y = screenHeight * circle.offsetYFraction
                )
                .background(circle.color, shape = CircleShape)
        )
    }
}

// 화면 비율 기준 원 정보 + 색상
data class CircleInfoFraction(
    val sizeFraction: Float,
    val offsetXFraction: Float,
    val offsetYFraction: Float,
    val color: Color
)
