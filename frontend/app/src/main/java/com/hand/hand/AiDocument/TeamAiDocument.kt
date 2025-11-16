// file: com/hand/hand/AiDocument/TeamAiDocumentActivity.kt
package com.hand.hand.AiDocument

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.api.Group.GroupManager
import com.hand.hand.api.Group.GroupMemberData
import com.hand.hand.ui.theme.BrandFontFamily
import android.content.Intent

class TeamAiDocumentActivity : ComponentActivity() {
    private var memberId: Int = -1
    private var orgId: Int = -1
    private var noteTextValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orgId = intent.getIntExtra("ORG_ID", -1)
        memberId = intent.getIntExtra("MEMBER_ID", -1)
        val memberName = intent.getStringExtra("MEMBER_NAME") ?: "멤버"

        if (orgId == -1 || memberId == -1) {
            Log.e("TeamAiDocument", "Invalid IDs. orgId: $orgId, memberId: $memberId. Finishing Activity.")
            finish()
            return
        }

        setContent {
            TeamAiDocumentScreen(
                orgId = orgId,
                memberId = memberId,
                memberName = memberName,
                onBackClick = {
                    // 뒤로가기 시 수정된 note 저장 (빈 문자열 포함)
                    GroupManager.updateMemberNotes(
                        groupId = orgId,
                        userId = memberId,
                        notes = noteTextValue,
                        onSuccess = {
                            Log.d("TeamAiDocument", "Notes updated successfully")
                            val resultIntent = Intent().apply {
                                putExtra("UPDATED_MEMBER_ID", memberId)
                                putExtra("UPDATED_SPECIAL_NOTES", noteTextValue)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        },
                        onError = { err ->
                            Log.e("TeamAiDocument", "Failed to update notes: $err")
                            finish()
                        }
                    )
                },
                onNoteChange = { noteTextValue = it }
            )
        }
    }
}

@Composable
fun TeamAiDocumentScreen(
    orgId: Int,
    memberId: Int,
    memberName: String,
    onBackClick: () -> Unit,
    onNoteChange: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    var memberData by remember { mutableStateOf<GroupMemberData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orgId, memberId) {
        isLoading = true
        GroupManager.getGroupMembers(
            groupId = orgId,
            onSuccess = { members ->
                val foundMember = members?.find { it.userId == memberId }
                if (foundMember != null) {
                    memberData = foundMember
                } else {
                    error = "멤버 정보를 찾을 수 없습니다."
                }
                isLoading = false
            },
            onError = { apiError ->
                error = "데이터 로딩 실패: $apiError"
                isLoading = false
            }
        )
    }

    val avgScore = memberData?.weeklyAvgRiskScore?.toInt()?.coerceIn(0, 100) ?: 0
    val scores = List(7) { avgScore }
    val xLabels = listOf("Day1", "Day2", "Day3", "Day4", "Day5", "Day6", "Day7")
    val adviceText = memberData?.let { "주간 평균 점수(${it.weeklyAvgRiskScore?.toInt()}) 기반의 조언입니다." } ?: ""
    val specialNote = memberData?.specialNotes ?: ""
    var noteText by rememberSaveable(memberData) { mutableStateOf(specialNote) }

    LaunchedEffect(noteText) {
        onNoteChange(noteText)
    }

    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStart: Dp = screenWidth * 0.07f
    val backButtonPaddingTop: Dp = screenHeight * 0.05f
    val bottomBoxHeight: Dp = screenHeight * 0.8f
    val bottomBoxRadius: Dp = 30.dp
    val imageWidth: Dp = screenWidth * 0.25f
    val imageHeight: Dp = screenHeight * 0.15f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFACA6E9))
    ) {
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "Back Button",
            modifier = Modifier
                .padding(start = backButtonPaddingStart, top = backButtonPaddingTop)
                .size(backButtonSize)
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
        )

        Text(
            text = memberName,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (screenHeight * 0.03f).value.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = backButtonPaddingStart + backButtonSize + 18.dp,
                    top = backButtonPaddingTop + (backButtonSize / 4)
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomBoxHeight)
                .align(Alignment.BottomCenter)
                .background(
                    color = Color(0xFFF7F4F2),
                    shape = RoundedCornerShape(topStart = bottomBoxRadius, topEnd = bottomBoxRadius)
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red,
                    fontSize = 16.sp
                )
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(top = imageHeight / 2)
                ) {
                    Text(
                        text = "감정 경향 그래프",
                        modifier = Modifier.fillMaxWidth().padding(start = screenWidth * 0.05f),
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.025f).value.sp,
                        color = Color(0xFF4F3422)
                    )
                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        // EmotionLineChart(scores = scores, xLabels = xLabels, modifier = Modifier.fillMaxWidth(0.8f).height(screenHeight * 0.2f))
                    }
                    Spacer(modifier = Modifier.height(screenHeight * 0.08f))
                    Text(
                        text = "특이사항",
                        modifier = Modifier.fillMaxWidth().padding(start = screenWidth * 0.05f),
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.025f).value.sp,
                        color = Color(0xFF4F3422)
                    )
                    Spacer(modifier = Modifier.height(screenHeight * 0.015f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenWidth * 0.05f)
                            .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                            .padding(vertical = 0.dp)
                    ) {
                        TextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("특이사항을 입력하세요", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (screenHeight * 0.023f).value.sp,
                                color = Color(0xFF4F3422),
                                lineHeight = (screenHeight * 0.03f).value.sp
                            ),
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            singleLine = false,
                            maxLines = Int.MAX_VALUE
                        )
                    }
                    Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                    Text(
                        text = "감정 개선 조언",
                        modifier = Modifier.fillMaxWidth().padding(start = screenWidth * 0.05f),
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.025f).value.sp,
                        color = Color(0xFF4F3422)
                    )
                    Spacer(modifier = Modifier.height(screenHeight * 0.015f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenWidth * 0.05f)
                            .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = adviceText,
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenHeight * 0.023f).value.sp,
                            color = Color(0xFF4F3422),
                            lineHeight = (screenHeight * 0.03f).value.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(screenHeight * 0.08f))
                }
            }
        }

        Image(
            painter = painterResource(
                id = when (avgScore) {
                    in 0..19 -> R.drawable.ai_document_sad
                    in 20..39 -> R.drawable.ai_document_down
                    in 40..59 -> R.drawable.ai_document_okay
                    in 60..79 -> R.drawable.ai_document_happy
                    in 80..100 -> R.drawable.ai_document_great
                    else -> R.drawable.ai_document_okay
                }
            ),
            contentDescription = "Score Icon",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = imageWidth, height = imageHeight)
                .offset(y = imageHeight * 0.8f)
        )
    }
}
