package com.wtscards.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wtscards.data.model.Order
import com.wtscards.data.model.OrderStatus
import com.wtscards.ui.components.AppDropdown
import com.wtscards.ui.components.ScrollableList
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgDropdown
import com.wtscards.ui.theme.bgPrimary
import com.wtscards.ui.theme.bgSecondary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.borderInput
import com.wtscards.ui.theme.errorColor
import com.wtscards.ui.theme.successColor
import com.wtscards.ui.theme.textOnAccent
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary
import com.wtscards.ui.theme.warningColor
import kotlin.math.ceil
import kotlinx.coroutines.delay

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
        onCreateOrderTrackingNumberChanged: (String) -> Unit,
        onDiscountChanged: (String) -> Unit,
        onLengthChanged: (String) -> Unit,
        onWidthChanged: (String) -> Unit,
        onHeightChanged: (String) -> Unit,
        onPoundsChanged: (String) -> Unit,
        onOuncesChanged: (String) -> Unit,
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
        onShowUpgradeShippingDialog: (String, Int) -> Unit,
        onDismissUpgradeShippingDialog: () -> Unit,
        onConfirmUpgradeShipping: () -> Unit,
        onShowSplitOrderDialog: (String, Int) -> Unit,
        onDismissSplitOrderDialog: () -> Unit,
        onConfirmSplitOrder: () -> Unit,
        onShowTrackingNumberDialog: (String, String?) -> Unit,
        onDismissTrackingNumberDialog: () -> Unit,
        onTrackingNumberChanged: (String) -> Unit,
        onConfirmTrackingNumber: () -> Unit,
        onDeleteOrder: (String) -> Unit,
        onShowToast: (String) -> Unit,
        onClearToast: () -> Unit,
        modifier: Modifier = Modifier
) {
    ToastAutoCloseEffect(toast = uiState.toast, onClearToast = onClearToast)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            HeaderRow(
                    statusFilters = uiState.statusFilters,
                    onStatusFilterToggled = onStatusFilterToggled
            )
            Spacer(modifier = Modifier.height(4.dp))
            SearchAndSortRow(
                    searchQuery = uiState.searchQuery,
                    sortOption = uiState.sortOption,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onSortOptionChanged = onSortOptionChanged
            )
            Spacer(modifier = Modifier.height(12.dp))
            ContentArea(
                    uiState = uiState,
                    onShowAddCardsDialog = onShowAddCardsDialog,
                    onEditOrder = onEditOrder,
                    onStatusChanged = onStatusChanged,
                    onShowRemoveCardDialog = onShowRemoveCardDialog,
                    onUpgradeShipping = onShowUpgradeShippingDialog,
                    onSplitOrder = onShowSplitOrderDialog,
                    onShowTrackingNumberDialog = onShowTrackingNumberDialog,
                    onDeleteOrder = onDeleteOrder,
                    onShowToast = onShowToast,
                    freeShippingEnabled = uiState.freeShippingEnabled,
                    freeShippingThreshold = uiState.freeShippingThreshold,
                    nicePricesEnabled = uiState.nicePricesEnabled
            )
        }

        FabScrim(visible = uiState.isFabExpanded, onDismiss = onCollapseFab)

        FabMenu(
                isExpanded = uiState.isFabExpanded,
                onToggle = onToggleFabExpanded,
                onCreateOrder = onShowCreateDialog,
                onCreateShippingLabels = onShowShippingLabelsDialog
        )

        OrderDialogs(
                uiState = uiState,
                onDismissCreateDialog = onDismissCreateDialog,
                onNameChanged = onNameChanged,
                onStreetAddressChanged = onStreetAddressChanged,
                onCityChanged = onCityChanged,
                onStateChanged = onStateChanged,
                onZipcodeChanged = onZipcodeChanged,
                onShippingTypeChanged = onShippingTypeChanged,
                onShippingPriceChanged = onShippingPriceChanged,
                onCreateOrderTrackingNumberChanged = onCreateOrderTrackingNumberChanged,
                onDiscountChanged = onDiscountChanged,
                onLengthChanged = onLengthChanged,
                onWidthChanged = onWidthChanged,
                onHeightChanged = onHeightChanged,
                onPoundsChanged = onPoundsChanged,
                onOuncesChanged = onOuncesChanged,
                onCreateOrUpdateOrder = onCreateOrUpdateOrder,
                onDismissAddCardsDialog = onDismissAddCardsDialog,
                onAddCardsSearchChanged = onAddCardsSearchChanged,
                onToggleCardSelection = onToggleCardSelection,
                onProceedToPriceConfirmation = onProceedToPriceConfirmation,
                onCardPriceChanged = onCardPriceChanged,
                onConfirmAddCards = onConfirmAddCards,
                onDismissRemoveCardDialog = onDismissRemoveCardDialog,
                onConfirmRemoveCard = onConfirmRemoveCard,
                onDismissUpgradeShippingDialog = onDismissUpgradeShippingDialog,
                onConfirmUpgradeShipping = onConfirmUpgradeShipping,
                onDismissSplitOrderDialog = onDismissSplitOrderDialog,
                onConfirmSplitOrder = onConfirmSplitOrder,
                onDismissShippingLabelsDialog = onDismissShippingLabelsDialog,
                onExportShippingLabels = onExportShippingLabels,
                onDismissTrackingNumberDialog = onDismissTrackingNumberDialog,
                onTrackingNumberChanged = onTrackingNumberChanged,
                onConfirmTrackingNumber = onConfirmTrackingNumber
        )

        OrderToast(toast = uiState.toast)
    }
}

