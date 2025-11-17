// file: com/hand/hand/ui/admin/header/AdminGreetingHeader.kt
package com.hand.hand.ui.admin.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.hand.hand.R
import com.hand.hand.ui.home.components.StatusPill
import com.hand.hand.ui.theme.*

// 0~100 점수 → 5단계 라벨 매핑 (디자인/아이콘 변경 없음)
private fun scoreToLabel(score: Float): String {
    val s = score.coerceIn(0f, 100f)
    return when {
        s >= 80f -> "great" // 1단계 = 80~100
        s >= 60f -> "happy" // 2단계 = 60~79
        s >= 40f -> "okay"  // 3단계 = 40~59
        s >= 20f -> "down"  // 4단계 = 20~39
        else     -> "sad"   // 5단계 = 0~19
    }
}

@Composable
fun AdminGreetingHeader(
    dateText: String,
    onModeToggle: () -> Unit,
    userName: String,

    // 관리자 전용 표기값
    registeredCount: Int,
    sadCount: Int,

    // 기존 호환: moodLabel은 유지
    // 새 옵션: avgScore100(0~100)을 주면 이 값으로 라벨/아이콘 자동 계산
    moodLabel: String,
    avgScore100: Float? = null,

    // 추천 마음 완화법 (비어도 줄 높이 유지)
    recommendation: String,

    // 검색창 (UI)
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,

    modifier: Modifier = Modifier,

    // 홈과 동일한 거터 규칙 사용
    horizontalGutterRatio: Float = 0.07f,

    // 세로 비율 고정 없음. 내용만큼.
    topPadding: Dp = 16.dp,
    bottomCornerRadius: Dp = 50.dp,
    backgroundColor: Color = Brown80,

    // 상위에서 Dp로 직접 넘기면 우선
    horizontalGutter: Dp? = null,

    // ▼ 검색창 설정
    searchBarHeight: Dp = 56.dp,
    searchBarCorner: Dp = 28.dp,
    searchBarElevation: Dp = 8.dp,
    searchBarHorizontalPadding: Dp = 18.dp,
    searchTextSizeSp: Int = 16,
    searchIconSizeDp: Dp = 22.dp,
) {
    val conf = LocalConfiguration.current
    val screenW = conf.screenWidthDp.dp

    // 상태바 높이
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // 좌우 여백(Dp) 계산
    val resolvedHorizontalGutter = horizontalGutter ?: (screenW * horizontalGutterRatio)

    // 점수 우선, 없으면 기존 라벨 사용
    val resolvedMoodLabel = scoreToLabel(avgScore100 ?: 0f).lowercase()

    // 외곽
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(
                        topStart = 0.dp, topEnd = 0.dp,
                        bottomStart = bottomCornerRadius, bottomEnd = bottomCornerRadius
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusTop)
                .padding(
                    start = resolvedHorizontalGutter,
                    end = resolvedHorizontalGutter,
                    top = topPadding,
                    bottom = 16.dp
                )
        ) {
            // 날짜 + 모드
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
                    Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
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

            // 아바타 + 인사 + 상태필
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
                        "반가워요! $userName!",
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
                                    painter = painterResource(R.drawable.ic_meta_human_green),
                                    contentDescription = "등록 인원",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            text = "${registeredCount} 명",
                            color = White,
                            fontSize = 18.sp
                        )

                        Spacer(Modifier.width(10.dp))

                        StatusPill(
                            leading = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_mini_warning),
                                    contentDescription = "주의",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            text = "${sadCount} 명",
                            color = White,
                            fontSize = 18.sp
                        )

                        Spacer(Modifier.width(10.dp))

                        StatusPill(
                            leading = {
                                Icon(
                                    painter = painterResource(
                                        when (resolvedMoodLabel) {
                                            "great" -> R.drawable.ic_mini_great
                                            "happy" -> R.drawable.ic_mini_happy
                                            "okay"  -> R.drawable.ic_mini_okay
                                            "down"  -> R.drawable.ic_mini_down
                                            "sad"   -> R.drawable.ic_mini_sad
                                            else    -> R.drawable.ic_mini_okay
                                        }
                                    ),
                                    contentDescription = "그룹 평균 점수 무드",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            text = resolvedMoodLabel,
                            color = White,
                            fontSize = 18.sp
                        )
                    }

                    // 추천 마음 완화법: 비어도 공간 유지
                    Spacer(Modifier.height(2.dp))
                    val recoText =
                        if (recommendation.isBlank()) " " else "추천 마음 완화법 - “$recommendation”"
                    Text(
                        text = recoText,
                        color = White.copy(alpha = 0.92f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = BrandFontFamily,
                        minLines = 1
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 검색창 (헤더 내부)
            AdminSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                onSubmit = { onSearch(searchQuery) },
                height = searchBarHeight,
                corner = searchBarCorner,
                elevation = searchBarElevation,
                horizontalPadding = searchBarHorizontalPadding,
                textSizeSp = searchTextSizeSp,
                iconSizeDp = searchIconSizeDp
            )
        }
    }
}

@Composable
private fun AdminSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    height: Dp,
    corner: Dp,
    elevation: Dp,
    horizontalPadding: Dp,
    textSizeSp: Int,
    iconSizeDp: Dp
) {
    Surface(
        color = Color.White,
        contentColor = Brown80,
        shape = RoundedCornerShape(corner),
        shadowElevation = elevation,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Brown80,
                    fontSize = textSizeSp.sp,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                modifier = Modifier.weight(1f)
            ) { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "그룹원 이름을 입력하세요",
                            color = Gray40,
                            fontSize = textSizeSp.sp,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    inner()
                }
            }

            Spacer(Modifier.width(12.dp))

            IconButton(
                onClick = { if (value.isNotBlank()) onSubmit() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mini_search),
                    contentDescription = "검색 실행",
                    tint = Color(0xFFB9B2AC),
                    modifier = Modifier.size(iconSizeDp)
                )
            }
        }
    }
}
