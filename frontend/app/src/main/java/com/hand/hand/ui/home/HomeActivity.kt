package com.hand.hand.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.hand.hand.wear.WearListenerForegroundService

class HomeActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Log.d("HomeActivity", "✅ All permissions granted")
            startWearListenerService()
        } else {
            Log.e("HomeActivity", "❌ Permissions denied: ${permissions.filter { !it.value }}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⭐ 로그인 후 HomeActivity 진입 시 Wear 데이터 수신 서비스 시작
        requestPermissionsAndStartService()

        setContent { HomeScreen() }
    }

    private fun requestPermissionsAndStartService() {
        val requiredPermissions = mutableListOf<String>()

        // Android 12+ (API 31+): Bluetooth 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        // Android 13+ (API 33+): 알림 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 권한이 이미 허용되었는지 확인
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            // 모든 권한이 이미 허용됨 → 바로 서비스 시작
            startWearListenerService()
        } else {
            // 권한 요청
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun startWearListenerService() {
        try {
            val intent = Intent(this, WearListenerForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Log.d("HomeActivity", "✅ WearListenerForegroundService started")

            // ⭐ Baseline 조회 및 워치로 전송
            fetchAndSyncBaseline()

        } catch (e: Exception) {
            Log.e("HomeActivity", "❌ Failed to start WearListenerService", e)
        }
    }

    /**
     * Baseline 조회/계산/워치 전송 로직
     * 1. 활성 Baseline 조회 → 있으면 워치로 전송
     * 2. 없으면 Baseline 계산 → 성공하면 워치로 전송
     * 3. 계산 실패 (데이터 부족) → 하드코딩 값 사용 (워치 기본값)
     */
    private fun fetchAndSyncBaseline() {
        com.hand.hand.api.Baseline.BaselineManager.getActiveBaseline(
            onSuccess = { baseline ->
                Log.d("HomeActivity", "✅ Active Baseline found: version=${baseline.version}")
                sendBaselineToWatch(baseline)
            },
            onNotFound = {
                Log.w("HomeActivity", "⚠️ No active Baseline, attempting to calculate...")
                calculateBaseline()
            },
            onFailure = { error ->
                Log.e("HomeActivity", "❌ Failed to fetch Baseline: ${error.message}")
            }
        )
    }

    private fun calculateBaseline() {
        com.hand.hand.api.Baseline.BaselineManager.calculateBaseline(
            days = 3,
            onSuccess = { baseline ->
                Log.d("HomeActivity", "✅ Baseline calculated: version=${baseline.version}, count=${baseline.measurementCount}")
                sendBaselineToWatch(baseline)
            },
            onInsufficientData = {
                Log.w("HomeActivity", "⚠️ Insufficient data for Baseline calculation (< 3 days)")
                Log.d("HomeActivity", "📊 Watch will use hardcoded default values")
            },
            onFailure = { error ->
                Log.e("HomeActivity", "❌ Failed to calculate Baseline: ${error.message}")
            }
        )
    }

    private fun sendBaselineToWatch(baseline: com.hand.hand.api.Baseline.BaselineResponse) {
        try {
            // WearListenerForegroundService의 static 메소드 호출
            WearListenerForegroundService.sendBaseline(baseline)
            Log.d("HomeActivity", "📤 Baseline sent to watch: version=${baseline.version}")
        } catch (e: Exception) {
            Log.e("HomeActivity", "❌ Failed to send Baseline to watch", e)
        }
    }
}