package com.davanok.dvnkquizz.ui.screens.auth

import androidx.compose.runtime.Immutable
import kotlin.time.Clock
import kotlin.time.Instant

@Immutable
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val token: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpMode: Boolean = false,
    val isLinkSent: Boolean = false,
    val resendAvailableAt: Instant = Clock.System.now()
)
sealed interface AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent
    data class PasswordChanged(val password: String) : AuthEvent
    data class TokenChanged(val token: String) : AuthEvent
    data object ToggleMode : AuthEvent
    data object SubmitEmail : AuthEvent
    data object DismissError : AuthEvent
    data object ResendLink : AuthEvent
    data object ChangeEmail : AuthEvent
}