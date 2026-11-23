package com.example.nanadrop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nanadrop.data.remote.CheapSharkDeal
import com.example.nanadrop.data.repository.RemoteDealsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DealsUiState {
    object Loading : DealsUiState
    data class Success(val deals: List<CheapSharkDeal>) : DealsUiState
    object Empty : DealsUiState
    data class Error(val message: String) : DealsUiState
}

class DealsViewModel(
    private val repository: RemoteDealsRepository = RemoteDealsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DealsUiState>(DealsUiState.Loading)
    val uiState: StateFlow<DealsUiState> = _uiState.asStateFlow()

    init {
        fetchDeals()
    }

    fun fetchDeals() {
        viewModelScope.launch {
            _uiState.value = DealsUiState.Loading
            repository.getDeals(pageSize = 20)
                .onSuccess { deals ->
                    if (deals.isEmpty()) {
                        _uiState.value = DealsUiState.Empty
                    } else {
                        _uiState.value = DealsUiState.Success(deals)
                    }
                }
                .onFailure { exception ->
                    val errorMessage = exception.localizedMessage ?: "Failed to connect to server. Check your internet connection."
                    _uiState.value = DealsUiState.Error(errorMessage)
                }
        }
    }

    fun retry() {
        fetchDeals()
    }
}
