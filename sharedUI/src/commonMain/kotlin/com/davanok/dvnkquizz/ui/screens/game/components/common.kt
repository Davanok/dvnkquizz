package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.enums.ParticipantRole
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.ic_error
import dvnkquizz.sharedui.generated.resources.ic_person
import org.jetbrains.compose.resources.painterResource

@Composable
fun ParticipantCard(
    participant: Participant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = when (participant.role) {
            ParticipantRole.HOST -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            ParticipantRole.PLAYER -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ParticipantRole.SPECTATOR -> null
        }
    ) {
        ParticipantImage(
            imageUrl = participant.user.image,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Text(
            text = participant.user.nickname,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
        if (participant.role == ParticipantRole.PLAYER) {
            ParticipantScore(
                score = participant.score,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ParticipantImage(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        when {
            imageUrl == null -> Icon(
                painter = painterResource(Res.drawable.ic_person),
                contentDescription = "profile icon"
            )
            else -> AsyncImage(
                model = imageUrl,
                contentDescription = "profile image",
                contentScale = ContentScale.Crop,
                error = painterResource(Res.drawable.ic_error)
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
            // Compare the new score with the old one to decide direction
            if (targetState > initialState) {
                // Score increased: Slide in from bottom, slide out to top
                (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
            } else {
                // Score decreased: Slide in from top, slide out to bottom
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
fun QuestionContent(
    question: Question,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
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