package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.game.entities.Participant
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import com.davanok.dvnkquizz.core.domain.game.entities.SessionAnswer
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.answer
import dvnkquizz.sharedui.generated.resources.answering
import dvnkquizz.sharedui.generated.resources.correct_answer
import dvnkquizz.sharedui.generated.resources.ic_check
import dvnkquizz.sharedui.generated.resources.ic_close
import dvnkquizz.sharedui.generated.resources.incorrect_answer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun AnsweringScreen(
    isHost: Boolean,
    question: Question,
    participant: Participant,
    answer: SessionAnswer,
    judgeAnswer: (Uuid, isCorrect: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ParticipantCard(
            participant = participant,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(0.5f)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.answering),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isHost) {
            Spacer(Modifier.height(24.dp))

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
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

            Spacer(Modifier.height(24.dp))

            HostJudgingButtons(
                judgeAnswer = { judgeAnswer(answer.id, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            )
        }
    }
}
@Composable
private fun HostJudgingButtons(
    judgeAnswer: (isCorrect: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { judgeAnswer(false) },
            modifier = Modifier
                .weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.incorrect_answer))
        }

        Button(
            onClick = { judgeAnswer(true) },
            modifier = Modifier
                .weight(1f)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.correct_answer))
        }
    }
}