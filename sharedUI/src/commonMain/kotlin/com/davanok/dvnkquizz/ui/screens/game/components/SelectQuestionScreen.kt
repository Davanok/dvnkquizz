package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
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
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.game.entities.GameBoardItem
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.next_round
import org.jetbrains.compose.resources.stringResource

@Composable
fun SelectQuestionScreen(
    isHost: Boolean,
    onSelectQuestion: (GameBoardItem) -> Unit,
    onNextRound: () -> Unit,
    questions: Map<String, List<GameBoardItem>>,
    modifier: Modifier = Modifier
) {
    // 1. Create a stable list of categories to guarantee column/row mapping order
    val categories = remember(questions) { questions.keys.toList() }
    val maxItems = remember(questions) { questions.values.maxOfOrNull { it.size } ?: 0 }

    if (categories.isEmpty() || maxItems == 0) return

    Column(modifier = modifier) {
        LazyHorizontalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            rows = GridCells.Fixed(categories.size),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First Column: Category Headers
            categories.forEach { category ->
                item {
                    CategoryHeader(
                        title = category,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }

            repeat(maxItems) { index ->
                categories.forEach { category ->
                    val item = questions[category]?.getOrNull(index)

                    if (item == null) {
                        item(contentType = "placeholder") {
                            Spacer(modifier = Modifier.width(100.dp).fillMaxHeight())
                        }
                    } else {
                        item(contentType = "item", key = item.questionId) {
                            GameBoardCard(
                                item = item,
                                isHost = isHost,
                                onClick = { onSelectQuestion(item) },
                                modifier = Modifier
                                    .size(width = 100.dp, height = 70.dp)
                                    .fillMaxHeight()
                            )
                        }
                    }
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
private fun CategoryHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.width(120.dp)
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
    item: GameBoardItem,
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
        onClick = onClick,
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