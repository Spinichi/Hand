// file: com/hand/hand/ui/admin/AdminHomeActivity.kt
package com.hand.hand.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.ui.admin.dialog.AdminLoginDialog
import com.hand.hand.ui.admin.header.AdminGreetingHeader
import com.hand.hand.ui.admin.member.MemberDetailActivity
import com.hand.hand.ui.admin.sections.AdminGroupRecordsSection
import com.hand.hand.ui.admin.sections.AdminMembersSection
import com.hand.hand.ui.admin.sections.GroupMember
import com.hand.hand.ui.admin.sections.Mood
import com.hand.hand.ui.admin.sections.MemberCard
import com.hand.hand.ui.home.HomeScreen
import com.hand.hand.ui.model.Organization
import com.hand.hand.ui.model.OrgSource
import com.hand.hand.ui.model.moodFromScore
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.theme.Brown10
import com.hand.hand.ui.theme.Brown80
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Home(또는 다른 진입)에서 넘긴 조직 식별자
        val initialOrgIdFromIntent = intent.getStringExtra("org_id").orEmpty()

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                AdminHomeScreen(initialOrgId = initialOrgIdFromIntent)
            }
//            setContent {
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    AdminHomeScreen(initialOrgId = initialOrgId)
//                }
//            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminHomeScreen(
    initialOrgId: String
) {
    var showAdminLogin by rememberSaveable { mutableStateOf(false) }
    var showHome by rememberSaveable { mutableStateOf(false) }

    // 현재 선택된 조직 (다이얼로그에서 변경)
    val allOrgs: List<Organization> = remember { OrgSource.organizations() }
    val fallbackOrgId = remember(allOrgs) { allOrgs.firstOrNull()?.id.orEmpty() }
    var currentOrgId by rememberSaveable { mutableStateOf(if (initialOrgId.isNotBlank()) initialOrgId else fallbackOrgId) }
    // ✅ 현재 선택된 조직 (다이얼로그에서 변경)
//    var currentOrgId by rememberSaveable { mutableStateOf(initialOrgId) }
//    var currentOrgId by rememberSaveable { mutableStateOf(initialOrgId) }

    // 태그(무드) 선택 상태 — 동일 태그 재탭 시 해제
    var selectedMood by rememberSaveable { mutableStateOf<Mood?>(null) }

    // 헤더 검색어
    var query by rememberSaveable { mutableStateOf("") }

    // 그룹 코드(디자인 표기용)
    var groupCode by rememberSaveable { mutableStateOf("######") }

    // 날짜
    val todayText = remember {
        SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date())
    }

    // ===== Home과 동일한 gutter 계산(비율 + 클램프) =====
    val cfg = LocalConfiguration.current
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

    // ── 단일 소스에서 조직/멤버 로드 ──
    val organizations: List<Organization> = remember { OrgSource.organizations() }
    val org = remember(currentOrgId, organizations) {
        organizations.firstOrNull { it.id == currentOrgId } ?: organizations.first()
    }
    val members = remember(org.id) { OrgSource.members(org.id) }

    // 파생 값
    val registeredCount = remember(members) { members.size }
    val sadCount = remember(members) { OrgSource.sadCount(org.id) }
    val avgScore100 = remember(org) { org.averageScore.coerceIn(0f, 100f) }

    // ── 검색/태그 필터 파이프라인 ──
    // 1) 검색(우선)
    val searchResults = remember(query, members) {
        if (query.isBlank()) emptyList()
        else members.filter { it.name.contains(query, ignoreCase = true) }
    }
    // 2) 태그(검색 없을 때만 반영)
    val moodFilteredMembers = remember(selectedMood, members) {
        if (selectedMood == null) members else members.filter { it.mood == selectedMood }
    }

    if (showHome) {
        HomeScreen()
        return
    }

    val context = LocalContext.current

    Scaffold(
        containerColor = Brown10,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AdminGreetingHeader(
                dateText = todayText,
                onModeToggle = { showAdminLogin = true },          // 다이얼로그 열기
                userName = org.name,                               // 헤더에 조직명 표기
                registeredCount = registeredCount,
                sadCount = sadCount,
                moodLabel = moodFromScore(avgScore100.toInt()).label, // 평균 점수 → 라벨(이모지 연동)
                recommendation = "",
                searchQuery = query,
                onSearchQueryChange = { query = it },
                onSearch = { /* 필요 시 서버 검색 트리거 */ },
                horizontalGutter = gutter
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ───────── 그룹 코드 라인 (디자인 그대로) ─────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = gutter, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "그룹 코드 : ",
                        fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontFamily = BrandFontFamily,
                        color = Brown80
                    )
                    Text(
                        text = groupCode,
                        fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontFamily = BrandFontFamily,
                        color = Brown80
                    )
                }
            }

            // ───────── 검색 결과(있으면 최우선) ─────────
            if (searchResults.isNotEmpty()) {
                items(searchResults, key = { it.id }) { member ->
                    Box(modifier = Modifier.padding(horizontal = gutter)) {
                        MemberCard(member = member, onClick = {
                            val intent = Intent(context, MemberDetailActivity::class.java).apply {
                                putExtra(MemberDetailActivity.EXTRA_ORG_ID, org.id)          // ✅ 키 통일
                                putExtra(MemberDetailActivity.EXTRA_MEMBER_ID, member.id)     // ✅ 키 통일
                            }
                            context.startActivity(intent)
                        })
                    }
                    Spacer(Modifier.height(8.dp))
                }
                return@LazyColumn
            } else if (query.isNotBlank()) {
                item {
                    Text(
                        text = "검색 결과가 없어요",
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        fontFamily = BrandFontFamily,
                        color = Brown80.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = gutter)
                    )
                }
                return@LazyColumn
            }

            // ───────── 기본 섹션 (태그 필터 반영) ─────────
            item {
                AdminGroupRecordsSection(
                    horizontalPadding = gutter,
                    sadCount = sadCount,
                    downCount = members.count { it.mood == Mood.DOWN },
                    happyCount = members.count { it.mood == Mood.HAPPY },
                    okayCount  = members.count { it.mood == Mood.OKAY  },
                    greatCount = members.count { it.mood == Mood.GREAT },
                    avgChangeCount = 2,
                    recentChangeName = members.firstOrNull()?.name ?: ""
                )
            }

            item {
                AdminMembersSection(
                    horizontalPadding = gutter,
                    members = moodFilteredMembers,              // 태그 필터된 목록
                    searchQuery = "",                           // 검색은 헤더 전용
                    selectedMood = selectedMood,
                    onSelectMood = { mood ->
                        selectedMood = if (selectedMood == mood) null else mood
                    },
                    onMemberClick = { member ->
                        // 오늘 기준 최근 7일 계산
                        val cal = java.util.Calendar.getInstance()
                        val year = cal.get(java.util.Calendar.YEAR)
                        val month = cal.get(java.util.Calendar.MONTH) + 1
                        val week = cal.get(java.util.Calendar.WEEK_OF_MONTH)

                        val intent = Intent(context, com.hand.hand.AiDocument.TeamAiDocumentActivity::class.java).apply {
                            putExtra("YEAR", year)
                            putExtra("MONTH", month)
                            putExtra("WEEK", week)
                        }
                        context.startActivity(intent)
                    }

                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // ── 다이얼로그: 조직 선택 시 현재 org 변경 (디자인 그대로) ──
    if (showAdminLogin) {
        AdminLoginDialog(
            onClose = { showAdminLogin = false },
            onEnterGroupCode = { /* TODO */ showAdminLogin = false },
            onAdminLoginClick = { /* TODO */ showAdminLogin = false },
            onOrgClick = { newOrgId ->
                if (newOrgId.isNotBlank()) currentOrgId = newOrgId  // 즉시 전환
                showAdminLogin = false
            },
            onPersonalLoginClick = {
                showAdminLogin = false
                showHome = true
            },
            organizations = organizations,
            registeredCount = registeredCount,
            sadCount = sadCount
        )
    }
}
