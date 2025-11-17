// com.hand.hand.api.Write
package com.hand.hand.api.Write

data class DiaryStartResponse(
    val success: Boolean,
    val data: DiaryStartData?,
    val message: String?
)

data class DiaryStartData(
    val sessionId: Long,
    val questionNumber: Int,
    val questionText: String
)

data class DiaryAnswerRequest(
    val answerText: String
)

data class DiaryAnswerResponse(
    val success: Boolean,
    val data: DiaryAnswerData?,
    val message: String?
)

data class DiaryAnswerData(
    val sessionId: Long,
    val questionNumber: Int,
    val questionText: String,
    val canFinish: Boolean
)

// 다이어리 완료 응답

data class DiaryCompleteResponse(
    val success: Boolean,
    val data: DiaryCompleteData?,
    val message: String?
)

data class DiaryCompleteData(
    val sessionId: Long,
    val emotions: DiaryEmotions,
    val depressionScore: Double,
    val shortSummary: String,
    val longSummary: String,
    val emotionalAdvice: String,
    val completedAt: String
)

data class DiaryEmotions(
    val joy: Double,
    val embarrassment: Double,
    val anger: Double,
    val anxiety: Double,
    val hurt: Double,
    val sadness: Double
)

// 오늘의 다이어리 상태 조회 응답
data class TodayDiaryStatusResponse(
    val success: Boolean,
    val data: TodayDiaryStatusData?,
    val message: String?
)

data class TodayDiaryStatusData(
    val status: String?,  // null: 작성 안함, "IN_PROGRESS": 작성중, "COMPLETED": 완료
    val sessionId: Long?,
    val conversations: List<ConversationItem>?,
    val questionCount: Int?
)

data class ConversationItem(
    val questionText: String,
    val answerText: String?,
    val questionNumber: Int
)