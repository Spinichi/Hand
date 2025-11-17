// file: com/hand/hand/api/ManagerCounseling/ManagerCounselingInterface.kt
package com.hand.hand.api.ManagerCounseling

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.http.POST

interface ManagerCounselingInterface {
    // GET /api/v1/manager/counseling/latest?groupId=..&userId=..
    @GET("v1/manager/counseling/latest")
    fun getLatestCounseling(
        @Query("groupId") groupId: Int,
        @Query("userId") userId: Int
    ): Call<WrappedResponse<ManagerCounselingData>>

    @POST("v1/manager/counseling/analyze")
    fun analyzeCounseling(
        @Body req: ManagerCounselingAnalyzeRequest
    ): Call<WrappedResponse<ManagerCounselingData>>

}
