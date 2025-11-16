@file:OptIn(ExperimentalMaterial3Api::class)

package com.hand.hand.ui.home

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.hand.hand.ui.test.WearTestActivity      // ✅ 워치 테스트용

import com.hand.hand.api.SignUp.IndividualUserManager
import com.hand.hand.api.Anomaly.AnomalyManager
import com.hand.hand.api.Group.GroupManager // ✅ 추가된 Import
import com.hand.hand.api.Group.GroupData // ✅ 추가된 Import
import com.hand.hand.ui.common.LoadingDialog


@Composable
fun HomeScreen() {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // 로딩 완료 체크 (변수는 유지하되 로딩 다이얼로그는 표시 안 함)
    var userLoaded by remember { mutableStateOf(false) }
    var diaryLoaded by remember { mutableStateOf(false) }
    var measurementLoaded by remember { mutableStateOf(false) }
    var anomalyLoaded by remember { mutableStateOf(false) }
    var sleepLoaded by remember { mutableStateOf(false) }
    var groupLoaded by remember { mutableStateOf(false) }

    // 개인용 헤더 데이터
    var userName by remember { mutableStateOf("싸피님") }
    LaunchedEffect(Unit) {
        IndividualUserManager.hasIndividualUser(
            onResult = { exists, data ->
                if (exists && data != null) {
                    userName = data.name.ifBlank { "싸피" } + "님"
                }
                userLoaded = true
            },
            onFailure = { e ->
                e.printStackTrace()
                userLoaded = true
            }
        )
    }

    // ⭐ 오늘의 다이어리 작성 상태 조회
    var diaryStatus by remember { mutableStateOf("작성 전") }

    LaunchedEffect(Unit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
        com.hand.hand.api.Diary.DiaryManager.getMyDiaryList(
            startDate = today,
            endDate = today,
            page = 0,
            size = 1,
            onSuccess = { items ->
                diaryStatus = when {
                    items.isEmpty() -> "작성 전"
                    items.first().status == "COMPLETED" -> "작성 완료"
                    items.first().status == "IN_PROGRESS" -> "작성 중"
                    else -> "작성 전"
                }
                diaryLoaded = true
                android.util.Log.d("HomeScreen", "✅ 오늘의 다이어리 상태: $diaryStatus (status=${items.firstOrNull()?.status})")
            },
            onFailure = { error ->
                diaryLoaded = true
                android.util.Log.e("HomeScreen", "❌ 다이어리 작성 여부 조회 실패: ${error.message}")
            }
        )
    }

    val isWritten = diaryStatus == "작성 완료"

    // ⭐ 최근 측정 데이터 조회
    var heartRateBpm by remember { mutableStateOf(75) }
    var personalMoodScore by remember { mutableStateOf(79) }
    var stressLevel by remember { mutableStateOf(2) }

    val mood = moodFromScore(personalMoodScore)
    val recommendation = "봉인 연습"

    LaunchedEffect(Unit) {
        com.hand.hand.api.Measurements.MeasurementsManager.getLatestMeasurement(
            onSuccess = { data ->
                data?.let {
                    // BPM 업데이트
                    heartRateBpm = it.heartRate?.toInt() ?: 75

                    // 스트레스 지수 업데이트 (0-100 → mood 계산용)
                    personalMoodScore = it.stressIndex?.toInt() ?: 79

                    // 스트레스 레벨 업데이트 (1-5)
                    stressLevel = it.stressLevel ?: 2

                    android.util.Log.d("HomeScreen", "✅ 최근 측정 데이터: BPM=$heartRateBpm, Score=$personalMoodScore, Level=$stressLevel")
                }
                measurementLoaded = true
            },
            onFailure = { error ->
                measurementLoaded = true
                android.util.Log.e("HomeScreen", "❌ 최근 측정 데이터 조회 실패: ${error.message}")
            }
        )
    }

    // ⭐ 오늘의 이상치 개수 조회
    var todayAnomalyCount by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val anomalyManager = AnomalyManager()
        anomalyManager.getAnomalyCount(
            onSuccess = { count ->
                android.util.Log.d("HomeScreen", "✅ 오늘 이상치 개수: $count")
                todayAnomalyCount = count
                anomalyLoaded = true
            },
            onError = { error ->
                anomalyLoaded = true
                android.util.Log.e("HomeScreen", "❌ 이상치 조회 실패: $error")
            }
        )
    }

    // ⭐ 오늘의 수면 데이터 조회
    var todaySleepMinutes by remember { mutableStateOf(0) }
    var hasSleepData by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.hand.hand.api.Sleep.SleepManager.getTodaySleep(
            onSuccess = { data ->
                sleepLoaded = true
                if (data != null) {
                    todaySleepMinutes = data.sleepDurationMinutes
                    hasSleepData = true
                    android.util.Log.d("HomeScreen", "✅ 오늘의 수면 데이터: ${data.sleepDurationMinutes}분")
                } else {
                    hasSleepData = false
                    android.util.Log.d("HomeScreen", "ℹ️ 오늘의 수면 데이터 없음")
                }
            },
            onFailure = { error ->
                sleepLoaded = true
                hasSleepData = false
                android.util.Log.e("HomeScreen", "❌ 수면 데이터 조회 실패: ${error.message}")
            }
        )
    }

    var organizations by remember { mutableStateOf<List<Organization>>(emptyList()) }

    LaunchedEffect(Unit) {
        com.hand.hand.api.Group.GroupManager.getGroups(
            onSuccess = { list: List<GroupData>? ->
                // Compose 상태 업데이트를 위해 메인 스레드로 전달
                Handler(Looper.getMainLooper()).post {
                    val apiList: List<GroupData> = list ?: emptyList()
                    organizations = apiList.mapNotNull { api: GroupData ->
                        // 현재 Organization 모델(memberCount 존재)을 사용하여 객체 생성
                        if (api.id == null || api.name == null) return@mapNotNull null
                        val rawCount = api.memberCount ?: 0
                        val memberOnlyCount = maxOf(0, rawCount - 1)
                        Organization(
                            id = api.id.toString(),
                            name = api.name,
                            memberCount = memberOnlyCount,
                            averageScore = api.avgMemberRiskScore?.toFloat() ?: 0f
                        )
                    }
                    groupLoaded = true
                }
            },
            onError = { err ->
                groupLoaded = true
            }
        )
    }

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
                horizontalGutter = gutter,
                diaryStatus = diaryStatus
            )
        },
        // ✅ 커브드 네비게이션 바
        bottomBar = {
            CurvedBottomNavBar(
                selectedTab = BottomTab.Home,
                onClickHome = {
                    // ✅ 이미 홈 화면이므로 아무것도 안 함
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
                    // ⭐ 워치 데이터 테스트 화면
                    context.startActivity(Intent(context, WearTestActivity::class.java))
                },
                onClickCenter = {
                    // ✅ 중앙 버튼 → CareActivity
                    context.startActivity(Intent(context, CareActivity::class.java))
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true

                // 모든 데이터 재조회
                com.hand.hand.api.Measurements.MeasurementsManager.getLatestMeasurement(
                    onSuccess = { data ->
                        data?.let {
                            heartRateBpm = it.heartRate?.toInt() ?: 75
                            personalMoodScore = it.stressIndex?.toInt() ?: 79
                            stressLevel = it.stressLevel ?: 2
                        }
                    },
                    onFailure = { }
                )

                val anomalyManager = AnomalyManager()
                anomalyManager.getAnomalyCount(
                    onSuccess = { count ->
                        todayAnomalyCount = count
                    },
                    onError = { }
                )

                com.hand.hand.api.Sleep.SleepManager.getTodaySleep(
                    onSuccess = { data ->
                        if (data != null) {
                            todaySleepMinutes = data.sleepDurationMinutes
                            hasSleepData = true
                        } else {
                            hasSleepData = false
                        }
                    },
                    onFailure = { }
                )

                // 다이어리 작성 여부 재조회
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
                com.hand.hand.api.Diary.DiaryManager.getMyDiaryList(
                    startDate = today,
                    endDate = today,
                    page = 0,
                    size = 1,
                    onSuccess = { items ->
                        diaryStatus = when {
                            items.isEmpty() -> "작성 전"
                            items.first().status == "COMPLETED" -> "작성 완료"
                            items.first().status == "IN_PROGRESS" -> "작성 중"
                            else -> "작성 전"
                        }
                        isRefreshing = false
                    },
                    onFailure = {
                        isRefreshing = false
                    }
                )
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(sdp(24.dp)),
                contentPadding = PaddingValues(top = sdp(16.dp), bottom = 0.dp)
            ) {
                item {
                    MyRecordsSection(
                        horizontalPadding = gutter,
                        moodChangeCount = todayAnomalyCount,
                        onMoodChangeClick = {
                            context.startActivity(MoodChangeActivity.intent(context, todayAnomalyCount))
                        }
                    )
                }
                item {
                    MyHealthInfoSection(
                        horizontalPadding = gutter,
                        stressScore = personalMoodScore,
                        sleepMinutes = todaySleepMinutes,
                        hasSleepData = hasSleepData,
                        onSleepDataSaved = {
                            // 수면 데이터 저장 후 다시 조회
                            com.hand.hand.api.Sleep.SleepManager.getTodaySleep(
                                onSuccess = { data ->
                                    if (data != null) {
                                        todaySleepMinutes = data.sleepDurationMinutes
                                        hasSleepData = true
                                    }
                                },
                                onFailure = { }
                            )
                        }
                    )
                }
                item { Spacer(Modifier.height(sdp(16.dp))) }
            }
        }
    }

    // 관리자/조직 진입 다이얼로그
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