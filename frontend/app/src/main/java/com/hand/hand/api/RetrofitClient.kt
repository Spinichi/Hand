// RetrofitClient.kt

package com.hand.hand.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://gatewaytohand.store/api/"
    private var retrofitClient: Retrofit? = null

    // 더 이상 RetrofitClient가 토큰을 직접 관리하지 않습니다.
    // fun setAccessToken(token: String?) { ... }
    // fun isLoggedIn(): Boolean { ... }

    fun getClient(): Retrofit {
        if (retrofitClient == null) {
            // HTTP 로그 인터셉터
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder()
                        .addHeader("Accept", "application/json")

                    // ★ TokenManager에서 직접 토큰을 불러와 헤더에 추가
                    TokenManager.loadToken()?.let {
                        builder.addHeader("Authorization", "Bearer $it")
                        android.util.Log.d("RetrofitClient", "Attach Authorization: Bearer ${it.take(12)}...")
                    }

                    val req = builder.build()
                    android.util.Log.d("RetrofitClient", "→ ${req.method} ${req.url}")

                    chain.proceed(req)
                }
                .addInterceptor(interceptor)
                .build()

            retrofitClient = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofitClient!!
    }
}
