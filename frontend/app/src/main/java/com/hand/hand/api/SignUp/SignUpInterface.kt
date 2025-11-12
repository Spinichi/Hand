package com.hand.hand.api.SignUp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SignUpInterface {
    //https://gatewaytohand.store/api

    @POST("v1/users/signup")
    fun signup(@Body signupRequest: SignupRequest): Call<SignupResponse>


}