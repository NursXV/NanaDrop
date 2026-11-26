package com.example.nanadrop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listingId: Long,
    val title: String,
    val category: String,
    val platform: String,
    val price: Double,
    val buyerName: String,
    val sellerName: String,
    val status: String = "ESCROW", // "ESCROW", "DELIVERED", "COMPLETED", "DISPUTED"
    val createdAt: Long = System.currentTimeMillis(),
    val deliveryInfo: String = ""
)
