package com.wtscards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgDropdown
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary

@Composable
fun AutocompleteTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    secondaryLabel: String? = null,
    suggestions: List<String>,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isFocused by remember { mutableStateOf(false) }
    var dismissedValue by remember { mutableStateOf<String?>(null) }
    var isManuallyDismissed by remember { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0) }
    var textFieldHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    var textFieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }

    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    val showDropdown = isFocused && suggestions.isNotEmpty() && value.isNotBlank() && value != dismissedValue && !isManuallyDismissed

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

        Box {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    if (newValue.text != dismissedValue) {
                        dismissedValue = null
                    }
                    isManuallyDismissed = false
                    onValueChange(newValue.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .onGloballyPositioned { coordinates ->
                        textFieldWidth = coordinates.size.width
                        textFieldHeight = coordinates.size.height
                    }
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (!focusState.isFocused) {
                            dismissedValue = null
                        }
                    }
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Tab && showDropdown) {
                            val selected = suggestions.first()
                            textFieldValue = TextFieldValue(selected, TextRange(selected.length))
                            onValueChange(selected)
                            dismissedValue = selected
                            true
                        } else {
                            false
                        }
                    },
                placeholder = {
                    Text(
                        text = placeholder,
                        color = textTertiary
                    )
                },
                trailingIcon = if (showDropdown) {
                    {
                        Text(
                            text = "Tab to autocomplete",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textTertiary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                } else null,
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

            if (showDropdown) {
                Popup(
                    alignment = Alignment.TopStart,
                    properties = PopupProperties(focusable = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isManuallyDismissed = true
                            }
                    )
                }

                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, textFieldHeight),
                    properties = PopupProperties(focusable = false)
                ) {
                    Column(
                        modifier = Modifier
                            .then(
                                if (textFieldWidth > 0) {
                                    Modifier.width(with(density) { textFieldWidth.toDp() })
                                } else {
                                    Modifier
                                }
                            )
                            .background(bgDropdown, RoundedCornerShape(8.dp))
                    ) {
                        suggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        textFieldValue = TextFieldValue(suggestion, TextRange(suggestion.length))
                                        onValueChange(suggestion)
                                        dismissedValue = suggestion
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    color = textPrimary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
