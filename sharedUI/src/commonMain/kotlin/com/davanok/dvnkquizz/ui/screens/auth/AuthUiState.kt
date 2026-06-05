package com.davanok.dvnkquizz.ui.screens.auth

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.auth.enums.UserAuthErrorCode
import kotlin.time.Clock
import kotlin.time.Instant

@Immutable
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val token: String = "",
    val isLoading: Boolean = false,
    val error: AuthUiError? = null,
    val isSignUpMode: Boolean = false,
    val isLinkSent: Boolean = false,
    val resendAvailableAt: Instant = Clock.System.now()
) {
    val generalError: AuthUiError? get() = error?.takeIf { !it.isFieldLevel }
    val emailError: AuthUiError? get() = error?.takeIf { it.field == AuthErrorField.Email }
    val passwordError: AuthUiError? get() = error?.takeIf { it.field == AuthErrorField.Password }
}
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

data class AuthUiError(
    val errorCode: UserAuthErrorCode,
    val description: String
) {
    val field get() = errorCode.field
    val isFieldLevel get() = field != null
}

enum class AuthErrorField {
    Email,
    Password
}

val UserAuthErrorCode.field: AuthErrorField?
    get() = when (this) {
        UserAuthErrorCode.EmailExists,
        UserAuthErrorCode.EmailNotConfirmed,
        UserAuthErrorCode.EmailAddressInvalid,
        UserAuthErrorCode.EmailAddressNotAuthorized,
        UserAuthErrorCode.ProviderEmailNeedsVerification -> AuthErrorField.Email

        UserAuthErrorCode.WeakPassword,
        UserAuthErrorCode.SamePassword,
        UserAuthErrorCode.InvalidCredentials -> AuthErrorField.Password

        else -> null
    }