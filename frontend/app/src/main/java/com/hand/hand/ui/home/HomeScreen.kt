@file:OptIn(ExperimentalMaterial3Api::class)

package com.hand.hand.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.ui.admin.AdminHomeActivity
import com.hand.hand.ui.home.dialog.HomeLoginDialog
import com.hand.hand.ui.home.header.HomeGreetingHeader
import com.hand.hand.ui.home.sections.MyHealthInfoSection
import com.hand.hand.ui.home.sections.MyRecordsSection
import com.hand.hand.ui.theme.Brown10
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.hand.hand.ui.model.Organization
import com.hand.hand.ui.model.OrgSource
import com.hand.hand.ui.model.moodFromScore
import com.hand.hand.ui.home.stats.MoodChangeActivity
import com.hand.hand.ui.personal.PersonalCareActivity
import com.hand.hand.care.CareActivity
import com.hand.hand.ui.home.HomeActivity          // ✅ 홈 이동용
import com.hand.hand.diary.DiaryHomeActivity       // ✅ 글쓰기 이동용
import com.hand.hand.AiDocument.PrivateAiDocumentHomeActivity  // ✅ 다이어리 이동용

@Composable
fun HomeScreen() {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    // 개인용 헤더 데이터
    val userName = "싸피님"
    val isWritten = false
    val heartRateBpm = 75
    val personalMoodScore = 79
    val mood = moodFromScore(personalMoodScore)
    val recommendation = "봉인 연습"
    val moodChangeCount = 7 // TODO: 나중에 실제 값으로 교체

    // 조직 리스트(관리자 다이얼로그용)
    val organizations: List<Organization> = remember { OrgSource.organizations() }

    // 반응형 스케일러
    val cfg = LocalConfiguration.current
    val screenW = cfg.screenWidthDp
    val scale = (screenW / 360f).coerceIn(0.85f, 1.25f)
    fun sdp(v: Dp): Dp = (v.value * scale).dp
    fun ssp(v: Float) = (v * scale).sp
    val horizontalGutterRatio = 16f / 360f
    fun resolvedGutterDp(
        ratio: Float = horizontalGutterRatio,
        min: Dp = 12.dp,
        max: Dp = 28.dp
    ): Dp {
        val wDp = cfg.screenWidthDp.dp
        return (wDp * ratio).coerceIn(min, max)
    }
    val gutter: Dp = resolvedGutterDp()

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
                userName = userName,
                isWritten = isWritten,
                heartRateBpm = heartRateBpm,
                moodLabel = mood.label,
                recommendation = recommendation,
                modifier = Modifier.fillMaxWidth(),
                horizontalGutter = gutter
            )
        },
        // ✅ 커브드 네비게이션 바
        bottomBar = {
            CurvedBottomNavBar(
                selectedTab = BottomTab.Home,
                onClickHome = {
                    // ✅ 홈 화면으로 이동
                    context.startActivity(Intent(context, HomeActivity::class.java))
                },
                onClickWrite = {
                    // ✅ 글쓰기 (DiaryHomeActivity)
                    context.startActivity(Intent(context, DiaryHomeActivity::class.java))
                },
                onClickDiary = {
                    // ✅ 다이어리 (PrivateAiDocumentHomeActivity)
                    context.startActivity(Intent(context, PrivateAiDocumentHomeActivity::class.java))
                },
                onClickProfile = {
                    // ✅ 추후 프로필 페이지 추가 가능
                },
                onClickCenter = {
                    // ✅ 중앙 버튼 → CareActivity
                    context.startActivity(Intent(context, CareActivity::class.java))
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(sdp(24.dp)),
            contentPadding = PaddingValues(top = sdp(16.dp), bottom = 0.dp)
        ) {
            item {
                MyRecordsSection(
                    horizontalPadding = gutter,
                    moodChangeCount = moodChangeCount,
                    onMoodChangeClick = {
                        context.startActivity(MoodChangeActivity.intent(context, moodChangeCount))
                    }
                )
            }
            item {
                MyHealthInfoSection(
                    horizontalPadding = gutter,
                    stressScore = personalMoodScore
                )
            }
            item { Spacer(Modifier.height(sdp(16.dp))) }
        }
    }

    // ✅ 관리자/조직 진입 다이얼로그
    if (showDialog) {
        HomeLoginDialog(
            onClose = { showDialog = false },
            onEnterGroupCode = { /* TODO */ },
            onAdminLoginClick = { /* TODO */ },
            onOrgClick = { orgId ->
                val intent = Intent(context, AdminHomeActivity::class.java)
                intent.putExtra("org_id", orgId)
                context.startActivity(intent)
                showDialog = false
            },
            organizations = organizations
        )
    }
}
