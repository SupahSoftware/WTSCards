package com.wtscards.ui.screens.import

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Card
import com.wtscards.domain.usecase.CardUseCase
import com.wtscards.domain.usecase.ImportStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ImportViewModel(
    private val cardUseCase: CardUseCase,
    private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(ImportUiState())
        private set

    private var pendingCards: List<Card> = emptyList()

    fun onFileHoverStart(fileName: String) {
        uiState = uiState.copy(importState = ImportState.Hovering(fileName))
    }

    fun onFileHoverEnd() {
        val currentState = uiState.importState
        if (currentState is ImportState.Hovering) {
            uiState = uiState.copy(importState = ImportState.Idle)
        }
    }

    fun onFileDropped(cards: List<Card>) {
        pendingCards = cards
        uiState = uiState.copy(importState = ImportState.Parsing)

        coroutineScope.launch {
            try {
                val collisions = cardUseCase.findCollisions(cards)
                if (collisions.isEmpty()) {
                    performImport(cards, ImportStrategy.OVERWRITE_ALL)
                } else {
                    uiState = uiState.copy(
                        importState = ImportState.ConflictDetected(
                            cards = cards,
                            collisions = collisions
                        )
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    importState = ImportState.Error(e.message ?: "Unknown error")
                )
            }
        }
    }

    fun onImportStrategySelected(strategy: ImportStrategy) {
        coroutineScope.launch {
            performImport(pendingCards, strategy)
        }
    }

    fun onConflictDialogDismissed() {
        pendingCards = emptyList()
        uiState = uiState.copy(importState = ImportState.Idle)
    }

    fun resetState() {
        uiState = uiState.copy(importState = ImportState.Idle)
    }

    fun onImportError(message: String) {
        uiState = uiState.copy(importState = ImportState.Error(message))
    }

    private suspend fun performImport(cards: List<Card>, strategy: ImportStrategy) {
        uiState = uiState.copy(importState = ImportState.Importing)
        try {
            cardUseCase.importCards(cards, strategy)
            uiState = uiState.copy(importState = ImportState.Success(cards.size))
        } catch (e: Exception) {
            uiState = uiState.copy(
                importState = ImportState.Error(e.message ?: "Import failed")
            )
        }
    }
}
