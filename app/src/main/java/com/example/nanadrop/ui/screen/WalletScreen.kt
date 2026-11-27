package com.example.nanadrop.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nanadrop.data.local.WalletEntity
import com.example.nanadrop.data.local.WalletTransactionEntity
import com.example.nanadrop.data.repository.WalletRepository
import com.example.nanadrop.ui.util.asCurrency
import com.example.nanadrop.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Hard cap for a single deposit / withdraw / freeze action. */
private const val MAX_ACTION_AMOUNT = 1_000_000.0

/** The wallet actions that can be triggered from the quick action row. */
private enum class WalletAction(val label: String, val icon: ImageVector, val hint: String) {
    DEPOSIT("Deposit", Icons.Default.ArrowDownward, "Add funds to your available balance."),
    WITHDRAW("Withdraw", Icons.Default.ArrowUpward, "Move funds out of your available balance."),
    FREEZE("Freeze", Icons.Default.Lock, "Move funds from available to frozen."),
    RELEASE("Release", Icons.Default.LockOpen, "Move frozen funds back to available.")
}

@Composable
fun WalletScreen(viewModel: WalletViewModel = viewModel()) {
    val walletState by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val scope = rememberCoroutineScope()

    // Non-null while an action dialog is open.
    var activeAction by remember { mutableStateOf<WalletAction?>(null) }

    // The title is provided by the app level TopAppBar, so this screen only renders content.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        WalletBalanceCards(walletState)
        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions (Deposit / Withdraw / Freeze / Release)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WalletAction.entries.forEach { action ->
                WalletActionButton(
                    text = action.label,
                    icon = action.icon,
                    onClick = { activeAction = action }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${transactions.size} transaction(s)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }

    activeAction?.let { action ->
        WalletActionDialog(
            action = action,
            wallet = walletState,
            onDismiss = { activeAction = null },
            onConfirm = { amount ->
                scope.launch {
                    when (action) {
                        WalletAction.DEPOSIT -> viewModel.deposit(amount)
                        WalletAction.WITHDRAW -> viewModel.withdraw(amount)
                        WalletAction.FREEZE -> viewModel.freezeFunds(amount, "Manual freeze")
                        WalletAction.RELEASE ->
                            viewModel.releaseFrozenToAvailable(amount, "Manual release")
                    }
                }
                activeAction = null
            }
        )
    }
}

@Composable
private fun RowScope.WalletActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            softWrap = false,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun WalletActionDialog(
    action: WalletAction,
    wallet: WalletEntity?,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toDoubleOrNull()
    val available = wallet?.availableBalance ?: 0.0
    val frozen = wallet?.frozenBalance ?: 0.0

    // Validation: amounts are capped, and withdraw/freeze/release can never exceed the source pot.
    val capError: String? = when {
        amount == null || amount <= 0.0 -> null
        amount > MAX_ACTION_AMOUNT -> "Maximum ${MAX_ACTION_AMOUNT.asCurrency()} per action."
        action == WalletAction.WITHDRAW && amount > available ->
            "Only ${available.asCurrency()} available."
        action == WalletAction.FREEZE && amount > available ->
            "Only ${available.asCurrency()} available to freeze."
        action == WalletAction.RELEASE && amount > frozen ->
            "Only ${frozen.asCurrency()} frozen."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(action.label) },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        // Digits with at most two decimals, and never above the cap.
                        val isWellFormed = input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))
                        val parsed = input.toDoubleOrNull() ?: 0.0
                        if (isWellFormed && parsed <= MAX_ACTION_AMOUNT) {
                            amountText = input
                        }
                    },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    singleLine = true,
                    isError = capError != null,
                    supportingText = {
                        Text(
                            text = capError ?: "Max ${MAX_ACTION_AMOUNT.asCurrency()} per action",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = action.hint, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available ${available.asCurrency()} · Frozen ${frozen.asCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { amount?.let(onConfirm) },
                enabled = amount != null && amount > 0.0 && capError == null
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Primary card for the available balance plus a two column row with the frozen balance and the
 * total earned, all formatted as `$1,250.00`.
 */
@Composable
fun WalletBalanceCards(wallet: WalletEntity?) {
    if (wallet == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Available Balance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = wallet.availableBalance.asCurrency(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ready to spend · protected by escrow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WalletStatCard(
            label = "Frozen Balance",
            amount = wallet.frozenBalance,
            icon = Icons.Default.Lock,
            accent = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        WalletStatCard(
            label = "Total Earned",
            amount = wallet.totalEarned,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            accent = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WalletStatCard(
    label: String,
    amount: Double,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = accent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount.asCurrency(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Card row for one ledger entry: colored type badge, title, timestamp and formatted amount. */
@Composable
fun TransactionItem(transaction: WalletTransactionEntity) {
    val style = transactionStyle(transaction.type)
    val timestampFormat = remember { SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.US) }
    val timestamp = remember(transaction.timestamp) {
        timestampFormat.format(Date(transaction.timestamp))
    }
    val signedAmount = (if (style.isCredit) "+" else "-") + transaction.amount.asCurrency()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = style.color.copy(alpha = 0.14f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = style.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = style.color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = signedAmount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = style.color
            )
        }
    }
}

private data class TransactionStyle(val label: String, val color: Color, val isCredit: Boolean)

@Composable
private fun transactionStyle(type: String): TransactionStyle = when (type) {
    WalletRepository.TYPE_DEPOSIT -> TransactionStyle("DEPOSIT", Color(0xFF2E7D32), true)
    WalletRepository.TYPE_WITHDRAW -> TransactionStyle("WITHDRAW", MaterialTheme.colorScheme.error, false)
    WalletRepository.TYPE_FREEZE -> TransactionStyle("FREEZE", Color(0xFFE65100), false)
    WalletRepository.TYPE_RELEASE -> TransactionStyle("RELEASE", Color(0xFF1565C0), true)
    WalletRepository.TYPE_REFUND -> TransactionStyle("REFUND", Color(0xFF6A1B9A), true)
    else -> TransactionStyle(type, MaterialTheme.colorScheme.onSurfaceVariant, false)
}
