/**
 * Login activity content
 */

package com.iomt.android.compose.view.login

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.iomt.android.R
import com.iomt.android.compose.components.textfield.Cell
import com.iomt.android.compose.components.textfield.TextField
import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.entities.SignUpInfo
import com.iomt.android.http.sendReg

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * SignUp activity content
 *
 * @param navigateToEmailConf callback to navigate to EmailConfView
 */
@Composable
@Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
fun RegistrationView(navigateToEmailConf: () -> Unit) {
    var signUpInfo by remember { mutableStateOf(SignUpInfo.empty) }
    var secondPassword by remember { mutableStateOf("") }
    var isValidationFailed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val onSignUpClicked = {
        validateAndSend(
            signUpInfo,
            { isValidationFailed = true },
            { MainScope().launch { Toast.makeText(context, "Error occurred on sign up.", Toast.LENGTH_SHORT).show() } },
        ) { response ->
            if (!response.isNullOrBlank()) {
                MainScope().launch {
                    Toast.makeText(context, response, Toast.LENGTH_LONG).show()
                }
            } else {
                MainScope().launch { navigateToEmailConf() }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(R.drawable.logo), "logo", Modifier.size(200.dp))
        OutlinedCard {
            Text("Sign up", modifier = Modifier.padding(15.dp), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.padding(10.dp))

        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround) {
            TextField(Cell(
                signUpInfo.name,
                description = "Name",
                validator = { !isValidationFailed || signUpInfo.isNameValid() },
            ) { signUpInfo = signUpInfo.copy(name = it) })
            TextField(Cell(
                signUpInfo.surname,
                description = "Surname",
                validator = { !isValidationFailed || signUpInfo.isSurnameValid() },
            ) { signUpInfo = signUpInfo.copy(surname = it) })
            TextField(Cell(
                signUpInfo.patronymic,
                description = "Patronymic",
                validator = { !isValidationFailed || signUpInfo.isPatronymicValid() },
            ) { signUpInfo = signUpInfo.copy(patronymic = it) })
            TextField(Cell(
                signUpInfo.birthdate,
                description = "Birthdate",
                validator = { !isValidationFailed || signUpInfo.isBirthdateValid() },
            ) { signUpInfo = signUpInfo.copy(birthdate = it) })
            TextField(Cell(
                signUpInfo.email,
                description = "Email",
                validator = { !isValidationFailed || signUpInfo.isEmailValid() },
            ) { signUpInfo = signUpInfo.copy(email = it) })
            TextField(Cell(
                signUpInfo.phoneNumber,
                description = "Phone number",
                validator = { !isValidationFailed || signUpInfo.isPhoneValid() },
            ) { signUpInfo = signUpInfo.copy(phoneNumber = it) })
            TextField(Cell(
                signUpInfo.login,
                description = "Login",
                validator = { !isValidationFailed || signUpInfo.isLoginValid() },
            ) { signUpInfo = signUpInfo.copy(login = it) })
            TextField(Cell(
                signUpInfo.password,
                description = "Password",
                validator = { !isValidationFailed || signUpInfo.isPasswordValid() },
            ) { signUpInfo = signUpInfo.copy(password = it) })
            TextField(Cell(
                secondPassword,
                description = "Confirm password",
                validator = { !isValidationFailed || SignUpInfo.isPasswordValid(secondPassword) },
            ) { secondPassword = it })
        }

        Column(
            modifier = Modifier.padding(vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onSignUpClicked) {
                Text(stringResource(R.string.create_account))
            }
        }
    }
}

private fun validateAndSend(
    signUpInfo: SignUpInfo,
    onValidationFailed: () -> Unit,
    onSignUpFailed: () -> Unit,
    onSignUpSuccess: (String?) -> Unit,
) {
    if (signUpInfo.isValid()) {
        sendReg(signUpInfo, onSignUpFailed, onSignUpSuccess)
    } else {
        onValidationFailed()
    }
}

@Preview
@Composable
private fun RegistrationViewPreview() {
    MaterialTheme(colorScheme) { RegistrationView { } }
}
