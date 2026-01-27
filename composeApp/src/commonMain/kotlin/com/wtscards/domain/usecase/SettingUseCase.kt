package com.wtscards.domain.usecase

import kotlinx.coroutines.flow.Flow

interface SettingUseCase {
    fun getAllSettingsFlow(): Flow<Map<String, String>>
    suspend fun getSetting(key: String): String?
    suspend fun setSetting(key: String, value: String)
}
