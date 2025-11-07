package com.hand.hand.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp



/**
 * 피그마 기준으로 정확히 맞추는 헤더
 * - edgeY: 좌우 모서리의 Y (dp)
 * - centerY: 중앙 최저 Y (dp)
 * - overhang: 좌/우를 살짝 넘겨 그려 모서리 틈새 방지
 */
@Composable
fun BrandWaveHeader(
    fillColor: Color,
    edgeY: Dp,
    centerY: Dp,
    overhang: Dp = 0.dp,
    height: Dp = centerY,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit) = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val edgeYpx = edgeY.toPx()
            val centerYpx = centerY.toPx()
            val oh = overhang.toPx()

            // 타원 사각형: 좌우 모서리(y=edgeY), 중앙 최저(y=centerY)
            val oval = Rect(
                left = -oh,
                top = edgeYpx,
                right = w + oh,
                bottom = centerYpx
            )

            val p = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, edgeYpx)
                // 오른쪽 → 왼쪽으로 180도 아크
                arcTo(oval, 0f, 180f, false)
                lineTo(0f, 0f)
                close()
            }
            drawPath(p, fillColor)
        }
        content()
    }
}
