package com.hand.hand.care

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily

// 태그+아이콘 모델
data class TagWithIcon(val text: String, val iconRes: Int)

@Composable
fun CareHeader2(
    headerHeightRatio: Float = 0.25f,
    backButtonSizeRatio: Float = 0.06f,
    backButtonPaddingStart: Float = 0.07f,
    backButtonPaddingTop: Float = 0.05f,
    titleText: String = "마음 완화법",
    subtitleTags: List<TagWithIcon> = emptyList(),
    subtitleText: String? = null, // 아이콘 없이 텍스트만
    titlePaddingTop: Float = 0.03f,
    subtitlePaddingTop: Float = 0.01f,
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

        Column(
            modifier = Modifier
                .padding(start = backButtonPaddingStartDp, top = backButtonPaddingTopDp)
                .align(Alignment.TopStart),
            horizontalAlignment = Alignment.Start
        ) {
            // 뒤로가기 버튼
            Image(
                painter = painterResource(id = R.drawable.back_white_btn),
                contentDescription = "Back Button",
                modifier = Modifier
                    .size(backButtonSize)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(titlePaddingTopDp))

            // 제목
            Text(
                text = titleText,
                color = Color.White,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.04f).value.sp
            )

            Spacer(modifier = Modifier.height(subtitlePaddingTopDp))

            // subtitle 처리 (아이콘 유무와 상관없이 둥근 배경 유지)
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (subtitleTags.isNotEmpty()) {
                    subtitleTags.forEach { tag ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFBCCB99),
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(
                                    horizontal = screenWidth * 0.03f,
                                    vertical = screenHeight * 0.008f
                                )
                        ) {
                            Image(
                                painter = painterResource(id = tag.iconRes),
                                contentDescription = tag.text,
                                modifier = Modifier.size(screenHeight * 0.02f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tag.text,
                                fontSize = (screenHeight * 0.018f).value.sp,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                } else if (!subtitleText.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFBCCB99),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .padding(
                                horizontal = screenWidth * 0.03f,
                                vertical = screenHeight * 0.008f
                            )
                    ) {
                        Text(
                            text = subtitleText,
                            fontSize = (screenHeight * 0.018f).value.sp,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
