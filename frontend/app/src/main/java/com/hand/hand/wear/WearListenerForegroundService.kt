package com.hand.hand.wear

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import com.hand.hand.R
import com.hand.hand.wear.model.BioSampleBatch
import com.hand.hand.wear.model.BioSample
import com.hand.hand.api.Measurements.MeasurementsManager
import com.hand.hand.api.Relief.ReliefManager

/**
 * 백그라운드에서 Wear 앱으로부터 데이터를 수신하는 Foreground Service
 * - DataClient 사용 (백그라운드 수신 지원)
 * - 패키지명이 달라도 작동
 */
class WearListenerForegroundService : Service() {

    companion object {
        private const val TAG = "WearListenerFG"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "wear_listener_channel"
        private const val DATA_PATH = "/mim/bio_data"
        private const val ANOMALY_PATH = "/mim/anomaly_alert"
        private const val RELIEF_EVENT_PATH = "/mim/relief_event"

        // ⭐ 완화법 진행 중 플래그 및 세션 ID
        @Volatile
        private var isReliefInProgress = false

        @Volatile
        private var currentSessionId: Long? = null

        // ⭐ 마지막 이상치 알림 시간 (쿨다운용)
        @Volatile
        private var lastAnomalyAlertTime: Long = 0L

        private const val ANOMALY_COOLDOWN_MS = 10 * 60 * 1000L  // 10분

        // ⭐ 최신 스트레스 점수 저장 (앱 수동 완화법용)
        @Volatile
        private var latestStressLevel: Int? = null

        @Volatile
        private var latestStressTimestamp: Long = 0L

        fun setReliefInProgress(inProgress: Boolean) {
            isReliefInProgress = inProgress
            Log.d(TAG, "Relief in progress: $isReliefInProgress")
        }

        fun setCurrentSessionId(sessionId: Long?) {
            currentSessionId = sessionId
            Log.d(TAG, "Current session ID: $currentSessionId")
        }

        fun getLatestStressLevel(): Int? = latestStressLevel

        fun getLatestStressTimestamp(): Long = latestStressTimestamp
    }

