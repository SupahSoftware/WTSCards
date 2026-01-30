package com.wtscards.ui.screens.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.wtscards.data.model.Card
import com.wtscards.data.model.Listing
import com.wtscards.ui.components.AppTextField
import com.wtscards.ui.components.ScrollableList
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgSecondary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.errorColor
import com.wtscards.ui.theme.successColor
import com.wtscards.ui.theme.textOnAccent
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary
import com.wtscards.util.UrlUtils
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlinx.coroutines.delay

private val SearchRowHeight = 56.dp

@Composable
fun ListingScreen(
        uiState: ListingUiState,
        onSearchQueryChanged: (String) -> Unit,
        onShowCreateDialog: () -> Unit,
        onDismissCreateDialog: () -> Unit,
        onTitleChanged: (String) -> Unit,
        onDiscountChanged: (String) -> Unit,
        onNicePricesChanged: (Boolean) -> Unit,
        onCreateListing: () -> Unit,
        onEditListing: (Listing) -> Unit,
        onShowAddCardsDialog: (String) -> Unit,
        onDismissAddCardsDialog: () -> Unit,
        onAddCardsSearchChanged: (String) -> Unit,
        onToggleCardSelection: (String) -> Unit,
        onConfirmAddCards: () -> Unit,
        onShowRemoveCardDialog: (String, String, String) -> Unit,
        onDismissRemoveCardDialog: () -> Unit,
        onConfirmRemoveCard: () -> Unit,
        onShowDeleteListingDialog: (String, String) -> Unit,
        onDismissDeleteListingDialog: () -> Unit,
        onConfirmDeleteListing: () -> Unit,
        onShowCopyToast: (String) -> Unit,
        onClearToast: () -> Unit,
        onClearFocusSearchFlag: () -> Unit,
        modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ListingScreenHeader()

            Spacer(modifier = Modifier.height(16.dp))

            SearchRow(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChanged = onSearchQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            ContentArea(
                    uiState = uiState,
                    onEditListing = onEditListing,
                    onShowAddCardsDialog = onShowAddCardsDialog,
                    onShowDeleteListingDialog = onShowDeleteListingDialog,
                    onShowRemoveCardDialog = onShowRemoveCardDialog,
                    onShowCopyToast = onShowCopyToast,
                    preBodyText = uiState.preBodyText,
                    postBodyText = uiState.postBodyText
            )
        }

        CreateListingFab(
                onClick = onShowCreateDialog,
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        )

        ListingDialogs(
                uiState = uiState,
                onDismissCreateDialog = onDismissCreateDialog,
                onTitleChanged = onTitleChanged,
                onDiscountChanged = onDiscountChanged,
                onNicePricesChanged = onNicePricesChanged,
                onCreateListing = onCreateListing,
                onDismissAddCardsDialog = onDismissAddCardsDialog,
                onAddCardsSearchChanged = onAddCardsSearchChanged,
                onToggleCardSelection = onToggleCardSelection,
                onConfirmAddCards = onConfirmAddCards,
                onDismissRemoveCardDialog = onDismissRemoveCardDialog,
                onConfirmRemoveCard = onConfirmRemoveCard,
                onDismissDeleteListingDialog = onDismissDeleteListingDialog,
                onConfirmDeleteListing = onConfirmDeleteListing,
                onClearFocusSearchFlag = onClearFocusSearchFlag
        )

        uiState.toast?.let { toast ->
            ListingToast(
                    toast = toast,
                    onDismiss = onClearToast,
                    modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ListingScreenHeader() {
    Text(text = "Listings", style = MaterialTheme.typography.headlineMedium, color = textPrimary)
}

@Composable
private fun ListingToast(
        toast: ListingToastState,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
) {
    ToastMessage(
            message = toast.message,
            isError = toast.isError,
            onDismiss = onDismiss,
            modifier = modifier
    )
}

@Composable
private fun SearchRow(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth().height(SearchRowHeight),
            placeholder = { Text("Search listings by title or card name", color = textTertiary) },
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
}

@Composable
private fun ContentArea(
        uiState: ListingUiState,
        onEditListing: (Listing) -> Unit,
        onShowAddCardsDialog: (String) -> Unit,
        onShowDeleteListingDialog: (String, String) -> Unit,
        onShowRemoveCardDialog: (String, String, String) -> Unit,
        onShowCopyToast: (String) -> Unit,
        preBodyText: String,
        postBodyText: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = accentPrimary)
            }
            uiState.error != null -> {
                Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
            }
            uiState.listings.isEmpty() -> {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                            text = "No listings yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                            text =
                                    "Create a listing to organize cards for social media posts. Cards added to listings remain in your collection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                    )
                }
            }
            uiState.filteredListings.isEmpty() -> {
                Text(
                        text = "No listings match your search",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondary
                )
            }
            else -> {
                ListingList(
                        listings = uiState.filteredListings,
                        onEditListing = onEditListing,
                        onShowAddCardsDialog = onShowAddCardsDialog,
                        onShowDeleteListingDialog = onShowDeleteListingDialog,
                        onShowRemoveCardDialog = onShowRemoveCardDialog,
                        onShowCopyToast = onShowCopyToast,
                        preBodyText = preBodyText,
                        postBodyText = postBodyText
                )
            }
        }
    }
}

