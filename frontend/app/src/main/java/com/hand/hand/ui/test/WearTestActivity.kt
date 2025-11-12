package com.hand.hand.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.wear.WearDataReceiver

/**
 * 워치 데이터 수신 테스트 화면
 * - 실시간으로 워치에서 받은 데이터 표시
 * - 안드로이드 스튜디오 없이도 확인 가능
 */
class WearTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearTestScreen()
        }
    }
}

@Composable
fun WearTestScreen() {
    val lastData by WearDataReceiver.lastReceivedData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "🔗 워치 데이터 수신 테스트",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (lastData == null) {
            Text(
                text = "⏳ 워치 데이터 대기 중...\n\n워치 앱이 실행 중인지 확인하세요.",
                color = Color.Yellow,
                fontSize = 14.sp
            )
        } else {
            val data = lastData!!

            Text(
                text = "✅ 데이터 수신 성공",
                color = Color.Green,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            DataRow("타임스탬프", "${data.timestampMs}")
            DataRow("심박수 (HR)", "${data.heartRate ?: "N/A"} bpm")
            DataRow("HRV SDNN", data.hrvSdnn?.let { "%.1f".format(it) } ?: "N/A")
            DataRow("HRV RMSSD", data.hrvRmssd?.let { "%.1f".format(it) } ?: "N/A")
            DataRow("피부 온도", "${data.objectTemp ?: "N/A"}°C")
            DataRow("주변 온도", "${data.ambientTemp ?: "N/A"}°C")
            DataRow("가속도 X", "${data.accelX ?: "N/A"} m/s²")
            DataRow("가속도 Y", "${data.accelY ?: "N/A"} m/s²")
            DataRow("가속도 Z", "${data.accelZ ?: "N/A"} m/s²")
            DataRow("움직임 강도", data.movementIntensity?.let { "%.2f".format(it) } ?: "N/A")
            DataRow("스트레스 지수", data.stressIndex?.let { "%.1f".format(it) } ?: "N/A")
            DataRow("스트레스 레벨", "${data.stressLevel ?: "N/A"}")
            DataRow("걸음 수", "${data.totalSteps ?: "N/A"}")
            DataRow("분당 걸음수", "${data.stepsPerMinute ?: "N/A"}")
            DataRow("⚠️ 이상치 여부", if (data.isAnomaly) "🔴 이상" else "🟢 정상")
        }
    }
}

@Composable
fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            color = Color(0xFFBBBBBB),
            fontSize = 14.sp,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}