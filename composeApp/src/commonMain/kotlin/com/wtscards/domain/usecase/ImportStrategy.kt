package com.wtscards.domain.usecase

enum class ImportStrategy {
    OVERWRITE_ALL,
    UPDATE_PRICES_ONLY,
    SAFE_IMPORT
}
