package com.hand.hand.api.riskToday

data class RiskTodayExistsResponse(
    val success: Boolean,
    val data: RiskTodayExistsData,
    val message: String
)

data class RiskTodayExistsData(
    val exists: Boolean
)

data class RiskTodayResponse(
    val success: Boolean,
    val data: RiskTodayData,
    val message: String
)

data class RiskTodayData(
    val id: Int,
    val scoreDate: String,
    val riskScore: Double,
    val diaryComponent: Double,
    val measurementComponent: Double,
    val sleepComponent: Double?,     // null 들어올 수 있으므로 nullable
    val measurementCount: Int,
    val anomalyCount: Int
)