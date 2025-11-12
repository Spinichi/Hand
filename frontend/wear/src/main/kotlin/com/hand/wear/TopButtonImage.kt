package com.hand.wear

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.hand.hand.R

@Composable
fun TopButtonImage(
    screenHeight: Dp,
    context: Context,
    modifier: Modifier = Modifier,
    topMargin: Dp = screenHeight * 0.07f // 기본 상단 여백
) {
    Image(
        painter = painterResource(id = R.drawable.back_white_btn),
        contentDescription = "Back White Button",
        modifier = modifier
            .size(screenHeight * 0.24f) // 버튼 크기
            .padding(top = topMargin) // 🔹 상단 여백
            .clickable(
                indication = null, // 🔹 ripple 제거
                interactionSource = MutableInteractionSource() // 🔹 필수
            ) {
                val intent = Intent(context, WearHomeActivity::class.java)
                context.startActivity(intent)
            },
        contentScale = ContentScale.Fit
    )
}


