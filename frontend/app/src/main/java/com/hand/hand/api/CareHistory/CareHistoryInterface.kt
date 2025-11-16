package com.hand.hand.api.CareHistory

import com.hand.hand.api.Diary.DiaryDetailWrapper
import com.hand.hand.api.Group.GroupCreateRequest
import com.hand.hand.api.Group.GroupData
import com.hand.hand.api.Group.WrappedResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CareHistoryInterface {
    @GET("v1/relief/history")
    fun getCareHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 7
    ): Call<CareHistoryResponse>
}
