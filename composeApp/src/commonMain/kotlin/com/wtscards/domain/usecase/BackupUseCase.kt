package com.wtscards.domain.usecase

import com.wtscards.domain.model.BackupInfo

interface BackupUseCase {
    suspend fun createBackup(): Result<BackupInfo>
    suspend fun createBackupIfNeeded()
    suspend fun getAvailableBackups(): List<BackupInfo>
    suspend fun getLastBackupDisplayDate(): String?
    suspend fun restoreFromBackup(fileName: String): Result<Unit>
}