@Composable
private fun ToastAutoCloseEffect(toast: ToastState?, onClearToast: () -> Unit) {
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(3000)
            onClearToast()
        }
    }
}

@Composable
private fun HeaderRow(statusFilters: Set<String>, onStatusFilterToggled: (String) -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Orders", style = MaterialTheme.typography.headlineMedium, color = textPrimary)

        Row(verticalAlignment = Alignment.CenterVertically) {
            OrderStatus.allStatuses.forEach { status ->
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                                Modifier.clickable { onStatusFilterToggled(status) }
                                        .padding(end = 16.dp)
                ) {
                    Checkbox(
                            modifier = Modifier.padding(0.dp),
                            checked = status in statusFilters,
                            onCheckedChange = { onStatusFilterToggled(status) },
                            colors =
                                    CheckboxDefaults.colors(
                                            checkedColor =
                                                    when (status) {
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
}

@Composable
private fun SearchAndSortRow(
        searchQuery: String,
        sortOption: OrderSortOption,
        onSearchQueryChanged: (String) -> Unit,
        onSortOptionChanged: (OrderSortOption) -> Unit
) {
    Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.weight(3f).fillMaxHeight(),
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
                colors =
                        OutlinedTextFieldDefaults.colors(
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

        OrderSortDropdown(
                selectedOption = sortOption,
                onOptionSelected = onSortOptionChanged,
                modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

@Composable
private fun ContentArea(
        uiState: OrderUiState,
        onShowAddCardsDialog: (String) -> Unit,
        onEditOrder: (Order) -> Unit,
        onStatusChanged: (String, String) -> Unit,
        onShowRemoveCardDialog: (String, String, String) -> Unit,
        onUpgradeShipping: (String, Int) -> Unit,
        onSplitOrder: (String, Int) -> Unit,
        onShowTrackingNumberDialog: (String, String?) -> Unit,
        onDeleteOrder: (String) -> Unit,
        onShowToast: (String) -> Unit,
        freeShippingEnabled: Boolean,
        freeShippingThreshold: Long,
        nicePricesEnabled: Boolean
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = accentPrimary)
            }
            uiState.error != null -> {
                Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
            }
            uiState.orders.isEmpty() -> {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                            text = "No orders yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                            text =
                                    "When you add cards to an order, they will be removed from your collection. If you remove cards from an order, they will return to your collection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
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
                        onShowRemoveCardDialog = onShowRemoveCardDialog,
                        onUpgradeShipping = onUpgradeShipping,
                        onSplitOrder = onSplitOrder,
                        onShowTrackingNumberDialog = onShowTrackingNumberDialog,
                        onDeleteOrder = onDeleteOrder,
                        onShowToast = onShowToast,
                        freeShippingEnabled = freeShippingEnabled,
                        freeShippingThreshold = freeShippingThreshold,
                        nicePricesEnabled = nicePricesEnabled
                )
            }
        }
    }
}

@Composable
private fun FabScrim(visible: Boolean, onDismiss: () -> Unit) {
    if (visible) {
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clickable(
                                        indication = null,
                                        interactionSource =
                                                remember {
                                                    androidx.compose.foundation.interaction
                                                            .MutableInteractionSource()
                                                },
                                        onClick = onDismiss
                                )
        )
    }
}

@Composable
private fun BoxScope.FabMenu(
        isExpanded: Boolean,
        onToggle: () -> Unit,
        onCreateOrder: () -> Unit,
        onCreateShippingLabels: () -> Unit
) {
    Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
                visible = isExpanded,
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
                        modifier =
                                Modifier.background(bgSurface, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                SmallFloatingActionButton(
                        onClick = onCreateShippingLabels,
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

        AnimatedVisibility(
                visible = isExpanded,
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
                        modifier =
                                Modifier.background(bgSurface, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                SmallFloatingActionButton(onClick = onCreateOrder, containerColor = accentPrimary) {
                    Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create order",
                            tint = textOnAccent
                    )
                }
            }
        }

        FloatingActionButton(onClick = onToggle, containerColor = accentPrimary) {
            Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isExpanded) "Close menu" else "Open menu",
                    tint = textOnAccent
            )
        }
    }
}

