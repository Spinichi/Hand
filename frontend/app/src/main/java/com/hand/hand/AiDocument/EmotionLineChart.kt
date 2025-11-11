import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun EmotionLineChart(
    scores: List<Int>,      // Y축 값: 0~100
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4F3422)
) {
    val padding = 16.dp
    Canvas(modifier = modifier.padding(vertical = padding)) {
        if (scores.isEmpty()) return@Canvas

        val widthPerPoint = size.width / (scores.size - 1)
        val heightScale = size.height / 100f  // 0~100 점수 기준

        val path = Path()
        scores.forEachIndexed { index, score ->
            val x = index * widthPerPoint
            val y = size.height - (score * heightScale)  // Canvas y축은 위가 0
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f) // <-- Stroke로 strokeWidth 전달
        )
    }
}
