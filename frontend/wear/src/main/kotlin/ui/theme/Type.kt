package com.example.hand.wear.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Typography
import com.hand.wear.R

val kyonggi = FontFamily(
    Font(R.font.kyonggi_light, FontWeight.Light),
    Font(R.font.kyonggi_medium, FontWeight.Normal),
    Font(R.font.kyonggi_medium, FontWeight.Medium),
    Font(R.font.kyonggi_bold, FontWeight.Bold)
)

val Typography = Typography(
    defaultFontFamily = kyonggi,
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    title1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    caption1 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)
