package com.hand.hand.care

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.hand.hand.R
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import com.hand.hand.ui.theme.BrandFontFamily
import androidx.compose.ui.unit.sp


@Composable
fun CareHeader(
    headerHeightRatio: Float = 0.25f,
    backButtonSizeRatio: Float = 0.06f,
    backButtonPaddingStart: Float = 0.07f,
    backButtonPaddingTop: Float = 0.05f,
    titleText: String = "마음 완화법",
    subtitleText: String = "추천 마음 완화법 - \"안전지대 연습\"", // ← 추가
    titlePaddingTop: Float = 0.03f,
    subtitlePaddingTop: Float = 0.01f, // 제목과 부제목 사이 간격
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val headerHeight: Dp = screenHeight * headerHeightRatio
    val backButtonSize: Dp = screenHeight * backButtonSizeRatio
    val backButtonPaddingStartDp: Dp = screenWidth * backButtonPaddingStart
    val backButtonPaddingTopDp: Dp = screenHeight * backButtonPaddingTop
    val titlePaddingTopDp: Dp = screenHeight * titlePaddingTop
    val subtitlePaddingTopDp: Dp = screenHeight * subtitlePaddingTop

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        // 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFF9BB167),
                    shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp)
                )
        )

        // back 버튼 + 제목 + 부제목
        Column(
            modifier = Modifier
                .padding(start = backButtonPaddingStartDp, top = backButtonPaddingTopDp)
                .align(Alignment.TopStart),
            horizontalAlignment = Alignment.Start
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_white_btn),
                contentDescription = "Back Button",
                modifier = Modifier
                    .size(backButtonSize)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(titlePaddingTopDp))

            Text(
                text = titleText,
                color = Color.White,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.04f).value.sp
            )

            Spacer(modifier = Modifier.height(subtitlePaddingTopDp))

            Text(
                text = subtitleText,
                color = Color.White,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = (screenHeight * 0.02f).value.sp
            )
        }
    }
}


