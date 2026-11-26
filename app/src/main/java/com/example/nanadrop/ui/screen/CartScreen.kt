package com.example.nanadrop.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nanadrop.ui.viewmodel.MarketplaceViewModel
import com.example.nanadrop.ui.viewmodel.StoreViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: StoreViewModel,
    marketplaceViewModel: MarketplaceViewModel,
    onBackClick: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsState()
    val walletBalance by marketplaceViewModel.walletBalance.collectAsState()
    val totalPrice = cartItems.sumOf { it.price }

    var isCheckingOut by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart (${cartItems.size})") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToOrders) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "My Escrow Orders")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your cart is empty", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onNavigateToOrders) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View My Orders")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cartItems) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", product.price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { viewModel.removeFromCart(product) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Wallet Balance:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", walletBalance)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (walletBalance >= totalPrice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        enabled = !isCheckingOut,
                        onClick = {
                            isCheckingOut = true
                            marketplaceViewModel.checkoutCartItems(cartItems) { success, message ->
                                isCheckingOut = false
                                if (success) {
                                    viewModel.clearCart()
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    onNavigateToOrders()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isCheckingOut) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Checkout to Escrow ($${String.format(Locale.US, "%.2f", totalPrice)})", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
