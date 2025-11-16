package com.hand.hand.api.Sleep

/**
 * 수면 데이터 저장 요청 DTO
 */
data class SleepRequest(
    val sleepStartTime: String,  // ISO-8601 형식: "2025-01-14T22:30:00"
    val sleepEndTime: String      // ISO-8601 형식: "2025-01-15T07:00:00"
)

/**
 * 수면 데이터 저장 응답 DTO
 */
data class SleepCreateData(
    val id: Long,
    val sleepDurationMinutes: Int,
    val sleepDurationHours: Int
)

/**
 * 수면 데이터 상세 응답 DTO
 */
data class SleepData(
    val id: Long,
    val sleepStartTime: String,
    val sleepEndTime: String,
    val sleepDurationMinutes: Int,
    val sleepDurationHours: Int,
    val sleepDate: String,
    val createdAt: String
)

/**
 * 공통 응답 래퍼 (저장)
 */
data class SleepCreateResponse(
    val success: Boolean,
    val data: SleepCreateData?,
    val message: String?
)

/**
 * 공통 응답 래퍼 (조회)
 */
data class SleepResponse(
    val success: Boolean,
    val data: SleepData?,
    val message: String?
)