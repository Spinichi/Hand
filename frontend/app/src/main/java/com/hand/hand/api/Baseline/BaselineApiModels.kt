package com.hand.hand.api.Baseline

/**
 * Baseline 응답 DTO
 */
data class BaselineResponse(
    val id: Long,
    val version: Int,
    val isActive: Boolean,

    // HRV 통계
    val hrvSdnnMean: Double?,
    val hrvSdnnStd: Double?,
    val hrvRmssdMean: Double?,
    val hrvRmssdStd: Double?,

    // 심박수 통계
    val heartRateMean: Double?,
    val heartRateStd: Double?,

    // 체온 통계
    val objectTempMean: Double?,
    val objectTempStd: Double?,

    // 메타데이터
    val measurementCount: Int?,
    val dataStartDate: String?,  // LocalDate -> String
    val dataEndDate: String?,    // LocalDate -> String

    // 시간
    val createdAt: String?,      // LocalDateTime -> String
    val updatedAt: String?       // LocalDateTime -> String
)

/**
 * API 공통 응답 래퍼
 */
data class BaselineApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
