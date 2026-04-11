package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component3
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.ui.screens.editGamePackage.EditGamePackageDialogRequest
import com.davanok.dvnkquizz.ui.screens.editGamePackage.EditGamePackageUiEvent
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.add_category_to_round
import dvnkquizz.sharedui.generated.resources.add_question_to_category
import dvnkquizz.sharedui.generated.resources.add_round
import dvnkquizz.sharedui.generated.resources.ic_add
import dvnkquizz.sharedui.generated.resources.ic_arrow_down
import dvnkquizz.sharedui.generated.resources.package_description_text_field_label
import dvnkquizz.sharedui.generated.resources.package_title_text_field_label
import dvnkquizz.sharedui.generated.resources.round_title
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
        onAddCategoryToRound = {
            eventSink(
                EditGamePackageUiEvent.ShowDialog(
                    EditGamePackageDialogRequest.AddCategory(it)
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

        items(
            items = gamePackage.rounds,
            key = { it.id }
        ) { round ->
            val isExpanded = round.id !in collapsedRounds

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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

                    round.categories.forEach { category ->
                        HorizontalDivider()

                        CategoryListItem(
                            category = category,
                            onQuestionClick = onQuestionClick,
                            onAddQuestionClick = { onAddQuestion(category.id) },
                            modifier = Modifier.fillMaxWidth().animateItem()
                        )
                    }
                }
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
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onExpandChanged(!isExpanded) }
        ) {
            val rotation by animateFloatAsState(if (isExpanded) 0f else -90f)
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_down),
                contentDescription = stringResource(Res.string.toggle_round_expand),
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
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

@Composable
private fun CategoryListItem(
    category: FullGameCategory,
    onQuestionClick: (Uuid) -> Unit,
    onAddQuestionClick: () -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Row {
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