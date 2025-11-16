package com.hand.hand.api.Measurements

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MeasurementsInterface {
    /**
     * 측정 데이터 저장
     * POST /v1/measurements
     * Authorization: Bearer {token} (자동 추가)
     */
    @POST("v1/measurements")
    fun createMeasurement(@Body request: MeasurementRequest): Call<MeasurementResponse>

    /**
     * 최근 측정 데이터 조회
     * GET /v1/measurements/latest
     * Authorization: Bearer {token} (자동 추가)
     */
    @GET("v1/measurements/latest")
    fun getLatestMeasurement(): Call<LatestMeasurementResponse>
}

interface StressTodayInterface {

    @GET("v1/measurements/stress/today")
    fun getTodayStress(
        @Query("date") date: String   // "2025-11-16" 이런 형식
    ): Call<StressTodayResponse>
}