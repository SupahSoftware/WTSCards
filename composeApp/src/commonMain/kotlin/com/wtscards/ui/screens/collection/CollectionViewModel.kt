package com.wtscards.ui.screens.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Card
import com.wtscards.domain.usecase.CardUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val cardUseCase: CardUseCase,
    private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(CollectionUiState())
        private set

    init {
        observeCards()
    }

    private fun observeCards() {
        cardUseCase.getAllCardsFlow()
            .onStart { uiState = uiState.copy(isLoading = true) }
            .onEach { cards ->
                uiState = uiState.copy(
                    isLoading = false,
                    allCards = cards,
                    error = null
                )
                updateDisplayedCards()
            }
            .catch { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load collection"
                )
            }
            .launchIn(coroutineScope)
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
        updateDisplayedCards()
    }

    fun onSortOptionChanged(sortOption: SortOption) {
        uiState = uiState.copy(sortOption = sortOption)
        updateDisplayedCards()
    }

    private fun updateDisplayedCards() {
        val filtered = if (uiState.searchQuery.isBlank()) {
            uiState.allCards
        } else {
            uiState.allCards.filter { card ->
                card.name.contains(uiState.searchQuery, ignoreCase = true)
            }
        }

        val sorted = when (uiState.sortOption) {
            SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            SortOption.PRICE_ASC -> filtered.sortedBy { it.priceInPennies }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { it.priceInPennies }
        }

        uiState = uiState.copy(displayedCards = sorted)
    }

    fun onRefresh() {
        coroutineScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val cards = cardUseCase.getAllCards()
                uiState = uiState.copy(
                    isLoading = false,
                    allCards = cards,
                    error = null
                )
                updateDisplayedCards()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh"
                )
            }
        }
    }
}
