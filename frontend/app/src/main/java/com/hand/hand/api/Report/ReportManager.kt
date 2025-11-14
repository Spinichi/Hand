package com.hand.hand.api.Report

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ReportManager {

    private val httpCall: ReportInterface =
        RetrofitClient.getClient().create(ReportInterface::class.java)

    /**
     * 주간 리포트 목록 조회
     */
    fun fetchWeeklyReports(
        page: Int = 0,
        size: Int = 20,
        onSuccess: (List<WeeklyReportItem>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Log.d("ReportManager", "주간 리포트 요청: page=$page, size=$size")

        httpCall.getWeeklyReports(page, size)
            .enqueue(object : Callback<WeeklyReportsResponse> {
            override fun onResponse(
                call: Call<WeeklyReportsResponse>,
                response: Response<WeeklyReportsResponse>
            ) {
                val body = response.body()
                val errorBodyStr = try {
                    response.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }

                Log.d(
                    "ReportManager",
                    "onResponse code=${response.code()} body=$body error=$errorBodyStr"
                )

                if (response.isSuccessful && body != null && body.success) {
                    val list = body.data?.content ?: emptyList()
                    onSuccess(list)
                } else {
                    onFailure(
                        RuntimeException(
                            "주간 리포트 응답 에러: code=${response.code()}, error=$errorBodyStr"
                        )
                    )
                }
            }

            override fun onFailure(call: Call<WeeklyReportsResponse>, t: Throwable) {
                Log.e("ReportManager", "주간 리포트 호출 실패", t)
                onFailure(t)
            }
        })
    }
    fun fetchMonthlyReports(
        page: Int = 0,
        size: Int = 20,
        onSuccess: (List<MonthlyReportItem>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Log.d("ReportManager", "월간 리포트 요청:   page=$page, size=$size")

        httpCall.getMonthlyReports(page, size).enqueue(object : Callback<MonthlyReportsResponse> {
            override fun onResponse(
                call: Call<MonthlyReportsResponse>,
                response: Response<MonthlyReportsResponse>
            ) {
                val body = response.body()
                val errorBodyStr = try { response.errorBody()?.string() } catch (_: Exception) { null }

                Log.d(
                    "ReportManager",
                    "monthly onResponse code=${response.code()} body=$body error=$errorBodyStr"
                )

                if (response.isSuccessful && body != null && body.success) {
                    val all = body.data?.content ?: emptyList()
                    val list = body.data?.content ?: emptyList()
                    onSuccess(list)
                } else {
                    onFailure(
                        RuntimeException(
                            "월간 리포트 응답 에러: code=${response.code()}, error=$errorBodyStr"
                        )
                    )
                }
            }

            override fun onFailure(call: Call<MonthlyReportsResponse>, t: Throwable) {
                Log.e("ReportManager", "월간 리포트 호출 실패", t)
                onFailure(t)
            }
        })
    }

    fun fetchWeeklyReportDetail(
        reportId: Long,
        onSuccess: (WeeklyReportDetail) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Log.d("ReportManager", "주간 리포트 상세 요청: id=$reportId")

        httpCall.getWeeklyReportDetail(reportId)
            .enqueue(object : Callback<WeeklyReportDetailResponse> {
                override fun onResponse(
                    call: Call<WeeklyReportDetailResponse>,
                    response: Response<WeeklyReportDetailResponse>
                ) {
                    val body = response.body()
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) { null }
                    Log.d(
                        "ReportManager",
                        "weekly detail onResponse code=${response.code()} body=$body error=$errorBody"
                    )

                    if (response.isSuccessful && body != null && body.success && body.data != null) {
                        onSuccess(body.data)
                    } else {
                        onFailure(
                            RuntimeException(
                                "주간 상세 리포트 응답 에러: code=${response.code()}, error=$errorBody"
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<WeeklyReportDetailResponse>, t: Throwable) {
                    Log.e("ReportManager", "주간 상세 리포트 호출 실패", t)
                    onFailure(t)
                }
            })
    }

    fun fetchMonthlyReportDetail(
        reportId: Long,
        onSuccess: (MonthlyReportDetail) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Log.d("ReportManager", "월간 리포트 상세 요청: id=$reportId")

        httpCall.getMonthlyReportDetail(reportId)
            .enqueue(object : Callback<MonthlyReportDetailResponse> {
                override fun onResponse(
                    call: Call<MonthlyReportDetailResponse>,
                    response: Response<MonthlyReportDetailResponse>
                ) {
                    val body = response.body()
                    val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }

                    Log.d(
                        "ReportManager",
                        "monthly detail onResponse code=${response.code()} body=$body error=$errorBody"
                    )

                    if (response.isSuccessful && body != null && body.success && body.data != null) {
                        onSuccess(body.data)
                    } else {
                        onFailure(
                            RuntimeException(
                                "월간 상세 리포트 응답 에러: code=${response.code()}, error=$errorBody"
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<MonthlyReportDetailResponse>, t: Throwable) {
                    Log.e("ReportManager", "월간 상세 리포트 호출 실패", t)
                    onFailure(t)
                }
            })
    }
}
