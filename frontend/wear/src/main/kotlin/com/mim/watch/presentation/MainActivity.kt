package com.mim.watch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import com.mim.watch.healthdebug.HealthDebugManager

import android.os.VibrationEffect
import android.os.Vibrator

import android.content.Intent
import android.os.Build
import com.mim.watch.services.BioForegroundService

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import com.mim.watch.sensors.WristShakeTrigger
import com.mim.watch.sensors.SensorCollector
import kotlinx.coroutines.launch

// STRESS 계산
import com.mim.watch.core.measurement.SensorSample
import com.mim.watch.services.SensorGatewayImpl

// 10분 레코딩
import com.mim.watch.core.measurement.BioRecorder
import android.util.Log
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⭐ 화면 항상 켜짐 유지 (배터리 소모 주의)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // ⭐ Samsung Health SDK 연결 (권한 동의 UI를 위해 Activity 전달)
        // MainActivity에서 먼저 연결하여 사용자 동의를 받은 후 서비스 시작
        HealthDebugManager.connect(applicationContext, this)

        // 앱 최초 진입 시 포그라운드 서비스 기동(한 번만 떠도 계속 유지)
        val i = Intent(this, BioForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i)
        } else {
            startService(i)
        }

        setContent {
            var trackerDump by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

            // 흔들기 UI 상태 (Activity 안의 시각적 오버레이만)
            var shakeCount by remember { mutableStateOf(0) }
            var lastShakeAt by remember { mutableStateOf<Long?>(null) }
            var overlayVisible by remember { mutableStateOf(false) }
            var overlayText by remember { mutableStateOf("손목 제스처 인식!") }
            val scope = rememberCoroutineScope()

            // 걸음 표시용
            var stepsBlock by remember { mutableStateOf("(no step data yet)") }

            // STRESS 계산 배선
            val gateway = remember { SensorGatewayImpl(this@MainActivity) }
            var stressIndex by remember { mutableStateOf<Double?>(null) }
            var stressLevel by remember { mutableStateOf<Int?>(null) }
            var hrvSdnn by remember { mutableStateOf<Double?>(null) }
            var hrvRmssd by remember { mutableStateOf<Double?>(null) }

            // 스무딩용 EMA (Exponential Moving Average)
            var smoothedIndex by remember { mutableStateOf<Double?>(null) }

            // HealthDebugManager는 서비스에서 connect/start되어 있으므로 여기선 덤프만 구독
            LaunchedEffect(Unit) {
                while (true) {
                    trackerDump = HealthDebugManager.latestByTracker
                    delay(1000L)
                }
            }

            // 1초마다 걸음 UI 업데이트
            LaunchedEffect(Unit) {
                while (true) {
                    val total = SensorCollector.lastStepCount
                    val derived = SensorCollector.derivedStepCount
                    val lastTs = SensorCollector.lastStepTimestampMs

                    stepsBlock = buildString {
                        appendLine("total(step_counter): ${total?.toString() ?: "-"}")
                        appendLine("derived(step_detector): $derived")
                        append("last_step_ts(ms): ${lastTs?.toString() ?: "-"}")
                    }.trimEnd()

                    delay(1000L)
                }
            }

            // 1초마다 STRESS 계산
            LaunchedEffect(Unit) {
                while (true) {
                    val parsed = parseFromDump(HealthDebugManager.latestByTracker)
                    val sample = SensorSample(
                        timestampMs    = System.currentTimeMillis(),
                        heartRateBpm   = parsed.hrBpm,
                        ibiMsList      = parsed.ibiMsList,
                        objectTempC    = parsed.objTempC,
                        accelMagnitude = null,
                        accelX         = parsed.accelX,
                        accelY         = parsed.accelY,
                        accelZ         = parsed.accelZ,
                        totalSteps     = SensorCollector.lastStepCount?.toLong(),
                        lastStepAtMs   = SensorCollector.lastStepTimestampMs,
                        stepsPerMinute = SensorCollector.getStepsPerMinute()
                    )
                    val result = gateway.processRealtimeSample(sample)

                    // EMA 스무딩 적용 (알파=0.3: 부드럽게 변화)
                    smoothedIndex = if (smoothedIndex == null) {
                        result.stressIndex  // 첫 값
                    } else {
                        val alpha = 0.3
                        smoothedIndex!! * (1 - alpha) + result.stressIndex * alpha
                    }

                    stressIndex = smoothedIndex  // 스무딩된 값 사용
                    stressLevel = result.stressLevel
                    hrvSdnn = result.sdnn
                    hrvRmssd = result.rmssd

                    // 디버깅: 원본과 스무딩된 값 비교
                    Log.d("StressDebug", "HR=${parsed.hrBpm} SDNN=${result.sdnn} RMSSD=${result.rmssd} → Raw=${result.stressIndex} Smoothed=${"%.1f".format(smoothedIndex)}")

                    delay(1000L)
                }
            }

            // (권장) 10분 레코딩은 서비스에서 돌리는 게 안정적이지만
            // UI 데모 목적이면 아래 로직 유지 가능
            LaunchedEffect(Unit) {
                BioRecorder.start()
                Log.d("BioRecorder", "Recording started (10min window)")
                val start = System.currentTimeMillis()
                val durationMs = 10 * 60 * 1000L

                while (isActive && System.currentTimeMillis() - start <= durationMs) {
                    val parsed = parseFromDump(HealthDebugManager.latestByTracker)

                    BioRecorder.add(
                        BioRecorder.Row(
                            tsMs = System.currentTimeMillis(),
                            hrBpm = parsed.hrBpm,
                            ibiMsList = parsed.ibiMsList,
                            objTempC  = parsed.objTempC,
                            totalSteps = SensorCollector.lastStepCount?.toLong(),
                            lastStepAtMs = SensorCollector.lastStepTimestampMs
                        )
                    )
                    delay(1000L)
                }

                BioRecorder.stop()
                val tail = BioRecorder.snapshot().takeLast(5)
                Log.d("BioRecorder", "Last5:\n" + tail.joinToString("\n") {
                    "ts=${it.tsMs}, hr=${it.hrBpm}, ibiN=${it.ibiMsList?.size ?: 0}, temp=${it.objTempC}, steps=${it.totalSteps}"
                })
            }

            // ── UI + 오버레이
            Box(modifier = Modifier.fillMaxSize()) {
                DebugScreen(
                    trackerDump = trackerDump + mapOf(
                        "STEPS" to stepsBlock,
                        "STRESS" to buildString {
                            if (stressIndex == null) append("(no stress data yet)")
                            else {
                                appendLine("index(0~100): ${"%.1f".format(stressIndex)}")
                                appendLine("level(1~5): $stressLevel")
                                appendLine("SDNN: ${hrvSdnn?.let { "%.1f".format(it) } ?: "-"} ms")
                                append("RMSSD: ${hrvRmssd?.let { "%.1f".format(it) } ?: "-"} ms")
                            }
                        }.trimEnd()
                    )
                )

                AnimatedVisibility(
                    visible = overlayVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = overlayText,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                Color(0xAA000000),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.caption1
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 걸음 수집(앱이 떠 있을 때 UI용; 서비스는 백그라운드 동작)
        SensorCollector.start(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 서비스가 계속 돌기 때문에 여기서 stopAll()은 호출하지 않음
        // SensorCollector.stop()도 UI 종료 시 굳이 강제하지 않아도 무방
    }
}

/**
 * 기존 DebugScreen: 그대로 사용 + "STRESS" 허용
 */
@Composable
fun DebugScreen(trackerDump: Map<String, String>) {
    MaterialTheme {
        val scrollState = rememberScrollState()

        val filteredDump = remember(trackerDump) {
            val allowTrackers = setOf(
                "HEART_RATE_CONTINUOUS", "HEART_RATE", "HEART",
                "ACCELEROMETER_CONTINUOUS", "ACCELEROMETER",
                "SKIN_TEMPERATURE_CONTINUOUS", "SKIN_TEMPERATURE_ON_DEMAND", "SKIN_TEMPERATURE",
                "ECG_ON_DEMAND", "ECG",
                "STEPS", "STRESS"
            )

            fun allowedPrefixesFor(section: String): Set<String> = when {
                section.contains("HEART", true) -> setOf("TS:", "HR:", "IBI(MS):", "HR_Q:", "IBI_Q:")
                section.contains("ACCELEROMETER", true) -> setOf("TS:", "ACCEL_X:", "ACCEL_Y:", "ACCEL_Z:")
                section.contains("SKIN_TEMPERATURE", true) -> setOf("TS:", "AmbTemp(C):", "ObjTemp(C):", "TEMP_Q:")
                section.contains("ECG", true) -> setOf("TS:", "ECG(mV):", "ECG_SEQ:", "ECG_MAX:", "ECG_MIN:", "ECG_LEAD_OFF:")
                section.contains("STEPS", true) -> emptySet()
                section.contains("STRESS", true) -> emptySet()
                else -> emptySet()
            }

            trackerDump
                .filter { (k, _) ->
                    val base = k.substringAfter("DISCOVERY:", k)
                    allowTrackers.any { base.contains(it, ignoreCase = true) } || k == "STEPS" || k == "STRESS"
                }
                .mapValues { (k, dump) ->
                    if (k.equals("STEPS", true) || k.equals("STRESS", true)) return@mapValues dump

                    val lines = dump.lineSequence().toList()
                    val (header, restStartIdx) = if (lines.isNotEmpty() && lines[0].startsWith("[")) {
                        lines[0] to 1
                    } else null to 0
                    val sectionName = header?.removePrefix("[")?.removeSuffix("]") ?: ""
                    val prefixes = allowedPrefixesFor(sectionName.ifEmpty { "" })

                    buildString {
                        if (header != null) appendLine(header)
                        lines.drop(restStartIdx).forEach { line ->
                            val trimmed = line.trimStart()
                            if (prefixes.isEmpty() || prefixes.any { trimmed.startsWith(it) }) {
                                appendLine(line)
                            }
                        }
                    }.trimEnd()
                }
                .filterValues { it.isNotBlank() }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(15.dp)
        ) {
            if (filteredDump.isEmpty()) {
                Text(color = MaterialTheme.colors.primary, text = "no tracker data yet...")
                return@Column
            }

            filteredDump.forEach { (trackerName, dumpText) ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp)
                ) {
                    Text(color = MaterialTheme.colors.primary, text = "───", style = MaterialTheme.typography.caption2)
                    Text(color = MaterialTheme.colors.primary, text = "[${shortName(trackerName)}]", style = MaterialTheme.typography.title3)
                    Text(color = MaterialTheme.colors.primary, text = dumpText.trim(), style = MaterialTheme.typography.caption1)
                    Text(color = MaterialTheme.colors.primary, text = "───", style = MaterialTheme.typography.caption2)
                }
            }
        }
    }
}

