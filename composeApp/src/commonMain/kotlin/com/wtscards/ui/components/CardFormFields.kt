package com.wtscards.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wtscards.ui.screens.addcard.gradeOptions
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary

/**
 * Reusable card form fields used by both AddCardScreen and EditCardDialog
 */
@Composable
fun CardFormFields(
    name: String,
    onNameChanged: (String) -> Unit,
    nameSuggestions: List<String>,
    cardNumber: String,
    onCardNumberChanged: (String) -> Unit,
    setName: String,
    onSetNameChanged: (String) -> Unit,
    setNameSuggestions: List<String>,
    parallelName: String,
    onParallelNameChanged: (String) -> Unit,
    parallelNameSuggestions: List<String>,
    gradeOption: String,
    onGradeOptionChanged: (String) -> Unit,
    priceText: String,
    onPriceChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name input with autocomplete
        AutocompleteTextField(
            value = name,
            onValueChange = onNameChanged,
            label = "Name",
            placeholder = "Player name",
            suggestions = nameSuggestions
        )

        // Card number input
        FormTextField(
            value = cardNumber,
            onValueChange = onCardNumberChanged,
            label = "Card Number",
            placeholder = "e.g., 123 or FT-3"
        )

        // Set name input (optional) with autocomplete
        AutocompleteTextField(
            value = setName,
            onValueChange = onSetNameChanged,
            label = "Set Name",
            secondaryLabel = "Optional",
            placeholder = "e.g., 2023 Topps Chrome",
            suggestions = setNameSuggestions
        )

        // Parallel name input (optional) with autocomplete
        AutocompleteTextField(
            value = parallelName,
            onValueChange = onParallelNameChanged,
            label = "Parallel Name",
            secondaryLabel = "Optional",
            placeholder = "e.g., Red Raywave",
            suggestions = parallelNameSuggestions
        )

        // Grade and Price row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Grade dropdown
            GradeDropdown(
                modifier = Modifier.weight(1f),
                selectedGrade = gradeOption,
                onGradeSelected = onGradeOptionChanged
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Price input (optional)
            FormTextField(
                modifier = Modifier.weight(1f),
                value = priceText,
                onValueChange = onPriceChanged,
                label = "Price",
                secondaryLabel = "Optional",
                placeholder = "0.00",
                keyboardType = KeyboardType.Decimal
            )
        }
    }
}

@Composable
fun FormTextField(
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
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = textPrimary
            )
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
            placeholder = {
                Text(
                    text = placeholder,
                    color = textTertiary
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
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
fun GradeDropdown(
    modifier: Modifier = Modifier,
    selectedGrade: String,
    onGradeSelected: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "Grade",
            style = MaterialTheme.typography.bodyMedium,
            color = textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        AppDropdown(
            selectedValue = selectedGrade,
            options = gradeOptions,
            onOptionSelected = onGradeSelected
        )
    }
}
