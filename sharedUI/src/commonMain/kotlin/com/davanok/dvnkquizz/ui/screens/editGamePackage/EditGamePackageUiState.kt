package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.GameCategory
import com.davanok.dvnkquizz.core.domain.entities.GameRound
import com.davanok.dvnkquizz.core.domain.entities.Question
import kotlin.uuid.Uuid

@Immutable
data class EditGamePackageUiState(
    val isLoading: Boolean = true,
    val criticalError: String? = null,
    val errorMessage: String? = null,
    val gamePackage: FullGamePackage = FullGamePackage.Empty,
    val dialog: EditGamePackageDialog? = null,
    val isSaveInProgress: Boolean = false,
    val uploadProgress: Float? = null
)

sealed interface EditGamePackageUiEvent {
    data object SaveDraft: EditGamePackageUiEvent
    data object UploadPackage: EditGamePackageUiEvent

    data class SetTitle(val title: String): EditGamePackageUiEvent
    data class SetDescription(val description: String): EditGamePackageUiEvent
    data class SetDifficulty(val difficulty: Int): EditGamePackageUiEvent

    data class ShowDialog(val dialogRequest: EditGamePackageDialogRequest): EditGamePackageUiEvent
    data object CloseDialog: EditGamePackageUiEvent

    data class UpdateRound(val round: GameRound): EditGamePackageUiEvent
    data class UpdateCategory(val category: GameCategory): EditGamePackageUiEvent
    data class UpdateQuestion(val question: Question): EditGamePackageUiEvent
}

sealed interface EditGamePackageDialogRequest {
    data object AddRound: EditGamePackageDialogRequest
    data class EditRound(val roundId: Uuid): EditGamePackageDialogRequest

    data class AddCategory(val roundId: Uuid): EditGamePackageDialogRequest
    data class EditCategory(val categoryId: Uuid): EditGamePackageDialogRequest

    data class AddQuestion(val categoryId: Uuid): EditGamePackageDialogRequest
    data class EditQuestion(val questionId: Uuid): EditGamePackageDialogRequest
}

@Immutable
sealed interface EditGamePackageDialog {
    data class EditRound(val round: GameRound): EditGamePackageDialog

    data class EditCategory(val category: GameCategory): EditGamePackageDialog

    data class EditQuestion(val question: Question): EditGamePackageDialog
}

object GamePackageLimits {
    const val TITLE_MAX_LENGTH = 50
    const val DESCRIPTION_MAX_LENGTH = 50
    const val DIFFICULTY_MAX_VALUE = 10
}