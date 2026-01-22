package com.wtscards.ui.screens.addcard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Card
import com.wtscards.domain.usecase.CardUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class AddCardViewModel(
    private val cardUseCase: CardUseCase,
    private val coroutineScope: CoroutineScope
) {
    private val defaultState = AddCardUiState()

    var uiState by mutableStateOf(defaultState)
        private set

    fun onNameChanged(name: String) {
        uiState = uiState.copy(name = name)
    }

    fun onCardNumberChanged(cardNumber: String) {
        uiState = uiState.copy(cardNumber = cardNumber)
    }

    fun onSetNameChanged(setName: String) {
        uiState = uiState.copy(setName = setName)
    }

    fun onParallelNameChanged(parallelName: String) {
        uiState = uiState.copy(parallelName = parallelName)
    }

    fun onGradeOptionChanged(gradeOption: String) {
        uiState = uiState.copy(gradeOption = gradeOption)
    }

    fun onQuantityChanged(quantityText: String) {
        // Only allow digits
        val filtered = quantityText.filter { it.isDigit() }
        uiState = uiState.copy(quantityText = filtered)
    }

    fun canSave(): Boolean {
        val hasName = uiState.name.isNotBlank()
        val hasCardNumber = uiState.cardNumber.isNotBlank()
        val hasValidQuantity = uiState.quantityText.isNotBlank() && (uiState.quantityText.toIntOrNull() ?: 0) > 0
        return hasName && hasCardNumber && hasValidQuantity && !uiState.isSaving
    }

    fun onSave() {
        if (!canSave()) return

        uiState = uiState.copy(isSaving = true)

        coroutineScope.launch {
            try {
                val quantity = uiState.quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1

                val card = Card(
                    id = UUID.randomUUID().toString(),
                    sportsCardProId = null,
                    name = buildCardName(),
                    setName = uiState.setName.toTitleCase(),
                    priceInPennies = 0L,
                    gradedString = uiState.gradeOption,
                    quantity = quantity
                )

                cardUseCase.addCard(card)

                uiState = defaultState.copy(
                    toastMessage = ToastMessage("Card added successfully", isError = false)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    toastMessage = ToastMessage(e.message ?: "Failed to add card", isError = true)
                )
            }
        }
    }

    private fun buildCardName(): String {
        val parts = mutableListOf<String>()
        parts.add(uiState.name.toTitleCase())
        if (uiState.cardNumber.isNotBlank()) {
            parts.add("#${uiState.cardNumber.uppercase()}")
        }
        if (uiState.parallelName.isNotBlank()) {
            parts.add(uiState.parallelName.toTitleCase())
        }
        return parts.joinToString(" ")
    }

    private fun String.toTitleCase(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    fun clearToast() {
        uiState = uiState.copy(toastMessage = null)
    }
}
