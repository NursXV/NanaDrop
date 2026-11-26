package com.example.nanadrop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "DEPOSIT" | "WITHDRAW" | "FREEZE" | "RELEASE" | "REFUND"
    val title: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
