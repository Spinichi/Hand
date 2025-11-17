// WriteManager.kt

package com.hand.hand.api.Write

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WriteManager {
    companion object {

        private const val TAG = "WriteManager"
        private val httpCall: WriteInterface =
            RetrofitClient.getClient().create(WriteInterface::class.java)

        /**
         * 오늘의 다이어리 상태 조회 GET /diaries/today
         */
        fun getTodayDiaryStatus(
            onSuccess: (TodayDiaryStatusResponse) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            Log.d(TAG, "📤 오늘의 다이어리 상태 조회 요청")

            httpCall.getTodayDiaryStatus().enqueue(object : Callback<TodayDiaryStatusResponse> {
                override fun onResponse(
                    call: Call<TodayDiaryStatusResponse>,
                    response: Response<TodayDiaryStatusResponse>
                ) {
                    val body = response.body()
                    val code = response.code()

                    Log.d(TAG, "getTodayDiaryStatus 응답 코드: $code, body: $body")

                    if (response.isSuccessful && body != null) {
                        Log.d(TAG, "✅ 오늘의 다이어리 상태: ${body.data?.status}")
                        onSuccess(body)
                    } else {
                        onFailure(
                            RuntimeException("getTodayDiaryStatus 실패: code=$code, body=$body")
                        )
                    }
                }

                override fun onFailure(call: Call<TodayDiaryStatusResponse>, t: Throwable) {
                    Log.e(TAG, "❌ getTodayDiaryStatus 통신 에러", t)
                    onFailure(t)
                }
            })
        }

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

        // 다이어리 완료 요청
        fun completeDiary(
            sessionId: Long,
            onSuccess: (DiaryCompleteResponse) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            Log.d(TAG, "다이어리 완료 요청: sessionId=$sessionId")

            httpCall.completeDiary(sessionId).enqueue(object : Callback<DiaryCompleteResponse> {
                override fun onResponse(
                    call: Call<DiaryCompleteResponse>,
                    response: Response<DiaryCompleteResponse>
                ) {
                    val body = response.body()
                    Log.d(TAG, "completeDiary 응답: code=${response.code()}, body=$body")

                    if (body != null) {
                        onSuccess(body)
                    } else {
                        onFailure(IllegalStateException("응답 body 없음"))
                    }
                }

                override fun onFailure(call: Call<DiaryCompleteResponse>, t: Throwable) {
                    Log.e(TAG, "completeDiary 실패", t)
                    onFailure(t)
                }
            })

        }
    }}
