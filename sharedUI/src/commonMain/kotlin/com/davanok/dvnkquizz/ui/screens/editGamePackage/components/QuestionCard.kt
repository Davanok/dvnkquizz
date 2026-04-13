package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionMedia
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.ui.utils.enumStrings.stringRes
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuestionCard(
    question: Question,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(
            text = question.questionText,
            style = MaterialTheme.typography.labelMedium
        )

        question.media?.let { media ->
            QuestionMedia(
                media = media,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
            )
        }

        Text(text = question.price.toString())

        Text(text = stringResource(question.type.stringRes))
    }
}

@Composable
private fun QuestionMedia(
    media: QuestionMedia,
    modifier: Modifier = Modifier
) {
    when (media.kind) {
        MediaKind.IMAGE -> AsyncImage(
            model = media.url,
            contentDescription = null,
            modifier = modifier
        )
        MediaKind.AUDIO -> {
            Text(text = "Audio not supported yet") // TODO
        }
        MediaKind.VIDEO -> {
            Text(text = "Audio not supported yet") // TODO
        }
        MediaKind.NONE -> {  }
    }
}