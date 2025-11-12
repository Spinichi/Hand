package com.hand.hand.api.SignUp

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpManager {

    companion object {
        private val httpCall: SignUpInterface =
            RetrofitClient.getClient().create(SignUpInterface::class.java)

        /**
         * 회원가입 요청
         */
        fun signup(
            signupRequest: SignupRequest,
            onSuccess: (SignupResponse) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            Log.d("SignUpManager", "📤 회원가입 요청: $signupRequest")

            httpCall.signup(signupRequest).enqueue(object : Callback<SignupResponse> {
                override fun onResponse(
                    call: Call<SignupResponse>,
                    response: Response<SignupResponse>
                ) {
                    Log.d(
                        "SignUpManager",
                        "📥 서버 응답 코드: ${response.code()} / body: ${response.body()} / errorBody: ${response.errorBody()?.string()}"
                    )

                    if (response.isSuccessful && response.body() != null) {
                        Log.d("SignUpManager", "✅ 회원가입 성공")
                        onSuccess(response.body()!!)
                    } else {
                        val errorMessage =
                            "회원가입 실패: ${response.code()} - ${response.message()}"
                        Log.e("SignUpManager", errorMessage)
                        onFailure(Throwable(errorMessage))
                    }
                }

                override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                    Log.e("SignUpManager", "🚨 통신 실패: ${t.localizedMessage}", t)
                    onFailure(t)
                }
            })
        }
    }
}
