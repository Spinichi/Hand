package com.hand.hand.ui.home.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.hand.hand.R
import com.hand.hand.ui.theme.*
import androidx.compose.ui.unit.Dp


@Composable
fun MyRecordsSection(horizontalPadding: Dp = 0.dp,
                     moodChangeCount: Int = 2,   // ← 추가
                     diaryDoneCount: Int = 31,   // ← 추가
                     diaryTotal: Int = 365       // ← 추가 (기본 365)
                              ) {
    Column(
        Modifier.padding(horizontal = horizontalPadding),   // 16.dp → param
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("내 기록", fontWeight = FontWeight.Bold, fontFamily = BrandFontFamily, fontSize = 16.sp, color = Brown80)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── 오늘 감정 변화 ──
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF9AB067)) // 피그마 그린
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.83f)
                        .padding(14.dp)
                ) {
                    // 상단: 하트 + 라벨
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
                            .weight(0.42f),                           // ← 숫자 영역 비율
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

                    // 🔽 그래프 영역: 그대로 58% 유지 → 크기 변화 없음
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

            // ── 감정 다이어리 (기존 유지) ──
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEF8834)) // 피그마 오렌지
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.83f)
                        .padding(14.dp)
                ) {
                    // 상단: 연필 아이콘 + 라벨
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
                        Text(
                            text = "${diaryDoneCount}/${diaryTotal}",
                            color = Color.White,
                            fontSize = 24.sp,
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
                            painter = painterResource(R.drawable.ic_dot_frame),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth(0.78f)          // 좌하단 반응형 배치 유지
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
