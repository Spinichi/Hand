package com.hand.hand.api.Diary

data class DiaryRequest(
    val startDate: String,
    val endDate: String,
    val pageable: PageableRequest
)

data class PageableRequest(
    val page: Int = 0,
    val size: Int = 30,
    val sort: List<String> = listOf("sessionDate,desc")
)

data class DiaryResponse(
    val success: Boolean,
    val data: DiaryData?,
    val message: String?
)

data class DiaryData(
    val content: List<DiaryItemResponse>,
    val totalPages: Int,
    val totalElements: Int
)

data class DiaryItemResponse(
    val sessionId: Int,
    val sessionDate: String,
    val status: String?,
    val questionCount: Int?,
    val createdAt: String,
    val completedAt: String?,
    val depressionScore: Int?,
    val shortSummary: String?
)

data class DiaryItem(
    val sessionId: Int,
    val sessionDate: String,
    val createdAt: String,
    val depressionScore: Int?,
    val shortSummary: String?
)

fun DiaryItemResponse.toDiaryItem(): DiaryItem {
    return DiaryItem(
        sessionId = sessionId,
        sessionDate = sessionDate,
        createdAt = createdAt,
        depressionScore = depressionScore,
        shortSummary = shortSummary
    )
}

data class DiaryDetailResponse(
    val sessionId: Int,
    val sessionDate: String, // yyyy-MM-dd
    val emotions: Emotions?,
    val depressionScore: Int,
    val shortSummary: String?,
    val longSummary: String?,
    val emotionalAdvice: String?
)

data class Emotions(
    val joy: Double,
    val embarrassment: Double,
    val anger: Double,
    val anxiety: Double,
    val hurt: Double,
    val sadness: Double
)