package com.wtscards.domain.usecase

import com.wtscards.data.db.SettingLocalDataSource
import kotlinx.coroutines.flow.Flow

class SettingUseCaseImpl(
    private val settingLocalDataSource: SettingLocalDataSource
) : SettingUseCase {

    override fun getAllSettingsFlow(): Flow<Map<String, String>> {
        return settingLocalDataSource.getAllSettingsFlow()
    }

    override suspend fun getSetting(key: String): String? {
        return settingLocalDataSource.getByKey(key)
    }

    override suspend fun setSetting(key: String, value: String) {
        settingLocalDataSource.upsert(key, value)
    }
}
