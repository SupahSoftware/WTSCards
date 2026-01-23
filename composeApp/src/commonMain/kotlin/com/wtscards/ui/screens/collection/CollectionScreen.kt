package com.wtscards.ui.screens.collection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.wtscards.ui.components.CardFormFields
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wtscards.data.model.Card
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgDropdown
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
import com.wtscards.util.UrlUtils
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import wtscards.composeapp.generated.resources.Res
import wtscards.composeapp.generated.resources.ebay_button_logo

private val SearchRowHeight = 56.dp

@Composable
fun CollectionScreen(
    uiState: CollectionUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSortOptionChanged: (SortOption) -> Unit,
    onRefresh: () -> Unit,
    onToggleEditMode: () -> Unit,
    onToggleCardSelection: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onEditCard: (Card) -> Unit,
    onDismissEditCardDialog: () -> Unit,
    onEditNameChanged: (String) -> Unit,
    onEditCardNumberChanged: (String) -> Unit,
    onEditSetNameChanged: (String) -> Unit,
    onEditParallelNameChanged: (String) -> Unit,
    onEditGradeOptionChanged: (String) -> Unit,
    onEditPriceChanged: (String) -> Unit,
    onSaveEditCard: () -> Unit,
    canSaveEditCard: Boolean,
    onClearToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            SearchAndSortRow(
                searchQuery = uiState.searchQuery,
                sortOption = uiState.sortOption,
                isEditMode = uiState.isEditMode,
                onSearchQueryChanged = onSearchQueryChanged,
                onSortOptionChanged = onSortOptionChanged,
                onToggleEditMode = onToggleEditMode
            )
            Spacer(modifier = Modifier.height(16.dp))
            ContentArea(
                uiState = uiState,
                onToggleCardSelection = onToggleCardSelection,
                onEditCard = onEditCard
            )
        }

        if (uiState.isEditMode) {
            DeleteFab(onClick = onDeleteClick)
        }

        if (uiState.showDeleteConfirmDialog) {
            DeleteConfirmDialog(
                selectedCount = uiState.selectedCardIds.size,
                onConfirm = onDeleteConfirm,
                onDismiss = onDeleteCancel
            )
        }

        if (uiState.showEditCardDialog) {
            EditCardDialog(
                formState = uiState.editCardForm,
                onDismiss = onDismissEditCardDialog,
                onNameChanged = onEditNameChanged,
                onCardNumberChanged = onEditCardNumberChanged,
                onSetNameChanged = onEditSetNameChanged,
                onParallelNameChanged = onEditParallelNameChanged,
                onGradeOptionChanged = onEditGradeOptionChanged,
                onPriceChanged = onEditPriceChanged,
                onSave = onSaveEditCard,
                canSave = canSaveEditCard
            )
        }

        // Toast message
        uiState.toastMessage?.let { toast ->
            ToastMessage(
                message = toast.message,
                isError = toast.isError,
                onDismiss = onClearToast,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ContentArea(
    uiState: CollectionUiState,
    onToggleCardSelection: (String) -> Unit,
    onEditCard: (Card) -> Unit
) {
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
            uiState.displayedCards.isEmpty() -> {
                val message = if (uiState.searchQuery.isNotBlank()) {
                    "No cards match your search"
                } else {
                    "Your collection is empty"
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textSecondary
                )
            }
            else -> {
                CardList(
                    cards = uiState.displayedCards,
                    searchQuery = uiState.searchQuery,
                    sortOption = uiState.sortOption,
                    isEditMode = uiState.isEditMode,
                    selectedCardIds = uiState.selectedCardIds,
                    onToggleCardSelection = onToggleCardSelection,
                    onEditCard = onEditCard
                )
            }
        }
    }
}

@Composable
private fun BoxScope.DeleteFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(24.dp),
        containerColor = errorColor
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete selected",
            tint = textOnAccent
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    selectedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Cards",
                color = textPrimary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete $selectedCount item${if (selectedCount != 1) "s" else ""}?",
                color = textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = errorColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = textSecondary)
            }
        },
        containerColor = bgSurface
    )
}

@Composable
private fun SearchAndSortRow(
    searchQuery: String,
    sortOption: SortOption,
    isEditMode: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onSortOptionChanged: (SortOption) -> Unit,
    onToggleEditMode: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SearchRowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Bar (weight 3)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight(),
            placeholder = {
                Text("Search cards...", color = textTertiary)
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
        SortDropdown(
            selectedOption = sortOption,
            onOptionSelected = onSortOptionChanged,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Edit Button
        Box(
            modifier = Modifier
                .size(SearchRowHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isEditMode) errorColor else accentPrimary)
                .then(Modifier.padding(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onToggleEditMode
            ) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = if (isEditMode) "Exit edit mode" else "Edit",
                    tint = textOnAccent
                )
            }
        }
    }
}

