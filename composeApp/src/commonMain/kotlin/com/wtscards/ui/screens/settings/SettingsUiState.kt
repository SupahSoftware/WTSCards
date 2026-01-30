package com.wtscards.ui.screens.settings

import com.wtscards.domain.model.BackupInfo

data class SettingsUiState(
    val preBodyText: String = "",
    val postBodyText: String = "",
    val listingNicePricesEnabled: Boolean = false,
    val listingDefaultDiscount: String = "0",
    val freeShippingEnabled: Boolean = false,
    val freeShippingThreshold: String = "",
    val nicePricesEnabled: Boolean = false,
    val defaultDiscount: String = "0",
    val isSaving: Boolean = false,
    val toast: SettingsToastState? = null,
    val lastBackupDate: String? = null,
    val availableBackups: List<BackupInfo> = emptyList(),
    val showRestoreDialog: Boolean = false,
    val showRestoreConfirmation: BackupInfo? = null,
    val isCreatingBackup: Boolean = false,
    val isRestoring: Boolean = false,
    val envelopeCost: String = "1.00",
    val envelopeLength: String = "3.5",
    val envelopeWidth: String = "6.5",
    val bubbleMailerCost: String = "7.00",
    val bubbleMailerLength: String = "6",
    val bubbleMailerWidth: String = "9",
    val boxCost: String = "10.00",
    val boxLength: String = "6",
    val boxWidth: String = "9",
    val boxHeight: String = "6"
)

data class SettingsToastState(
    val message: String,
    val isError: Boolean = false
)
