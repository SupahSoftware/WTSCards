package com.wtscards.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wtscards.data.model.Order
import kotlinx.coroutines.delay
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgPrimary
import com.wtscards.ui.theme.bgSecondary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.borderInput
import com.wtscards.ui.theme.successColor
import com.wtscards.ui.theme.textOnAccent
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary

@Composable
fun OrderScreen(
    uiState: OrderUiState,
    onShowCreateDialog: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onNameChanged: (String) -> Unit,
    onStreetAddressChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onStateChanged: (String) -> Unit,
    onZipcodeChanged: (String) -> Unit,
    onCreateOrder: () -> Unit,
    onShowAddCardsDialog: (String) -> Unit,
    onDismissAddCardsDialog: () -> Unit,
    onAddCardsSearchChanged: (String) -> Unit,
    onToggleCardSelection: (String) -> Unit,
    onProceedToPriceConfirmation: () -> Unit,
    onCardPriceChanged: (String, String) -> Unit,
    onConfirmAddCards: () -> Unit,
    onClearToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-clear toast after 3 seconds
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            delay(3000)
            onClearToast()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Header
            Text(
                text = "Orders",
                style = MaterialTheme.typography.headlineMedium,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Content
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(color = accentPrimary)
                    }
                    uiState.error != null -> {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    uiState.orders.isEmpty() -> {
                        Text(
                            text = "No orders yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondary
                        )
                    }
                    else -> {
                        OrderList(
                            orders = uiState.orders,
                            onShowAddCardsDialog = onShowAddCardsDialog
                        )
                    }
                }
            }
        }

        // FAB for creating new order
        FloatingActionButton(
            onClick = onShowCreateDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = accentPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create order",
                tint = textOnAccent
            )
        }

        // Create Order Dialog
        if (uiState.showCreateDialog) {
            CreateOrderDialog(
                formState = uiState.createFormState,
                onDismiss = onDismissCreateDialog,
                onNameChanged = onNameChanged,
                onStreetAddressChanged = onStreetAddressChanged,
                onCityChanged = onCityChanged,
                onStateChanged = onStateChanged,
                onZipcodeChanged = onZipcodeChanged,
                onCreateOrder = onCreateOrder
            )
        }
        
        // Add Cards Dialog
        uiState.addCardsDialogState?.let { dialogState ->
            AddCardsDialog(
                dialogState = dialogState,
                availableCards = uiState.availableCards,
                onDismiss = onDismissAddCardsDialog,
                onSearchChanged = onAddCardsSearchChanged,
                onToggleCardSelection = onToggleCardSelection,
                onProceedToPriceConfirmation = onProceedToPriceConfirmation,
                onCardPriceChanged = onCardPriceChanged,
                onConfirmAddCards = onConfirmAddCards
            )
        }

        // Toast message
        uiState.toastMessage?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentPrimary)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textOnAccent
                )
            }
        }
    }
}

