package com.wtscards

import com.wtscards.domain.usecase.AutocompleteUseCase
import com.wtscards.domain.usecase.BackupUseCase
import com.wtscards.domain.usecase.CardUseCase
import com.wtscards.domain.usecase.ListingUseCase
import com.wtscards.domain.usecase.OrderUseCase
import com.wtscards.domain.usecase.SettingUseCase
import kotlinx.coroutines.CoroutineScope

data class AppDependencies(
    val cardUseCase: CardUseCase,
    val orderUseCase: OrderUseCase,
    val listingUseCase: ListingUseCase,
    val autocompleteUseCase: AutocompleteUseCase,
    val settingUseCase: SettingUseCase,
    val backupUseCase: BackupUseCase,
    val coroutineScope: CoroutineScope
)
