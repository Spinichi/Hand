package com.hand.hand.api.CareHistory

data class CareSession(
    val sessionId: Int,
    val interventionId: Int,
    val interventionName: String,
    val startedAt: String,
    val beforeStress: Int,
    val afterStress: Int,
    val reduction: Int
)

// 하루 단위 히스토리
data class CareHistoryDay(
    val date: String,
    val sessions: List<CareSession>
)

// 통계 정보
data class InterventionStat(
    val interventionId: Int,
    val name: String,
    val days: Int,
    val sessions: Int,
    val avgReduction: Double
)

data class CareStatistics(
    val mostUsed: InterventionStat,
    val mostEffective: InterventionStat
)

// 전체 데이터
data class CareHistoryData(
    val statistics: CareStatistics,
    val history: List<CareHistoryDay>,
    val totalElements: Int,
    val currentPage: Int,
    val pageSize: Int
)

// 최종 래퍼
data class CareHistoryResponse(
    val success: Boolean,
    val data: CareHistoryData,
    val message: String
)