package com.hand.hand.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.hand.hand.R

@Composable
fun NavBar(
    onHomeClick: () -> Unit = {},
    onDiaryClick: () -> Unit = {},
    onDocumentClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    // 각 버튼 간 간격 개별 조절
    spacingHomeDiary: Float = 0.1f,      // 화면 너비 대비
    spacingDocumentProfile: Float = 0.1f,
    spacingBetweenGroups: Float = 0.1f,  // 좌우 그룹 사이 간격
    // 각 버튼 높이 개별 설정 (화면 높이 대비)
    homeHeightRatio: Float = 0.03f,
    diaryHeightRatio: Float = 0.03f,
    documentHeightRatio: Float = 0.03f,
    profileHeightRatio: Float = 0.03f,
    // 새로운 nav_care 아이콘 옵션
    careHeightRatio: Float = 0.1f,
    careWidthRatio: Float = 0.2f,
    careOffsetX: Float = 0.4f,   // 화면 가로 대비 위치 (0~1)
    careOffsetY: Float = -0.04f,   // 화면 세로 대비 위치 (0~1)
    onCareClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val navBarHeight = screenHeight * 0.12f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(navBarHeight)
    ) {
        // 배경 이미지
        Image(
            painter = painterResource(R.drawable.navbar_icon),
            contentDescription = "Navigation Bar Background",
            modifier = Modifier
                .fillMaxWidth()
                .height(navBarHeight),
            contentScale = ContentScale.FillWidth
        )

        // 버튼 그룹 Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = screenWidth * spacingBetweenGroups),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 첫 번째 가로 묶음 (Home, Diary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(screenWidth * spacingHomeDiary),
                verticalAlignment = Alignment.Bottom
            ) {
                Image(
                    painter = painterResource(R.drawable.nav_home),
                    contentDescription = "Home",
                    modifier = Modifier
                        .height(screenHeight * homeHeightRatio)
                        .aspectRatio(1f)
                        .clickable { onHomeClick() },
                    contentScale = ContentScale.FillHeight
                )
                Image(
                    painter = painterResource(R.drawable.nav_diary),
                    contentDescription = "Diary",
                    modifier = Modifier
                        .height(screenHeight * diaryHeightRatio)
                        .aspectRatio(1f)
                        .clickable { onDiaryClick() },
                    contentScale = ContentScale.FillHeight
                )
            }

            // 두 번째 가로 묶음 (Document, Profile)
            Row(
                horizontalArrangement = Arrangement.spacedBy(screenWidth * spacingDocumentProfile),
                verticalAlignment = Alignment.Bottom
            ) {
                Image(
                    painter = painterResource(R.drawable.nav_document),
                    contentDescription = "Document",
                    modifier = Modifier
                        .height(screenHeight * documentHeightRatio)
                        .aspectRatio(1f)
                        .clickable { onDocumentClick() },
                    contentScale = ContentScale.FillHeight
                )
                Image(
                    painter = painterResource(R.drawable.nav_profile),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .height(screenHeight * profileHeightRatio)
                        .aspectRatio(1f)
                        .clickable { onProfileClick() },
                    contentScale = ContentScale.FillHeight
                )
            }
        }

        // 새로운 nav_care 아이콘 (Row 밖, 자유 위치)
        Image(
            painter = painterResource(R.drawable.nav_care),
            contentDescription = "Care Icon",
            modifier = Modifier
                .size(width = screenWidth * careWidthRatio, height = screenHeight * careHeightRatio)
                .offset(
                    x = screenWidth * careOffsetX,
                    y = screenHeight * careOffsetY
                )
                .clickable { onCareClick() },
            contentScale = ContentScale.FillHeight
        )
    }
}
