//IndividualUserApiModels.kt

package com.hand.hand.api.SignUp



// POST /individual-users 요청 바디
data class IndividualUserRequest(
    val name: String,
    val age: Int,
    val gender: String,          // "M" / "F"
    val job: String,
    val height: Int,
    val weight: Int,
    val disease: String,
    val residenceType: String,
    val diaryReminderEnabled: Boolean,
    val notificationHour: Int?   // 0-23 (null 허용)
)

// 공통 응답 껍데기 (스웨거 예시 기준)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

// data 안에 들어오는 실제 회원 정보
data class IndividualUserData(
    val id: Long,
    val userId: Long,
    val name: String,
    val age: Int,
    val gender: String,
    val job: String,
    val height: Int,
    val weight: Int,
    val disease: String,
    val residenceType: String,
    val diaryReminderEnabled: Boolean,
    val notificationHour: Int?  // 0-23 (null 허용)
)
