package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.FullGameRound
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.enums.QuestionType
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
                    errorMessage = null,
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
                        errorMessage = null,
                        gamePackage = gamePackage
                    )
                },
                onFailure = { thr ->
                    state.copy(
                        isLoading = false,
                        errorMessage = thr.message,
                        gamePackage = FullGamePackage.Empty
                    )
                }
            )
        }
    }

    fun eventSink(event: EditGamePackageUiEvent) {
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

            is EditGamePackageUiEvent.AddRound -> {
                val newRound = FullGameRound(
                    id = Uuid.random(),
                    name = event.name,
                    ordinal = _gamePackage.value.rounds.lastOrNull()?.ordinal?.plus(1) ?: 1,
                    categories = emptyList()
                )
                _gamePackage.update {
                    it.copy(rounds = it.rounds + newRound)
                }
            }
            is EditGamePackageUiEvent.AddCategory -> {
                val updateRoundIndex = _gamePackage.value.rounds
                    .indexOfFirst { it.id == event.roundId }
                val updateRound = _gamePackage.value.rounds[updateRoundIndex]

                val newCategory = FullGameCategory(
                    id = Uuid.random(),
                    name = event.name,
                    ordinal = updateRound.categories.lastOrNull()?.ordinal?.plus(1) ?: 1,
                    questions = emptyList()
                )

                val updatedRounds = _gamePackage.value.rounds.toMutableList()
                updatedRounds[updateRoundIndex] = updateRound.copy(categories = updateRound.categories + newCategory)

                _gamePackage.update {
                    it.copy(rounds = updatedRounds)
                }
            }

            is EditGamePackageUiEvent.NewQuestion -> {
                val newQuestion = Question(
                    id = Uuid.random(),
                    categoryId = event.categoryId,
                    questionText = "",
                    answerText = "",
                    price = 0,
                    type = QuestionType.NORMAL,
                    media = null
                )

                _uiState.update {
                    it.copy(editQuestion = newQuestion)
                }
            }

            is EditGamePackageUiEvent.EditQuestion -> {
                var questionToEdit: Question? = null

                _gamePackage.value.rounds.forEach root@ { round ->
                    round.categories.forEach { category ->
                        category.questions.forEach { question ->
                            if (question.id == event.questionId) {
                                questionToEdit = question
                                return@root
                            }
                        }
                    }
                }

                _uiState.update {
                    it.copy(editQuestion = questionToEdit)
                }
            }
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted packageId: Uuid?): EditGamePackageViewModel
    }
}