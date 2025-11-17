package com.hand.hand.api.riskToday

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RiskTodayManager {

    private val api: riskTodayInterface =
        RetrofitClient.getClient().create(riskTodayInterface::class.java)

    // ✔ 오늘의 점수 존재 여부 조회
    fun checkRiskTodayExists(
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        api.riskTodayExists().enqueue(object : Callback<RiskTodayExistsResponse> {
            override fun onResponse(
                call: Call<RiskTodayExistsResponse>,
                response: Response<RiskTodayExistsResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onSuccess(body.data.exists)
                    } else {
                        onError("응답이 null입니다.")
                    }
                } else {
                    onError("서버 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<RiskTodayExistsResponse>, t: Throwable) {
                onError("네트워크 오류: ${t.message}")
            }
        })
    }

    // ✔ 오늘의 점수 상세 조회
    fun getRiskToday(
        onSuccess: (RiskTodayData) -> Unit,
        onError: (String) -> Unit
    ) {
        api.riskToday().enqueue(object : Callback<RiskTodayResponse> {
            override fun onResponse(
                call: Call<RiskTodayResponse>,
                response: Response<RiskTodayResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onSuccess(body.data)
                    } else {
                        onError("응답 body가 null입니다.")
                    }
                } else {
                    onError("서버 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<RiskTodayResponse>, t: Throwable) {
                onError("네트워크 오류: ${t.message}")
            }
        })
    }
}

