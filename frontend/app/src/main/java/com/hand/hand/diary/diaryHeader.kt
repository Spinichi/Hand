package com.hand.hand.diary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DiaryHeader(
    subtitleText: String = "감정 다이어리",
    onBackClick: () -> Unit = {},
    calendar: Calendar,
    onMonthChange: (Calendar) -> Unit,
    titlePaddingTop: Float = 0.03f,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp


    val headerHeight: Dp = screenHeight * 0.25f
    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStartDp: Dp = screenWidth * 0.07f
    val backButtonPaddingTopDp: Dp = screenHeight * 0.05f

    val dateFormat = remember { SimpleDateFormat("yyyy년 M월", Locale.getDefault()) }
    val formattedDate by remember(calendar.time) { mutableStateOf(dateFormat.format(calendar.time)) }
    val titlePaddingTopDp: Dp = screenHeight * titlePaddingTop
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        // 🔸 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFFEF8834),
                    shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp)
                )
        )

        // 🔸 뒤로가기 버튼
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "Back Button",
            modifier = Modifier
                .padding(start = backButtonPaddingStartDp, top = backButtonPaddingTopDp)
                .size(backButtonSize)
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
        )

        // 🔸 타이틀 및 월 변경
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = backButtonPaddingTopDp + backButtonSize + titlePaddingTopDp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 제목 Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    color = Color.White,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.04f).value.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.diary_left_arrow),
                        contentDescription = "Previous Month",
                        modifier = Modifier
                            .size(screenHeight * 0.035f)
                            .clickable {
                                val newCal = (calendar.clone() as Calendar).apply {
                                    add(Calendar.MONTH, -1)
                                }
                                onMonthChange(newCal)
                            }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.diary_right_arrow),
                        contentDescription = "Next Month",
                        modifier = Modifier
                            .size(screenHeight * 0.035f)
                            .clickable {
                                val newCal = (calendar.clone() as Calendar).apply {
                                    add(Calendar.MONTH, 1)
                                }
                                onMonthChange(newCal)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitleText,
                color = Color.White,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = (screenHeight * 0.02f).value.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 30.dp)
            )
        }
    }
}
