package com.mim.watch.services

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.mim.watch.data.model.BioSampleBatch
import kotlinx.coroutines.tasks.await
import com.google.gson.Gson

/**
 * Wear → Phone 데이터 전송
 * - Wearable MessageClient 사용
 * - connectedNodes로 페어링된 Phone 찾기
 */
class WearMessageSender(private val context: Context) {

    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val gson = Gson()

    companion object {
        private const val TAG = "WearMessageSender"
        private const val MESSAGE_PATH = "/mim/bio_data"  // 고유 경로
    }

    /**
     * BioSampleBatch를 Phone으로 전송
     */
    suspend fun sendBatch(batch: BioSampleBatch): Boolean {
        return try {
            // 1. 페어링된 Phone 노드 찾기
            val nodes = nodeClient.connectedNodes.await()

            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected phone found")
                return false
            }

            // 2. JSON으로 직렬화
            val json = gson.toJson(batch)
            val data = json.toByteArray(Charsets.UTF_8)

            Log.d(TAG, "Sending batch: ${batch.samples.size} samples to ${nodes.size} node(s)")

            // 3. 모든 연결된 노드에 전송 (보통 Phone 1개)
            var successCount = 0
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(node.id, MESSAGE_PATH, data).await()
                    Log.d(TAG, "✅ Sent to node: ${node.displayName} (${node.id})")
                    successCount++
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to send to node ${node.id}: ${e.message}", e)
                }
            }

            successCount > 0

        } catch (e: Exception) {
            Log.e(TAG, "sendBatch failed: ${e.message}", e)
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