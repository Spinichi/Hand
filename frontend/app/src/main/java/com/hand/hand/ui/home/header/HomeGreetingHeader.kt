package com.hand.hand.ui.home.header

import androidx.compose.foundation.Image
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
    // CareHeader와 매칭되는 비율값들
    headerHeightRatio: Float = 0.25f,
    horizontalGutterRatio: Float = 0.07f,
    topPaddingRatio: Float = 0.05f,
    bottomCornerRadius: Dp = 50.dp,
    // 상위(HomeScreen)에서 Dp로 직접 넘기면 그 값을 우선 사용
    horizontalGutter: Dp? = null,
) {
    val conf = LocalConfiguration.current
    val screenH = conf.screenHeightDp.dp
    val screenW = conf.screenWidthDp.dp

    val headerHeight = screenH * headerHeightRatio
    val topPadding = screenH * topPaddingRatio
    // 🔧 핵심: 상위에서 준 Dp가 있으면 그걸 쓰고, 없으면 비율로 계산
    val resolvedHorizontalGutter = horizontalGutter ?: (screenW * horizontalGutterRatio)

    Surface(
        color = Brown80,
        contentColor = White,
        shape = RoundedCornerShape(
            topStart = 0.dp, topEnd = 0.dp,
            bottomStart = bottomCornerRadius, bottomEnd = bottomCornerRadius
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(
                    start = resolvedHorizontalGutter,
                    end = resolvedHorizontalGutter,
                    top = topPadding,
                    bottom = 16.dp
                )
        ) {
            // ... 이하 동일 ...
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
                        StatusPill(
                            leading = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_edit),
                                    contentDescription = "작성 상태",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            text = if (isWritten) "작성 완료" else "작성 전",
                            color = White,
                            fontSize = 18.sp,
                        )

                        Spacer(Modifier.width(10.dp))

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
