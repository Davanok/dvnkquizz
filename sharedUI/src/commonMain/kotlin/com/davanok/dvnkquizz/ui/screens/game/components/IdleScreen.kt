package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davanok.dvnkquizz.core.domain.game.entities.Participant
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.start
import org.jetbrains.compose.resources.stringResource

@Composable
fun IdleScreen(
    isHost: Boolean,
    nextRoundEnabled: Boolean,
    onNextRound: () -> Unit,
    participants: List<Participant>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        ParticipantGrid(
            participants = participants,
            modifier = Modifier.weight(1f)
        )

        if (isHost) {
            HostBottomBar(
                nextRoundEnabled = nextRoundEnabled,
                onNextRound = onNextRound,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun HostBottomBar(
    nextRoundEnabled: Boolean,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        enabled = nextRoundEnabled,
        onClick = { if (nextRoundEnabled) onNextRound() },
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.start),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun ParticipantGrid(
    participants: List<Participant>,
    modifier: Modifier = Modifier
) {
    val participantGroups = remember(participants) {
        participants.groupBy { it.role }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        participantGroups.forEach { (role, group) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = role.toString(), // Adjust based on your Role enum/class properties
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            items(
                items = group,
                key = { it.id }
            ) { participant ->
                ParticipantCard(
                    participant = participant,
                    modifier = Modifier
                        .animateItem()
                )
            }
        }
    }
}