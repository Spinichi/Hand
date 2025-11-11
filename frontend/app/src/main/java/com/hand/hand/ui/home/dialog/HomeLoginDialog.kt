// file: com/hand/hand/ui/home/dialog/HomeLoginDialog.kt
package com.hand.hand.ui.home.dialog

import android.content.Intent
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.hand.hand.ui.model.GroupCodeRepository // ★ 더미 검증

@Composable
fun HomeLoginDialog(
    onClose: () -> Unit,
    onEnterGroupCode: (String) -> Unit,
    onAdminLoginClick: () -> Unit,
    onOrgClick: (String) -> Unit,
    organizations: List<Organization>
) {
    var groupCode by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }

    // ✨ 여기 추가
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GroupCodeInputChip(
                                value = groupCode,
                                onValueChange = {
                                    groupCode = it
                                    // 필요시 검증 해제 로직: verified = false
                                },
                                isFocused = isFocused,
                                onFocusChange = { isFocused = it },

                                // ✨ 체크 눌렀을 때 포커스 해제
                                onCheckClick = {
                                    if (!verified) {
                                        val code = groupCode.trim()
                                        val ok = GroupCodeRepository.verify(code)
                                        if (ok) {
//                                            onEnterGroupCode(code)
                                            verified = true
                                            focusManager.clearFocus(force = true) // ← 여기!
                                        } else {
                                            verified = false
                                        }
                                    } else {
                                        verified = false
                                    }
                                },

                                verified = verified,
                                completeLength = 6
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // 타이틀 + 우측 원형 버튼 (그대로)
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
                                        // onAdminLoginClick()
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

                        // 조직 리스트 (그대로)
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

                // 닫기 X (그대로)
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

/**
 * 디자인 그대로.
 * - 외부 폭: 최초 측정값으로 고정(폭·좌우 간격 변동 없음)
 * - 체크 원: 오버레이로 배치(레이아웃에 영향 X)
 * - verified = true 이면 칩 안 텍스트를 “가입되었습니다”로 표시
 * - 체크를 다시 누르면 verified 토글(입력 상태 복귀)
 */
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

    // 외부 폭 고정
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
                // 완료/검증 상태가 아닐 때만 사람 아이콘 표시
                if (!isComplete && !verified) {
                    Icon(
                        painter = painterResource(R.drawable.ic_meta_people),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }

                // 표시할 텍스트
                val displayText = if (verified) "가입되었습니다" else value

                BasicTextField(
                    value = displayText,
                    onValueChange = {
                        // verified 때는 텍스트를 바꾸지 않음(디자인 유지)
                        if (!verified) onValueChange(it)
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = BrandFontFamily
                    ),
                    readOnly = verified,                       // 검증 상태에선 입력 잠금
                    cursorBrush = if (verified)
                        androidx.compose.ui.graphics.SolidColor(Color.Transparent) // 커서 숨김
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

                // 체크가 보일 때만 텍스트 오른쪽 여유 확보
                if (isComplete || verified) {
                    Spacer(Modifier.width(trailingGap + trailingSize))
                }
            }

            // 체크(오버레이) — 누르면 onCheckClick() → 검증/토글
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
