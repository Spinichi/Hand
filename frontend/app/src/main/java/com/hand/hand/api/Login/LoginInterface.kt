package com.hand.hand.api.Login

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginInterface {
    // BASE_URL = https://gatewaytohand.store/api/  이므로 "v1/..."만 적기
    @POST("v1/auth/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>
}
