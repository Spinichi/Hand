package com.hand.hand.api.Diary

import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object DiaryManager {

    private val api = RetrofitClient.getClient().create(DiaryInterface::class.java)

    // 다이어리 목록 조회
    fun getMyDiaryList(
        startDate: String,
        endDate: String,
        page: Int = 0,
        size: Int = 30,
        onSuccess: (List<DiaryItem>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        api.getMyDiaryList(startDate, endDate, page, size)
            .enqueue(object : Callback<DiaryResponse> {
                override fun onResponse(call: Call<DiaryResponse>, response: Response<DiaryResponse>) {
                    if (!response.isSuccessful) {
                        onFailure(Throwable("HTTP ${response.code()}"))
                        return
                    }
                    val items = response.body()?.data?.content?.map { it.toDiaryItem() } ?: emptyList()
                    onSuccess(items)
                }

                override fun onFailure(call: Call<DiaryResponse>, t: Throwable) {
                    onFailure(t)
                }
            })
    }

    // 다이어리 상세 조회
    fun getDiaryDetail(
        sessionId: Long,
        onSuccess: (DiaryDetailResponse) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        api.getDiaryDetail(sessionId)  // <-- 여기 api로 수정!
            .enqueue(object : Callback<DiaryDetailResponse> {
                override fun onResponse(
                    call: Call<DiaryDetailResponse>,
                    response: Response<DiaryDetailResponse>
                ) {
                    response.body()?.let { onSuccess(it) }
                        ?: onFailure(Throwable("Empty body"))
                }

                override fun onFailure(call: Call<DiaryDetailResponse>, t: Throwable) {
                    onFailure(t)
                }
            })
    }
}
