package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.davanok.dvnkquizz.core.domain.entities.Question

@Composable
fun AnswerScreen(
    isHost: Boolean,
    question: Question,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        QuestionContent(question)
        Text(text = question.answerText)
        if (isHost) {
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "next question")
            }
        }
    }
}