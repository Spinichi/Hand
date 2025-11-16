package com.hand.hand.api.Measurements

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
