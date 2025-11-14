// file: com/hand/hand/ui/admin/dialog/AdminLoginDialog.kt
package com.hand.hand.ui.admin.dialog

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hand.hand.R
import com.hand.hand.feature.auth.SignUpManagerActivity
import com.hand.hand.ui.home.components.OrganizationCard
import com.hand.hand.ui.model.Organization
import com.hand.hand.ui.model.toOrgMoodUi
import com.hand.hand.ui.theme.*
import com.hand.hand.ui.model.GroupCodeRepository // 더미 검증
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.hand.hand.api.Group.GroupManager

@Composable
fun AdminLoginDialog(
    onClose: () -> Unit,
    onEnterGroupCode: (String) -> Unit,      // ※ 지금은 즉시 호출 안함 (설계상 유지만)
    onAdminLoginClick: () -> Unit,
    onOrgClick: (String) -> Unit,
    onPersonalLoginClick: () -> Unit,
    registeredCount: Int,
    sadCount: Int,
    organizations: List<Organization>
) {
    var groupCode by rememberSaveable("admin_group_code") { mutableStateOf("") }
    var isFocused by rememberSaveable("admin_group_focus") { mutableStateOf(false) }
    var verified  by rememberSaveable("admin_group_verified") { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── 카드(디자인 그대로) ──
                Surface(
                    color = Brown10,
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 14.dp,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ── 단체 코드 입력 칩 (Home과 동일 동작/디자인) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // context와 focusManager는 Composable 스코프에서 미리 가져오기 (이미 defined)
                            val context = LocalContext.current
                            val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

                            GroupCodeInputChip(
                                value = groupCode,
                                onValueChange = {
                                    groupCode = it
                                    // 필요 시 입력 수정하면 검증 해제하려면 아래 주석 해제
                                    // verified = false
                                },
                                isFocused = isFocused,
                                onFocusChange = { isFocused = it },

                                // 여기서 서버에 가입 요청을 보냄 — 성공하면 verified = true, 실패하면 false 및 토스트
                                onCheckClick = {
                                    if (!verified) {
                                        val code = groupCode.trim()
                                        if (code.isEmpty()) {
                                            Toast.makeText(context, "코드를 입력하세요.", Toast.LENGTH_SHORT).show()
                                            return@GroupCodeInputChip
                                        }

                                        // 네트워크 호출 (GroupManager.joinGroup 사용)
                                        GroupManager.joinGroup(
                                            inviteCode = code,
                                            onSuccess = { groupData ->
                                                // 콜백은 백그라운드 스레드일 수 있으니 메인 스레드에서 상태 업데이트
                                                Handler(Looper.getMainLooper()).post {
                                                    verified = true
                                                    focusManager.clearFocus(force = true)
                                                    Toast.makeText(context, "가입 완료", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onError = { errMsg ->
                                                Handler(Looper.getMainLooper()).post {
                                                    verified = false
                                                    Toast.makeText(context, "가입 실패: $errMsg", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    } else {
                                        // 이미 가입된 상태면 토글 오프
                                        verified = false
                                    }
                                },

                                verified = verified,
                                completeLength = 6
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── 개인으로 로그인 (디자인 그대로) ──
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            onClick = onPersonalLoginClick,
                            shape = MaterialTheme.shapes.large
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(44.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "개인으로 로그인",
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

                        Spacer(Modifier.height(20.dp))

                        // ── "관리자로 로그인 +" 줄 (디자인 그대로) ──
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.wrapContentWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "관리자로 로그인",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Brown80,
                                    lineHeight = 30.sp,
                                    fontFamily = BrandFontFamily
                                )
                                val context = LocalContext.current

                                Surface(
                                    color = Color.Transparent,
                                    contentColor = Color.Unspecified,
                                    shape = CircleShape,
                                    onClick = {
                                        val intent = Intent(context, SignUpManagerActivity::class.java)
                                        if (context !is android.app.Activity) {
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
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
                        }

                        Spacer(Modifier.height(14.dp))

                        // ── 조직 리스트 (단일 소스) ──
                        organizations.forEach { org ->
                            val ui = org.toOrgMoodUi()
                            OrganizationCard(
                                moodIconRes = ui.moodIconRes,
                                moodBg = ui.moodBg,
                                title = org.name,
                                count = org.memberCount,
                                titleColor = Brown80,
                                metaColor = Color(0xFFA5A39F)
                            ) { onOrgClick(org.id) }
                        }

                        Spacer(Modifier.height(6.dp))
                    }
                }

                // ── 닫기 X (디자인 그대로) ──
                Spacer(Modifier.height(40.dp))
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    tonalElevation = 0.dp,
                    onClick = onClose,
                    modifier = Modifier
                        .size(52.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "✕",
                            fontSize = 18.sp,
                            color = Brown80,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GroupCodeInputChip: Home과 동일 동작/디자인 (변경 없음)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GroupCodeInputChip(
    value: String,
    onValueChange: (String) -> Unit,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onCheckClick: () -> Unit,
    verified: Boolean,
    completeLength: Int = 6
) {
    val density = LocalDensity.current
    val trailingSize = 24.dp   // 체크 원 지름
    val trailingGap  = 8.dp    // 텍스트와 체크 사이 간격
    val horizontalPad = 12.dp  // 칩 좌/우 내부 패딩

    val isComplete = value.length >= completeLength

    var lockedWidth: Dp? by remember { mutableStateOf(null) }
    val chipBg = if (verified) Color(0xFF9AB067) else Brown80

    Surface(
        color = chipBg,
        contentColor = Color.White,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier
            .height(34.dp)
            .then(if (lockedWidth != null) Modifier.width(lockedWidth!!) else Modifier.wrapContentWidth())
            .onGloballyPositioned {
                if (lockedWidth == null) {
                    lockedWidth = with(density) { it.size.width.toDp() }
                }
            },
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }

                val displayText = if (verified) "가입되었습니다" else value

                BasicTextField(
                    value = displayText,
                    onValueChange = { if (!verified) onValueChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = BrandFontFamily
                    ),
                    readOnly = verified,
                    cursorBrush = if (verified)
                        androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                    else
                        androidx.compose.ui.graphics.SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onCheckClick() }),
                    modifier = Modifier
                        .height(34.dp)
                        .onFocusChanged { onFocusChange(it.isFocused) }
                ) { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (!verified && displayText.isEmpty()) {
                            Text(
                                "단체 코드 입력",
                                color = if (isFocused) Color.White.copy(alpha = 0.7f) else Color.White,
                                fontSize = 13.sp,
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
