package com.hand.hand.api.riskToday

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface riskTodayInterface {
    @GET("v1/risk/today/exists")
    fun riskTodayExists(): Call<RiskTodayExistsResponse>

    @GET("v1/risk/today")
    fun riskToday(): Call<RiskTodayResponse>
}