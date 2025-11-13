package com.hand.hand.api.Measurements

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MeasurementsInterface {
    /**
     * 측정 데이터 저장
     * POST /v1/measurements
     * Authorization: Bearer {token} (자동 추가)
     */
    @POST("v1/measurements")
    fun createMeasurement(@Body request: MeasurementRequest): Call<MeasurementResponse>
}
