// file: com/hand/hand/ui/admin/AdminHomeActivity.kt
package com.hand.hand.ui.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.hand.hand.api.Group.GroupData
import com.hand.hand.api.Group.GroupManager
import com.hand.hand.api.Group.GroupMemberData
import com.hand.hand.ui.admin.dialog.AdminLoginDialog
import com.hand.hand.ui.admin.header.AdminGreetingHeader
import com.hand.hand.ui.admin.sections.*
import com.hand.hand.ui.home.HomeScreen
import com.hand.hand.ui.model.Organization
import com.hand.hand.ui.model.moodFromScore
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.theme.Brown10
import com.hand.hand.ui.theme.Brown80
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.hand.hand.ui.model.toOrgMoodUi

private fun scoreToMood(score: Int): Mood {
    val s = score.toFloat().coerceIn(0f, 100f)
    return when {
        s >= 80f -> Mood.GREAT
        s >= 60f -> Mood.HAPPY
        s >= 40f -> Mood.OKAY
        s >= 20f -> Mood.DOWN
        else -> Mood.SAD
    }
}

private fun GroupData.toOrganization(): Organization? {
    if (this.id == null || this.name == null) return null

    val rawCount = this.memberCount ?: 0
    val memberOnlyCount = maxOf(0, rawCount - 1)
    val avgScoreSafe: Float = this.avgMemberRiskScore?.toFloat()?.coerceIn(0f, 100f) ?: 0f
    return Organization(
        id = this.id.toString(),
        name = this.name,
        memberCount = memberOnlyCount,
        averageScore = avgScoreSafe
    )
}

private fun GroupMemberData.toGroupMember(): GroupMember {
    val riskScore = this.weeklyAvgRiskScore?.toFloat()?.coerceIn(0f, 100f) ?: 0f
    return GroupMember(
        id = this.userId.toString(),
        name = this.name,
        avgScore = riskScore.toInt(),
        note = this.specialNotes
    )
}

private fun GroupData.getGroupCode(): String = this.inviteCode ?: "######"

class AdminHomeActivity : ComponentActivity() {
    private var currentOrgId by mutableStateOf("")
    private var members by mutableStateOf<List<GroupMember>>(emptyList())

