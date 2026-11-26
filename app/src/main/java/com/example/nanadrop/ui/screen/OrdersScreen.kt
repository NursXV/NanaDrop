package com.example.nanadrop.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nanadrop.data.local.OrderEntity
import com.example.nanadrop.ui.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: MarketplaceViewModel,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Purchases, 1 = Sales
    val purchases by viewModel.purchases.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()

    var deliveryCodeInput by remember { mutableStateOf("") }
    var selectedOrderForDelivery by remember { mutableStateOf<OrderEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Escrow Orders")
                        Text(
                            "Wallet: $${String.format(Locale.US, "%.2f", walletBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Purchases (${purchases.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Sales (${sales.size})") }
                )
            }

            val ordersList = if (selectedTab == 0) purchases else sales

            if (ordersList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) "No purchases yet." else "No sales yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ordersList, key = { it.id }) { order ->
                        OrderItemCard(
                            order = order,
                            isPurchase = selectedTab == 0,
                            onDeliverClick = { selectedOrderForDelivery = order },
                            onConfirmReceipt = {
                                viewModel.updateOrderStatus(order, "COMPLETED")
                            },
                            onDispute = {
                                viewModel.updateOrderStatus(order, "DISPUTED")
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedOrderForDelivery != null) {
        val targetOrder = selectedOrderForDelivery!!
        AlertDialog(
            onDismissRequest = { selectedOrderForDelivery = null },
            title = { Text("Deliver Order Item / Key") },
            text = {
                Column {
                    Text("Enter digital code or access instructions for item:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deliveryCodeInput,
                        onValueChange = { deliveryCodeInput = it },
                        label = { Text("Key / Serial Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deliveryCodeInput.isNotBlank()) {
                            viewModel.updateOrderStatus(targetOrder, "DELIVERED", deliveryCodeInput)
                            deliveryCodeInput = ""
                            selectedOrderForDelivery = null
                        }
                    }
                ) {
                    Text("Send Delivery")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForDelivery = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OrderItemCard(
    order: OrderEntity,
    isPurchase: Boolean,
    onDeliverClick: () -> Unit,
    onConfirmReceipt: () -> Unit,
    onDispute: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val dateStr = remember(order.createdAt) { dateFormat.format(Date(order.createdAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderStatusBadge(status = order.status)
                Text(
                    text = "$${String.format(Locale.US, "%.2f", order.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Platform: ${order.platform} | Category: ${order.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isPurchase) "Seller: ${order.sellerName}" else "Buyer: ${order.buyerName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (order.deliveryInfo.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Delivery Info:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = order.deliveryInfo,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isPurchase && order.status == "ESCROW") {
                    Button(
                        onClick = onDeliverClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Delivered")
                    }
                }

                if (isPurchase && (order.status == "ESCROW" || order.status == "DELIVERED")) {
                    OutlinedButton(
                        onClick = onDispute,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dispute")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirmReceipt,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm Receipt")
                    }
                }

                if (order.status == "COMPLETED") {
                    Text(
                        text = "Funds Released & Transaction Completed",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (order.status == "DISPUTED") {
                    Text(
                        text = "Under Dispute Review",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    val (backgroundColor, textColor, icon, label) = when (status) {
        "ESCROW" -> Quadruple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Default.Lock,
            "ESCROW HELD"
        )
        "DELIVERED" -> Quadruple(
            Color(0xFFE3F2FD),
            Color(0xFF1565C0),
            Icons.Default.LocalShipping,
            "DELIVERED"
        )
        "COMPLETED" -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Default.Check,
            "COMPLETED"
        )
        "DISPUTED" -> Quadruple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Icons.Default.ReportProblem,
            "DISPUTED"
        )
        else -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Lock,
            status
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
