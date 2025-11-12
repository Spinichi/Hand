// file: com/hand/hand/ui/admin/sections/AdminGroupRecordsSection.kt
package com.hand.hand.ui.admin.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.BrandFontFamily

@Composable
fun AdminGroupRecordsSection(
    horizontalPadding: Dp = 0.dp,
    // 왼쪽 카드 데이터
    sadCount: Int = 2,      // 중앙 큰 카운트
    downCount: Int = 5,
    happyCount: Int = 5,
    okayCount: Int = 5,
    greatCount: Int = 5,
    // 오른쪽 카드 데이터
    avgChangeCount: Int = 2,
    recentChangeName: String = "이희준",
) {
    val Orange = Color(0xFFEF8834)
    val ChipBg = Color(0xFFFFEEE2)

    // 제목(Row) 아래에서 섹션 내용이 시작되는 동일 고정 간격
    val sectionTopGap = 30.dp
    // 하단 2×2 그리드, 각 행 사이 간격
    val gridRowGap = 8.dp

    val alignNudge = 15.dp

    Column(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "그룹 기록",
            fontWeight = FontWeight.Bold,
            fontFamily = BrandFontFamily,
            fontSize = 16.sp,
            color = Color(0xFF4B2E1E)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ───────── 왼쪽 카드: 그룹 건강 분포 ─────────
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF9AB067))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.83f)
                        .padding(14.dp)
                ) {
                    // 헤더
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mini_graph),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "그룹 건강 분포",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily
                        )
                    }

                    // ⬇ 헤더 뒤 동일 간격
                    Spacer(Modifier.height(sectionTopGap))

                    // ⬅ 상단 영역(0.42) : 큰 이모지 + 숫자 (상단에서 바로 시작)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.42f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_mini_sad_white),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "${sadCount}명",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BrandFontFamily
                            )
                        }
                    }

                    // ⬅ 하단 영역(0.58) : 2×2 (Top 시작 + 고정 간격)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.58f)
                            .padding(top = alignNudge),
                        verticalArrangement = Arrangement.spacedBy(gridRowGap)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MoodCountDot(
                                iconRes = R.drawable.ic_mini_down_white,
                                text = "${downCount}명",
                                iconSize = 26.dp
                            )
                            MoodCountDot(
                                iconRes = R.drawable.ic_mini_okay_white_png,
                                text = "${okayCount}명",
                                iconSize = 26.dp
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MoodCountDot(
                                iconRes = R.drawable.ic_mini_happy_white,
                                text = "${happyCount}명",
                                iconSize = 26.dp
                            )
                            MoodCountDot(
                                iconRes = R.drawable.ic_mini_great_white_png,
                                text = "${greatCount}명",
                                iconSize = 26.dp
                            )
                        }
                    }
                }
            }

            // ───────── 오른쪽 카드: 그룹 통계 ─────────
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Orange)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.83f)
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 헤더(좌측 정렬)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_meta_people),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "그룹 통계",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily
                        )
                    }

                    // ⬇ 헤더 뒤 동일 간격 (왼쪽과 동일)
                    Spacer(Modifier.height(sectionTopGap))

                    // ➡ 상단 영역(0.42) : "평균 감정 변화 횟수"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.42f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "평균 감정 변화 횟수",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(10.dp)) // 라벨 ↔ 칩 간격
                        Surface(
                            modifier = Modifier.height(30.dp),
                            color = ChipBg,
                            contentColor = Orange,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${avgChangeCount} 회",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = BrandFontFamily,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // ➡ 하단 영역(0.58) : "최근 급격한 감정 변화자"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.58f)
                            .padding(top = alignNudge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "최근 급격한 감정 변화자",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BrandFontFamily,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.height(30.dp),
                            color = ChipBg,
                            contentColor = Orange,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recentChangeName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = BrandFontFamily,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodCountDot(
    iconRes: Int,
    text: String,
    iconSize: Dp = 22.dp,
    gap: Dp = 6.dp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(iconSize)
        )
        Spacer(Modifier.width(gap))
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = BrandFontFamily
        )
    }
}
