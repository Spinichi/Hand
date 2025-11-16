package com.hand.hand.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.hand.hand.fcm.FCMTokenManager
import com.hand.hand.wear.WearListenerForegroundService

class HomeActivity : ComponentActivity() {

    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 2000 // 2초

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

        // 뒤로가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < backPressInterval) {
                    // 2초 이내에 다시 눌렀으면 앱 종료
                    finish()
                } else {
                    // 처음 눌렀을 때 Toast 표시
                    backPressedTime = currentTime
                    Toast.makeText(this@HomeActivity, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // ⭐ 로그인 후 HomeActivity 진입 시 Wear 데이터 수신 서비스 시작
        requestPermissionsAndStartService()

        // ⭐ FCM 초기화 (알림 권한이 이미 요청되므로 바로 실행)
        initializeFCM()

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
                Log.d("HomeActivity", "✅ Active Baseline found: version=${baseline.version}, updatedAt=${baseline.updatedAt}")

                // ⭐ Baseline 만료 체크 (30일 이상 지났으면 재계산)
                if (isBaselineExpired(baseline.updatedAt)) {
                    Log.w("HomeActivity", "⚠️ Baseline is expired (older than 30 days), recalculating with 30-day data...")
                    calculateBaseline(days = 30)  // 30일치 데이터로 재계산
                } else {
                    sendBaselineToWatch(baseline)
                }
            },
            onNotFound = {
                Log.w("HomeActivity", "⚠️ No active Baseline, attempting to calculate with 3-day data...")
                calculateBaseline(days = 3)  // 첫 생성은 3일치
            },
            onFailure = { error ->
                Log.e("HomeActivity", "❌ Failed to fetch Baseline: ${error.message}")
            }
        )
    }

    /**
     * Baseline이 만료되었는지 확인 (30일 기준)
     * @param updatedAt Baseline의 마지막 업데이트 시간 (ISO-8601 문자열)
     * @return 30일 이상 지났으면 true
     */
    private fun isBaselineExpired(updatedAt: String?): Boolean {
        if (updatedAt == null) return true

        return try {
            // SimpleDateFormat 사용 (API 24 호환)
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val updatedDate = format.parse(updatedAt) ?: return true
            val now = java.util.Date()

            val diffMs = now.time - updatedDate.time
            val daysDiff = diffMs / (1000 * 60 * 60 * 24)  // ms -> days

            Log.d("HomeActivity", "📅 Baseline age: $daysDiff days")
            daysDiff >= 30  // 30일 이상이면 만료
        } catch (e: Exception) {
            Log.e("HomeActivity", "❌ Failed to parse updatedAt: $updatedAt", e)
            true  // 파싱 실패하면 만료로 간주
        }
    }

    private fun calculateBaseline(days: Int = 3) {
        Log.d("HomeActivity", "📊 Calculating Baseline with $days-day data...")
        com.hand.hand.api.Baseline.BaselineManager.calculateBaseline(
            days = days,
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

    /**
     * FCM 초기화 및 토큰 등록
     */
    private fun initializeFCM() {
        // 알림 권한 확인 (이미 requestPermissionsAndStartService에서 요청함)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("HomeActivity", "⚠️ POST_NOTIFICATIONS permission not granted, FCM may not work")
                return
            }
        }

        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("HomeActivity", "❌ Failed to get FCM token", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("HomeActivity", "✅ FCM Token: ${token.take(20)}...")

            // 1. 로컬에 저장
            FCMTokenManager.saveToken(this, token)

            // 2. 백엔드에 등록
            com.hand.hand.api.Notification.NotificationManager.registerToken(
                deviceToken = token,
                onSuccess = {
                    Log.d("HomeActivity", "✅ FCM token registered to backend")
                },
                onFailure = { error ->
                    Log.e("HomeActivity", "❌ Failed to register FCM token: ${error.message}")
                }
            )
        }
    }
}