package com.mim.watch.services

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import com.mim.watch.healthdebug.HealthDebugManager
import com.mim.watch.presentation.MainActivity
import com.mim.watch.sensors.WristShakeTrigger
import android.util.Log
import com.mim.watch.data.model.BioSample
import com.mim.watch.sensors.SensorCollector
import android.provider.Settings

class BioForegroundService : Service() {

    companion object {
        private const val TAG = "BioFG"
        private const val CHANNEL_ID = "sensor_foreground"
        private const val CHANNEL_NAME = "Sensor Tracking"
        private const val NOTIF_ID = 1001
        const val ACTION_STOP = "com.mim.watch.action.STOP_FOREGROUND"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val created = AtomicBoolean(false)
    private var wakeLock: PowerManager.WakeLock? = null

    // ⭐ Phone 전송용
    private lateinit var messageSender: WearMessageSender

    override fun onCreate() {
        super.onCreate()
        if (created.compareAndSet(false, true)) {
            startAsForeground()
            acquireShortWakeLock()

            // ⭐ Device ID 설정 & MessageSender 초기화
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            DataBufferManager.setDeviceId(deviceId)
            messageSender = WearMessageSender(applicationContext)

            // 1) Health SDK 연결 (Activity 없이)
            HealthDebugManager.connect(applicationContext, null)

            // 2) 백그라운드에서도 센서가 실제로 흐르도록 트래커 시작
            //    ⚠️ 개별 start* 함수가 없으므로, 존재하는 API만 호출
            try {
                HealthDebugManager.startAllTrackers()
            } catch (e: Throwable) {
                Log.e(TAG, "startAllTrackers() failed", e)
            }

            // 3) 실시간 샘플 콜백 → 10초 버퍼링 → Phone 전송
            HealthDebugManager.onSample = { ts, hr, ibi, move, extras ->
                Log.d(TAG, "sample ts=$ts hr=$hr ibi=$ibi move=$move")

                // BioSample 생성
                val sample = BioSample(
                    timestampMs = ts,
                    heartRateBpm = hr,
                    ibiMsList = ibi?.let { listOf(it) },  // Float? → List<Float>?
                    skinTempC = null,  // TODO: 온도 데이터 추가
                    movementIndex = move,
                    totalSteps = SensorCollector.lastStepCount?.toLong(),
                    stressIndex = null,  // TODO: 스트레스 지수 추가
                    stressLevel = null
                )

                // 버퍼에 추가 → 10개 모이면 배치 반환
                val batch = DataBufferManager.addSample(sample)

                // 배치가 준비되면 Phone으로 전송
                batch?.let {
                    scope.launch {
                        val success = messageSender.sendBatch(it)
                        if (success) {
                            Log.d(TAG, "✅ Batch sent: ${it.samples.size} samples")
                        } else {
                            Log.w(TAG, "❌ Failed to send batch")
                        }
                    }
                }
            }

            // 4) 손목 제스처 트리거를 서비스에서 상시 구동 → 앱 자동 실행
            startWristTrigger()

            // Heartbeat 로그 (확인용)
            scope.launch {
                while (isActive) {
                    Log.d(TAG, "heartbeat: service alive")
                    delay(30_000L)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        // 서비스가 죽더라도 시스템이 재시작하도록
        return START_STICKY
    }

    // 태스크가 스와이프로 제거돼도 다시 살리기 (신규 파일 없이 자가 재기동)
    override fun onTaskRemoved(rootIntent: Intent?) {
        scheduleSelfRestart(delayMs = 3000L)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        try {
            HealthDebugManager.onSample = null
            HealthDebugManager.stopAll()
        } catch (_: Throwable) { /* no-op */ }

        stopWristTrigger()

        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null

        // 예기치 않게 종료될 경우를 대비해 재기동 예약
        scheduleSelfRestart(delayMs = 3000L)

        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ────────────────────────────────────────────────────────────
    // 내부 유틸
    // ────────────────────────────────────────────────────────────

    private fun startAsForeground() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(ch)
        }

        val n: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("생체 데이터 수집 중")
            .setContentText("화면이 꺼져도 센서를 계속 기록합니다.")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, n)
    }

    private fun acquireShortWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mim:bio-short")
            wl.setReferenceCounted(false)
            wl.acquire(30_000L) // 초기 연결 안정화
            wakeLock = wl
        } catch (_: Throwable) { /* optional */ }
    }

    private fun startWristTrigger() {
        val vibrator = getSystemService(Vibrator::class.java)

        WristShakeTrigger.start(
            context = this,
            config = WristShakeTrigger.Config(
                accelThreshold = 11.5f,
                minAlternations = 3,
                windowMs = 600L,
                cooldownMs = 2000L,
                useGyroAssist = true,
                gyroThreshold = 2.0f,
                signThreshold = 0.5f
            )
        ) {
            // 제스처 감지 시 진동 & 액티비티 실행
            try {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(160, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } catch (_: Throwable) {}

            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
            }
            startActivity(intent)
        }
    }

    private fun stopWristTrigger() {
        try {
            WristShakeTrigger.stop()
        } catch (_: Throwable) { /* no-op */ }
    }

    // 신규 파일 없이 서비스 자체를 예약 재시작 (AlarmManager 사용)
    private fun scheduleSelfRestart(delayMs: Long) {
        try {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(applicationContext, BioForegroundService::class.java)
            val flags = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                else -> PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(applicationContext, 0, intent, flags)
            } else {
                PendingIntent.getService(applicationContext, 0, intent, flags)
            }
            val triggerAt = System.currentTimeMillis() + delayMs
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
            Log.d(TAG, "scheduleSelfRestart in ${delayMs}ms")
        } catch (e: Throwable) {
            Log.w(TAG, "scheduleSelfRestart failed", e)
        }
    }
}
