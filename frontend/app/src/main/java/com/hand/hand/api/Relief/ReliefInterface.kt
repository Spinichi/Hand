//ReliefInterface.kt
package com.hand.hand.api.Relief

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ReliefInterface {

    // POST https://gatewaytohand.store/api/v1/relief/sessions/start
    @POST("v1/relief/sessions/start")
    fun startReliefSession(
//        @Header("Authorization") authorization: String,  // "Bearer xxx"
        @Body request: ReliefSessionStartRequest
    ): Call<ReliefSessionStartResponse>

    @POST("v1/relief/sessions/{id}/end")
    fun endReliefSession(
        @Path("id") sessionId: Long,
        @Body request: ReliefSessionEndRequest
    ): Call<Void>  // 응답이 {}라서 Void로 처리
}