@Composable
private fun ListingList(
        listings: List<Listing>,
        onEditListing: (Listing) -> Unit,
        onShowAddCardsDialog: (String) -> Unit,
        onShowDeleteListingDialog: (String, String) -> Unit,
        onShowRemoveCardDialog: (String, String, String) -> Unit,
        onShowCopyToast: (String) -> Unit,
        preBodyText: String,
        postBodyText: String
) {
    val listState = rememberLazyListState()

    ScrollableList(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = listings, key = { it.id }) { listing ->
            ListingCard(
                    listing = listing,
                    onEditListing = { onEditListing(listing) },
                    onShowAddCardsDialog = { onShowAddCardsDialog(listing.id) },
                    onShowDeleteListingDialog = {
                        onShowDeleteListingDialog(listing.id, listing.title)
                    },
                    onShowRemoveCardDialog = { cardId, cardName ->
                        onShowRemoveCardDialog(listing.id, cardId, cardName)
                    },
                    onShowCopyToast = onShowCopyToast,
                    preBodyText = preBodyText,
                    postBodyText = postBodyText
            )
        }
    }
}

@Composable
private fun ListingCard(
        listing: Listing,
        onEditListing: () -> Unit,
        onShowAddCardsDialog: () -> Unit,
        onShowDeleteListingDialog: () -> Unit,
        onShowRemoveCardDialog: (String, String) -> Unit,
        onShowCopyToast: (String) -> Unit,
        preBodyText: String,
        postBodyText: String
) {
    val markdownBody = generateMarkdownBody(listing.cards, listing.discount, listing.nicePrices)

    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgSurface)
                            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            if (listing.cards.isNotEmpty()) {
                val totalPrice =
                        listing.cards
                                .filter { card -> card.priceSold == null || card.priceSold <= 0 }
                                .sumOf { card ->
                                    calculateListingPrice(
                                            card.priceInPennies,
                                            listing.discount,
                                            listing.nicePrices
                                    )
                                }
                if (totalPrice > 0) {
                    Text(
                            text = formatPrice(totalPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = successColor
                    )
                }
            }

            Box(
                    modifier =
                            Modifier.clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        UrlUtils.openRedditSubmit(listing.title, markdownBody)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                        text = "reddit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4500) // Reddit orange
                )
            }

            ListingOverflowMenu(
                    onEdit = onEditListing,
                    onAddCards = onShowAddCardsDialog,
                    onDelete = onShowDeleteListingDialog,
                    onCopyTitle = {
                        copyToClipboard(listing.title)
                        onShowCopyToast("Title copied to clipboard")
                    },
                    onCopyBody = {
                        val fullBody = buildFullBody(preBodyText, markdownBody, postBodyText)
                        copyToClipboard(fullBody)
                        onShowCopyToast("Body copied to clipboard")
                    }
            )
        }

        if (listing.cards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            val sortedCards =
                    listing.cards.sortedWith(
                            compareBy<Card> { card ->
                                if (card.priceSold != null && card.priceSold > 0) 1 else 0
                            }
                                    .thenByDescending { card -> card.priceInPennies }
                    )

            sortedCards.forEach { card ->
                CardItem(
                        card = card,
                        discountPercent = listing.discount,
                        nicePrices = listing.nicePrices,
                        onRemove = { onShowRemoveCardDialog(card.id, card.name) }
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = "No cards added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textTertiary
            )
        }
    }
}

