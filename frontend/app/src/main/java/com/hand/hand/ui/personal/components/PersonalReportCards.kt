// file: com/hand/hand/ui/personal/components/PersonalReportCards.kt
package com.hand.hand.ui.personal.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.hand.hand.ui.theme.BrandFontFamily
import androidx.compose.ui.text.font.FontWeight

private val CardWhite   = Color(0xFFFFFFFF)
private val ChipF7F4F2  = Color(0xFFF7F4F2)
private val TextBrown80 = Color(0xFF4B3A2F) // 기존 Brown80 톤과 유사(앱 Theme에 있으면 교체)

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = TextBrown80,
        fontSize = 20.sp,                 // 요청: 본문 20sp
        fontWeight = FontWeight.Bold,     // 요청: Bold
        fontFamily = BrandFontFamily,
        modifier = modifier
    )
}

/** “월간 감정보고서” 큰 카드 (배경 #FFFFFF, corner 25) */
@Composable
fun MonthReportCard(
    monthLabel: String,                 // "10월"
    title: String,                      // "AI 분석 감정 보고서"
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    minHeight: Dp = 0.dp,
) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(25.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 칩 (배경 #F7F4F2, corner 15)
            Box(
                modifier = Modifier
                    .widthIn(min = 64.dp)
                    .background(ChipF7F4F2, RoundedCornerShape(15.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monthLabel,
                    color = TextBrown80,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BrandFontFamily
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = title,
                color = TextBrown80,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BrandFontFamily
            )
        }
    }
}

/** “주간 감정보고서” 행 카드 (배경 #FFFFFF, corner 25 / 좌측 칩 #F7F4F2, corner 15) */
@Composable
fun WeekReportRow(
    weekLabel: String,                  // "1주차"
    title: String,                      // "AI 분석 감정 보고서"
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(25.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(ChipF7F4F2, RoundedCornerShape(15.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = weekLabel,
                    color = TextBrown80,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BrandFontFamily
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = title,
                color = TextBrown80,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BrandFontFamily
            )
        }
    }
}
