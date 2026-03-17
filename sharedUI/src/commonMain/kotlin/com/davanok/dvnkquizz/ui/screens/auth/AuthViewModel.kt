package com.davanok.dvnkquizz.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.repositories.AuthRepository
import com.davanok.dvnkquizz.core.utils.EmailPattern
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

@Inject
@ViewModelKey(AuthViewModel::class)
@ContributesIntoMap(AppScope::class)
class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> _state.update {
                it.copy(
                    email = event.email,
                    error = null
                )
            }

            is AuthEvent.PasswordChanged -> _state.update {
                it.copy(
                    password = event.password,
                    error = null
                )
            }

            is AuthEvent.TokenChanged -> _state.update {
                it.copy(
                    token = event.token,
                    error = null
                )
            }

            AuthEvent.ToggleMode -> _state.update {
                it.copy(
                    isSignUpMode = !it.isSignUpMode,
                    error = null
                )
            }

            AuthEvent.SubmitEmail -> submitEmail()
            AuthEvent.DismissError -> _state.update { it.copy(error = null) }
            AuthEvent.ChangeEmail -> _state.update { it.copy(isLinkSent = false) }
            AuthEvent.ResendLink -> resendToken()
        }
    }

    private fun submitEmail() {
        val currentState = _state.value

        // Prevent double submission if a request is already in flight
        if (currentState.isLoading) return

        if (!Regex.EmailPattern.matches(currentState.email)) {
            _state.update { it.copy(error = "Invalid email") } // Consider using StringRes IDs here
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = if (currentState.isSignUpMode) {
                repository.signUpWithEmail(currentState.email, currentState.password)
            } else {
                repository.signInWithEmail(currentState.email, currentState.password)
            }

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isLinkSent = true) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    private fun resendToken() {
        val currentState = _state.value
        if (currentState.isLoading) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Assuming resendVerificationCode returns a Result like the other methods
            repository.resendEmail(currentState.email)
                .fold(
                    onSuccess = {
                        _state.update { it.copy(isLoading = false) }
                        startResendTimer()
                    },
                    onFailure = { error ->
                        _state.update { it.copy(isLoading = false, error = error.message) }
                    }
                )
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val endTime = Clock.System.now().epochSeconds + RESEND_TOKEN_TIMEOUT

            while (true) {
                val remaining = (endTime - Clock.System.now().epochSeconds).toInt()

                if (remaining <= 0) {
                    _state.update { it.copy(resendUntil = 0) }
                    break
                }

                _state.update { it.copy(resendUntil = remaining) }
                delay(1000)
            }
        }
    }

    companion object {
        private const val RESEND_TOKEN_TIMEOUT = 60
    }
}