@Composable
private fun ListingOverflowMenu(
        onEdit: () -> Unit,
        onAddCards: () -> Unit,
        onDelete: () -> Unit,
        onCopyTitle: () -> Unit,
        onCopyBody: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(36.dp)) {
            Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = textSecondary
            )
        }

        DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(bgSecondary)
        ) {
            DropdownMenuItem(
                    text = { Text("Edit listing", color = textPrimary) },
                    onClick = {
                        expanded = false
                        onEdit()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = accentPrimary)
                    }
            )
            DropdownMenuItem(
                    text = { Text("Copy title", color = textPrimary) },
                    onClick = {
                        expanded = false
                        onCopyTitle()
                    },
                    leadingIcon = {
                        Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = accentPrimary
                        )
                    }
            )
            DropdownMenuItem(
                    text = { Text("Copy body", color = textPrimary) },
                    onClick = {
                        expanded = false
                        onCopyBody()
                    },
                    leadingIcon = {
                        Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = accentPrimary
                        )
                    }
            )
            DropdownMenuItem(
                    text = { Text("Add cards", color = textPrimary) },
                    onClick = {
                        expanded = false
                        onAddCards()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null, tint = accentPrimary)
                    }
            )
            DropdownMenuItem(
                    text = { Text("Delete listing", color = errorColor) },
                    onClick = {
                        expanded = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = errorColor)
                    }
            )
        }
    }
}

@Composable
private fun CardItem(card: Card, discountPercent: Int, nicePrices: Boolean, onRemove: () -> Unit) {
    val adjustedPrice = calculateListingPrice(card.priceInPennies, discountPercent, nicePrices)
    val isSold = card.priceSold != null && card.priceSold > 0
    val textColor = if (isSold) textTertiary else textPrimary
    val textDecoration = if (isSold) TextDecoration.LineThrough else null

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
                text = card.name,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textDecoration = textDecoration
        )

        if (isSold) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "[SOLD]", style = MaterialTheme.typography.bodyMedium, color = textTertiary)
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (adjustedPrice > 0) {
            Text(
                    text = formatPrice(adjustedPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = successColor
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
                text = "EBAY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentPrimary,
                modifier =
                        Modifier.clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    UrlUtils.openInBrowser(
                                            UrlUtils.getEbaySoldListingsUrl(card.name)
                                    )
                                }
                                .padding(horizontal = 2.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
                text = "SportsCardPro",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentPrimary,
                modifier =
                        Modifier.clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    UrlUtils.openInBrowser(UrlUtils.getSportsCardProUrl(card.name))
                                }
                                .padding(horizontal = 2.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove card",
                tint = errorColor,
                modifier =
                        Modifier.size(24.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(onClick = onRemove)
        )
    }
}

@Composable
private fun BoxScope.CreateListingFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(onClick = onClick, modifier = modifier, containerColor = accentPrimary) {
        Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create listing",
                tint = textOnAccent
        )
    }
}

