// GmsRetrofitClient.kt

package com.hand.hand.api.GMS


import com.hand.hand.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GmsRetrofitClient {

    // BuildConfig에서 읽어옴 (환경변수 또는 기본값)
    private val BASE_URL = BuildConfig.GMS_BASE_URL
    private val GMS_API_KEY = BuildConfig.GMS_API_KEY

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${GMS_API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
