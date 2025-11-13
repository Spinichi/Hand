// IndividualUserManager.kt

package com.hand.hand.api.SignUp

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IndividualUserManager {
    companion object {
        private val httpCall: IndividualUserInterface =
            RetrofitClient.getClient().create(IndividualUserInterface::class.java)

        /**
         * 1) 개인정보 등록 (POST /individual-users)
         */
        fun registerIndividualUser(
            name: String,
            age: Int,
            gender: String,
            job: String,
            height: Int,
            weight: Int,
            disease: String,
            residenceType: String,
            diaryReminderEnabled: Boolean,
            hour: Int,
            minute: Int,
            onSuccess: (IndividualUserData) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {

            val notificationTimeStr =
                if (diaryReminderEnabled) {
                    String.format("%02d:%02d", hour, minute)
                } else {
                    // 알림 OFF일 때 백엔드랑 맞춘 기본값 (원하면 "00:00" 말고 null 허용으로 바꾸라고 요청해도 됨)
                    "00:00"
                }

            val req = IndividualUserRequest(
                name = name,
                age = age,
                gender = gender,
                job = job,
                height = height,
                weight = weight,
                disease = disease,
                residenceType = residenceType,
                diaryReminderEnabled = diaryReminderEnabled,
                notificationTime = notificationTimeStr
            )

            Log.d("IndividualUserManager", "개인정보 등록 요청: $req")

            httpCall.createIndividualUser(req).enqueue(object :
                Callback<ApiResponse<IndividualUserData>> {
                override fun onResponse(
                    call: Call<ApiResponse<IndividualUserData>>,
                    response: Response<ApiResponse<IndividualUserData>>
                ) {
                    val body = response.body()
                    Log.d("IndividualUserManager", "response: $response, body: $body")

                    if (response.isSuccessful && body?.data != null) {
                        onSuccess(body.data)
                    } else {
                        onFailure(
                            RuntimeException(
                                "개인정보 등록 실패 code=${response.code()} msg=${body?.message}"
                            )
                        )
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<IndividualUserData>>,
                    t: Throwable
                ) {
                    Log.e("IndividualUserManager", "개인정보 등록 실패", t)
                    onFailure(t)
                }
            })
        }

        /**
         * 2) 이미 개인정보가 등록돼 있는지 체크 (GET /individual-users/me)
         *    → 나중에 '있으면 스킵 / 없으면 설문으로' 할 때 쓸 예정
         */
        fun hasIndividualUser(
            onResult: (exists: Boolean, data: IndividualUserData?) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            httpCall.getMyIndividualUser().enqueue(object :
                Callback<ApiResponse<IndividualUserData>> {
                override fun onResponse(
                    call: Call<ApiResponse<IndividualUserData>>,
                    response: Response<ApiResponse<IndividualUserData>>
                ) {
                    val body = response.body()
                    val errorBodyStr = try { response.errorBody()?.string() } catch (_: Exception) { null }

                    Log.d("IndividualUserManager", "개인정보 등록 응답")
                    Log.d("IndividualUserManager", "code = ${response.code()}")
                    Log.d("IndividualUserManager", "body = $body")
                    Log.d("IndividualUserManager", "errorBody = $errorBodyStr")


                    if (response.isSuccessful) {
                        val exists = body?.data != null
                        onResult(exists, body?.data)
                    } else if (response.code() == 404) {
                        // 백엔드에서 "아직 없음"을 404로 주면 이렇게 처리
                        onResult(false, null)
                    } else {
                        onFailure(
                            RuntimeException(
                                "check 실패 code=${response.code()} msg=${body?.message}"
                            )
                        )
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<IndividualUserData>>,
                    t: Throwable
                ) {
                    Log.e("IndividualUserManager", "hasIndividualUser 실패", t)
                    onFailure(t)
                }
            })
        }
    }
}
