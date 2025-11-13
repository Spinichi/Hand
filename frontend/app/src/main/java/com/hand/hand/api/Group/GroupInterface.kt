package com.hand.hand.api.Group

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface GroupInterface {
    @POST("v1/groups")
    fun createGroup(@Body req: GroupCreateRequest): Call<WrappedResponse<GroupData>>

    @POST("v1/groups/join")
    fun joinGroup(@Body req: GroupJoinRequest): Call<WrappedResponse<GroupData>>
}
