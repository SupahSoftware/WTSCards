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
                uiState = uiState.copy(
                    preBodyText = settings[KEY_PRE_BODY_TEXT] ?: "",
                    postBodyText = settings[KEY_POST_BODY_TEXT] ?: ""
                )
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

    fun onSave() {
        uiState = uiState.copy(isSaving = true)
        coroutineScope.launch {
            try {
                settingUseCase.setSetting(KEY_PRE_BODY_TEXT, uiState.preBodyText)
                settingUseCase.setSetting(KEY_POST_BODY_TEXT, uiState.postBodyText)
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
    }
}
