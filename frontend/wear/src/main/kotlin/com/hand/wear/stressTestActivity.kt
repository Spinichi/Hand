package com.hand.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Scaffold
import com.hand.wear.components.BackgroundCircles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.hand.hand.R

class StressTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StressTestScreen()
        }
    }

    val KyonggiFont5 = FontFamily(Font(R.font.kyonggi_bold))

    @Composable
    fun StressTestScreen() {
        Scaffold {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F4F2)),
                contentAlignment = Alignment.Center
            ) {
                val screenHeight = this.maxHeight
                val screenWidth = this.maxWidth

                // 배경 원
                BackgroundCircles(screenWidth = screenWidth, screenHeight = screenHeight)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = screenHeight * 0.05f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {

                    Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                    // 애니메이션 진행률 상태
                    val animatedProgress = remember { Animatable(0f) }

                    LaunchedEffect(Unit) {
                        // 10초 뒤 다른 화면으로 이동
                        launch {
                            delay(10000)
                            // ⭐ 현재 스트레스 점수(인덱스)를 StressScoreActivity로 전달
                            val currentStressScore = com.mim.watch.services.SensorGatewayImpl.currentStressIndex
                            val intent = Intent(this@StressTestActivity, StressScoreActivity::class.java)
                            intent.putExtra("stressScore", currentStressScore)
                            startActivity(intent)
                            finish()
                        }

                        // 무한 반복 애니메이션
                        while (true) {
                            animatedProgress.snapTo(0f)
                            animatedProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 2000,
                                    easing = LinearEasing
                                )
                            )
                        }
                    }

                    // Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val widthPx = size.width
                        val heightPx = size.height

                        val points = listOf(
                            Offset(0f, heightPx * 0.8f),
                            Offset(widthPx * 0.15f, heightPx * 0.3f),
                            Offset(widthPx * 0.35f, heightPx * 0.55f),
                            Offset(widthPx * 0.55f, heightPx * 0.2f),
                            Offset(widthPx * 0.8f, heightPx * 0.6f),
                            Offset(widthPx, heightPx * 0.3f)
                        )

                        val path = Path()
                        if (points.size >= 2) {
                            path.moveTo(points[0].x, points[0].y)
                            for (i in 0 until points.size - 1) {
                                val p0 = if (i - 1 >= 0) points[i - 1] else points[i]
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val p3 = if (i + 2 < points.size) points[i + 2] else p2

                                for (t in 1..20) {
                                    val tt = t / 20f
                                    val x = 0.5f * ((-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * tt * tt * tt +
                                            (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * tt * tt +
                                            (-p0.x + p2.x) * tt +
                                            2 * p1.x)
                                    val y = 0.5f * ((-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * tt * tt * tt +
                                            (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * tt * tt +
                                            (-p0.y + p2.y) * tt +
                                            2 * p1.y)
                                    path.lineTo(x, y)
                                }
                            }
                        }

                        val drawPath = Path()
                        val measure = android.graphics.PathMeasure(path.asAndroidPath(), false)
                        measure.getSegment(
                            0f,
                            measure.length * animatedProgress.value,
                            drawPath.asAndroidPath(),
                            true
                        )

                        drawPath(
                            path = drawPath,
                            color = Color(0xFFFF0000),
                            style = Stroke(width = 10f, cap = StrokeCap.Round)
                        )
                    }

                    // 텍스트
                    Text(
                        text = "스트레스 측정 중...",
                        color = Color(0xFF4F3422),
                        fontSize = (screenHeight.value * 0.08).sp,
                        textAlign = TextAlign.Center,
                        fontFamily = KyonggiFont5,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }
            }
        }
    }
}
