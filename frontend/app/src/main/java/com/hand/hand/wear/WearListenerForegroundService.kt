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
import com.google.gson.Gson
import com.hand.hand.R
import com.hand.hand.wear.model.BioSampleBatch
import com.hand.hand.wear.model.BioSample

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
    }

    private lateinit var dataClient: DataClient
    private val gson = Gson()

    private val dataListener = DataClient.OnDataChangedListener { dataEvents ->
        Log.d(TAG, "⭐ onDataChanged: ${dataEvents.count} events")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                Log.d(TAG, "  Data changed: $path")

                if (path == DATA_PATH) {
                    handleBioData(event)
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

        // DataClient 리스너 등록
        dataClient = Wearable.getDataClient(this)
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

            // TODO: 여기서 백엔드 서버로 전송
            // sendToBackend(sample)

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing bio data", e)
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