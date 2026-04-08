package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.back
import dvnkquizz.sharedui.generated.resources.draft_saved
import dvnkquizz.sharedui.generated.resources.ic_arrow_back
import dvnkquizz.sharedui.generated.resources.ic_check
import dvnkquizz.sharedui.generated.resources.ic_upload
import dvnkquizz.sharedui.generated.resources.saving_draft
import dvnkquizz.sharedui.generated.resources.unnamed_game_package
import dvnkquizz.sharedui.generated.resources.upload_game_package
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditGamePackageScreen(
    viewModel: EditGamePackageViewModel,
    navigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onNavigateBack = navigateBack,
        eventSink = viewModel::eventSink,
        modifier = Modifier.fillMaxSize(),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    uiState: EditGamePackageUiState,
    onNavigateBack: () -> Unit,
    eventSink: (EditGamePackageUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var wantToNavigateBack by remember { mutableStateOf(false) }

    LaunchedEffect(wantToNavigateBack, uiState.isSaveInProgress) {
        if (wantToNavigateBack && !uiState.isSaveInProgress) {
            delay(100)
            onNavigateBack()
        }
    }

    if (wantToNavigateBack && uiState.isSaveInProgress) {
        Box(modifier) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingIndicator()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.saving_draft),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    else {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        val title = uiState.gamePackage.title.ifBlank { stringResource(Res.string.unnamed_game_package) }
                        Text(text = title)
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                eventSink(EditGamePackageUiEvent.SaveDraft)
                                wantToNavigateBack = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = stringResource(Res.string.back)
                            )
                        }
                    },
                    actions = {
                        if (uiState.isSaveInProgress)
                            LoadingIndicator()
                        else
                            Icon(
                                painter = painterResource(Res.drawable.ic_check),
                                contentDescription = stringResource(Res.string.draft_saved)
                            )

                        if (uiState.uploadProgress != null)
                            LoadingIndicator(progress = { uiState.uploadProgress })
                        else
                            IconButton(onClick = { eventSink(EditGamePackageUiEvent.UploadPackage) }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_upload),
                                    contentDescription = stringResource(Res.string.upload_game_package)
                                )
                            }
                    }
                )
            }
        ) { paddingValues ->
            val modifier = Modifier.padding(paddingValues).fillMaxSize()
            when {
                uiState.isLoading -> Box(modifier) {
                    LoadingIndicator(Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> Box(modifier) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = uiState.errorMessage,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> EditGamePackageContent(
                    gamePackage = uiState.gamePackage,
                    eventSink = eventSink,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun EditGamePackageContent(
    gamePackage: FullGamePackage,
    eventSink: (EditGamePackageUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {

}