package com.wtscards.ui.screens.addcard

data class AddCardUiState(
    val name: String = "",
    val cardNumber: String = "",
    val setName: String = "",
    val parallelName: String = "",
    val gradeOption: String = "Ungraded",
    val quantityText: String = "1",
    val priceText: String = "",
    val isSaving: Boolean = false,
    val toastMessage: ToastMessage? = null,
    val nameSuggestions: List<String> = emptyList(),
    val setNameSuggestions: List<String> = emptyList(),
    val parallelNameSuggestions: List<String> = emptyList()
)

data class ToastMessage(
    val message: String,
    val isError: Boolean
)

val gradeOptions = listOf("Ungraded") + (20 downTo 0).map { it / 2.0 }.map { grade ->
    if (grade == grade.toLong().toDouble()) {
        grade.toLong().toString()
    } else {
        grade.toString()
    }
}
