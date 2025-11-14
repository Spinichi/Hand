package com.hand.hand.api.Report

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface ReportInterface {

    // 주간 전체 조히ㅗ
    @GET("v1/reports/weekly")
    fun getWeeklyReports(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "weekStartDate,desc"
    ): Call<WeeklyReportsResponse>

    // 주간 상세
    @GET("v1/reports/weekly/{reportId}")
    fun getWeeklyReportDetail(
        @Path("reportId") reportId: Long
    ): Call<WeeklyReportDetailResponse>


    // 월간 전체 조회
    @GET("v1/reports/monthly")
    fun getMonthlyReports(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "monthStartDate,desc"
    ): Call<MonthlyReportsResponse>

    // 월간 상세
    @GET("v1/reports/monthly/{reportId}")
    fun getMonthlyReportDetail(
        @Path("reportId") reportId: Long
    ): Call<MonthlyReportDetailResponse>
}
