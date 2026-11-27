package com.example.nanadrop.data.repository

import com.example.nanadrop.data.local.WalletDao
import com.example.nanadrop.data.local.WalletEntity
import com.example.nanadrop.data.local.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the user's money.
 *
 * Every balance movement goes through this repository so that the wallet row and the
 * transaction ledger can never drift apart. All amounts are absolute (always positive).
 */
class WalletRepository(private val walletDao: WalletDao) {

    fun getWallet(): Flow<WalletEntity?> = walletDao.getWallet()
    suspend fun getWalletOnce(): WalletEntity? = walletDao.getWalletOnce()

    suspend fun insertWallet(wallet: WalletEntity) = walletDao.insertWallet(wallet)
    suspend fun updateWallet(wallet: WalletEntity) = walletDao.updateWallet(wallet)

    fun getAllTransactions(): Flow<List<WalletTransactionEntity>> = walletDao.getAllTransactions()
    fun getRecentTransactions(limit: Int): Flow<List<WalletTransactionEntity>> =
        walletDao.getRecentTransactions(limit)

    suspend fun insertTransaction(transaction: WalletTransactionEntity) =
        walletDao.insertTransaction(transaction)

    suspend fun insertTransactions(transactions: List<WalletTransactionEntity>) =
        walletDao.insertTransactions(transactions)

    /** Returns the singleton wallet row, seeding a default one when the table is still empty. */
    suspend fun wallet(): WalletEntity {
        val existing = walletDao.getWalletOnce()
        if (existing != null) return existing
        val seeded = WalletEntity(id = 0)
        walletDao.insertWallet(seeded)
        return seeded
    }

    /** Adds funds to the available balance. */
    suspend fun deposit(amount: Double, title: String = "Wallet top-up"): Boolean {
        if (amount <= 0.0) return false
        val current = wallet()
        walletDao.updateWallet(
            current.copy(availableBalance = current.availableBalance + amount)
        )
        record(WalletTransactionEntity(type = TYPE_DEPOSIT, title = title, amount = amount))
        return true
    }

    /** Takes funds out of the available balance. Fails when the balance is too low. */
    suspend fun withdraw(amount: Double, title: String = "Withdrawal"): Boolean {
        if (amount <= 0.0) return false
        val current = wallet()
        if (current.availableBalance < amount) return false
        walletDao.updateWallet(
            current.copy(availableBalance = current.availableBalance - amount)
        )
        record(WalletTransactionEntity(type = TYPE_WITHDRAW, title = title, amount = amount))
        return true
    }

    /**
     * Escrow hold: moves [amount] from available to frozen.
     * Returns false when the available balance cannot cover the hold.
     */
    suspend fun freezeEscrow(amount: Double, title: String): Boolean {
        if (amount <= 0.0) return false
        val current = wallet()
        if (current.availableBalance < amount) return false
        walletDao.updateWallet(
            current.copy(
                availableBalance = current.availableBalance - amount,
                frozenBalance = current.frozenBalance + amount
            )
        )
        record(WalletTransactionEntity(type = TYPE_FREEZE, title = title, amount = amount))
        return true
    }

    /** Escrow released to an external seller: the frozen hold simply leaves the wallet. */
    suspend fun releaseEscrow(amount: Double, title: String): Boolean {
        if (amount <= 0.0) return false
        val current = wallet()
        walletDao.updateWallet(
            current.copy(frozenBalance = (current.frozenBalance - amount).coerceAtLeast(0.0))
        )
        record(WalletTransactionEntity(type = TYPE_RELEASE, title = title, amount = amount))
        return true
    }

    /** Escrow cancelled: the frozen hold goes back to the buyer's available balance. */
    suspend fun refundEscrow(amount: Double, title: String): Boolean {
        if (amount <= 0.0) return false
        val current = wallet()
        walletDao.updateWallet(
            current.copy(
                frozenBalance = (current.frozenBalance - amount).coerceAtLeast(0.0),
                availableBalance = current.availableBalance + amount
            )
        )
        record(WalletTransactionEntity(type = TYPE_REFUND, title = title, amount = amount))
        return true
    }

    /** Escrow released to the current user as a seller: lands in available funds and earnings. */
    suspend fun creditEscrowEarning(amount: Double, title: String): Boolean {
        if (amount <= 0.0) return false
        val current = wallet()
        walletDao.updateWallet(
            current.copy(
                availableBalance = current.availableBalance + amount,
                totalEarned = current.totalEarned + amount
            )
        )
        record(WalletTransactionEntity(type = TYPE_RELEASE, title = title, amount = amount))
        return true
    }

    /** Manual wallet action: moves frozen funds back to available. */
    suspend fun releaseFrozenToAvailable(amount: Double, title: String): Boolean {
        if (amount <= 0.0) return false
        val current = wallet()
        if (current.frozenBalance < amount) return false
        walletDao.updateWallet(
            current.copy(
                frozenBalance = current.frozenBalance - amount,
                availableBalance = current.availableBalance + amount
            )
        )
        record(WalletTransactionEntity(type = TYPE_RELEASE, title = title, amount = amount))
        return true
    }

    private suspend fun record(transaction: WalletTransactionEntity) {
        walletDao.insertTransaction(transaction)
    }

    companion object {
        const val TYPE_DEPOSIT = "DEPOSIT"
        const val TYPE_WITHDRAW = "WITHDRAW"
        const val TYPE_FREEZE = "FREEZE"
        const val TYPE_RELEASE = "RELEASE"
        const val TYPE_REFUND = "REFUND"
    }
}