@Composable
private fun OrderDialogs(
        uiState: OrderUiState,
        onDismissCreateDialog: () -> Unit,
        onNameChanged: (String) -> Unit,
        onStreetAddressChanged: (String) -> Unit,
        onCityChanged: (String) -> Unit,
        onStateChanged: (String) -> Unit,
        onZipcodeChanged: (String) -> Unit,
        onShippingTypeChanged: (String) -> Unit,
        onShippingPriceChanged: (String) -> Unit,
        onCreateOrderTrackingNumberChanged: (String) -> Unit,
        onDiscountChanged: (String) -> Unit,
        onLengthChanged: (String) -> Unit,
        onWidthChanged: (String) -> Unit,
        onHeightChanged: (String) -> Unit,
        onPoundsChanged: (String) -> Unit,
        onOuncesChanged: (String) -> Unit,
        onCreateOrUpdateOrder: () -> Unit,
        onDismissAddCardsDialog: () -> Unit,
        onAddCardsSearchChanged: (String) -> Unit,
        onToggleCardSelection: (String) -> Unit,
        onProceedToPriceConfirmation: () -> Unit,
        onCardPriceChanged: (String, String) -> Unit,
        onConfirmAddCards: () -> Unit,
        onDismissRemoveCardDialog: () -> Unit,
        onConfirmRemoveCard: () -> Unit,
        onDismissUpgradeShippingDialog: () -> Unit,
        onConfirmUpgradeShipping: () -> Unit,
        onDismissSplitOrderDialog: () -> Unit,
        onConfirmSplitOrder: () -> Unit,
        onDismissShippingLabelsDialog: () -> Unit,
        onExportShippingLabels: (List<Order>) -> Unit,
        onDismissTrackingNumberDialog: () -> Unit,
        onTrackingNumberChanged: (String) -> Unit,
        onConfirmTrackingNumber: () -> Unit
) {
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
                onCreateOrderTrackingNumberChanged = onCreateOrderTrackingNumberChanged,
                onDiscountChanged = onDiscountChanged,
                onLengthChanged = onLengthChanged,
                onWidthChanged = onWidthChanged,
                onHeightChanged = onHeightChanged,
                onPoundsChanged = onPoundsChanged,
                onOuncesChanged = onOuncesChanged,
                onConfirm = onCreateOrUpdateOrder
        )
    }

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

    uiState.removeCardDialogState?.let { dialogState ->
        RemoveCardConfirmDialog(
                dialogState = dialogState,
                onDismiss = onDismissRemoveCardDialog,
                onConfirm = onConfirmRemoveCard
        )
    }

    uiState.upgradeShippingDialogState?.let { dialogState ->
        UpgradeShippingConfirmDialog(
                dialogState = dialogState,
                onDismiss = onDismissUpgradeShippingDialog,
                onConfirm = onConfirmUpgradeShipping
        )
    }

    uiState.splitOrderDialogState?.let { dialogState ->
        SplitOrderConfirmDialog(
                dialogState = dialogState,
                onDismiss = onDismissSplitOrderDialog,
                onConfirm = onConfirmSplitOrder
        )
    }

    if (uiState.showShippingLabelsDialog) {
        ShippingLabelsConfirmDialog(
                newOrders = uiState.newStatusOrders,
                onDismiss = onDismissShippingLabelsDialog,
                onExport = onExportShippingLabels
        )
    }

    uiState.trackingNumberDialogState?.let { dialogState ->
        TrackingNumberDialog(
                dialogState = dialogState,
                onDismiss = onDismissTrackingNumberDialog,
                onTrackingNumberChanged = onTrackingNumberChanged,
                onConfirm = onConfirmTrackingNumber
        )
    }
}

