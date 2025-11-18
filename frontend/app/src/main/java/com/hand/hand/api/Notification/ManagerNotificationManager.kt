package com.hand.hand.api.Notification

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManagerNotificationManager {
    companion object {
        private const val TAG = "ManagerNotificationManager"

        private val httpCall: ManagerNotificationInterface =
            RetrofitClient.getClient().create(ManagerNotificationInterface::class.java)

        /**
         * 특정 멤버에게 개별 알림 전송
         */
        fun sendToUser(
            userId: Long,
            title: String,
            body: String,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val request = SendNotificationRequest(
                userId = userId,
                title = title,
                body = body
            )
            Log.d(TAG, "개별 알림 전송 요청: userId=$userId, title=$title")

            httpCall.sendNotification(request).enqueue(object : Callback<ManagerNotificationResponse<Any?>> {
                override fun onResponse(
                    call: Call<ManagerNotificationResponse<Any?>>,
                    response: Response<ManagerNotificationResponse<Any?>>
                ) {
                    Log.d(TAG, "======== 알림 전송 응답 시작 ========")
                    Log.d(TAG, "HTTP 상태 코드: ${response.code()}")
                    Log.d(TAG, "성공 여부: ${response.isSuccessful}")

                    val body = response.body()
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) {
                        null
                    }

                    Log.d(TAG, "응답 body=$body")
                    Log.d(TAG, "에러 body=$errorBodyStr")
                    Log.d(TAG, "======== 알림 전송 응답 끝 ========")

                    if (response.isSuccessful && body?.success == true) {
                        Log.d(TAG, "✅ 알림 전송 성공: ${body.message}")
                        onSuccess()
                    } else {
                        val msg = body?.message ?: errorBodyStr ?: "알림 전송 실패 (${response.code()})"
                        Log.e(TAG, "❌ 알림 전송 실패: $msg")
                        onFailure(msg)
                    }
                }

                override fun onFailure(call: Call<ManagerNotificationResponse<Any?>>, t: Throwable) {
                    Log.e(TAG, "통신 실패: ${t.localizedMessage}", t)
                    onFailure(t.localizedMessage ?: "네트워크 오류")
                }
            })
        }
    }
}
