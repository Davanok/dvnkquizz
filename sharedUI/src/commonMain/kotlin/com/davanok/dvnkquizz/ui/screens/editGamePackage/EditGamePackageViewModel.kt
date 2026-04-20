package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.FullGameRound
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.mappers.toFullGameCategory
import com.davanok.dvnkquizz.core.domain.mappers.toFullGameRound
import com.davanok.dvnkquizz.core.domain.mappers.toGameCategory
import com.davanok.dvnkquizz.core.domain.mappers.toGameRound
import com.davanok.dvnkquizz.core.domain.repositories.UserGamePackagesRepository
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
import dvnkquizz.sharedui.generated.resources.error_package_upload_failed
import dvnkquizz.sharedui.generated.resources.error_upload_failed
import dvnkquizz.sharedui.generated.resources.filetype_not_supported
import dvnkquizz.sharedui.generated.resources.max_question_media_size
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.withScopedAccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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
    private val repository: UserGamePackagesRepository
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

                    is EditGamePackageUiEvent.ShowDialog -> showDialog(event.dialogRequest)
                    EditGamePackageUiEvent.CloseDialog -> _uiState.update { it.copy(dialog = null) }

                    is EditGamePackageUiEvent.UpdateRound -> updateGamePackageRound(event.round.id) {
                        it.copy(name = event.round.name, ordinal = event.round.ordinal)
                    }
                    is EditGamePackageUiEvent.UpdateCategory -> updateGamePackageCategory(event.category.id) {
                        it.copy(name = event.category.name, ordinal = event.category.ordinal)
                    }
                    is EditGamePackageUiEvent.UpdateQuestion -> updateGamePackageQuestion(event.question.id) {
                        event.question
                    }

                    is EditGamePackageUiEvent.AddRound -> addGamePackageRound(event.round.toFullGameRound())
                    is EditGamePackageUiEvent.AddCategory -> addGamePackageCategory(event.roundId, event.category.toFullGameCategory())
                    is EditGamePackageUiEvent.AddQuestion -> addGamePackageQuestion(event.categoryId, event.question)

                    EditGamePackageUiEvent.OpenQuestionMediaSelector -> openMediaSelector()
                    EditGamePackageUiEvent.RemoveQuestionMedia -> deleteQuestionMedia()
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
        _uiState.update { it.copy(isLoading = true) }

        if (isNew) {
            _gamePackage.update { FullGamePackage.Empty }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    criticalError = null,
                    isUploaded = false
                )
            }
            return@launch
        }

        val draft = repository.getPackageDraft(packageId).getOrNull()
        if (draft != null) {
            _gamePackage.update { draft }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    criticalError = null,
                    isUploaded = false
                )
            }
        } else {
            repository.getGamePackage(packageId).fold(
                onSuccess = { gamePackage ->
                    _gamePackage.update { gamePackage }
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            criticalError = null,
                            isUploaded = true
                        )
                    }
                },
                onFailure = { thr ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            criticalError = thr.message,
                            isUploaded = false
                        )
                    }
                }
            )
        }
    }

    private fun showDialog(dialogRequest: EditGamePackageDialogRequest) {
        val dialog = when (dialogRequest) {
            EditGamePackageDialogRequest.AddRound ->
                EditGamePackageDialog.AddRound

            is EditGamePackageDialogRequest.EditRound -> {
                val round = getRoundOrNull(dialogRequest.roundId) ?: return
                EditGamePackageDialog.EditRound(round.toGameRound())
            }

            is EditGamePackageDialogRequest.AddCategory ->
                EditGamePackageDialog.AddCategory(dialogRequest.roundId)

            is EditGamePackageDialogRequest.EditCategory -> {
                val category = getCategoryOrNull(dialogRequest.categoryId) ?: return
                EditGamePackageDialog.EditCategory(category.toGameCategory())
            }

            is EditGamePackageDialogRequest.AddQuestion -> {
                EditGamePackageDialog.AddQuestion(dialogRequest.categoryId, null)
            }

            is EditGamePackageDialogRequest.EditQuestion -> {
                val question = getQuestionOrNull(dialogRequest.questionId) ?: return
                EditGamePackageDialog.EditQuestion(question, null)
            }
        }

        _uiState.update { it.copy(dialog = dialog) }
    }

    private suspend fun validateMedia(file: PlatformFile): String? {
        if (file.size() > GamePackageLimits.QUESTION_MEDIA_MAX_SIZE) {
            return getString(Res.string.max_question_media_size, GamePackageLimits.QUESTION_MEDIA_MAX_SIZE)
        }
        if (file.extension.lowercase() !in GamePackageLimits.allowedMediaFileExtensions) {
            return getString(Res.string.filetype_not_supported, file.extension, GamePackageLimits.allowedMediaFileExtensions)
        }
        val mimeType = file.mimeType()
        if (mimeType == null || mimeType !in GamePackageLimits.allowedMediaFileMimeTypes) {
            return getString(Res.string.filetype_not_supported, mimeType.toString(), GamePackageLimits.allowedMediaFileMimeTypes.map { it.toString() })
        }
        return null
    }

    private fun updateGamePackageQuestion(questionId: Uuid, block: (Question) -> Question) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.updateQuestion(it, questionId, block) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_question)
        }
    }
    private fun updateGamePackageCategory(categoryId: Uuid, block: (FullGameCategory) -> FullGameCategory) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.updateCategory(it, categoryId, block) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_category)
        }
    }
    private fun updateGamePackageRound(roundId: Uuid, block: (FullGameRound) -> FullGameRound) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.updateRound(it, roundId, block) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_round)
        }
    }

    private fun addGamePackageQuestion(categoryId: Uuid, question: Question) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.addQuestion(it, categoryId, question) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_question)
        }
    }
    private fun addGamePackageCategory(roundId: Uuid, category: FullGameCategory) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.addCategory(it, roundId, category) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_category)
        }
    }
    private fun addGamePackageRound(round: FullGameRound) = viewModelScope.launch {
        runCatching {
            _gamePackage.update { FullGamePackageUtils.addRound(it, round) }
        }.handleFailure {
            getString(Res.string.error_failed_to_update_round)
        }
    }

    private fun openMediaSelector() {
        val dialog = uiState.value.dialog as? EditGamePackageDialog.EditQuestion ?: return
        val question = dialog.question

        viewModelScope.launch {
            val file = FileKit.openFilePicker(
                type = FileKitType.File(GamePackageLimits.allowedMediaFileExtensions)
            ) ?: return@launch

            file.withScopedAccess {
                val errorMessage = validateMedia(file)
                if (errorMessage != null) {
                    _uiState.update { it.copy(errorMessage = errorMessage) }
                    return@launch
                }

                repository.uploadQuestionMedia(
                    packageId = packageId,
                    bytes = file.readBytes(),
                    mimeType = file.mimeType().toString(),
                ).collect { uploadResult ->
                    uploadResult.fold(
                        onFailure = {
                            setErrorMessage(getString(Res.string.error_upload_failed))
                        },
                        onSuccess = { media ->
                            val currentDialog = uiState.value.dialog
                            if (currentDialog is EditGamePackageDialog.EditQuestion) {
                                _uiState.update {
                                    it.copy(
                                        dialog = currentDialog.copy(question = currentDialog.question.copy(media = media))
                                    )
                                }
                            }

                            if (media.progress >= 1f)
                                updateGamePackageQuestion(question.id) { it.copy(media = media) }
                        }
                    )
                }
            }
        }
    }

    private fun deleteQuestionMedia() {
        val dialog = uiState.value.dialog as? EditGamePackageDialog.EditQuestion ?: return
        val media = dialog.question.media ?: return

        _uiState.update { state ->
            state.copy(
                dialog = dialog.copy(
                    question = dialog.question.copy(media = media.copy(progress = 0.5f))
                )
            )
        }

        viewModelScope.launch {
            repository.deleteQuestionMedia(dialog.question.id)
                .handleFailure { getString(Res.string.error_delete_media_failed) }
                .fold(
                    onSuccess = {
                        val currentDialog = uiState.value.dialog

                        if (currentDialog is EditGamePackageDialog.EditQuestion && currentDialog.question.media?.url == media.url) {
                            _uiState.update {
                                it.copy(
                                    dialog = currentDialog.copy(
                                        question = currentDialog.question.copy(
                                            media = null
                                        )
                                    )
                                )
                            }
                        }
                        updateGamePackageQuestion(dialog.question.id) { it.copy(media = null) }
                    },
                    onFailure = {
                        val currentDialog = uiState.value.dialog
                        if (currentDialog is EditGamePackageDialog.EditQuestion && currentDialog.question.media?.url == media.url) {
                            _uiState.update {
                                it.copy(
                                    dialog = currentDialog.copy(
                                        question = currentDialog.question.copy(
                                            media = media
                                        )
                                    )
                                )
                            }
                        }
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

    private fun setErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
    private inline fun Result<*>.handleFailure(message: (Throwable) -> String?) = onFailure { thr ->
        message(thr)?.let { setErrorMessage(it) }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted packageId: Uuid?): EditGamePackageViewModel
    }

    companion object {
        private val AutoSavePeriod = 1.minutes
    }
}