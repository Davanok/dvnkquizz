package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuestionScreen(
    isHost: Boolean,
    onBuzz: () -> Unit,
    showQuestionAt: Instant?,
    question: Question,
    modifier: Modifier = Modifier
) {
    val showQuestionIn: Int? = produceState<Int?>(null, showQuestionAt) {
        if (showQuestionAt != null) {
            var delta: Int
            do {
                delta = (showQuestionAt - Clock.System.now()).inWholeSeconds.toInt()
                value = delta
                delay(500)
            } while (delta > 0)
        }
    }.value

    Box(modifier = modifier.padding(16.dp)) {
        if (showQuestionIn != null) {
            if (showQuestionIn > 0) CountdownOverlay(showQuestionIn)
            else QuestionContent(
                isHost = isHost,
                onBuzz = onBuzz,
                question = question
            )
        } else {
            val progress = question.media?.progress
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when  {
                    progress == null -> CircularWavyProgressIndicator()
                    progress < 1 -> CircularWavyProgressIndicator(progress = { progress })
                    else -> Text(text = "Waiting other participants")
                }
            }
        }
    }
}

@Composable
private fun CountdownOverlay(seconds: Int) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = seconds,
            transitionSpec = {
                (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
            }
        ) { targetCount ->
            Text(
                text = targetCount.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
@Composable
private fun QuestionContent(
    isHost: Boolean,
    onBuzz: () -> Unit,
    question: Question,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
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

        Spacer(Modifier.height(24.dp))

        if (!isHost) {
            val haptic = LocalHapticFeedback.current
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBuzz()
                }
            ) {
                Text("BUZZ", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}