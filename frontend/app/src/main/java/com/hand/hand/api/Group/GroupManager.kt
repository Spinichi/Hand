package com.hand.hand.api.Group

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object GroupManager {
    private const val TAG = "GroupManager"

    private fun api(): GroupInterface =
        RetrofitClient.getClient().create(GroupInterface::class.java)

    fun createGroup(
        name: String,
        groupType: String,
        onSuccess: (GroupData?) -> Unit,
        onError: (String) -> Unit
    ) {
        val req = GroupCreateRequest(name = name, groupType = groupType)
        api().createGroup(req).enqueue(object : Callback<WrappedResponse<GroupData>> {
            override fun onResponse(call: Call<WrappedResponse<GroupData>>, response: Response<WrappedResponse<GroupData>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        onSuccess(body.data)
                    } else {
                        val msg = body?.message ?: "Unknown server response"
                        Log.e(TAG, "createGroup server error: $msg")
                        onError(msg)
                    }
                } else {
                    val code = response.code()
                    val err = response.errorBody()?.string()
                    Log.e(TAG, "createGroup http error: code=$code body=$err")
                    onError("HTTP $code")
                }
            }

            override fun onFailure(call: Call<WrappedResponse<GroupData>>, t: Throwable) {
                Log.e(TAG, "createGroup failure", t)
                onError(t.message ?: "Network error")
            }
        })
    }

    fun joinGroup(
        inviteCode: String,
        onSuccess: (GroupData?) -> Unit,
        onError: (String) -> Unit
    ) {
        val req = GroupJoinRequest(inviteCode = inviteCode)
        api().joinGroup(req).enqueue(object : Callback<WrappedResponse<GroupData>> {
            override fun onResponse(call: Call<WrappedResponse<GroupData>>, response: Response<WrappedResponse<GroupData>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        onSuccess(body.data)
                    } else {
                        onError(body?.message ?: "Unknown server response")
                    }
                } else {
                    onError("HTTP ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WrappedResponse<GroupData>>, t: Throwable) {
                onError(t.message ?: "Network error")
            }
        })
    }
}
