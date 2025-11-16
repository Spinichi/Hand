package com.hand.hand.api.Diary

// ------------------------------
// 📌 다이어리 목록 조회 요청 DTO
// ------------------------------
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

// ------------------------------
// 📌 다이어리 목록 조회 응답 DTO
// ------------------------------
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
    val sessionId: Long,
    val sessionDate: String,
    val status: String?,
    val questionCount: Int?,
    val createdAt: String,
    val completedAt: String?,
    val depressionScore: Int?,
    val shortSummary: String?
)

// ------------------------------
// 📌 목록 화면에서 사용하는 변환 DTO
// ------------------------------
data class DiaryItem(
    val sessionId: Long,
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

// ------------------------------
// 📌 다이어리 상세 조회 응답 DTO (서버 JSON 완전 일치)
// ------------------------------
data class DiaryDetailWrapper(
    val success: Boolean,
    val data: DiaryDetailResponse?,
    val message: String?
)

data class DiaryDetailResponse(
    val sessionId: Long,
    val sessionDate: String?,
    val status: String?,
    val conversations: List<ConversationItem>?,
    val emotions: Emotions?,
    val depressionScore: Double?,
    val shortSummary: String?,
    val longSummary: String?,
    val emotionalAdvice: String?,
    val createdAt: String?,
    val completedAt: String?
)

// ------------------------------
// 📌 상세 조회 내부 구조
// ------------------------------
data class ConversationItem(
    val questionNumber: Int,
    val questionText: String?,
    val source: String?,
    val answerText: String?,
    val answeredAt: String?
)

data class Emotions(
    val joy: Double?,
    val embarrassment: Double?,
    val anger: Double?,
    val anxiety: Double?,
    val hurt: Double?,
    val sadness: Double?
)
