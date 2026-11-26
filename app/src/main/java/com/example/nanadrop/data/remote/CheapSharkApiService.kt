package com.example.nanadrop.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface CheapSharkApiService {
    @Headers("User-Agent: NanaDrop/1.0 (contact@nanadrop.com)")
    @GET("deals")
    suspend fun getDeals(
        @Query("pageSize") pageSize: Int = 20
    ): List<CheapSharkDeal>
}
