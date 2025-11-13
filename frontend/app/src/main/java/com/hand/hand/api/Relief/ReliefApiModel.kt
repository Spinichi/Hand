//ReliefApiModel.kt

package com.hand.hand.api.Relief

// ───────── Request ─────────
data class ReliefSessionStartRequest(
    val interventionId: Int,       // 예: 1
    val triggerType: String,       // 예: "AUTO_SUGGEST"
    val anomalyDetectionId: Int?,  // 없으면 null 가능
    val gestureCode: String?,      // 제스처 코드 (없으면 null)
    val startedAt: String          // "2025-11-13T06:51:42.400Z"
)

// ───────── Response ─────────
data class ReliefSessionStartData(
    val sessionId: Long,
    val beforeStress: Float?,      // null 허용 (swagger에서 null 나왔으니까)
    val startedAt: String
)

data class ReliefSessionStartResponse(
    val success: Boolean,
    val data: ReliefSessionStartData?,
    val message: String?
)

// 세션 종료
data class ReliefSessionEndRequest(
    val endedAt: String,   // "2025-11-13T07:03:32.083Z"
    val userRating: Int    // 유저 불편감 점수
)
