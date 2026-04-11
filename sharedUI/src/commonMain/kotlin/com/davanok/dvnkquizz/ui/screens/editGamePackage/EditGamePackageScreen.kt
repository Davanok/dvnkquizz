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
import com.davanok.dvnkquizz.ui.LocalSnackBarHostState
import com.davanok.dvnkquizz.ui.screens.editGamePackage.components.EditCategoryDialog
import com.davanok.dvnkquizz.ui.screens.editGamePackage.components.EditGamePackageContent
import com.davanok.dvnkquizz.ui.screens.editGamePackage.components.EditGamePackageQuestionDialog
import com.davanok.dvnkquizz.ui.screens.editGamePackage.components.EditRoundDialog
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
    } else {
        val snackbarState = LocalSnackBarHostState.current

        LaunchedEffect(uiState.errorMessage) {
            if (uiState.errorMessage != null) {
                snackbarState.showSnackbar(uiState.errorMessage)
            }
        }

        Scaffold(
            modifier = modifier,
            topBar = {
                EditPackageTopBar(
                    title = uiState.gamePackage.title,
                    onBackClick = {
                        eventSink(EditGamePackageUiEvent.SaveDraft)
                        wantToNavigateBack = true
                    },
                    isSaveInProgress = uiState.isSaveInProgress,
                    onSaveClick = { eventSink(EditGamePackageUiEvent.SaveDraft) },
                    uploadProgress = uiState.uploadProgress,
                    onUploadClick = { eventSink(EditGamePackageUiEvent.UploadPackage) }
                )
            }
        ) { paddingValues ->
            val modifier = Modifier.padding(paddingValues).padding(horizontal = 12.dp).fillMaxSize()

            when {
                uiState.isLoading -> Box(modifier) {
                    LoadingIndicator(Modifier.align(Alignment.Center))
                }

                uiState.criticalError != null -> Box(modifier) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = uiState.criticalError,
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

            when (val dialog = uiState.dialog) {
                is EditGamePackageDialog.EditRound -> EditRoundDialog(
                    round = dialog.round,
                    onSave = { eventSink(EditGamePackageUiEvent.UpdateRound(it)) },
                    onDismissRequest = { eventSink(EditGamePackageUiEvent.CloseDialog) }
                )
                is EditGamePackageDialog.EditCategory -> EditCategoryDialog(
                    category = dialog.category,
                    onSave = { eventSink(EditGamePackageUiEvent.UpdateCategory(it)) },
                    onDismissRequest = { eventSink(EditGamePackageUiEvent.CloseDialog) }
                )
                is EditGamePackageDialog.EditQuestion -> EditGamePackageQuestionDialog(
                    question = dialog.question,
                    onSave = { eventSink(EditGamePackageUiEvent.UpdateQuestion(it)) },
                    onDismissRequest = { eventSink(EditGamePackageUiEvent.CloseDialog) }
                )
                null -> {  }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditPackageTopBar(
    title: String,
    onBackClick: () -> Unit,
    isSaveInProgress: Boolean,
    onSaveClick: () -> Unit,
    uploadProgress: Float?,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(text = title.ifBlank { stringResource(Res.string.unnamed_game_package) })
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.back)
                )
            }
        },
        actions = {
            if (isSaveInProgress)
                LoadingIndicator()
            else
                IconButton(onClick = onSaveClick) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = stringResource(Res.string.draft_saved)
                    )
                }

            if (uploadProgress != null)
                LoadingIndicator(progress = { uploadProgress })
            else
                IconButton(onClick = onUploadClick) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_upload),
                        contentDescription = stringResource(Res.string.upload_game_package)
                    )
                }
        }
    )
}

