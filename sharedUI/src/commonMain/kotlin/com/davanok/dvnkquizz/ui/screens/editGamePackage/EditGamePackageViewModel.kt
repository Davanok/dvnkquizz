package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.game.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.game.entities.FullGameRound
import com.davanok.dvnkquizz.core.domain.game.entities.GameCategory
import com.davanok.dvnkquizz.core.domain.game.entities.GameRound
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import com.davanok.dvnkquizz.core.domain.game.entities.QuestionMedia
import com.davanok.dvnkquizz.core.domain.game.mappers.toGameCategory
import com.davanok.dvnkquizz.core.domain.game.mappers.toGameRound
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.gamePackage.repositories.GamePackagesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.error_delete_media_failed
import dvnkquizz.sharedui.generated.resources.error_failed_to_process_event
import dvnkquizz.sharedui.generated.resources.error_failed_to_update_category
import dvnkquizz.sharedui.generated.resources.error_failed_to_update_question
import dvnkquizz.sharedui.generated.resources.error_failed_to_update_round
import dvnkquizz.sharedui.generated.resources.error_package_download_failed
import dvnkquizz.sharedui.generated.resources.error_package_upload_failed
import dvnkquizz.sharedui.generated.resources.error_upload_failed
import dvnkquizz.sharedui.generated.resources.failed_to_delete_package
import dvnkquizz.sharedui.generated.resources.filetype_not_supported
import dvnkquizz.sharedui.generated.resources.max_question_media_size
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@AssistedInject
class EditGamePackageViewModel(
    @Assisted packageId: Uuid?,
    private val repository: GamePackagesRepository
) : ViewModel() {

    private val isNew = packageId == null
    private val packageId: Uuid = packageId ?: Uuid.random()

    private val _gamePackage = MutableStateFlow(FullGamePackage.Empty)
    private val _uiState = MutableStateFlow(EditGamePackageUiState())

    val uiState = combine(
        _gamePackage,
        _uiState
    ) { gamePackage, uiState ->
        uiState.copy(gamePackage = gamePackage)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditGamePackageUiState()
    )

    init {
        loadGamePackage()
        autoDraftSaver()
    }

    fun eventSink(event: EditGamePackageUiEvent) {
        viewModelScope.launch {
            runCatching {
                when (event) {
                    EditGamePackageUiEvent.SaveDraft -> saveDraft()
                    EditGamePackageUiEvent.UploadPackage -> uploadPackage()
                    EditGamePackageUiEvent.DownloadPackage -> downloadPackage()

                    is EditGamePackageUiEvent.SetTitle -> {
                        if (event.title.length <= GamePackageLimits.TITLE_MAX_LENGTH) {
                            _gamePackage.update { it.copy(title = event.title) }
                        }
                    }

                    is EditGamePackageUiEvent.SetDescription -> {
                        if (event.description.length <= GamePackageLimits.DESCRIPTION_MAX_LENGTH) {
                            _gamePackage.update { it.copy(description = event.description) }
                        }
                    }

                    is EditGamePackageUiEvent.SetDifficulty -> {
                        if (event.difficulty in GamePackageLimits.DIFFICULTY_RANGE) {
                            _gamePackage.update { it.copy(difficulty = event.difficulty) }
                        }
                    }

                    is EditGamePackageUiEvent.SetIsPublic -> {
                        _gamePackage.update { it.copy(isPublic = event.isPublic) }
                    }

                    is EditGamePackageUiEvent.ShowDialog -> showDialog(event.dialogRequest)
                    EditGamePackageUiEvent.CloseDialog -> _uiState.update { it.copy(dialog = null) }

                    is EditGamePackageUiEvent.UpdateRound -> upsertGamePackageRound(event.round)
                    is EditGamePackageUiEvent.UpdateCategory -> upsertGamePackageCategory(event.category)
                    is EditGamePackageUiEvent.UpdateQuestion -> upsertGamePackageQuestion(event.question)

                    is EditGamePackageUiEvent.SetQuestionMedia -> setQuestionMedia(
                        event.mimeType,
                        event.media
                    )

                    EditGamePackageUiEvent.RemoveQuestionMedia -> deleteQuestionMedia()
                    is EditGamePackageUiEvent.DeletePackage -> deleteGamePackage(event.onSuccess)
                }
            }.handleFailure {
                getString(
                    Res.string.error_failed_to_process_event,
                    event.toString()
                )
            }
        }
    }

    private fun loadGamePackage() = viewModelScope.launch {
        if (isNew) {
            _gamePackage.update { FullGamePackage.Empty }
            _uiState.update { it.copy(isLoading = false, criticalError = null, isUploaded = false) }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true) }

        val uploadedDeferred = async { repository.getGamePackage(packageId) }
        val draftDeferred = async { repository.getPackageDraft(packageId) }

        val uploaded = uploadedDeferred.await()
        val draft = draftDeferred.await().getOrNull()

        val uploadedPackage = uploaded.getOrNull()
        val isDownloadAvailable = uploadedPackage != null

        val preferDraft =
            draft != null && (uploadedPackage == null || draft.updatedAt > uploadedPackage.updatedAt)

        if (preferDraft) {
            _gamePackage.update { FullGamePackageUtils.sortGamePackage(draft) }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    criticalError = null,
                    isUploaded = false,
                    isDownloadAvailable = isDownloadAvailable
                )
            }
            return@launch
        }

        uploaded.fold(
            onSuccess = { gamePackage ->
                _gamePackage.update {
                    gamePackage?.let { FullGamePackageUtils.sortGamePackage(it) }
                        ?: FullGamePackage.Empty
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        criticalError = null,
                        isUploaded = true,
                        isDownloadAvailable = isDownloadAvailable
                    )
                }
            },
            onFailure = { thr ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        criticalError = thr.message,
                        isUploaded = false,
                        isDownloadAvailable = isDownloadAvailable
                    )
                }
            }
        )
    }

    private fun showDialog(dialogRequest: EditGamePackageDialogRequest) {
        val dialog = when (dialogRequest) {
            EditGamePackageDialogRequest.AddRound -> {
                val ordinal = _gamePackage.value.rounds.maxOfOrNull { it.ordinal }?.plus(1) ?: 1
                EditGamePackageDialog.EditRound(
                    GameRound(ordinal = ordinal),
                    false
                )
            }

            is EditGamePackageDialogRequest.EditRound -> {
                val round = getRoundOrNull(dialogRequest.roundId) ?: return
                EditGamePackageDialog.EditRound(round.toGameRound(), true)
            }

            is EditGamePackageDialogRequest.AddCategory -> {
                val ordinal = _gamePackage.value.rounds
                    .firstOrNull { it.id == dialogRequest.roundId }
                    ?.categories
                    ?.maxOfOrNull { it.ordinal }
                    ?.plus(1)
                    ?: 1
                EditGamePackageDialog.EditCategory(
                    GameCategory(
                        roundId = dialogRequest.roundId,
                        ordinal = ordinal
                    ),
                    false
                )
            }

            is EditGamePackageDialogRequest.EditCategory -> {
                val category = getCategoryOrNull(dialogRequest.categoryId) ?: return
                EditGamePackageDialog.EditCategory(category.toGameCategory(), true)
            }

            is EditGamePackageDialogRequest.AddQuestion -> {
                EditGamePackageDialog.EditQuestion(
                    Question(categoryId = dialogRequest.categoryId),
                    null,
                    false
                )
            }

            is EditGamePackageDialogRequest.EditQuestion -> {
                val question = getQuestionOrNull(dialogRequest.questionId) ?: return
                EditGamePackageDialog.EditQuestion(question, null, true)
            }
        }

        _uiState.update { it.copy(dialog = dialog) }
    }

    private suspend fun validateMedia(mimeType: String, bytes: ByteArray): String? {
        if (bytes.size > GamePackageLimits.QUESTION_MEDIA_MAX_SIZE) {
            return getString(
                Res.string.max_question_media_size,
                GamePackageLimits.QUESTION_MEDIA_MAX_SIZE
            )
        }
        if (mimeType !in GamePackageLimits.allowedMediaFileMimeTypes) {
            return getString(
                Res.string.filetype_not_supported,
                mimeType,
                GamePackageLimits.allowedMediaFileMimeTypes
            )
        }
        return null
    }

    private fun upsertGamePackageQuestion(question: Question) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.upsertQuestion(it, question) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_question)
        }
    }

    private fun upsertGamePackageCategory(category: GameCategory) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.upsertCategory(it, category) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_category)
        }
    }

    private fun upsertGamePackageRound(round: GameRound) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.upsertRound(it, round) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_round)
        }
    }

    private fun updateEditQuestionDialogMedia(oldMedia: QuestionMedia?, newMedia: QuestionMedia?) {
        _uiState.update { state ->
            val currentDialog = state.dialog
            if (
                currentDialog is EditGamePackageDialog.EditQuestion &&
                currentDialog.question.media?.filename == oldMedia?.filename
            ) {
                state.copy(
                    dialog = currentDialog.copy(
                        question = currentDialog.question.copy(media = newMedia)
                    )
                )
            } else state
        }
    }

    private fun setQuestionMedia(mimeType: String, media: ByteArray) = viewModelScope.launch {
        val errorMessage = validateMedia(mimeType, media)
        if (errorMessage != null) {
            setErrorMessage(errorMessage)
            return@launch
        }

        val dialog = uiState.value.dialog as? EditGamePackageDialog.EditQuestion ?: return@launch
        var oldMedia = dialog.question.media

        repository.uploadQuestionMedia(
            packageId = packageId,
            bytes = media,
            mimeType = mimeType,
        ).onEach { uploadResult ->
            uploadResult.fold(
                onFailure = { setErrorMessage(getString(Res.string.error_upload_failed)) },
                onSuccess = { media ->
                    updateEditQuestionDialogMedia(
                        oldMedia = oldMedia,
                        newMedia = media
                    )
                    oldMedia = media
                }
            )
        }.collect()

        FullGamePackageUtils.findQuestion(_gamePackage.value, dialog.question)?.let {
            if (it.media?.filename == dialog.question.media?.filename) // check if while upload user changed media again
                upsertGamePackageQuestion(it.copy(media = oldMedia))
        }
    }

    private fun deleteQuestionMedia() {
        val dialog = uiState.value.dialog as? EditGamePackageDialog.EditQuestion ?: return
        val oldMedia = dialog.question.media ?: return

        _uiState.update { state ->
            state.copy(
                dialog = dialog.copy(
                    question = dialog.question.copy(media = oldMedia.copy(progress = 0.5f))
                )
            )
        }

        viewModelScope.launch {
            repository.deleteQuestionMedia(_gamePackage.value.id, oldMedia.filename)
                .handleFailure { getString(Res.string.error_delete_media_failed) }
                .fold(
                    onSuccess = {
                        updateEditQuestionDialogMedia(oldMedia, null)
                        FullGamePackageUtils.findQuestion(_gamePackage.value, dialog.question)
                            ?.let {
                                if (it.media?.filename == oldMedia.filename) // check if while upload user changed media
                                    upsertGamePackageQuestion(it.copy(media = null))
                            }
                    },
                    onFailure = {
                        updateEditQuestionDialogMedia(oldMedia, oldMedia)
                    }
                )
        }
    }

    private fun autoDraftSaver() = viewModelScope.launch {
        while (isActive) {
            delay(AutoSavePeriod)
            saveDraft()
        }
    }

    private fun saveDraft() = viewModelScope.launch {
        _uiState.update { it.copy(isSaveInProgress = true) }

        repository.updatePackageDraft(_gamePackage.value)

        _uiState.update { it.copy(isSaveInProgress = false) }
    }

    private fun uploadPackage() = viewModelScope.launch {
        _uiState.update { it.copy(isUploadInProgress = true) }

        repository.updateGamePackage(_gamePackage.value)
            .onSuccess { _uiState.update { it.copy(isUploaded = true) } }
            .handleFailure { getString(Res.string.error_package_upload_failed) }

        _uiState.update { it.copy(isUploadInProgress = false) }
    }

    private fun downloadPackage() = viewModelScope.launch {
        _uiState.update { it.copy(isDownloadInProgress = true) }

        repository.getGamePackage(packageId)
            .onSuccess { gamePackage ->
                if (gamePackage != null) _gamePackage.update { gamePackage }
            }
            .handleFailure { getString(Res.string.error_package_download_failed) }

        _uiState.update { it.copy(isDownloadInProgress = false) }
    }

    // --- Helper Functions ---

    private fun getRoundOrNull(id: Uuid): FullGameRound? =
        _gamePackage.value.rounds.firstOrNull { it.id == id }

    private fun getCategoryOrNull(id: Uuid): FullGameCategory? =
        _gamePackage.value.rounds.firstNotNullOfOrNull { r -> r.categories.firstOrNull { c -> c.id == id } }

    private fun getQuestionOrNull(id: Uuid): Question? =
        _gamePackage.value.rounds.firstNotNullOfOrNull { r ->
            r.categories.firstNotNullOfOrNull { c ->
                c.questions.firstOrNull { q -> q.id == id }
            }
        }

    private fun deleteGamePackage(onSuccess: () -> Unit) = viewModelScope.launch {
        repository
            .deleteGamePackage(_gamePackage.value.id)
            .handleFailure { getString(Res.string.failed_to_delete_package) }
            .onSuccess { onSuccess() }
    }

    private fun setErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private inline fun Result<*>.handleFailure(message: (Throwable) -> String?) = onFailure { thr ->
        message(thr)?.let { setErrorMessage(it) }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted packageId: Uuid?): EditGamePackageViewModel
    }

    companion object {
        private val AutoSavePeriod = 1.minutes
    }
}