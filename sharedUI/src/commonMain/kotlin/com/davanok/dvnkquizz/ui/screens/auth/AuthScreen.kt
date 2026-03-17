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
            text = "DVNKQuizz",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Use Crossfade for a smooth transition between the input form and the link sent screen
        Crossfade(targetState = state.isLinkSent, label = "AuthStepTransition") { isLinkSent ->
            if (isLinkSent) {
                EmailLinkSentStep(
                    email = state.email,
                    resendUntil = state.resendUntil, // Assuming this exists in your state
                    isLoading = state.isLoading,
                    onResendLink = { onEvent(AuthEvent.ResendLink) }, // Adjust to your actual event
                    onChangeEmail = { onEvent(AuthEvent.ChangeEmail) }, // Adjust to your actual event
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
    password: String, // Note: You might want to remove this if you only use Magic Links now
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
            label = { Text("Email Address") },
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
            label = { Text("Password") },
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
                Text(if (isSignUpMode) "Sign Up" else "Sign In")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onToggleMode,
            enabled = enabled,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (isSignUpMode) "Already have an account? Sign In"
                else "Don't have an account? Sign Up"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmailLinkSentStep(
    email: String,
    resendUntil: Int,
    isLoading: Boolean,
    onResendLink: () -> Unit,
    onChangeEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Check your email",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "We sent a sign-in link to $email. Click the link to securely access your account.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "After confirm email, please sign in",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onResendLink,
            modifier = Modifier.fillMaxWidth(),
            enabled = resendUntil <= 0 && !isLoading
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else {
                Text(if (resendUntil > 0) "Resend link in ${resendUntil}s" else "Resend Link")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onChangeEmail, enabled = !isLoading) {
            Text("Change email")
        }
    }
}