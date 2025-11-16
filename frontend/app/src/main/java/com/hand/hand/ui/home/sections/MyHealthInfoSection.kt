package com.hand.hand.ui.home.sections

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.carehistory.CareHistoryActivity
import com.hand.hand.ui.theme.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.min

@Composable
fun MyHealthInfoSection(
    horizontalPadding: Dp = 0.dp,
    soothingHours: Float = 2.5f,
    stressScore: Int = 0,
    sleepHours: Int = 8
) {
    var showSleepDialog by remember { mutableStateOf(false) }

    Column(
        Modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "내 건강 정보",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Brown80,
            fontFamily = BrandFontFamily
        )

        // 마음 완화 기록
        val context = LocalContext.current
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
            },
            modifier = Modifier.clickable {
                // 클릭 시 CareHistoryActivity로 이동
                val intent = Intent(context, CareHistoryActivity::class.java)
                context.startActivity(intent)
            }
        )

        // 오늘의 수면
        val sleepText = "${sleepHours}시간 / Today"
        HealthInfoCardRes(
            iconRes = R.drawable.ic_heath_sleep,
            title = "오늘의 수면",
            value = sleepText,
            iconBg = Purple10,
            iconSize = 32.dp,
            trailing = {
                SleepRing(
                    progressValue = sleepHours.coerceIn(0, 12),
                    labelText = sleepHours.toString(),
                    max = 12,
                    size = 44.dp,
                    stroke = 8.dp,
                    colorTrack = Color(0xFFEFE9FF),
                    colorProgress = Purple40,
                    textColor = Brown40
                )
            },
            modifier = Modifier.clickable { showSleepDialog = true } // 클릭 시 모달
        )

        // 스트레스 레벨
        val level = com.hand.hand.ui.model.moodFromScore(stressScore).level
        val caption = com.hand.hand.ui.model.moodCaption(stressScore)
        StressInfoCard(
            iconRes = R.drawable.ic_heath_stress,
            title = "스트레스 레벨",
            level = level,
            caption = caption,
            iconSize = 32.dp
        )
    }

    // 수면 모달
    // 수면 모달
    if (showSleepDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showSleepDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 모달 내용
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F4F2)),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                        .clickable(enabled = false) {} // Card 안쪽 클릭은 닫지 않음
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Text(
                            text = "오늘의 수면 기록",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brown80,
                            fontFamily = BrandFontFamily
                        )

                        SleepWheelPicker(
                            label = "잠든 시간",
                            initialHour = 10,
                            initialMinute = 0,
                            initialAmPm = "PM"
                        ) { hour, minute, amPm -> }

                        SleepWheelPicker(
                            label = "일어난 시간",
                            initialHour = 7,
                            initialMinute = 0,
                            initialAmPm = "AM"
                        ) { hour, minute, amPm -> }
                    }
                }

                // 모달 바깥쪽 아래 저장 버튼
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp), // 화면 아래에서 32dp 위
                    contentAlignment = Alignment.BottomCenter
                ) {
                    androidx.compose.material3.Button(
                        onClick = {
                            // 저장 로직
                            showSleepDialog = false
                        },
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(48.dp)
                    ) {
                        Text(text = "저장", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }



}

/** 공통 카드 */
@Composable
private fun HealthInfoCardRes(
    @DrawableRes iconRes: Int,
    title: String,
    value: String,
    iconBg: Color,
    iconSize: Dp = 32.dp,
    trailing: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Brown80,
                        fontFamily = BrandFontFamily
                    )
                    Text(
                        value,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Gray50,
                        fontFamily = BrandFontFamily
                    )
                }
            }
            trailing()
        }
    }
}

/** 스트레스 카드 */
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
                Text(
                    text = title,
                    color = Brown80,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BrandFontFamily
                )
                Spacer(Modifier.height(8.dp))
                StressLevelBar(
                    level = level,
                    barHeight = 6.dp,
                    gap = 8.dp,
                    active = Yellow40,
                    inactive = Gray20
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = caption,
                    color = Gray50,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = BrandFontFamily
                )
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

