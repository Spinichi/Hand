package com.hand.hand.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.hand.ui.theme.Brown10
import com.hand.hand.ui.theme.Brown40
import com.hand.hand.ui.theme.Gray20

enum class BottomTab { Home, Write, Diary, Profile, None }

@Composable
fun CurvedBottomNavBar(
    modifier: Modifier = Modifier,
    selectedTab: BottomTab = BottomTab.None,
    barHeight: Dp = 68.dp,
    barRadius: Dp = 24.dp,
    centerButtonSize: Dp = 56.dp,
    // 이미지 아이콘을 쓰고 싶으면 webp를 res/drawable 에 두고 ID를 넘기면 됨. null이면 기본 벡터 아이콘 사용
    @DrawableRes homeIconRes: Int? = null,
    @DrawableRes writeIconRes: Int? = null,
    @DrawableRes diaryIconRes: Int? = null,
    @DrawableRes profileIconRes: Int? = null,
    @DrawableRes centerIconRes: Int? = null,

    onClickHome: () -> Unit = {},
    onClickWrite: () -> Unit = {},
    onClickDiary: () -> Unit = {},
    onClickProfile: () -> Unit = {},
    onClickCenter: () -> Unit = {},
) {
    val CenterGreen = Color(0xFF9BB167)


    // 컷아웃 미세조정 파라미터
    val cutoutRadiusScale = 0.64f   // 컷아웃 반지름 = 버튼지름 * scale (0.60~0.68 권장)
    val cutoutCenterYRate = -0.22f  // 컷아웃 원 중심의 Y (상단 기준) 음수일수록 더 깊게 파임
    val buttonOffsetK     = 0.255f  // 중앙 버튼이 바 위로 겹치는 정도

    // 하단 시스템 내비게이션 패딩
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brown10)
            .padding(start = 16.dp, end = 16.dp, bottom = bottomInset.coerceAtLeast(6.dp), top = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ==== 흰색 바 + U 컷아웃 ====
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(12.dp, RoundedCornerShape(barRadius), clip = false),
            color = Color.Transparent
        ) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val r = barRadius.toPx()

                // 1) 라운드 박스
                val barRound = RoundRect(
                    left = 0f,
                    top = 0f,
                    right = w,
                    bottom = h,
                    cornerRadius = CornerRadius(r, r)
                )

                // 2) 컷아웃 원
                val cutR = centerButtonSize.toPx() * cutoutRadiusScale
                val cx   = w / 2f
                val cy   = cutR * cutoutCenterYRate  // 상단보다 살짝 위에 중심 → 아래로 둥글게 파임
                val cutOval = Rect(cx - cutR, cy - cutR, cx + cutR, cy + cutR)

                // 3) EvenOdd: 바에서 원 영역을 빼서 U 컷 생성
                val path = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRoundRect(barRound)
                    addOval(cutOval)
                }
                drawPath(path = path, color = Color.White, style = Fill)
            }
        }

        // ==== 중앙 버튼 ====
        Box(
            modifier = Modifier
                .offset(y = (-barHeight / 2) - (centerButtonSize * buttonOffsetK))
                .size(centerButtonSize)
                .shadow(6.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(CenterGreen)
                .clickable { onClickCenter() },
            contentAlignment = Alignment.Center
        ) {
            if (centerIconRes != null) {
                Icon(
                    painter = painterResource(centerIconRes),
                    contentDescription = "center",
                    tint = Color.White
                )
            } else {
                Icon(Icons.Filled.Favorite, contentDescription = "center", tint = Color.White)
            }
        }

        // ==== 좌/우 아이콘 버블 ====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                NavIconBubble(
                    iconVector = Icons.Filled.Home,
                    iconRes = homeIconRes,
                    selected = selectedTab == BottomTab.Home,
                    onClick = onClickHome
                )
                NavIconBubble(
                    iconVector = Icons.Filled.Edit,
                    iconRes = writeIconRes,
                    selected = selectedTab == BottomTab.Write,
                    onClick = onClickWrite
                )
            }
            Spacer(Modifier.width(centerButtonSize + 20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                NavIconBubble(
                    iconVector = Icons.Filled.Description,
                    iconRes = diaryIconRes,
                    selected = selectedTab == BottomTab.Diary,
                    onClick = onClickDiary
                )
                NavIconBubble(
                    iconVector = Icons.Filled.Person,
                    iconRes = profileIconRes,
                    selected = selectedTab == BottomTab.Profile,
                    onClick = onClickProfile
                )
            }
        }
    }
}

@Composable
private fun NavIconBubble(
    iconVector: ImageVector,
    @DrawableRes iconRes: Int? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bubbleBg = Color(0xFFF8F6F3)
    val tint = if (selected) Brown40 else Gray20

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bubbleBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint
            )
        } else {
            Icon(imageVector = iconVector, contentDescription = null, tint = tint)
        }
    }
}
