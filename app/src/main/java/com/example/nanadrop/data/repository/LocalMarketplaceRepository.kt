package com.example.nanadrop.data.repository

import com.example.nanadrop.data.local.ListingDao
import com.example.nanadrop.data.local.ListingEntity
import com.example.nanadrop.data.local.OrderDao
import com.example.nanadrop.data.local.OrderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LocalMarketplaceRepository(
    private val listingDao: ListingDao,
    private val orderDao: OrderDao
) {

    val allListings: Flow<List<ListingEntity>> = listingDao.getAllListings()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

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

    // Order operations
    fun getPurchasesForUser(buyerName: String): Flow<List<OrderEntity>> {
        return orderDao.getPurchasesForUser(buyerName)
    }

    fun getSalesForUser(sellerName: String): Flow<List<OrderEntity>> {
        return orderDao.getSalesForUser(sellerName)
    }

    suspend fun insertOrder(order: OrderEntity): Long = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
    }

    suspend fun insertOrders(orders: List<OrderEntity>): List<Long> = withContext(Dispatchers.IO) {
        orderDao.insertOrders(orders)
    }

    suspend fun updateOrder(order: OrderEntity): Unit = withContext(Dispatchers.IO) {
        orderDao.updateOrder(order)
    }

    suspend fun updateOrderStatus(orderId: Long, status: String): Unit = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, status)
    }
}
