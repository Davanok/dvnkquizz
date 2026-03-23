package com.davanok.dvnkquizz.ui.screens.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.davanok.dvnkquizz.ui.screens.game.components.FatalErrorScreen
import com.davanok.dvnkquizz.ui.screens.game.components.IdleScreen
import com.davanok.dvnkquizz.ui.screens.game.components.LoadingScreen
import com.davanok.dvnkquizz.ui.screens.game.components.QuestionScreen
import com.davanok.dvnkquizz.ui.screens.game.components.SelectQuestionScreen
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource

@Composable
fun GameScreen(
    navigateBack: () -> Unit,
    viewModel: GameViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Content(
        state = state,
        eventSink = viewModel::eventSink,
        navigateBack = {
            viewModel.eventSink(GameScreenUiEvent.Leave)
            navigateBack()
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: GameScreenUiState,
    eventSink: (GameScreenUiEvent) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = state.gamePackage?.title ?: "Unknown") },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "back"
                        )
                    }
                },
                actions = {
                    if (state.isHost) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Host") }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val modifier = Modifier.fillMaxSize()

        AnimatedContent(
            modifier = Modifier.padding(paddingValues),
            targetState = state
        ) { state ->
            when (state) {
                GameScreenUiState.Loading -> LoadingScreen(modifier = modifier)
                is GameScreenUiState.FatalError -> FatalErrorScreen(message = state.message, modifier = modifier)
                is GameScreenUiState.Idle -> IdleScreen(
                    isHost = state.isHost,
                    onNextRound = { eventSink(GameScreenUiEvent.NextRound) },
                    participants = state.participants,
                    modifier = modifier
                )
                is GameScreenUiState.SelectQuestion -> SelectQuestionScreen(
                    isHost = state.isHost,
                    onSelectQuestion = { eventSink(GameScreenUiEvent.SelectQuestion(it.questionId)) },
                    questions = state.board,
                    modifier = modifier
                )
                is GameScreenUiState.Question -> QuestionScreen(
                    isHost = state.isHost,
                    onBuzz = { eventSink(GameScreenUiEvent.Buzz) },
                    showQuestionIn = state.showQuestionIn,
                    question = state.question,
                    modifier = modifier
                )
                is GameScreenUiState.Answering -> {
                    Text(text = "Not yet implemented")
                }
                is GameScreenUiState.Answer -> {
                    Text(text = "Not yet implemented")
                }
                is GameScreenUiState.Results -> {
                    Text(text = "Not yet implemented")
                }
            }
        }
    }
}