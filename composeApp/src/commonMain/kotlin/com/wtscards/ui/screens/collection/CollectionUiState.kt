package com.wtscards.ui.screens.collection

data class CollectionUiState(
    val isLoading: Boolean = false,
    val items: List<Any> = emptyList(),
    val error: String? = null
)
