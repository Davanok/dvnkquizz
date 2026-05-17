package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.game.entities.Participant
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import com.davanok.dvnkquizz.core.domain.game.enums.ParticipantRole
import com.davanok.dvnkquizz.core.domain.gamePackage.enums.MediaKind
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.answer
import dvnkquizz.sharedui.generated.resources.ic_error
import dvnkquizz.sharedui.generated.resources.ic_person
import dvnkquizz.sharedui.generated.resources.no_profile_image
import dvnkquizz.sharedui.generated.resources.profile_image
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParticipantCard(
    participant: Participant,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedCard(
            border =
                if (participant.isReady)
                    CardDefaults.outlinedCardBorder()
                else
                    CardDefaults.outlinedCardBorder()
                        .copy(brush = SolidColor(MaterialTheme.colorScheme.secondary)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ParticipantImage(
                    imageUrl = participant.user.image,
                    modifier = Modifier
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = participant.user.nickname,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(modifier = Modifier.height(16.dp)) {
                    if (participant.role == ParticipantRole.PLAYER) {
                        ParticipantScore(score = participant.score)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        Badge(
            containerColor = when (participant.role) {
                ParticipantRole.HOST -> MaterialTheme.colorScheme.secondary
                ParticipantRole.PLAYER -> MaterialTheme.colorScheme.primary
                ParticipantRole.SPECTATOR -> MaterialTheme.colorScheme.outline
            },
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(text = participant.role.name)
        }
    }
}

@Composable
private fun ParticipantImage(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CardDefaults.shape,
        color = MaterialTheme.colorScheme.surfaceDim
    ) {
        if (imageUrl == null) {
            Icon(
                painter = painterResource(Res.drawable.ic_person),
                contentDescription = stringResource(Res.string.no_profile_image)
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(Res.string.profile_image),
                contentScale = ContentScale.Crop,
                error = painterResource(Res.drawable.ic_error),
                placeholder = painterResource(Res.drawable.ic_person)
            )
        }
    }
}

@Composable
private fun ParticipantScore(
    score: Int,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = score,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
            } else {
                (slideInVertically { height -> -height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> height } + fadeOut())
            } using SizeTransform(clip = false)
        },
        label = "ScoreAnimation",
        modifier = modifier
    ) { targetScore ->
        Text(
            text = targetScore.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}

@Composable
fun QuestionCard(
    question: Question,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.Center
        ) {
            question.media?.let { media ->
                when (media.kind) {
                    MediaKind.IMAGE -> AsyncImage(
                        model = media.url,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .clip(MaterialTheme.shapes.large)
                    )
                    MediaKind.AUDIO -> {
                        Text(text = "Audio not supported yet") // TODO
                    }
                    MediaKind.VIDEO -> {
                        Text(text = "Video not supported yet") // TODO
                    }
                    MediaKind.NONE -> {}
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuestionAnswerCard(
    question: Question,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(Res.string.answer),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            SelectionContainer {
                Text(
                    text = question.answerText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}