package com.example.nanadrop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nanadrop.data.local.ListingEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDialog(
    editingListing: ListingEntity?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        category: String,
        price: Double,
        sellerName: String,
        description: String,
        platform: String,
        isUserListing: Boolean,
        isSold: Boolean
    ) -> Unit
) {
    val isEditMode = editingListing != null

    var title by remember { mutableStateOf(editingListing?.title ?: "") }
    var category by remember { mutableStateOf(editingListing?.category ?: "Keys") }
    var priceText by remember { mutableStateOf(editingListing?.price?.toString() ?: "") }
    var sellerName by remember { mutableStateOf(editingListing?.sellerName ?: "") }
    var description by remember { mutableStateOf(editingListing?.description ?: "") }
    var platform by remember { mutableStateOf(editingListing?.platform ?: "") }
    var isSold by remember { mutableStateOf(editingListing?.isSold ?: false) }

    var titleError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val categories = listOf("Keys", "Accounts", "Subscriptions", "Hardware", "Other")
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Edit Listing" else "Create New Listing",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    label = { Text("Title *") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Price
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        priceError = it.toDoubleOrNull() == null || (it.toDoubleOrNull() ?: -1.0) < 0
                    },
                    label = { Text("Price ($) *") },
                    isError = priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Seller Name
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    label = { Text("Seller Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Platform
                OutlinedTextField(
                    value = platform,
                    onValueChange = { platform = it },
                    label = { Text("Platform (e.g. Steam, PC, PS5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Is Sold Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mark as Sold")
                    Switch(
                        checked = isSold,
                        onCheckedChange = { isSold = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    if (title.isBlank()) titleError = true
                    if (price == null || price < 0) priceError = true

                    if (title.isNotBlank() && price != null && price >= 0) {
                        onSave(
                            editingListing?.id ?: 0L,
                            title.trim(),
                            category,
                            price,
                            if (sellerName.isBlank()) "User" else sellerName.trim(),
                            description.trim(),
                            if (platform.isBlank()) "PC" else platform.trim(),
                            true,
                            isSold
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEditMode) "Save Changes" else "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
