// GmsRetrofitClient.kt

package com.hand.hand.api.GMS


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GmsRetrofitClient {

    private const val BASE_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/"

    private const val GMS_API_KEY = "S13P32A106-1bd9c436-0876-44c9-bdf2-bb77386d352c"

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
