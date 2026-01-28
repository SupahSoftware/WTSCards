package com.wtscards.ui.screens.settings

import com.wtscards.domain.model.BackupInfo

data class SettingsUiState(
    val preBodyText: String = "",
    val postBodyText: String = "",
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
    val isRestoring: Boolean = false
)

data class SettingsToastState(
    val message: String,
    val isError: Boolean = false
)