@Composable
private fun TrackingNumberDialog(
        dialogState: TrackingNumberDialogState,
        onDismiss: () -> Unit,
        onTrackingNumberChanged: (String) -> Unit,
        onConfirm: () -> Unit
) {
    AlertDialog(
            onDismissRequest = { if (!dialogState.isSaving) onDismiss() },
            title = {
                Text(
                        text = "Tracking Number",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary
                )
            },
            text = {
                OutlinedTextField(
                        value = dialogState.trackingNumber,
                        onValueChange = onTrackingNumberChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter tracking number", color = textTertiary) },
                        singleLine = true,
                        enabled = !dialogState.isSaving,
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary,
                                        focusedBorderColor = accentPrimary,
                                        unfocusedBorderColor = borderInput,
                                        cursorColor = accentPrimary,
                                        focusedContainerColor = bgSecondary,
                                        unfocusedContainerColor = bgSecondary
                                ),
                        shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        enabled = !dialogState.isSaving,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = accentPrimary,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSecondary,
                                        disabledContentColor = textTertiary
                                ),
                        shape = RoundedCornerShape(8.dp)
                ) { Text(if (dialogState.isSaving) "Saving..." else "Confirm") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !dialogState.isSaving) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = bgSurface,
            shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun RemoveCardConfirmDialog(
        dialogState: RemoveCardDialogState,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
) {
    AlertDialog(
            onDismissRequest = { if (!dialogState.isRemoving) onDismiss() },
            title = {
                Text(
                        text = "Remove ${dialogState.cardName} from order?",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary
                )
            },
            text = {
                Text(
                        text =
                                "Removing this card from the order will place it back in your collection and it will no longer have a sold record.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                )
            },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        enabled = !dialogState.isRemoving,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = errorColor,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSecondary,
                                        disabledContentColor = textTertiary
                                ),
                        shape = RoundedCornerShape(8.dp)
                ) { Text(if (dialogState.isRemoving) "Removing..." else "Remove") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !dialogState.isRemoving) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = bgSurface,
            shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun UpgradeShippingConfirmDialog(
        dialogState: UpgradeShippingDialogState,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
) {
    AlertDialog(
            onDismissRequest = { if (!dialogState.isProcessing) onDismiss() },
            title = {
                Text(
                        text = "Upgrade to Bubble Mailer?",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary
                )
            },
            text = {
                Text(
                        text =
                                "This will change shipping from Envelope (\$1.00) to Bubble Mailer (\$5.00). The order has ${dialogState.cardCount} cards which may not ship safely in an envelope.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                )
            },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        enabled = !dialogState.isProcessing,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = warningColor,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSecondary,
                                        disabledContentColor = textTertiary
                                ),
                        shape = RoundedCornerShape(8.dp)
                ) { Text(if (dialogState.isProcessing) "Upgrading..." else "Upgrade") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !dialogState.isProcessing) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = bgSurface,
            shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun SplitOrderConfirmDialog(
        dialogState: SplitOrderDialogState,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
) {
    val cardsPerOrder = ceil(dialogState.cardCount / dialogState.splitCount.toDouble()).toInt()
    AlertDialog(
            onDismissRequest = { if (!dialogState.isProcessing) onDismiss() },
            title = {
                Text(
                        text = "Split into ${dialogState.splitCount} bubble mailers?",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary
                )
            },
            text = {
                Column {
                    Text(
                            text =
                                    "This will distribute ${dialogState.cardCount} cards evenly across ${dialogState.splitCount} orders (~$cardsPerOrder cards each).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                            text = "Each new order will have:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text =
                                    "\u2022 Same purchaser information\n\u2022 Same creation date\n\u2022 Separate order ID\n\u2022 Requires separate shipping label",
                            style = MaterialTheme.typography.bodySmall,
                            color = textTertiary
                    )
                }
            },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        enabled = !dialogState.isProcessing,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = warningColor,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSecondary,
                                        disabledContentColor = textTertiary
                                ),
                        shape = RoundedCornerShape(8.dp)
                ) { Text(if (dialogState.isProcessing) "Splitting..." else "Split Order") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !dialogState.isProcessing) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = bgSurface,
            shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun ShippingLabelsConfirmDialog(
        newOrders: List<Order>,
        onDismiss: () -> Unit,
        onExport: (List<Order>) -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
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
                    Column {
                        Text(
                                text =
                                        "${newOrders.size} shipping label row${if (newOrders.size > 1) "s" else ""} will be created in a CSV file that Pirate Ship will accept as an import.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary
                        )

                        val incompleteOrders =
                                newOrders.filter { order ->
                                    order.streetAddress.isBlank() ||
                                            order.city.isBlank() ||
                                            order.state.isBlank() ||
                                            order.zipcode.isBlank() ||
                                            order.length <= 0 ||
                                            order.width <= 0 ||
                                            (order.pounds <= 0 && order.ounces <= 0)
                                }

                        if (incompleteOrders.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                    text =
                                            "${incompleteOrders.size} order${if (incompleteOrders.size > 1) "s are" else " is"} missing shipping info (address, dimensions, or weight).",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = warningColor,
                                    fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onExport(newOrders) },
                        enabled = newOrders.isNotEmpty(),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = accentPrimary,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSecondary,
                                        disabledContentColor = textTertiary
                                ),
                        shape = RoundedCornerShape(8.dp)
                ) { Text("Export") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = textSecondary) }
            },
            containerColor = bgSurface,
            shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun BoxScope.OrderToast(toast: ToastState?) {
    toast?.let {
        Box(
                modifier =
                        Modifier.align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (it.isError) errorColor else successColor)
                                .padding(16.dp)
        ) {
            Text(
                    text = it.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textOnAccent
            )
        }
    }
}

