package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component3
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.ui.screens.editGamePackage.EditGamePackageUiEvent
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.add
import dvnkquizz.sharedui.generated.resources.add_category_dialog_title
import dvnkquizz.sharedui.generated.resources.add_category_to_round
import dvnkquizz.sharedui.generated.resources.add_question_to_category
import dvnkquizz.sharedui.generated.resources.add_round
import dvnkquizz.sharedui.generated.resources.add_round_dialog_title
import dvnkquizz.sharedui.generated.resources.cancel
import dvnkquizz.sharedui.generated.resources.ic_add
import dvnkquizz.sharedui.generated.resources.ic_arrow_down
import dvnkquizz.sharedui.generated.resources.package_description_text_field_label
import dvnkquizz.sharedui.generated.resources.package_title_text_field_label
import dvnkquizz.sharedui.generated.resources.round_title
import dvnkquizz.sharedui.generated.resources.title_cannot_be_empty
import dvnkquizz.sharedui.generated.resources.title_text_field_label
import dvnkquizz.sharedui.generated.resources.toggle_round_expand
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun EditGamePackageContent(
    gamePackage: FullGamePackage,
    eventSink: (EditGamePackageUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddRoundDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf<Uuid?>(null) }

    Content(
        gamePackage = gamePackage,
        onTitleChanged = { eventSink(EditGamePackageUiEvent.SetTitle(it)) },
        onDescriptionChanged = { eventSink(EditGamePackageUiEvent.SetDescription(it)) },
        onDifficultyChanged = { eventSink(EditGamePackageUiEvent.SetDifficulty(it)) },
        onAddRound = { showAddRoundDialog = true },
        onAddCategoryToRound = { showAddCategoryDialog = it },
        onAddQuestion = { eventSink(EditGamePackageUiEvent.NewQuestion(it)) },
        onQuestionClick = { eventSink(EditGamePackageUiEvent.EditQuestion(it)) },
        modifier = modifier
    )

    if (showAddRoundDialog)
        AddDialog(
            title = { Text(stringResource(Res.string.add_round_dialog_title)) },
            onDismissRequest = { name ->
                if (name != null)
                    eventSink(EditGamePackageUiEvent.AddRound(name))
                showAddRoundDialog = false
            }
        )

    showAddCategoryDialog?.let { roundId ->
        AddDialog(
            title = { Text(stringResource(Res.string.add_category_dialog_title)) },
            onDismissRequest = { name ->
                if (name != null)
                    eventSink(EditGamePackageUiEvent.AddCategory(roundId, name))
                showAddCategoryDialog = null
            }
        )
    }
}

@Composable
private fun Content(
    gamePackage: FullGamePackage,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDifficultyChanged: (Int) -> Unit,
    onAddRound: () -> Unit,
    onAddCategoryToRound: (roundId: Uuid) -> Unit,
    onAddQuestion: (categoryId: Uuid) -> Unit,
    onQuestionClick: (questionId: Uuid) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val (title, difficulty, description) = remember { FocusRequester.createRefs() }

    val collapsedRounds = rememberSaveable { mutableStateSetOf<Uuid>() }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = gamePackage.title,
                    onValueChange = onTitleChanged,
                    label = { Text(stringResource(Res.string.package_title_text_field_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { difficulty.requestFocus() }),
                    modifier = Modifier.weight(2 / 3f).focusRequester(title)
                )

                OutlinedTextField(
                    value = gamePackage.difficulty.toString(),
                    onValueChange = { it.toIntOrNull()?.let(onDifficultyChanged) },
                    label = { Text(stringResource(Res.string.package_description_text_field_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { description.requestFocus() }),
                    modifier = Modifier.weight(1 / 3f).focusRequester(difficulty)
                )
            }
        }
        item {
            OutlinedTextField(
                value = gamePackage.description,
                onValueChange = onDescriptionChanged,
                label = { Text(stringResource(Res.string.package_description_text_field_label)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth().focusRequester(description)
            )
        }

        gamePackage.rounds.forEach { round ->
            val isExpanded = round.id !in collapsedRounds

            stickyHeader(
                key = "round: ${round.id}",
                contentType = "round header"
            ) {
                RoundHeader(
                    roundOrdinal = round.ordinal,
                    roundTitle = round.name,
                    isExpanded = isExpanded,
                    onExpandChanged = { expand ->
                        if (expand) collapsedRounds.remove(round.id)
                        else collapsedRounds.add(round.id)
                    },
                    onAddCategoryClick = { onAddCategoryToRound(round.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isExpanded)
                items(
                    items = round.categories,
                    key = { "round: ${it.id}" },
                    contentType = { "category" }
                ) { category ->
                    CategoryListItem(
                        category = category,
                        onQuestionClick = onQuestionClick,
                        onAddQuestionClick = { onAddQuestion(category.id) },
                        modifier = Modifier.fillMaxWidth().animateItem()
                    )
                }
        }

        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onAddRound,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(Res.string.add_round),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundHeader(
    roundOrdinal: Int,
    roundTitle: String,
    isExpanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    onAddCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onExpandChanged(!isExpanded) }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_down),
                    contentDescription = stringResource(Res.string.toggle_round_expand),
                    modifier = Modifier.rotate(if (isExpanded) 0f else 180f)
                )
            }

            Text(
                text = stringResource(Res.string.round_title, roundOrdinal, roundTitle),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onAddCategoryClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = stringResource(Res.string.add_category_to_round)
                )
            }
        }
    }
}

@Composable
private fun CategoryListItem(
    category: FullGameCategory,
    onQuestionClick: (Uuid) -> Unit,
    onAddQuestionClick: () -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Row(Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = category.name,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onAddQuestionClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = stringResource(Res.string.add_question_to_category)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = category.questions,
                key = { it.id }
            ) { question ->
                QuestionCard(
                    question = question,
                    onClick = { onQuestionClick(question.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDialog(
    title: @Composable () -> Unit,
    onDismissRequest: (String?) -> Unit
) {
    var newName by remember { mutableStateOf("") }

    var textFieldError by remember { mutableStateOf(false) }
    val onDismiss = {
        if (newName.isBlank()) textFieldError = true
        else onDismissRequest(newName)
    }

    AlertDialog(
        onDismissRequest = { onDismissRequest(null) },
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
                } else null
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest(null) }) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}