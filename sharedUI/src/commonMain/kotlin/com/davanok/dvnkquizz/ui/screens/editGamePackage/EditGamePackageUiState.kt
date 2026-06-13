package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.core.media.AllowedExtensions
import com.davanok.dvnkquizz.core.core.media.AllowedMimeTypes
import com.davanok.dvnkquizz.core.domain.game.entities.GameCategory
import com.davanok.dvnkquizz.core.domain.game.entities.GameRound
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.FullGamePackage
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
    val isDownloadAvailable: Boolean = false,
    val isUploadInProgress: Boolean = false,
    val isDownloadInProgress: Boolean = false
)

sealed interface EditGamePackageUiEvent {
    data class DeletePackage(val onSuccess: () -> Unit): EditGamePackageUiEvent

    data object SaveDraft: EditGamePackageUiEvent
    data object UploadPackage: EditGamePackageUiEvent
    data object DownloadPackage: EditGamePackageUiEvent

    data class SetTitle(val title: String): EditGamePackageUiEvent
    data class SetDescription(val description: String): EditGamePackageUiEvent
    data class SetDifficulty(val difficulty: Int): EditGamePackageUiEvent
    data class SetIsPublic(val isPublic: Boolean): EditGamePackageUiEvent

    data class ShowDialog(val dialogRequest: EditGamePackageDialogRequest): EditGamePackageUiEvent
    data object CloseDialog: EditGamePackageUiEvent

    data class UpdateRound(val round: GameRound): EditGamePackageUiEvent
    data class UpdateCategory(val category: GameCategory): EditGamePackageUiEvent
    data class UpdateQuestion(val question: Question): EditGamePackageUiEvent

    data class SetQuestionMedia(val mimeType: String, val media: ByteArray): EditGamePackageUiEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as SetQuestionMedia

            if (mimeType != other.mimeType) return false
            if (!media.contentEquals(other.media)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = mimeType.hashCode()
            result = 31 * result + media.contentHashCode()
            return result
        }
    }

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
    data class EditRound(val round: GameRound, val isEdit: Boolean): EditGamePackageDialog
    data class EditCategory(val category: GameCategory, val isEdit: Boolean): EditGamePackageDialog
    data class EditQuestion(val question: Question, val mediaErrorMessage: String?, val isEdit: Boolean): EditGamePackageDialog
}

object GamePackageLimits {
    const val TITLE_MAX_LENGTH = 35
    const val DESCRIPTION_MAX_LENGTH = 80

    const val DIFFICULTY_MIN_VALUE = 1
    const val DIFFICULTY_MAX_VALUE = 10
    val DIFFICULTY_RANGE = DIFFICULTY_MIN_VALUE..DIFFICULTY_MAX_VALUE

    const val QUESTION_TEXT_MAX_LENGTH = 150
    const val QUESTION_ANSWER_MAX_LENGTH = 150

    const val QUESTION_PRICE_MIN_VALUE = 0
    const val QUESTION_PRICE_MAX_VALUE = 1000
    val QUESTION_PRICE_RANGE = QUESTION_PRICE_MIN_VALUE..QUESTION_PRICE_MAX_VALUE

    const val QUESTION_MEDIA_MAX_SIZE = 200L * 1024 * 1024 // 200 Mib in bytes

    val allowedMediaFileExtensions = AllowedExtensions.All
    val allowedMediaFileMimeTypes = AllowedMimeTypes.All
}