private fun shortName(name: String): String {
    return when {
        name.contains("HEART_RATE_CONTINUOUS", true) -> "HR_CONT"
        name.contains("HEART_RATE", true)            -> "HEART"
        name.contains("ACCELEROMETER_CONTINUOUS", true) -> "ACCEL"
        name.contains("ACCELEROMETER", true)         -> "ACCEL"
        name.contains("SKIN_TEMPERATURE_CONTINUOUS", true) -> "SKIN_TEMP"
        name.contains("SKIN_TEMPERATURE_ON_DEMAND", true)  -> "SKIN_TEMP"
        name.contains("SKIN_TEMPERATURE", true)      -> "SKIN_TEMP"
        name.contains("DISCOVERY:", true)            -> name.removePrefix("DISCOVERY:")
        name.contains("PPG_GREEN", true)             -> "PPG_GRN"
        name.contains("PPG_RED", true)               -> "PPG_RED"
        name.contains("PPG_IR", true)                -> "PPG_IR"
        name.contains("EDA", true)                   -> "EDA"
        name.contains("ECG", true)                   -> "ECG"
        name.equals("STEPS", true)                   -> "STEPS"
        name.equals("STRESS", true)                  -> "STRESS"
        else                                         -> name
    }
}

// ───────────────────────────────────────────────────────────────
// HR/IBI/온도/가속도 파싱
// ───────────────────────────────────────────────────────────────
private data class ParsedVitals(
    val hrBpm: Double?,
    val ibiMsList: List<Double>?,
    val objTempC: Double?,
    val accelX: Double?,
    val accelY: Double?,
    val accelZ: Double?
)

