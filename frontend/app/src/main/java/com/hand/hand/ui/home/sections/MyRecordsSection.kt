package com.hand.hand.ui.home.sections

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.diary.DiaryHomeActivity
import com.hand.hand.ui.theme.*

@Composable
fun MyRecordsSection(
    horizontalPadding: Dp = 0.dp,
    moodChangeCount: Int,
    diaryDoneCount: Int = 31,
    diaryTotal: Int = 365,

    // 🔥 추가된 부분
    exists: Boolean,
    riskScore: Double?,

    onMoodChangeClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        Modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "내 기록",
            fontWeight = FontWeight.Bold,
            fontFamily = BrandFontFamily,
            fontSize = 16.sp,
            color = Brown80
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 오늘 감정 변화 카드 ──
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onMoodChangeClick() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF9AB067))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.83f)
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mini_heart),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "오늘 감정 변화",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.42f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${moodChangeCount}회",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily
                        )
                        Spacer(Modifier.weight(1f))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.58f),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_graph),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(129f / 84f)
                                .padding(bottom = 1.dp)
                                .align(Alignment.BottomStart)
                        )
                    }
                }
            }

            // ── 감정 다이어리 카드 ──
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val intent = Intent(context, DiaryHomeActivity::class.java)
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (exists) Color(0xFF6B9BD1) else Color(0xFFEF8834)  // 작성 완료: 파란색, 작성 전: 주황색
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.83f)
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit_white),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "감정 다이어리",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.42f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(Modifier.weight(1f))

                        // 🔥 exists 값에 따라 표시되는 부분
                        if (exists) {
                            // 작성 완료: "오늘의 점수" + 점수
                            Text(
                                text = "오늘의 점수",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = BrandFontFamily
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${riskScore?.toInt()}점",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BrandFontFamily
                            )
                        } else {
                            // 작성 전
                            Text(
                                text = "다이어리 작성 전!",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BrandFontFamily
                            )
                        }

                        Spacer(Modifier.weight(1f))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.58f),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_dot_frame),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth(0.78f)
                                .aspectRatio(114f / 94f)
                                .align(Alignment.BottomStart)
                                .padding(bottom = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
