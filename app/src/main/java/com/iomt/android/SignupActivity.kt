package com.iomt.android

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SignupActivity : AppCompatActivity() {
    private var year = 0
    private var month = 0
    private var day = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var nameText: EditText
    private lateinit var surnameText: EditText
    private lateinit var patronymicText: EditText
    private lateinit var birthdateText: EditText
    private lateinit var emailText: EditText
    private lateinit var mobileText: EditText
    private lateinit var loginText: EditText
    private lateinit var passwordText: EditText
    private lateinit var reEnterPasswordText: EditText
    private lateinit var signupButton: LinearLayout
    private lateinit var loginLink: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        progressBar = ProgressBar(this)

        nameText = findViewById(R.id.input_name)
        surnameText = findViewById(R.id.input_surname)
        patronymicText = findViewById(R.id.input_patronymic)
        birthdateText = findViewById(R.id.input_birthdate)
        emailText = findViewById(R.id.input_email)
        mobileText = findViewById(R.id.input_mobile)
        loginText = findViewById(R.id.input_login)
        passwordText = findViewById(R.id.input_password)
        reEnterPasswordText = findViewById(R.id.input_reEnterPassword)

        signupButton = findViewById(R.id.btn_signup)
        signupButton.setOnClickListener { signup() }

        loginLink = findViewById(R.id.link_login)
        loginLink.setOnClickListener {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
        birthdateText.setOnClickListener { callDatePicker() }
    }

    private fun callDatePicker() {
        val cal = Calendar.getInstance()
        if (year == 0) {
            year = cal[Calendar.YEAR]
            month = cal[Calendar.MONTH]
            day = cal[Calendar.DAY_OF_MONTH]
        }
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.MySpinnerDatePickerStyle,
            { _, currentYear, monthOfYear, dayOfMonth ->
                val zm = if (monthOfYear + 1 >= 10) "" else "0"
                val zd = if (dayOfMonth >= 10) "" else "0"
                val editTextDateParam = "$zd$dayOfMonth.$zm${monthOfYear + 1}.$currentYear"
                birthdateText.setText(editTextDateParam)
                year = currentYear
                month = monthOfYear
                day = dayOfMonth
            }, year, month, day
        )
        datePickerDialog.show()
    }

    private fun signup() {
        Log.d(TAG, "Signup")
        if (!validate()) {
            onSignupFailed("Validation failed.")
            return
        }
        signupButton.isEnabled = false
        progressBar.isIndeterminate = true
        progressBar.visibility = ProgressBar.VISIBLE
        Requests(this).sendReg(
            nameText.text.toString(),
            surnameText.text.toString(),
            patronymicText.text.toString(),
            birthdateText.text.toString(),
            emailText.text.toString(),
            mobileText.text.toString(),
            loginText.text.toString(),
            passwordText.text.toString(),
            { runOnUiThread {  onSignupFailed("") } }
        ) { args: Array<String?>? ->
            runOnUiThread {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        val arg = requireNotNull(args?.get(0))
                        if (arg.isNotEmpty()) {
                            onSignupFailed(arg)
                        } else {
                            onSignupSuccess()
                        }
                        progressBar.visibility = ProgressBar.INVISIBLE
                    }, 1000
                )
            }
        }
    }

    private fun onSignupSuccess() {
        signupButton.isEnabled = true
        setResult(RESULT_OK, null)
        val intent = Intent(this, EmailConf::class.java)
        startActivity(intent)
        finish()
    }

    private fun onSignupFailed(err: String?) {
        Toast.makeText(baseContext, COULD_NOT_SIGNUP_ERROR_TEXT, Toast.LENGTH_LONG).show()
        signupButton.isEnabled = true
        if (err == "email") {
            emailText.error = EMAIL_IS_BUSY_ERROR_TEXT
        } else if (err == "login") {
            loginText.error = LOGIN_IS_BUSY_ERROR_TEXT
        }
    }

    private fun validateName() = validateStringForm(nameText, NAME_ERROR_TEXT)
    private fun validateSurname() = validateStringForm(surnameText, SURNAME_ERROR_TEXT)
    private fun validatePatronymic() = validateStringForm(patronymicText, PATRONYMIC_ERROR_TEXT)

    private fun validateBirthdate() = validateStringForm(birthdateText, NO_BIRTHDAY_ERROR_TEXT) {
        it.isEmpty()
    }

    private fun validateBirthday(year: Int, month: Int, day: Int): Boolean {
        val calendar = Calendar.getInstance()
        val calYear = calendar[Calendar.YEAR]
        val calMonth = calendar[Calendar.MONTH]
        val calDay = calendar[Calendar.DAY_OF_MONTH]
        return if (
            year == 0 ||
            year > calYear ||
            year == calYear && month > calMonth ||
            year == calYear && month == calMonth && day > calDay
        ) {
            birthdateText.error = BIRTHDAY_ERROR_TEXT
            false
        } else {
            birthdateText.error = null
            true
        }
    }

    private fun validateLogin() = validateStringForm(loginText, LOGIN_ERROR_TEXT)

    private fun validateEmail() = validateStringForm(emailText, EMAIL_ERROR_TEXT) {
        it.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(it).matches()
    }

    private fun validatePhoneNumber() =  validateStringForm(mobileText, PHONE_NUMBER_ERROR_TEXT) {
        it.length != 11
    }

    private fun validatePasswords(): Boolean = validateStringForm(passwordText, PASSWORD_ERROR_TEXT) {
            it.length < 4 || it.length > 14
        } && validateStringForm(reEnterPasswordText, SECOND_PASSWORD_ERROR_TEXT) {
            it.length < 4 || it.length > 14
        }

    private fun validateStringForm(
        form: EditText,
        errorText: String,
        isInvalidFunc: (String) -> Boolean = { it.length < 2 },
    ) = form.text.toString().let { textFromForm ->
        if (isInvalidFunc(textFromForm)) {
            form.error = errorText
            false
        } else {
            form.error = null
            true
        }
    }

    private fun validate(): Boolean {
        val validateNames = validateName() && validateSurname() && validatePatronymic()
        val validateCredentials = validateLogin() && validateEmail() && validatePasswords() && validatePhoneNumber()
        val validateDates = validateBirthdate() && validateBirthday(year, month, day)
        return validateNames && validateCredentials && validateDates
    }

    companion object {
        private const val TAG = "SignupActivity"

        private const val NAME_ERROR_TEXT = "Не менее 2 символов"
        private const val SURNAME_ERROR_TEXT = "Не менее 2 символов"
        private const val PATRONYMIC_ERROR_TEXT = "Не менее 2 символов"

        private const val LOGIN_ERROR_TEXT = "Не менее 2 символов"
        private const val EMAIL_ERROR_TEXT = "Введите корректно e-mail"
        private const val PHONE_NUMBER_ERROR_TEXT = "Введите корректно номер телефона"

        private const val PASSWORD_ERROR_TEXT = "Не менее 4 и не более 14 символов"
        private const val SECOND_PASSWORD_ERROR_TEXT = "Пароли не совпадают"

        private const val NO_BIRTHDAY_ERROR_TEXT = "Введите дату рождения"
        private const val BIRTHDAY_ERROR_TEXT = "Введите корректно дату рождения"

        private const val EMAIL_IS_BUSY_ERROR_TEXT = "Этот e-mail занят"
        private const val LOGIN_IS_BUSY_ERROR_TEXT = "Этот логин занят"

        private const val COULD_NOT_SIGNUP_ERROR_TEXT = "Не удалось зарегистрировать"
    }
}