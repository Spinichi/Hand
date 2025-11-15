package com.hand.hand.api.Diary

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DiaryInterface {
    @GET("v1/diaries/my")
    fun getMyDiaryList(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("pageable.page") page: Int = 0,
        @Query("pageable.size") size: Int = 31
    ): Call<DiaryResponse>

    @GET("v1/diaries/{sessionId}")
    fun getDiaryDetail(
        @Path("sessionId") sessionId: Long
    ): Call<DiaryDetailResponse>



}