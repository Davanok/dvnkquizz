package com.davanok.dvnkquizz.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.entities.ExternalFile
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
        
        ProfilePart(
            nickname = state.nickname,
            image = state.image,
            onNicknameChange = viewModel::setNickname,
            submitNickname = viewModel::submitNickname,
            onLogOut = viewModel::logOut,
            modifier = Modifier
        )
        
        GamePart(
            inviteCode = inviteCode,
            onInviteCodeChange = { inviteCode = it.uppercase() },
            selectedPackage = selectedPackage,
            onSelectPackage = { selectedPackage = it },
            joinEnabled = inviteCode.length == 6,
            createEnabled = selectedPackage != null,
            onJoin = {
                viewModel.onJoinClicked(inviteCode, onNavigateToLobby)
                     },
            onCreate = {
                selectedPackage?.let {
                    viewModel.onCreateGame(it.id, onNavigateToLobby)
                }
                       },
            modifier = Modifier
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage.toString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ProfilePart(
    nickname: String,
    image: ExternalFile?,
    onNicknameChange: (String) -> Unit,
    submitNickname: () -> Unit,
    onLogOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        if (image != null)
            AsyncImage(
                model = image,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                onError = { e ->
                    Logger.e(e.result.throwable) { "failed to render profile image" }
                }
            )

        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text("Your Nickname") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submitNickname() }),
            singleLine = true
        )

        Button(
            onClick = onLogOut
        ) {
            Text(text = "logout")
        }
    }
}

@Composable
private fun GamePart(
    inviteCode: String,
    onInviteCodeChange: (String) -> Unit,
    selectedPackage: GamePackage?,
    onSelectPackage: (GamePackage?) -> Unit,
    joinEnabled: Boolean,
    createEnabled: Boolean,
    onJoin: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Join a Game", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = inviteCode,
                onValueChange = onInviteCodeChange,
                label = { Text("Invite Code") },
                placeholder = { Text("AB1234") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth(),
                enabled = joinEnabled
            ) {
                Text("Join Lobby")
            }
        }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Host New Game", style = MaterialTheme.typography.titleMedium)

        PackagePicker(
            onPackageSelected = onSelectPackage,
            modifier = Modifier.fillMaxWidth()
        )

        selectedPackage?.let {
            ListItem(
                headlineContent = { Text("Selected: ${it.title}") },
                trailingContent = {
                    IconButton(onClick = { onSelectPackage(null) }) {
                        Icon(painterResource(Res.drawable.ic_clear), contentDescription = null)
                    }
                }
            )
        }

        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth(),
            enabled = createEnabled,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Host with Selected Package")
        }
    }
}