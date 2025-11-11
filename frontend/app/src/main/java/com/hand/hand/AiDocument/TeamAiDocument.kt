package com.hand.hand.AiDocument

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
import androidx.compose.foundation.text.BasicTextField
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
import com.hand.hand.ui.admin.sections.GroupMember
import com.hand.hand.ui.model.OrgSource
import com.hand.hand.ui.theme.BrandFontFamily
import java.util.Calendar
import androidx.compose.material3.TextFieldDefaults
import java.time.format.TextStyle


class TeamAiDocumentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orgId = intent.getStringExtra("ORG_ID") ?: "multipampus"
        val memberName = intent.getStringExtra("MEMBER_NAME") ?: "멤버"
        val memberId = intent.getStringExtra("MEMBER_ID") ?: ""

        // 오늘 기준 최근 7일 날짜 계산
        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.DAY_OF_MONTH, -6)
        val sevenDaysAgo = cal.time

        setContent {
            TeamAiDocumentScreen(
                orgId = orgId,
                memberName = memberName,
                memberId = memberId,
                sevenDaysAgo = sevenDaysAgo,
                today = today,
                onBackClick = {
                    val intent = Intent(this, com.hand.hand.ui.admin.AdminHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun TeamAiDocumentScreen(
    orgId: String,
    memberName: String,
    memberId: String,
    sevenDaysAgo: java.util.Date,
    today: java.util.Date,
    onBackClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val backButtonSize: Dp = screenHeight * 0.06f
    val backButtonPaddingStart: Dp = screenWidth * 0.07f
    val backButtonPaddingTop: Dp = screenHeight * 0.05f

    val bottomBoxHeight: Dp = screenHeight * 0.8f
    val bottomBoxRadius: Dp = 30.dp

    val imageWidth: Dp = screenWidth * 0.25f
    val imageHeight: Dp = screenHeight * 0.15f

    // 멤버 week 점수 & 평균 계산
    val memberWeekScores = OrgSource.memberWeekScores(orgId, memberId)
    val avgScore = if (memberWeekScores.any { it != null }) {
        memberWeekScores.filterNotNull().average().toInt()
    } else 0
    val scores = memberWeekScores.map { it ?: 0 } // 그래프용

    val xLabels = listOf("Day1", "Day2", "Day3", "Day4", "Day5", "Day6", "Day7")

    // 감정 개선 조언 및 특이사항
    val adviceText = OrgSource.memberAdvice(orgId, memberId)
    val specialNote = OrgSource.memberSpecialNote(orgId, memberId)

    // 특이사항 입력 상태 초기값 = specialNote
    var noteText by rememberSaveable { mutableStateOf(specialNote) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFACA6E9))
    ) {
        // 백버튼
        Image(
            painter = painterResource(id = R.drawable.back_white_btn),
            contentDescription = "Back Button",
            modifier = Modifier
                .padding(start = backButtonPaddingStart, top = backButtonPaddingTop)
                .size(backButtonSize)
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
        )

        // 상단 멤버 이름
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

        // 하단 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomBoxHeight)
                .align(Alignment.BottomCenter)
                .background(
                    color = Color(0xFFF7F4F2),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = bottomBoxRadius,
                        topEnd = bottomBoxRadius
                    )
                )
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = imageHeight / 2)
            ) {
                // 감정 경향 그래프
                Text(
                    text = "감정 경향 그래프",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = screenWidth * 0.05f),
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EmotionLineChart(
                        scores = scores,
                        xLabels = xLabels,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(screenHeight * 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.08f))

                // 특이사항
                Text(
                    text = "특이사항",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = screenWidth * 0.05f),
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenHeight * 0.025f).value.sp,
                    color = Color(0xFF4F3422)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.015f))

                // 특이사항 입력창
                // 특이사항 입력창
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = screenWidth * 0.05f)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp) // 감정 개선 조언과 동일
                        )
                        .padding(vertical = 0.dp) // 감정 개선 조언과 동일
                ) {
                    TextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = {
                            Text(
                                text = "특이사항을 입력하세요",
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (screenHeight * 0.023f).value.sp,
                                color = Color.Gray
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = BrandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenHeight * 0.023f).value.sp,
                            color = Color(0xFF4F3422),
                            lineHeight = (screenHeight * 0.03f).value.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(), // 텍스트에 맞춰 높이 유동적으로
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = false,
                        maxLines = Int.MAX_VALUE // 제한 없이 줄 늘어나도록
                    )
                }




                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                // 감정 개선 조언
                Text(
                    text = "감정 개선 조언",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = screenWidth * 0.05f),
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
                        .background(
                            color = Color.White,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = adviceText,
                        fontFamily = BrandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenHeight * 0.023f).value.sp,
                        color = Color(0xFF4F3422),
                        lineHeight = (screenHeight * 0.03f).value.sp,
                    )
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.08f))
            }
        }

        // 점수 이미지
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
