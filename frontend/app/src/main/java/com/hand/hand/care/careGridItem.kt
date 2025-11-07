package com.hand.hand.care

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CareGridItem(
    text: String,
    subText: String? = null,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    onClick: (() -> Unit)? = null
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color(0x80000000),
                spotColor = Color(0x80000000)
            )
            .background(Color.White, RoundedCornerShape(cornerRadius))
            .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.03f)
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = text,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.021f).value.sp,
                    color = Color(0xFF4F3422)
                )

                if (subText != null) {
                    Spacer(modifier = Modifier.height(screenHeight * 0.005f))
                    Text(
                        text = subText,
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.02f).value.sp,
                        color = Color(0xFFB0ADA9)
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.care_action_back),
                contentDescription = null,
                modifier = Modifier.size(screenHeight * 0.025f)
            )
        }
    }
}
