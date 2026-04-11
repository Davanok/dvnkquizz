package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import com.davanok.dvnkquizz.core.domain.entities.GameCategory
import com.davanok.dvnkquizz.core.domain.entities.GameRound
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.add
import dvnkquizz.sharedui.generated.resources.add_category_dialog_title
import dvnkquizz.sharedui.generated.resources.add_round_dialog_title
import dvnkquizz.sharedui.generated.resources.cancel
import dvnkquizz.sharedui.generated.resources.title_cannot_be_empty
import dvnkquizz.sharedui.generated.resources.title_text_field_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditRoundDialog(
    round: GameRound,
    onSave: (GameRound) -> Unit,
    onDismissRequest: () -> Unit
) {
    EditDialog(
        title = { Text(stringResource(Res.string.add_round_dialog_title)) },
        oldName = round.name,
        onSave = { onSave(round.copy(name = it)) },
        onDismissRequest = onDismissRequest
    )
}
@Composable
fun EditCategoryDialog(
    category: GameCategory,
    onSave: (GameCategory) -> Unit,
    onDismissRequest: () -> Unit
) {
    EditDialog(
        title = { Text(stringResource(Res.string.add_category_dialog_title)) },
        oldName = category.name,
        onSave = { onSave(category.copy(name = it)) },
        onDismissRequest = onDismissRequest
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDialog(
    title: @Composable () -> Unit,
    oldName: String = "",
    onSave: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var newName by remember { mutableStateOf(oldName) }

    var textFieldError by remember { mutableStateOf(false) }
    val onDismiss = {
        when {
            newName.isBlank() -> textFieldError = true
            newName == oldName -> onDismissRequest()
            else -> {
                onSave(newName)
                onDismissRequest()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(Res.string.title_text_field_label)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDismiss() }),
                isError = textFieldError,
                supportingText = if (textFieldError) {
                    {
                        Text(
                            text = stringResource(Res.string.title_cannot_be_empty),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else null,
                singleLine = true
            )
        },
        confirmButton = {
            val buttonEnabled by remember(oldName, newName) {
                derivedStateOf { newName.isNotBlank() && newName != oldName }
            }
            TextButton(
                onClick = onDismiss,
                enabled = buttonEnabled
            ) {
                Text(stringResource(Res.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}