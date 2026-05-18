package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
private val CellsSpacing = 12.dp

@Composable
fun SelectQuestionScreen(
    isHost: Boolean,
    onSelectQuestion: (GameBoardQuestion) -> Unit,
    onNextRound: () -> Unit,
    gameBoard: List<GameBoardRow>,
    modifier: Modifier = Modifier
) {
    if (gameBoard.isEmpty()) return

    val rowItems = remember(gameBoard) {
        val maxItems = gameBoard.maxOfOrNull { it.questions.size } ?: 0

        List(maxItems) { index ->
            buildList {
                gameBoard.forEach { category ->
                    category.questions.getOrNull(index)
                        ?.let(::add)
                }
            }
        }
    }

    val categoryNames = remember(gameBoard) {
        gameBoard.map { it.categoryName }
    }

    Column(modifier = modifier) {

        Row(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            CategoryNamesColumn(categories = categoryNames)

            Spacer(Modifier.width(CellsSpacing))

            LazyRow(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CellsSpacing)
            ) {
                items(rowItems) { items ->
                    QuestionsColumn(
                        isHost = isHost,
                        items = items,
                        onItemClick = onSelectQuestion
                    )
                }
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
private fun CategoryNamesColumn(
    categories: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CellsSpacing)) {
        categories.forEach {
            CategoryHeader(
                title = it,
                modifier = Modifier.size(CellSize)
            )
        }
    }
}

@Composable
private fun QuestionsColumn(
    isHost: Boolean,
    items: List<GameBoardQuestion>,
    onItemClick: (GameBoardQuestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CellsSpacing)) {
        items.forEach {
            GameBoardCard(
                item = it,
                isHost = isHost,
                onClick = { onItemClick(it) },
                modifier = Modifier.size(CellSize)
            )
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