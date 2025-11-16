// file: com/hand/hand/api/ManagerCounseling/ManagerCounselingApiModels.kt
package com.hand.hand.api.ManagerCounseling

// daily diary item
data class ManagerDailyDiary(
    val date: String?,
    val depressionScore: Double?,
    val shortSummary: String?,
    val longSummary: String?
)

// main data returned in "data" field
data class ManagerCounselingData(
    val reportId: Int?,
    val userId: Int?,
    val startDate: String?,
    val endDate: String?,
    val diaryCount: Int?,
    val dailyDiaries: List<ManagerDailyDiary>?,
    val report: String?,
    val advice: String?,
    val averageDepressionScore: Double?,
    val maxDepressionScore: Double?,
    val minDepressionScore: Double?,
    val specialNotes: String?,
    val createdAt: String?,
    val counselingAdvice: String?
)

// Local wrapper type for the API responses used here.
// (Separate from any other WrappedResponse definition you may have in project.)
data class WrappedResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
