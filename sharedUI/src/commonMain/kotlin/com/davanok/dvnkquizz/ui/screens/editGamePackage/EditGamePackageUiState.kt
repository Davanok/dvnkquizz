package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.Question
import kotlin.uuid.Uuid

@Immutable
data class EditGamePackageUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val gamePackage: FullGamePackage = FullGamePackage.Empty,
    val editQuestion: Question? = null,
    val isSaveInProgress: Boolean = false,
    val uploadProgress: Float? = null
)

sealed interface EditGamePackageUiEvent {
    data object SaveDraft: EditGamePackageUiEvent
    data object UploadPackage: EditGamePackageUiEvent

    data class SetTitle(val title: String): EditGamePackageUiEvent
    data class SetDescription(val description: String): EditGamePackageUiEvent
    data class SetDifficulty(val difficulty: Int): EditGamePackageUiEvent

    data class AddRound(val name: String): EditGamePackageUiEvent
    data class AddCategory(val roundId: Uuid, val name: String): EditGamePackageUiEvent

    data class NewQuestion(val categoryId: Uuid): EditGamePackageUiEvent
    data class EditQuestion(val questionId: Uuid?): EditGamePackageUiEvent
}

object GamePackageLimits {
    const val TITLE_MAX_LENGTH = 50
    const val DESCRIPTION_MAX_LENGTH = 50
    const val DIFFICULTY_MAX_VALUE = 10
}