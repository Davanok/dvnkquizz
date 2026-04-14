package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            question.media?.let { media ->
                QuestionMedia(
                    media = media,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(question.type.stringRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = question.price.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
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
            contentScale = ContentScale.Crop, // Ensures the image neatly fills the constrained bounds
            modifier = modifier
        )
        MediaKind.AUDIO -> {
            PlaceholderBox(
                text = "Audio not supported yet",
                modifier = modifier
            )
        }
        MediaKind.VIDEO -> {
            PlaceholderBox(
                text = "Video not supported yet",
                modifier = modifier
            )
        }
        MediaKind.NONE -> { /* Intentionally left blank */ }
    }
}

// Added a quick helper for your placeholders to keep the layout consistent
@Composable
private fun PlaceholderBox(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}