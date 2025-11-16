// GmsSttInterface.kt

package com.hand.hand.api.GMS

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GmsSttInterface {

    @Multipart
    @POST("v1/audio/transcriptions")
    fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("language") language: RequestBody? = null
    ): Call<GmsSttResponse>
}
