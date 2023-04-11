/**
 * Login activity content
 */

package com.iomt.android.jetpack.view.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.credentialmanager.AppCredentialManager
import com.iomt.android.entities.AuthInfo
import com.iomt.android.http.RequestParams
import com.iomt.android.http.sendLogin
import com.iomt.android.jetpack.theme.colorScheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Login activity content
 *
 * @param navigateToRegistration callback to navigate to RegistrationView
 * @param navigateToMain callback to navigate to after-login part of the app
 * @param navigateToEmailConf callback to navigate to EmailConfView
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
fun LoginView(
    navigateToRegistration: () -> Unit,
    navigateToMain: () -> Unit,
    navigateToEmailConf: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var credentialManager by remember { mutableStateOf<AppCredentialManager?>(null) }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignInFailed by remember { mutableStateOf(false) }

    var isLoginError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    val onLoginClicked = {
        isLoginError = login.isBlank()
        isPasswordError = password.isBlank() || password.length < 4 || password.length > 16
        if (!isLoginError && !isPasswordError) {
            sendLoginRequest(login, password, { isSignInFailed = it }) { newAuthInfo ->
                if (newAuthInfo.wasFailed) {
                    isSignInFailed = true
                } else if (!newAuthInfo.confirmed) {
                    MainScope().launch { navigateToEmailConf() }
                } else {
                    RequestParams.userId = newAuthInfo.userId
                    scope.launch {
                        RequestParams.credentials?.let { credentialsToSave ->
                            credentialManager?.createIfNotPresent(credentialsToSave)
                        }
                    }
                    MainScope().launch { navigateToMain() }
                }
            }
        }
    }

    LaunchedEffect(context) {
        credentialManager = AppCredentialManager(context)
        credentialManager?.get()?.also {
            login = it.login
            password = it.password
            onLoginClicked()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null
        )

        OutlinedTextField(
            login,
            { login = it },
            isError = isLoginError,
            singleLine = true,
            label = { Text(stringResource(R.string.login)) },
            supportingText = {
                if (isLoginError) {
                    Text("Введите корректный логин")
                }
            }
        )
        OutlinedTextField(
            password,
            { password = it },
            isError = isPasswordError,
            singleLine = true,
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            supportingText = {
                if (isPasswordError) {
                    Text("Не менее 4 и не более 14 символов")
                }
            }
        )

        Column(
            modifier = Modifier.padding(vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button({ onLoginClicked() }) {
                Text(stringResource(R.string.login_text))
            }

            OutlinedButton(navigateToRegistration) {
                Text(stringResource(R.string.create_account))
            }
        }
    }
    if (isSignInFailed) {
        Toast.makeText(context, "Не удалось войти", Toast.LENGTH_LONG).show()
    }
}

private fun sendLoginRequest(
    login: String,
    password: String,
    updateIsSignupFailed: (Boolean) -> Unit,
    updateAuthInfo: (AuthInfo) -> Unit,
) {
    if (login.isEmpty() || password.isEmpty() || password.length < 4 || password.length > 14) {
        updateIsSignupFailed(true)
    }
    sendLogin(login, password, updateAuthInfo)
}

@Preview
@Composable
private fun LoginViewPreview() {
    MaterialTheme(colorScheme) { LoginView({ }, { }) { } }
}
