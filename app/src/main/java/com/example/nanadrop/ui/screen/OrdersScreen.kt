package com.example.nanadrop.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
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
import com.example.nanadrop.data.local.WalletEntity
import com.example.nanadrop.ui.util.asCurrency
import com.example.nanadrop.ui.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: MarketplaceViewModel,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Purchases, 1 = Sales
    val purchases by viewModel.purchases.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val wallet by viewModel.wallet.collectAsState()

    var deliveryCodeInput by remember { mutableStateOf("") }
    var selectedOrderForDelivery by remember { mutableStateOf<OrderEntity?>(null) }
    var selectedOrderForCancel by remember { mutableStateOf<OrderEntity?>(null) }

    // The app level TopAppBar shows the title, this screen owns only its content.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    ) {
        WalletStrip(
            wallet = wallet,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryTabRow(selectedTabIndex = selectedTab) {
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
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(ordersList, key = { it.id }) { order ->
                    OrderItemCard(
                        order = order,
                        isPurchase = selectedTab == 0,
                        onDeliverClick = { selectedOrderForDelivery = order },
                        onConfirmReceipt = {
                            viewModel.updateOrderStatus(order, OrderEntity.STATUS_COMPLETED)
                        },
                        onCancelOrder = { selectedOrderForCancel = order },
                        onDispute = {
                            viewModel.updateOrderStatus(order, OrderEntity.STATUS_DISPUTED)
                        }
                    )
                }
            }
        }
    }

    selectedOrderForDelivery?.let { targetOrder ->
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
                            viewModel.updateOrderStatus(
                                targetOrder,
                                OrderEntity.STATUS_DELIVERED,
                                deliveryCodeInput
                            )
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

    selectedOrderForCancel?.let { targetOrder ->
        AlertDialog(
            onDismissRequest = { selectedOrderForCancel = null },
            title = { Text("Cancel escrow order?") },
            text = {
                Text(
                    "${targetOrder.price.asCurrency()} will be released from escrow and returned " +
                        "to your available balance."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOrderStatus(targetOrder, OrderEntity.STATUS_CANCELLED)
                        selectedOrderForCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Cancel order")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForCancel = null }) {
                    Text("Keep order")
                }
            }
        )
    }
}

@Composable
private fun WalletStrip(wallet: WalletEntity?, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BalancePill(
            label = "Available",
            value = wallet?.availableBalance ?: 0.0,
            accent = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        BalancePill(
            label = "In Escrow",
            value = wallet?.frozenBalance ?: 0.0,
            accent = Color(0xFFE65100),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onBackClick) { Text("Back") }
    }
}

@Composable
private fun BalancePill(
    label: String,
    value: Double,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Text(
                text = value.asCurrency(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
fun OrderItemCard(
    order: OrderEntity,
    isPurchase: Boolean,
    onDeliverClick: () -> Unit,
    onConfirmReceipt: () -> Unit,
    onCancelOrder: () -> Unit,
    onDispute: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.US) }
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
                    text = order.price.asCurrency(),
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
                    text = "${order.platform} · ${order.category}",
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

            when {
                !isPurchase && order.status == OrderEntity.STATUS_ESCROW -> {
                    Button(
                        onClick = onDeliverClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Delivered")
                    }
                }

                isPurchase && (order.status == OrderEntity.STATUS_ESCROW ||
                    order.status == OrderEntity.STATUS_DELIVERED) -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDispute,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Dispute", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = onCancelOrder,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = onConfirmReceipt,
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                order.status == OrderEntity.STATUS_COMPLETED -> {
                    StatusNote("Funds released · order completed", Color(0xFF2E7D32))
                }

                order.status == OrderEntity.STATUS_CANCELLED -> {
                    StatusNote("Cancelled · escrow refunded", MaterialTheme.colorScheme.outline)
                }

                order.status == OrderEntity.STATUS_DISPUTED -> {
                    StatusNote("Under dispute review", MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun StatusNote(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun OrderStatusBadge(status: String) {
    val (backgroundColor, textColor, icon, label) = when (status) {
        OrderEntity.STATUS_ESCROW -> Quadruple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Default.Lock,
            "ESCROW HELD"
        )
        OrderEntity.STATUS_DELIVERED -> Quadruple(
            Color(0xFFE3F2FD),
            Color(0xFF1565C0),
            Icons.Default.LocalShipping,
            "DELIVERED"
        )
        OrderEntity.STATUS_COMPLETED -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Default.Check,
            "COMPLETED"
        )
        OrderEntity.STATUS_DISPUTED -> Quadruple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Icons.Default.ReportProblem,
            "DISPUTED"
        )
        OrderEntity.STATUS_CANCELLED -> Quadruple(
            Color(0xFFECEFF1),
            Color(0xFF546E7A),
            Icons.Default.Block,
            "CANCELLED"
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
