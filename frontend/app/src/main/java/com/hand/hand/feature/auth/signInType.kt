package com.hand.hand.feature.auth

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.home.HomeActivity
import com.hand.hand.ui.common.BrandWaveHeader
import com.hand.hand.ui.home.components.OrganizationCard
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.theme.Brown80
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import com.hand.hand.ui.model.GroupCodeRepository   // ✅ 더미 검증

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

        // ── 본문 ──
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // ── 단체 코드 입력 ──
            val groupInputHeight = (screenHeightDp * 0.06f).dp
            val groupInputPaddingHorizontal = (screenWidthDp * 0.05f).dp
            val iconSize = (screenHeightDp * 0.03f).dp
            val spacerWidth = (screenWidthDp * 0.015f).dp
            val fontSize = (screenHeightDp * 0.023f).sp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GroupCodeInputChipSignIn(
                    // 디자인 파라미터 유지
                    height = groupInputHeight,
                    widthFraction = 0.49f,
                    horizontalPad = groupInputPaddingHorizontal,
                    iconSize = iconSize,
                    textSize = fontSize,
                    // 상태/동작
                    value = groupCode,
                    onValueChange = {
                        groupCode = it
                        // 필요 시 입력 변경 시 검증 해제하려면 아래 주석 해제
                        // verified = false
                    },
                    isFocused = isFocused,
                    onFocusChange = { isFocused = it },
                    verified = verified,
                    completeLength = 6,
                    onCheckClick = {
                        if (!verified) {
                            val code = groupCode.trim()
                            if (GroupCodeRepository.verify(code)) {
                                verified = true
                                focusManager.clearFocus(force = true) // 포커스 해제
                                // onEnterGroupCode(code) ❌ 여기서 호출하지 않음 (로컬 토글만)
                            } else {
                                verified = false
                            }
                        } else {
                            // 토글 off
                            verified = false
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            val personalCardHeight = (screenHeightDp * 0.08f).dp

            // ── 개인으로 로그인 ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = {
                    context.startActivity(Intent(context, HomeActivity::class.java))
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

            // ── 개인으로 등록 (SignUpManagerActivity로 이동) ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = {
                    context.startActivity(Intent(context, SignUpPrivateActivity::class.java))
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
                    Text(
                        text = "개인으로 등록",
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
                        context.startActivity(Intent(context, SignUpManagerActivity::class.java))
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

            // ── 조직 예시 리스트 ──
            OrganizationCard(
                moodIconRes = R.drawable.ic_solid_mood_smile,
                moodBg = Color(0xFFFFF1C7),
                title = "멀티캠퍼스",
                count = 20,
                titleColor = Brown80,
                metaColor = Color(0xFFA5A39F)
            ) { onOrgClick("멀티캠퍼스") }

            OrganizationCard(
                moodIconRes = R.drawable.ic_solid_mood_depressed,
                moodBg = Color(0xFFE7DBFF),
                title = "강남 경찰서",
                count = 20,
                titleColor = Brown80,
                metaColor = Color(0xFFA5A39F)
            ) { onOrgClick("강남 경찰서") }

            OrganizationCard(
                moodIconRes = R.drawable.ic_solid_mood_sad,
                moodBg = Color(0xFFFFE1CC),
                title = "서초 경찰서",
                count = 20,
                titleColor = Brown80,
                metaColor = Color(0xFFA5A39F)
            ) { onOrgClick("서초 경찰서") }

            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

/* ─────────────────────────────────────────────────────────────────────────────
   GroupCodeInputChipSignIn
   - 디자인은 SignInTypeScreen의 기존 값(높이/폭/폰트/패딩) 그대로 사용
   - 동작은 Home/Admin 칩과 동일(토글, 배경 9AB067, "가입되었습니다")
   - 체크는 오버레이(오른쪽), 레이아웃 폭에 영향 없음
   - 좌우 간격/폰트/크기 절대 변경하지 않음
   ───────────────────────────────────────────────────────────────────────────── */
@Composable
private fun GroupCodeInputChipSignIn(
    // 디자인 파라미터
    height: Dp,
    widthFraction: Float,
    horizontalPad: Dp,
    iconSize: Dp,
    textSize: TextUnit,

    // 상태/동작
    value: String,
    onValueChange: (String) -> Unit,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    verified: Boolean,
    completeLength: Int = 6,
    onCheckClick: () -> Unit
) {
    val trailingSize = 24.dp   // 체크 원 지름 (동일)
    val trailingGap  = 8.dp    // 텍스트와 체크 사이 간격 (동일)

    val isComplete = value.length >= completeLength
    val chipBg = if (verified) Color(0xFF9AB067) else Brown80

    Surface(
        color = chipBg,
        contentColor = Color.White,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier
            .height(height)
            .fillMaxWidth(widthFraction),
        onClick = { /* no-op: 디자인 유지 */ }
    ) {
        // 체크 유무와 무관하게 오른쪽 예약공간 유지 → 겹침/흔들림 없음
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
                    Spacer(Modifier.width((iconSize.value * 0.5f).dp)) // 기존 spacerWidth 비율 유지감
                }

                val displayText = if (verified) "가입되었습니다" else value

                BasicTextField(
                    value = displayText,
                    onValueChange = { if (!verified) onValueChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = textSize,
                        fontWeight = FontWeight.Bold,       // 기존 굵기 유지
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

            // 체크 아이콘(오버레이)
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
