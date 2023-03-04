package com.iomt.android

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

/**
 * Activity that is responsible for signing up
 */
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
        @Suppress("MAGIC_NUMBER")
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.MySpinnerDatePickerStyle,
            { _,
                currentYear,
                monthOfYear,
                dayOfMonth ->
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
        if (!isValid()) {
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
            { runOnUiThread { onSignupFailed("") } }
        ) { args: Array<String?>? ->
            runOnUiThread {
                Handler(Looper.getMainLooper()).postDelayed({
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
        val intent = Intent(this, EmailConfirmation::class.java)
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

    private fun isValid(): Boolean {
        val validateNames = nameText.isNameValid() && surnameText.isSurnameValid() && patronymicText.isPatronymicValid()
        val validateCredentials = loginText.isLoginValid() && emailText.isEmailValid() && mobileText.isPhoneNumberValid()
        val validatePasswords = passwordText.isFirstPasswordValid() && reEnterPasswordText.isSecondPasswordValid()
        val validateDates = birthdateText.isBirthdateValid() && birthdateText.isBirthdayValid(year, month, day)
        return validateNames && validateCredentials && validatePasswords && validateDates
    }

    companion object {
        private const val COULD_NOT_SIGNUP_ERROR_TEXT = "Не удалось зарегистрировать"
        private const val EMAIL_IS_BUSY_ERROR_TEXT = "Этот e-mail занят"
        private const val LOGIN_IS_BUSY_ERROR_TEXT = "Этот логин занят"

        private const val TAG = "SignupActivity"
    }
}
