package com.hand.hand.api.Anomaly

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 이상치 데이터 API 호출 관리 클래스
 */
class AnomalyManager {

    private val anomalyService: AnomalyInterface =
        RetrofitClient.getClient().create(AnomalyInterface::class.java)

    companion object {
        private const val TAG = "AnomalyManager"
        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        /**
         * Date를 yyyy-MM-dd 문자열로 변환
         */
        private fun formatDate(date: Date): String {
            return dateFormatter.format(date)
        }
    }

    /**
     * 특정 날짜의 이상치 개수 조회
     *
     * @param date 조회할 날짜 (null이면 오늘)
     * @param onSuccess 성공 시 콜백 (이상치 개수)
     * @param onError 실패 시 콜백 (에러 메시지)
     */
    fun getAnomalyCount(
        date: Date? = null,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val dateString = date?.let { formatDate(it) }

        anomalyService.getDailyAnomalies(dateString).enqueue(object : Callback<ApiResponse<DailyAnomalyResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<DailyAnomalyResponse>>,
                response: Response<ApiResponse<DailyAnomalyResponse>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val count = body.data.anomalyCount
                        Log.d(TAG, "이상치 개수 조회 성공: ${count}개")
                        onSuccess(count)
                    } else {
                        val errorMsg = body?.message ?: "알 수 없는 오류"
                        Log.e(TAG, "이상치 개수 조회 실패: $errorMsg")
                        onError(errorMsg)
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    Log.e(TAG, "이상치 개수 조회 실패: $errorMsg")
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<DailyAnomalyResponse>>, t: Throwable) {
                Log.e(TAG, "이상치 개수 조회 네트워크 오류", t)
                onError(t.message ?: "네트워크 오류")
            }
        })
    }

    /**
     * 특정 날짜의 이상치 상세 목록 조회
     *
     * @param date 조회할 날짜 (null이면 오늘)
     * @param onSuccess 성공 시 콜백 (일일 이상치 데이터)
     * @param onError 실패 시 콜백 (에러 메시지)
     */
    fun getDailyAnomalies(
        date: Date? = null,
        onSuccess: (DailyAnomalyResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val dateString = date?.let { formatDate(it) }

        anomalyService.getDailyAnomalies(dateString).enqueue(object : Callback<ApiResponse<DailyAnomalyResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<DailyAnomalyResponse>>,
                response: Response<ApiResponse<DailyAnomalyResponse>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Log.d(TAG, "일일 이상치 조회 성공: ${body.data.anomalyCount}개")
                        onSuccess(body.data)
                    } else {
                        val errorMsg = body?.message ?: "알 수 없는 오류"
                        Log.e(TAG, "일일 이상치 조회 실패: $errorMsg")
                        onError(errorMsg)
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    Log.e(TAG, "일일 이상치 조회 실패: $errorMsg")
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<DailyAnomalyResponse>>, t: Throwable) {
                Log.e(TAG, "일일 이상치 조회 네트워크 오류", t)
                onError(t.message ?: "네트워크 오류")
            }
        })
    }

    /**
     * 주간 이상치 목록 조회 (최근 7일)
     *
     * @param onSuccess 성공 시 콜백 (주간 이상치 데이터)
     * @param onError 실패 시 콜백 (에러 메시지)
     */
    fun getWeeklyAnomalies(
        onSuccess: (WeeklyAnomalyResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        anomalyService.getWeeklyAnomalies()
            .enqueue(object : Callback<ApiResponse<WeeklyAnomalyResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<WeeklyAnomalyResponse>>,
                    response: Response<ApiResponse<WeeklyAnomalyResponse>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true && body.data != null) {
                            Log.d(TAG, "주간 이상치 조회 성공: 총 ${body.data.totalAnomalyCount}개")
                            onSuccess(body.data)
                        } else {
                            val errorMsg = body?.message ?: "알 수 없는 오류"
                            Log.e(TAG, "주간 이상치 조회 실패: $errorMsg")
                            onError(errorMsg)
                        }
                    } else {
                        val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                        Log.e(TAG, "주간 이상치 조회 실패: $errorMsg")
                        onError(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ApiResponse<WeeklyAnomalyResponse>>, t: Throwable) {
                    Log.e(TAG, "주간 이상치 조회 네트워크 오류", t)
                    onError(t.message ?: "네트워크 오류")
                }
            })
    }
}
