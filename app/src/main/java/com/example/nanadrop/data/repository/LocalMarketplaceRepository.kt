package com.example.nanadrop.data.repository

import com.example.nanadrop.data.local.ListingDao
import com.example.nanadrop.data.local.ListingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LocalMarketplaceRepository(private val listingDao: ListingDao) {

    val allListings: Flow<List<ListingEntity>> = listingDao.getAllListings()

    fun getListingById(id: Long): Flow<ListingEntity?> {
        return listingDao.getListingById(id)
    }

    suspend fun insertListing(listing: ListingEntity): Long = withContext(Dispatchers.IO) {
        listingDao.insertListing(listing)
    }

    suspend fun updateListing(listing: ListingEntity): Unit = withContext(Dispatchers.IO) {
        listingDao.updateListing(listing)
    }

    suspend fun deleteListing(listing: ListingEntity): Unit = withContext(Dispatchers.IO) {
        listingDao.deleteListing(listing)
    }

    suspend fun deleteListingById(id: Long): Unit = withContext(Dispatchers.IO) {
        listingDao.deleteListingById(id)
    }
}
