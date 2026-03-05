package com.davanok.dvnkquizz.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.ui.screens.packagePicker.PackagePicker
import dev.zacsweers.metrox.viewmodel.metroViewModel
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.ic_clear
import org.jetbrains.compose.resources.painterResource
import kotlin.uuid.Uuid

@Composable
fun HomeScreen(
    onNavigateToLobby: (Uuid) -> Unit,
    viewModel: HomeViewModel = metroViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var nickname by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var selectedPackage by remember { mutableStateOf<GamePackage?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Handle small screens
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "SI-Game",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Your Nickname") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // JOIN SECTION
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Join a Game", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    label = { Text("Invite Code") },
                    placeholder = { Text("AB1234") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { viewModel.onJoinClicked(nickname, inviteCode, onNavigateToLobby) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nickname.isNotBlank() && inviteCode.length >= 4 && state !is HomeScreenUiState.Loading
                ) {
                    Text("Join Lobby")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // HOST SECTION
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Host New Game", style = MaterialTheme.typography.titleMedium)

            PackagePicker(
                onPackageSelected = { selectedPackage = it },
                modifier = Modifier.fillMaxWidth()
            )

            selectedPackage?.let {
                ListItem(
                    headlineContent = { Text("Selected: ${it.title}") },
                    trailingContent = {
                        IconButton(onClick = { selectedPackage = null }) {
                            Icon(painterResource(Res.drawable.ic_clear), contentDescription = null)
                        }
                    }
                )
            }

            Button(
                onClick = {
                    val pkgId = selectedPackage?.id ?: Uuid.NIL
                    viewModel.onCreateGame(pkgId, nickname, onNavigateToLobby)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nickname.isNotBlank() && selectedPackage != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Host with Selected Package")
            }
        }

        if (state is HomeScreenUiState.Error) {
            Text(
                text = (state as HomeScreenUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}