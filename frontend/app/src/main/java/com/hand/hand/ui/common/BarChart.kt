package com.hand.hand.ui.common // 파일 위치에 맞는 패키지 선언

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.hand.ui.theme.Brown20 // 프로젝트의 색상 테마 import
import com.hand.hand.ui.theme.Brown40

/**
 * '오늘 감정 변화' 카드에 사용될 막대 그래프 Composable
 *
 * @param dataPoints 그래프에 표시할 데이터 리스트. 각 항목은 0.0(최소 높이)에서 1.0(최대 높이) 사이의 값을 가집니다.
 * @param barColor 막대의 색상
 * @param maxBarHeight 막대의 최대 높이
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    dataPoints: List<Float>,
    barColor: Color = Brown40,
    maxBarHeight: Dp = 60.dp // 그래프의 전체 최대 높이
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(maxBarHeight),
        horizontalArrangement = Arrangement.SpaceBetween, // 막대들을 균등한 간격으로 배치
        verticalAlignment = Alignment.Bottom // 막대들이 아래쪽을 기준으로 정렬되도록 함
    ) {
        dataPoints.forEach { dataPoint ->
            // dataPoint 값이 0.0 ~ 1.0 사이의 비율이라고 가정
            val barHeight = maxBarHeight * dataPoint.coerceIn(0f, 1f)

            // 높이가 0에 가까우면 아주 작은 점으로 표시하여 흔적을 남김
            val finalHeight = if (barHeight < 2.dp) 2.dp else barHeight
            val finalColor = if (dataPoint > 0.8f) barColor else Brown20 // 가장 높은 막대는 강조

            Box(
                modifier = Modifier
                    .weight(1f) // 모든 막대가 동일한 너비를 갖도록 함
                    .height(finalHeight)
                    .padding(horizontal = 2.dp) // 막대 사이의 최소 간격
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)) // 막대 위쪽을 둥글게
                    .background(finalColor)
            )
        }
    }
}
