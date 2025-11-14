package com.hand.hand.api.Report

// 최상위 응답: { "success": true, "data": { ... } }
data class WeeklyReportsResponse(
    val success: Boolean,
    val data: WeeklyReportsPage?
)

// data 안에 들어있는 페이지 정보
data class WeeklyReportsPage(
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val content: List<WeeklyReportItem>
)

// 실제 한 주간 리포트 데이터
data class WeeklyReportItem(
    val id: Long,
    val userId: Long,
    val year: Int,
    val weekNumber: Int,
    val weekStartDate: String,   // "2025-11-14"
    val weekEndDate: String,
    val mongodbReportId: String?,
    val diaryCount: Int,
    val status: String,          // "GENERATING" 등
    val createdAt: String,
    val completedAt: String?
)


//        월간 리포트

// 전체 응답
data class MonthlyReportsResponse(
    val success: Boolean,
    val data: MonthlyReportsPage?
)

// data 안 페이지 정보
data class MonthlyReportsPage(
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val content: List<MonthlyReportItem>
)

// 실제 월간 리포트 데이터
data class MonthlyReportItem(
    val id: Long,
    val userId: Long,
    val year: Int,
    val month: Int,
    val monthStartDate: String,    // "2025-11-01"
    val monthEndDate: String,      // "2025-11-30"
    val mongodbReportId: String?,
    val diaryCount: Int,
    val status: String,            // "GENERATING" 등
    val createdAt: String,
    val completedAt: String?
)

// 주간 상세


data class WeeklyReportDetailResponse(
    val success: Boolean,
    val data: WeeklyReportDetail?,
    val message: String?
)

data class WeeklyReportDetail(
    val id: String,
    val userId: Long,
    val year: Int,
    val weekNumber: Int,
    val weekStartDate: String,
    val weekEndDate: String,

    // 안에 뭐가 들어갈지 아직 확실치 않아서 Map 형태로 넉넉하게 받아둠
    val dailyDiaries: List<Map<String, Any>>?,
    val userBaseline: Map<String, Any>?,
    val anomalies: List<Map<String, Any>>?,
    val userInfo: Map<String, Any>?,

    val report: String?,
    val emotionalAdvice: String?,
    val totalDiaryCount: Int,
    val averageDepressionScore: Double,
    val maxDepressionScore: Double,
    val minDepressionScore: Double,
    val createdAt: String,
    val analyzedAt: String
)


// 월간 상세


data class MonthlyReportDetailResponse(
    val success: Boolean,
    val data: MonthlyReportDetail?,
    val message: String?
)

data class MonthlyReportDetail(
    val id: String,
    val userId: Long,
    val year: Int,
    val month: Int,
    val monthStartDate: String,
    val monthEndDate: String,

    val dailyDiaries: List<Map<String, Any>>?,
    val userBaseline: Map<String, Any>?,
    val anomalies: List<Map<String, Any>>?,
    val userInfo: Map<String, Any>?,

    val report: String?,
    val emotionalAdvice: String?,
    val diaryCount: Int,
    val createdAt: String,
    val analyzedAt: String
)
