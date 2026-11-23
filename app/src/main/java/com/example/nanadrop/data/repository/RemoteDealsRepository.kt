package com.example.nanadrop.data.repository

import com.example.nanadrop.data.remote.CheapSharkApiService
import com.example.nanadrop.data.remote.CheapSharkDeal
import com.example.nanadrop.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteDealsRepository(
    private val apiService: CheapSharkApiService = RetrofitClient.cheapSharkApiService
) {
    suspend fun getDeals(pageSize: Int = 20): Result<List<CheapSharkDeal>> = withContext(Dispatchers.IO) {
        try {
            val deals = apiService.getDeals(pageSize)
            Result.success(deals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
