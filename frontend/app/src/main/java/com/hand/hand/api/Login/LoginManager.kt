// LoginManager.kt
package com.hand.hand.api.Login

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginManager {
    companion object {
        private val httpCall: LoginInterface =
            RetrofitClient.getClient().create(LoginInterface::class.java)

        /**
         * 로그인 요청
         */
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
                    val errorBodyStr = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    val authHeader = response.headers()["Authorization"]
                    val setCookie  = response.headers()["Set-Cookie"]
                    Log.d("LoginManager", "headers Authorization=$authHeader, Set-Cookie=$setCookie")

                    //  Authorization 헤더에서 Bearer 토큰 추출 → RetrofitClient에 주입
                    authHeader
                        ?.removePrefix("Bearer ")
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { token ->
                            RetrofitClient.setAccessToken(token)
                            Log.d("LoginManager", "AccessToken set to RetrofitClient")
                        }

                    Log.d(
                        "LoginManager",
                        "응답코드=${response.code()} body=$body errorBody=$errorBodyStr"
                    )
                    if (response.isSuccessful && body != null) {
                        Log.d("LoginManager", "로그인 성공: ${body.message}")
                        onSuccess(body)
                    } else {
                        val msg = "로그인 실패: ${response.code()} - ${response.message()}"
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
