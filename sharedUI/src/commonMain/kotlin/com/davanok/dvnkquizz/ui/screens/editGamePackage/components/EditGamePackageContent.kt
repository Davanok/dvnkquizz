package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.game.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.FullGamePackage
import com.davanok.dvnkquizz.ui.screens.editGamePackage.EditGamePackageDialogRequest
import com.davanok.dvnkquizz.ui.screens.editGamePackage.EditGamePackageUiEvent
import com.davanok.dvnkquizz.ui.screens.editGamePackage.GamePackageLimits
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.add_category
import dvnkquizz.sharedui.generated.resources.add_question
import dvnkquizz.sharedui.generated.resources.add_round
import dvnkquizz.sharedui.generated.resources.edit_category
import dvnkquizz.sharedui.generated.resources.edit_question
import dvnkquizz.sharedui.generated.resources.ic_add
import dvnkquizz.sharedui.generated.resources.ic_arrow_down
import dvnkquizz.sharedui.generated.resources.ic_edit
import dvnkquizz.sharedui.generated.resources.no_categories_yet
import dvnkquizz.sharedui.generated.resources.package_description_text_field_label
import dvnkquizz.sharedui.generated.resources.package_details
import dvnkquizz.sharedui.generated.resources.package_difficulty_text_field_label
import dvnkquizz.sharedui.generated.resources.package_title_text_field_label
import dvnkquizz.sharedui.generated.resources.round_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun EditGamePackageContent(
    gamePackage: FullGamePackage,
    eventSink: (EditGamePackageUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Content(
        gamePackage = gamePackage,
        onTitleChanged = { eventSink(EditGamePackageUiEvent.SetTitle(it)) },
        onDescriptionChanged = { eventSink(EditGamePackageUiEvent.SetDescription(it)) },
        onDifficultyChanged = { eventSink(EditGamePackageUiEvent.SetDifficulty(it)) },
        onAddRound = {
            eventSink(
                EditGamePackageUiEvent.ShowDialog(
                    EditGamePackageDialogRequest.AddRound
                )
            )
                     },
        onEditRound = {
            eventSink(
                EditGamePackageUiEvent.ShowDialog(
                    EditGamePackageDialogRequest.EditRound(it)
                )
            )
        },
        onAddCategoryToRound = {
            eventSink(
                EditGamePackageUiEvent.ShowDialog(
                    EditGamePackageDialogRequest.AddCategory(it)
                )
            )
        },
        onEditCategory = {
            eventSink(
                EditGamePackageUiEvent.ShowDialog(
                    EditGamePackageDialogRequest.EditCategory(it)
                )
            )
        },
        onAddQuestion = {
            eventSink(
                EditGamePackageUiEvent.ShowDialog(
                    EditGamePackageDialogRequest.AddQuestion(it)
                )
            )
        },
        onQuestionClick = {
            eventSink(
                EditGamePackageUiEvent.ShowDialog(
                    EditGamePackageDialogRequest.EditQuestion(it)
                )
            )
                          },
        modifier = modifier
    )
}
@Composable
private fun Content(
    gamePackage: FullGamePackage,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDifficultyChanged: (Int) -> Unit,
    onAddRound: () -> Unit,
    onEditRound: (Uuid) -> Unit,
    onAddCategoryToRound: (roundId: Uuid) -> Unit,
    onEditCategory: (Uuid) -> Unit,
    onAddQuestion: (categoryId: Uuid) -> Unit,
    onQuestionClick: (questionId: Uuid) -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, difficulty, description) = remember { FocusRequester.createRefs() }
    val collapsedRounds = rememberSaveable { mutableStateSetOf<Uuid>() }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp), // Overall page padding
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Package Info Section ---
        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.package_details),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                            modifier = Modifier.weight(3f).focusRequester(title),
                            supportingText = textLengthLimitText(gamePackage.title.length, GamePackageLimits.TITLE_MAX_LENGTH)
                        )

                        DifficultySelector(
                            value = gamePackage.difficulty,
                            onValueChange = onDifficultyChanged,
                            label = { Text(stringResource(Res.string.package_difficulty_text_field_label)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { description.requestFocus() }),
                            modifier = Modifier.weight(1f).focusRequester(difficulty)
                        )
                    }

                    OutlinedTextField(
                        value = gamePackage.description,
                        onValueChange = onDescriptionChanged,
                        label = { Text(stringResource(Res.string.package_description_text_field_label)) },
                        modifier = Modifier.fillMaxWidth().focusRequester(description),
                        supportingText = textLengthLimitText(gamePackage.description.length, GamePackageLimits.DESCRIPTION_MAX_LENGTH)
                    )
                }
            }
        }

        // --- Rounds Section ---
        items(
            items = gamePackage.rounds,
            key = { it.id }
        ) { round ->
            val isExpanded = round.id !in collapsedRounds

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.fillMaxWidth()) {
                    RoundHeader(
                        roundOrdinal = round.ordinal,
                        roundTitle = round.name,
                        isExpanded = isExpanded,
                        onExpandChanged = { expand ->
                            if (expand) collapsedRounds.remove(round.id)
                            else collapsedRounds.add(round.id)
                        },
                        onEditRoundClick = { onEditRound(round.id) },
                        onAddCategoryClick = { onAddCategoryToRound(round.id) }
                    )

                    AnimatedVisibility(isExpanded) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            if (round.categories.isEmpty()) {
                                Text(
                                    text = stringResource(Res.string.no_categories_yet),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            round.categories.forEach { category ->
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp))

                                CategoryListItem(
                                    category = category,
                                    onQuestionClick = onQuestionClick,
                                    onEditCategoryClick = { onEditCategory(category.id) },
                                    onAddQuestionClick = { onAddQuestion(category.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Footer Action ---
        item {
            Button(
                onClick = onAddRound,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp)
            ) {
                Icon(painterResource(Res.drawable.ic_add), null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.add_round))
            }
        }
    }
}

@Composable
private fun RoundHeader(
    roundOrdinal: Int,
    roundTitle: String,
    isExpanded: Boolean,
    onEditRoundClick: () -> Unit,
    onExpandChanged: (Boolean) -> Unit,
    onAddCategoryClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.round_title, roundOrdinal, roundTitle),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp).weight(1f)
        )

        IconButton(onClick = onAddCategoryClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = stringResource(Res.string.add_category)
            )
        }
        IconButton(onClick = onEditRoundClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_edit),
                contentDescription = stringResource(Res.string.edit_question)
            )
        }

        IconButton(onClick = { onExpandChanged(!isExpanded) }) {
            val rotation by animateFloatAsState(if (isExpanded) 0f else 180f)
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_down),
                contentDescription = null,
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            )
        }
    }
}

@Composable
private fun CategoryListItem(
    category: FullGameCategory,
    onQuestionClick: (Uuid) -> Unit,
    onEditCategoryClick: () -> Unit,
    onAddQuestionClick: () -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEditCategoryClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_edit),
                    contentDescription = stringResource(Res.string.edit_category)
                )
            }
            TextButton(onClick = onAddQuestionClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(Res.string.add_question),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(category.questions) { question ->
                QuestionCard(
                    question = question,
                    onClick = { onQuestionClick(question.id) },
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultySelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value.toString(),
            onValueChange = {  },
            readOnly = true,
            singleLine = true,
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            GamePackageLimits.DIFFICULTY_RANGE.forEach {
                DropdownMenuItem(
                    text = { Text(text = it.toString()) },
                    onClick = {
                        expanded = false
                        onValueChange(it)
                    }
                )
            }
        }
    }
}