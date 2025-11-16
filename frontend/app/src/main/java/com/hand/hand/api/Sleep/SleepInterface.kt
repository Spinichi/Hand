package com.hand.hand.api.Sleep

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SleepInterface {
    /**
     * 수면 데이터 저장
     * POST /v1/sleep
     * Authorization: Bearer {token} (자동 추가)
     */
    @POST("v1/sleep")
    fun createSleep(@Body request: SleepRequest): Call<SleepCreateResponse>

    /**
     * 오늘의 수면 데이터 조회
     * GET /v1/sleep/today
     * Authorization: Bearer {token} (자동 추가)
     */
    @GET("v1/sleep/today")
    fun getTodaySleep(): Call<SleepResponse>
}