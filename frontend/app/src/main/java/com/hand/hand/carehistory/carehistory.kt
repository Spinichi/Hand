package com.hand.hand.carehistory

import android.os.Bundle
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
import androidx.compose.runtime.Composable
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
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    top = screenHeight * 0.4f,
                    start = screenWidth * 0.0f,
                    end = screenWidth * 0.07f,
                    bottom = screenHeight * 0.02f
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 본문 제목
            Text(
                text = "마음 완화 히스토리",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = screenWidth * 0.05f),
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.025f).value.sp,
                color = Color(0xFF4F3422)
            )
            CalmHistoryItem(
                month = "11월",
                day = "16일",
                method = "안전지대 연습",
                score = 50,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp) // 마진처럼 왼쪽 띄우기
            )

            // 예시 카드 (추가 가능)
            // CalmItemCard(title = "예제 카드", tagText = "태그")
        }

        // 📌 FloatingCard: 화면 어디든 자유롭게 배치 가능
        FloatingRowCalmCards(
            card1Title = "효과가 좋은 완화법",
            card1Tag = "안정 호흡법",
            card2Title = "자주 사용한 완화법",
            card2Tag = "안전지대 연습",
            x = screenWidth * 0.05f,
            y = screenHeight * 0.15f,
            spacing = 16.dp
        )

        // 📌 FloatingIcon: 화면 어디든 자유롭게 배치 가능
        val iconSize = screenHeight * 0.08f  // 아이콘 크기
        FloatingIcon(
            resId = R.drawable.carehistory_icon,
            x = screenWidth / 2 - iconSize / 2,  // 화면 가운대
            y = screenHeight * 0.29f,             // 원하는 y 위치
            size = iconSize                       // 아이콘 크기
        )

    }
}

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
    score: Int,       // 0 ~ 100
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
        // 왼쪽 날짜 박스
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
                    text = month,
                    fontSize = 14.sp,
                    fontFamily = BrandFontFamily,
                    color = Color(0xFFB0ADA9) // 월 글씨 색 변경
                )
                Spacer(modifier = Modifier.height(4.dp)) // 세
                Text(
                    text = day,
                    fontSize = 20.sp, // 글자 조금 더 크게
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F3422) // 일 글씨 색 변경
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 중앙 텍스트
        Text(
            text = method,
            fontFamily = BrandFontFamily,
            fontSize = 25.sp, // 더 크게
            fontWeight = FontWeight.Bold, // Bold
            color = Color(0xFF4F3422),
            modifier = Modifier.weight(1f)
        )

        // 오른쪽 원형 회복 점수
        Box(
            modifier = Modifier.size(screenHeight * 0.09f), // 전체 조금 더 크게
            contentAlignment = Alignment.Center
        ) {
            // 배경 원
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

            // 작은 흰색 원 안에 점수와 회복 텍스트
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(screenHeight * 0.06f) // 조금 더 크게
                    .background(Color.White, shape = RoundedCornerShape(50))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        fontFamily = BrandFontFamily,
                        fontSize = 20.sp, // 조금 더 크게
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F3422)
                    )
                    Text(
                        text = "회복",
                        fontFamily = BrandFontFamily,
                        fontSize = 15.sp, // 조금 더 크게
                        color = Color(0xFF4F3422)
                    )
                }
            }
        }
    }
}

