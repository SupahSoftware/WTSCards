package com.wtscards

import com.wtscards.domain.usecase.CardUseCase
import kotlinx.coroutines.CoroutineScope

data class AppDependencies(
    val cardUseCase: CardUseCase,
    val coroutineScope: CoroutineScope
)
