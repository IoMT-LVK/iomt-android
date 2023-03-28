/**
 * Login activity content
 */

package com.iomt.android.jetpack

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.iomt.android.*
import com.iomt.android.R
import com.iomt.android.entities.AuthInfo

/**
 * Login activity content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
@Suppress("LOCAL_VARIABLE_EARLY_DECLARATION", "FUNCTION_NAME_INCORRECT_CASE")
fun LoginView() {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoginError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    var isSignInFailed by remember { mutableStateOf(false) }

    var authInfo by remember { mutableStateOf<AuthInfo?>(null) }

    val context = LocalContext.current

    val onLoginClicked = {
        isLoginError = login.isBlank()
        isPasswordError = password.isBlank() || password.length < 4 || password.length > 16
        if (!isLoginError && !isPasswordError) {
            sendLoginRequest(login, password, { isSignInFailed = it }) { newAuthInfo ->
                if (newAuthInfo.wasFailed) {
                    isSignInFailed = true
                } else if (!newAuthInfo.confirmed) {
                    val intent = Intent(context, EmailConfirmation::class.java)
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, DeviceListActivity::class.java)
                    context.getSharedPreferences(context.getString(R.string.ACC_DATA), Context.MODE_PRIVATE)
                        .edit()
                        .apply {
                            putString("JWT", newAuthInfo.jwt)
                            putString("UserId", newAuthInfo.userId)
                        }
                        .apply()
                    intent.putExtra("JWT", newAuthInfo.jwt)
                    intent.putExtra("UserId", newAuthInfo.userId)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
                authInfo = newAuthInfo
            }
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

            OutlinedButton({
                val intent = Intent(context, SignupActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }) {
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
    Requests().sendLogin(login, password, updateAuthInfo)
}
