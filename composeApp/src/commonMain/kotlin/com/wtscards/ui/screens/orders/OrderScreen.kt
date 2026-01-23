package com.wtscards.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.wtscards.data.model.Order
import com.wtscards.data.model.OrderStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxHeight
import com.wtscards.ui.components.AppDropdown
import androidx.compose.foundation.layout.PaddingValues
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgDropdown
import com.wtscards.ui.theme.bgPrimary
import com.wtscards.ui.theme.bgSecondary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.borderInput
import com.wtscards.ui.theme.errorColor
import com.wtscards.ui.theme.successColor
import com.wtscards.ui.theme.warningColor
import com.wtscards.ui.theme.textOnAccent
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary

@Composable
fun OrderScreen(
    uiState: OrderUiState,
    onToggleFabExpanded: () -> Unit,
    onCollapseFab: () -> Unit,
    onShowCreateDialog: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onShowShippingLabelsDialog: () -> Unit,
    onDismissShippingLabelsDialog: () -> Unit,
    onExportShippingLabels: (List<Order>) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onStatusFilterToggled: (String) -> Unit,
    onSortOptionChanged: (OrderSortOption) -> Unit,
    onNameChanged: (String) -> Unit,
    onStreetAddressChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onStateChanged: (String) -> Unit,
    onZipcodeChanged: (String) -> Unit,
    onShippingTypeChanged: (String) -> Unit,
    onShippingPriceChanged: (String) -> Unit,
    onCreateOrUpdateOrder: () -> Unit,
    onEditOrder: (Order) -> Unit,
    onStatusChanged: (String, String) -> Unit,
    onShowAddCardsDialog: (String) -> Unit,
    onDismissAddCardsDialog: () -> Unit,
    onAddCardsSearchChanged: (String) -> Unit,
    onToggleCardSelection: (String) -> Unit,
    onProceedToPriceConfirmation: () -> Unit,
    onCardPriceChanged: (String, String) -> Unit,
    onConfirmAddCards: () -> Unit,
    onShowRemoveCardDialog: (String, String, String) -> Unit,
    onDismissRemoveCardDialog: () -> Unit,
    onConfirmRemoveCard: () -> Unit,
    onClearToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-clear toast after 3 seconds
    LaunchedEffect(uiState.toast) {
        if (uiState.toast != null) {
            delay(3000)
            onClearToast()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header row with title and status filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Orders",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textPrimary
                )

                // Status filter checkboxes
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OrderStatus.allStatuses.forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onStatusFilterToggled(status) }
                                .padding(end = 16.dp)
                        ) {
                            Checkbox(
                                modifier = Modifier.padding(0.dp),
                                checked = status in uiState.statusFilters,
                                onCheckedChange = { onStatusFilterToggled(status) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = when (status) {
                                        OrderStatus.NEW -> errorColor
                                        OrderStatus.LABEL_CREATED -> warningColor
                                        OrderStatus.SHIPPED -> successColor
                                        else -> accentPrimary
                                    },
                                    uncheckedColor = textTertiary,
                                    checkmarkColor = textOnAccent
                                )
                            )
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Search and Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar (weight 3)
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxHeight(),
                    placeholder = {
                        Text(
                            text = "Search by purchaser info or included card names",
                            color = textTertiary
                        )
                    },
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
                        focusedContainerColor = bgSurface,
                        unfocusedContainerColor = bgSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Sort Dropdown (weight 1)
                OrderSortDropdown(
                    selectedOption = uiState.sortOption,
                    onOptionSelected = onSortOptionChanged,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    uiState.filteredOrders.isEmpty() -> {
                        Text(
                            text = "No orders match your search",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondary
                        )
                    }
                    else -> {
                        OrderList(
                            orders = uiState.filteredOrders,
                            onShowAddCardsDialog = onShowAddCardsDialog,
                            onEditOrder = onEditOrder,
                            onStatusChanged = onStatusChanged,
                            onShowRemoveCardDialog = onShowRemoveCardDialog
                        )
                    }
                }
            }
        }

        // Scrim when FAB is expanded (must be before FAB menu so it's behind it)
        if (uiState.isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onCollapseFab
                    )
            )
        }

        // Expandable FAB Menu
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Create Shipping Labels option
            AnimatedVisibility(
                visible = uiState.isFabExpanded,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Create shipping labels",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary,
                        modifier = Modifier
                            .background(bgSurface, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    SmallFloatingActionButton(
                        onClick = onShowShippingLabelsDialog,
                        containerColor = accentPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Create shipping labels",
                            tint = textOnAccent
                        )
                    }
                }
            }

            // Create Order option
            AnimatedVisibility(
                visible = uiState.isFabExpanded,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Create order",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary,
                        modifier = Modifier
                            .background(bgSurface, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    SmallFloatingActionButton(
                        onClick = onShowCreateDialog,
                        containerColor = accentPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create order",
                            tint = textOnAccent
                        )
                    }
                }
            }

            // Main FAB (toggle)
            FloatingActionButton(
                onClick = onToggleFabExpanded,
                containerColor = accentPrimary
            ) {
                Icon(
                    imageVector = if (uiState.isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (uiState.isFabExpanded) "Close menu" else "Open menu",
                    tint = textOnAccent
                )
            }
        }

        // Create/Edit Order Dialog
        if (uiState.showCreateDialog) {
            CreateOrderDialog(
                formState = uiState.createFormState,
                isEditMode = uiState.editingOrderId != null,
                onDismiss = onDismissCreateDialog,
                onNameChanged = onNameChanged,
                onStreetAddressChanged = onStreetAddressChanged,
                onCityChanged = onCityChanged,
                onStateChanged = onStateChanged,
                onZipcodeChanged = onZipcodeChanged,
                onShippingTypeChanged = onShippingTypeChanged,
                onShippingPriceChanged = onShippingPriceChanged,
                onConfirm = onCreateOrUpdateOrder
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

        // Remove Card Confirmation Dialog
        uiState.removeCardDialogState?.let { dialogState ->
            AlertDialog(
                onDismissRequest = { if (!dialogState.isRemoving) onDismissRemoveCardDialog() },
                title = {
                    Text(
                        text = "Remove ${dialogState.cardName} from order?",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary
                    )
                },
                text = {
                    Text(
                        text = "Removing this card from the order will place it back in your collection and it will no longer have a sold record.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmRemoveCard,
                        enabled = !dialogState.isRemoving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = errorColor,
                            contentColor = textOnAccent,
                            disabledContainerColor = bgSecondary,
                            disabledContentColor = textTertiary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (dialogState.isRemoving) "Removing..." else "Remove")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismissRemoveCardDialog,
                        enabled = !dialogState.isRemoving
                    ) {
                        Text("Cancel", color = textSecondary)
                    }
                },
                containerColor = bgSurface,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Shipping Labels Confirmation Dialog
        if (uiState.showShippingLabelsDialog) {
            val newOrders = uiState.newStatusOrders
            AlertDialog(
                onDismissRequest = onDismissShippingLabelsDialog,
                title = {
                    Text(
                        text = "Create Shipping Labels",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary
                    )
                },
                text = {
                    if (newOrders.isEmpty()) {
                        Text(
                            text = "No orders with 'New' status to export.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    } else {
                        Text(
                            text = "${newOrders.size} shipping label row${if (newOrders.size > 1) "s" else ""} will be created in a CSV file that Pirate Ship will accept as an import.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onExportShippingLabels(newOrders) },
                        enabled = newOrders.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentPrimary,
                            contentColor = textOnAccent,
                            disabledContainerColor = bgSecondary,
                            disabledContentColor = textTertiary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Export")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissShippingLabelsDialog) {
                        Text("Cancel", color = textSecondary)
                    }
                },
                containerColor = bgSurface,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Toast message
        uiState.toast?.let { toast ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (toast.isError) errorColor else successColor)
                    .padding(16.dp)
            ) {
                Text(
                    text = toast.message,
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
    onShowAddCardsDialog: (String) -> Unit,
    onEditOrder: (Order) -> Unit,
    onStatusChanged: (String, String) -> Unit,
    onShowRemoveCardDialog: (String, String, String) -> Unit
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
                onShowAddCardsDialog = onShowAddCardsDialog,
                onEditOrder = onEditOrder,
                onStatusChanged = onStatusChanged,
                onShowRemoveCardDialog = onShowRemoveCardDialog
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onShowAddCardsDialog: (String) -> Unit,
    onEditOrder: (Order) -> Unit,
    onStatusChanged: (String, String) -> Unit,
    onShowRemoveCardDialog: (String, String, String) -> Unit
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    var isEditingCards by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isEditingCards) {
                    Modifier.border(2.dp, errorColor, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .background(bgSurface)
            .padding(16.dp)
    ) {
        // Top row: purchaser name on left, status dropdown and overflow menu on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: purchaser name
            Text(
                text = order.name,
                style = MaterialTheme.typography.titleLarge,
                color = textPrimary
            )

            // Right side: status dropdown and overflow menu (or back icon in edit mode)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppDropdown(
                    modifier = Modifier.width(200.dp),
                    padding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    selectedValue = order.status,
                    options = OrderStatus.allStatuses,
                    onOptionSelected = { newStatus ->
                        if (newStatus != order.status) {
                            onStatusChanged(order.id, newStatus)
                        }
                    },
                    backgroundColor = when (order.status.trim()) {
                        OrderStatus.NEW -> errorColor
                        OrderStatus.LABEL_CREATED -> warningColor
                        OrderStatus.SHIPPED -> successColor
                        else -> bgPrimary
                    },
                    textColor = textOnAccent
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (isEditingCards) {
                    // Back icon to exit edit mode
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit edit mode",
                        tint = textSecondary,
                        modifier = Modifier
                            .clickable { isEditingCards = false }
                            .padding(4.dp)
                    )
                } else {
                    // Overflow menu
                    Box {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = textSecondary,
                            modifier = Modifier
                                .clickable { showOverflowMenu = true }
                                .padding(4.dp)
                        )

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = textPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add cards", color = textPrimary)
                                    }
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onShowAddCardsDialog(order.id)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = textPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Edit order", color = textPrimary)
                                    }
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onEditOrder(order)
                                }
                            )
                            if (order.cards.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = textPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Edit cards", color = textPrimary)
                                        }
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        isEditingCards = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Address info
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

        // Display shipping and cards if present
        if (order.shippingType != null || order.cards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            // Shipping line item first
            if (order.shippingType != null) {
                Text(
                    text = "Shipping: ${order.shippingType}    ${formatPrice(order.shippingCost)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Cards
            order.cards.forEach { card ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingCards) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove card",
                            tint = errorColor,
                            modifier = Modifier
                                .clickable {
                                    onShowRemoveCardDialog(order.id, card.id, card.name)
                                }
                                .padding(end = 8.dp)
                        )
                    }
                    Text(
                        text = "${card.name}    ${formatPrice(card.priceSold ?: 0)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Total at the bottom
        Spacer(modifier = Modifier.height(8.dp))
        val cardsTotal = order.cards.sumOf { it.priceSold ?: 0 }
        val total = cardsTotal + order.shippingCost
        Text(
            text = "Total ${formatPrice(total)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = successColor
        )
    }
}

@Composable
private fun OrderSortDropdown(
    selectedOption: OrderSortOption,
    onOptionSelected: (OrderSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(bgDropdown)
                .clickable { expanded = true }
                .onGloballyPositioned { coordinates ->
                    dropdownWidth = coordinates.size.width
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = selectedOption.displayName(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (selectedOption.isAscending()) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = accentPrimary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(bgDropdown)
                .then(
                    if (dropdownWidth > 0) {
                        Modifier.width(with(density) { dropdownWidth.toDp() })
                    } else {
                        Modifier
                    }
                )
        ) {
            OrderSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.displayName(),
                                color = if (option == selectedOption) accentPrimary else textPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (option.isAscending()) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (option == selectedOption) accentPrimary else textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun OrderSortOption.displayName(): String = when (this) {
    OrderSortOption.DATE_DESC -> "Date"
    OrderSortOption.DATE_ASC -> "Date"
    OrderSortOption.TOTAL_DESC -> "Order total"
    OrderSortOption.TOTAL_ASC -> "Order total"
}

private fun OrderSortOption.isAscending(): Boolean = when (this) {
    OrderSortOption.DATE_DESC -> false  // Newest first shows down arrow
    OrderSortOption.DATE_ASC -> true    // Oldest first shows up arrow
    OrderSortOption.TOTAL_DESC -> false // Most expensive first shows down arrow
    OrderSortOption.TOTAL_ASC -> true   // Cheapest first shows up arrow
}

private fun formatPrice(priceInPennies: Long): String {
    val dollars = priceInPennies / 100.0
    return "$${String.format("%.2f", dollars)}"
}

private val shippingTypeOptions = listOf("Bubble mailer", "Envelope", "Box", "Other")

@Composable
private fun CreateOrderDialog(
    formState: CreateOrderFormState,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onStreetAddressChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onStateChanged: (String) -> Unit,
    onZipcodeChanged: (String) -> Unit,
    onShippingTypeChanged: (String) -> Unit,
    onShippingPriceChanged: (String) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Edit Order" else "Create Order",
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
                DialogFormTextField(
                    value = formState.name,
                    onValueChange = onNameChanged,
                    label = "Name",
                    placeholder = "Customer name"
                )

                // Street Address
                DialogFormTextField(
                    value = formState.streetAddress,
                    onValueChange = onStreetAddressChanged,
                    label = "Street Address",
                    placeholder = "123 Main St"
                )

                // City, State, Zipcode row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // City
                    DialogFormTextField(
                        value = formState.city,
                        onValueChange = onCityChanged,
                        label = "City",
                        placeholder = "City",
                        modifier = Modifier.weight(1f)
                    )

                    // State
                    DialogFormTextField(
                        value = formState.state,
                        onValueChange = onStateChanged,
                        label = "State",
                        placeholder = "CA",
                        modifier = Modifier.width(80.dp)
                    )

                    // Zipcode
                    DialogFormTextField(
                        value = formState.zipcode,
                        onValueChange = onZipcodeChanged,
                        label = "Zip",
                        placeholder = "12345",
                        modifier = Modifier.width(100.dp)
                    )
                }

                // Package type and Shipping price row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Package type dropdown
                    ShippingTypeDropdown(
                        selectedType = formState.shippingType,
                        onTypeSelected = onShippingTypeChanged,
                        modifier = Modifier.weight(1f)
                    )

                    // Shipping price
                    DialogFormTextField(
                        value = formState.shippingPrice,
                        onValueChange = onShippingPriceChanged,
                        label = "Shipping Price",
                        placeholder = "0.00",
                        prefix = "$",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = formState.isValid(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentPrimary,
                    contentColor = textOnAccent,
                    disabledContainerColor = bgSecondary,
                    disabledContentColor = textTertiary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditMode) "Save Changes" else "Create Order")
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
private fun DialogFormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    prefix: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = textTertiary
                )
            },
            prefix = if (prefix != null) {
                { Text(prefix, color = textPrimary) }
            } else null,
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
    }
}

@Composable
private fun ShippingTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Package Type",
            style = MaterialTheme.typography.bodyMedium,
            color = textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        AppDropdown(
            selectedValue = selectedType,
            options = shippingTypeOptions,
            onOptionSelected = onTypeSelected
        )
    }
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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            listState.scrollBy(-dragAmount.y)
                        }
                    }
                },
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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth().height(500.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            listState.scrollBy(-dragAmount.y)
                        }
                    }
                },
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        .padding(horizontal = 8.dp, vertical = 4.dp),
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
                        shape = RoundedCornerShape(8.dp)
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
