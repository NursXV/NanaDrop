package com.example.nanadrop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val price: Double,
    val sellerName: String,
    val description: String,
    val platform: String,
    val isUserListing: Boolean = true,
    val isSold: Boolean = false
)
