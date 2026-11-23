package com.example.nanadrop.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface CheapSharkApiService {
    @GET("deals")
    suspend fun getDeals(
        @Query("pageSize") pageSize: Int = 20
    ): List<CheapSharkDeal>
}
