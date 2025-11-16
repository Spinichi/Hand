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