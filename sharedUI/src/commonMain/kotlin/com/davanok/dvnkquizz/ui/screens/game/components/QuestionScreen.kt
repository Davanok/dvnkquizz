package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.enums.MediaKind

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuestionScreen(
    isHost: Boolean,
    onBuzz: () -> Unit,
    showQuestionIn: Int?,
    question: Question,
    modifier: Modifier = Modifier
) {
    if (showQuestionIn != null) {
        if (showQuestionIn > 0) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                AnimatedContent(showQuestionIn) { showQuestionIn ->
                    Text(
                        text = showQuestionIn.toString(),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
        }
        else {
            QuestionContent(isHost, onBuzz, question, modifier)
        }
    }
    else {
        val media = question.media
        if (media != null && media.progress < 1f) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoadingIndicator(progress = { media.progress })
                    Text(text = (media.progress * 100).toInt().toString())
                }
            }
        }
        else {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(text = "waiting other participants")
            }
        }
    }
}

@Composable
private fun QuestionContent(
    isHost: Boolean,
    onBuzz: () -> Unit,
    question: Question,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column {
                when (question.media?.kind) {
                    MediaKind.IMAGE -> AsyncImage(
                        model = question.media!!.url,
                        contentDescription = null,
                        onError = {
                            Logger.e(it.result.throwable) { "failed to render image" }
                        }
                    )
                    MediaKind.AUDIO -> TODO()
                    MediaKind.VIDEO -> TODO()
                    MediaKind.NONE,
                    null -> {}
                }

                Text(text = question.questionText)
            }
        }
        if (!isHost) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBuzz
            ) {
                Text(text = "Buzz")
            }
        }
    }
}