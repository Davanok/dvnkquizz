package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.next_question
import org.jetbrains.compose.resources.stringResource

@Composable
fun AnswerScreen(
    isHost: Boolean,
    question: Question,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuestionCard(
            question = question,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .widthIn(600.dp)
        )
        QuestionAnswerCard(
            question = question,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .widthIn(600.dp)
        )
        if (isHost) {
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.next_question))
            }
        }
    }
}