package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.davanok.dvnkquizz.ui.theme.LocalSnackBarHostState
import com.davanok.dvnkquizz.ui.screens.editGamePackage.components.EditCategoryDialog
import com.davanok.dvnkquizz.ui.screens.editGamePackage.components.EditGamePackageContent
import com.davanok.dvnkquizz.ui.screens.editGamePackage.components.EditGamePackageQuestionDialog
import com.davanok.dvnkquizz.ui.screens.editGamePackage.components.EditRoundDialog
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.back
import dvnkquizz.sharedui.generated.resources.delete_package
import dvnkquizz.sharedui.generated.resources.download_game_package
import dvnkquizz.sharedui.generated.resources.draft_saved
import dvnkquizz.sharedui.generated.resources.ic_arrow_back
import dvnkquizz.sharedui.generated.resources.ic_check
import dvnkquizz.sharedui.generated.resources.ic_delete
import dvnkquizz.sharedui.generated.resources.ic_download
import dvnkquizz.sharedui.generated.resources.ic_more_vert
import dvnkquizz.sharedui.generated.resources.ic_upload
import dvnkquizz.sharedui.generated.resources.open_menu
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

    val snackbarHostState = LocalSnackBarHostState.current
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

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
                    isUploadInProgress = uiState.isUploadInProgress,
                    onUploadClick = { eventSink(EditGamePackageUiEvent.UploadPackage) },
                    isDownloadAvailable = uiState.isUploaded,
                    isDownloadInProgress = uiState.isDownloadInProgress,
                    onDownloadClick = { eventSink(EditGamePackageUiEvent.DownloadPackage) },
                    onDeletePackageClick = {
                        eventSink(EditGamePackageUiEvent.DeletePackage {
                            onNavigateBack()
                        })
                    }
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
                null -> {  }

                is EditGamePackageDialog.EditRound -> EditRoundDialog(
                    round = dialog.round,
                    isEdit = dialog.isEdit,
                    onSave = { eventSink(EditGamePackageUiEvent.UpdateRound(it)) },
                    onDismissRequest = { eventSink(EditGamePackageUiEvent.CloseDialog) }
                )
                is EditGamePackageDialog.EditCategory -> EditCategoryDialog(
                    category = dialog.category,
                    isEdit = dialog.isEdit,
                    onSave = { eventSink(EditGamePackageUiEvent.UpdateCategory(it)) },
                    onDismissRequest = { eventSink(EditGamePackageUiEvent.CloseDialog) }
                )
                is EditGamePackageDialog.EditQuestion -> EditGamePackageQuestionDialog(
                    question = dialog.question,
                    isEdit = dialog.isEdit,
                    questionMediaErrorMessage = dialog.mediaErrorMessage,
                    setQuestionMedia = { m, b -> eventSink(EditGamePackageUiEvent.SetQuestionMedia(m, b)) },
                    removeMedia = { eventSink(EditGamePackageUiEvent.RemoveQuestionMedia) },
                    onSave = { eventSink(EditGamePackageUiEvent.UpdateQuestion(it)) },
                    onDismissRequest = { eventSink(EditGamePackageUiEvent.CloseDialog) }
                )
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
    isUploadInProgress: Boolean,
    onUploadClick: () -> Unit,
    isDownloadAvailable: Boolean,
    isDownloadInProgress: Boolean,
    onDownloadClick: () -> Unit,
    onDeletePackageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
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

            if (isDownloadAvailable) {
                if (isDownloadInProgress)
                    LoadingIndicator()
                else
                    IconButton(
                        onClick = onDownloadClick,
                        enabled = !isUploadInProgress
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_download),
                            contentDescription = stringResource(Res.string.download_game_package)
                        )
                    }
            }

            if (isUploadInProgress)
                LoadingIndicator()
            else
                IconButton(
                    onClick = onUploadClick,
                    enabled = !isDownloadInProgress
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_upload),
                        contentDescription = stringResource(Res.string.upload_game_package)
                    )
                }

            IconButton(
                onClick = { menuExpanded = !menuExpanded }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_more_vert),
                    contentDescription = stringResource(Res.string.open_menu)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.delete_package)) },
                    leadingIcon = { Icon(painter = painterResource(Res.drawable.ic_delete), contentDescription = null) },
                    onClick = onDeletePackageClick
                )
            }
        }
    )
}

