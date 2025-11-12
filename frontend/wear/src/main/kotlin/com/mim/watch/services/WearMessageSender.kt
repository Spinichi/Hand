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
        private const val DATA_PATH = "/mim/bio_data"  // 고유 경로
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