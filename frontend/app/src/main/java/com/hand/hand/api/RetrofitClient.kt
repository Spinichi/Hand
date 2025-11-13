package com.hand.hand.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://gatewaytohand.store/api/"
    private var retrofitClient: Retrofit? = null

    @Volatile
    private var accessToken: String? = null

    // 로그인 성공 시 토큰 저장용
    fun setAccessToken(token: String?) {
        accessToken = token
    }

    fun isLoggedIn(): Boolean {
        return !accessToken.isNullOrBlank()
    }

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

                    // ★ 토큰이 있을 때 Authorization 헤더 추가
                    accessToken?.takeIf { it.isNotBlank() }?.let {
                        builder.addHeader("Authorization", "Bearer $it")
                        android.util.Log.d("RetrofitClient", "Attach Authorization: Bearer ${it.take(12)}...")
                    }

                    val req = builder.build()
                    android.util.Log.d("RetrofitClient", "→ ${req.method} ${req.url}")

                    // ★ proceed는 한 번만 호출해야 함
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
