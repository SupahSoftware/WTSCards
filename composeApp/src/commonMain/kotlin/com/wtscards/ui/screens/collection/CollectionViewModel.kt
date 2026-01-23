package com.wtscards.ui.screens.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Card
import com.wtscards.data.parser.CardNameParser
import com.wtscards.domain.usecase.AutocompleteUseCase
import com.wtscards.domain.usecase.CardUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val cardUseCase: CardUseCase,
    private val autocompleteUseCase: AutocompleteUseCase,
    private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(CollectionUiState())
        private set

    init {
        observeCards()
    }

    private fun observeCards() {
        cardUseCase.getAllCardsFlow()
            .onStart { uiState = uiState.copy(isLoading = true) }
            .onEach { cards ->
                uiState = uiState.copy(
                    isLoading = false,
                    allCards = cards,
                    error = null
                )
                updateDisplayedCards()
            }
            .catch { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load collection"
                )
            }
            .launchIn(coroutineScope)
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
        updateDisplayedCards()
    }

    fun onSortOptionChanged(sortOption: SortOption) {
        uiState = uiState.copy(sortOption = sortOption)
        updateDisplayedCards()
    }

    private fun updateDisplayedCards() {
        val filtered = if (uiState.searchQuery.isBlank()) {
            uiState.allCards
        } else {
            uiState.allCards.filter { card ->
                card.name.contains(uiState.searchQuery, ignoreCase = true)
            }
        }

        val sorted = when (uiState.sortOption) {
            SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            SortOption.PRICE_ASC -> filtered.sortedBy { it.priceInPennies }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { it.priceInPennies }
        }

        uiState = uiState.copy(displayedCards = sorted)
    }

    fun onRefresh() {
        coroutineScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val cards = cardUseCase.getAllCards()
                uiState = uiState.copy(
                    isLoading = false,
                    allCards = cards,
                    error = null
                )
                updateDisplayedCards()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh"
                )
            }
        }
    }

    fun toggleEditMode() {
        uiState = if (uiState.isEditMode) {
            uiState.copy(isEditMode = false, selectedCardIds = emptySet())
        } else {
            uiState.copy(isEditMode = true)
        }
    }

    fun toggleCardSelection(cardId: String) {
        val newSelection = if (cardId in uiState.selectedCardIds) {
            uiState.selectedCardIds - cardId
        } else {
            uiState.selectedCardIds + cardId
        }
        uiState = uiState.copy(selectedCardIds = newSelection)
    }

    fun showDeleteConfirmation() {
        uiState = uiState.copy(showDeleteConfirmDialog = true)
    }

    fun dismissDeleteConfirmation() {
        uiState = uiState.copy(showDeleteConfirmDialog = false)
    }

    fun confirmDelete() {
        coroutineScope.launch {
            try {
                cardUseCase.deleteCards(uiState.selectedCardIds.toList())
                uiState = uiState.copy(
                    showDeleteConfirmDialog = false,
                    isEditMode = false,
                    selectedCardIds = emptySet()
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    showDeleteConfirmDialog = false,
                    error = e.message ?: "Failed to delete cards"
                )
            }
        }
    }

    // Edit card functionality
    fun onEditCard(card: Card) {
        // Parse the card name to extract player name and parallel name
        val parsed = CardNameParser.parse(card.name)

        // Extract card number from the name (everything after #)
        val cardNumberMatch = Regex("""#(\S+)""").find(card.name)
        val cardNumber = cardNumberMatch?.groupValues?.get(1) ?: ""

        // Format price for display
        val priceText = if (card.priceInPennies > 0) {
            String.format("%.2f", card.priceInPennies / 100.0)
        } else {
            ""
        }

        uiState = uiState.copy(
            showEditCardDialog = true,
            editCardForm = EditCardFormState(
                cardId = card.id,
                name = parsed.playerName ?: "",
                cardNumber = cardNumber,
                setName = card.setName,
                parallelName = parsed.parallelName ?: "",
                gradeOption = card.gradedString,
                priceText = priceText
            )
        )
    }

    fun onDismissEditCardDialog() {
        uiState = uiState.copy(
            showEditCardDialog = false,
            editCardForm = EditCardFormState()
        )
    }

    fun onEditNameChanged(name: String) {
        uiState = uiState.copy(
            editCardForm = uiState.editCardForm.copy(name = name)
        )
        fetchEditNameSuggestions(name)
    }

    private fun fetchEditNameSuggestions(query: String) {
        coroutineScope.launch {
            val suggestions = autocompleteUseCase.getPlayerNameSuggestions(query)
            uiState = uiState.copy(
                editCardForm = uiState.editCardForm.copy(nameSuggestions = suggestions)
            )
        }
    }

    fun onEditCardNumberChanged(cardNumber: String) {
        uiState = uiState.copy(
            editCardForm = uiState.editCardForm.copy(cardNumber = cardNumber)
        )
    }

    fun onEditSetNameChanged(setName: String) {
        uiState = uiState.copy(
            editCardForm = uiState.editCardForm.copy(setName = setName)
        )
        fetchEditSetNameSuggestions(setName)
    }

    private fun fetchEditSetNameSuggestions(query: String) {
        coroutineScope.launch {
            val suggestions = autocompleteUseCase.getSetNameSuggestions(query)
            uiState = uiState.copy(
                editCardForm = uiState.editCardForm.copy(setNameSuggestions = suggestions)
            )
        }
    }

    fun onEditParallelNameChanged(parallelName: String) {
        uiState = uiState.copy(
            editCardForm = uiState.editCardForm.copy(parallelName = parallelName)
        )
        fetchEditParallelNameSuggestions(parallelName)
    }

    private fun fetchEditParallelNameSuggestions(query: String) {
        coroutineScope.launch {
            val suggestions = autocompleteUseCase.getParallelNameSuggestions(query)
            uiState = uiState.copy(
                editCardForm = uiState.editCardForm.copy(parallelNameSuggestions = suggestions)
            )
        }
    }

    fun onEditGradeOptionChanged(gradeOption: String) {
        uiState = uiState.copy(
            editCardForm = uiState.editCardForm.copy(gradeOption = gradeOption)
        )
    }

    fun onEditPriceChanged(priceText: String) {
        // Allow digits and at most one decimal point
        val filtered = priceText.filter { it.isDigit() || it == '.' }
        val parts = filtered.split(".")
        val result = if (parts.size > 2) {
            parts[0] + "." + parts.drop(1).joinToString("")
        } else {
            filtered
        }
        uiState = uiState.copy(
            editCardForm = uiState.editCardForm.copy(priceText = result)
        )
    }

    fun canSaveEditCard(): Boolean {
        val form = uiState.editCardForm
        return form.name.isNotBlank() && form.cardNumber.isNotBlank() && !form.isSaving
    }

    fun onSaveEditCard() {
        if (!canSaveEditCard()) return

        val form = uiState.editCardForm
        uiState = uiState.copy(
            editCardForm = form.copy(isSaving = true)
        )

        coroutineScope.launch {
            try {
                // Convert price to pennies if provided
                val priceInPennies = if (form.priceText.isNotBlank()) {
                    ((form.priceText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                } else {
                    0L
                }

                // Build the card name
                val cardName = buildCardName(
                    name = form.name,
                    cardNumber = form.cardNumber,
                    parallelName = form.parallelName
                )

                // Find the original card to preserve sportsCardProId and priceSold
                val originalCard = uiState.allCards.find { it.id == form.cardId }

                val updatedCard = Card(
                    id = form.cardId,
                    sportsCardProId = originalCard?.sportsCardProId,
                    name = cardName,
                    setName = form.setName.toTitleCase(),
                    priceInPennies = priceInPennies,
                    gradedString = form.gradeOption,
                    priceSold = originalCard?.priceSold
                )

                cardUseCase.updateCard(updatedCard)

                // Add autocomplete entries
                autocompleteUseCase.addPlayerName(form.name.toTitleCase())
                if (form.setName.isNotBlank()) {
                    autocompleteUseCase.addSetName(form.setName.toTitleCase())
                }
                if (form.parallelName.isNotBlank()) {
                    autocompleteUseCase.addParallelName(form.parallelName.toTitleCase())
                }

                uiState = uiState.copy(
                    showEditCardDialog = false,
                    editCardForm = EditCardFormState(),
                    toastMessage = ToastMessage("Card updated successfully", isError = false)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    editCardForm = uiState.editCardForm.copy(isSaving = false),
                    toastMessage = ToastMessage(e.message ?: "Failed to update card", isError = true)
                )
            }
        }
    }

    private fun buildCardName(name: String, cardNumber: String, parallelName: String): String {
        val parts = mutableListOf<String>()
        parts.add(name.toTitleCase())
        if (cardNumber.isNotBlank()) {
            parts.add("#${cardNumber.uppercase()}")
        }
        if (parallelName.isNotBlank()) {
            parts.add("[${parallelName.toTitleCase()}]")
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
