package com.example.nanadrop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nanadrop.data.local.ListingEntity
import com.example.nanadrop.data.local.MarketplaceDatabase
import com.example.nanadrop.data.repository.LocalMarketplaceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MarketplaceViewModel::class.java)) {
                return MarketplaceViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val repository: LocalMarketplaceRepository
    val allListings: StateFlow<List<ListingEntity>>

    // Dialog & Editing State
    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    private val _editingListing = MutableStateFlow<ListingEntity?>(null)
    val editingListing: StateFlow<ListingEntity?> = _editingListing.asStateFlow()

    // Filter & Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredListings: StateFlow<List<ListingEntity>>

    init {
        val dao = MarketplaceDatabase.getDatabase(application).listingDao()
        repository = LocalMarketplaceRepository(dao)

        allListings = repository.allListings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredListings = combine(allListings, _searchQuery, _selectedCategory) { listings, query, category ->
            listings.filter { listing ->
                val matchesQuery = listing.title.contains(query, ignoreCase = true) ||
                        listing.description.contains(query, ignoreCase = true) ||
                        listing.sellerName.contains(query, ignoreCase = true)
                val matchesCategory = category == "All" || listing.category.equals(category, ignoreCase = true)
                matchesQuery && matchesCategory
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openCreateDialog() {
        _editingListing.value = null
        _isDialogOpen.value = true
    }

    fun openEditDialog(listing: ListingEntity) {
        _editingListing.value = listing
        _isDialogOpen.value = true
    }

    fun closeDialog() {
        _isDialogOpen.value = false
        _editingListing.value = null
    }

    fun saveListing(
        id: Long = 0,
        title: String,
        category: String,
        price: Double,
        sellerName: String,
        description: String,
        platform: String,
        isUserListing: Boolean = true,
        isSold: Boolean = false
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val newListing = ListingEntity(
                    title = title,
                    category = category,
                    price = price,
                    sellerName = sellerName,
                    description = description,
                    platform = platform,
                    isUserListing = isUserListing,
                    isSold = isSold
                )
                repository.insertListing(newListing)
            } else {
                val updatedListing = ListingEntity(
                    id = id,
                    title = title,
                    category = category,
                    price = price,
                    sellerName = sellerName,
                    description = description,
                    platform = platform,
                    isUserListing = isUserListing,
                    isSold = isSold
                )
                repository.updateListing(updatedListing)
            }
            closeDialog()
        }
    }

    fun toggleSoldStatus(listing: ListingEntity) {
        viewModelScope.launch {
            val updated = listing.copy(isSold = !listing.isSold)
            repository.updateListing(updated)
        }
    }

    fun deleteListing(listing: ListingEntity) {
        viewModelScope.launch {
            repository.deleteListing(listing)
        }
    }
}
