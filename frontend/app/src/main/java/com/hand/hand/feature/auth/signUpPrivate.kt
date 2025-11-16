package com.hand.hand.feature.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hand.hand.R
import com.hand.hand.ui.common.BrandWaveHeader
import com.hand.hand.ui.theme.BrandFontFamily

import android.widget.Toast
import com.hand.hand.api.SignUp.IndividualUserManager

class SignUpPrivateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpPrivateScreen()
        }
    }
}

@Composable
fun SignUpPrivateScreen(
    onClickLogin: (String, String) -> Unit = { _, _ -> },
    onClickSignUp: () -> Unit = {}
)
 {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val horizontalPadding: Dp = (screenWidthDp * 0.05f).dp

    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var disease by remember { mutableStateOf("") }
    var isAlarmEnabled by remember { mutableStateOf(false) }
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }
     var job by remember { mutableStateOf("") }
     var familyCount by remember { mutableStateOf("") }


     val edgeY = (screenHeightDp * 0.08f).dp
    val centerY = (screenHeightDp * 0.25f).dp
    val overhang = (screenWidthDp * 0.06f).dp
    val headerHeight = centerY * 0.9f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        val scrollState = rememberScrollState()

        // ── 헤더 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeightDp.dp * 0.28f)
                .zIndex(1f)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding)
                .padding(top = screenHeightDp.dp * 0.23f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "개인 등록",
                fontSize = (screenWidthDp * 0.07f).sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = BrandFontFamily,
                color = Color(0xFF4F3422),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Image(
                painter = painterResource(id = R.drawable.signup_private_level),
                contentDescription = "레벨 안내 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeightDp * 0.05f).dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(20.dp))

            // 이름
            LabeledTextField(
                label = "이름",
                value = name,
                onValueChange = { name = it },
                placeholder = "이름을 입력하세요.",
                iconRes = R.drawable.signup_private_name
            )

            Spacer(Modifier.height(16.dp))

            // 나이 + 성별
            AgeGenderRow(

                screenWidthDp = screenWidthDp,
                screenHeightDp = screenHeightDp,
                age = age,
                onAgeChange = { age = it },
                selectedGender = selectedGender,
                onGenderSelected = { selectedGender = it }
            )

            Spacer(Modifier.height(20.dp))

            // 키 & 몸무게
            HeightWeightRow(
                screenWidthDp = screenWidthDp,
                screenHeightDp = screenHeightDp,
                height = height,
                onHeightChange = { height = it },
                weight = weight,
                onWeightChange = { weight = it }
            )

            Spacer(Modifier.height(16.dp))

            // 질병 입력
            LabeledTextField(
                label = "질병 - 신체/정신",
                value = disease,
                onValueChange = { disease = it },
                placeholder = "병명을 입력하세요.",
                iconRes = R.drawable.signup_private_sick
            )

            Spacer(Modifier.height(20.dp))

            JobFamilyRow(
                screenWidthDp = screenWidthDp,
                screenHeightDp = screenHeightDp,
                job = job,
                onJobChange = { job = it },
                familyCount = familyCount,
                onFamilySelected = { familyCount = it }
            )

            Spacer(Modifier.height(20.dp))


            // 다이어리 알림
            DiaryAlarmSection(
                isAlarmEnabled = isAlarmEnabled,
                onToggle = { isAlarmEnabled = it },
                hour = hour,
                minute = minute,
                onHourChange = { hour = it },
                onMinuteChange = { minute = it },
                screenWidthDp = screenWidthDp,
                screenHeightDp = screenHeightDp
            )

            Spacer(Modifier.height(28.dp))

            // 등록 버튼
            Button(
                onClick = {
                    if (name.isBlank() || age.isBlank() || selectedGender.isBlank() ||
                        job.isBlank() || familyCount.isBlank()) {
                        Toast.makeText(context, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val ageInt = age.toIntOrNull() ?: 0
                    val heightInt = height.toIntOrNull() ?: 0
                    val weightInt = weight.toIntOrNull() ?: 0

                    val alarmHourInt = if (isAlarmEnabled) hour.toIntOrNull() ?: 0 else 0
                    val alarmMinuteInt = if (isAlarmEnabled) minute.toIntOrNull() ?: 0 else 0

                    val genderCode = when (selectedGender) {
                        "남성" -> "M"
                        "여성" -> "F"
                        else -> ""
                    }

                    IndividualUserManager.registerIndividualUser(
                        name = name,
                        age = ageInt,
                        gender = genderCode,
                        job = job,
                        height = heightInt,
                        weight = weightInt,
                        disease = disease,
                        residenceType = familyCount,
                        diaryReminderEnabled = isAlarmEnabled,
                        hour = alarmHourInt,
                        minute = alarmMinuteInt,
                        onSuccess = {
                            val intent = Intent(context, SignUpPrivateSurveyActivity::class.java)
                            context.startActivity(intent)
                        },
                        onFailure = { e ->
                            e.printStackTrace()
                            Toast.makeText(
                                context,
                                "개인 정보 등록 실패: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenWidthDp * 0.14f).dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A2E1F),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "다음단계",
                        color = Color.White,
                        fontSize = (screenWidthDp * 0.04f).sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = BrandFontFamily
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Image(
                        painter = painterResource(id = R.drawable.login_btn),
                        contentDescription = null,
                        modifier = Modifier.size((screenWidthDp * 0.05f).dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ────────────────────────────────
// UI 구성 요소
// ────────────────────────────────

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    Text(
        text = label,
        fontSize = (screenWidthDp * 0.03f).sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4F3422),
        modifier = Modifier.fillMaxWidth(),
        fontFamily = BrandFontFamily
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        placeholder = { Text(placeholder, fontFamily = BrandFontFamily) },
        leadingIcon = {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size((screenWidthDp * 0.06f).dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF9BB168),
            unfocusedBorderColor = Color(0xFFBFD19B),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = Color(0xFF9BB168)
        )
    )
}


// ────────────────────────────────
// 하위 UI 구성 컴포넌트
// ────────────────────────────────


@Composable
fun AgeGenderRow(
    screenWidthDp: Int,
    screenHeightDp: Int,
    age: String,
    onAgeChange: (String) -> Unit,
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            modifier = Modifier.weight(0.8f),
            placeholder = { Text("나이", fontFamily = BrandFontFamily) },
            trailingIcon = {
                Text(
                    text = "세",
                    color = Color(0xFF4F3422),
                    fontSize = (screenWidthDp * 0.035f).sp,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9BB168),
                unfocusedBorderColor = Color(0xFFBFD19B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        val options = listOf("남성", "여성")
        Row(
            modifier = Modifier
                .weight(1.2f)
                .height((screenHeightDp * 0.065f).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedGender == option
                Surface(
                    color = if (isSelected) Color(0xFF9BB168) else Color.White,
                    shape = RoundedCornerShape(100.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onGenderSelected(option) }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = option,
                            color = if (isSelected) Color.White else Color(0xFF4F3422),
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily,
                            fontSize = (screenWidthDp * 0.035f).sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeightWeightRow(
    screenWidthDp: Int,
    screenHeightDp: Int,
    height: String,
    onHeightChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = height,
            onValueChange = onHeightChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("키", fontFamily = BrandFontFamily) },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.signup_private_height),
                    contentDescription = "키 아이콘",
                    modifier = Modifier.size((screenWidthDp * 0.06f).dp)
                )
            },
            trailingIcon = {
                Text(
                    text = "cm",
                    color = Color(0xFF4F3422),
                    fontSize = (screenWidthDp * 0.035f).sp,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9BB168),
                unfocusedBorderColor = Color(0xFFBFD19B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("몸무게", fontFamily = BrandFontFamily) },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.signup_private_weight),
                    contentDescription = "몸무게 아이콘",
                    modifier = Modifier.size((screenWidthDp * 0.06f).dp)
                )
            },
            trailingIcon = {
                Text(
                    text = "kg",
                    color = Color(0xFF4F3422),
                    fontSize = (screenWidthDp * 0.035f).sp,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9BB168),
                unfocusedBorderColor = Color(0xFFBFD19B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
fun JobFamilyRow(
    screenWidthDp: Int,
    screenHeightDp: Int,
    job: String,
    onJobChange: (String) -> Unit,
    familyCount: String,
    onFamilySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // ───── 직업 입력 ─────
        OutlinedTextField(
            value = job,
            onValueChange = onJobChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("직업", fontFamily = BrandFontFamily) },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.job),
                    contentDescription = "직업 아이콘",
                    modifier = Modifier.size((screenWidthDp * 0.06f).dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9BB168),
                unfocusedBorderColor = Color(0xFFBFD19B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        // ───── 가구원 수 드롭다운 ─────
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("1명", "2명", "3명 이상")

        Box(modifier = Modifier
            .weight(1f)
            .height((screenHeightDp * 0.065f).dp)
        ) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFD19B))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (familyCount.isBlank()) "가구원 수" else familyCount,
                        fontFamily = BrandFontFamily,
                        color = Color(0xFF4F3422),
                        fontSize = (screenWidthDp * 0.035f).sp
                    )

                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontFamily = BrandFontFamily,
                                color = Color(0xFF4F3422)
                            )
                        },
                        onClick = {
                            onFamilySelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun DiaryAlarmSection(
    isAlarmEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    hour: String,
    minute: String,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    screenWidthDp: Int,
    screenHeightDp: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "다이어리 작성 알림 여부",
                fontSize = (screenWidthDp * 0.06f).sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F3422),
                fontFamily = BrandFontFamily
            )
            Switch(
                checked = isAlarmEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    uncheckedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF9BB167),
                    uncheckedTrackColor = Color(0xFFE8DDD9)
                )
            )
        }

        if (isAlarmEnabled) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = hour,
                    onValueChange = onHourChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("18", fontFamily = BrandFontFamily, color = Color(0xFFBFBFBF)) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.signup_private_time),
                            contentDescription = null,
                            modifier = Modifier.size((screenWidthDp * 0.06f).dp)
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = "시",
                            color = Color(0xFF4F3422),
                            fontSize = (screenWidthDp * 0.035f).sp,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9BB168),
                        unfocusedBorderColor = Color(0xFFBFD19B),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = minute,
                    onValueChange = onMinuteChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("00", fontFamily = BrandFontFamily, color = Color(0xFFBFBFBF)) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.signup_private_time),
                            contentDescription = null,
                            modifier = Modifier.size((screenWidthDp * 0.06f).dp)
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = "분",
                            color = Color(0xFF4F3422),
                            fontSize = (screenWidthDp * 0.035f).sp,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9BB168),
                        unfocusedBorderColor = Color(0xFFBFD19B),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }
    }
}
