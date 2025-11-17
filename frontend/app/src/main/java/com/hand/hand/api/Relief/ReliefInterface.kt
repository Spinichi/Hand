package com.hand.hand.api.Relief

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReliefInterface {

    // ═══════════════════════════════════════════════════════════
    // 워치 자동 완화법용 (Auto Relief from Watch)
    // ═══════════════════════════════════════════════════════════

    /**
     * 완화법 세션 시작 (워치 자동용)
     * POST /v1/relief/sessions/start
     * Authorization: Bearer {token} (자동 추가)
     */
    @POST("v1/relief/sessions/start")
    fun startSession(@Body request: ReliefStartRequest): Call<ReliefApiResponse<ReliefStartResponse>>

    /**
     * 완화법 세션 종료 (워치 자동용)
     * POST /v1/relief/sessions/{id}/end
     * Authorization: Bearer {token} (자동 추가)
     */
    @POST("v1/relief/sessions/{id}/end")
    fun endSession(
        @Path("id") sessionId: Long,
        @Body request: ReliefEndRequest
    ): Call<ReliefApiResponse<ReliefEndResponse>>

    // ═══════════════════════════════════════════════════════════
    // 앱 수동 완화법용 (Manual Relief from App - careSafeZone)
    // ═══════════════════════════════════════════════════════════

    /**
     * 릴리프 세션 시작 (앱 수동용)
     * POST /v1/relief/sessions/start
     */
    @POST("v1/relief/sessions/start")
    fun startReliefSession(
        @Body request: ReliefSessionStartRequest
    ): Call<ReliefSessionStartResponse>

    /**
     * 릴리프 세션 종료 (앱 수동용)
     * POST /v1/relief/sessions/{id}/end
     */
    @POST("v1/relief/sessions/{id}/end")
    fun endReliefSession(
        @Path("id") sessionId: Long,
        @Body request: ReliefEndRequest
    ): Call<ReliefApiResponse<ReliefEndResponse>>

    /**
     * 오늘의 세션 개수 조회
     * GET /v1/relief/today/count
     */
    @GET("v1/relief/today/count")
    fun getTodaySessionCount(): Call<ReliefApiResponse<TodayCountData>>
}
