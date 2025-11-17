package com.hand.hand.ui.home.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import com.hand.hand.R
import com.hand.hand.ui.home.components.StatusPill
import com.hand.hand.ui.theme.*

@Composable
fun HomeGreetingHeader(
    dateText: String,
    onModeToggle: () -> Unit,
    userName: String,
    isWritten: Boolean,
    heartRateBpm: Int,
    moodLabel: String,
    recommendation: String,
    modifier: Modifier = Modifier,
    diaryStatus: String = if (isWritten) "작성 완료" else "작성 전",

    // CareHeader2와 매칭되는 값들
    headerHeightRatio: Float = 0.25f,
    horizontalGutterRatio: Float = 0.07f,
    topPaddingRatio: Float = 0.05f,
    bottomCornerRadius: Dp = 50.dp,
    // 외곽 배경색(기본: CareHeader2의 녹색)
    backgroundColor: Color = Brown80,

    // 상위에서 Dp로 직접 넘기면 그 값을 우선 사용
    horizontalGutter: Dp? = null,
) {
    val conf = LocalConfiguration.current
    val screenH = conf.screenHeightDp.dp
    val screenW = conf.screenWidthDp.dp

    // 상태바 높이
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

//    // 보이는 헤더 높이(25%) + 상태바 포함한 실제 topBar 높이
//    val headerVisible = screenH * headerHeightRatio
//    val headerTotal = headerVisible + statusTop
//
//    // 내부 패딩 계산
//    val topPadding = screenH * topPaddingRatio
    val resolvedHorizontalGutter = horizontalGutter ?: (screenW * horizontalGutterRatio)
    val topPadding = 16.dp

    // === CareHeader2 외곽 모양(파일 나누기 없이 인라인) ===
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // ← 콘텐츠 높이만큼. 잘림 방지
            .background( // ← 배경을 바깥 Box에 직접 적용 (matchParentSize 제거)
                color = backgroundColor,
                shape = RoundedCornerShape(
                    topStart = 0.dp, topEnd = 0.dp,
                    bottomStart = bottomCornerRadius, bottomEnd = bottomCornerRadius
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusTop) // 상태바 만큼 내림
                .padding(
                    start = resolvedHorizontalGutter,
                    end = resolvedHorizontalGutter,
                    top = topPadding,    // ← dp 기반
                    bottom = 16.dp
                )
        )  {
            // 상단 날짜 + 모드
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mini_calendar),
                        contentDescription = "캘린더",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        dateText,
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BrandFontFamily
                    )
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    color = Brown20,
                    contentColor = Brown60,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.height(32.dp),
                    onClick = onModeToggle
                ) {
                    Box(
                        Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "모드 전환 하기",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 아바타 + 이름 + 상태 필
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_user_round),
                    contentDescription = "사용자",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "안녕하세요! $userName!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        fontFamily = BrandFontFamily
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        /*
                        StatusPill(
                            leading = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_edit),
                                    contentDescription = "작성 상태",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            text = diaryStatus,
                            color = White,
                            fontSize = 18.sp,
                        )

                        Spacer(Modifier.width(10.dp))
                        */

                        StatusPill(
                            leading = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_heart_ecg),
                                    contentDescription = "심박수",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            text = "$heartRateBpm bpm",
                            color = White,
                            fontSize = 18.sp
                        )

                        Spacer(Modifier.width(10.dp))

                        StatusPill(
                            leading = {
                                Icon(
                                    painter = painterResource(
                                        when (moodLabel.lowercase()) {
                                            "great" -> R.drawable.ic_mini_great
                                            "happy" -> R.drawable.ic_mini_happy
                                            "okay"  -> R.drawable.ic_mini_okay
                                            "down"  -> R.drawable.ic_mini_down
                                            "sad"   -> R.drawable.ic_mini_sad
                                            else    -> R.drawable.ic_mini_okay
                                        }
                                    ),
                                    contentDescription = "기분",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            text = moodLabel,
                            color = White,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.height(2.dp))

                    Text(
                        "추천 마음 완화법 - “$recommendation”",
                        color = White.copy(alpha = 0.92f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = BrandFontFamily
                    )
                }
            }
        }
    }
}
