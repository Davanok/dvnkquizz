package com.davanok.dvnkquizz.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.after_confirm_sign_in
import dvnkquizz.sharedui.generated.resources.app_name
import dvnkquizz.sharedui.generated.resources.change_email
import dvnkquizz.sharedui.generated.resources.check_email
import dvnkquizz.sharedui.generated.resources.email_field_label
import dvnkquizz.sharedui.generated.resources.email_sent
import dvnkquizz.sharedui.generated.resources.password_field_label
import dvnkquizz.sharedui.generated.resources.resend_email
import dvnkquizz.sharedui.generated.resources.resend_email_in
import dvnkquizz.sharedui.generated.resources.sign_in
import dvnkquizz.sharedui.generated.resources.sign_up
import dvnkquizz.sharedui.generated.resources.switch_to_sign_in
import dvnkquizz.sharedui.generated.resources.switch_to_sign_up
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = metroViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Content(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Content(
    state: AuthUiState,
    onEvent: (AuthEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Crossfade(targetState = state.isLinkSent, label = "AuthStepTransition") { isLinkSent ->
            if (isLinkSent) {
                EmailLinkSentStep(
                    email = state.email,
                    resendAvailableAt = state.resendAvailableAt,
                    isLoading = state.isLoading,
                    onResendLink = { onEvent(AuthEvent.ResendLink) },
                    onChangeEmail = { onEvent(AuthEvent.ChangeEmail) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                EmailPasswordStep(
                    email = state.email,
                    onEmailChanged = { onEvent(AuthEvent.EmailChanged(it)) },
                    password = state.password,
                    onPasswordChanged = { onEvent(AuthEvent.PasswordChanged(it)) },
                    isLoading = state.isLoading,
                    onSubmitEmail = { onEvent(AuthEvent.SubmitEmail) },
                    isSignUpMode = state.isSignUpMode,
                    onToggleMode = { onEvent(AuthEvent.ToggleMode) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Animated Error Handling
        AnimatedVisibility(visible = state.error != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmailPasswordStep(
    email: String,
    onEmailChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    isLoading: Boolean,
    onSubmitEmail: () -> Unit,
    isSignUpMode: Boolean,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = !isLoading
    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(Res.string.email_field_label)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusRequester.requestFocus() }
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(Res.string.password_field_label)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmitEmail() }
            ),
            visualTransformation = remember { PasswordVisualTransformation() },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            singleLine = true,
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmitEmail,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else {
                Text(
                    if (isSignUpMode) stringResource(Res.string.sign_up)
                    else stringResource(Res.string.sign_in)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onToggleMode,
            enabled = enabled,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (isSignUpMode) stringResource(Res.string.switch_to_sign_in)
                else stringResource(Res.string.switch_to_sign_up)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmailLinkSentStep(
    email: String,
    resendAvailableAt: Instant,
    isLoading: Boolean,
    onResendLink: () -> Unit,
    onChangeEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resendAvailableIn by produceState(0, resendAvailableAt) {
        var delta: Int
        do {
            delta = (resendAvailableAt - Clock.System.now()).inWholeSeconds.toInt()
            value = delta
            delay(100)
        } while (delta > 0)
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(Res.string.check_email),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(Res.string.email_sent, email),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(Res.string.after_confirm_sign_in),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onResendLink,
            modifier = Modifier.fillMaxWidth(),
            enabled = resendAvailableIn <= 0 && !isLoading
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else {
                Text(
                    if (resendAvailableIn > 0) stringResource(Res.string.resend_email_in,)
                    else stringResource(Res.string.resend_email)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onChangeEmail, enabled = !isLoading) {
            Text(stringResource(Res.string.change_email))
        }
    }
}