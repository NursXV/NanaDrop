package com.example.nanadrop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nanadrop.data.local.WalletEntity
import com.example.nanadrop.data.local.WalletTransactionEntity
import com.example.nanadrop.data.repository.WalletRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Thin presentation layer over [WalletRepository]. Every balance mutation is delegated to the
 * repository so the wallet row and the transaction ledger always stay in sync.
 */
class WalletViewModel(private val walletRepository: WalletRepository) : ViewModel() {

    /** The single Room wallet row (null while the first emission is on its way). */
    val wallet: StateFlow<WalletEntity?> = walletRepository.getWallet()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val transactions: StateFlow<List<WalletTransactionEntity>> = walletRepository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        // Make sure the singleton wallet row exists so pending escrows can always be settled.
        viewModelScope.launch { walletRepository.wallet() }
    }

    suspend fun deposit(amount: Double): Boolean = walletRepository.deposit(amount)

    suspend fun withdraw(amount: Double): Boolean = walletRepository.withdraw(amount)

    suspend fun freezeFunds(amount: Double, orderTitle: String): Boolean =
        walletRepository.freezeEscrow(amount, "Frozen for: $orderTitle")

    suspend fun releaseFrozenToAvailable(amount: Double, orderTitle: String): Boolean =
        walletRepository.releaseFrozenToAvailable(amount, "Released from: $orderTitle")

    class Factory(private val walletRepository: WalletRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
                return WalletViewModel(walletRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
