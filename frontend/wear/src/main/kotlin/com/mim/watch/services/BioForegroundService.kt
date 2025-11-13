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
import com.hand.wear.WearHomeActivity
import com.mim.watch.sensors.WristShakeTrigger
import android.util.Log
import com.mim.watch.data.model.BioSample
import com.mim.watch.sensors.SensorCollector
import android.provider.Settings
import com.mim.watch.services.SensorGatewayImpl
import com.mim.watch.core.measurement.SensorSample
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.hand.wear.BeforeRelaxActivity

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

    // ⭐ Stress 계산용
    private lateinit var stressGateway: SensorGatewayImpl

    // ⭐ IBI 버퍼 (스트레스 계산용 - 최근 10개 저장)
    private val ibiBuffer = mutableListOf<Double>()

    // ⭐ Phone으로부터 메시지 수신
    private lateinit var messageClient: MessageClient
    private val messageListener = MessageClient.OnMessageReceivedListener { messageEvent ->
        val path = messageEvent.path
        val command = String(messageEvent.data)
        Log.d(TAG, "📩 Message received from phone: path=$path, command=$command")

        // ⭐ /relief/command 경로만 처리
        if (path == "/relief/command" && command == "START_RELIEF") {
            Log.d(TAG, "🎯 Starting relief activity automatically")
            startBeforeRelaxActivity(isAutomatic = true)
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (created.compareAndSet(false, true)) {
            startAsForeground()
            acquireShortWakeLock()

            // ⭐ Device ID 설정 & MessageSender 초기화
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            DataBufferManager.setDeviceId(deviceId)
            messageSender = WearMessageSender(applicationContext)

            // ⭐ MessageClient 초기화 (Phone으로부터 명령 수신)
            messageClient = Wearable.getMessageClient(applicationContext)
            messageClient.addListener(messageListener)
            Log.d(TAG, "✅ MessageClient listener registered")

            // ⭐ Stress 계산 Gateway 초기화
            stressGateway = SensorGatewayImpl(applicationContext)

            // ⭐ 걸음 수 센서 시작
            SensorCollector.start(applicationContext)
            Log.d(TAG, "✅ SensorCollector started")

            // 1) Health SDK 연결 (Activity 없이)
            // ✅ connect() 성공 시 자동으로 센서 시작 (onConnectionSuccess 콜백)
            HealthDebugManager.connect(applicationContext, null)

            // 3) 실시간 샘플 콜백 → 스트레스 계산 → 10초 버퍼링 → Phone 전송
            HealthDebugManager.onSample = { ts, hr, ibi, move, extras ->
                // ⭐ 디버깅: extras 내용 확인
                Log.d(TAG, "onSample extras keys: ${extras.keys}")

                // ⭐ extras에서 센서 데이터 추출 (extras에 이미 모든 데이터가 병합되어 있음)
                val objectTemp = (extras["objectTemp"] as? Number)?.toFloat()
                val ambientTemp = (extras["ambientTemp"] as? Number)?.toFloat()
                val accelX = (extras["accelX"] as? Number)?.toFloat()
                val accelY = (extras["accelY"] as? Number)?.toFloat()
                val accelZ = (extras["accelZ"] as? Number)?.toFloat()
                val movementIntensity = (extras["movementIntensity"] as? Number)?.toFloat()

                // ⭐ 디버깅: 추출된 값 확인
                Log.d(TAG, "Extracted: accelX=$accelX, accelY=$accelY, accelZ=$accelZ, movement=$movementIntensity")

                // ⭐ IBI 버퍼에 추가 (최근 10개 유지)
                ibi?.let {
                    synchronized(ibiBuffer) {
                        ibiBuffer.add(it.toDouble())
                        if (ibiBuffer.size > 10) {
                            ibiBuffer.removeAt(0)
                        }
                    }
                }

                // ⭐ 스트레스 실시간 계산 (coroutine에서 실행)
                scope.launch {
                    // IBI 버퍼 복사 (2개 이상일 때만 사용)
                    val ibiList = synchronized(ibiBuffer) {
                        if (ibiBuffer.size >= 2) ibiBuffer.toList() else null
                    }

                    val sensorSample = SensorSample(
                        timestampMs = ts,
                        heartRateBpm = hr?.toDouble(),
                        ibiMsList = ibiList,  // ⭐ 여러 개의 IBI 사용
                        objectTempC = objectTemp?.toDouble(),
                        accelMagnitude = movementIntensity?.toDouble(),
                        accelX = accelX?.toDouble(),
                        accelY = accelY?.toDouble(),
                        accelZ = accelZ?.toDouble(),
                        totalSteps = SensorCollector.lastStepCount?.toLong(),
                        lastStepAtMs = SensorCollector.lastStepTimestampMs,
                        stepsPerMinute = SensorCollector.getStepsPerMinute()
                    )
                    val stressResult = stressGateway.processRealtimeSample(sensorSample)

                    Log.d(TAG, "sample ts=$ts hr=$hr temp=$objectTemp accelX=$accelX stress=${stressResult.stressIndex} level=${stressResult.stressLevel}")

                    // ⭐ BioSample 생성 (isAnomaly는 기본값 false)
                    val sample = BioSample(
                        timestampMs = ts,
                        heartRate = hr,
                        hrvSdnn = stressResult.sdnn,
                        hrvRmssd = stressResult.rmssd,
                        objectTemp = objectTemp,
                        ambientTemp = ambientTemp,
                        accelX = accelX,
                        accelY = accelY,
                        accelZ = accelZ,
                        movementIntensity = movementIntensity,
                        stressIndex = stressResult.stressIndex,
                        stressLevel = stressResult.stressLevel,
                        totalSteps = SensorCollector.lastStepCount?.toLong(),
                        stepsPerMinute = SensorCollector.getStepsPerMinute(),
                        isAnomaly = false  // 기본값 false, 대표 샘플 생성 시 재설정
                    )

                    // 버퍼에 추가 → 10개 모이면 대표 샘플 1개 반환 (isAnomaly 포함)
                    val representativeSample = DataBufferManager.addSample(sample)

                    // 대표 샘플이 준비되면 Phone으로 전송
                    representativeSample?.let { repSample ->
                        Log.d(TAG, "✅ Representative sample: stressLevel=${repSample.stressLevel} isAnomaly=${repSample.isAnomaly}")

                        // Phone으로 대표 샘플 1개 전송
                        val success = messageSender.sendSample(repSample)
                        if (success) {
                            Log.d(TAG, "✅ Sample sent to Phone")
                        } else {
                            Log.w(TAG, "❌ Failed to send sample")
                        }

                        // ⭐ 이상치 감지 시 Phone으로 알림 전송
                        if (repSample.isAnomaly) {
                            Log.w(TAG, "🚨 Anomaly detected! Sending alert to Phone")
                            scope.launch {
                                messageSender.sendAnomalyAlert(
                                    stressLevel = repSample.stressLevel ?: 0,
                                    stressIndex = repSample.stressIndex ?: 0.0
                                )
                            }
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
            val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mim:bio-tracking")
            wl.setReferenceCounted(false)
            wl.acquire(10 * 60 * 1000L) // ⭐ 10분 고정 - 화면 켜면 자동 초기화
            wakeLock = wl
            Log.d(TAG, "✅ WakeLock acquired (10min)")
        } catch (e: Throwable) {
            Log.e(TAG, "❌ Failed to acquire WakeLock", e)
        }
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

            val intent = Intent(this, WearHomeActivity::class.java).apply {
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

    /**
     * BeforeRelaxActivity 시작 (자동 또는 수동)
     * @param isAutomatic true면 자동 실행 (이상치 감지), false면 수동 실행 (버튼 클릭)
     */
    private fun startBeforeRelaxActivity(isAutomatic: Boolean) {
        try {
            // 진동
            val vibrator = getSystemService(Vibrator::class.java)
            vibrator?.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )

            val intent = Intent(this, BeforeRelaxActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                // ⭐ 자동/수동 구분 전달
                putExtra("triggerType", if (isAutomatic) "AUTO_SUGGEST" else "MANUAL")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                // 화면 깨우기 (Deprecated flags 대신 setShowWhenLocked/setTurnScreenOn 사용)
                // BeforeRelaxActivity에서 설정 필요
            }

            startActivity(intent)
            Log.d(TAG, "✅ BeforeRelaxActivity started (triggerType=${if (isAutomatic) "AUTO" else "MANUAL"})")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start BeforeRelaxActivity", e)
        }
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
