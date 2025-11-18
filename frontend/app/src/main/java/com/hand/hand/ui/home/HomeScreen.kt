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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.hand.hand.ui.mypage.MyPageActivity      // ✅ 마이페이지 이동용

import com.hand.hand.api.SignUp.IndividualUserManager
import com.hand.hand.api.Anomaly.AnomalyManager
import com.hand.hand.api.Group.GroupManager         // ✅ 추가된 Import
import com.hand.hand.api.Group.GroupData           // ✅ 추가된 Import
import com.hand.hand.api.riskToday.RiskTodayManager
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

    // ⭐ 오늘의 다이어리 작성 상태
    var diaryStatus by remember { mutableStateOf("작성 전") }

    // 🔹 다이어리 상태 재조회 공통 함수
    fun refreshDiaryStatus(onComplete: (() -> Unit)? = null) {
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
                android.util.Log.d(
                    "HomeScreen",
                    "📓 refreshDiaryStatus 결과: $diaryStatus (status=${items.firstOrNull()?.status})"
                )
                onComplete?.invoke()
            },
            onFailure = { error ->
                diaryLoaded = true
                android.util.Log.e(
                    "HomeScreen",
                    "❌ 다이어리 작성 여부 조회 실패: ${error.message}"
                )
                onComplete?.invoke()
            }
        )
    }

    // 처음 진입 시 다이어리 상태 조회
    LaunchedEffect(Unit) {
        refreshDiaryStatus()
    }

    val isWritten = diaryStatus == "작성 완료"

    // ⭐ 최근 측정 데이터 조회
    var heartRateBpm by remember { mutableStateOf(75) }
    var personalMoodScore by remember { mutableStateOf(79) }
    var stressLevel by remember { mutableStateOf(2) }

    val mood = moodFromScore(personalMoodScore)
    val recommendation = "안전지대연습"

    LaunchedEffect(Unit) {
        com.hand.hand.api.Measurements.MeasurementsManager.getLatestMeasurement(
            onSuccess = { data ->
                data?.let {
                    heartRateBpm = it.heartRate?.toInt() ?: 75
                    personalMoodScore = it.stressIndex?.toInt() ?: 79
                    stressLevel = it.stressLevel ?: 2

                    android.util.Log.d(
                        "HomeScreen",
                        "✅ 최근 측정 데이터: BPM=$heartRateBpm, Score=$personalMoodScore, Level=$stressLevel"
                    )
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
    var todaySleepData by remember {
        mutableStateOf<com.hand.hand.api.Sleep.SleepData?>(null)
    }

    LaunchedEffect(Unit) {
        com.hand.hand.api.Sleep.SleepManager.getTodaySleep(
            onSuccess = { data ->
                sleepLoaded = true
                todaySleepData = data
                if (data != null) {
                    android.util.Log.d(
                        "HomeScreen",
                        "✅ 오늘의 수면 데이터: ${data.sleepDurationMinutes}분"
                    )
                } else {
                    android.util.Log.d("HomeScreen", "ℹ️ 오늘의 수면 데이터 없음")
                }
            },
            onFailure = { error ->
                sleepLoaded = true
                todaySleepData = null
                android.util.Log.e("HomeScreen", "❌ 수면 데이터 조회 실패: ${error.message}")
            }
        )
    }

    // ⭐ 오늘의 마음 완화 세션 개수 조회
    var todaySessionCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        com.hand.hand.api.Relief.ReliefManager.getTodaySessionCount(
            onSuccess = { count ->
                todaySessionCount = count.toInt()
                android.util.Log.d("HomeScreen", "✅ 오늘의 세션 개수: $count")
            },
            onFailure = { error ->
                android.util.Log.e("HomeScreen", "❌ 세션 개수 조회 실패: ${error.message}")
            }
        )
    }

    // ⭐ 오늘의 리스크 점수 상태
    var todayRiskExists by remember { mutableStateOf(false) }
    var todayRiskScore by remember { mutableStateOf<Double?>(null) }

    // 처음 진입 시 오늘의 점수 조회
    LaunchedEffect(Unit) {
        RiskTodayManager.checkRiskTodayExists(
            onSuccess = { exists ->
                todayRiskExists = exists
                if (exists) {
                    RiskTodayManager.getRiskToday(
                        onSuccess = { data ->
                            android.util.Log.d(
                                "HomeScreen",
                                "✅ getRiskToday 성공: riskScore=${data.riskScore}"
                            )
                            todayRiskScore = data.riskScore
                        },
                        onError = { msg ->
                            android.util.Log.e("HomeScreen", "❌ getRiskToday 실패: $msg")
                            todayRiskScore = null
                        }
                    )
                } else {
                    todayRiskScore = null
                }
            },
            onError = {
                todayRiskExists = false
                todayRiskScore = null
            }
        )
    }

    // ✅ 화면 복귀 시 다이어리 상태, 세션, 오늘의 점수 재조회
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 다이어리 상태 재조회
                refreshDiaryStatus()

                // 오늘의 점수 재조회
                RiskTodayManager.checkRiskTodayExists(
                    onSuccess = { exists ->
                        todayRiskExists = exists
                        if (exists) {
                            RiskTodayManager.getRiskToday(
                                onSuccess = { data ->
                                    todayRiskScore = data.riskScore
                                    android.util.Log.d(
                                        "HomeScreen",
                                        "🔄 ON_RESUME - 오늘의 점수 재조회 성공: ${data.riskScore}"
                                    )
                                },
                                onError = {
                                    todayRiskScore = null
                                }
                            )
                        } else {
                            todayRiskScore = null
                        }
                    },
                    onError = {
                        todayRiskExists = false
                        todayRiskScore = null
                    }
                )

                // 마음 완화 세션 개수 재조회
                com.hand.hand.api.Relief.ReliefManager.getTodaySessionCount(
                    onSuccess = { count ->
                        todaySessionCount = count.toInt()
                        android.util.Log.d(
                            "HomeScreen",
                            "🔄 화면 복귀 - 세션 개수: $count"
                        )
                    },
                    onFailure = { }
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var organizations by remember { mutableStateOf<List<Organization>>(emptyList()) }

    LaunchedEffect(Unit) {
        com.hand.hand.api.Group.GroupManager.getGroups(
            onSuccess = { list: List<GroupData>? ->
                Handler(Looper.getMainLooper()).post {
                    val apiList: List<GroupData> = list ?: emptyList()
                    organizations = apiList.mapNotNull { api: GroupData ->
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
            onError = { _ ->
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
        bottomBar = {
            CurvedBottomNavBar(
                selectedTab = BottomTab.Home,
                onClickHome = { /* 이미 홈 */ },
                onClickWrite = {
                    context.startActivity(Intent(context, DiaryHomeActivity::class.java))
                },
                onClickDiary = {
                    context.startActivity(
                        Intent(
                            context,
                            PrivateAiDocumentHomeActivity::class.java
                        )
                    )
                },
                onClickProfile = {
                    context.startActivity(Intent(context, MyPageActivity::class.java))
                },
                onClickCenter = {
                    context.startActivity(Intent(context, CareActivity::class.java))
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true

                // 측정 데이터 재조회
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
                        todaySleepData = data
                    },
                    onFailure = { }
                )

                // 다이어리 상태 재조회
                refreshDiaryStatus {
                    isRefreshing = false
                }

                // (원하면 여기서도 RiskToday 재조회 추가 가능)
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(sdp(24.dp)),
                contentPadding = PaddingValues(top = sdp(16.dp), bottom = 0.dp)
            ) {
                item(key = todayRiskScore) {
                    MyRecordsSection(
                        horizontalPadding = gutter,
                        moodChangeCount = todayAnomalyCount,
                        exists = diaryStatus == "작성 완료",
                        riskScore = todayRiskScore,
                        onMoodChangeClick = {
                            context.startActivity(
                                MoodChangeActivity.intent(
                                    context,
                                    todayAnomalyCount
                                )
                            )
                        }
                    )
                }
                item {
                    MyHealthInfoSection(
                        horizontalPadding = gutter,
                        stressScore = personalMoodScore,
                        sleepData = todaySleepData,
                        todaySessionCount = todaySessionCount,
                        onSleepDataSaved = {
                            com.hand.hand.api.Sleep.SleepManager.getTodaySleep(
                                onSuccess = { data ->
                                    todaySleepData = data
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
