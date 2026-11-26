package com.example.nanadrop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nanadrop.data.DigitalProduct
import com.example.nanadrop.data.ProductRepository
import com.example.nanadrop.data.local.ListingEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StoreViewModel : ViewModel() {

    // товарлардын тызымыго крч
    private val _products = MutableStateFlow<List<DigitalProduct>>(ProductRepository.products)
    val products: StateFlow<List<DigitalProduct>> = _products.asStateFlow()

    // ыздеу жол текст
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // котегориялар (Filters: All, Keys, Accounts, Subscriptions)
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // лист что добавлен
    private val _cartItems = MutableStateFlow<List<DigitalProduct>>(emptyList())
    val cartItems: StateFlow<List<DigitalProduct>> = _cartItems.asStateFlow()

    // барын санау
    val totalCartItemsCount: StateFlow<Int> = _cartItems
        .map { items -> items.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterProducts(query, _selectedCategory.value)
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        filterProducts(_searchQuery.value, category)
    }

    private fun filterProducts(query: String, category: String) {
        val allProducts = ProductRepository.products
        val filtered = allProducts.filter { product ->
            val matchesQuery = product.title.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
        _products.value = filtered
    }

    fun addToCart(product: DigitalProduct) {
        if (!_cartItems.value.contains(product)) {
            _cartItems.value = _cartItems.value + product
        }
    }

    fun removeFromCart(product: DigitalProduct) {
        _cartItems.value = _cartItems.value - product
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun addListingToCart(listing: ListingEntity) {
        val digitalProduct = DigitalProduct(
            id = "lot_${listing.id}",
            title = listing.title,
            category = listing.category,
            price = listing.price,
            description = listing.description,
            platform = listing.platform
        )
        addToCart(digitalProduct)
    }

}