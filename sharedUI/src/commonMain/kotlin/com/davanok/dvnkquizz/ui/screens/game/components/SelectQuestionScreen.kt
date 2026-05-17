package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.game.entities.GameBoardQuestion
import com.davanok.dvnkquizz.core.domain.game.entities.GameBoardRow
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.next_round
import org.jetbrains.compose.resources.stringResource

private val CellSize = DpSize(100.dp, 70.dp)

@Composable
fun SelectQuestionScreen(
    isHost: Boolean,
    onSelectQuestion: (GameBoardQuestion) -> Unit,
    onNextRound: () -> Unit,
    gameBoard: List<GameBoardRow>,
    modifier: Modifier = Modifier
) {
    val maxItems = remember(gameBoard) { gameBoard.maxOfOrNull { it.questions.size } } ?: 0

    if (gameBoard.isEmpty() || maxItems == 0) return

    val rowState = rememberLazyListState()

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(
                items = gameBoard,
                key = { it.categoryId }
            ) { gameBoardRow ->
                CategoryRow(
                    isHost = isHost,
                    row = gameBoardRow,
                    maxRowItems = maxItems,
                    lazyRowState = rowState,
                    onItemClick = onSelectQuestion,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
        if (isHost)
            Button(
                onClick = onNextRound,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.next_round))
            }
    }
}

@Composable
private fun CategoryRow(
    isHost: Boolean,
    row: GameBoardRow,
    maxRowItems: Int,
    lazyRowState: LazyListState,
    onItemClick: (GameBoardQuestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        CategoryHeader(
            title = row.categoryName,
            modifier = Modifier.size(CellSize)
        )

        LazyRow(
            modifier = Modifier.weight(1f),
            state = lazyRowState,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(maxRowItems) { index ->
                if (index < row.questions.size) {
                    val item = row.questions[index]
                    item(key = item.questionId) {
                        GameBoardCard(
                            item = item,
                            isHost = isHost,
                            onClick = { onItemClick(item) },
                            modifier = Modifier.size(CellSize)
                        )
                    }
                } else {
                    item {
                        Spacer(Modifier.size(CellSize))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GameBoardCard(
    item: GameBoardQuestion,
    isHost: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = isHost && !item.isAnswered

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (item.isAnswered) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        enabled = isEnabled,
        onClick = { if (isEnabled) onClick() },
        border = if (item.isAnswered) null else BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = item.price.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (item.isAnswered) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}