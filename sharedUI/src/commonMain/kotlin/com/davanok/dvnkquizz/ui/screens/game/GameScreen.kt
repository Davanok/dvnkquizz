package com.davanok.dvnkquizz.ui.screens.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.game.entities.Participant
import com.davanok.dvnkquizz.ui.screens.game.components.AnswerScreen
import com.davanok.dvnkquizz.ui.screens.game.components.AnsweringScreen
import com.davanok.dvnkquizz.ui.screens.game.components.FatalErrorScreen
import com.davanok.dvnkquizz.ui.screens.game.components.IdleScreen
import com.davanok.dvnkquizz.ui.screens.game.components.LoadingScreen
import com.davanok.dvnkquizz.ui.screens.game.components.ParticipantCard
import com.davanok.dvnkquizz.ui.screens.game.components.QuestionScreen
import com.davanok.dvnkquizz.ui.screens.game.components.ResultsScreen
import com.davanok.dvnkquizz.ui.screens.game.components.SelectQuestionScreen
import com.davanok.dvnkquizz.ui.theme.LocalClipboardManager
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.back
import dvnkquizz.sharedui.generated.resources.copy_invite_code
import dvnkquizz.sharedui.generated.resources.host
import dvnkquizz.sharedui.generated.resources.ic_arrow_back
import dvnkquizz.sharedui.generated.resources.ic_copy
import dvnkquizz.sharedui.generated.resources.invite_code
import dvnkquizz.sharedui.generated.resources.unknown_game_package
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
                title = { Title(state.gamePackage?.title, state.inviteCode) },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    if (state.isHost) {
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(Res.string.host)) }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (state !is GameScreenUiState.Idle) {
                ParticipantsList(
                    participants = state.participants,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }
            PagesContent(
                state = state,
                eventSink = eventSink,
                navigateBack = navigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Title(
    title: String?,
    code: String?,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title ?: stringResource(Res.string.unknown_game_package),
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (code != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.invite_code),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AssistChip(
                    onClick = { scope.launch { clipboard.setString(code) } },
                    label = { Text(text = code) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_copy),
                            contentDescription = stringResource(Res.string.copy_invite_code)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
                        trailingIconContentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer)
                    )
                )
            }
        }
    }
}

@Composable
private fun PagesContent(
    state: GameScreenUiState,
    eventSink: (GameScreenUiEvent) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier
) {
    AnimatedContent(
        targetState = state,
        modifier = modifier,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { state ->
        val modifier = Modifier.fillMaxSize()
        when (state) {
            GameScreenUiState.Loading -> LoadingScreen(modifier = modifier)
            is GameScreenUiState.FatalError -> FatalErrorScreen(message = state.message, modifier = modifier)
            is GameScreenUiState.Idle -> IdleScreen(
                isHost = state.isHost,
                nextRoundEnabled = state.startEnabled,
                onNextRound = { eventSink(GameScreenUiEvent.NextRound) },
                participants = state.participants,
                modifier = modifier
            )
            is GameScreenUiState.SelectQuestion -> SelectQuestionScreen(
                isHost = state.isHost,
                onSelectQuestion = { eventSink(GameScreenUiEvent.SelectQuestion(it.questionId)) },
                onNextRound = { eventSink(GameScreenUiEvent.NextRound) },
                gameBoard = state.board,
                modifier = modifier
            )
            is GameScreenUiState.Question -> QuestionScreen(
                isHost = state.isHost,
                onBuzz = { eventSink(GameScreenUiEvent.Buzz(it)) },
                showQuestionAt = state.showQuestionAt,
                question = state.question,
                modifier = modifier
            )
            is GameScreenUiState.Answering -> AnsweringScreen(
                isHost = state.isHost,
                question = state.question,
                participant = state.buzzedParticipant,
                answer = state.answer,
                judgeAnswer = { id, isCorrect -> eventSink(GameScreenUiEvent.JudgeAnswer(id, isCorrect)) },
                modifier = modifier
            )
            is GameScreenUiState.Answer -> AnswerScreen(
                isHost = state.isHost,
                question = state.question,
                onNextQuestion = { eventSink(GameScreenUiEvent.NextQuestion) },
                modifier = modifier
            )
            is GameScreenUiState.Results -> ResultsScreen(
                participants = state.participants,
                onLeave = navigateBack,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ParticipantsList(
    participants: List<Participant>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        items(
            items = participants,
            key = { it.id }
        ) { participant ->
            ParticipantCard(
                participant = participant,
                modifier = Modifier.width(100.dp).animateItem()
            )
        }
    }
}