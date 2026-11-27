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
    val status: String = STATUS_ESCROW,
    val createdAt: Long = System.currentTimeMillis(),
    val deliveryInfo: String = ""
) {
    companion object {
        /** Funds are frozen in the buyer's wallet and held by NanaDrop. */
        const val STATUS_ESCROW = "ESCROW"

        /** Seller marked the item as delivered, funds are still frozen. */
        const val STATUS_DELIVERED = "DELIVERED"

        /** Buyer confirmed receipt, escrow funds were released. */
        const val STATUS_COMPLETED = "COMPLETED"

        /** Buyer opened a dispute, funds stay frozen until resolved. */
        const val STATUS_DISPUTED = "DISPUTED"

        /** Order was cancelled, frozen funds were refunded to the buyer. */
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
