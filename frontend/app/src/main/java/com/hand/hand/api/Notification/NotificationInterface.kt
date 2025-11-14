package com.hand.hand.api.Notification

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * FCM 알림 관련 Retrofit API 인터페이스
 */
interface NotificationInterface {

    /**
     * 디바이스 토큰 등록
     * POST /api/v1/notifications/token
     */
    @POST("v1/notifications/token")
    fun registerDeviceToken(
        @Body request: DeviceTokenRequest
    ): Call<ApiResponse<Void>>
}
