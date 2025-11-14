package com.hand.hand.api.Notification

/**
 * FCM 디바이스 토큰 등록 요청
 * 백엔드 TokenRequest와 매칭
 */
data class DeviceTokenRequest(
    val token: String  // 백엔드: "token" 사용
)

/**
 * API 공통 응답 형식
 */
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T?
)

/**
 * 디바이스 토큰 등록 응답
 */
data class DeviceTokenResponse(
    val success: Boolean
)
