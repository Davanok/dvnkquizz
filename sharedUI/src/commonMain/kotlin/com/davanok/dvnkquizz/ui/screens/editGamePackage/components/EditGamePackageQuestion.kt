package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionMedia
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.enums.QuestionType
import com.davanok.dvnkquizz.ui.screens.editGamePackage.GamePackageLimits
import com.davanok.dvnkquizz.ui.utils.enumStrings.stringRes
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.cancel
import dvnkquizz.sharedui.generated.resources.change_question_media
import dvnkquizz.sharedui.generated.resources.ic_delete
import dvnkquizz.sharedui.generated.resources.ic_edit
import dvnkquizz.sharedui.generated.resources.ic_error
import dvnkquizz.sharedui.generated.resources.ic_perm_media
import dvnkquizz.sharedui.generated.resources.question_answer_label
import dvnkquizz.sharedui.generated.resources.question_no_media_selected_placeholder
import dvnkquizz.sharedui.generated.resources.question_price_label
import dvnkquizz.sharedui.generated.resources.question_text_label
import dvnkquizz.sharedui.generated.resources.question_type_label
import dvnkquizz.sharedui.generated.resources.remove_question_media
import dvnkquizz.sharedui.generated.resources.save
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditGamePackageQuestionDialog(
    question: Question,
    questionMediaErrorMessage: String?,
    openMediaSelector: () -> Unit,
    removeMedia: () -> Unit,
    onSave: (Question) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Content(
                question = question,
                questionMediaErrorMessage = questionMediaErrorMessage,
                openMediaSelector = openMediaSelector,
                removeMedia = removeMedia,
                onSave = onSave,
                onDismissRequest = onDismissRequest,
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun Content(
    question: Question,
    questionMediaErrorMessage: String?,
    openMediaSelector: () -> Unit,
    removeMedia: () -> Unit,
    onSave: (Question) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var questionText by remember { mutableStateOf(question.questionText) }
    var answerText by remember { mutableStateOf(question.answerText) }
    var price by remember { mutableIntStateOf(question.price) }
    var type by remember { mutableStateOf(question.type) }

    val changed by remember {
        derivedStateOf {
            questionText != question.questionText ||
                    answerText != question.answerText ||
                    price != question.price ||
                    type != question.type
        }
    }

    Column(modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = questionText,
                onValueChange = { text ->
                    questionText = text.take(GamePackageLimits.QUESTION_TEXT_MAX_LENGTH)
                                },
                label = { Text(stringResource(Res.string.question_text_label)) },
                supportingText = textLengthLimitText(questionText.length, GamePackageLimits.QUESTION_TEXT_MAX_LENGTH),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = answerText,
                onValueChange = {  text ->
                    answerText = text.take(GamePackageLimits.QUESTION_ANSWER_MAX_LENGTH)
                                },
                label = { Text(stringResource(Res.string.question_answer_label)) },
                supportingText = textLengthLimitText(
                    answerText.length,
                    GamePackageLimits.QUESTION_ANSWER_MAX_LENGTH
                ),
                modifier = Modifier.fillMaxWidth()
            )

            MediaSelectorCard(
                media = question.media,
                errorMessage = questionMediaErrorMessage,
                onChangeMedia = openMediaSelector,
                onRemoveMedia = removeMedia,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = price.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()
                            ?.takeIf { it in GamePackageLimits.QUESTION_PRICE_RANGE }
                            ?.let { price = it }
                    },
                    label = { Text(stringResource(Res.string.question_price_label)) },
                    modifier = Modifier.weight(1f)
                )
                QuestionTypeSelector(
                    value = type,
                    onValueChanged = { type = it }
                )
            }
        }

        HorizontalDivider()

        FlowRow(
            modifier = Modifier.align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = {
                    if (changed) {
                        val q = question.copy(
                            questionText = questionText,
                            answerText = answerText,
                            price = price,
                            type = type
                        )
                        onSave(q)
                    }
                    onDismissRequest()
                },
                enabled = changed
            ) {
                Text(stringResource(Res.string.save))
            }

            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(Res.string.cancel))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionTypeSelector(
    value: QuestionType,
    onValueChanged: (QuestionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = stringResource(value.stringRes),
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(Res.string.question_type_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            QuestionType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(stringResource(type.stringRes)) },
                    onClick = { onValueChanged(type) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaSelectorCard(
    media: QuestionMedia?,
    errorMessage: String?,
    onChangeMedia: () -> Unit,
    onRemoveMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(enabled = media == null, onClick = onChangeMedia)
    ) {
        val modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(vertical = 16.dp, horizontal = 24.dp)

        when {
            errorMessage != null -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_error),
                    contentDescription = null
                )
                Text(
                    text = errorMessage,
                    textAlign = TextAlign.Center
                )
            }

            media == null -> MediaSelectorPlaceholder(modifier = modifier)

            media.progress < 1f -> CircularWavyProgressIndicator(
                progress = { media.progress },
                modifier = modifier
            )

            else -> QuestionMediaContent(
                media = media,
                onChangeClick = onChangeMedia,
                onRemoveClick = onRemoveMedia,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardDefaults.shape)
            )
        }
    }
}

@Composable
private fun MediaSelectorPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_perm_media),
            contentDescription = null
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.question_no_media_selected_placeholder),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuestionMediaContent(
    media: QuestionMedia,
    onChangeClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (media.kind) {
            MediaKind.IMAGE -> AsyncImage(
                model = media.url,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().align(Alignment.Center)
            )

            MediaKind.AUDIO -> Text("MediaKind.Audio not supported yet")
            MediaKind.VIDEO -> Text("MediaKind.Video not supported yet")
            MediaKind.NONE -> error("MediaKind.None not allowed there")
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(MaterialTheme.colorScheme.primary),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onRemoveClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = stringResource(Res.string.remove_question_media),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onChangeClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_edit),
                    contentDescription = stringResource(Res.string.change_question_media)
                )
            }
        }
    }
}