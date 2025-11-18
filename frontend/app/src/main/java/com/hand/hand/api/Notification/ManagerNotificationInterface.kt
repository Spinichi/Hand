package com.hand.hand.api.Notification

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ManagerNotificationInterface {
    @POST("v1/manager/notifications/send")
    fun sendNotification(
        @Body request: SendNotificationRequest
    ): Call<ManagerNotificationResponse<Any?>>
}

data class SendNotificationRequest(
    val userId: Long,
    val title: String,
    val body: String
)

/**
 * 매니저 알림 API 응답 (백엔드 ApiResponse 형식과 일치)
 */
data class ManagerNotificationResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
