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
    val screenHeight = config.screenHeightDp.dp
    val screenWidth = config.screenWidthDp.dp

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
                mostEffectiveName = response.data.statistics.mostEffective.name
                mostUsedName = response.data.statistics.mostUsed.name
                historyData = response.data.history
                Log.d(
                    "CareHistoryScreen",
                    "API 성공: mostEffective=$mostEffectiveName, mostUsed=$mostUsedName, history=${historyData.size}"
                )
            },
            onFailure = { t ->
                Log.e("CareHistoryScreen", "API 실패", t)
                mostEffectiveName = "불러오기 실패"
                mostUsedName = "불러오기 실패"
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 전체 배경을 흰색으로 변경
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
        // 🔥 스크롤 외 영역 (고정 영역)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    top = screenHeight * 0.4f,
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.07f
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

                historyData.forEach { dayHistory: CareHistoryDay ->

                    val dateParts = dayHistory.date.split("-")
                    val month = dateParts.getOrNull(1) ?: ""
                    val day = dateParts.getOrNull(2) ?: ""

                    dayHistory.sessions.forEach { session: CareSession ->
                        CalmHistoryItem(
                            month = month,
                            day = day,
                            method = session.interventionName,
                            score = session.reduction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp)
                        )
                    }
                }
            }
        }


        // 📌 FloatingCard: 화면 어디든 자유롭게 배치 가능
        FloatingRowCalmCards(
            card1Title = "효과가 좋은 완화법",
            card1Tag = mostEffectiveName,
            card2Title = "자주 사용한 완화법",
            card2Tag = mostUsedName,
            x = screenWidth * 0.05f,
            y = screenHeight * 0.15f,
            spacing = 16.dp
        )

        // 📌 FloatingIcon: 화면 어디든 자유롭게 배치 가능
        val iconSize = screenHeight * 0.08f  // 아이콘 크기
        FloatingIcon(
            resId = R.drawable.carehistory_icon,
            x = screenWidth / 2 - iconSize / 2,  // 화면 가운데
            y = screenHeight * 0.29f,           // 원하는 y 위치
            size = iconSize                      // 아이콘 크기
        )
    }

}

// -------------------------
// 나머지 컴포저블은 이전 코드와 동일
// BrandWaveHeader2, CalmHistoryItem, FloatingRowCalmCards, FloatingIcon 등
// -------------------------



// --------------------------------------------------
// 🔹 하단 UI 컴포넌트 (기존 그대로)
// --------------------------------------------------

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
        content()
    }
}

@Composable
fun CalmItemCard(
    title: String,
    tagText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(20.dp))
            .padding(vertical = 15.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(
                text = title,
                fontFamily = BrandFontFamily,
                fontSize = 20.sp,
                color = Color(0xFF4F3422)
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF9BB168), shape = RoundedCornerShape(100.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tagText,
                    fontSize = 20.sp,
                    fontFamily = BrandFontFamily,
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
    spacing: Dp = 16.dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.absoluteOffset(x = x, y = y),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalmItemCard(title = card1Title, tagText = card1Tag)
            CalmItemCard(title = card2Title, tagText = card2Tag)
        }
    }
}

@Composable
fun FloatingIcon(
    resId: Int,
    x: Dp,
    y: Dp,
    size: Dp
) {
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
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

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
                drawCircle(color = Color(0xFFDAE3C3))
                val sweep = 360 * (score / 100f)
                drawArc(
                    color = Color(0xFF9BB167),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = true
                )
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
