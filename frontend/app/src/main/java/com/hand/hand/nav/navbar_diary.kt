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
fun NavBarDiary(
    onHomeClick: () -> Unit = {},
    onDiaryClick: () -> Unit = {},
    onDocumentClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    spacingHomeDiary: Float = 0.1f,
    spacingDocumentProfile: Float = 0.1f,
    spacingBetweenGroups: Float = 0.1f,
    homeHeightRatio: Float = 0.03f,
    diaryHeightRatio: Float = 0.03f,
    documentHeightRatio: Float = 0.03f,
    profileHeightRatio: Float = 0.03f,
    careHeightRatio: Float = 0.1f,
    careWidthRatio: Float = 0.2f,
    careOffsetX: Float = 0.4f,
    careOffsetY: Float = -0.04f,
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
        // 🔹 배경 이미지
        Image(
            painter = painterResource(R.drawable.navbar_icon),
            contentDescription = "Navigation Bar Background",
            modifier = Modifier
                .fillMaxWidth()
                .height(navBarHeight),
            contentScale = ContentScale.FillWidth
        )

        // 🔹 버튼 그룹
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = screenWidth * spacingBetweenGroups),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔸 왼쪽 그룹 (Home, Diary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(screenWidth * spacingHomeDiary),
                verticalAlignment = Alignment.Bottom
            ) {
                // Home 아이콘
                Image(
                    painter = painterResource(R.drawable.nav_home),
                    contentDescription = "Home",
                    modifier = Modifier
                        .height(screenHeight * homeHeightRatio)
                        .aspectRatio(1f)
                        .clickable { onHomeClick() },
                    contentScale = ContentScale.FillHeight
                )

                // Diary 아이콘 교체 (중앙점 맞춤 보정)
                Image(
                    painter = painterResource(R.drawable.diary_page_icon),
                    contentDescription = "Diary",
                    modifier = Modifier
                        .height(screenHeight * diaryHeightRatio * 2f) // 살짝 크기 보정
                        .aspectRatio(1f)
                        .padding(top = screenHeight * 0.002f) // 중앙점 위아래 보정
                        .clickable { onDiaryClick() },
                    contentScale = ContentScale.FillHeight
                )
            }

            // 🔸 오른쪽 그룹 (Document, Profile)
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

        // 🔹 중앙 Care 아이콘
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
