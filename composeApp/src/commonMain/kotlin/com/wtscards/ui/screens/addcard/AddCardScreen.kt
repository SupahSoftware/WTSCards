package com.wtscards.ui.screens.addcard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgSecondary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.borderInput
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
    onSave: () -> Unit,
    canSave: Boolean,
    onClearToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add New Card",
                style = MaterialTheme.typography.headlineMedium,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name input
            FormTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                label = "Name",
                placeholder = "Player name"
            )

            // Card number input
            FormTextField(
                value = uiState.cardNumber,
                onValueChange = onCardNumberChanged,
                label = "Card Number",
                placeholder = "e.g., 123 or FT-3"
            )

            // Set name input (optional)
            FormTextField(
                value = uiState.setName,
                onValueChange = onSetNameChanged,
                label = "Set Name",
                secondaryLabel = "Optional",
                placeholder = "e.g., 2023 Topps Chrome"
            )

            // Parallel name input (optional)
            FormTextField(
                value = uiState.parallelName,
                onValueChange = onParallelNameChanged,
                label = "Parallel Name",
                secondaryLabel = "Optional",
                placeholder = "e.g., Red Raywave or Image Variation or Image Variation Red"
            )

            // wrap grade dropdown and quntity input in a row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Grade dropdown
                GradeDropdown(
                    modifier = Modifier.weight(1f),
                    selectedGrade = uiState.gradeOption,
                    onGradeSelected = onGradeOptionChanged
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Quantity input
                FormTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.quantityText,
                    onValueChange = onQuantityChanged,
                    label = "Quantity",
                    placeholder = "1",
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
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

        // Toast message
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
                focusedBorderColor = accentPrimary,
                unfocusedBorderColor = borderInput,
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
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Grade",
            style = MaterialTheme.typography.bodyMedium,
            color = textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = selectedGrade,
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(bgSecondary)
            ) {
                gradeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = if (option == selectedGrade) accentPrimary else textPrimary
                            )
                        },
                        onClick = {
                            onGradeSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
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
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(
                color = if (isError) errorColor else successColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = message,
            color = textOnAccent,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
