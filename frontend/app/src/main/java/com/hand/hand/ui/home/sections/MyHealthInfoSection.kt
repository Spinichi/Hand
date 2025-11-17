package com.hand.hand.ui.home.sections

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.window.Dialog
import com.hand.hand.R
import com.hand.hand.carehistory.CareHistoryActivity
import com.hand.hand.ui.theme.*
import java.util.Calendar
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
    stressScore: Int = 0,
    sleepMinutes: Int = 0,
    hasSleepData: Boolean = false,
    todaySessionCount: Int = 0,   // ★ 오늘 마음 완화 실행 횟수 (API에서 전달)
    onSleepDataSaved: () -> Unit = {}
) {
    var showSleepDialog by remember { mutableStateOf(false) }

    // 수면 분을 시/분으로 변환
    val sleepHours = sleepMinutes / 60
    val sleepMins = sleepMinutes % 60

    Column(
        Modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "내 건강 정보",
            fontWeight = FontWeight.Bold,
            fontFamily = BrandFontFamily,
            fontSize = 16.sp,
            color = Brown80
        )

        // ────────────────────────────────
        // ★ 마음 완화 기록 카드
        // ────────────────────────────────
        val context = LocalContext.current
        HealthInfoCardRes(
            iconRes = R.drawable.ic_heath_calander,
            title = "마음 완화 기록",
            value = "${todaySessionCount}회 / Today",   // ★ 오늘 실행 횟수 표시
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
                val intent = Intent(context, CareHistoryActivity::class.java)
                context.startActivity(intent)
            }
        )

        // ────────────────────────────────
        // ★ 오늘의 수면 카드
        // ────────────────────────────────
        val sleepText = if (hasSleepData) {
            if (sleepMins > 0) {
                "${sleepHours}시간 ${sleepMins}분 / Today"
            } else {
                "${sleepHours}시간 / Today"
            }
        } else {
            "오늘의 수면 시간을 입력해주세요!"
        }

        HealthInfoCardRes(
            iconRes = R.drawable.ic_heath_sleep,
            title = "오늘의 수면",
            value = sleepText,
            iconBg = Purple10,
            iconSize = 32.dp,
            trailing = {
                if (hasSleepData) {
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
                }
            },
            modifier = Modifier.clickable { showSleepDialog = true }
        )

        // ────────────────────────────────
        // ★ 스트레스 레벨 카드
        // ────────────────────────────────
        val level = com.hand.hand.ui.model.moodFromScore(stressScore).level
        val caption = com.hand.hand.ui.model.moodCaption(stressScore)

        StressInfoCard(
            iconRes = R.drawable.ic_heath_stress,
            title = "현재 스트레스 레벨",
            level = level,
            caption = caption,
            iconSize = 32.dp
        )
    }

    // ───────────────────────────────────────
    // ★ 수면 입력 다이얼로그
    // ───────────────────────────────────────
    if (showSleepDialog) {

        var sleepStartHour by remember { mutableStateOf(22) }
        var sleepStartMinute by remember { mutableStateOf(0) }
        var sleepStartAmPm by remember { mutableStateOf("PM") }

        var sleepEndHour by remember { mutableStateOf(7) }
        var sleepEndMinute by remember { mutableStateOf(0) }
        var sleepEndAmPm by remember { mutableStateOf("AM") }

        Dialog(
            onDismissRequest = { showSleepDialog = false }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 바깥 클릭 → 닫힘
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showSleepDialog = false }
                )

                // 중앙 모달 카드
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F4F2)),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                        .wrapContentSize(Alignment.Center)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
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
                            initialHour = sleepStartHour,
                            initialMinute = sleepStartMinute,
                            initialAmPm = sleepStartAmPm
                        ) { h, m, ap ->
                            sleepStartHour = h
                            sleepStartMinute = m
                            sleepStartAmPm = ap
                        }

                        SleepWheelPicker(
                            label = "일어난 시간",
                            initialHour = sleepEndHour,
                            initialMinute = sleepEndMinute,
                            initialAmPm = sleepEndAmPm
                        ) { h, m, ap ->
                            sleepEndHour = h
                            sleepEndMinute = m
                            sleepEndAmPm = ap
                        }
                    }
                }

                // 저장 버튼
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(
                        onClick = {
                            // 모달 먼저 닫기
                            showSleepDialog = false

                            val start24 = convertTo24(sleepStartHour, sleepStartAmPm)
                            val end24 = convertTo24(sleepEndHour, sleepEndAmPm)

                            val now = Calendar.getInstance()
                            val startCal = now.clone() as Calendar
                            val endCal = now.clone() as Calendar

                            val startMin = start24 * 60 + sleepStartMinute
                            val endMin = end24 * 60 + sleepEndMinute

                            // 만약 22 → 07이면 전날로 조정
                            if (startMin > endMin) startCal.add(Calendar.DATE, -1)

                            val sleepStartTime = String.format(
                                "%04d-%02d-%02dT%02d:%02d:00",
                                startCal.get(Calendar.YEAR),
                                startCal.get(Calendar.MONTH) + 1,
                                startCal.get(Calendar.DATE),
                                start24,
                                sleepStartMinute
                            )

                            val sleepEndTime = String.format(
                                "%04d-%02d-%02dT%02d:%02d:00",
                                endCal.get(Calendar.YEAR),
                                endCal.get(Calendar.MONTH) + 1,
                                endCal.get(Calendar.DATE),
                                end24,
                                sleepEndMinute
                            )

                            com.hand.hand.api.Sleep.SleepManager.saveSleep(
                                sleepStartTime = sleepStartTime,
                                sleepEndTime = sleepEndTime,
                                onSuccess = { onSleepDataSaved() },
                                onFailure = { /* 실패 시에도 모달은 이미 닫힘 */ }
                            )
                        },
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(48.dp)
                    ) {
                        Text("저장", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                }
            }
        }
    }
}

// 12시간 → 24시간 변환 함수
private fun convertTo24(hour: Int, amPm: String): Int {
    return when {
        amPm == "PM" && hour != 12 -> hour + 12
        amPm == "AM" && hour == 12 -> 0
        else -> hour
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
    iconSize: Dp = 32.dp,

) {
    // ⭐ 스트레스 레벨별 색상 (1=Great, 2=Happy, 3=Okay, 4=Down, 5=Sad)
    // 감정일기 페이지 색상 매핑과 동일하게 적용
    val (iconBgColor, barActiveColor) = when (level) {
        1 -> Pair(Color(0xFFFFF7E6), Color(0xFF9BB167))  // Great - 초록색
        2 -> Pair(Color(0xFFFFF7E6), Color(0xFFFFCE5C))  // Happy - 노란색
        3 -> Pair(Color(0xFFFFF7E6), Color(0xFFC0A091))  // Okay - 베이지색
        4 -> Pair(Color(0xFFFFF7E6), Color(0xFFED7E1C))  // Down - 주황색
        5 -> Pair(Color(0xFFFFF7E6), Color(0xFFC2B1FF))  // Sad - 보라색
        else -> Pair(Color(0xFFFFF7E6), Color(0xFFC0A091))
    }

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
                    .background(iconBgColor),
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
                    active = barActiveColor,
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
