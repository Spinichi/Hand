package com.hand.hand.care

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.home.BottomTab
import com.hand.hand.ui.home.CurvedBottomNavBar
import com.hand.hand.ui.home.HomeActivity  // ✅ 홈 이동용
import com.hand.hand.diary.DiaryHomeActivity // ✅ 글쓰기(다이어리 홈) 이동용
import com.hand.hand.AiDocument.PrivateAiDocumentHomeActivity // ✅ 다이어리 버튼 이동용
import com.hand.hand.ui.mypage.MyPageActivity

class CareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareScreen()
        }
    }
}

@Composable
fun CareScreen() {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val headerHeight = screenHeight * 0.25f
    val context = LocalContext.current

    val gridItems = listOf(
        "복식호흡 훈련" to "6분",
        "근육 이완 훈련" to "13분",
        "안전지대 연습" to null,
        "착지 연습" to null,
        "빛줄기 기법" to "8분",
        "바디스캔" to "17분",
        "봉인연습" to null,
        "자원강화" to null
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4F2))
    ) {
        // ✅ 상단 헤더
        CareHeader(
            onBackClick = {
                // 뒤로가기 → 홈으로 이동
                val intent = Intent(context, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(intent)
            }
        )

        // ✅ 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = headerHeight + 40.dp,
                    start = screenWidth * 0.05f,
                    end = screenWidth * 0.05f
                ),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "마음 완화법 리스트",
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color(0xFF4F3422)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(screenHeight * 0.015f)) {
                for (row in 0 until 4) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0 until 2) {
                            val index = row * 2 + col
                            if (index < gridItems.size) {
                                val (text, subText) = gridItems[index]
                                CareGridItem(
                                    text = text,
                                    subText = subText,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (text == "안전지대 연습") {
                                            context.startActivity(
                                                Intent(context, CareSafeZone1Activity::class.java)
                                            )
                                        }
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // ✅ 하단 곡선형 네비게이션 바
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            CurvedBottomNavBar(
                selectedTab =  BottomTab.None,
                onClickHome = {
                    // 홈 버튼 클릭 → HomeActivity 이동
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    context.startActivity(intent)
                },
                onClickWrite = {
                    // ✅ 글쓰기 버튼 클릭 → DiaryHomeActivity 이동
                    val intent = Intent(context, DiaryHomeActivity::class.java)
                    context.startActivity(intent)
                },
                onClickDiary = {
                    // ✅ 다이어리 버튼 클릭 → PrivateAiDocumentHomeActivity 이동
                    val intent = Intent(context, PrivateAiDocumentHomeActivity::class.java)
                    context.startActivity(intent)
                },
                onClickProfile = {
                    context.startActivity(Intent(context, MyPageActivity::class.java))
                },
                onClickCenter = {
                    // ✅ 중앙 버튼 클릭 → CareActivity로 이동
                    val intent = Intent(context, CareActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }
    }
}
