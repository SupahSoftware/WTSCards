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

    fun onPriceChanged(priceText: String) {
        // Allow digits and at most one decimal point
        val filtered = priceText.filter { it.isDigit() || it == '.' }
        val parts = filtered.split(".")
        val result = if (parts.size > 2) {
            parts[0] + "." + parts.drop(1).joinToString("")
        } else {
            filtered
        }
        uiState = uiState.copy(priceText = result)
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
                
                // Convert price to pennies if provided
                val priceInPennies = if (uiState.priceText.isNotBlank()) {
                    ((uiState.priceText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                } else {
                    0L
                }

                // Create separate card instances for each quantity
                val cards = List(quantity) {
                    Card(
                        id = UUID.randomUUID().toString(),
                        sportsCardProId = null,
                        name = buildCardName(),
                        setName = uiState.setName.toTitleCase(),
                        priceInPennies = priceInPennies,
                        gradedString = uiState.gradeOption,
                        priceSold = null
                    )
                }

                cardUseCase.addCards(cards)

                val successMessage = if (quantity == 1) {
                    "Card added successfully"
                } else {
                    "$quantity cards added successfully"
                }

                uiState = defaultState.copy(
                    toastMessage = ToastMessage(successMessage, isError = false)
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
