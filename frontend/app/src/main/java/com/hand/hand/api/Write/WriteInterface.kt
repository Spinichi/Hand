// WriteInterface.kt

package com.hand.hand.api.Write

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WriteInterface {

    // GET /diaries/today - 오늘의 다이어리 상태 조회
    @GET("v1/diaries/today")
    fun getTodayDiaryStatus(): Call<TodayDiaryStatusResponse>

    // POST /diaries/start
    @POST("v1/diaries/start")
    fun startDiary(): Call<DiaryStartResponse>

    @POST("v1/diaries/{sessionId}/answer")
    fun sendAnswer(
        @Path("sessionId") sessionId: Long,
        @Body body: DiaryAnswerRequest
    ): Call<DiaryAnswerResponse>

    // 다이어리 완료 요청
    @POST("v1/diaries/{sessionId}/complete")
    fun completeDiary(
        @Path("sessionId") sessionId: Long
    ): Call<DiaryCompleteResponse>
}
