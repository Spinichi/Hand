// file: com/hand/hand/api/ManagerCounseling/ManagerCounselingManager.kt
package com.hand.hand.api.ManagerCounseling

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ManagerCounselingManager {
    private const val TAG = "ManagerCounselingMgr"

    private fun api(): ManagerCounselingInterface =
        RetrofitClient.getClient().create(ManagerCounselingInterface::class.java)

    /**
     * 최신 상담 리포트 조회
     * RetrofitClient 에서 토큰을 붙이므로 별도 토큰 파라미터는 필요없음.
     */
    fun getLatestCounseling(
        groupId: Int,
        userId: Int,
        onSuccess: (ManagerCounselingData?) -> Unit,
        onError: (String) -> Unit
    ) {
        api().getLatestCounseling(groupId, userId).enqueue(object : Callback<WrappedResponse<ManagerCounselingData>> {
            override fun onResponse(
                call: Call<WrappedResponse<ManagerCounselingData>>,
                response: Response<WrappedResponse<ManagerCounselingData>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        onSuccess(body.data)
                    } else {
                        val msg = body?.message ?: "Unknown server response"
                        Log.e(TAG, "getLatestCounseling server error: $msg")
                        onError(msg)
                    }
                } else {
                    val code = response.code()
                    val err = response.errorBody()?.string()
                    Log.e(TAG, "getLatestCounseling http error: code=$code body=$err")

                    if (code == 400 && err?.contains("상담 보고서를 찾을 수 없습니다.") == true) {
                        onSuccess(null)
                    } else { onError("HTTP $code") }


                }
            }

            override fun onFailure(call: Call<WrappedResponse<ManagerCounselingData>>, t: Throwable) {
                Log.e(TAG, "getLatestCounseling failure", t)
                onError(t.message ?: "Network error")
            }
        })
    }

    fun analyzeCounseling(
        groupId: Int,
        userId: Int,
        startDate: String,
        endDate: String,
        onSuccess: (ManagerCounselingData?) -> Unit,
        onError: (String) -> Unit
    ) {
        val req = ManagerCounselingAnalyzeRequest(
            groupId = groupId,
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        api().analyzeCounseling(req)
            .enqueue(object : Callback<WrappedResponse<ManagerCounselingData>> {
                override fun onResponse(
                    call: Call<WrappedResponse<ManagerCounselingData>>,
                    response: Response<WrappedResponse<ManagerCounselingData>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success) {
                            onSuccess(body.data)
                        } else {
                            val msg = body?.message ?: "Unknown server response"
                            Log.e(TAG, "analyzeCounseling server error: $msg")
                            onError(msg)
                        }
                    } else {
                        val code = response.code()
                        val err = response.errorBody()?.string()
                        Log.e(TAG, "analyzeCounseling http error: code=$code body=$err")
                        onError("HTTP $code")
                    }
                }

                override fun onFailure(
                    call: Call<WrappedResponse<ManagerCounselingData>>,
                    t: Throwable
                ) {
                    Log.e(TAG, "analyzeCounseling failure", t)
                    onError(t.message ?: "Network error")
                }
            })
    }

}
