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