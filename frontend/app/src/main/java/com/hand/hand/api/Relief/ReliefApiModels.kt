package com.hand.hand.api.Relief

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 완화법 세션 시작 요청 DTO
 */
data class ReliefStartRequest(
    val interventionId: Long,           // 완화법 ID (1~6)
    val triggerType: String,            // "AUTO_SUGGEST" or "MANUAL"
    val anomalyDetectionId: Long?,      // 이상치 감지 ID (자동인 경우)
    val gestureCode: String?,           // 제스처 코드 (예: "care1", "care2")
    val startedAt: String?              // ISO-8601 형식 시작 시각
) {
    companion object {
        fun create(
            interventionId: Long,
            triggerType: String,
            gestureCode: String? = null,
            timestampMs: Long = System.currentTimeMillis()
        ): ReliefStartRequest {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val startedAt = Instant.ofEpochMilli(timestampMs)
                .atZone(ZoneId.of("Asia/Seoul"))  // KST로 변경
                .format(formatter)

            return ReliefStartRequest(
                interventionId = interventionId,
                triggerType = triggerType,
                anomalyDetectionId = null,  // 현재는 null, 추후 확장 가능
                gestureCode = gestureCode,
                startedAt = startedAt
            )
        }
    }
}

/**
 * 완화법 세션 시작 응답 DTO
 */
data class ReliefStartResponse(
    val sessionId: Long,
    val beforeStress: Int?,
    val startedAt: String
)

/**
 * 완화법 세션 종료 요청 DTO
 */
data class ReliefEndRequest(
    val endedAt: String?,
    val userRating: Int?  // 1~5 별점
) {
    companion object {
        fun create(
            userRating: Int?,
            timestampMs: Long = System.currentTimeMillis()
        ): ReliefEndRequest {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val endedAt = Instant.ofEpochMilli(timestampMs)
                .atZone(ZoneId.of("Asia/Seoul"))  // KST로 변경
                .format(formatter)

            return ReliefEndRequest(
                endedAt = endedAt,
                userRating = userRating
            )
        }
    }
}

/**
 * 완화법 세션 종료 응답 DTO
 */
data class ReliefEndResponse(
    val afterStress: Int?,
    val durationSeconds: Int,
    val endedAt: String
)

/**
 * 공통 응답 래퍼 (워치 자동용)
 */
data class ReliefApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

// ═══════════════════════════════════════════════════════════
// 앱 수동 완화법용 (Manual Relief from App - careSafeZone)
// ═══════════════════════════════════════════════════════════

/**
 * 릴리프 세션 시작 요청 (앱 수동용)
 */
data class ReliefSessionStartRequest(
    val interventionId: Int,       // 예: 1
    val triggerType: String,       // 예: "MANUAL"
    val anomalyDetectionId: Int?,  // 없으면 null 가능
    val gestureCode: String?,      // 제스처 코드 (없으면 null)
    val startedAt: String          // "2025-11-13T06:51:42"
)

/**
 * 릴리프 세션 시작 응답 데이터 (앱 수동용)
 */
data class ReliefSessionStartData(
    val sessionId: Long,
    val beforeStress: Float?,      // null 허용
    val startedAt: String
)

/**
 * 릴리프 세션 시작 응답 (앱 수동용)
 */
data class ReliefSessionStartResponse(
    val success: Boolean,
    val data: ReliefSessionStartData?,
    val message: String?
)

/**
 * 릴리프 세션 종료 요청 (앱 수동용)
 */
data class ReliefSessionEndRequest(
    val endedAt: String,   // "2025-11-13T07:03:32"
    val userRating: Int    // 유저 불편감 점수
)