@Composable
private fun ListingDialogs(
        uiState: ListingUiState,
        onDismissCreateDialog: () -> Unit,
        onTitleChanged: (String) -> Unit,
        onDiscountChanged: (String) -> Unit,
        onNicePricesChanged: (Boolean) -> Unit,
        onCreateListing: () -> Unit,
        onDismissAddCardsDialog: () -> Unit,
        onAddCardsSearchChanged: (String) -> Unit,
        onToggleCardSelection: (String) -> Unit,
        onConfirmAddCards: () -> Unit,
        onDismissRemoveCardDialog: () -> Unit,
        onConfirmRemoveCard: () -> Unit,
        onDismissDeleteListingDialog: () -> Unit,
        onConfirmDeleteListing: () -> Unit,
        onClearFocusSearchFlag: () -> Unit
) {
    if (uiState.showCreateDialog) {
        CreateListingDialog(
                formState = uiState.createFormState,
                isEditing = uiState.editingListingId != null,
                onDismiss = onDismissCreateDialog,
                onTitleChanged = onTitleChanged,
                onDiscountChanged = onDiscountChanged,
                onNicePricesChanged = onNicePricesChanged,
                onCreate = onCreateListing
        )
    }

    uiState.addCardsDialogState?.let { dialogState ->
        AddCardsDialog(
                dialogState = dialogState,
                availableCards = uiState.availableCards,
                existingCardIds =
                        uiState.listings
                                .find { it.id == dialogState.listingId }
                                ?.cards
                                ?.map { it.id }
                                ?.toSet()
                                ?: emptySet(),
                onDismiss = onDismissAddCardsDialog,
                onSearchChanged = onAddCardsSearchChanged,
                onToggleCardSelection = onToggleCardSelection,
                onConfirm = onConfirmAddCards,
                onClearFocusSearchFlag = onClearFocusSearchFlag
        )
    }

    uiState.removeCardDialogState?.let { dialogState ->
        RemoveCardDialog(
                cardName = dialogState.cardName,
                isRemoving = dialogState.isRemoving,
                onDismiss = onDismissRemoveCardDialog,
                onConfirm = onConfirmRemoveCard
        )
    }

    uiState.deleteListingDialogState?.let { dialogState ->
        DeleteListingDialog(
                listingTitle = dialogState.listingTitle,
                isDeleting = dialogState.isDeleting,
                onDismiss = onDismissDeleteListingDialog,
                onConfirm = onConfirmDeleteListing
        )
    }
}

@Composable
private fun CreateListingDialog(
        formState: CreateListingFormState,
        isEditing: Boolean,
        onDismiss: () -> Unit,
        onTitleChanged: (String) -> Unit,
        onDiscountChanged: (String) -> Unit,
        onNicePricesChanged: (Boolean) -> Unit,
        onCreate: () -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(if (isEditing) "Edit Listing" else "Create Listing", color = textPrimary)
            },
            text = {
                Column {
                    if (!isEditing) {
                        Text(
                                text = "You will add cards after you create the listing.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    AppTextField(
                            value = formState.title,
                            onValueChange = onTitleChanged,
                            label = "Title",
                            containerColor = bgSecondary,
                            borderColor = textTertiary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppTextField(
                            value = formState.discount,
                            onValueChange = onDiscountChanged,
                            label = "Discount",
                            suffix = "%",
                            modifier = Modifier.width(200.dp),
                            containerColor = bgSecondary,
                            borderColor = textTertiary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                    text = "Nice prices",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                    text = "Round up to the next dollar amount",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                                checked = formState.nicePrices,
                                onCheckedChange = onNicePricesChanged,
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor = textOnAccent,
                                                checkedTrackColor = accentPrimary,
                                                uncheckedThumbColor = textTertiary,
                                                uncheckedTrackColor = bgSecondary
                                        )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = onCreate,
                        enabled = formState.isValid(),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = accentPrimary,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSurface,
                                        disabledContentColor = textTertiary
                                ),
                        shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                            when {
                                formState.isSaving && isEditing -> "Updating..."
                                formState.isSaving -> "Creating..."
                                isEditing -> "Update"
                                else -> "Create"
                            }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = textSecondary) }
            },
            containerColor = bgSecondary
    )
}

