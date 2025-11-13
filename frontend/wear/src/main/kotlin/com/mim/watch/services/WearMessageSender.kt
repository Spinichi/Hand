package com.mim.watch.services

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.mim.watch.data.model.BioSampleBatch
import com.mim.watch.data.model.BioSample
import kotlinx.coroutines.tasks.await
import com.google.gson.Gson

/**
 * Wear → Phone 데이터 전송
 * - Wearable DataClient 사용 (백그라운드 수신 지원)
 * - connectedNodes로 페어링된 Phone 찾기
 */
class WearMessageSender(private val context: Context) {

    private val dataClient = Wearable.getDataClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val gson = Gson()

    companion object {
        private const val TAG = "WearMessageSender"
        private const val DATA_PATH = "/mim/bio_data"  // 생체 데이터 경로
        private const val ANOMALY_PATH = "/mim/anomaly_alert"  // 이상치 알림 경로
        private const val RELIEF_EVENT_PATH = "/mim/relief_event"  // 완화법 이벤트 경로
    }

    /**
     * BioSample 1개를 Phone으로 전송 (대표 샘플)
     * DataClient를 사용하여 백그라운드 수신 지원
     */
    suspend fun sendSample(sample: BioSample): Boolean {
        return try {
            // 1. JSON으로 직렬화
            val json = gson.toJson(sample)

            Log.d(TAG, "Sending sample: stressLevel=${sample.stressLevel}, isAnomaly=${sample.isAnomaly}")

            // 2. DataItem 생성 (타임스탬프를 포함하여 매번 새로운 데이터로 인식)
            val putDataReq = PutDataMapRequest.create(DATA_PATH).apply {
                dataMap.putString("json", json)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
                .setUrgent() // 즉시 전송

            // 3. 전송
            val result = dataClient.putDataItem(putDataReq).await()
            Log.d(TAG, "✅ Sample sent, uri=${result.uri}")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ sendSample failed: ${e.message}", e)
            false
        }
    }

    /**
     * BioSampleBatch를 Phone으로 전송 (레거시 - 사용 안 함)
     * DataClient를 사용하여 백그라운드 수신 지원
     */
    suspend fun sendBatch(batch: BioSampleBatch): Boolean {
        return try {
            // 1. JSON으로 직렬화
            val json = gson.toJson(batch)

            // ⭐ 디버깅: 전송할 JSON 확인 (첫 500자)
            Log.d(TAG, "Sending JSON: ${json.take(500)}...")

            // ⭐ 첫 번째 샘플의 accel 값 확인
            if (batch.samples.isNotEmpty()) {
                val first = batch.samples[0]
                Log.d(TAG, "First sample: accelX=${first.accelX}, accelY=${first.accelY}, accelZ=${first.accelZ}, movement=${first.movementIntensity}")
            }

            // 2. DataItem 생성 (타임스탬프를 포함하여 매번 새로운 데이터로 인식)
            val putDataReq = PutDataMapRequest.create(DATA_PATH).apply {
                dataMap.putString("json", json)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
                .setUrgent() // 즉시 전송

            // 3. 전송
            val result = dataClient.putDataItem(putDataReq).await()
            Log.d(TAG, "✅ Data sent: ${batch.samples.size} samples, uri=${result.uri}")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ sendBatch failed: ${e.message}", e)
            false
        }
    }

    /**
     * 이상치 감지 알림을 Phone으로 전송
     * Phone이 완화법 진행 중인지 확인 후 워치로 명령 전송
     */
    suspend fun sendAnomalyAlert(stressLevel: Int, stressIndex: Double): Boolean {
        return try {
            val data = mapOf(
                "stressLevel" to stressLevel,
                "stressIndex" to stressIndex,
                "timestamp" to System.currentTimeMillis()
            )
            val json = gson.toJson(data)

            Log.d(TAG, "🚨 Sending anomaly alert: stressLevel=$stressLevel, stressIndex=$stressIndex")

            val putDataReq = PutDataMapRequest.create(ANOMALY_PATH).apply {
                dataMap.putString("json", json)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
                .setUrgent()

            val result = dataClient.putDataItem(putDataReq).await()
            Log.d(TAG, "✅ Anomaly alert sent, uri=${result.uri}")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ sendAnomalyAlert failed: ${e.message}", e)
            false
        }
    }

    /**
     * 완화법 시작 이벤트를 Phone으로 전송
     */
    suspend fun sendReliefStartEvent(
        interventionId: Long,
        triggerType: String,
        gestureCode: String?
    ): Boolean {
        return try {
            val data = mapOf(
                "eventType" to "START",
                "interventionId" to interventionId,
                "triggerType" to triggerType,
                "gestureCode" to gestureCode,
                "timestamp" to System.currentTimeMillis()
            )
            val json = gson.toJson(data)

            Log.d(TAG, "🏁 Sending relief START event: interventionId=$interventionId, triggerType=$triggerType")

            val putDataReq = PutDataMapRequest.create(RELIEF_EVENT_PATH).apply {
                dataMap.putString("json", json)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
                .setUrgent()

            val result = dataClient.putDataItem(putDataReq).await()
            Log.d(TAG, "✅ Relief START event sent, uri=${result.uri}")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ sendReliefStartEvent failed: ${e.message}", e)
            false
        }
    }

    /**
     * 완화법 종료 이벤트를 Phone으로 전송
     */
    suspend fun sendReliefEndEvent(
        sessionId: Long,
        userRating: Int?
    ): Boolean {
        return try {
            val data = mapOf(
                "eventType" to "END",
                "sessionId" to sessionId,
                "userRating" to userRating,
                "timestamp" to System.currentTimeMillis()
            )
            val json = gson.toJson(data)

            Log.d(TAG, "🏁 Sending relief END event: sessionId=$sessionId, userRating=$userRating")

            val putDataReq = PutDataMapRequest.create(RELIEF_EVENT_PATH).apply {
                dataMap.putString("json", json)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
                .setUrgent()

            val result = dataClient.putDataItem(putDataReq).await()
            Log.d(TAG, "✅ Relief END event sent, uri=${result.uri}")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ sendReliefEndEvent failed: ${e.message}", e)
            false
        }
    }

    /**
     * 연결된 Phone 있는지 확인
     */
    suspend fun isPhoneConnected(): Boolean {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            nodes.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "isPhoneConnected check failed: ${e.message}", e)
            false
        }
    }
}