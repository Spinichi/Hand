package com.hand.hand.api.Sleep

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SleepManager {
    companion object {
        private const val TAG = "SleepManager"

        private val httpCall: SleepInterface =
            RetrofitClient.getClient().create(SleepInterface::class.java)

        /**
         * 수면 데이터 저장
         *
         * @param sleepStartTime 수면 시작 시간 (ISO-8601 형식)
         * @param sleepEndTime 수면 종료 시간 (ISO-8601 형식)
         * @param onSuccess 성공 콜백
         * @param onFailure 실패 콜백
         */
        fun saveSleep(
            sleepStartTime: String,
            sleepEndTime: String,
            onSuccess: ((SleepCreateData) -> Unit)? = null,
            onFailure: ((Throwable) -> Unit)? = null
        ) {
            val request = SleepRequest(sleepStartTime, sleepEndTime)

            Log.d(TAG, "📤 수면 데이터 저장 요청: start=$sleepStartTime, end=$sleepEndTime")

            httpCall.createSleep(request).enqueue(object : Callback<SleepCreateResponse> {
                override fun onResponse(
                    call: Call<SleepCreateResponse>,
                    response: Response<SleepCreateResponse>
                ) {
                    val body = response.body()
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) {
                        null
                    }

                    Log.d(TAG, "📥 응답코드=${response.code()} body=$body errorBody=$errorBodyStr")

                    if (response.isSuccessful && body != null && body.success) {
                        Log.d(TAG, "✅ 수면 데이터 저장 성공: ${body.data}")
                        body.data?.let { onSuccess?.invoke(it) }
                    } else {
                        val msg = "수면 데이터 저장 실패: ${response.code()} - ${body?.message ?: response.message()}"
                        Log.e(TAG, msg)
                        onFailure?.invoke(Throwable(msg))
                    }
                }

                override fun onFailure(call: Call<SleepCreateResponse>, t: Throwable) {
                    Log.e(TAG, "🚨 통신 실패: ${t.localizedMessage}", t)
                    onFailure?.invoke(t)
                }
            })
        }

        /**
         * 오늘의 수면 데이터 조회
         *
         * @param onSuccess 성공 콜백
         * @param onFailure 실패 콜백
         */
        fun getTodaySleep(
            onSuccess: ((SleepData?) -> Unit)? = null,
            onFailure: ((Throwable) -> Unit)? = null
        ) {
            Log.d(TAG, "📤 오늘의 수면 데이터 조회 요청")

            httpCall.getTodaySleep().enqueue(object : Callback<SleepResponse> {
                override fun onResponse(
                    call: Call<SleepResponse>,
                    response: Response<SleepResponse>
                ) {
                    val body = response.body()
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) {
                        null
                    }

                    Log.d(TAG, "📥 응답코드=${response.code()} body=$body errorBody=$errorBodyStr")

                    if (response.isSuccessful && body != null && body.success) {
                        Log.d(TAG, "✅ 오늘의 수면 데이터 조회 성공: ${body.data}")
                        onSuccess?.invoke(body.data)
                    } else {
                        val msg = "오늘의 수면 데이터 조회 실패: ${response.code()} - ${body?.message ?: response.message()}"
                        Log.e(TAG, msg)
                        onFailure?.invoke(Throwable(msg))
                    }
                }

                override fun onFailure(call: Call<SleepResponse>, t: Throwable) {
                    Log.e(TAG, "🚨 통신 실패: ${t.localizedMessage}", t)
                    onFailure?.invoke(t)
                }
            })
        }
    }
}