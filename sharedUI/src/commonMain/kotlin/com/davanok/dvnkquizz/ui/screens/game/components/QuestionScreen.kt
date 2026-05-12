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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.buzz
import dvnkquizz.sharedui.generated.resources.waiting_other_participants
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuestionScreen(
    isHost: Boolean,
    onBuzz: (answer: String) -> Unit,
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
                delay(100)
            } while (delta > 0)
        }
    }.value

    Box(modifier = modifier) {
        when {
            showQuestionIn == null -> ProgressBox(
                progress = question.media?.progress,
                modifier = Modifier.fillMaxSize()
            )
            showQuestionIn > 0 -> {
                if (isHost)
                    HostCountdown(
                        showQuestionIn = showQuestionIn,
                        question = question
                    )
                else
                    CountdownText(
                        seconds = showQuestionIn,
                        modifier = Modifier.fillMaxSize()
                    )
            }
            else -> Content(
                isHost = isHost,
                onBuzz = onBuzz,
                question = question
            )
        }
    }
}

@Composable
private fun HostCountdown(
    showQuestionIn: Int,
    question: Question,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CountdownText(
            seconds = showQuestionIn,
            modifier = Modifier
                .height(50.dp)
                .align(Alignment.End)
        )
        Content(
            isHost = true,
            onBuzz = {  },
            question = question
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProgressBox(progress: Float?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when  {
            progress == null -> CircularWavyProgressIndicator()
            progress < 1 -> CircularWavyProgressIndicator(progress = { progress })
            else -> Text(text = stringResource(Res.string.waiting_other_participants))
        }
    }
}

@Composable
private fun CountdownText(
    seconds: Int,
    modifier: Modifier
) {
    AnimatedContent(
        modifier = modifier,
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
@Composable
private fun Content(
    isHost: Boolean,
    onBuzz: (answer: String) -> Unit,
    question: Question,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionCard(
            question = question,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .widthIn(max = 600.dp)
        )

        if (isHost) {
            QuestionAnswerCard(
                question = question,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            )
        } else {
            PlayerSection(
                onBuzz = onBuzz
            )
        }
    }
}

@Composable
private fun PlayerSection(
    onBuzz: (answer: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var answerText by remember { mutableStateOf("") }

    TextField(
        value = answerText,
        onValueChange = { answerText = it }
    )

    Button(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onBuzz(answerText)
        }
    ) {
        Text(
            stringResource(Res.string.buzz),
            style = MaterialTheme.typography.titleLarge
        )
    }
}