/** 원형 수면 링 */
@Composable
private fun SleepRing(
    progressValue: Int,
    labelText: String,
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
            drawArc(
                color = colorTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.toPx() - strokePx, size.toPx() - strokePx)
            )
            drawArc(
                color = colorProgress,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.toPx() - strokePx, size.toPx() - strokePx)
            )
        }
        Text(
            text = labelText,
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = BrandFontFamily
        )
    }
}

// 마음 완화 기록 소수 제거
private fun format1d(x: Float): String {
    return if (x % 1f == 0f) x.toInt().toString()
    else String.format(java.util.Locale.US, "%.1f", x)
}

@Composable
fun SleepWheelPicker(
    initialHour: Int = 10,
    initialMinute: Int = 0,
    initialAmPm: String = "PM",
    label: String = "잠든 시간",
    onTimeChange: (hour: Int, minute: Int, amPm: String) -> Unit = { _, _, _ -> }
) {
    var selectedHour by remember { mutableStateOf(initialHour.coerceIn(1,12)) }
    var selectedMinute by remember { mutableStateOf(initialMinute.coerceIn(0,59)) }
    var selectedAmPm by remember { mutableStateOf(initialAmPm) }

    val hours = (1..12).toList()
    val minutes = (0..59).toList()
    val ampmList = listOf("AM", "PM")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp) // 라벨과 WheelPicker 간 간격 줄임
    ) {
        Text(
            text = label,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = Brown80,
            fontFamily = BrandFontFamily
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Hour
            WheelPicker(items = hours.map { it.toString() }, selectedItem = selectedHour.toString()) { value ->
                selectedHour = value.toInt()
                onTimeChange(selectedHour, selectedMinute, selectedAmPm)
            }
            // Minute
            WheelPicker(items = minutes.map { it.toString().padStart(2,'0') }, selectedItem = selectedMinute.toString().padStart(2,'0')) { value ->
                selectedMinute = value.toInt()
                onTimeChange(selectedHour, selectedMinute, selectedAmPm)
            }
            // AM/PM
            WheelPicker(items = ampmList, selectedItem = selectedAmPm) { value ->
                selectedAmPm = value
                onTimeChange(selectedHour, selectedMinute, selectedAmPm)
            }
        }
    }
}

@Composable
fun WheelPicker(
    items: List<String>,
    selectedItem: String,
    visibleCount: Int = 3,
    onItemSelected: (String) -> Unit
) {
    val itemHeight = 36.dp // 높이 조금 줄임
    var selectedIndex by remember { mutableStateOf(items.indexOf(selectedItem)) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .width(60.dp)
            .height(itemHeight * visibleCount)
            .background(Color(0xFFEFEFEF), shape = RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    offsetY += dragAmount
                    if (offsetY > itemHeight.toPx() / 2) {
                        offsetY = 0f
                        selectedIndex = (selectedIndex - 1).coerceIn(0, items.size - 1)
                        onItemSelected(items[selectedIndex])
                    } else if (offsetY < -itemHeight.toPx() / 2) {
                        offsetY = 0f
                        selectedIndex = (selectedIndex + 1).coerceIn(0, items.size - 1)
                        onItemSelected(items[selectedIndex])
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            // 위 숫자
            if (selectedIndex > 0) {
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .background(Color(0xFFEFEFEF), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[selectedIndex - 1],
                        fontSize = 14.sp, // 위/아래 숫자 작게
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(itemHeight))
            }

            // 중앙 선택 숫자
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth()
                    .background(Color(0xFFC2B1FF), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = items[selectedIndex],
                    fontSize = 18.sp, // 중앙 숫자 조금 줄임
                    fontWeight = FontWeight.Bold,
                    color = Brown80
                )
            }

            // 아래 숫자
            if (selectedIndex < items.size - 1) {
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .background(Color(0xFFEFEFEF), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[selectedIndex + 1],
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(itemHeight))
            }
        }
    }
}
