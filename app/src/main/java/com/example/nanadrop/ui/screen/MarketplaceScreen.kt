package com.example.nanadrop.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nanadrop.data.local.ListingEntity
import com.example.nanadrop.ui.components.CategoryChip
import com.example.nanadrop.ui.components.ListingDialog
import com.example.nanadrop.ui.viewmodel.MarketplaceViewModel
import com.example.nanadrop.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: MarketplaceViewModel,
    storeViewModel: StoreViewModel,
    onNavigateToCart: () -> Unit,
    onListingClick: (Long) -> Unit = {}
) {
    val listings by viewModel.filteredListings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val hideSoldListings by viewModel.hideSoldListings.collectAsState()
    val isDialogOpen by viewModel.isDialogOpen.collectAsState()
    val editingListing by viewModel.editingListing.collectAsState()
    val cartItems by storeViewModel.cartItems.collectAsState()
    val totalCartCount by storeViewModel.totalCartItemsCount.collectAsState()

    val categories = listOf("All", "Keys", "Accounts", "Subscriptions", "Hardware", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marketplace") },
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Shopping Cart ($totalCartCount items)",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            if (totalCartCount > 0) {
                                Text(
                                    text = totalCartCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreateDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Listing") },
                text = { Text("Add Listing") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search marketplace listings...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category & Preferences Filter Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = hideSoldListings,
                            onClick = { viewModel.toggleHideSoldListings(!hideSoldListings) },
                            label = { Text("Hide Sold") },
                            leadingIcon = {
                                if (hideSoldListings) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    items(categories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = category == selectedCategory,
                            onSelect = { viewModel.setSelectedCategory(category) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (listings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No listings found.\nTap '+' to create your first listing!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(listings, key = { it.id }) { listing ->
                            ListingCard(
                                listing = listing,
                                onClick = { onListingClick(listing.id) },
                                onEditClick = { viewModel.openEditDialog(listing) },
                                onDeleteClick = { viewModel.deleteListing(listing) },
                                onToggleSoldClick = { viewModel.toggleSoldStatus(listing) },
                                onBuyClick = { storeViewModel.addListingToCart(listing) },
                                isInCart = cartItems.any { it.id == "lot_${listing.id}" },
                                isSold = listing.isSold
                            )
                        }
                    }
                }
            }

            // Dialog for Create / Edit Listing
            if (isDialogOpen) {
                ListingDialog(
                    editingListing = editingListing,
                    onDismiss = { viewModel.closeDialog() },
                    onSave = { id, title, category, price, sellerName, description, platform, isUserListing, isSold ->
                        viewModel.saveListing(
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
                    }
                )
            }
        }
    )
}

@Composable
fun ListingCard(
    listing: ListingEntity,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleSoldClick: () -> Unit,
    onBuyClick: () -> Unit,
    isInCart: Boolean,
    isSold: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSold) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = listing.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (isSold) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "SOLD",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = listing.platform,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = listing.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Seller: ${listing.sellerName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format(java.util.Locale.US, "%.2f", listing.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSold) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Buy / Added Button
                    val buyButtonEnabled = !isSold && !isInCart
                    val buyButtonText = if (isInCart) "Added" else "Buy"
                    val buyButtonColors = ButtonDefaults.buttonColors(
                        containerColor = if (isInCart) MaterialTheme.colorScheme.secondaryContainer
                        else if (!buyButtonEnabled) MaterialTheme.colorScheme.surfaceContainerHighest
                        else MaterialTheme.colorScheme.primary,
                        contentColor = if (isInCart) MaterialTheme.colorScheme.onSecondaryContainer
                        else if (!buyButtonEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onPrimary
                    )

                    Button(
                        onClick = onBuyClick,
                        enabled = buyButtonEnabled,
                        colors = buyButtonColors,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = buyButtonText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Toggle Sold Status
                    IconButton(
                        onClick = onToggleSoldClick,
                        enabled = true
                    ) {
                        Icon(
                            imageVector = if (isSold) Icons.Default.Refresh else Icons.Default.CheckCircle,
                            contentDescription = if (isSold) "Mark Available" else "Mark Sold",
                            tint = if (isSold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Edit Button
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Listing",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Delete Button
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Listing",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
