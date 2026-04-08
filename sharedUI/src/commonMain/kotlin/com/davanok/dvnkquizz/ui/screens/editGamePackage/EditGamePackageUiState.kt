package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage

@Immutable
data class EditGamePackageUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val gamePackage: FullGamePackage = FullGamePackage.Empty,
    val isSaveInProgress: Boolean = false,
    val uploadProgress: Float? = null
)

sealed interface EditGamePackageUiEvent {
    data object SaveDraft: EditGamePackageUiEvent
    data object UploadPackage: EditGamePackageUiEvent
}