package com.wtscards.ui.screens.addcard

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wtscards.ui.components.AppDropdown
import com.wtscards.ui.components.AutocompleteTextField
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.errorColor
import com.wtscards.ui.theme.successColor
import com.wtscards.ui.theme.textOnAccent
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary
import kotlinx.coroutines.delay

@Composable
fun AddCardScreen(
        uiState: AddCardUiState,
        onNameChanged: (String) -> Unit,
        onCardNumberChanged: (String) -> Unit,
        onSetNameChanged: (String) -> Unit,
        onParallelNameChanged: (String) -> Unit,
        onGradeOptionChanged: (String) -> Unit,
        onQuantityChanged: (String) -> Unit,
        onPriceChanged: (String) -> Unit,
        onSave: () -> Unit,
        canSave: Boolean,
        onClearToast: () -> Unit,
        modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 48.dp)
                                .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                    text = "Add New Card",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            AutocompleteTextField(
                    value = uiState.name,
                    onValueChange = onNameChanged,
                    label = "Name",
                    placeholder = "Player name",
                    suggestions = uiState.nameSuggestions
            )

            FormTextField(
                    value = uiState.cardNumber,
                    onValueChange = onCardNumberChanged,
                    label = "Card Number",
                    placeholder = "e.g., 123 or FT-3"
            )

            AutocompleteTextField(
                    value = uiState.setName,
                    onValueChange = onSetNameChanged,
                    label = "Set Name",
                    secondaryLabel = "Optional",
                    placeholder = "e.g., 2023 Topps Chrome",
                    suggestions = uiState.setNameSuggestions
            )

            AutocompleteTextField(
                    value = uiState.parallelName,
                    onValueChange = onParallelNameChanged,
                    label = "Parallel Name",
                    secondaryLabel = "Optional",
                    placeholder = "e.g., Red Raywave or Image Variation or Image Variation Red",
                    suggestions = uiState.parallelNameSuggestions
            )

            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GradeDropdown(
                        modifier = Modifier.weight(1f),
                        selectedGrade = uiState.gradeOption,
                        onGradeSelected = onGradeOptionChanged
                )

                Spacer(modifier = Modifier.width(16.dp))

                FormTextField(
                        modifier = Modifier.weight(1f),
                        value = uiState.quantityText,
                        onValueChange = onQuantityChanged,
                        label = "Quantity",
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.width(16.dp))

                FormTextField(
                        modifier = Modifier.weight(1f),
                        value = uiState.priceText,
                        onValueChange = onPriceChanged,
                        label = "Price",
                        secondaryLabel = "Optional",
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                    onClick = onSave,
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = accentPrimary,
                                    contentColor = textOnAccent,
                                    disabledContainerColor = bgSurface,
                                    disabledContentColor = textTertiary
                            ),
                    shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                        text = if (uiState.isSaving) "Saving..." else "Save Card",
                        style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxSize().padding(end = 4.dp),
                style =
                        defaultScrollbarStyle()
                                .copy(
                                        unhoverColor = accentPrimary.copy(alpha = 0.4f),
                                        hoverColor = accentPrimary.copy(alpha = 0.7f)
                                )
        )

        uiState.toastMessage?.let { toast ->
            ToastMessage(
                    message = toast.message,
                    isError = toast.isError,
                    onDismiss = onClearToast,
                    modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun FormTextField(
        modifier: Modifier = Modifier,
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        placeholder: String,
        secondaryLabel: String? = null,
        keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = textPrimary)
            if (secondaryLabel != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                        text = secondaryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text(text = placeholder, color = textTertiary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = accentPrimary,
                                focusedContainerColor = bgSurface,
                                unfocusedContainerColor = bgSurface
                        ),
                shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun GradeDropdown(
        modifier: Modifier = Modifier,
        selectedGrade: String,
        onGradeSelected: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(text = "Grade", style = MaterialTheme.typography.bodyMedium, color = textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        AppDropdown(
                selectedValue = selectedGrade,
                options = gradeOptions,
                onOptionSelected = onGradeSelected
        )
    }
}

@Composable
private fun ToastMessage(
        message: String,
        isError: Boolean,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
) {
    LaunchedEffect(message) {
        delay(3000)
        onDismiss()
    }

    Box(
            modifier =
                    modifier.padding(16.dp)
                            .fillMaxWidth()
                            .background(
                                    color = if (isError) errorColor else successColor,
                                    shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
    ) { Text(text = message, color = textOnAccent, style = MaterialTheme.typography.bodyMedium) }
}
