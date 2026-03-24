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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
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
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem

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

    Column {
        LazyHorizontalGrid(
            modifier = modifier.fillMaxWidth().weight(1f),
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

            // Remaining Columns: The question cards
            for (index in 0 until maxItems) {
                categories.forEach { category ->
                    val item = questions[category]?.getOrNull(index)

                    if (item == null) {
                        item(contentType = "placeholder") {
                            // Use a fresh modifier, NOT the screen's root modifier
                            Spacer(modifier = Modifier.width(100.dp).fillMaxHeight())
                        }
                    } else {
                        item(contentType = "item", key = item.questionId) { // Use ID for better recomposition
                            GameBoardCard( // Renamed to avoid shadowing
                                item = item,
                                isHost = isHost,
                                onClick = { onSelectQuestion(item) },
                                modifier = Modifier.width(100.dp).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
        Button(onClick = onNextRound) {
            Text(text = "Next round")
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
        modifier = modifier.width(120.dp) // Give headers a consistent width
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
        shape = MaterialTheme.shapes.medium, // 'medium' is standard for cards
        // Dim the background if answered
        color = if (item.isAnswered) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        enabled = isEnabled,
        onClick = onClick,
        border = if (item.isAnswered) null else BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        // Use a Box to perfectly center the price text
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = item.price.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                // Dim the text if answered
                color = if (item.isAnswered) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}