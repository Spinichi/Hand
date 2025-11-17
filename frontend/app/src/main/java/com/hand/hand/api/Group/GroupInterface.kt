package com.hand.hand.api.Group

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GroupInterface {
    @GET("v1/groups")
    fun getGroups(): Call<WrappedResponse<List<GroupData>>>

    @POST("v1/groups")
    fun createGroup(@Body req: GroupCreateRequest): Call<WrappedResponse<GroupData>>

    @POST("v1/groups/join")
    fun joinGroup(@Body req: GroupJoinRequest): Call<WrappedResponse<GroupData>>

    @GET("v1/groups/{id}")
    fun getGroupInfo(@Path("id") groupId: Int): Call<WrappedResponse<GroupData>>

    @GET("v1/groups/{groupId}/statistics/anomalies")
    fun getGroupAnomalies(@Path("groupId") groupId: Int): Call<WrappedResponse<GroupAnomaliesData>>

    @GET("v1/groups/{groupId}/members")
    fun getGroupMembers(@Path("groupId") groupId: Int): Call<WrappedResponse<List<GroupMemberData>>>

    @PUT("v1/groups/{groupId}/members/{userId}/notes")
    fun updateMemberNotes(
        @Path("groupId") groupId: Int,
        @Path("userId") userId: Int,
        @Body req: MemberNotesUpdateRequest
    ): Call<WrappedResponse<GroupMemberData>>
}
