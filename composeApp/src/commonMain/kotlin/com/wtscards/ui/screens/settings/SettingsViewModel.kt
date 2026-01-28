package com.wtscards.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.domain.usecase.SettingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingUseCase: SettingUseCase,
    private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        observeSettings()
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
                        defaultDiscount = settings[KEY_DEFAULT_DISCOUNT] ?: "0"
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

    companion object {
        const val KEY_PRE_BODY_TEXT = "listing_pre_body_text"
        const val KEY_POST_BODY_TEXT = "listing_post_body_text"
        const val KEY_FREE_SHIPPING_ENABLED = "order_free_shipping_enabled"
        const val KEY_FREE_SHIPPING_THRESHOLD = "order_free_shipping_threshold"
        const val KEY_NICE_PRICES_ENABLED = "order_nice_prices_enabled"
        const val KEY_DEFAULT_DISCOUNT = "order_default_discount"
    }
}
