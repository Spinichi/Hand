package com.hand.hand.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hand.hand.ui.theme.BrandFontFamily

@Composable
fun LoadingDialog(
    message: String = "잠시만 기다려주세요"
) {
    Dialog(onDismissRequest = { /* 로딩 중엔 닫을 수 없음 */ }) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color(0xFFC2B1FF), // 보라색 스피너
                    strokeWidth = 4.dp
                )

                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = BrandFontFamily,
                    color = Color(0xFF4F3422)
                )
            }
        }
    }
}