@Composable
private fun AddCardsDialog(
        dialogState: ListingAddCardsDialogState,
        availableCards: List<Card>,
        existingCardIds: Set<String>,
        onDismiss: () -> Unit,
        onSearchChanged: (String) -> Unit,
        onToggleCardSelection: (String) -> Unit,
        onConfirm: () -> Unit,
        onClearFocusSearchFlag: () -> Unit
) {
    val cardsToShow = availableCards.filter { it.id !in existingCardIds }
    val filteredCards =
            if (dialogState.searchQuery.isBlank()) {
                cardsToShow
            } else {
                cardsToShow.filter { card ->
                    card.name.contains(dialogState.searchQuery, ignoreCase = true)
                }
            }

    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(dialogState.searchQuery)) }

    // Sync textFieldValue with dialogState changes
    LaunchedEffect(dialogState.searchQuery) {
        if (textFieldValue.text != dialogState.searchQuery) {
            textFieldValue = TextFieldValue(dialogState.searchQuery)
        }
    }

    LaunchedEffect(dialogState.shouldFocusSearch) {
        if (dialogState.shouldFocusSearch) {
            // Select all text
            textFieldValue =
                    textFieldValue.copy(selection = TextRange(0, textFieldValue.text.length))
            focusRequester.requestFocus()
            onClearFocusSearchFlag()
        }
    }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Cards", color = textPrimary) },
            text = {
                Column(modifier = Modifier.width(400.dp).height(400.dp)) {
                    OutlinedTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                                onSearchChanged(newValue.text)
                            },
                            placeholder = { Text("Search cards...") },
                            leadingIcon = {
                                Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = textTertiary
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary,
                                            focusedBorderColor = accentPrimary,
                                            unfocusedBorderColor = textTertiary,
                                            cursorColor = accentPrimary
                                    )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredCards.isEmpty()) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text =
                                            if (cardsToShow.isEmpty())
                                                    "All cards are already in this listing"
                                            else "No cards match your search",
                                    color = textSecondary
                            )
                        }
                    } else {
                        ScrollableList(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                scrollbarPadding = 16.dp
                        ) {
                            items(filteredCards) { card ->
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .clickable {
                                                            onToggleCardSelection(card.id)
                                                        }
                                                        .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                            checked = card.id in dialogState.selectedCardIds,
                                            onCheckedChange = { onToggleCardSelection(card.id) },
                                            colors =
                                                    CheckboxDefaults.colors(
                                                            checkedColor = accentPrimary,
                                                            uncheckedColor = textTertiary,
                                                            checkmarkColor = bgSurface
                                                    )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = card.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textPrimary
                                        )
                                        Text(
                                                text = card.setName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textSecondary
                                        )
                                    }
                                    if (card.priceInPennies > 0) {
                                        Text(
                                                text = formatPrice(card.priceInPennies),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = successColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        enabled = dialogState.selectedCardIds.isNotEmpty() && !dialogState.isSaving,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = accentPrimary,
                                        contentColor = textOnAccent,
                                        disabledContainerColor = bgSurface,
                                        disabledContentColor = textTertiary
                                )
                ) {
                    Text(
                            if (dialogState.isSaving) "Adding..."
                            else
                                    "Add ${dialogState.selectedCardIds.size} Card${if (dialogState.selectedCardIds.size != 1) "s" else ""}"
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = textSecondary) }
            },
            containerColor = bgSecondary
    )
}

@Composable
private fun RemoveCardDialog(
        cardName: String,
        isRemoving: Boolean,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Remove Card", color = textPrimary) },
            text = {
                Text(text = "Remove \"$cardName\" from this listing?", color = textSecondary)
            },
            confirmButton = {
                TextButton(onClick = onConfirm, enabled = !isRemoving) {
                    Text(if (isRemoving) "Removing..." else "Remove", color = errorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = textSecondary) }
            },
            containerColor = bgSecondary
    )
}

@Composable
private fun DeleteListingDialog(
        listingTitle: String,
        isDeleting: Boolean,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete Listing", color = textPrimary) },
            text = {
                Text(
                        text =
                                "Are you sure you want to delete \"$listingTitle\"? This cannot be undone.",
                        color = textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm, enabled = !isDeleting) {
                    Text(if (isDeleting) "Deleting..." else "Delete", color = errorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = textSecondary) }
            },
            containerColor = bgSecondary
    )
}

