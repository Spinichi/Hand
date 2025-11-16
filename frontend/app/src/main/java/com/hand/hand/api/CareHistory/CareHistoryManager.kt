package com.hand.hand.api.CareHistory

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CareHistoryManager {

    private const val TAG = "CareHistoryManager"

    private val api: CareHistoryInterface by lazy {
        RetrofitClient.getClient().create(CareHistoryInterface::class.java)
    }

    /**
     * 히스토리 조회
     * @param page 조회 페이지 (0부터 시작)
     * @param size 한 페이지 사이즈
     * @param onSuccess 성공 콜백 (CareHistoryResponse)
     * @param onFailure 실패 콜백 (Throwable)
     */
    fun getCareHistory(
        page: Int = 0,
        size: Int = 7,
        onSuccess: (CareHistoryResponse) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        api.getCareHistory(page, size).enqueue(object : Callback<CareHistoryResponse> {
            override fun onResponse(
                call: Call<CareHistoryResponse>,
                response: Response<CareHistoryResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        onFailure(Throwable("Response body is null"))
                    }
                } else {
                    Log.e(TAG, "Request failed: ${response.code()} / ${response.message()}")
                    onFailure(Throwable("Request failed: ${response.code()} / ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<CareHistoryResponse>, t: Throwable) {
                Log.e(TAG, "Network call failed", t)
                onFailure(t)
            }
        })
    }
}
