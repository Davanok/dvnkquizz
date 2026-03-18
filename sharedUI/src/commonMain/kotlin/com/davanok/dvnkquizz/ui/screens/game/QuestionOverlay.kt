package com.davanok.dvnkquizz.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.entities.Question
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.ic_check
import dvnkquizz.sharedui.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource
import kotlin.uuid.Uuid

@Composable
fun QuestionOverlay(
    question: Question,
    isHost: Boolean,
    buzzedParticipant: Participant?, // Null if the buzzer is open
    onBuzz: () -> Unit,
    onJudge: (Uuid, isCorrect: Boolean) -> Unit,
    onSkip: () -> Unit         // Host only: skips question if nobody answers
) {
    // Using a Dialog ensures it floats above the main game board
    // and intercepts back presses/clicks outside.
    Dialog(
        onDismissRequest = { /* Prevent accidental dismissals */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false // Allows us to use more screen space
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Question Metadata
                    Text(
                        text = "For ${question.price} Points",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 2. The Question Text
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 3. Contextual Controls (Host vs Player)
                    if (isHost) {
                        HostControls(
                            buzzedParticipant = buzzedParticipant,
                            onJudge = onJudge,
                            onSkip = onSkip
                        )
                    } else {
                        PlayerControls(
                            buzzedParticipant = buzzedParticipant,
                            onBuzz = onBuzz
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostControls(
    buzzedParticipant: Participant?,
    onJudge: (Uuid, Boolean) -> Unit,
    onSkip: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (buzzedParticipant == null) {
            Text("Waiting for players to buzz...", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onSkip) {
                Text("Skip Question")
            }
        } else {
            Text(
                text = "${buzzedParticipant.user.nickname} is answering!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onJudge(buzzedParticipant.id, false) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(painter = painterResource(Res.drawable.ic_close), contentDescription = "Incorrect")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Incorrect")
                }

                Button(
                    onClick = { onJudge(buzzedParticipant.id, true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green
                ) {
                    Icon(painterResource(Res.drawable.ic_check), contentDescription = "Correct")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Correct")
                }
            }
        }
    }
}

@Composable
private fun PlayerControls(
    buzzedParticipant: Participant?,
    onBuzz: () -> Unit
) {
    // Buzzer is open!
    var isLocalLoading by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (buzzedParticipant == null) {
            Button(
                onClick = {
                    isLocalLoading = true
                    onBuzz()
                },
                enabled = !isLocalLoading, // Prevent double-clicking
                modifier = Modifier.size(120.dp)
            ) {
                Text("BUZZ")
            }
        } else {
            LaunchedEffect(buzzedParticipant) {
                isLocalLoading = false
            }
            // Someone buzzed
            if (buzzedParticipant.isMe) {
                Text(
                    text = "You buzzed in! Answer the host.",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4CAF50), // Green indicating it's their turn
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "${buzzedParticipant.user.nickname} buzzed in!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Buzzer locked.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}