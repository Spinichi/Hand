package com.hand.hand.api.Notification

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * FCM 알림 관련 API Manager
 */
class NotificationManager {
    companion object {
        private const val TAG = "NotificationManager"

        private val httpCall: NotificationInterface =
            RetrofitClient.getClient().create(NotificationInterface::class.java)

        /**
         * FCM 디바이스 토큰을 백엔드에 등록
         */
        fun registerToken(
            deviceToken: String,
            onSuccess: () -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            val request = DeviceTokenRequest(token = deviceToken)  // "token" 필드 사용
            Log.d(TAG, "디바이스 토큰 등록 요청: ${deviceToken.take(20)}...")

            httpCall.registerDeviceToken(request).enqueue(object : Callback<ApiResponse<Void>> {
                override fun onResponse(
                    call: Call<ApiResponse<Void>>,
                    response: Response<ApiResponse<Void>>
                ) {
                    val body = response.body()
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) {
                        null
                    }

                    Log.d(TAG, "응답코드=${response.code()} body=$body errorBody=$errorBodyStr")

                    if (response.isSuccessful && body != null) {
                        Log.d(TAG, "토큰 등록 성공: ${body.message}")
                        onSuccess()
                    } else {
                        val msg = "토큰 등록 실패: ${response.code()} - ${response.message()}"
                        Log.e(TAG, msg)
                        onFailure(Throwable(msg))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                    Log.e(TAG, "통신 실패: ${t.localizedMessage}", t)
                    onFailure(t)
                }
            })
        }
    }
}