@Composable
private fun OrderList(
        orders: List<Order>,
        onShowAddCardsDialog: (String) -> Unit,
        onEditOrder: (Order) -> Unit,
        onStatusChanged: (String, String) -> Unit,
        onShowRemoveCardDialog: (String, String, String) -> Unit,
        onUpgradeShipping: (String, Int) -> Unit,
        onSplitOrder: (String, Int) -> Unit,
        onShowTrackingNumberDialog: (String, String?) -> Unit,
        onDeleteOrder: (String) -> Unit,
        onShowToast: (String) -> Unit,
        freeShippingEnabled: Boolean,
        freeShippingThreshold: Long,
        nicePricesEnabled: Boolean
) {
    val listState = rememberLazyListState()

    ScrollableList(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = orders, key = { it.id }) { order ->
            OrderCard(
                    order = order,
                    onShowAddCardsDialog = onShowAddCardsDialog,
                    onEditOrder = onEditOrder,
                    onStatusChanged = onStatusChanged,
                    onShowRemoveCardDialog = onShowRemoveCardDialog,
                    onUpgradeShipping = onUpgradeShipping,
                    onSplitOrder = onSplitOrder,
                    onShowTrackingNumberDialog = onShowTrackingNumberDialog,
                    onDeleteOrder = onDeleteOrder,
                    onShowToast = onShowToast,
                    freeShippingEnabled = freeShippingEnabled,
                    freeShippingThreshold = freeShippingThreshold,
                    nicePricesEnabled = nicePricesEnabled
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
        onShowRemoveCardDialog: (String, String, String) -> Unit,
        onUpgradeShipping: (String, Int) -> Unit,
        onSplitOrder: (String, Int) -> Unit,
        onShowTrackingNumberDialog: (String, String?) -> Unit,
        onDeleteOrder: (String) -> Unit,
        onShowToast: (String) -> Unit,
        freeShippingEnabled: Boolean,
        freeShippingThreshold: Long,
        nicePricesEnabled: Boolean
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    var isEditingCards by remember { mutableStateOf(false) }

    Column(
            modifier =
                    Modifier.fillMaxWidth()
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
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                    text = order.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                        modifier =
                                Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(bgDropdown)
                                        .clickable {
                                            onShowTrackingNumberDialog(
                                                    order.id,
                                                    order.trackingNumber
                                            )
                                        }
                                        .padding(vertical = 8.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit tracking number",
                            tint = textPrimary,
                            modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = order.trackingNumber ?: "Tracking number",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (order.trackingNumber != null) textPrimary else textTertiary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

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
                        backgroundColor =
                                when (order.status.trim()) {
                                    OrderStatus.NEW -> errorColor
                                    OrderStatus.LABEL_CREATED -> warningColor
                                    OrderStatus.SHIPPED -> successColor
                                    else -> bgPrimary
                                },
                        textColor = textOnAccent
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (isEditingCards) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit edit mode",
                            tint = textSecondary,
                            modifier = Modifier.clickable { isEditingCards = false }.padding(4.dp)
                    )
                } else {
                    Box {
                        Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = textSecondary,
                                modifier =
                                        Modifier.clickable { showOverflowMenu = true }.padding(4.dp)
                        )

                        DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false },
                                modifier = Modifier.background(bgSecondary)
                        ) {
                            DropdownMenuItem(
                                    text = { Text("Add cards", color = textPrimary) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onShowAddCardsDialog(order.id)
                                    },
                                    leadingIcon = {
                                        Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                tint = accentPrimary
                                        )
                                    }
                            )
                            DropdownMenuItem(
                                    text = { Text("Edit order", color = textPrimary) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onEditOrder(order)
                                    },
                                    leadingIcon = {
                                        Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = accentPrimary
                                        )
                                    }
                            )
                            DropdownMenuItem(
                                    text = { Text("Copy details", color = textPrimary) },
                                    onClick = {
                                        showOverflowMenu = false
                                        val text =
                                                buildOrderCopyText(
                                                        order,
                                                        nicePricesEnabled,
                                                        freeShippingEnabled,
                                                        freeShippingThreshold
                                                )
                                        copyToClipboard(text)
                                        onShowToast("Order copied to clipboard")
                                    },
                                    leadingIcon = {
                                        Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = accentPrimary
                                        )
                                    }
                            )
                            if (order.cards.isNotEmpty()) {
                                DropdownMenuItem(
                                        text = { Text("Edit cards", color = textPrimary) },
                                        onClick = {
                                            showOverflowMenu = false
                                            isEditingCards = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = accentPrimary
                                            )
                                        }
                                )
                            }
                            DropdownMenuItem(
                                    text = { Text("Delete order", color = errorColor) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onDeleteOrder(order.id)
                                    },
                                    leadingIcon = {
                                        Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = errorColor
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        if (order.streetAddress.isNotBlank()) {
            Text(
                    text = order.streetAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
            )
        }
        val cityStateZip = buildString {
            if (order.city.isNotBlank()) append(order.city)
            if (order.state.isNotBlank()) {
                if (isNotEmpty()) append(", ")
                append(order.state)
            }
            if (order.zipcode.isNotBlank()) {
                if (isNotEmpty()) append(" ")
                append(order.zipcode)
            }
        }
        if (cityStateZip.isNotBlank()) {
            Text(
                    text = cityStateZip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
                text = formatOrderDate(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = textTertiary
        )

        if (order.shippingType != null || order.cards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            val cardsTotal =
                    if (nicePricesEnabled) {
                        order.cards.sumOf { card ->
                            val price = card.priceSold ?: 0L
                            if (price > 0) ceil(price / 100.0).toLong() * 100 else 0L
                        }
                    } else {
                        order.cards.sumOf { it.priceSold ?: 0L }
                    }

            val qualifiesForFreeShipping =
                    freeShippingEnabled &&
                            freeShippingThreshold > 0 &&
                            cardsTotal >= freeShippingThreshold

            if (order.shippingType != null) {
                Text(
                        text =
                                "Shipping: ${order.shippingType}    ${formatPrice(order.shippingCost)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (qualifiesForFreeShipping) textTertiary else textPrimary,
                        textDecoration =
                                if (qualifiesForFreeShipping) {
                                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                                } else null
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

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
                                modifier =
                                        Modifier.clickable {
                                                    onShowRemoveCardDialog(
                                                            order.id,
                                                            card.id,
                                                            card.name
                                                    )
                                                }
                                                .padding(end = 8.dp)
                        )
                    }
                    val displayPrice =
                            if (nicePricesEnabled) {
                                val price = card.priceSold ?: 0L
                                if (price > 0) ceil(price / 100.0).toLong() * 100 else 0L
                            } else {
                                card.priceSold ?: 0L
                            }
                    Text(
                            text = "${card.name}    ${formatPrice(displayPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            val discountAmount =
                    if (order.discount > 0) {
                        cardsTotal * order.discount / 100
                    } else 0L

            if (order.discount > 0) {
                Text(
                        text = "Discount (${order.discount}%)    -${formatPrice(discountAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            val effectiveShipping = if (qualifiesForFreeShipping) 0L else order.shippingCost
            val total = cardsTotal - discountAmount + effectiveShipping
            Text(
                    text = "Total ${formatPrice(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = successColor
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            val total = order.shippingCost
            Text(
                    text = "Total ${formatPrice(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = successColor
            )
        }

        OrderWarningBanner(
                order = order,
                onUpgradeShipping = onUpgradeShipping,
                onSplitOrder = onSplitOrder
        )
    }
}

@Composable
private fun OrderWarningBanner(
        order: Order,
        onUpgradeShipping: (String, Int) -> Unit,
        onSplitOrder: (String, Int) -> Unit
) {
    val cardCount = order.cards.size
    val showWarnings = order.status == OrderStatus.NEW || order.status == OrderStatus.LABEL_CREATED
    if (!showWarnings || cardCount == 0) return

    val isEnvelope = order.shippingType?.equals("Envelope", ignoreCase = true) == true
    val isBubbleMailer = order.shippingType?.equals("Bubble mailer", ignoreCase = true) == true

    if (isEnvelope && cardCount >= 2) {
        Spacer(modifier = Modifier.height(8.dp))
        WarningBanner(
                message =
                        "2 or more toploaders may require extra postage and incur a non machineable upcharge.",
                buttonText = "Upgrade to bubble mailer",
                onButtonClick = { onUpgradeShipping(order.id, cardCount) }
        )
    }

    if (isBubbleMailer && cardCount >= 15) {
        val splitCount = ceil(cardCount / 15.0).toInt()
        Spacer(modifier = Modifier.height(8.dp))
        WarningBanner(
                message = "Each bubble mailer can hold up to around 15 toploaders",
                buttonText = "Split into $splitCount orders",
                onButtonClick = { onSplitOrder(order.id, cardCount) }
        )
    }
}

@Composable
private fun WarningBanner(
        message: String,
        buttonText: String? = null,
        onButtonClick: (() -> Unit)? = null
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(warningColor)
                            .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = textOnAccent,
                modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (buttonText != null && onButtonClick != null) {
            TextButton(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = textOnAccent),
                    contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                        text = buttonText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = textOnAccent)
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
                modifier =
                        Modifier.fillMaxWidth()
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
                        imageVector =
                                if (selectedOption.isAscending()) Icons.Default.ArrowUpward
                                else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = accentPrimary
                )
            }
        }

        DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier =
                        Modifier.background(bgDropdown)
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
                                        color =
                                                if (option == selectedOption) accentPrimary
                                                else textPrimary,
                                        style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                        imageVector =
                                                if (option.isAscending()) Icons.Default.ArrowUpward
                                                else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint =
                                                if (option == selectedOption) accentPrimary
                                                else textSecondary,
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

private fun OrderSortOption.displayName(): String =
        when (this) {
            OrderSortOption.DATE_DESC -> "Date"
            OrderSortOption.DATE_ASC -> "Date"
            OrderSortOption.TOTAL_DESC -> "Order total"
            OrderSortOption.TOTAL_ASC -> "Order total"
        }

private fun OrderSortOption.isAscending(): Boolean =
        when (this) {
            OrderSortOption.DATE_DESC -> false // Newest first shows down arrow
            OrderSortOption.DATE_ASC -> true // Oldest first shows up arrow
            OrderSortOption.TOTAL_DESC -> false // Most expensive first shows down arrow
            OrderSortOption.TOTAL_ASC -> true // Cheapest first shows up arrow
        }

private fun formatPrice(priceInPennies: Long): String {
    val dollars = priceInPennies / 100.0
    return "$${String.format("%.2f", dollars)}"
}

private fun buildOrderCopyText(
        order: Order,
        nicePricesEnabled: Boolean,
        freeShippingEnabled: Boolean,
        freeShippingThreshold: Long
): String {
    val cardsTotal =
            if (nicePricesEnabled) {
                order.cards.sumOf { card ->
                    val price = card.priceSold ?: 0L
                    if (price > 0) kotlin.math.ceil(price / 100.0).toLong() * 100 else 0L
                }
            } else {
                order.cards.sumOf { it.priceSold ?: 0L }
            }
    val qualifiesForFreeShipping =
            freeShippingEnabled && freeShippingThreshold > 0 && cardsTotal >= freeShippingThreshold
    val discountAmount = if (order.discount > 0) cardsTotal * order.discount / 100 else 0L
    val effectiveShipping = if (qualifiesForFreeShipping) 0L else order.shippingCost
    val total = cardsTotal - discountAmount + effectiveShipping

    return buildString {
                appendLine(order.name)
                append("\n")
                if (order.streetAddress.isNotBlank()) appendLine(order.streetAddress)
                val cityStateZip = buildString {
                    if (order.city.isNotBlank()) append(order.city)
                    if (order.state.isNotBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(order.state)
                    }
                    if (order.zipcode.isNotBlank()) {
                        if (isNotEmpty()) append(" ")
                        append(order.zipcode)
                    }
                }
                if (cityStateZip.isNotBlank()) appendLine(cityStateZip)
                append("\n")
                appendLine(formatPrice(total))
                append("\n")
                order.cards.forEach { card ->
                    appendLine(card.name)
                    append("\n")
                }
            }
            .trimEnd()
}

private fun copyToClipboard(text: String) {
    try {
        val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
        val selection = java.awt.datatransfer.StringSelection(text)
        clipboard.setContents(selection, selection)
    } catch (e: Exception) {
        e.printStackTrace()
    }
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
        onCreateOrderTrackingNumberChanged: (String) -> Unit,
        onDiscountChanged: (String) -> Unit,
        onLengthChanged: (String) -> Unit,
        onWidthChanged: (String) -> Unit,
        onHeightChanged: (String) -> Unit,
        onPoundsChanged: (String) -> Unit,
        onOuncesChanged: (String) -> Unit,
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
                    DialogFormTextField(
                            value = formState.name,
                            onValueChange = onNameChanged,
                            label = "Name",
                            placeholder = "Customer name",
                            borderColor = if (formState.name.isBlank()) errorColor else null
                    )

                    DialogFormTextField(
                            value = formState.streetAddress,
                            onValueChange = onStreetAddressChanged,
                            label = "Street Address",
                            placeholder = "123 Main St",
                            secondaryText = "All fields marked in yellow can be updated later",
                            secondaryTextColor = warningColor,
                            borderColor = if (formState.streetAddress.isBlank()) warningColor else null
                    )

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DialogFormTextField(
                                value = formState.city,
                                onValueChange = onCityChanged,
                                label = "City",
                                placeholder = "City",
                                modifier = Modifier.weight(1f),
                                borderColor = if (formState.city.isBlank()) warningColor else null
                        )

                        DialogFormTextField(
                                value = formState.state,
                                onValueChange = onStateChanged,
                                label = "State",
                                placeholder = "CA",
                                modifier = Modifier.width(80.dp),
                                borderColor = if (formState.state.isBlank()) warningColor else null
                        )

                        DialogFormTextField(
                                value = formState.zipcode,
                                onValueChange = onZipcodeChanged,
                                label = "Zip",
                                placeholder = "12345",
                                modifier = Modifier.width(100.dp),
                                borderColor = if (formState.zipcode.isBlank()) warningColor else null
                        )
                    }

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ShippingTypeDropdown(
                                selectedType = formState.shippingType,
                                onTypeSelected = onShippingTypeChanged,
                                modifier = Modifier.weight(1f)
                        )

                        DialogFormTextField(
                                value = formState.shippingPrice,
                                onValueChange = onShippingPriceChanged,
                                label = "Shipping Price",
                                placeholder = "0.00",
                                prefix = "$",
                                modifier = Modifier.weight(1f)
                        )

                        DialogFormTextField(
                                value = formState.discount,
                                onValueChange = onDiscountChanged,
                                label = "Discount",
                                placeholder = "0",
                                suffix = "%",
                                modifier = Modifier.width(80.dp)
                        )
                    }

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DialogFormTextField(
                                value = formState.length,
                                onValueChange = onLengthChanged,
                                label = "Length",
                                placeholder = "0",
                                suffix = "in",
                                modifier = Modifier.weight(1f)
                        )

                        DialogFormTextField(
                                value = formState.width,
                                onValueChange = onWidthChanged,
                                label = "Width",
                                placeholder = "0",
                                suffix = "in",
                                modifier = Modifier.weight(1f)
                        )

                        val showHeight =
                                formState.shippingType == "Box" || formState.shippingType == "Other"
                        if (showHeight) {
                            DialogFormTextField(
                                    value = formState.height,
                                    onValueChange = onHeightChanged,
                                    label = "Height",
                                    placeholder = "0",
                                    suffix = "in",
                                    modifier = Modifier.weight(1f)
                            )
                        }

                        val weightEmpty = (formState.pounds.toIntOrNull() ?: 0) == 0 &&
                                (formState.ounces.toIntOrNull() ?: 0) == 0

                        DialogFormTextField(
                                value = formState.pounds,
                                onValueChange = onPoundsChanged,
                                label = "Pounds",
                                placeholder = "0",
                                suffix = "lbs",
                                modifier = Modifier.weight(1f),
                                borderColor = if (weightEmpty) warningColor else null
                        )

                        DialogFormTextField(
                                value = formState.ounces,
                                onValueChange = onOuncesChanged,
                                label = "Ounces",
                                placeholder = "0",
                                suffix = "oz",
                                modifier = Modifier.weight(1f),
                                borderColor = if (weightEmpty) warningColor else null
                        )
                    }

                    DialogFormTextField(
                            value = formState.trackingNumber,
                            onValueChange = onCreateOrderTrackingNumberChanged,
                            label = "Tracking Number",
                            placeholder = "1Z12345678901234567890",
                            secondaryText = "Optional, you can add this later",
                            borderColor = if (formState.trackingNumber.isBlank()) warningColor else null
                    )
                }
            },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        enabled = formState.isValid(),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = accentPrimary,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSecondary,
                                        disabledContentColor = textTertiary
                                ),
                        shape = RoundedCornerShape(8.dp)
                ) { Text(if (isEditMode) "Save Changes" else "Create Order") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !formState.isSaving) {
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
        prefix: String? = null,
        suffix: String? = null,
        secondaryText: String? = null,
        secondaryTextColor: androidx.compose.ui.graphics.Color = textTertiary,
        borderColor: androidx.compose.ui.graphics.Color? = null
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = textPrimary)
            if (secondaryText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                        text = secondaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = placeholder, color = textTertiary) },
                prefix =
                        if (prefix != null) {
                            { Text(prefix, color = textPrimary) }
                        } else null,
                suffix =
                        if (suffix != null) {
                            { Text(suffix, color = textPrimary) }
                        } else null,
                singleLine = true,
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedBorderColor = borderColor ?: Color.Transparent,
                                unfocusedBorderColor = borderColor ?: Color.Transparent,
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
                        text =
                                if (dialogState.step == AddCardsStep.SELECT_CARDS)
                                        "Add Cards to Order"
                                else "Confirm Prices",
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
                                selectedCards =
                                        availableCards.filter {
                                            it.id in dialogState.selectedCardIds
                                        },
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
                        enabled =
                                when (dialogState.step) {
                                    AddCardsStep.SELECT_CARDS ->
                                            dialogState.selectedCardIds.isNotEmpty() &&
                                                    !dialogState.isSaving
                                    AddCardsStep.CONFIRM_PRICES -> {
                                        val selectedCards =
                                                availableCards.filter {
                                                    it.id in dialogState.selectedCardIds
                                                }
                                        selectedCards.all { card ->
                                            val price = dialogState.cardPrices[card.id]
                                            price != null && price.isNotBlank()
                                        } && !dialogState.isSaving
                                    }
                                },
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = accentPrimary,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSecondary,
                                        disabledContentColor = textTertiary
                                ),
                        shape = RoundedCornerShape(8.dp)
                ) { Text(if (dialogState.step == AddCardsStep.SELECT_CARDS) "Next" else "Confirm") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !dialogState.isSaving) {
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

    Column(modifier = Modifier.fillMaxWidth().height(500.dp)) {
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
                colors =
                        OutlinedTextFieldDefaults.colors(
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

        val filteredCards =
                if (searchQuery.isBlank()) {
                    availableCards
                } else {
                    availableCards.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }

        ScrollableList(
                modifier = Modifier.fillMaxWidth().weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                scrollbarPadding = 16.dp
        ) {
            items(items = filteredCards, key = { it.id }) { card ->
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                                if (card.id in selectedCardIds)
                                                        accentPrimary.copy(alpha = 0.1f)
                                                else bgPrimary
                                        )
                                        .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Checkbox(
                            checked = card.id in selectedCardIds,
                            onCheckedChange = { onToggleCardSelection(card.id) },
                            colors =
                                    androidx.compose.material3.CheckboxDefaults.colors(
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

    Column(modifier = Modifier.fillMaxWidth().height(500.dp)) {
        ScrollableList(
                modifier = Modifier.fillMaxWidth().weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                scrollbarPadding = 16.dp
        ) {
            items(items = selectedCards, key = { it.id }) { card ->
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
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
                                val filtered = value.filter { it.isDigit() || it == '.' }
                                if (filtered.count { it == '.' } <= 1) {
                                    onCardPriceChanged(card.id, filtered)
                                }
                            },
                            modifier = Modifier.width(120.dp),
                            placeholder = { Text("0.00", color = textTertiary) },
                            prefix = { Text("$", color = textPrimary) },
                            singleLine = true,
                            colors =
                                    OutlinedTextFieldDefaults.colors(
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
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }

    val monthNames =
            arrayOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
            )
    val month = monthNames[calendar.get(java.util.Calendar.MONTH)]
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val year = calendar.get(java.util.Calendar.YEAR)
    val hour24 = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)

    val amPm = if (hour24 < 12) "am" else "pm"
    val displayHour =
            when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }

    val minuteStr = minute.toString().padStart(2, '0')

    return "$month $day, $year $displayHour:${minuteStr}$amPm"
}
