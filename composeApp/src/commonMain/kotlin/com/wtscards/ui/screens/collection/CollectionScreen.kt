package com.wtscards.ui.screens.collection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.wtscards.data.model.Card
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgSecondary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.borderInput
import com.wtscards.ui.theme.successColor
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // Search and Sort Row
        SearchAndSortRow(
            searchQuery = uiState.searchQuery,
            sortOption = uiState.sortOption,
            onSearchQueryChanged = onSearchQueryChanged,
            onSortOptionChanged = onSortOptionChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                        sortOption = uiState.sortOption
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAndSortRow(
    searchQuery: String,
    sortOption: SortOption,
    onSearchQueryChanged: (String) -> Unit,
    onSortOptionChanged: (SortOption) -> Unit
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
                focusedBorderColor = accentPrimary,
                unfocusedBorderColor = borderInput,
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
    }
}

@Composable
private fun SortDropdown(
    selectedOption: SortOption,
    onOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = selectedOption.displayName(),
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
            modifier = Modifier.background(bgSecondary)
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
    sortOption: SortOption
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
                key = { it.sportsCardProId }
            ) { card ->
                CardRow(card = card)
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
private fun CardRow(card: Card) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            if (card.gradedString.isNotBlank() && card.gradedString.lowercase() != "ungraded") {
                Text(
                    text = card.gradedString,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentPrimary
                )
            }
        }

        // eBay Button (before price, with 16dp margin)
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
        Column(horizontalAlignment = Alignment.End) {
            if (card.priceInPennies > 0) {
                Text(
                    text = formatPrice(card.priceInPennies),
                    style = MaterialTheme.typography.bodyLarge,
                    color = successColor
                )
            } else {
                Text(
                    text = "No sales",
                    style = MaterialTheme.typography.bodyLarge,
                    color = warningColor
                )
            }
            if (card.quantity > 1) {
                Text(
                    text = "x${card.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textTertiary
                )
            }
        }
    }
}

private fun formatPrice(priceInPennies: Long): String {
    val dollars = priceInPennies / 100.0
    return "$${String.format("%.2f", dollars)}"
}
