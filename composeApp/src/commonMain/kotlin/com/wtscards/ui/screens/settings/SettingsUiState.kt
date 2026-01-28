package com.wtscards.ui.screens.settings

data class SettingsUiState(
    val preBodyText: String = "",
    val postBodyText: String = "",
    val freeShippingEnabled: Boolean = false,
    val freeShippingThreshold: String = "",
    val nicePricesEnabled: Boolean = false,
    val defaultDiscount: String = "0",
    val isSaving: Boolean = false,
    val toast: SettingsToastState? = null
)

data class SettingsToastState(
    val message: String,
    val isError: Boolean = false
)
