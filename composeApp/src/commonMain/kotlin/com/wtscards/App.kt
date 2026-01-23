package com.wtscards

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wtscards.ui.components.ImportConflictDialog
import com.wtscards.ui.navigation.BottomNavBar
import com.wtscards.ui.navigation.NavigationItem
import com.wtscards.ui.screens.addcard.AddCardScreen
import com.wtscards.ui.screens.addcard.AddCardViewModel
import com.wtscards.ui.screens.collection.CollectionScreen
import com.wtscards.ui.screens.collection.CollectionViewModel
import com.wtscards.ui.screens.import.ImportScreen
import com.wtscards.ui.screens.import.ImportState
import com.wtscards.ui.screens.import.ImportViewModel
import com.wtscards.data.model.Order
import com.wtscards.ui.screens.orders.OrderScreen
import com.wtscards.ui.screens.orders.OrderViewModel
import com.wtscards.ui.theme.WTSCardsTheme
import com.wtscards.ui.theme.bgPrimary

@Composable
fun App(
    dependencies: AppDependencies,
    importViewModel: ImportViewModel,
    onBrowseFiles: () -> Unit,
    onExportShippingLabels: (List<Order>, OrderViewModel) -> Unit
) {
    WTSCardsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bgPrimary
        ) {
            MainScreen(
                dependencies = dependencies,
                importViewModel = importViewModel,
                onBrowseFiles = onBrowseFiles,
                onExportShippingLabels = onExportShippingLabels
            )
        }
    }
}

