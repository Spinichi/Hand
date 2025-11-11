package com.hand.hand.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily

// 팔레트: 기존 화면과 동일 톤 유지
private val Brown80 = Color(0xFF4B2E1E)
private val ChipBrown = Brown80               // 기본 칩 배경
private val SuccessGreen = Color(0xFF9AB067)  // 가입 성공 배경 (MoodGreen)
private val CheckBubble = Color(0xFFC2B1FF)   // 우측 체크 원 배경 (피그마 C2B1FF)

@Composable
fun GroupCodeInputChip(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    codeLength: Int = 6,
    enabled: Boolean = true,
    // 체크 눌러 성공 처리 시 콜백(예: 서버 검증/조직 교체 등)
    onConfirm: (String) -> Unit = {},
    // 외부에서 성공 상태를 굳이 제어할 필요 없으면 내부 관리
    initiallyJoined: Boolean = false,
) {
    var isFocused by rememberSaveable { mutableStateOf(false) }
    var joined by rememberSaveable { mutableStateOf(initiallyJoined) }

    val pillColor = if (joined) SuccessGreen else ChipBrown
    val textColor = Color.White
    val canConfirm = value.trim().length == codeLength && !joined

    Surface(
        color = pillColor,
        contentColor = textColor,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
            .height(34.dp),
        onClick = { /* pill 자체 클릭 액션 없음 - 디자인 유지 */ },
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측 사람 아이콘 (디자인 유지)
            Icon(
                painter = painterResource(R.drawable.ic_meta_people),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))

            if (joined) {
                // 가입 완료 상태: 고정 텍스트
                Text(
                    text = "가입되었습니다",
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = BrandFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // 입력 필드
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(it.take(codeLength)) }, // 최대 6글자
                    singleLine = true,
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = BrandFontFamily
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val code = value.trim()
                            if (code.length == codeLength) {
                                joined = true
                                onConfirm(code)
                            }
                        }
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused }
                ) { inner ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (value.isEmpty()) {
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

            // 우측 체크 버블: 입력이 codeLength에 도달하면 표시
            if (canConfirm || joined) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CheckBubble)
                        .clickable(enabled = !joined) {
                            val code = value.trim()
                            if (code.length == codeLength) {
                                joined = true
                                onConfirm(code)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mini_check),
                        contentDescription = "confirm",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}