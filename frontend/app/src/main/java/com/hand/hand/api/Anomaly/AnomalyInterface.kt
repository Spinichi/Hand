package com.hand.hand.api.Anomaly

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 이상치 데이터 조회 Retrofit API 인터페이스
 */
interface AnomalyInterface {

    /**
     * 특정 날짜의 이상치 상세 목록 조회
     * GET /measurements/anomalies/daily?date={date}
     *
     * @param date 조회할 날짜 (yyyy-MM-dd), 선택적 (기본: 오늘)
     * @return 일일 이상치 상세 데이터
     */
    @GET("v1/measurements/anomalies/daily")
    fun getDailyAnomalies(
        @Query("date") date: String?
    ): Call<ApiResponse<DailyAnomalyResponse>>

    /**
     * 주간 이상치 목록 조회 (최근 7일)
     * GET /measurements/anomalies/weekly
     *
     * @return 주간 이상치 상세 데이터
     */
    @GET("v1/measurements/anomalies/weekly")
    fun getWeeklyAnomalies(): Call<ApiResponse<WeeklyAnomalyResponse>>
}

/**
 * 공통 API 응답 래퍼
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
