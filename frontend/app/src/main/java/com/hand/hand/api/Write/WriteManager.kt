// WriteManager.kt

package com.hand.hand.api.Write

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WriteManager {
    companion object {
        private val httpCall: WriteInterface =
            RetrofitClient.getClient().create(WriteInterface::class.java)

        /**
         * 다이어리 세션 시작 POST /diaries/start
         */
        fun startDiary(
            onSuccess: (DiaryStartResponse) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            httpCall.startDiary().enqueue(object : Callback<DiaryStartResponse> {
                override fun onResponse(
                    call: Call<DiaryStartResponse>,
                    response: Response<DiaryStartResponse>
                ) {
                    val body = response.body()
                    val code = response.code()

                    Log.d("WriteManager", "startDiary 응답 코드: $code, body: $body")

                    if (response.isSuccessful && body != null) {
                        onSuccess(body)
                    } else {
                        onFailure(
                            RuntimeException("startDiary 실패: code=$code, body=$body")
                        )
                    }
                }

                override fun onFailure(call: Call<DiaryStartResponse>, t: Throwable) {
                    Log.e("WriteManager", "startDiary 통신 에러", t)
                    onFailure(t)
                }
            })
        }
        fun sendAnswer(
            sessionId: Long,
            answerText: String,
            onSuccess: (DiaryAnswerResponse) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            val req = DiaryAnswerRequest(answerText = answerText)
            Log.d("WriteManager", "sendAnswer 요청: sessionId=$sessionId, body=$req")

            httpCall.sendAnswer(sessionId, req).enqueue(object : Callback<DiaryAnswerResponse> {
                override fun onResponse(
                    call: Call<DiaryAnswerResponse>,
                    response: Response<DiaryAnswerResponse>
                ) {
                    val body = response.body()
                    val code = response.code()
                    Log.d("WriteManager", "sendAnswer 응답 코드: $code, body: $body")

                    if (response.isSuccessful && body != null) {
                        onSuccess(body)
                    } else {
                        onFailure(
                            RuntimeException("sendAnswer 실패: code=$code, body=$body")
                        )
                    }
                }

                override fun onFailure(call: Call<DiaryAnswerResponse>, t: Throwable) {
                    Log.e("WriteManager", "sendAnswer 통신 에러", t)
                    onFailure(t)
                }
            })
        }

    }
}