@Composable
private fun ToastMessage(
        message: String,
        isError: Boolean,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
) {
    LaunchedEffect(message) {
        delay(3000)
        onDismiss()
    }

    Box(
            modifier =
                    modifier.padding(16.dp)
                            .fillMaxWidth()
                            .background(
                                    color = if (isError) errorColor else successColor,
                                    shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
    ) { Text(text = message, color = textOnAccent, style = MaterialTheme.typography.bodyMedium) }
}

private fun buildFullBody(preBodyText: String, body: String, postBodyText: String): String {
    return buildString {
        if (preBodyText.isNotBlank()) {
            append(preBodyText)
            append("\n")
        }
        append(body)
        if (postBodyText.isNotBlank()) {
            append("\n")
            append("\n")
            append(postBodyText)
        }
        append("\n")
        append("\n")
        append("Created with [WTSCards](https://github.com/SupahSoftware/WTSCards)")
    }
}

private fun calculateListingPrice(
        priceInPennies: Long,
        discountPercent: Int,
        nicePrices: Boolean
): Long {
    if (priceInPennies <= 0) return priceInPennies
    val discounted = (priceInPennies * (100 - discountPercent) / 100.0)
    val rounded = kotlin.math.ceil(discounted).toLong()
    return if (nicePrices) {
        val remainder = rounded % 100
        if (remainder == 0L) rounded else rounded + (100 - remainder)
    } else {
        rounded
    }
}

private fun generateMarkdownBody(
        cards: List<Card>,
        discountPercent: Int,
        nicePrices: Boolean
): String {
    if (cards.isEmpty()) return ""

    val availableCards = cards.filter { card -> card.priceSold == null || card.priceSold <= 0 }
    val soldCards = cards.filter { card -> card.priceSold != null && card.priceSold > 0 }

    val sections = mutableListOf<String>()

    if (availableCards.isNotEmpty()) {
        val availableSection = buildString {
            append("**Available**\n\n")
            append(
                    availableCards.joinToString("\n") { card ->
                        val adjustedPrice =
                                calculateListingPrice(
                                        card.priceInPennies,
                                        discountPercent,
                                        nicePrices
                                )
                        val priceStr =
                                if (adjustedPrice > 0) {
                                    "$${String.format("%.2f", adjustedPrice / 100.0)}"
                                } else {
                                    "$0.00"
                                }
                        val scpUrl = UrlUtils.getSportsCardProUrl(card.name)
                        val ebayUrl = UrlUtils.getEbaySoldListingsUrl(card.name)
                        "- ${card.name} - $priceStr ([EBAY]($ebayUrl)) ([SportsCardPro]($scpUrl))"
                    }
            )
        }
        sections.add(availableSection)
    }

    if (soldCards.isNotEmpty()) {
        val soldSection = buildString {
            append("**Sold**\n\n")
            append(
                    soldCards.joinToString("\n") { card ->
                        val adjustedPrice =
                                calculateListingPrice(
                                        card.priceInPennies,
                                        discountPercent,
                                        nicePrices
                                )
                        val priceStr =
                                if (adjustedPrice > 0) {
                                    "$${String.format("%.2f", adjustedPrice / 100.0)}"
                                } else {
                                    "$0.00"
                                }
                        val scpUrl = UrlUtils.getSportsCardProUrl(card.name)
                        val ebayUrl = UrlUtils.getEbaySoldListingsUrl(card.name)
                        "- ~~${card.name}~~ - $priceStr ([EBAY]($ebayUrl)) ([SportsCardPro]($scpUrl))"
                    }
            )
        }
        sections.add(soldSection)
    }

    return sections.joinToString("\n\n")
}

private fun formatPrice(priceInPennies: Long): String {
    val dollars = priceInPennies / 100.0
    return "$${String.format("%.2f", dollars)}"
}

private fun copyToClipboard(text: String) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(text)
        clipboard.setContents(selection, selection)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
