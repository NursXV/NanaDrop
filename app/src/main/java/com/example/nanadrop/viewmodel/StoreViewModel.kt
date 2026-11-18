package com.example.nanadrop.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nanadrop.data.DigitalProduct
import com.example.nanadrop.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoreViewModel : ViewModel() {

    // Барлық тауарлар тізімі
    private val _products = MutableStateFlow<List<DigitalProduct>>(ProductRepository.products)
    val products: StateFlow<List<DigitalProduct>> = _products.asStateFlow()

    // Іздеу жолағының мәтіні
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Таңдалған категория (Filters: All, Keys, Accounts, Subscriptions)
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Себетке қосылған тауарлар тізімі
    private val _cartItems = MutableStateFlow<List<DigitalProduct>>(emptyList())
    val cartItems: StateFlow<List<DigitalProduct>> = _cartItems.asStateFlow()

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
}