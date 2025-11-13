package com.hand.hand.api.Baseline

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Baseline API Interface
 */
interface BaselineInterface {

    /**
     * 활성 Baseline 조회
     * GET /v1/baselines/active
     */
    @GET("v1/baselines/active")
    fun getActiveBaseline(): Call<BaselineApiResponse<BaselineResponse>>

    /**
     * Baseline 계산 및 생성
     * POST /v1/baselines/calculate?days=3
     */
    @POST("v1/baselines/calculate")
    fun calculateBaseline(
        @Query("days") days: Int = 3
    ): Call<BaselineApiResponse<BaselineResponse>>
}