    private val updateMemberLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val updatedMemberId = data.getIntExtra("UPDATED_MEMBER_ID", -1)
            val updatedSpecialNotes = data.getStringExtra("UPDATED_SPECIAL_NOTES") ?: ""
            members = members.map {
                if (it.id.toIntOrNull() == updatedMemberId) it.copy(note = updatedSpecialNotes)
                else it
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialOrgIdFromIntent = intent.getStringExtra("org_id").orEmpty()

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                AdminHomeScreen(
                    initialOrgId = initialOrgIdFromIntent,
                    membersState = members,
                    currentOrgIdState = currentOrgId,
                    onMemberClick = { member ->
                        val intent = Intent(this, com.hand.hand.AiDocument.TeamAiDocumentActivity::class.java).apply {
                            putExtra("ORG_ID", currentOrgId.toIntOrNull() ?: -1)
                            putExtra("MEMBER_ID", member.id.toIntOrNull() ?: -1)
                            putExtra("MEMBER_NAME", member.name)
                            putExtra("MEMBER_SPECIAL_NOTES", member.note ?: "")
                        }
                        updateMemberLauncher.launch(intent)
                    },
                    onMembersUpdate = { updatedMembers -> members = updatedMembers },
                    onCurrentOrgIdUpdate = { newOrgId -> currentOrgId = newOrgId }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminHomeScreen(
    initialOrgId: String,
    membersState: List<GroupMember>,
    currentOrgIdState: String,
    onMemberClick: (GroupMember) -> Unit,
    onMembersUpdate: (List<GroupMember>) -> Unit,
    onCurrentOrgIdUpdate: (String) -> Unit
) {
    var showAdminLogin by rememberSaveable { mutableStateOf(false) }
    var showHome by rememberSaveable { mutableStateOf(false) }

    var organizations by remember { mutableStateOf<List<Organization>>(emptyList()) }
    val members = membersState // 외부 상태를 직접 사용
    var currentOrg by rememberSaveable { mutableStateOf<Organization?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var currentOrgId by rememberSaveable { mutableStateOf(currentOrgIdState) }
    var selectedMood by rememberSaveable { mutableStateOf<Mood?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var groupCode by rememberSaveable { mutableStateOf("######") }
    val todayText = remember { SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date()) }
    val context = LocalContext.current

    // Load organizations first
    LaunchedEffect(Unit) {
        isLoading = true
        GroupManager.getGroups(
            onSuccess = { groupDatas ->
                organizations = groupDatas?.mapNotNull { it.toOrganization() } ?: emptyList()
                if (initialOrgId.isNotBlank()) currentOrgId = initialOrgId
                else if (currentOrgId.isBlank() && organizations.isNotEmpty()) currentOrgId = organizations.first().id
            },
            onError = { error -> Log.e("AdminHome", "Failed to load groups: $error"); isLoading = false }
        )
    }

    // Load members whenever currentOrgId changes
    LaunchedEffect(currentOrgId, organizations) {
        val orgIdInt = currentOrgId.toIntOrNull()
        if (orgIdInt != null && organizations.isNotEmpty()) {
            isLoading = true
            currentOrg = organizations.firstOrNull { it.id == currentOrgId }

            GroupManager.getGroupInfo(
                groupId = orgIdInt,
                onSuccess = { groupData -> groupCode = groupData?.getGroupCode() ?: "######" },
                onError = { error -> Log.e("AdminHome", "Failed to load group info: $error") }
            )

            GroupManager.getGroupMembers(
                groupId = orgIdInt,
                onSuccess = { memberDatas ->
                    val loadedMembers = memberDatas
                        ?.filter { it.role == "MEMBER" }
                        ?.map { it.toGroupMember() }
                        ?: emptyList()
                    onMembersUpdate(loadedMembers)
                    isLoading = false
                },
                onError = { error ->
                    Log.e("AdminHome", "Failed to load members: $error")
                    onMembersUpdate(emptyList())
                    isLoading = false
                }
            )

            onCurrentOrgIdUpdate(currentOrgId)
        } else {
            if (organizations.isNotEmpty()) Log.w("AdminHome", "Invalid currentOrgId: $currentOrgId")
            isLoading = false
        }
    }

    val cfg = LocalConfiguration.current
    val horizontalGutterRatio = 16f / 360f
    fun resolvedGutterDp(ratio: Float = horizontalGutterRatio, min: Dp = 12.dp, max: Dp = 28.dp): Dp {
        val wDp = cfg.screenWidthDp.dp
        return (wDp * ratio).coerceIn(min, max)
    }
    val gutter: Dp = resolvedGutterDp()

    val org = currentOrg ?: Organization(id = "", name = "로딩 중...", memberCount = 0, averageScore = 50f)
    val registeredCount = remember(members) { members.size }
    val sadCount = remember(members) { members.count { scoreToMood(it.avgScore) == Mood.SAD || scoreToMood(it.avgScore) == Mood.DOWN } }
    val avgScore100 = remember(org) { org.averageScore.coerceIn(0f, 100f) }
    val searchResults = remember(query, members) { if (query.isBlank()) emptyList() else members.filter { it.name.contains(query, ignoreCase = true) } }
    val moodFilteredMembers = remember(selectedMood, members) { if (selectedMood == null) members else members.filter { scoreToMood(it.avgScore) == selectedMood } }

    if (showHome) { HomeScreen(); return }

    if (isLoading && organizations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Brown80)
        }
        return
    }

    val ui = org.toOrgMoodUi() // Organization.toOrgMoodUi() 사용 (이미 네 코드에 있음)

    Scaffold(
        containerColor = Brown10,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AdminGreetingHeader(
                dateText = todayText,
                onModeToggle = { showAdminLogin = true },
                userName = org.name,
                registeredCount = registeredCount,
                sadCount = sadCount,
                moodLabel = ui.moodLabel,           // fallback(호환용)
                avgScore100 = org.averageScore,     // 여전히 전달 가능
//                moodIconRes = ui.moodIconRes,       // 핵심: 다이얼로그와 동일한 소스
//                moodText = ui.moodLabel,            // 핵심: 라벨도 동일하게
                recommendation = "",
                searchQuery = query,
                onSearchQueryChange = { query = it },
                onSearch = { },
                horizontalGutter = gutter
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = gutter, vertical = 16.dp),
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

            if (searchResults.isNotEmpty()) {
                items(searchResults, key = { it.id }) { member ->
                    Box(modifier = Modifier.padding(horizontal = gutter)) {
                        MemberCard(member = member, onClick = { onMemberClick(member) })
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

            item {
                AdminGroupRecordsSection(
                    horizontalPadding = gutter,
                    sadCount = sadCount,
                    downCount = members.count { scoreToMood(it.avgScore) == Mood.DOWN },
                    happyCount = members.count { scoreToMood(it.avgScore) == Mood.HAPPY },
                    okayCount  = members.count { scoreToMood(it.avgScore) == Mood.OKAY  },
                    greatCount = members.count { scoreToMood(it.avgScore) == Mood.GREAT },
                    avgChangeCount = 2,
                    recentChangeName = members.firstOrNull()?.name ?: ""
                )
            }

            item {
                AdminMembersSection(
                    horizontalPadding = gutter,
                    members = moodFilteredMembers,
                    searchQuery = "",
                    selectedMood = selectedMood,
                    onSelectMood = { mood -> selectedMood = if (selectedMood == mood) null else mood },
                    onMemberClick = onMemberClick,
                    org = org
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showAdminLogin) {
        AdminLoginDialog(
            onClose = { showAdminLogin = false },
            onEnterGroupCode = { showAdminLogin = false },
            onAdminLoginClick = { showAdminLogin = false },
            onOrgClick = { newOrgId ->
                if (newOrgId.isNotBlank()) {
                    currentOrgId = newOrgId
                    selectedMood = null
                    query = ""
                }
                showAdminLogin = false
            },
            onPersonalLoginClick = { showAdminLogin = false; showHome = true },
            organizations = organizations,
            registeredCount = registeredCount,
            sadCount = sadCount
        )
    }
}
