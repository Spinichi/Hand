package com.hand.hand.api.Group

// request models
data class GroupCreateRequest(
    val name: String,
    val groupType: String
)

data class GroupJoinRequest(
    val inviteCode: String
)

data class MemberNotesUpdateRequest(
    val specialNotes: String
)

// response inner data (서버에서 반환한 data 필드의 구조)
data class GroupData(
    val id: Int,
    val name: String?,
    val groupType: String?,
    val inviteCode: String?,
    val createdBy: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val avgMemberRiskScore : Double?,
    val memberCount : Int?
)

data class GroupMemberData(
    val userId: Int,
    val name: String,
    val role: String,
    val specialNotes: String?,
    val joinedAt: String?,
    val weeklyAvgRiskScore: Double? = null
)

// 서버가 래핑해서 줌: { "success": true, "data": { ... }, "message": "ok" }
data class WrappedResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
