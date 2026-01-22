package com.wtscards.ui.screens.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
                    cards = cards,
                    error = null
                )
            }
            .catch { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load collection"
                )
            }
            .launchIn(coroutineScope)
    }

    fun onRefresh() {
        coroutineScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val cards = cardUseCase.getAllCards()
                uiState = uiState.copy(isLoading = false, cards = cards, error = null)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh"
                )
            }
        }
    }
}