@Composable
private fun OrderList(
    orders: List<Order>,
    onShowAddCardsDialog: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = orders,
            key = { it.id }
        ) { order ->
            OrderCard(
                order = order,
                onShowAddCardsDialog = onShowAddCardsDialog
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onShowAddCardsDialog: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgSurface)
            .padding(16.dp)
    ) {
        // Left side: main content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = order.name,
                style = MaterialTheme.typography.titleLarge,
                color = textPrimary
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = order.streetAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
            Text(
                text = "${order.city}, ${order.state} ${order.zipcode}",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatOrderDate(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = textTertiary
            )
            
            // Display cards if present
            if (order.cards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                order.cards.forEach { card ->
                    Text(
                        text = "${card.name}    ${formatPrice(card.priceSold ?: 0)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
        
        // Right side: total price and add cards button
        Column(
            modifier = Modifier.align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatPrice(order.cards.sumOf { it.priceSold ?: 0 }),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = successColor
            )
            
            androidx.compose.material3.TextButton(
                onClick = { onShowAddCardsDialog(order.id) },
                modifier = Modifier.padding(0.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    text = "ADD CARDS",
                    style = MaterialTheme.typography.bodyLarge,
                    color = accentPrimary
                )
            }
        }
    }
}

private fun formatPrice(priceInPennies: Long): String {
    val dollars = priceInPennies / 100.0
    return "$${String.format("%.2f", dollars)}"
}

@Composable
private fun CreateOrderDialog(
    formState: CreateOrderFormState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onStreetAddressChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onStateChanged: (String) -> Unit,
    onZipcodeChanged: (String) -> Unit,
    onCreateOrder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Order",
                style = MaterialTheme.typography.titleLarge,
                color = textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = onNameChanged,
                    label = { Text("Name", color = textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = borderInput,
                        cursorColor = accentPrimary,
                        focusedContainerColor = bgSecondary,
                        unfocusedContainerColor = bgSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Street Address
                OutlinedTextField(
                    value = formState.streetAddress,
                    onValueChange = onStreetAddressChanged,
                    label = { Text("Street Address", color = textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = accentPrimary,
                        focusedContainerColor = bgSecondary,
                        unfocusedContainerColor = bgSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // City, State, Zipcode row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // City
                    OutlinedTextField(
                        value = formState.city,
                        onValueChange = onCityChanged,
                        label = { Text("City", color = textTertiary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = accentPrimary,
                            focusedContainerColor = bgSecondary,
                            unfocusedContainerColor = bgSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // State
                    OutlinedTextField(
                        value = formState.state,
                        onValueChange = onStateChanged,
                        label = { Text("State", color = textTertiary) },
                        modifier = Modifier.width(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = accentPrimary,
                            focusedContainerColor = bgSecondary,
                            unfocusedContainerColor = bgSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Zipcode
                    OutlinedTextField(
                        value = formState.zipcode,
                        onValueChange = onZipcodeChanged,
                        label = { Text("Zip", color = textTertiary) },
                        modifier = Modifier.width(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = accentPrimary,
                            focusedContainerColor = bgSecondary,
                            unfocusedContainerColor = bgSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreateOrder,
                enabled = formState.isValid(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentPrimary,
                    contentColor = textOnAccent,
                    disabledContainerColor = bgSecondary,
                    disabledContentColor = textTertiary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Order")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !formState.isSaving
            ) {
                Text("Cancel", color = textSecondary)
            }
        },
        containerColor = bgSurface,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun AddCardsDialog(
    dialogState: AddCardsDialogState,
    availableCards: List<com.wtscards.data.model.Card>,
    onDismiss: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onToggleCardSelection: (String) -> Unit,
    onProceedToPriceConfirmation: () -> Unit,
    onCardPriceChanged: (String, String) -> Unit,
    onConfirmAddCards: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (dialogState.step == AddCardsStep.SELECT_CARDS) "Add Cards to Order" else "Confirm Prices",
                style = MaterialTheme.typography.titleLarge,
                color = textPrimary
            )
        },
        text = {
            when (dialogState.step) {
                AddCardsStep.SELECT_CARDS -> {
                    CardSelectionContent(
                        searchQuery = dialogState.searchQuery,
                        availableCards = availableCards,
                        selectedCardIds = dialogState.selectedCardIds,
                        onSearchChanged = onSearchChanged,
                        onToggleCardSelection = onToggleCardSelection
                    )
                }
                AddCardsStep.CONFIRM_PRICES -> {
                    PriceConfirmationContent(
                        selectedCards = availableCards.filter { it.id in dialogState.selectedCardIds },
                        cardPrices = dialogState.cardPrices,
                        onCardPriceChanged = onCardPriceChanged
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (dialogState.step) {
                        AddCardsStep.SELECT_CARDS -> onProceedToPriceConfirmation()
                        AddCardsStep.CONFIRM_PRICES -> onConfirmAddCards()
                    }
                },
                enabled = when (dialogState.step) {
                    AddCardsStep.SELECT_CARDS -> dialogState.selectedCardIds.isNotEmpty() && !dialogState.isSaving
                    AddCardsStep.CONFIRM_PRICES -> {
                        val selectedCards = availableCards.filter { it.id in dialogState.selectedCardIds }
                        selectedCards.all { card -> 
                            val price = dialogState.cardPrices[card.id]
                            price != null && price.isNotBlank()
                        } && !dialogState.isSaving
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentPrimary,
                    contentColor = textOnAccent,
                    disabledContainerColor = bgSecondary,
                    disabledContentColor = textTertiary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (dialogState.step == AddCardsStep.SELECT_CARDS) "Next" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !dialogState.isSaving
            ) {
                Text("Cancel", color = textSecondary)
            }
        },
        containerColor = bgSurface,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun CardSelectionContent(
    searchQuery: String,
    availableCards: List<com.wtscards.data.model.Card>,
    selectedCardIds: Set<String>,
    onSearchChanged: (String) -> Unit,
    onToggleCardSelection: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().height(500.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search cards...", color = textTertiary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = textTertiary
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = accentPrimary,
                focusedContainerColor = bgSecondary,
                unfocusedContainerColor = bgSecondary
            ),
            shape = RoundedCornerShape(8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Cards list
        val filteredCards = if (searchQuery.isBlank()) {
            availableCards
        } else {
            availableCards.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = filteredCards,
                key = { it.id }
            ) { card ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (card.id in selectedCardIds) accentPrimary.copy(alpha = 0.1f) else bgPrimary)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = card.id in selectedCardIds,
                        onCheckedChange = { onToggleCardSelection(card.id) },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = accentPrimary,
                            uncheckedColor = textTertiary,
                            checkmarkColor = textOnAccent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceConfirmationContent(
    selectedCards: List<com.wtscards.data.model.Card>,
    cardPrices: Map<String, String>,
    onCardPriceChanged: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().height(500.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = selectedCards,
                key = { it.id }
            ) { card ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(bgPrimary)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = cardPrices[card.id] ?: "",
                        onValueChange = { value ->
                            // Only allow digits and decimal point
                            val filtered = value.filter { it.isDigit() || it == '.' }
                            // Only allow one decimal point
                            if (filtered.count { it == '.' } <= 1) {
                                onCardPriceChanged(card.id, filtered)
                            }
                        },
                        modifier = Modifier.width(120.dp),
                        placeholder = { Text("0.00", color = textTertiary) },
                        prefix = { Text("$", color = textPrimary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = accentPrimary,
                            focusedContainerColor = bgSecondary,
                            unfocusedContainerColor = bgSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun formatOrderDate(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    
    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val month = monthNames[calendar.get(java.util.Calendar.MONTH)]
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val year = calendar.get(java.util.Calendar.YEAR)
    val hour24 = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)
    
    val amPm = if (hour24 < 12) "am" else "pm"
    val displayHour = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    
    val minuteStr = minute.toString().padStart(2, '0')
    
    return "$month $day, $year $displayHour:${minuteStr}$amPm"
}
