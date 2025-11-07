package com.hand.hand.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.hand.hand.R
import com.hand.hand.ui.home.dialog.AdminLoginDialog
import com.hand.hand.ui.home.header.HomeGreetingHeader
import com.hand.hand.ui.theme.Brown10
import com.hand.hand.ui.theme.Brown80
import com.hand.hand.ui.theme.Orange20
import com.hand.hand.ui.theme.Orange60

@Composable
fun PersonalScreen(
    // 헤더용
    userName: String = "김싸피",
    isWritten: Boolean = false,
    heartRateBpm: Int = 75,
    moodLabel: String = "Happy",
    recommendation: String = "봉인 연습",

    // 본문 카드용
    avgScore: Int = 23,
    warningText: String = "불안 증세를 보이므로 주의를 요함",
    favoritePractice: String = "봉인 연습",
    avgMoodChangesPerDay: Int = 5,
    monthStatus: String = "불안 증세를 보이므로 주의를 요함.\n수면 시간이 평소보다 줄어듦.\n최근 1주 감정 변화 증가."
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    // 반응형 스케일 (화면 폭 기준)
    val cfg = LocalConfiguration.current
    val widthDp = cfg.screenWidthDp.toFloat()
    val heightDp = cfg.screenHeightDp
    val scale = (widthDp / 360f).coerceIn(0.85f, 1.25f)

    fun sdp(v: Dp): Dp = (v.value * scale).dp
    fun ssp(v: TextUnit): TextUnit = (v.value * scale).sp

    // 토큰
    val pagePad = sdp(16.dp)
    val sectionGap = sdp(16.dp)
    val bigRound = sdp(32.dp)
    val nameIconSize = sdp(45.dp)
    val nameTextSize = ssp(45.sp)
    val nameBadgeGap = sdp(60.dp)
    val badgePadH = sdp(12.dp)
    val badgePadV = sdp(6.dp)

    // 헤더 높이: 전체 화면의 25%
    val headerHeight = (heightDp * 0.25f).dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brown10)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 상단 공용 헤더 (25% 높이 적용) ──
        HomeGreetingHeader(
            dateText = "2025.11.05",
            onModeToggle = { showDialog = true },
            userName = userName,
            isWritten = isWritten,
            heartRateBpm = heartRateBpm,
            moodLabel = moodLabel,
            recommendation = recommendation,
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        )

        // 본문
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = pagePad, vertical = sectionGap),
            verticalArrangement = Arrangement.spacedBy(sectionGap)
        ) {
            // ========= 이름/점수 (가운데 정렬, 반응형) =========
            NameRowCentered(
                name = userName,
                moodIconRes = R.drawable.ic_solid_mood_sad,
                avgScore = avgScore,
                nameIconSize = nameIconSize,
                nameTextSize = nameTextSize,
                nameBadgeGap = nameBadgeGap,
                badgePadH = badgePadH,
                badgePadV = badgePadV
            )

            // 경고 배너
            WarningBanner(
                text = warningText,
                corner = bigRound
            )

            // 통계 캡슐 2개
            Row(
                horizontalArrangement = Arrangement.spacedBy(sdp(12.dp)),
                modifier = Modifier.fillMaxWidth()
            ) {
                PillStatCard(
                    modifier = Modifier.weight(1f),
                    bg = Color(0xFF9AB067),
                    title = "자주 사용한 완화법",
                    mainText = favoritePractice,
                    leadingRes = R.drawable.ic_mini_graph,
                    height = sdp(110.dp),
                    corner = bigRound,
                    titleSize = ssp(13.sp),
                    valueSize = ssp(26.sp)
                )
                PillStatCard(
                    modifier = Modifier.weight(1f),
                    bg = Color(0xFFA588FB),
                    title = "평균 감정 변화 횟수",
                    mainText = "${avgMoodChangesPerDay}회",
                    leadingRes = R.drawable.ic_mini_heart_white,
                    height = sdp(110.dp),
                    corner = bigRound,
                    titleSize = ssp(13.sp),
                    valueSize = ssp(26.sp)
                )
            }

            // 한달간 심리 상태 (타이틀은 밖, 내용은 단일 텍스트)
            MonthStatusSection(
                text = monthStatus,
                corner = bigRound,
                titleSize = ssp(18.sp),
                bodySize = ssp(16.sp),
                bodyLine = ssp(22.sp),
                outerStart = sdp(2.dp),
                outerBottom = sdp(10.dp),
                innerPadH = sdp(18.dp),
                innerPadV = sdp(16.dp)
            )

            Spacer(Modifier.height(sdp(90.dp))) // 네브바 여유
        }
    }

    if (showDialog) {
        AdminLoginDialog(
            onClose = { showDialog = false },
            onEnterGroupCode = { /* TODO */ },
            onAdminLoginClick = { /* TODO */ },
            onOrgClick = { /* TODO */ }
        )
    }
}

/* --------------------------- Pieces --------------------------- */

@Composable
private fun NameRowCentered(
    name: String,
    moodIconRes: Int,
    avgScore: Int,
    nameIconSize: Dp,
    nameTextSize: TextUnit,
    nameBadgeGap: Dp,
    badgePadH: Dp,
    badgePadV: Dp
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(nameIconSize)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(moodIconRes),
                    contentDescription = "현재 기분",
                    modifier = Modifier.size(nameIconSize)
                )
            }

            Spacer(Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    fontSize = nameTextSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Brown80
                )

                Spacer(Modifier.width(nameBadgeGap))

                Surface(
                    color = Orange20,
                    contentColor = Orange60,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "평균 $avgScore 점",
                        fontSize = 14.sp, // 필요하면 ssp(14.sp)로 변경
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = badgePadH, vertical = badgePadV)
                    )
                }
            }
        }
    }
}

@Composable
private fun WarningBanner(
    text: String,
    corner: Dp
) {
    Surface(
        color = Color(0xFFEF8834),
        contentColor = Color.White,
        shape = RoundedCornerShape(corner),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun PillStatCard(
    modifier: Modifier = Modifier,
    bg: Color,
    title: String,
    mainText: String,
    leadingRes: Int,
    height: Dp,
    corner: Dp,
    titleSize: TextUnit,
    valueSize: TextUnit
) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(corner),
        modifier = modifier.height(height)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(leadingRes),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = mainText,
                color = Color.White,
                fontSize = valueSize,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MonthStatusSection(
    text: String,
    corner: Dp,
    titleSize: TextUnit,
    bodySize: TextUnit,
    bodyLine: TextUnit,
    outerStart: Dp,
    outerBottom: Dp,
    innerPadH: Dp,
    innerPadV: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "한달간 심리 상태",
            color = Brown80,
            fontSize = titleSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = outerStart, bottom = outerBottom)
        )
        Card(
            shape = RoundedCornerShape(corner),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                color = Brown80,
                fontSize = bodySize,
                lineHeight = bodyLine,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = innerPadH, vertical = innerPadV)
            )
        }
    }
}
