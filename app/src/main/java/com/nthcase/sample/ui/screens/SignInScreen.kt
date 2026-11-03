package com.nthcase.sample.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.AppState
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.theme.ItemNumberStyle

const val SIGN_IN_WRONG = "Wrong email or password"
const val SIGN_IN_MISSING = "Enter your email and password"

@Composable
fun SignInScreen(onSignedIn: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun submit() {
        error = when {
            email.isBlank() || password.isEmpty() -> SIGN_IN_MISSING
            !AppState.credentialsMatch(email, password) -> SIGN_IN_WRONG
            else -> null
        }
        if (error == null) {
            AppState.signIn(email)
            onSignedIn()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestIds.SIGNIN_SCREEN)
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.Shop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Column {
                Text("Mossgrove", style = MaterialTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
                Text("Home and desk goods", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = null },
                label = { Text("Email") },
                singleLine = true,
                isError = error != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestIds.SIGNIN_EMAIL),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Password") },
                singleLine = true,
                isError = error != null,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.testTag(TestIds.SIGNIN_PASSWORD_TOGGLE)) {
                        Icon(
                            if (showPassword) Icons.EyeOff else Icons.Eye,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestIds.SIGNIN_PASSWORD),
            )
            error?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag(TestIds.SIGNIN_ERROR)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
            Button(onClick = { submit() }, modifier = Modifier.fillMaxWidth().height(48.dp).testTag(TestIds.SIGNIN_SUBMIT)) {
                Text("Sign in")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedCard(modifier = Modifier.fillMaxWidth().testTag(TestIds.SIGNIN_TEST_ACCOUNT).semantics(mergeDescendants = true) {}) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Test account", style = MaterialTheme.typography.titleSmall)
                    Text("This is a sample app. Sign in with:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(AppState.TEST_EMAIL, style = ItemNumberStyle)
                    Text(AppState.TEST_PASSWORD, style = ItemNumberStyle)
                    TextButton(
                        onClick = { email = AppState.TEST_EMAIL; password = AppState.TEST_PASSWORD; error = null },
                        modifier = Modifier.testTag(TestIds.SIGNIN_USE_TEST_ACCOUNT),
                    ) { Text("Use test account") }
                }
            }
        }
    }
}
