package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.GameCategory
import com.davanok.dvnkquizz.core.domain.entities.GameRound
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.utils.AllowedExtensions
import com.davanok.dvnkquizz.core.utils.AllowedMimeTypes
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlin.uuid.Uuid

@Immutable
data class EditGamePackageUiState(
    val isLoading: Boolean = true,
    val criticalError: String? = null,
    val errorMessage: String? = null,
    val gamePackage: FullGamePackage = FullGamePackage.Empty,
    val dialog: EditGamePackageDialog? = null,
    val isSaveInProgress: Boolean = false,
    val isUploaded: Boolean = false,
    val isUploadInProgress: Boolean = false
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

    data class AddRound(val round: GameRound) : EditGamePackageUiEvent
    data class AddCategory(val roundId: Uuid, val category: GameCategory) : EditGamePackageUiEvent
    data class AddQuestion(val categoryId: Uuid, val question: Question) : EditGamePackageUiEvent

    data object OpenQuestionMediaSelector: EditGamePackageUiEvent
    data object RemoveQuestionMedia: EditGamePackageUiEvent
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
    data class EditQuestion(val question: Question, val mediaErrorMessage: String?): EditGamePackageDialog

    data object AddRound: EditGamePackageDialog
    data class AddCategory(val roundId: Uuid): EditGamePackageDialog
    data class AddQuestion(val categoryId: Uuid, val mediaErrorMessage: String?): EditGamePackageDialog
}

object GamePackageLimits {
    const val TITLE_MAX_LENGTH = 35
    const val DESCRIPTION_MAX_LENGTH = 80

    const val DIFFICULTY_MIN_VALUE = 1
    const val DIFFICULTY_MAX_VALUE = 10
    val DIFFICULTY_RANGE = DIFFICULTY_MIN_VALUE..DIFFICULTY_MAX_VALUE

    const val QUESTION_TEXT_MAX_LENGTH = 100
    const val QUESTION_ANSWER_MAX_LENGTH = 100

    const val QUESTION_PRICE_MIN_VALUE = 0
    const val QUESTION_PRICE_MAX_VALUE = 1000
    val QUESTION_PRICE_RANGE = QUESTION_PRICE_MIN_VALUE..QUESTION_PRICE_MAX_VALUE

    const val QUESTION_MEDIA_MAX_SIZE = 200L * 1024 * 1024 // 200 Mib in bytes

    val allowedMediaFileExtensions = AllowedExtensions.All
    val allowedMediaFileMimeTypes = AllowedMimeTypes.All.map { MimeType.parse(it) }.toSet()
}