    private lateinit var dataClient: DataClient
    private lateinit var messageClient: MessageClient
    private val gson = Gson()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val dataListener = DataClient.OnDataChangedListener { dataEvents ->
        Log.d(TAG, "⭐ onDataChanged: ${dataEvents.count} events")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                Log.d(TAG, "  Data changed: $path")

                when (path) {
                    DATA_PATH -> handleBioData(event)
                    ANOMALY_PATH -> handleAnomalyAlert(event)
                    RELIEF_EVENT_PATH -> handleReliefEvent(event)
                }
            }
        }
        dataEvents.release()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "⭐ WearListenerForegroundService onCreate()")

        // Foreground 알림 시작
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // DataClient 및 MessageClient 초기화
        dataClient = Wearable.getDataClient(this)
        messageClient = Wearable.getMessageClient(this)
        dataClient.addListener(dataListener)

        Log.d(TAG, "✅ DataClient listener registered for path: $DATA_PATH")

        // 연결된 노드 확인
        checkConnectedNodes()
    }

    private fun checkConnectedNodes() {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            Log.d(TAG, "📱 Connected nodes: ${nodes.size}")
            nodes.forEach { node ->
                Log.d(TAG, "  - ${node.displayName} (${node.id})")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "❌ Failed to get connected nodes", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "⭐ WearListenerForegroundService onDestroy()")

        // 리스너 해제
        dataClient.removeListener(dataListener)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun handleBioData(event: DataEvent) {
        try {
            val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
            val json = dataMapItem.dataMap.getString("json")

            if (json == null) {
                Log.e(TAG, "❌ JSON data is null")
                return
            }

            // ⭐ 대표 샘플 1개 수신 (10초마다)
            val sample = gson.fromJson(json, BioSample::class.java)

            Log.d(TAG, "=== Received Representative Sample from Watch ===")
            Log.d(TAG, "Timestamp: ${sample.timestampMs}")
            Log.d(TAG, "HR=${sample.heartRate} bpm, " +
                    "HRV_SDNN=${sample.hrvSdnn?.let { "%.1f".format(it) } ?: "N/A"} ms, " +
                    "HRV_RMSSD=${sample.hrvRmssd?.let { "%.1f".format(it) } ?: "N/A"} ms, " +
                    "ObjTemp=${sample.objectTemp}°C, " +
                    "AmbTemp=${sample.ambientTemp}°C, " +
                    "Accel(${sample.accelX}, ${sample.accelY}, ${sample.accelZ}), " +
                    "Movement=${sample.movementIntensity?.let { "%.2f".format(it) } ?: "N/A"}, " +
                    "Stress=${sample.stressIndex?.let { "%.1f".format(it) } ?: "N/A"}(Lv${sample.stressLevel}), " +
                    "Steps=${sample.totalSteps}, " +
                    "SPM=${sample.stepsPerMinute}, " +
                    "isAnomaly=${sample.isAnomaly}")

            // ⭐ 최신 스트레스 점수 저장
            sample.stressLevel?.let {
                latestStressLevel = it
                latestStressTimestamp = sample.timestampMs
                Log.d(TAG, "📊 Latest stress level updated: $it (timestamp: ${sample.timestampMs})")
            }

            // ⭐ 테스트 화면용: 데이터 업데이트
            WearDataReceiver.updateData(sample)

            // ⭐ 백엔드 서버로 전송 (Bearer 토큰 자동 포함)
            MeasurementsManager.sendBioData(
                sample = sample,
                onSuccess = { response ->
                    Log.d(TAG, "✅ DB 저장 성공: ID=${response.id}, 이상치=${response.isAnomaly}")
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ DB 저장 실패: ${error.message}")
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing bio data", e)
        }
    }

    private fun handleAnomalyAlert(event: DataEvent) {
        try {
            val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
            val json = dataMapItem.dataMap.getString("json")

            if (json == null) {
                Log.e(TAG, "❌ Anomaly alert JSON is null")
                return
            }

            val data = gson.fromJson(json, Map::class.java)
            val stressLevel = (data["stressLevel"] as? Double)?.toInt() ?: 0
            val stressIndex = data["stressIndex"] as? Double ?: 0.0

            Log.w(TAG, "🚨 Anomaly alert received: stressLevel=$stressLevel, stressIndex=$stressIndex")

            // ⭐ 완화법 진행 중이면 무시
            if (isReliefInProgress) {
                Log.d(TAG, "⏭️ Relief in progress, ignoring anomaly alert")
                return
            }

            // ⭐ 쿨다운 체크 (마지막 알림 후 10분 이내면 무시)
            val currentTime = System.currentTimeMillis()
            val timeSinceLastAlert = currentTime - lastAnomalyAlertTime
            if (timeSinceLastAlert < ANOMALY_COOLDOWN_MS) {
                val remainingMinutes = (ANOMALY_COOLDOWN_MS - timeSinceLastAlert) / 60000
                Log.d(TAG, "⏭️ Cooldown active, ignoring anomaly alert (${remainingMinutes}분 남음)")
                return
            }

            // ⭐ 쿨다운 시작 (명령 전송 시점에 바로 시작)
            lastAnomalyAlertTime = System.currentTimeMillis()

            // ⭐ 워치로 완화법 시작 명령 전송
            Log.d(TAG, "📤 Sending start relief command to watch... (쿨다운 시작)")
            sendStartReliefCommandToWatch()

        } catch (e: Exception) {
            Log.e(TAG, "Error handling anomaly alert", e)
        }
    }

    private fun sendStartReliefCommandToWatch() {
        serviceScope.launch {
            try {
                val nodesTask = Wearable.getNodeClient(this@WearListenerForegroundService)
                    .connectedNodes

                val nodes = Tasks.await(nodesTask)

                if (nodes.isEmpty()) {
                    Log.w(TAG, "No connected watch found")
                    return@launch
                }

                val message = "START_RELIEF".toByteArray()

                for (node in nodes) {
                    val sendTask = messageClient.sendMessage(node.id, "/relief/command", message)
                    Tasks.await(sendTask)
                    Log.d(TAG, "✅ Start relief command sent to watch: ${node.displayName}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send start relief command", e)
            }
        }
    }

    private fun handleReliefEvent(event: DataEvent) {
        try {
            val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
            val json = dataMapItem.dataMap.getString("json")

            if (json == null) {
                Log.e(TAG, "❌ Relief event JSON is null")
                return
            }

            val data = gson.fromJson(json, Map::class.java)
            val eventType = data["eventType"] as? String ?: return

            Log.d(TAG, "🏁 Relief event received: $eventType")

            when (eventType) {
                "START" -> {
                    val interventionId = (data["interventionId"] as? Double)?.toLong() ?: return
                    val triggerType = data["triggerType"] as? String ?: "MANUAL"
                    val gestureCode = data["gestureCode"] as? String

                    Log.d(TAG, "▶️ Starting relief session: interventionId=$interventionId, triggerType=$triggerType")

                    // ⭐ 완화법 진행 중 플래그 설정
                    setReliefInProgress(true)

                    // ⭐ 백엔드 API 호출: 세션 시작
                    ReliefManager.startSession(
                        interventionId = interventionId,
                        triggerType = triggerType,
                        gestureCode = gestureCode,
                        onSuccess = { response ->
                            Log.d(TAG, "✅ Relief session started: sessionId=${response.sessionId}")
                            setCurrentSessionId(response.sessionId)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "❌ Failed to start relief session: ${error.message}")
                            setReliefInProgress(false)
                        }
                    )
                }
                "END" -> {
                    // ⭐ sessionId=0L이면 무시하고 저장된 currentSessionId 사용
                    val receivedSessionId = (data["sessionId"] as? Double)?.toLong()
                    val sessionId = if (receivedSessionId == null || receivedSessionId == 0L) {
                        currentSessionId
                    } else {
                        receivedSessionId
                    } ?: run {
                        Log.e(TAG, "❌ No valid sessionId found (received=$receivedSessionId, current=$currentSessionId)")
                        return
                    }
                    val userRating = (data["userRating"] as? Double)?.toInt()

                    Log.d(TAG, "⏹️ Ending relief session: sessionId=$sessionId (received=$receivedSessionId, used=${if (receivedSessionId == 0L) "stored" else "received"}), userRating=$userRating")

                    // ⭐ 백엔드 API 호출: 세션 종료
                    ReliefManager.endSession(
                        sessionId = sessionId,
                        userRating = userRating,
                        onSuccess = { response ->
                            Log.d(TAG, "✅ Relief session ended: duration=${response.durationSeconds}초")
                            setReliefInProgress(false)
                            setCurrentSessionId(null)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "❌ Failed to end relief session: ${error.message}")
                        }
                    )
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling relief event", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wear Data Listener",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "워치 데이터 수신 중"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("워치 연결 중")
        .setContentText("워치로부터 생체 데이터를 수신하고 있습니다")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()
}