package com.example.nanadrop.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE buyerName = :buyerName ORDER BY createdAt DESC")
    fun getPurchasesForUser(buyerName: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE sellerName = :sellerName ORDER BY createdAt DESC")
    fun getSalesForUser(sellerName: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>): List<Long>

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Long): Flow<OrderEntity?>
}
