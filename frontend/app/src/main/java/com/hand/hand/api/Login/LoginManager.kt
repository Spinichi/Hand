// LoginManager.kt
package com.hand.hand.api.Login

import android.util.Log
import com.hand.hand.api.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginManager {
    companion object {
        // ★ by lazy를 사용해 httpCall의 초기화를 실제로 사용할 때까지 지연시킵니다.
        private val httpCall: LoginInterface by lazy {
            com.hand.hand.api.RetrofitClient.getClient().create(LoginInterface::class.java)
        }

        fun login(
            email: String,
            password: String,
            onSuccess: (LoginResponse) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            val req = LoginRequest(email = email, password = password)
            Log.d("LoginManager", "로그인 요청: $req")

            httpCall.login(req).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    val body = response.body()
                    Log.d("LoginManager", "응답코드=${response.code()}, body=$body")

                    val authHeader = response.headers()["Authorization"]
                    val token = authHeader?.removePrefix("Bearer ")?.trim()

                    if (response.isSuccessful && !token.isNullOrBlank()) {
                        TokenManager.saveToken(token)
                        Log.d("LoginManager", "TokenManager에 토큰 저장 성공. 로그인 성공 처리합니다.")

                        if (body != null) {
                            onSuccess(body)
                        } else {
                            onFailure(Throwable("로그인 성공 응답을 받았으나 body가 비어있습니다."))
                        }
                    } else {
                        val msg = "로그인 실패 (토큰 없음 또는 응답 실패): 코드=${response.code()}, 메시지=${body?.message ?: response.message()}"
                        Log.e("LoginManager", msg)
                        onFailure(Throwable(msg))
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LoginManager", "통신 실패: ${t.localizedMessage}", t)
                    onFailure(t)
                }
            })
        }
    }
}
