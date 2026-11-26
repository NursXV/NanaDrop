package com.example.nanadrop.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://www.cheapshark.com/api/1.0/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "NanaDrop/1.0 (contact@nanadrop.com)")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val cheapSharkApiService: CheapSharkApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CheapSharkApiService::class.java)
    }
}
