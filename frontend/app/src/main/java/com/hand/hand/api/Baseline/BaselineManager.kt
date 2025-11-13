package com.hand.hand.api.Baseline

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Baseline API Manager
 */
object BaselineManager {

    private const val TAG = "BaselineManager"
    private val httpCall: BaselineInterface = RetrofitClient.getClient().create(BaselineInterface::class.java)

    /**
     * 활성 Baseline 조회
     */
    fun getActiveBaseline(
        onSuccess: (BaselineResponse) -> Unit,
        onNotFound: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Log.d(TAG, "📤 활성 Baseline 조회 요청")

        httpCall.getActiveBaseline().enqueue(object : Callback<BaselineApiResponse<BaselineResponse>> {
            override fun onResponse(
                call: Call<BaselineApiResponse<BaselineResponse>>,
                response: Response<BaselineApiResponse<BaselineResponse>>
            ) {
                val body = response.body()
                val errorBodyStr = try {
                    response.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }

                Log.d(TAG, "📥 응답코드=${response.code()} body=$body errorBody=$errorBodyStr")

                when {
                    response.isSuccessful && body != null && body.success -> {
                        body.data?.let {
                            Log.d(TAG, "✅ Baseline 조회 성공: version=${it.version}, active=${it.isActive}")
                            onSuccess(it)
                        } ?: run {
                            Log.w(TAG, "⚠️ Baseline 데이터가 null")
                            onNotFound()
                        }
                    }
                    response.code() == 404 -> {
                        Log.w(TAG, "⚠️ Baseline 없음 (404)")
                        onNotFound()
                    }
                    else -> {
                        val msg = "Baseline 조회 실패: ${response.code()} - ${body?.message ?: response.message()}"
                        Log.e(TAG, msg)
                        onFailure(Throwable(msg))
                    }
                }
            }

            override fun onFailure(call: Call<BaselineApiResponse<BaselineResponse>>, t: Throwable) {
                Log.e(TAG, "🚨 통신 실패: ${t.localizedMessage}", t)
                onFailure(t)
            }
        })
    }

    /**
     * Baseline 계산 및 생성 (3일치 데이터 기준)
     */
    fun calculateBaseline(
        days: Int = 3,
        onSuccess: (BaselineResponse) -> Unit,
        onInsufficientData: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Log.d(TAG, "📤 Baseline 계산 요청: days=$days")

        httpCall.calculateBaseline(days).enqueue(object : Callback<BaselineApiResponse<BaselineResponse>> {
            override fun onResponse(
                call: Call<BaselineApiResponse<BaselineResponse>>,
                response: Response<BaselineApiResponse<BaselineResponse>>
            ) {
                val body = response.body()
                val errorBodyStr = try {
                    response.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }

                Log.d(TAG, "📥 응답코드=${response.code()} body=$body errorBody=$errorBodyStr")

                when {
                    response.isSuccessful && body != null && body.success -> {
                        body.data?.let {
                            Log.d(TAG, "✅ Baseline 생성 성공: version=${it.version}, count=${it.measurementCount}")
                            onSuccess(it)
                        } ?: run {
                            Log.e(TAG, "❌ Baseline 데이터가 null")
                            onFailure(Throwable("Baseline 데이터가 null"))
                        }
                    }
                    errorBodyStr?.contains("INSUFFICIENT_DATA") == true -> {
                        Log.w(TAG, "⚠️ 데이터 부족: $errorBodyStr")
                        onInsufficientData()
                    }
                    else -> {
                        val msg = "Baseline 생성 실패: ${response.code()} - ${body?.message ?: response.message()}"
                        Log.e(TAG, msg)
                        onFailure(Throwable(msg))
                    }
                }
            }

            override fun onFailure(call: Call<BaselineApiResponse<BaselineResponse>>, t: Throwable) {
                Log.e(TAG, "🚨 통신 실패: ${t.localizedMessage}", t)
                onFailure(t)
            }
        })
    }
}
