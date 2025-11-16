package com.hand.hand.feature.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import com.hand.hand.R
import com.hand.hand.ui.home.HomeActivity
import com.hand.hand.ui.admin.AdminHomeActivity
import com.hand.hand.ui.common.BrandWaveHeader
import com.hand.hand.ui.home.components.OrganizationCard
import com.hand.hand.ui.model.*
import com.hand.hand.api.Group.GroupManager
import com.hand.hand.api.Group.GroupData
import com.hand.hand.api.SignUp.IndividualUserManager
import com.hand.hand.ui.theme.*

class SignInTypeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignInTypeScreen()
        }
    }
}

@Composable
fun SignInTypeScreen(
    onAdminLoginClick: () -> Unit = {},
    onOrgClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val horizontalPadding: Dp = (screenWidthDp * 0.05f).dp

    var groupCode by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // ── 서버에서 조직 목록 가져오기 ──
    var organizations by remember { mutableStateOf<List<Organization>>(emptyList()) }
    var orgLoading by remember { mutableStateOf(true) }
    var orgError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        orgLoading = true
        orgError = null
        GroupManager.getGroups(
            onSuccess = { list: List<GroupData>? ->
                Handler(Looper.getMainLooper()).post {
                    val apiList: List<GroupData> = list ?: emptyList()
                    organizations = apiList.map { api: GroupData ->
                        Organization(
                            id = api.id?.toString() ?: "",          // null이면 빈 문자열
                            name = api.name ?: "알 수 없음",
                            memberCount = api.memberCount ?: 0,
                            averageScore = api.avgMemberRiskScore?.toFloat() ?: 0f
                        )
                    }
                    orgLoading = false
                }
            },
            onError = { err ->
                Handler(Looper.getMainLooper()).post {
                    orgError = err
                    orgLoading = false
                }
            }
        )
    }

    // 개인 정보 등록 여부 상태
    var isPersonalRegistered by remember { mutableStateOf<Boolean?>(null) }
    var isCheckingPersonal by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        IndividualUserManager.hasIndividualUser(
            // 👇 **수정 전:** onResult = { exists -> ... }
            onResult = { exists, data -> // ✅ 수정: 두 번째 인자(data 또는 _ )를 추가합니다.
                isPersonalRegistered = exists
                isCheckingPersonal = false
            },
            onFailure = {
                isPersonalRegistered = false
                isCheckingPersonal = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
            .padding(horizontal = horizontalPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── BrandWaveHeader ──
        val edgeY = (screenHeightDp * 0.08f).dp
        val centerY = (screenHeightDp * 0.25f).dp
        val overhang = (screenWidthDp * 0.06f).dp
        val headerHeight = centerY * 0.9f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(centerY)
        ) {
            BrandWaveHeader(
                fillColor = Color(0xFF9BB168),
                edgeY = edgeY,
                centerY = centerY,
                overhang = overhang,
                height = headerHeight
            ) {
                Image(
                    painter = painterResource(R.drawable.image_14),
                    contentDescription = "로고",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-screenHeightDp * 0.04f).dp)
                        .size(width = screenWidthDp.dp * 0.3f, height = screenHeightDp.dp * 0.05f)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // ── 단체 코드 입력 ──
            val groupInputHeight = (screenHeightDp * 0.06f).dp
            val groupInputPaddingHorizontal = (screenWidthDp * 0.05f).dp
            val iconSize = (screenHeightDp * 0.03f).dp
            val fontSize = (screenHeightDp * 0.023f).sp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GroupCodeInputChipSignIn(
                    height = groupInputHeight,
                    widthFraction = 0.49f,
                    horizontalPad = groupInputPaddingHorizontal,
                    iconSize = iconSize,
                    textSize = fontSize,
                    value = groupCode,
                    onValueChange = { groupCode = it },
                    isFocused = isFocused,
                    onFocusChange = { isFocused = it },
                    verified = verified,
                    completeLength = 6,
                    onCheckClick = {
                        if (!verified) {
                            val code = groupCode.trim()
                            if (code.isEmpty()) return@GroupCodeInputChipSignIn
                            GroupManager.joinGroup(
                                inviteCode = code,
                                onSuccess = { verified = true },
                                onError = { verified = false }
                            )
                        } else {
                            verified = false
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            val personalCardHeight = (screenHeightDp * 0.08f).dp
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = {
                    if (isCheckingPersonal) return@Card

                    if (isPersonalRegistered == true) {
                        // HomeActivity로 즉시 이동 (배경색 설정으로 검은 화면 방지)
                        val intent = Intent(context, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    } else {
                        val intent = Intent(context, SignUpPrivateActivity::class.java)
                        context.startActivity(intent)
                    }
                },
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = personalCardHeight)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val buttonText = when {
                        isCheckingPersonal -> "확인 중..."
                        isPersonalRegistered == true -> "개인으로 로그인"
                        else -> "개인으로 등록"
                    }
                    Text(
                        text = buttonText,
                        color = Brown80,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BrandFontFamily,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(R.drawable.chevron_right),
                        contentDescription = "이동",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── 관리자 로그인 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "관리자로 로그인",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brown80,
                    fontFamily = BrandFontFamily
                )
                Spacer(modifier = Modifier.width((screenWidthDp * 0.02f).dp))
                Surface(
                    color = Color.Transparent,
                    shape = CircleShape,
                    onClick = {
                        val intent = Intent(context, SignUpManagerActivity::class.java)
                        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_base_button_orange),
                        contentDescription = "관리자 추가",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 조직 리스트 (API 연동 완료) ──
            organizations.forEach { org ->
                val ui = org.toOrgMoodUi()
                val rawCount = org.memberCount
                val memberOnlyCount = maxOf(0, rawCount - 1)
                OrganizationCard(
                    moodIconRes = ui.moodIconRes,
                    moodBg = ui.moodBg,
                    title = org.name,
                    count = memberOnlyCount,
                    titleColor = Brown80,
                    metaColor = Color(0xFFA5A39F)
                ) {
                    val intent = Intent(context, AdminHomeActivity::class.java)
                    intent.putExtra("org_id", org.id)
                    if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }

            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

/* ───────────────────────────────────────────────────────────────
   GroupCodeInputChipSignIn
   (디자인 완전히 유지)
────────────────────────────────────────────────────────────── */
@Composable
private fun GroupCodeInputChipSignIn(
    height: Dp,
    widthFraction: Float,
    horizontalPad: Dp,
    iconSize: Dp,
    textSize: TextUnit,
    value: String,
    onValueChange: (String) -> Unit,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    verified: Boolean,
    completeLength: Int = 6,
    onCheckClick: () -> Unit
) {
    val trailingSize = 24.dp
    val trailingGap = 8.dp
    val isComplete = value.length >= completeLength
    val chipBg = if (verified) Color(0xFF9AB067) else Brown80

    Surface(
        color = chipBg,
        contentColor = Color.White,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier
            .height(height)
            .fillMaxWidth(widthFraction),
        onClick = { /* no-op */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = horizontalPad, end = horizontalPad)
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isComplete && !verified) {
                    Icon(
                        painter = painterResource(R.drawable.ic_meta_people),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(Modifier.width((iconSize.value * 0.5f).dp))
                }

                val displayText = if (verified) "가입되었습니다" else value

                BasicTextField(
                    value = displayText,
                    onValueChange = { if (!verified) onValueChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = textSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BrandFontFamily
                    ),
                    readOnly = verified,
                    cursorBrush = if (verified) SolidColor(Color.Transparent) else SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onCheckClick() }),
                    modifier = Modifier
                        .height(height)
                        .onFocusChanged { onFocusChange(it.isFocused) }
                ) { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (!verified && displayText.isEmpty()) {
                            Text(
                                "단체 코드 입력",
                                color = if (isFocused) Color.White.copy(alpha = 0.7f) else Color.White,
                                fontSize = textSize,
                                fontWeight = FontWeight.Medium,
                                fontFamily = BrandFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        inner()
                    }
                }

                if (isComplete || verified) {
                    Spacer(Modifier.width(trailingGap + trailingSize))
                }
            }

            if (isComplete || verified) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF0EBFF),
                    onClick = onCheckClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(trailingSize)
                        .clip(CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.ic_mini_check),
                            contentDescription = "확인",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
