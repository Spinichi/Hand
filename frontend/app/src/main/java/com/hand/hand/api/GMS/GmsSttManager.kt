// GmsSttManager.kt

package com.hand.hand.api.GMS

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object GmsSttManager {

    private const val TAG = "GMS_STT"

    private val api: GmsSttInterface =
        GmsRetrofitClient.instance.create(GmsSttInterface::class.java)

    fun requestStt(
        audioFile: File,
        mimeType: String = "audio/m4a",
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        try {
            val requestFile = audioFile.asRequestBody(mimeType.toMediaType())
            val filePart = MultipartBody.Part.createFormData(
                "file",
                audioFile.name,
                requestFile
            )

            val model = "whisper-1".toRequestBody("text/plain".toMediaType())

            val language = "ko".toRequestBody("text/plain".toMediaType())

            api.transcribe(filePart, model, language = language).enqueue(object : Callback<GmsSttResponse> {
                override fun onResponse(
                    call: Call<GmsSttResponse>,
                    response: Response<GmsSttResponse>
                ) {

                    Log.d(TAG, "onResponse() code=${response.code()}")

                    val body = response.body()
                    Log.d(TAG, "onResponse() body 객체 = $body")  // GmsSttResponse(text=...)

                    if (response.isSuccessful && body != null) {
                        Log.d(TAG, "STT 최종 text = '${body.text}'")
                        onSuccess(body.text)
//                        val body = response.body()
//                        if (body != null) {
//                            Log.d(TAG, "GMS STT 성공: ${body.text}")
//                            onSuccess(body.text)
                        } else {
                        val err = response.errorBody()?.string()
                        Log.e(TAG, "STT 실패: code=${response.code()}, errorBody=$err")
                            onFailure(RuntimeException("응답 body null"))
                        }
//                    } else {
//                        onFailure(
//                            RuntimeException(
//                                "STT 실패: ${response.code()} - ${response.errorBody()?.string()}"
//                            )
//                        )
//                    }
                }

                override fun onFailure(call: Call<GmsSttResponse>, t: Throwable) {
                    Log.e(TAG, "GMS STT 에러", t)
                    onFailure(t)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "requestStt 예외", e)
            onFailure(e)
        }
    }
}
