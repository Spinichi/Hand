package com.hand.hand.ui.home.dialog

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hand.hand.R
import com.hand.hand.ui.home.components.OrganizationCard
import com.hand.hand.ui.theme.*

@Composable
fun AdminLoginDialog(
    onClose: () -> Unit,
    onEnterGroupCode: (String) -> Unit,   // 칩 입력 확정시 호출
    onAdminLoginClick: () -> Unit,
    onOrgClick: (String) -> Unit
) {
    var groupCode by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

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
                // ───────────────── 카드(그대로) ─────────────────
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
                        // 칩 + 입력
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Brown80,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.height(34.dp),
                                onClick = { /* 디자인 유지용: 별도 동작 없음 */ }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .fillMaxHeight(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_meta_people),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))

                                    BasicTextField(
                                        value = groupCode,
                                        onValueChange = { groupCode = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = BrandFontFamily
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                val code = groupCode.trim()
                                                if (code.isNotEmpty()) onEnterGroupCode(code)
                                            }
                                        ),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .wrapContentWidth()
                                            .onFocusChanged { isFocused = it.isFocused }
                                    ) { inner ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (groupCode.isEmpty()) {
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
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 타이틀 + 우측 원형 버튼
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
                                Surface(
                                    color = Color.Transparent,
                                    contentColor = Color.Unspecified,
                                    shape = CircleShape,
                                    onClick = onAdminLoginClick,
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

                        // 조직 리스트
                        OrganizationCard(
                            moodIconRes = R.drawable.ic_solid_mood_smile,
                            moodBg = Color(0xFFFFF1C7),
                            title = "강남 경찰서",
                            count = 20,
                            titleColor = Brown80,
                            metaColor = Color(0xFFA5A39F)
                        ) { onOrgClick("강남 경찰서") }

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
                            title = "강남 경찰서",
                            count = 20,
                            titleColor = Brown80,
                            metaColor = Color(0xFFA5A39F)
                        ) { onOrgClick("강남 경찰서") }

                        Spacer(Modifier.height(6.dp))
                    }
                }

                // ───────── 카드 '바로 아래' X 버튼 (기기 무관 고정 간격) ─────────
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
