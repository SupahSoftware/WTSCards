package com.wtscards

import com.wtscards.domain.usecase.AutocompleteUseCase
import com.wtscards.domain.usecase.CardUseCase
import com.wtscards.domain.usecase.OrderUseCase
import kotlinx.coroutines.CoroutineScope

data class AppDependencies(
    val cardUseCase: CardUseCase,
    val orderUseCase: OrderUseCase,
    val autocompleteUseCase: AutocompleteUseCase,
    val coroutineScope: CoroutineScope
)
