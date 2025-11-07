@file:OptIn(ExperimentalMaterial3Api::class)
package com.hand.hand.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.ui.home.dialog.AdminLoginDialog
import com.hand.hand.ui.home.header.HomeGreetingHeader
import com.hand.hand.ui.home.sections.MyHealthInfoSection
import com.hand.hand.ui.home.sections.MyRecordsSection
import com.hand.hand.ui.mypage.PersonalScreen
import com.hand.hand.ui.theme.Brown10
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.hand.hand.ui.admin.AdminHomeActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.hand.hand.ui.model.moodFromScore
import com.hand.hand.ui.model.moodCaption

@Composable
fun HomeScreen() {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showPersonal by rememberSaveable { mutableStateOf(false) }

    // ===== 반응형 스케일러 (기존 유지) =====
    val cfg = LocalConfiguration.current
    val screenW = cfg.screenWidthDp
    val screenH = cfg.screenHeightDp
    val scale = (screenW / 360f).coerceIn(0.85f, 1.25f)
    fun sdp(v: Dp): Dp = (v.value * scale).dp
    fun ssp(v: Float) = (v * scale).sp

    // ===== 공용 좌우 여백을 '비율→Dp'로만 계산 (디자인 유지) =====
    // 360dp 기준 16dp가 되도록: 16 / 360 = 0.044444...
    val horizontalGutterRatio = 16f / 360f

    fun resolvedGutterDp(
        ratio: Float = horizontalGutterRatio,
        min: Dp = 12.dp,    // 안전 클램프 (태블릿/초소형 대비)
        max: Dp = 28.dp
    ): Dp {
        val wDp = cfg.screenWidthDp.dp
        return (wDp * ratio).coerceIn(min, max)
    }

    // ✅ 이제 여기만 비율 기반으로 바꿈 (sdp 사용 X)
    val gutter: Dp = resolvedGutterDp()

    val moodScore = 0
    val mood = moodFromScore(moodScore)

    val todayText = remember {
        SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date())
    }

    Scaffold(
        containerColor = Brown10,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            HomeGreetingHeader(
                dateText = todayText,
                onModeToggle = { showDialog = true },
                userName = "싸피님",
                isWritten = false,
                heartRateBpm = 75,
                moodLabel = mood.label,
                recommendation = "봉인 연습",
                modifier = Modifier
                    .fillMaxWidth(),
                // 헤더 쪽도 동일한 gutter Dp를 그대로 전달 (디자인 유지)
                horizontalGutter = gutter
            )
        },
        bottomBar = {
            CurvedBottomNavBar(
                selectedTab = if (showPersonal) BottomTab.Profile else BottomTab.Home,
                onClickHome    = { showPersonal = false },
                onClickWrite   = { /* TODO */ },
                onClickDiary   = { /* TODO */ },
                onClickProfile = { showPersonal = true },
                onClickCenter  = { /* TODO */ }
            )
        }
    ) { paddingValues ->
        if (showPersonal) {
            Box(Modifier.padding(paddingValues)) {
                PersonalScreen()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(sdp(24.dp)), // 기존 유지
                contentPadding = PaddingValues(top = sdp(16.dp), bottom = 0.dp) // 기존 유지
            ) {
                // 섹션들도 동일 gutter를 공유
                item { MyRecordsSection(horizontalPadding = gutter) }
                item { MyHealthInfoSection(horizontalPadding = gutter, stressScore = moodScore) }
                item { Spacer(Modifier.height(sdp(16.dp))) }
            }
        }
    }

    val context = LocalContext.current
    if (showDialog) {
        AdminLoginDialog(
            onClose = { showDialog = false },
            onEnterGroupCode = { /* TODO */ },
            onAdminLoginClick = { /* TODO */ },
            onOrgClick = { orgName ->
                val intent = Intent(context, AdminHomeActivity::class.java)
                intent.putExtra("org_name", orgName)
                context.startActivity(intent)
                showDialog = false
            }
        )
    }
}
