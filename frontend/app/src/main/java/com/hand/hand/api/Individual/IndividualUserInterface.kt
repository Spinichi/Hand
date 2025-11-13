// IndividualUserInterface.kt
package com.hand.hand.api.SignUp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IndividualUserInterface {

    // 개인정보 등록
    // POST /individual-users
    @POST("v1/individual-users")
    fun createIndividualUser(
        @Body request: IndividualUserRequest
    ): Call<ApiResponse<IndividualUserData>>

    // 이미 등록된 정보 있는지 확인
    // GET /individual-users/me
    @GET("v1/individual-users/me")
    fun getMyIndividualUser(
    ): Call<ApiResponse<IndividualUserData>>
}
