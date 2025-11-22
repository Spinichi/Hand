package com.hand.hand.carehistory

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.api.CareHistory.CareHistoryDay
import com.hand.hand.api.CareHistory.CareHistoryManager
import com.hand.hand.api.CareHistory.CareHistoryResponse
import com.hand.hand.api.CareHistory.CareSession
import com.hand.hand.ui.theme.BrandFontFamily

class CareHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareHistoryScreen(onBackClick = { finish() })
        }
    }
}

@Composable
fun CareHistoryScreen(onBackClick: () -> Unit) {

    val config = LocalConfiguration.current
    val screenHeightDp = config.screenHeightDp.takeIf { it > 0 } ?: 800
    val screenWidthDp = config.screenWidthDp.takeIf { it > 0 } ?: 400
    val screenHeight = screenHeightDp.dp
    val screenWidth = screenWidthDp.dp

    // 🔹 API 데이터 상태
    var mostEffectiveName by remember { mutableStateOf("불러오는 중...") }
    var mostUsedName by remember { mutableStateOf("불러오는 중...") }
    var historyData by remember { mutableStateOf(listOf<CareHistoryDay>()) }

    // 🔹 API 호출
    LaunchedEffect(Unit) {
        CareHistoryManager.getCareHistory(
            page = 0,
            size = 7,
            onSuccess = { response: CareHistoryResponse ->
                mostEffectiveName = response.data.statistics.mostEffective?.name ?: "없음"
                mostUsedName = response.data.statistics.mostUsed?.name ?: "없음"
                historyData = response.data.history ?: emptyList()
                Log.d(
                    "CareHistoryScreen",
                    "API 성공: mostEffective=$mostEffectiveName, mostUsed=$mostUsedName, history=${historyData.size}"
                )
            },
            onFailure = { t ->
                Log.e("CareHistoryScreen", "API 실패", t)
                mostEffectiveName = "불러오기 실패"
                mostUsedName = "불러오기 실패"
                historyData = emptyList()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // 🔥 헤더 Wave
        BrandWaveHeader2(
            fillColor = Color(0xFF9BB168),
            edgeY = screenHeight * 0.28f,
            centerY = screenHeight * 0.4f,
            overhang = 40.dp,
            height = screenHeight * 0.22f,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 뒤로가기 버튼
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = screenWidth * 0.07f, top = screenHeight * 0.05f)
                .size(screenHeight * 0.06f)
                .clickable { onBackClick() }
        )

        // 타이틀
        Text(
            text = "마음 완화 기록",
            fontFamily = BrandFontFamily,
            fontSize = (screenHeight * 0.03f).value.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.1f + screenHeight * 0.07f,
                    top = screenHeight * 0.065f
                )
        )

        // 📌 메인 컨텐츠 (스크롤용)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    top = screenHeight * 0.4f,
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.05f
                )
        ) {
            Text(
                text = "마음 완화 히스토리",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.025f).value.sp,
                color = Color(0xFF4F3422)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 리스트만 스크롤
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (historyData.isEmpty()) {
                    Text(
                        text = "마음 완화 히스토리가 없습니다.",
                        fontFamily = BrandFontFamily,
                        fontSize = 18.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    historyData.forEach { dayHistory ->
                        val dateParts = dayHistory.date?.split("-") ?: listOf("", "", "")
                        val month = dateParts.getOrNull(1)?.takeIf { it.isNotEmpty() } ?: "00"
                        val day = dateParts.getOrNull(2)?.takeIf { it.isNotEmpty() } ?: "00"

                        dayHistory.sessions?.forEach { session ->
                            val safeScore = (session.reduction ?: 0).coerceIn(0, 100)
                            CalmHistoryItem(
                                month = month,
                                day = day,
                                method = session.interventionName ?: "알 수 없음",
                                score = safeScore,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 📌 FloatingCard
        FloatingRowCalmCards(
            card1Title = "효과적인 방법",
            card1Tag = mostEffectiveName,
            card2Title = "자주 사용",
            card2Tag = mostUsedName,
            x = screenWidth * 0.05f,
            y = screenHeight * 0.15f,
            maxWidth = screenWidth * 0.9f,
            spacing = 12.dp
        )

        // 📌 FloatingIcon
        val iconSize = screenHeight * 0.08f
        FloatingIcon(
            resId = R.drawable.carehistory_icon,
            x = screenWidth / 2 - iconSize / 2,
            y = screenHeight * 0.29f,
            size = iconSize
        )
    }
}
// -------------------------
// 하단 컴포저블
// -------------------------

@Composable
fun BrandWaveHeader2(
    fillColor: Color,
    edgeY: Dp,
    centerY: Dp,
    overhang: Dp = 0.dp,
    height: Dp = centerY + 40.dp,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit) = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F4F2))
            .height(height),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (size.width > 0 && size.height > 0) {
                val w = size.width
                val edgeYpx = edgeY.toPx()
                val centerYpx = centerY.toPx()
                val oh = overhang.toPx()

                val p = Path().apply {
                    moveTo(-oh, 0f)
                    lineTo(w + oh, 0f)
                    lineTo(w + oh, edgeYpx)
                    quadraticBezierTo(
                        w / 2, centerYpx,
                        -oh, edgeYpx
                    )
                    close()
                }

                drawPath(p, fillColor)
            }
        }
        content()
    }
}

@Composable
fun CalmItemCard(title: String, tagText: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                fontFamily = BrandFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4F3422)
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF9BB168), shape = RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tagText,
                    fontSize = 16.sp,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FloatingRowCalmCards(
    card1Title: String,
    card1Tag: String,
    card2Title: String,
    card2Tag: String,
    x: Dp,
    y: Dp,
    maxWidth: Dp,
    spacing: Dp = 16.dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .absoluteOffset(x = x, y = y)
                .width(maxWidth),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalmItemCard(
                title = card1Title,
                tagText = card1Tag,
                modifier = Modifier.weight(1f)
            )
            CalmItemCard(
                title = card2Title,
                tagText = card2Tag,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FloatingIcon(resId: Int, x: Dp, y: Dp, size: Dp) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier
                .absoluteOffset(x = x, y = y)
                .size(size)
        )
    }
}

@Composable
fun CalmHistoryItem(
    month: String,
    day: String,
    method: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.takeIf { it > 0 }?.dp ?: 800.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.takeIf { it > 0 }?.dp ?: 400.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F4F2), shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(screenWidth * 0.15f)
                .height(screenHeight * 0.08f)
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${month}월",
                    fontSize = 14.sp,
                    fontFamily = BrandFontFamily,
                    color = Color(0xFFB0ADA9)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${day}일",
                    fontSize = 20.sp,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F3422)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = method,
            fontFamily = BrandFontFamily,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4F3422),
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier.size(screenHeight * 0.09f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (size.width > 0 && size.height > 0) {
                    drawCircle(color = Color(0xFFDAE3C3))
                    val sweep = 360 * (score / 100f)
                    drawArc(
                        color = Color(0xFF9BB167),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(screenHeight * 0.06f)
                    .background(Color.White, shape = RoundedCornerShape(50))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        fontFamily = BrandFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F3422)
                    )
                    Text(
                        text = "회복",
                        fontFamily = BrandFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF4F3422)
                    )
                }
            }
        }
    }
}
