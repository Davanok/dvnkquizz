package com.davanok.dvnkquizz.ui.screens.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import kotlin.uuid.Uuid

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // 1. Top Bar / Scores
//            ScoreRow(participants = state.participants)

            // 2. The Game Board
            if (state.session?.currentQuestionId == null) {
                GameBoard(
                    questions = state.board,
                    onQuestionClick = { viewModel.onQuestionClicked(it) }
                )
            }
        }

        // 3. Active Question Overlay
        state.currentQuestion?.let { question ->
            QuestionOverlay(
                isHost = state.isHost,
                onBuzz = viewModel::onBuzzIn,
                onJudge = viewModel::onJudge,
                question = question,
                buzzedParticipant = state.buzzedParticipant,
                onSkip = viewModel::onSkipQuestion
            )
        }
    }
}

@Composable
fun GameBoard(questions: List<GameBoardItem>, onQuestionClick: (Uuid) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5), // Adjust based on your category count
        modifier = Modifier.padding(16.dp)
    ) {
        items(
            items = questions,
            key = { it.questionId }
        ) { question ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1.5f)
                    .clickable(enabled = !question.isAnswered) {
                        onQuestionClick(question.questionId)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (question.isAnswered) Color.DarkGray else Color.Blue
                )
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = if (question.isAnswered) "" else "${question.price}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}