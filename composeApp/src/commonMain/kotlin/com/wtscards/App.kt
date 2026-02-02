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
import com.wtscards.data.model.Order
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
import com.wtscards.ui.screens.listings.ListingScreen
import com.wtscards.ui.screens.listings.ListingViewModel
import com.wtscards.ui.screens.orders.OrderScreen
import com.wtscards.ui.screens.orders.OrderViewModel
import com.wtscards.ui.screens.settings.SettingsScreen
import com.wtscards.ui.screens.settings.SettingsViewModel
import com.wtscards.ui.theme.WTSCardsTheme
import com.wtscards.ui.theme.bgPrimary

@Composable
fun App(
        dependencies: AppDependencies,
        importViewModel: ImportViewModel,
        onBrowseFiles: () -> Unit,
        onExportShippingLabels: (List<Order>, OrderViewModel) -> Unit,
        onRestoreComplete: () -> Unit
) {
    WTSCardsTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = bgPrimary) {
            MainScreen(
                    dependencies = dependencies,
                    importViewModel = importViewModel,
                    onBrowseFiles = onBrowseFiles,
                    onExportShippingLabels = onExportShippingLabels,
                    onRestoreComplete = onRestoreComplete
            )
        }
    }
}

@Composable
fun MainScreen(
        dependencies: AppDependencies,
        importViewModel: ImportViewModel,
        onBrowseFiles: () -> Unit,
        onExportShippingLabels: (List<Order>, OrderViewModel) -> Unit,
        onRestoreComplete: () -> Unit
) {
    var currentRoute by remember { mutableStateOf(NavigationItem.Collection.route) }

    val collectionViewModel = remember {
        CollectionViewModel(
                dependencies.cardUseCase,
                dependencies.autocompleteUseCase,
                dependencies.coroutineScope
        )
    }

    val orderViewModel = remember {
        OrderViewModel(
                dependencies.orderUseCase,
                dependencies.cardUseCase,
                dependencies.settingUseCase,
                dependencies.coroutineScope
        )
    }

    val listingViewModel = remember {
        ListingViewModel(
                dependencies.listingUseCase,
                dependencies.cardUseCase,
                dependencies.settingUseCase,
                dependencies.orderUseCase,
                dependencies.coroutineScope
        )
    }

    val settingsViewModel = remember {
        SettingsViewModel(
                dependencies.settingUseCase,
                dependencies.backupUseCase,
                onRestoreComplete,
                dependencies.coroutineScope
        )
    }

    val addCardViewModel = remember {
        AddCardViewModel(
                dependencies.cardUseCase,
                dependencies.autocompleteUseCase,
                dependencies.coroutineScope
        )
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
                        onEditCard = collectionViewModel::onEditCard,
                        onDismissEditCardDialog = collectionViewModel::onDismissEditCardDialog,
                        onEditNameChanged = collectionViewModel::onEditNameChanged,
                        onEditCardNumberChanged = collectionViewModel::onEditCardNumberChanged,
                        onEditSetNameChanged = collectionViewModel::onEditSetNameChanged,
                        onEditParallelNameChanged = collectionViewModel::onEditParallelNameChanged,
                        onEditGradeOptionChanged = collectionViewModel::onEditGradeOptionChanged,
                        onEditPriceChanged = collectionViewModel::onEditPriceChanged,
                        onSaveEditCard = collectionViewModel::onSaveEditCard,
                        canSaveEditCard = collectionViewModel.canSaveEditCard(),
                        onClearToast = collectionViewModel::clearToast,
                        modifier = Modifier.padding(paddingValues)
                )
            }
            NavigationItem.Listings.route -> {
                ListingScreen(
                        uiState = listingViewModel.uiState,
                        onSearchQueryChanged = listingViewModel::onSearchQueryChanged,
                        onShowCreateDialog = listingViewModel::onShowCreateDialog,
                        onDismissCreateDialog = listingViewModel::onDismissCreateDialog,
                        onTitleChanged = listingViewModel::onTitleChanged,
                        onDiscountChanged = listingViewModel::onDiscountChanged,
                        onNicePricesChanged = listingViewModel::onNicePricesChanged,
                        onCreateListing = listingViewModel::onCreateListing,
                        onEditListing = listingViewModel::onEditListing,
                        onShowAddCardsDialog = listingViewModel::onShowAddCardsDialog,
                        onDismissAddCardsDialog = listingViewModel::onDismissAddCardsDialog,
                        onAddCardsSearchChanged = listingViewModel::onAddCardsSearchChanged,
                        onToggleCardSelection = listingViewModel::onToggleCardSelection,
                        onConfirmAddCards = listingViewModel::onConfirmAddCards,
                        onShowRemoveCardDialog = listingViewModel::onShowRemoveCardDialog,
                        onDismissRemoveCardDialog = listingViewModel::onDismissRemoveCardDialog,
                        onConfirmRemoveCard = listingViewModel::onConfirmRemoveCard,
                        onShowDeleteListingDialog = listingViewModel::onShowDeleteListingDialog,
                        onDismissDeleteListingDialog =
                                listingViewModel::onDismissDeleteListingDialog,
                        onConfirmDeleteListing = listingViewModel::onConfirmDeleteListing,
                        onShowImageUrlDialog = listingViewModel::onShowImageUrlDialog,
                        onDismissImageUrlDialog = listingViewModel::onDismissImageUrlDialog,
                        onImageUrlChanged = listingViewModel::onImageUrlChanged,
                        onConfirmImageUrl = listingViewModel::onConfirmImageUrl,
                        onShowCopyToast = listingViewModel::showCopyToast,
                        onClearToast = listingViewModel::clearToast,
                        onClearFocusSearchFlag = listingViewModel::onClearFocusSearchFlag,
                        onShowCreateOrderFromListing = listingViewModel::onShowCreateOrderFromListing,
                        onDismissCreateOrderFromListing = listingViewModel::onDismissCreateOrderFromListing,
                        onCreateOrderSearchChanged = listingViewModel::onCreateOrderSearchChanged,
                        onToggleCreateOrderCardSelection = listingViewModel::onToggleCreateOrderCardSelection,
                        onProceedToCreateOrderPriceConfirmation = listingViewModel::onProceedToCreateOrderPriceConfirmation,
                        onCreateOrderCardPriceChanged = listingViewModel::onCreateOrderCardPriceChanged,
                        onProceedToCreateOrderForm = listingViewModel::onProceedToCreateOrderForm,
                        onCreateOrderNameChanged = listingViewModel::onCreateOrderNameChanged,
                        onCreateOrderStreetAddressChanged = listingViewModel::onCreateOrderStreetAddressChanged,
                        onCreateOrderCityChanged = listingViewModel::onCreateOrderCityChanged,
                        onCreateOrderStateChanged = listingViewModel::onCreateOrderStateChanged,
                        onCreateOrderZipcodeChanged = listingViewModel::onCreateOrderZipcodeChanged,
                        onCreateOrderShippingTypeChanged = listingViewModel::onCreateOrderShippingTypeChanged,
                        onCreateOrderShippingPriceChanged = listingViewModel::onCreateOrderShippingPriceChanged,
                        onCreateOrderTrackingNumberChanged = listingViewModel::onCreateOrderTrackingNumberChanged,
                        onCreateOrderDiscountChanged = listingViewModel::onCreateOrderDiscountChanged,
                        onCreateOrderLengthChanged = listingViewModel::onCreateOrderLengthChanged,
                        onCreateOrderWidthChanged = listingViewModel::onCreateOrderWidthChanged,
                        onCreateOrderHeightChanged = listingViewModel::onCreateOrderHeightChanged,
                        onCreateOrderPoundsChanged = listingViewModel::onCreateOrderPoundsChanged,
                        onCreateOrderOuncesChanged = listingViewModel::onCreateOrderOuncesChanged,
                        onConfirmCreateOrderFromListing = listingViewModel::onConfirmCreateOrderFromListing,
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
                        onDismissShippingLabelsDialog =
                                orderViewModel::onDismissShippingLabelsDialog,
                        onExportShippingLabels = { orders ->
                            onExportShippingLabels(orders, orderViewModel)
                        },
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
                        onCreateOrderTrackingNumberChanged =
                                orderViewModel::onCreateOrderTrackingNumberChanged,
                        onDiscountChanged = orderViewModel::onDiscountChanged,
                        onLengthChanged = orderViewModel::onLengthChanged,
                        onWidthChanged = orderViewModel::onWidthChanged,
                        onHeightChanged = orderViewModel::onHeightChanged,
                        onPoundsChanged = orderViewModel::onPoundsChanged,
                        onOuncesChanged = orderViewModel::onOuncesChanged,
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
                        onDismissUpgradeShippingDialog =
                                orderViewModel::onDismissUpgradeShippingDialog,
                        onConfirmUpgradeShipping = orderViewModel::onConfirmUpgradeShipping,
                        onShowSplitOrderDialog = orderViewModel::onShowSplitOrderDialog,
                        onDismissSplitOrderDialog = orderViewModel::onDismissSplitOrderDialog,
                        onConfirmSplitOrder = orderViewModel::onConfirmSplitOrder,
                        onShowTrackingNumberDialog = orderViewModel::onShowTrackingNumberDialog,
                        onDismissTrackingNumberDialog =
                                orderViewModel::onDismissTrackingNumberDialog,
                        onTrackingNumberChanged = orderViewModel::onTrackingNumberChanged,
                        onConfirmTrackingNumber = orderViewModel::onConfirmTrackingNumber,
                        onShowTotalOverrideDialog = orderViewModel::onShowTotalOverrideDialog,
                        onDismissTotalOverrideDialog = orderViewModel::onDismissTotalOverrideDialog,
                        onTotalOverrideChanged = orderViewModel::onTotalOverrideChanged,
                        onConfirmTotalOverride = orderViewModel::onConfirmTotalOverride,
                        onDeleteOrder = orderViewModel::onDeleteOrder,
                        onShowToast = orderViewModel::showToast,
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
                        onPriceChanged = addCardViewModel::onPriceChanged,
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
            NavigationItem.Settings.route -> {
                SettingsScreen(
                        uiState = settingsViewModel.uiState,
                        onPreBodyTextChanged = settingsViewModel::onPreBodyTextChanged,
                        onPostBodyTextChanged = settingsViewModel::onPostBodyTextChanged,
                        onListingNicePricesEnabledChanged =
                                settingsViewModel::onListingNicePricesEnabledChanged,
                        onListingDefaultDiscountChanged =
                                settingsViewModel::onListingDefaultDiscountChanged,
                        onFreeShippingEnabledChanged =
                                settingsViewModel::onFreeShippingEnabledChanged,
                        onFreeShippingThresholdChanged =
                                settingsViewModel::onFreeShippingThresholdChanged,
                        onNicePricesEnabledChanged = settingsViewModel::onNicePricesEnabledChanged,
                        onDefaultDiscountChanged = settingsViewModel::onDefaultDiscountChanged,
                        onEnvelopeCostChanged = settingsViewModel::onEnvelopeCostChanged,
                        onEnvelopeLengthChanged = settingsViewModel::onEnvelopeLengthChanged,
                        onEnvelopeWidthChanged = settingsViewModel::onEnvelopeWidthChanged,
                        onBubbleMailerCostChanged = settingsViewModel::onBubbleMailerCostChanged,
                        onBubbleMailerLengthChanged =
                                settingsViewModel::onBubbleMailerLengthChanged,
                        onBubbleMailerWidthChanged = settingsViewModel::onBubbleMailerWidthChanged,
                        onBoxCostChanged = settingsViewModel::onBoxCostChanged,
                        onBoxLengthChanged = settingsViewModel::onBoxLengthChanged,
                        onBoxWidthChanged = settingsViewModel::onBoxWidthChanged,
                        onBoxHeightChanged = settingsViewModel::onBoxHeightChanged,
                        onSave = settingsViewModel::onSave,
                        onClearToast = settingsViewModel::clearToast,
                        onBackupNow = settingsViewModel::onBackupNow,
                        onShowRestoreDialog = settingsViewModel::onShowRestoreDialog,
                        onDismissRestoreDialog = settingsViewModel::onDismissRestoreDialog,
                        onSelectBackupToRestore = settingsViewModel::onSelectBackupToRestore,
                        onConfirmRestore = settingsViewModel::onConfirmRestore,
                        onDismissRestoreConfirmation =
                                settingsViewModel::onDismissRestoreConfirmation,
                        modifier = Modifier.padding(paddingValues)
                )
            }
        }

        val importState = importViewModel.uiState.importState
        if (importState is ImportState.ConflictDetected) {
            ImportConflictDialog(
                    collisionCount = importState.collisions.size,
                    totalCount = importState.cards.size,
                    onStrategySelected = { strategy ->
                        importViewModel.onImportStrategySelected(strategy)
                    },
                    onDismiss = { importViewModel.onConflictDialogDismissed() }
            )
        }
    }
}
