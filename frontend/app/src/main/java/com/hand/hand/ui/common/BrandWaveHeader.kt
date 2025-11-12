package com.hand.hand.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 자연스러운 곡선형 브랜드 헤더
 * - edgeY: 좌우 모서리의 높이 (dp)
 * - centerY: 중앙 최저점 (dp)
 * - overhang: 좌우 끝을 살짝 넘겨서 그려 틈새 방지
 */
@Composable
fun BrandWaveHeader(
    fillColor: Color,
    edgeY: Dp,
    centerY: Dp,
    overhang: Dp = 0.dp,
    // ✅ 높이를 기존 centerY보다 살짝 키움 (여유 공간 확보)
    height: Dp = centerY + 40.dp,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit) = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F4F2))
            .height(height),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val edgeYpx = edgeY.toPx()
            val centerYpx = centerY.toPx()
            val oh = overhang.toPx()

            val p = Path().apply {
                moveTo(-oh, 0f)
                lineTo(w + oh, 0f)
                lineTo(w + oh, edgeYpx)

                quadraticBezierTo(
                    w / 2, centerYpx,
                    -oh, edgeYpx
                )

                close()
            }

            drawPath(p, fillColor)
        }
        content()
    }
}
