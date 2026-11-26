package com.example.nanadrop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 0,
    val availableBalance: Double = 100.0,
    val frozenBalance: Double = 0.0,
    val totalEarned: Double = 0.0
)
