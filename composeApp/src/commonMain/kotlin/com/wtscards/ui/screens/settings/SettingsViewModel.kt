package com.wtscards.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.domain.model.BackupInfo
import com.wtscards.domain.usecase.BackupUseCase
import com.wtscards.domain.usecase.SettingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingUseCase: SettingUseCase,
    private val backupUseCase: BackupUseCase,
    private val onRestoreComplete: () -> Unit,
    private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        observeSettings()
        loadLastBackupDate()
    }

    private fun observeSettings() {
        settingUseCase.getAllSettingsFlow()
            .onEach { settings ->
                if (!uiState.isSaving) {
                    uiState = uiState.copy(
                        preBodyText = settings[KEY_PRE_BODY_TEXT] ?: "",
                        postBodyText = settings[KEY_POST_BODY_TEXT] ?: "",
                        freeShippingEnabled = settings[KEY_FREE_SHIPPING_ENABLED] == "true",
                        freeShippingThreshold = settings[KEY_FREE_SHIPPING_THRESHOLD] ?: "",
                        nicePricesEnabled = settings[KEY_NICE_PRICES_ENABLED] == "true",
                        defaultDiscount = settings[KEY_DEFAULT_DISCOUNT] ?: "0",
                        envelopeLength = settings[KEY_SHIPPING_ENVELOPE_LENGTH] ?: "3.5",
                        envelopeWidth = settings[KEY_SHIPPING_ENVELOPE_WIDTH] ?: "6.5",
                        bubbleMailerLength = settings[KEY_SHIPPING_BUBBLE_MAILER_LENGTH] ?: "6",
                        bubbleMailerWidth = settings[KEY_SHIPPING_BUBBLE_MAILER_WIDTH] ?: "9",
                        boxLength = settings[KEY_SHIPPING_BOX_LENGTH] ?: "6",
                        boxWidth = settings[KEY_SHIPPING_BOX_WIDTH] ?: "9",
                        boxHeight = settings[KEY_SHIPPING_BOX_HEIGHT] ?: "6"
                    )
                }
            }
            .catch { }
            .launchIn(coroutineScope)
    }

    fun onPreBodyTextChanged(value: String) {
        uiState = uiState.copy(preBodyText = value)
    }

    fun onPostBodyTextChanged(value: String) {
        uiState = uiState.copy(postBodyText = value)
    }

    fun onFreeShippingEnabledChanged(enabled: Boolean) {
        uiState = uiState.copy(freeShippingEnabled = enabled)
    }

    fun onFreeShippingThresholdChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(freeShippingThreshold = filtered)
        }
    }

    fun onNicePricesEnabledChanged(enabled: Boolean) {
        uiState = uiState.copy(nicePricesEnabled = enabled)
    }

    fun onDefaultDiscountChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        uiState = uiState.copy(defaultDiscount = filtered)
    }

    fun onEnvelopeLengthChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(envelopeLength = filtered)
        }
    }

    fun onEnvelopeWidthChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(envelopeWidth = filtered)
        }
    }

    fun onBubbleMailerLengthChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(bubbleMailerLength = filtered)
        }
    }

    fun onBubbleMailerWidthChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(bubbleMailerWidth = filtered)
        }
    }

    fun onBoxLengthChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(boxLength = filtered)
        }
    }

    fun onBoxWidthChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(boxWidth = filtered)
        }
    }

    fun onBoxHeightChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(boxHeight = filtered)
        }
    }

    fun onSave() {
        uiState = uiState.copy(isSaving = true)
        coroutineScope.launch {
            try {
                settingUseCase.setSetting(KEY_PRE_BODY_TEXT, uiState.preBodyText)
                settingUseCase.setSetting(KEY_POST_BODY_TEXT, uiState.postBodyText)
                settingUseCase.setSetting(KEY_FREE_SHIPPING_ENABLED, uiState.freeShippingEnabled.toString())
                settingUseCase.setSetting(KEY_FREE_SHIPPING_THRESHOLD, uiState.freeShippingThreshold)
                settingUseCase.setSetting(KEY_NICE_PRICES_ENABLED, uiState.nicePricesEnabled.toString())
                settingUseCase.setSetting(KEY_DEFAULT_DISCOUNT, uiState.defaultDiscount)
                settingUseCase.setSetting(KEY_SHIPPING_ENVELOPE_LENGTH, uiState.envelopeLength)
                settingUseCase.setSetting(KEY_SHIPPING_ENVELOPE_WIDTH, uiState.envelopeWidth)
                settingUseCase.setSetting(KEY_SHIPPING_BUBBLE_MAILER_LENGTH, uiState.bubbleMailerLength)
                settingUseCase.setSetting(KEY_SHIPPING_BUBBLE_MAILER_WIDTH, uiState.bubbleMailerWidth)
                settingUseCase.setSetting(KEY_SHIPPING_BOX_LENGTH, uiState.boxLength)
                settingUseCase.setSetting(KEY_SHIPPING_BOX_WIDTH, uiState.boxWidth)
                settingUseCase.setSetting(KEY_SHIPPING_BOX_HEIGHT, uiState.boxHeight)
                uiState = uiState.copy(
                    isSaving = false,
                    toast = SettingsToastState("Settings saved")
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    toast = SettingsToastState(e.message ?: "Failed to save settings", isError = true)
                )
            }
        }
    }

    fun clearToast() {
        uiState = uiState.copy(toast = null)
    }

    private fun loadLastBackupDate() {
        coroutineScope.launch {
            val date = backupUseCase.getLastBackupDisplayDate()
            uiState = uiState.copy(lastBackupDate = date)
        }
    }

    fun onBackupNow() {
        uiState = uiState.copy(isCreatingBackup = true)
        coroutineScope.launch {
            backupUseCase.createBackup().fold(
                onSuccess = { backupInfo ->
                    uiState = uiState.copy(
                        isCreatingBackup = false,
                        lastBackupDate = backupInfo.displayDate,
                        toast = SettingsToastState("Backup created successfully")
                    )
                },
                onFailure = { e ->
                    uiState = uiState.copy(
                        isCreatingBackup = false,
                        toast = SettingsToastState(e.message ?: "Failed to create backup", isError = true)
                    )
                }
            )
        }
    }

    fun onShowRestoreDialog() {
        coroutineScope.launch {
            val backups = backupUseCase.getAvailableBackups()
            uiState = uiState.copy(
                showRestoreDialog = true,
                availableBackups = backups
            )
        }
    }

    fun onDismissRestoreDialog() {
        uiState = uiState.copy(showRestoreDialog = false)
    }

    fun onSelectBackupToRestore(backup: BackupInfo) {
        uiState = uiState.copy(
            showRestoreDialog = false,
            showRestoreConfirmation = backup
        )
    }

    fun onConfirmRestore() {
        val backup = uiState.showRestoreConfirmation ?: return
        uiState = uiState.copy(isRestoring = true, showRestoreConfirmation = null)
        coroutineScope.launch {
            backupUseCase.restoreFromBackup(backup.fileName).fold(
                onSuccess = {
                    onRestoreComplete()
                },
                onFailure = { e ->
                    uiState = uiState.copy(
                        isRestoring = false,
                        toast = SettingsToastState(e.message ?: "Failed to restore backup", isError = true)
                    )
                }
            )
        }
    }

    fun onDismissRestoreConfirmation() {
        uiState = uiState.copy(showRestoreConfirmation = null)
    }

    companion object {
        const val KEY_PRE_BODY_TEXT = "listing_pre_body_text"
        const val KEY_POST_BODY_TEXT = "listing_post_body_text"
        const val KEY_FREE_SHIPPING_ENABLED = "order_free_shipping_enabled"
        const val KEY_FREE_SHIPPING_THRESHOLD = "order_free_shipping_threshold"
        const val KEY_NICE_PRICES_ENABLED = "order_nice_prices_enabled"
        const val KEY_DEFAULT_DISCOUNT = "order_default_discount"
        const val KEY_SHIPPING_ENVELOPE_LENGTH = "shipping_envelope_length"
        const val KEY_SHIPPING_ENVELOPE_WIDTH = "shipping_envelope_width"
        const val KEY_SHIPPING_BUBBLE_MAILER_LENGTH = "shipping_bubble_mailer_length"
        const val KEY_SHIPPING_BUBBLE_MAILER_WIDTH = "shipping_bubble_mailer_width"
        const val KEY_SHIPPING_BOX_LENGTH = "shipping_box_length"
        const val KEY_SHIPPING_BOX_WIDTH = "shipping_box_width"
        const val KEY_SHIPPING_BOX_HEIGHT = "shipping_box_height"
    }
}
