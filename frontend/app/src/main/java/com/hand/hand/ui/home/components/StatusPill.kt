package com.hand.hand.ui.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.hand.hand.ui.theme.*
import androidx.compose.ui.unit.TextUnit

@Composable
fun StatusPill(
    leading: @Composable () -> Unit,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,          // ← 추가
    fontWeight: FontWeight = FontWeight.Medium // ← 선택: 굵기도 조절하고 싶으면
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(28.dp)
    ) {
        leading()
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontWeight = fontWeight,
            fontSize = fontSize,          // ← 적용
            fontFamily = BrandFontFamily
        )
    }
}
