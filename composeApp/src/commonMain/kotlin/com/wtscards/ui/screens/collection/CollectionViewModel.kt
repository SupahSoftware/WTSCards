package com.wtscards.ui.screens.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.repository.CollectionRepository

class CollectionViewModel(
    private val repository: CollectionRepository
) {
    var uiState by mutableStateOf(CollectionUiState())
        private set

    fun loadCollection() {
        uiState = uiState.copy(isLoading = true)
        // TODO: Implement with coroutines
        uiState = uiState.copy(isLoading = false, items = emptyList())
    }

    fun onRefresh() {
        loadCollection()
    }
}
