// file: com/hand/hand/ui/admin/AdminHomeActivity.kt
package com.hand.hand.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// API 관련 클래스 import
import com.hand.hand.api.Group.GroupData
import com.hand.hand.api.Group.GroupManager
import com.hand.hand.api.Group.GroupMemberData
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
// OrgSource는 더 이상 사용하지 않음
// import com.hand.hand.ui.model.OrgSource
import com.hand.hand.ui.model.moodFromScore
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.theme.Brown10
import com.hand.hand.ui.theme.Brown80
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- API 응답을 UI 모델로 변환하는 헬퍼 함수 ---

/** 0~100점수(RiskScore)를 5단계 Mood enum으로 변환 */
private fun scoreToMood(score: Int): Mood {
    val s = score.toFloat().coerceIn(0f, 100f)
    return when {
        s >= 80f -> Mood.GREAT // 80~100
        s >= 60f -> Mood.HAPPY // 60~79
        s >= 40f -> Mood.OKAY  // 40~59
        s >= 20f -> Mood.DOWN  // 20~39
        else     -> Mood.SAD   // 0~19
    }
}

/** GroupData(API)를 Organization(UI) 모델로 변환 */
private fun GroupData.toOrganization(): Organization? {
    if (this.id == null || this.name == null) return null
    return Organization(
        id = this.id.toString(),
        name = this.name,
        memberCount = this.memberCount ?: 0,
        averageScore = this.avgMemberRiskScore?.toFloat()?.coerceIn(0f, 100f) ?: 50f // 그룹 평균
    )
}

/** GroupMemberData(API)를 GroupMember(UI) 모델로 변환 */
private fun GroupMemberData.toGroupMember(): GroupMember {
    val riskScore = this.weeklyAvgRiskScore?.toFloat()?.coerceIn(0f, 100f) ?: 50f // 멤버
    return GroupMember(
        id = this.userId.toString(),
        name = this.name,
        // ✅ 수정: weeklyAvgRiskScore 값을 avgScore 필드에 직접 할당
        avgScore = riskScore.toInt()
        // note = this.specialNotes (필요하다면 매핑)
    )
}

/** GroupData에서 초대 코드를 가져오는 헬퍼 함수 */
private fun GroupData.getGroupCode(): String = this.inviteCode ?: "######"


class AdminHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Home(또는 다른 진입)에서 넘긴 조직 식별자
        val initialOrgIdFromIntent = intent.getStringExtra("org_id").orEmpty()

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                AdminHomeScreen(initialOrgId = initialOrgIdFromIntent)
            }
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

    // --- API 데이터 상태 ---
    var organizations by remember { mutableStateOf<List<Organization>>(emptyList()) }
    var members by remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    var currentOrg by rememberSaveable { mutableStateOf<Organization?>(null) } // 현재 선택된 조직 객체
    var isLoading by rememberSaveable { mutableStateOf(true) } // 로딩 상태

    // 현재 선택된 조직 (다이얼로그에서 변경)
    val fallbackOrgId = remember(organizations) { organizations.firstOrNull()?.id.orEmpty() }
    var currentOrgId by rememberSaveable { mutableStateOf(if (initialOrgId.isNotBlank()) initialOrgId else fallbackOrgId) }

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

    // --- API 데이터 로드 ---

    // 1. 조직 목록 로드 (앱 실행 시 1회)
    LaunchedEffect(Unit) {
        isLoading = true
        GroupManager.getGroups(
            onSuccess = { groupDatas ->
                organizations = groupDatas?.mapNotNull { it.toOrganization() } ?: emptyList()
                // Intent로 ID를 받지 않았고, fallbackOrgId가 비어있었다면, API 로드 후 첫 번째 조직으로 ID 설정
                if (initialOrgId.isBlank() && currentOrgId.isBlank() && organizations.isNotEmpty()) {
                    currentOrgId = organizations.first().id
                }
                // 만약 currentOrgId가 여전히 비어있다면 (그룹이 0개), 로딩 종료
                if (currentOrgId.isBlank()) {
                    isLoading = false
                }
            },
            onError = { error ->
                Log.e("AdminHome", "Failed to load groups: $error")
                isLoading = false // 에러 발생 시 로딩 종료
            }
        )
    }

    // 2. 현재 조직 정보 및 멤버 목록 로드 (currentOrgId 변경 시)
    LaunchedEffect(currentOrgId, organizations) { // organizations가 갱신될 때도 currentOrg 객체 갱신
        val orgIdInt = currentOrgId.toIntOrNull()
        if (orgIdInt != null) {
            isLoading = true
            // 현재 Organization 객체 갱신
            currentOrg = organizations.firstOrNull { it.id == currentOrgId }

            // 그룹 정보 로드 (초대 코드용)
            GroupManager.getGroupInfo(
                groupId = orgIdInt,
                onSuccess = { groupData ->
                    groupCode = groupData?.getGroupCode() ?: "######"
                },
                onError = { error ->
                    Log.e("AdminHome", "Failed to load group info: $error")
                }
            )

            // 그룹 멤버 로드
            GroupManager.getGroupMembers(
                groupId = orgIdInt,
                onSuccess = { memberDatas ->
                    members = memberDatas
                        ?.filter { it.role == "MEMBER" }
                        ?.map { it.toGroupMember() }
                        ?: emptyList()
                    isLoading = false // 멤버 로드 완료 시 로딩 종료
                },
                onError = { error ->
                    Log.e("AdminHome", "Failed to load members: $error")
                    members = emptyList() // 에러 시 빈 목록
                    isLoading = false
                }
            )
        } else {
            // 유효한 ID가 없음
            if (organizations.isNotEmpty()) { // 조직 목록은 있는데 ID가 잘못된 경우
                Log.w("AdminHome", "Invalid currentOrgId: $currentOrgId")
            }
            isLoading = false
        }
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


    // ── 단일 소스(API 상태)에서 조직/멤버 로드 ──
    val org = currentOrg ?: Organization(id = "", name = "로딩 중...", memberCount = 0, averageScore = 50f)
    // val members = remember(org.id) { OrgSource.members(org.id) } // API 상태 'members'가 대체

    // 파생 값 (API 상태 기반)
    val registeredCount = remember(members) { members.size }
    val sadCount = remember(members) {
        members.count {
            val mood = scoreToMood(it.avgScore)
            mood == Mood.SAD || mood == Mood.DOWN
        }
    } // SAD/DOWN을 주의 인원으로 집계
    val avgScore100 = remember(org) { org.averageScore.coerceIn(0f, 100f) }

    // ── 검색/태그 필터 파이프라인 (API 상태 'members' 기반) ──
    val searchResults = remember(query, members) {
        if (query.isBlank()) emptyList()
        else members.filter { it.name.contains(query, ignoreCase = true) }
    }
    val moodFilteredMembers = remember(selectedMood, members) {
        if (selectedMood == null) members else members.filter { scoreToMood(it.avgScore) == selectedMood }
    }

    if (showHome) {
        HomeScreen()
        return
    }

    val context = LocalContext.current

    // --- 로딩 인디케이터 ---
    // (디자인 변경 최소화를 위해, 첫 로딩 시에만 전체 화면 인디케이터 표시)
    if (isLoading && organizations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Brown80)
        }
        return
    }

    Scaffold(
        containerColor = Brown10,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AdminGreetingHeader(
                dateText = todayText,
                onModeToggle = { showAdminLogin = true },
                userName = org.name, // API 데이터
                registeredCount = registeredCount, // API 데이터
                sadCount = sadCount, // API 데이터
                moodLabel = moodFromScore(avgScore100.toInt()).label, // API 데이터
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
                        text = groupCode, // API 데이터
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
                                putExtra(MemberDetailActivity.EXTRA_ORG_ID, org.id)
                                putExtra(MemberDetailActivity.EXTRA_MEMBER_ID, member.id)
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
                    sadCount = sadCount, // API 데이터
                    downCount = members.count { scoreToMood(it.avgScore) == Mood.DOWN },
                    happyCount = members.count { scoreToMood(it.avgScore) == Mood.HAPPY },
                    okayCount  = members.count { scoreToMood(it.avgScore) == Mood.OKAY  },
                    greatCount = members.count { scoreToMood(it.avgScore) == Mood.GREAT }, // API 데이터
                    avgChangeCount = 2, // TODO: 이 값은 API에서 받아와야 할 수 있음
                    recentChangeName = members.firstOrNull()?.name ?: "" // API 데이터
                )
            }

            item {
                AdminMembersSection(
                    horizontalPadding = gutter,
                    members = moodFilteredMembers, // API 데이터
                    searchQuery = "",
                    selectedMood = selectedMood,
                    onSelectMood = { mood ->
                        selectedMood = if (selectedMood == mood) null else mood
                    },
                    onMemberClick = { member ->
                        val cal = java.util.Calendar.getInstance()
                        val year = cal.get(java.util.Calendar.YEAR)
                        val month = cal.get(java.util.Calendar.MONTH) + 1
                        val week = cal.get(java.util.Calendar.WEEK_OF_MONTH)

                        context.startActivity(
                            Intent(context, com.hand.hand.AiDocument.TeamAiDocumentActivity::class.java).apply {
                                putExtra("YEAR", year)
                                putExtra("MONTH", month)
                                putExtra("WEEK", week)
                                putExtra("ORG_ID", org.id)
                                putExtra("MEMBER_ID", member.id)
                                putExtra("MEMBER_NAME", member.name)
                            }
                        )
                    },
                    org = org // API 데이터
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
                if (newOrgId.isNotBlank()) {
                    currentOrgId = newOrgId  // 즉시 전환 (LaunchedEffect 트리거)
                    // 필터 초기화
                    selectedMood = null
                    query = ""
                }
                showAdminLogin = false
            },
            onPersonalLoginClick = {
                showAdminLogin = false
                showHome = true
            },
            organizations = organizations, // API 데이터
            registeredCount = registeredCount, // API 데이터
            sadCount = sadCount // API 데이터
        )
    }
}