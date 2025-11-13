package com.hand.hand.api.Login

// 요청 DTO
data class LoginRequest(
    val email: String,
    val password: String
)

// 서버 공통 응답 예시:
// { "success": true, "data": null, "message": "로그인 성공" }
// 현재는 data=null이므로 아래처럼 받아두고,
// 나중에 토큰이 내려오면 data 클래스를 바꾸면 됨.
data class LoginResponse(
    val success: Boolean,
    val data: Any? = null,   // 추후 { accessToken: String, ... } 로 교체 가능
    val message: String? = null
)
