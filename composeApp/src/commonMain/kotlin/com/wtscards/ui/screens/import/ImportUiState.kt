package com.wtscards.ui.screens.import

import com.wtscards.data.model.Card

sealed class ImportState {
    data object Idle : ImportState()
    data class Hovering(val fileName: String) : ImportState()
    data object Parsing : ImportState()
    data class ConflictDetected(
        val cards: List<Card>,
        val collisions: List<Card>
    ) : ImportState()
    data object Importing : ImportState()
    data class Success(val count: Int) : ImportState()
    data class Error(val message: String) : ImportState()
}

data class ImportUiState(
    val importState: ImportState = ImportState.Idle
)
