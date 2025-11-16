// WriteInterface.kt

package com.hand.hand.api.Write

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface WriteInterface {

    // POST /diaries/start
    @POST("v1/diaries/start")
    fun startDiary(): Call<DiaryStartResponse>

    @POST("v1/diaries/{sessionId}/answer")
    fun sendAnswer(
        @Path("sessionId") sessionId: Long,
        @Body body: DiaryAnswerRequest
    ): Call<DiaryAnswerResponse>

}
