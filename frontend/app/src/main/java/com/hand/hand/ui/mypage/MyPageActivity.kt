package com.hand.hand.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.api.SignUp.IndividualUserData
import com.hand.hand.api.SignUp.IndividualUserManager
import com.hand.hand.care.CareActivity
import com.hand.hand.diary.DiaryHomeActivity
import com.hand.hand.AiDocument.PrivateAiDocumentHomeActivity
import com.hand.hand.ui.home.HomeActivity
import com.hand.hand.ui.home.CurvedBottomNavBar
import com.hand.hand.ui.home.BottomTab
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.theme.Brown40
import com.hand.hand.ui.theme.Brown80

class MyPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPageScreen(
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun MyPageScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var userData by remember { mutableStateOf<IndividualUserData?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        IndividualUserManager.hasIndividualUser(
            onResult = { exists, data ->
                isLoading = false
                if (exists && data != null) {
                    userData = data
                }
            },
            onFailure = { error ->
                isLoading = false
                Toast.makeText(context, "정보 불러오기 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 반응형 스케일러
    val cfg = LocalConfiguration.current
    val screenHeight = cfg.screenHeightDp.dp
    val screenWidth = cfg.screenWidthDp.dp
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

    Scaffold(
        containerColor = Color(0xFFF7F4F2),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            CurvedBottomNavBar(
                selectedTab = BottomTab.Profile,
                onClickHome = {
                    context.startActivity(Intent(context, HomeActivity::class.java))
                },
                onClickWrite = {
                    context.startActivity(Intent(context, DiaryHomeActivity::class.java))
                },
                onClickDiary = {
                    context.startActivity(Intent(context, PrivateAiDocumentHomeActivity::class.java))
                },
                onClickProfile = {
                    // 이미 마이페이지
                },
                onClickCenter = {
                    context.startActivity(Intent(context, CareActivity::class.java))
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 커스텀 헤더
            MyPageHeader(onBackClick = onBackClick)

            // 콘텐츠 영역
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = screenHeight * 0.25f)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Brown40)
                    }
                } else if (userData == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "프로필 정보를 불러올 수 없습니다",
                            fontFamily = BrandFontFamily,
                            color = Brown80.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = gutter),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                // 프로필 헤더
                item {
                    ProfileHeader(
                        name = userData!!.name,
                        onEditClick = { showEditDialog = true }
                    )
                }

                // 기본 정보
                item {
                    InfoSection(
                        title = "기본 정보",
                        items = listOf(
                            "나이" to "${userData!!.age}세",
                            "성별" to if (userData!!.gender == "M") "남성" else "여성",
                            "직업" to userData!!.job
                        )
                    )
                }

                // 건강 정보
                item {
                    InfoSection(
                        title = "건강 정보",
                        items = listOf(
                            "키" to "${userData!!.height}cm",
                            "몸무게" to "${userData!!.weight}kg",
                            "질환" to userData!!.disease
                        )
                    )
                }

                // 생활 정보
                item {
                    InfoSection(
                        title = "생활 정보",
                        items = listOf(
                            "거주 형태" to userData!!.residenceType
                        )
                    )
                }

                // 알림 설정
                item {
                    InfoSection(
                        title = "알림 설정",
                        items = listOf(
                            "다이어리 알림" to if (userData!!.diaryReminderEnabled) "켜짐" else "꺼짐",
                            "알림 시간" to "${userData!!.notificationHour ?: 20}시"
                        )
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
            }
        }
    }

    // 수정 다이얼로그
    if (showEditDialog && userData != null) {
        EditProfileDialog(
            userData = userData!!,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                IndividualUserManager.updateIndividualUser(
                    name = updated.name,
                    age = updated.age,
                    gender = updated.gender,
                    job = updated.job,
                    height = updated.height,
                    weight = updated.weight,
                    disease = updated.disease,
                    residenceType = updated.residenceType,
                    diaryReminderEnabled = updated.diaryReminderEnabled,
                    hour = updated.notificationHour ?: 20,
                    onSuccess = { data ->
                        userData = data
                        showEditDialog = false
                        Toast.makeText(context, "프로필이 수정되었습니다", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "수정 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BrandFontFamily,
                    color = Brown80
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "프로필 관리",
                    fontSize = 14.sp,
                    fontFamily = BrandFontFamily,
                    color = Brown80.copy(alpha = 0.6f)
                )
            }
            FilledTonalButton(
                onClick = onEditClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Brown40.copy(alpha = 0.1f),
                    contentColor = Brown40
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "수정",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "수정",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = BrandFontFamily,
            color = Brown80,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontFamily = BrandFontFamily,
                            color = Brown80.copy(alpha = 0.7f)
                        )
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = BrandFontFamily,
                            color = Brown80
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    userData: IndividualUserData,
    onDismiss: () -> Unit,
    onSave: (IndividualUserData) -> Unit
) {
    var name by remember { mutableStateOf(userData.name) }
    var age by remember { mutableStateOf(userData.age.toString()) }
    var job by remember { mutableStateOf(userData.job) }
    var height by remember { mutableStateOf(userData.height.toString()) }
    var weight by remember { mutableStateOf(userData.weight.toString()) }
    var disease by remember { mutableStateOf(userData.disease) }
    var residenceType by remember { mutableStateOf(userData.residenceType) }
    var notificationHour by remember { mutableStateOf((userData.notificationHour ?: 20).toString()) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 타이틀
                Text(
                    text = "프로필 수정",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BrandFontFamily,
                    color = Brown80,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 스크롤 가능한 입력 필드
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("이름", fontFamily = BrandFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown40,
                                focusedLabelColor = Brown40,
                                cursorColor = Brown40
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("나이", fontFamily = BrandFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown40,
                                focusedLabelColor = Brown40,
                                cursorColor = Brown40
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = job,
                            onValueChange = { job = it },
                            label = { Text("직업", fontFamily = BrandFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown40,
                                focusedLabelColor = Brown40,
                                cursorColor = Brown40
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("키 (cm)", fontFamily = BrandFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown40,
                                focusedLabelColor = Brown40,
                                cursorColor = Brown40
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("몸무게 (kg)", fontFamily = BrandFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown40,
                                focusedLabelColor = Brown40,
                                cursorColor = Brown40
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = disease,
                            onValueChange = { disease = it },
                            label = { Text("질환", fontFamily = BrandFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown40,
                                focusedLabelColor = Brown40,
                                cursorColor = Brown40
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = residenceType,
                            onValueChange = { residenceType = it },
                            label = { Text("거주 형태", fontFamily = BrandFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown40,
                                focusedLabelColor = Brown40,
                                cursorColor = Brown40
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = notificationHour,
                            onValueChange = { notificationHour = it },
                            label = { Text("알림 시간 (0-23)", fontFamily = BrandFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown40,
                                focusedLabelColor = Brown40,
                                cursorColor = Brown40
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Brown80.copy(alpha = 0.7f)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(Brown80.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "취소",
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val ageInt = age.toIntOrNull() ?: userData.age
                            val heightInt = height.toIntOrNull() ?: userData.height
                            val weightInt = weight.toIntOrNull() ?: userData.weight
                            val hourInt = notificationHour.toIntOrNull() ?: 20

                            onSave(
                                userData.copy(
                                    name = name,
                                    age = ageInt,
                                    job = job,
                                    height = heightInt,
                                    weight = weightInt,
                                    disease = disease,
                                    residenceType = residenceType,
                                    notificationHour = hourInt
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown40,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "저장",
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPageHeader(onBackClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val headerHeight: Dp = screenHeight * 0.25f
    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStartDp: Dp = screenWidth * 0.07f
    val backButtonPaddingTopDp: Dp = screenHeight * 0.05f
    val titlePaddingTopDp: Dp = screenHeight * 0.03f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        // 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFF5C5C5C),
                    shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp)
                )
        )

        // 뒤로가기 버튼
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .padding(start = backButtonPaddingStartDp, top = backButtonPaddingTopDp)
                .size(backButtonSize)
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
        )

        // 타이틀
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = backButtonPaddingTopDp + backButtonSize + titlePaddingTopDp)
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "마이페이지",
                color = Color.White,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.04f).value.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "프로필 관리",
                color = Color.White,
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = (screenHeight * 0.02f).value.sp
            )
        }
    }
}
