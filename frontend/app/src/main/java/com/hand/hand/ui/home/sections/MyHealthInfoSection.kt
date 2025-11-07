package com.hand.hand.ui.home.sections

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.*

@Composable
fun MyHealthInfoSection(horizontalPadding: Dp = 0.dp,
                        soothingHours: Float = 2.5f,
                        stressScore: Int = 0,
                        sleepHours: Int = 8
) {
    Column(
        Modifier.padding(horizontal = horizontalPadding),   // 16.dp → param
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("내 건강 정보", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Brown80, fontFamily = BrandFontFamily)

        // 1) 마음 완화 기록 — 우측 미니 그래프 이미지
        HealthInfoCardRes(
            iconRes = R.drawable.ic_heath_calander,
            title = "마음 완화 기록",
            value = "${format1d(soothingHours)}h / Today",
            iconBg = Green10,
            iconSize = 32.dp,
            trailing = {
                Image(
                    painter = painterResource(R.drawable.ic_heath_graph),
                    contentDescription = null,
                    modifier = Modifier
                        .height(40.dp)
                        .width(72.dp)
                )
            }
        )

        // 2) 오늘의 수면 — 우측 원형 링(0..12) + 중앙 숫자
        val sleepText = "${sleepHours}시간 / Today"

        HealthInfoCardRes(
            iconRes = R.drawable.ic_heath_sleep,
            title = "오늘의 수면",
            value = sleepText,
            iconBg = Purple10,
            iconSize = 32.dp,
            trailing = {
                SleepRing(
                    progressValue = sleepHours.coerceIn(0, 12), // 링 진행도는 0..12로 캡
                    labelText = sleepHours.toString(),          // 가운데 숫자는 입력 그대로 표시
                    max = 12,
                    size = 44.dp,
                    stroke = 8.dp,
                    colorTrack = Color(0xFFEFE9FF),
                    colorProgress = Purple40,
                    textColor = Brown40
                )
            }
        )
        val level = com.hand.hand.ui.model.moodFromScore(stressScore).level
        val caption = com.hand.hand.ui.model.moodCaption(stressScore)
        // 3) 스트레스 레벨 — 제목 → 탭바 → 캡션
        StressInfoCard(
            iconRes = R.drawable.ic_heath_stress,
            title = "스트레스 레벨",
            level = level,
            caption = caption,
            iconSize = 32.dp
        )
    }
}

/** 공통 카드: 좌측 아이콘(webp; 배경 없음) + 타이틀/서브텍스트 + 우측 트레일링 슬롯 */
@Composable
private fun HealthInfoCardRes(
    @DrawableRes iconRes: Int,
    title: String,
    value: String,
    iconBg: Color,
    iconSize: Dp = 32.dp,
    trailing: @Composable () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = title,
                        modifier = Modifier.size(iconSize)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Brown80,  fontFamily = BrandFontFamily)
                    Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Gray50,  fontFamily = BrandFontFamily)
                }
            }
            trailing()
        }
    }
}

/** 스트레스 전용 카드: 제목 → 탭바 → 캡션. 아이콘은 배경 박스 + 큰 아이콘 */
@Composable
private fun StressInfoCard(
    @DrawableRes iconRes: Int,
    title: String,
    level: Int,
    caption: String,
    iconSize: Dp = 32.dp
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Yellow10),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(iconSize)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Brown80, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = BrandFontFamily)
                Spacer(Modifier.height(8.dp))
                StressLevelBar(level = level, barHeight = 6.dp, gap = 8.dp, active = Yellow40, inactive = Gray20)
                Spacer(Modifier.height(8.dp))
                Text(text = caption, color = Gray50, fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = BrandFontFamily)
            }
        }
    }
}

/** 5칸 스트레스 바 */
@Composable
private fun StressLevelBar(
    level: Int,
    barHeight: Dp,
    gap: Dp,
    active: Color,
    inactive: Color
) {
    val clamped = level.coerceIn(0, 5)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(barHeight)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (index < clamped) active else inactive)
            )
        }
    }
}

/** 원형 수면 링(0..max). 중앙에 값 표시 */
@Composable
private fun SleepRing(
    progressValue: Int,                 // ← 진행도(0..max)
    labelText: String,                  // ← 가운데 표시 텍스트(캡 안함)
    max: Int = 12,
    size: Dp = 44.dp,
    stroke: Dp = 8.dp,
    colorTrack: Color = Color(0xFFEFE9FF),
    colorProgress: Color = Purple40,
    textColor: Color = Brown80
) {
    val clamped = progressValue.coerceIn(0, max)
    val sweep = 360f * (clamped.toFloat() / max.toFloat())

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2f
            // 배경
            drawArc(
                color = colorTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.toPx() - strokePx, size.toPx() - strokePx)
            )
            // 진행
            drawArc(
                color = colorProgress,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.toPx() - strokePx, size.toPx() - strokePx)
            )
        }
        Text(
            text = labelText,            // ← 입력 그대로 표시(예: 13)
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = BrandFontFamily
        )
    }
}
//마음 완화 기록 밑에 시간 뒤에 .0제거용
private fun format1d(x: Float): String {
    return if (x % 1f == 0f) x.toInt().toString()
    else String.format(java.util.Locale.US, "%.1f", x)
}