@Composable
fun MainScreen(
    dependencies: AppDependencies,
    importViewModel: ImportViewModel,
    onBrowseFiles: () -> Unit,
    onExportShippingLabels: (List<Order>, OrderViewModel) -> Unit
) {
    var currentRoute by remember { mutableStateOf(NavigationItem.Collection.route) }

    val collectionViewModel = remember {
        CollectionViewModel(dependencies.cardUseCase, dependencies.coroutineScope)
    }

    val orderViewModel = remember {
        OrderViewModel(dependencies.orderUseCase, dependencies.cardUseCase, dependencies.coroutineScope)
    }

    val addCardViewModel = remember {
        AddCardViewModel(dependencies.cardUseCase, dependencies.coroutineScope)
    }

    Scaffold(
        containerColor = bgPrimary,
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { item -> currentRoute = item.route }
            )
        }
    ) { paddingValues ->
        when (currentRoute) {
            NavigationItem.Collection.route -> {
                CollectionScreen(
                    uiState = collectionViewModel.uiState,
                    onSearchQueryChanged = collectionViewModel::onSearchQueryChanged,
                    onSortOptionChanged = collectionViewModel::onSortOptionChanged,
                    onRefresh = collectionViewModel::onRefresh,
                    onToggleEditMode = collectionViewModel::toggleEditMode,
                    onToggleCardSelection = collectionViewModel::toggleCardSelection,
                    onDeleteClick = collectionViewModel::showDeleteConfirmation,
                    onDeleteConfirm = collectionViewModel::confirmDelete,
                    onDeleteCancel = collectionViewModel::dismissDeleteConfirmation,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            NavigationItem.Orders.route -> {
                OrderScreen(
                    uiState = orderViewModel.uiState,
                    onToggleFabExpanded = orderViewModel::onToggleFabExpanded,
                    onCollapseFab = orderViewModel::onCollapseFab,
                    onShowCreateDialog = orderViewModel::onShowCreateDialog,
                    onDismissCreateDialog = orderViewModel::onDismissCreateDialog,
                    onShowShippingLabelsDialog = orderViewModel::onShowShippingLabelsDialog,
                    onDismissShippingLabelsDialog = orderViewModel::onDismissShippingLabelsDialog,
                    onExportShippingLabels = { orders -> onExportShippingLabels(orders, orderViewModel) },
                    onSearchQueryChanged = orderViewModel::onSearchQueryChanged,
                    onStatusFilterToggled = orderViewModel::onStatusFilterToggled,
                    onSortOptionChanged = orderViewModel::onSortOptionChanged,
                    onNameChanged = orderViewModel::onNameChanged,
                    onStreetAddressChanged = orderViewModel::onStreetAddressChanged,
                    onCityChanged = orderViewModel::onCityChanged,
                    onStateChanged = orderViewModel::onStateChanged,
                    onZipcodeChanged = orderViewModel::onZipcodeChanged,
                    onShippingTypeChanged = orderViewModel::onShippingTypeChanged,
                    onShippingPriceChanged = orderViewModel::onShippingPriceChanged,
                    onCreateOrUpdateOrder = orderViewModel::onCreateOrUpdateOrder,
                    onEditOrder = orderViewModel::onEditOrder,
                    onStatusChanged = orderViewModel::onStatusChanged,
                    onShowAddCardsDialog = orderViewModel::onShowAddCardsDialog,
                    onDismissAddCardsDialog = orderViewModel::onDismissAddCardsDialog,
                    onAddCardsSearchChanged = orderViewModel::onAddCardsSearchChanged,
                    onToggleCardSelection = orderViewModel::onToggleCardSelection,
                    onProceedToPriceConfirmation = orderViewModel::onProceedToPriceConfirmation,
                    onCardPriceChanged = orderViewModel::onCardPriceChanged,
                    onConfirmAddCards = orderViewModel::onConfirmAddCards,
                    onShowRemoveCardDialog = orderViewModel::onShowRemoveCardDialog,
                    onDismissRemoveCardDialog = orderViewModel::onDismissRemoveCardDialog,
                    onConfirmRemoveCard = orderViewModel::onConfirmRemoveCard,
                    onShowUpgradeShippingDialog = orderViewModel::onShowUpgradeShippingDialog,
                    onDismissUpgradeShippingDialog = orderViewModel::onDismissUpgradeShippingDialog,
                    onConfirmUpgradeShipping = orderViewModel::onConfirmUpgradeShipping,
                    onShowSplitOrderDialog = orderViewModel::onShowSplitOrderDialog,
                    onDismissSplitOrderDialog = orderViewModel::onDismissSplitOrderDialog,
                    onConfirmSplitOrder = orderViewModel::onConfirmSplitOrder,
                    onShowTrackingNumberDialog = orderViewModel::onShowTrackingNumberDialog,
                    onDismissTrackingNumberDialog = orderViewModel::onDismissTrackingNumberDialog,
                    onTrackingNumberChanged = orderViewModel::onTrackingNumberChanged,
                    onConfirmTrackingNumber = orderViewModel::onConfirmTrackingNumber,
                    onClearToast = orderViewModel::clearToast,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            NavigationItem.AddCard.route -> {
                AddCardScreen(
                    uiState = addCardViewModel.uiState,
                    onNameChanged = addCardViewModel::onNameChanged,
                    onCardNumberChanged = addCardViewModel::onCardNumberChanged,
                    onSetNameChanged = addCardViewModel::onSetNameChanged,
                    onParallelNameChanged = addCardViewModel::onParallelNameChanged,
                    onGradeOptionChanged = addCardViewModel::onGradeOptionChanged,
                    onQuantityChanged = addCardViewModel::onQuantityChanged,
                    onSave = addCardViewModel::onSave,
                    canSave = addCardViewModel.canSave(),
                    onClearToast = addCardViewModel::clearToast,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            NavigationItem.Import.route -> {
                ImportScreen(
                    uiState = importViewModel.uiState,
                    onBrowseFiles = onBrowseFiles,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        // Show conflict dialog when needed
        val importState = importViewModel.uiState.importState
        if (importState is ImportState.ConflictDetected) {
            ImportConflictDialog(
                collisionCount = importState.collisions.size,
                totalCount = importState.cards.size,
                onStrategySelected = { strategy ->
                    importViewModel.onImportStrategySelected(strategy)
                },
                onDismiss = {
                    importViewModel.onConflictDialogDismissed()
                }
            )
        }
    }
}
