package com.davanok.dvnkquizz.ui.screens.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.enums.ParticipantRole
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.ic_error
import dvnkquizz.sharedui.generated.resources.ic_person
import org.jetbrains.compose.resources.painterResource

@Composable
fun ParticipantCard(
    participant: Participant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = when (participant.role) {
            ParticipantRole.HOST -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            ParticipantRole.PLAYER -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ParticipantRole.SPECTATOR -> null
        }
    ) {
        CharacterImage(
            imageUrl = participant.user.image,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Text(
            text = participant.user.nickname,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
        Text(
            text = participant.score.toString(),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CharacterImage(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        when {
            imageUrl == null -> Icon(
                painter = painterResource(Res.drawable.ic_person),
                contentDescription = "profile icon"
            )
            else -> AsyncImage(
                model = imageUrl,
                contentDescription = "profile image",
                error = painterResource(Res.drawable.ic_error)
            )
        }
    }
}