@Composable
private fun SortDropdown(
    selectedOption: SortOption,
    onOptionSelected: (SortOption) -> Unit,
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
            SortOption.entries.forEach { option ->
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

private fun SortOption.displayName(): String = when (this) {
    SortOption.NAME_ASC -> "Name"
    SortOption.NAME_DESC -> "Name"
    SortOption.PRICE_ASC -> "Price"
    SortOption.PRICE_DESC -> "Price"
}

private fun SortOption.isAscending(): Boolean = when (this) {
    SortOption.NAME_ASC -> false // A->Z shows down arrow
    SortOption.NAME_DESC -> true // Z->A shows up arrow
    SortOption.PRICE_ASC -> true
    SortOption.PRICE_DESC -> false
}

@Composable
private fun CardList(
    cards: List<Card>,
    searchQuery: String,
    sortOption: SortOption,
    isEditMode: Boolean,
    selectedCardIds: Set<String>,
    onToggleCardSelection: (String) -> Unit,
    onEditCard: (Card) -> Unit
) {
    // Key the list state to filters so it resets when they change
    val listState = remember(searchQuery, sortOption) {
        androidx.compose.foundation.lazy.LazyListState()
    }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 24.dp) // Add padding for scrollbar
                .pointerInput(listState) {
                    var lastY = 0f
                    var velocity = 0f
                    var lastTime = System.currentTimeMillis()

                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    lastY = event.changes.first().position.y
                                    velocity = 0f
                                    lastTime = System.currentTimeMillis()
                                }
                                PointerEventType.Move -> {
                                    if (event.changes.first().pressed) {
                                        val currentY = event.changes.first().position.y
                                        val currentTime = System.currentTimeMillis()
                                        val delta = lastY - currentY
                                        val timeDelta = (currentTime - lastTime).coerceAtLeast(1)

                                        velocity = delta / timeDelta * 2000 // pixels per second
                                        lastY = currentY
                                        lastTime = currentTime

                                        coroutineScope.launch {
                                            listState.scrollBy(delta)
                                        }
                                    }
                                }
                                PointerEventType.Release -> {
                                    // Apply fling with calculated velocity
                                    if (kotlin.math.abs(velocity) > 100) {
                                        coroutineScope.launch {
                                            listState.scroll {
                                                var remainingVelocity = velocity * 0.5f
                                                val decay = 0.95f
                                                while (kotlin.math.abs(remainingVelocity) > 1f) {
                                                    scrollBy(remainingVelocity / 60f) // 60fps approximation
                                                    remainingVelocity *= decay
                                                    kotlinx.coroutines.delay(16) // ~60fps
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = cards,
                key = { it.id }
            ) { card ->
                CardRow(
                    card = card,
                    isEditMode = isEditMode,
                    isSelected = card.id in selectedCardIds,
                    onToggleSelection = { onToggleCardSelection(card.id) },
                    onEditClick = { onEditCard(card) }
                )
            }
        }

        androidx.compose.foundation.VerticalScrollbar(
            adapter = androidx.compose.foundation.rememberScrollbarAdapter(listState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 4.dp),
            style = androidx.compose.foundation.defaultScrollbarStyle().copy(
                unhoverColor = accentPrimary.copy(alpha = 0.4f),
                hoverColor = accentPrimary.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun CardRow(
    card: Card,
    isEditMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onEditClick: () -> Unit
) {
    val showGradeBadge = card.gradedString.isNotBlank() && card.gradedString.lowercase() != "ungraded"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(bgSurface)
            .clickable { onEditClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Grade badge (rotated 90 degrees)
        if (showGradeBadge) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentPrimary,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.gradedString,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textOnAccent,
                    modifier = Modifier
                        .graphicsLayer { rotationZ = 90f }
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox and Edit button (shown in edit mode)
            if (isEditMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    modifier = Modifier.size(24.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = accentPrimary,
                        uncheckedColor = textTertiary,
                        checkmarkColor = bgSurface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit card",
                        tint = accentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Card Info (takes remaining space)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.setName,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
            }

            // SportsCardPro Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { UrlUtils.openSportsCardPro(card.name) }
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Row {
                    Text(
                        text = "SPORTSCARD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5) // Blue
                    )
                    Text(
                        text = "PRO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF43A047) // Green
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // eBay Button
            IconButton(
                onClick = { UrlUtils.openEbaySoldListings(card.name) },
                modifier = Modifier.size(40.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.ebay_button_logo),
                    contentDescription = "Search on eBay",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Price (rightmost)
            if (card.priceInPennies > 0) {
                Text(
                    text = formatPrice(card.priceInPennies),
                    style = MaterialTheme.typography.bodyLarge,
                    color = successColor
                )
            } else {
                Text(
                    text = "\$0.00",
                    style = MaterialTheme.typography.bodyLarge,
                    color = warningColor
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
private fun EditCardDialog(
    formState: EditCardFormState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onCardNumberChanged: (String) -> Unit,
    onSetNameChanged: (String) -> Unit,
    onParallelNameChanged: (String) -> Unit,
    onGradeOptionChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Card",
                style = MaterialTheme.typography.headlineSmall,
                color = textPrimary
            )
        },
        text = {
            CardFormFields(
                name = formState.name,
                onNameChanged = onNameChanged,
                nameSuggestions = formState.nameSuggestions,
                cardNumber = formState.cardNumber,
                onCardNumberChanged = onCardNumberChanged,
                setName = formState.setName,
                onSetNameChanged = onSetNameChanged,
                setNameSuggestions = formState.setNameSuggestions,
                parallelName = formState.parallelName,
                onParallelNameChanged = onParallelNameChanged,
                parallelNameSuggestions = formState.parallelNameSuggestions,
                gradeOption = formState.gradeOption,
                onGradeOptionChanged = onGradeOptionChanged,
                priceText = formState.priceText,
                onPriceChanged = onPriceChanged
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentPrimary,
                    contentColor = textOnAccent,
                    disabledContainerColor = bgSurface,
                    disabledContentColor = textTertiary
                )
            ) {
                Text(if (formState.isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = textSecondary)
            }
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
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(
                color = if (isError) errorColor else successColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = message,
            color = textOnAccent,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