private fun parseFromDump(dump: Map<String, String>): ParsedVitals {
    var hr: Double? = null
    var ibi: List<Double>? = null
    var objTemp: Double? = null
    var accelX: Double? = null
    var accelY: Double? = null
    var accelZ: Double? = null

    dump.entries.firstOrNull { (k, _) -> k.contains("HEART", ignoreCase = true) }?.let { (_, v) ->
        Regex("""HR:\s*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
            .find(v)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.let { hr = it }

        Regex("""IBI\(ms\):\s*(.*)""", RegexOption.IGNORE_CASE)
            .find(v)?.groupValues?.getOrNull(1)?.let { tail ->
                val nums = Regex("""[-+]?\d+(?:\.\d+)?""").findAll(tail).map { it.value.toDouble() }.toList()
                ibi = if (nums.size >= 2) nums else null
            }
    }

    dump.entries.firstOrNull { (k, _) -> k.contains("SKIN_TEMPERATURE", ignoreCase = true) }?.let { (_, v) ->
        Regex("""ObjTemp\(C\):\s*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
            .find(v)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.let { objTemp = it }
    }

    dump.entries.firstOrNull { (k, _) -> k.contains("ACCELEROMETER", ignoreCase = true) }?.let { (_, v) ->
        Regex("""ACCEL_X:\s*([-+]?[0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
            .find(v)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.let { accelX = it }
        Regex("""ACCEL_Y:\s*([-+]?[0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
            .find(v)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.let { accelY = it }
        Regex("""ACCEL_Z:\s*([-+]?[0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
            .find(v)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.let { accelZ = it }
    }

    return ParsedVitals(hrBpm = hr, ibiMsList = ibi, objTempC = objTemp, accelX = accelX, accelY = accelY, accelZ = accelZ)
}
