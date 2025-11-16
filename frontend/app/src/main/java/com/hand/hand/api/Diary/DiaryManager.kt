package com.hand.hand.api.Diary

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object DiaryManager {

    private val api = RetrofitClient.getClient().create(DiaryInterface::class.java)

    /**
     * 🔹 다이어리 목록 조회
     */
    fun getMyDiaryList(
        startDate: String,
        endDate: String,
        page: Int = 0,
        size: Int = 30,
        onSuccess: (List<DiaryItem>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Log.d("DiaryManager", "📡 getMyDiaryList 호출: $startDate ~ $endDate")

        api.getMyDiaryList(startDate, endDate, page, size)
            .enqueue(object : Callback<DiaryResponse> {

                override fun onResponse(
                    call: Call<DiaryResponse>,
                    response: Response<DiaryResponse>
                ) {
                    Log.d("DiaryManager", "📥 응답 코드: ${response.code()}")

                    if (!response.isSuccessful) {
                        Log.e("DiaryManager", "❌ 응답 실패 HTTP ${response.code()}")
                        onFailure(Throwable("HTTP ${response.code()}"))
                        return
                    }

                    val body = response.body()
                    Log.d("DiaryManager", "📥 응답 body: $body")

                    val items = body?.data?.content?.map { it.toDiaryItem() } ?: emptyList()
                    onSuccess(items)
                }

                override fun onFailure(call: Call<DiaryResponse>, t: Throwable) {
                    Log.e("DiaryManager", "❌ 목록 조회 실패: ${t.message}")
                    onFailure(t)
                }
            })
    }

    /**
     * 🔹 다이어리 상세 조회
     */
    fun getDiaryDetail(
        sessionId: Long,
        onSuccess: (DiaryDetailResponse) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Log.d("DiaryManager", "📡 getDiaryDetail 호출: sessionId = $sessionId")

        api.getDiaryDetail(sessionId)
            .enqueue(object : Callback<DiaryDetailWrapper> {

                override fun onResponse(
                    call: Call<DiaryDetailWrapper>,
                    response: Response<DiaryDetailWrapper>
                ) {
                    Log.d("DiaryManager", "📥 응답 코드: ${response.code()}")

                    if (!response.isSuccessful) {
                        Log.e("DiaryManager", "❌ 상세 응답 실패 HTTP ${response.code()}")
                        onFailure(Throwable("HTTP ${response.code()}"))
                        return
                    }

                    val body = response.body()
                    Log.d("DiaryManager", "📥 상세 응답 body: $body")

                    if (body?.success == true && body.data != null) {
                        onSuccess(body.data)  // ✅ data 안의 DiaryDetailResponse 전달
                    } else {
                        onFailure(Throwable("Empty or unsuccessful response"))
                    }
                }

                override fun onFailure(call: Call<DiaryDetailWrapper>, t: Throwable) {
                    Log.e("DiaryManager", "❌ 상세 조회 실패: ${t.message}")
                    onFailure(t)
                }
            })
    }
}