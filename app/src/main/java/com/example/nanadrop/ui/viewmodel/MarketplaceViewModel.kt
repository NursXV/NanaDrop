package com.example.nanadrop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nanadrop.data.DigitalProduct
import com.example.nanadrop.data.local.ListingEntity
import com.example.nanadrop.data.local.MarketplaceDatabase
import com.example.nanadrop.data.local.OrderEntity
import com.example.nanadrop.data.local.UserPreferencesRepository
import com.example.nanadrop.data.local.UserProfile
import com.example.nanadrop.data.local.WalletEntity
import com.example.nanadrop.data.repository.LocalMarketplaceRepository
import com.example.nanadrop.data.repository.WalletRepository
import com.example.nanadrop.ui.util.asCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val walletRepository: WalletRepository
    private val userPreferencesRepository = UserPreferencesRepository(application)

    val currentUserName = "You (CurrentUser)"

    val allListings: StateFlow<List<ListingEntity>>
    val allOrders: StateFlow<List<OrderEntity>>

    val purchases: StateFlow<List<OrderEntity>>
    val sales: StateFlow<List<OrderEntity>>

    // DataStore Preference Flows
    val hideSoldListings: StateFlow<Boolean> = userPreferencesRepository.hideSoldListings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isDarkMode: StateFlow<Boolean> = userPreferencesRepository.isDarkMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val userProfile: StateFlow<UserProfile> = userPreferencesRepository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    /**
     * The one and only wallet. Backed by Room so the marketplace, cart, orders and wallet
     * screens all read the exact same balances.
     */
    val wallet: StateFlow<WalletEntity?>

    /** Convenience view of [wallet] for price checks (null wallet is treated as 0.0). */
    val availableBalance: StateFlow<Double>

    // Dialog & Editing State
    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    private val _editingListing = MutableStateFlow<ListingEntity?>(null)
    val editingListing: StateFlow<ListingEntity?> = _editingListing.asStateFlow()

    // Filter, Search, Sort & User Listing state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow("Newest")
    val sortOption: StateFlow<String> = _sortOption.asStateFlow()

    private val _onlyUserListings = MutableStateFlow(false)
    val onlyUserListings: StateFlow<Boolean> = _onlyUserListings.asStateFlow()

    val filteredListings: StateFlow<List<ListingEntity>>

    init {
        val db = MarketplaceDatabase.getDatabase(application)
        repository = LocalMarketplaceRepository(db.listingDao(), db.orderDao())
        walletRepository = WalletRepository(db.walletDao())
        viewModelScope.launch { walletRepository.wallet() }

        wallet = walletRepository.getWallet().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        availableBalance = wallet
            .map { it?.availableBalance ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

        allListings = repository.allListings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allOrders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        purchases = repository.getPurchasesForUser(currentUserName).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        sales = repository.getSalesForUser(currentUserName).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredListings = combine(
            allListings,
            _searchQuery,
            _selectedCategory,
            hideSoldListings,
            _onlyUserListings,
            _sortOption
        ) { flowArray ->
            @Suppress("UNCHECKED_CAST")
            val listings = flowArray[0] as List<ListingEntity>
            val query = flowArray[1] as String
            val category = flowArray[2] as String
            val hideSold = flowArray[3] as Boolean
            val userOnly = flowArray[4] as Boolean
            val sort = flowArray[5] as String

            val filtered = listings.filter { listing ->
                val matchesQuery = listing.title.contains(query, ignoreCase = true) ||
                        listing.description.contains(query, ignoreCase = true) ||
                        listing.sellerName.contains(query, ignoreCase = true)
                val matchesCategory = category == "All" || listing.category.equals(category, ignoreCase = true)
                val matchesSoldFilter = !hideSold || !listing.isSold
                val matchesUserOnly = !userOnly || listing.isUserListing
                matchesQuery && matchesCategory && matchesSoldFilter && matchesUserOnly
            }

            when (sort) {
                "Price: Low to High" -> filtered.sortedBy { it.price }
                "Price: High to Low" -> filtered.sortedByDescending { it.price }
                else -> filtered.sortedByDescending { it.id } // "Newest"
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

    fun setSortOption(option: String) {
        _sortOption.value = option
    }

    fun toggleOnlyUserListings(enabled: Boolean) {
        _onlyUserListings.value = enabled
    }

    fun toggleHideSoldListings(hide: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.setHideSoldListings(hide)
        }
    }

    fun toggleDarkMode(darkMode: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.setDarkMode(darkMode)
        }
    }

    fun updateDisplayName(displayName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.updateDisplayName(displayName)
        }
    }

    fun updateBio(bio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.updateBio(bio)
        }
    }

    fun updateProfile(displayName: String, bio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.updateProfile(displayName, bio)
        }
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

    // Checkout & Escrow flows

    /**
     * Moves the cart total from the available balance into the frozen escrow balance (Room),
     * records a FREEZE transaction, creates the orders and marks listings as sold.
     *
     * [onComplete] is always invoked on the main thread.
     */
    fun checkoutCartItems(cartItems: List<DigitalProduct>, onComplete: (Boolean, String) -> Unit) {
        if (cartItems.isEmpty()) {
            onComplete(false, "Your cart is empty")
            return
        }

        val totalAmount = cartItems.sumOf { it.price }

        viewModelScope.launch(Dispatchers.IO) {
            val frozen = walletRepository.freezeEscrow(
                amount = totalAmount,
                title = "Escrow hold · ${cartItems.size} item(s) · ${totalAmount.asCurrency()}"
            )

            if (!frozen) {
                val available = walletRepository.wallet().availableBalance
                val message = "Insufficient balance: ${totalAmount.asCurrency()} needed, " +
                    "${available.asCurrency()} available."
                withContext(Dispatchers.Main) { onComplete(false, message) }
                return@launch
            }

            val newOrders = cartItems.map { item ->
                val listingId = if (item.id.startsWith("lot_")) {
                    item.id.removePrefix("lot_").toLongOrNull() ?: 0L
                } else 0L

                val seller = if (listingId > 0L) {
                    allListings.value.find { it.id == listingId }?.sellerName ?: "MarketplaceSeller"
                } else {
                    "NanaDrop Direct"
                }

                OrderEntity(
                    listingId = listingId,
                    title = item.title,
                    category = item.category,
                    platform = item.platform,
                    price = item.price,
                    buyerName = currentUserName,
                    sellerName = seller,
                    status = OrderEntity.STATUS_ESCROW,
                    createdAt = System.currentTimeMillis(),
                    deliveryInfo = "Digital Key / Item access code pending seller delivery."
                )
            }

            repository.insertOrders(newOrders)

            // Mark marketplace listings as sold so they leave the storefront.
            cartItems.forEach { item ->
                if (item.id.startsWith("lot_")) {
                    val listingId = item.id.removePrefix("lot_").toLongOrNull()
                    val listing = listingId?.let { id -> allListings.value.find { it.id == id } }
                    if (listing != null) {
                        repository.updateListing(listing.copy(isSold = true))
                    }
                }
            }

            val message = "${totalAmount.asCurrency()} held in escrow for " +
                "${newOrders.size} order(s)."
            withContext(Dispatchers.Main) { onComplete(true, message) }
        }
    }

    /**
     * Applies an escrow status change and settles the money in the Room wallet.
     *
     * - [OrderEntity.STATUS_COMPLETED]: the escrow is released; it is credited to the
     *   available balance + total earned when the current user is the seller.
     * - [OrderEntity.STATUS_CANCELLED]: the frozen hold is refunded to the buyer.
     */
    fun updateOrderStatus(order: OrderEntity, newStatus: String, deliveryCode: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedDelivery = if (deliveryCode.isNotBlank()) deliveryCode else order.deliveryInfo

            when (newStatus) {
                OrderEntity.STATUS_COMPLETED -> if (order.sellerName == currentUserName) {
                    walletRepository.creditEscrowEarning(
                        amount = order.price,
                        title = "Escrow payout · ${order.title}"
                    )
                } else {
                    walletRepository.releaseEscrow(
                        amount = order.price,
                        title = "Escrow released · ${order.title}"
                    )
                }

                OrderEntity.STATUS_CANCELLED -> if (order.buyerName == currentUserName) {
                    walletRepository.refundEscrow(
                        amount = order.price,
                        title = "Escrow refund · ${order.title}"
                    )
                }
            }

            repository.updateOrder(order.copy(status = newStatus, deliveryInfo = updatedDelivery))
        }
    }
}
