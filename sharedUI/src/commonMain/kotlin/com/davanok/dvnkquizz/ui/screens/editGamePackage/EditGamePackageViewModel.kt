package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.GameCategory
import com.davanok.dvnkquizz.core.domain.entities.GameRound
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.enums.QuestionType
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
class EditGamePackageViewModel(
    @Assisted private val packageId: Uuid?,
    private val repository: UserGamePackagesRepository
): ViewModel() {
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
        viewModelScope.launch {
            loadGamePackage()
        }
    }

    private suspend fun loadGamePackage() {
        if (packageId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    criticalError = null,
                    gamePackage = FullGamePackage.Empty
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        val result = repository.getGamePackage(packageId)

        _uiState.update { state ->
            result.fold(
                onSuccess = { gamePackage ->
                    state.copy(
                        isLoading = false,
                        criticalError = null,
                        gamePackage = gamePackage
                    )
                },
                onFailure = { thr ->
                    state.copy(
                        isLoading = false,
                        criticalError = thr.message,
                        gamePackage = FullGamePackage.Empty
                    )
                }
            )
        }
    }

    fun eventSink(event: EditGamePackageUiEvent) {
        runCatching {
            when (event) {
                EditGamePackageUiEvent.SaveDraft -> TODO()
                EditGamePackageUiEvent.UploadPackage -> TODO()

                is EditGamePackageUiEvent.SetTitle -> {
                    if (event.title.length <= GamePackageLimits.TITLE_MAX_LENGTH)
                        _gamePackage.update {
                            it.copy(title = event.title)
                        }
                }

                is EditGamePackageUiEvent.SetDescription -> {
                    if (event.description.length <= GamePackageLimits.DESCRIPTION_MAX_LENGTH)
                        _gamePackage.update {
                            it.copy(description = event.description)
                        }
                }

                is EditGamePackageUiEvent.SetDifficulty -> {
                    if (event.difficulty <= GamePackageLimits.DIFFICULTY_MAX_VALUE)
                        _gamePackage.update {
                            it.copy(difficulty = event.difficulty)
                        }
                }

                is EditGamePackageUiEvent.ShowDialog -> showDialog(event.dialogRequest)
                EditGamePackageUiEvent.CloseDialog -> _uiState.update { it.copy(dialog = null) }

                is EditGamePackageUiEvent.UpdateRound -> TODO()
                is EditGamePackageUiEvent.UpdateCategory -> TODO()
                is EditGamePackageUiEvent.UpdateQuestion -> TODO()
            }
        }.onFailure { thr ->
            Logger.e(thr) { "failed to process uiEvent $event" }
            _uiState.update { it.copy(errorMessage = thr.message) }
        }
    }

    private fun showDialog(dialogRequest: EditGamePackageDialogRequest) {
        val dialog = when(dialogRequest) {
            EditGamePackageDialogRequest.AddRound -> {
                val ordinal = _gamePackage.value.rounds.maxOfOrNull { it.ordinal }?.plus(1) ?: 0
                EditGamePackageDialog.EditRound(GameRound(ordinal = ordinal))
            }
            is EditGamePackageDialogRequest.EditRound -> {
                val round = _gamePackage.value.rounds.first { it.id == dialogRequest.roundId }
                EditGamePackageDialog.EditRound(round.toGameRound())
            }

            is EditGamePackageDialogRequest.AddCategory -> {
                val round = _gamePackage.value.rounds.first { it.id == dialogRequest.roundId }
                val ordinal = round.categories.maxOfOrNull { it.ordinal }?.plus(1) ?: 0
                EditGamePackageDialog.EditCategory(GameCategory(ordinal = ordinal))
            }
            is EditGamePackageDialogRequest.EditCategory -> {
                val category = _gamePackage.value.rounds.firstNotNullOf { round ->
                    round.categories.firstOrNull { it.id == dialogRequest.categoryId }
                }
                EditGamePackageDialog.EditCategory(category.toGameCategory())
            }

            is EditGamePackageDialogRequest.AddQuestion -> {
                val question = Question(
                    id = Uuid.random(),
                    categoryId = dialogRequest.categoryId,
                    questionText = "",
                    answerText = "",
                    price = 0,
                    type = QuestionType.NORMAL,
                    media = null
                )
                EditGamePackageDialog.EditQuestion(question)
            }
            is EditGamePackageDialogRequest.EditQuestion -> {
                val question = _gamePackage.value.rounds.firstNotNullOf { round ->
                    round.categories.firstNotNullOfOrNull { category ->
                        category.questions.firstOrNull { it.id == dialogRequest.questionId }
                    }
                }
                EditGamePackageDialog.EditQuestion(question)
            }
        }

        _uiState.update { it.copy(dialog = dialog) }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted packageId: Uuid?): EditGamePackageViewModel
    }
}