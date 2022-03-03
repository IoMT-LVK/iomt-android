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
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0

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
        if (mYear == 0) {
            mYear = cal[Calendar.YEAR]
            mMonth = cal[Calendar.MONTH]
            mDay = cal[Calendar.DAY_OF_MONTH]
        }
        val datePickerDialog = DatePickerDialog(this,
            R.style.MySpinnerDatePickerStyle,
            { _, year, monthOfYear, dayOfMonth ->
                val zm = if (monthOfYear + 1 >= 10) "" else "0"
                val zd = if (dayOfMonth >= 10) "" else "0"
                val editTextDateParam = zd + dayOfMonth + "." + zm + (monthOfYear + 1) + "." + year
                birthdateText.setText(editTextDateParam)
                mYear = year
                mMonth = monthOfYear
                mDay = dayOfMonth
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }

    private fun signup() {
        Log.d(TAG, "Signup")
        if (!validate()) {
            onSignupFailed("")
            return
        }
        signupButton.isEnabled = false
        progressBar.isIndeterminate = true
        progressBar.visibility = ProgressBar.VISIBLE
        val name = nameText.text.toString()
        val surname = surnameText.text.toString()
        val patronymic = patronymicText.text.toString()
        val birthdate = birthdateText.text.toString()
        val email = emailText.text.toString()
        val mobile = mobileText.text.toString()
        val login = loginText.text.toString()
        val password = passwordText.text.toString()
        val successAction = Action { args: Array<String?>? ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    assert(args!![0] != null)
                    if (args[0]!!.isNotEmpty()) {
                        onSignupFailed(args[0])
                    } else {
                        onSignupSuccess()
                    }
                    progressBar.visibility = ProgressBar.INVISIBLE
                }, 1000
            )
        }
        val errorAction = ErrorAction { onSignupFailed("") }
        val httpRequests = HTTPRequests(this)
        httpRequests.sendReg(
            name,
            surname,
            patronymic,
            birthdate,
            email,
            mobile,
            login,
            password,
            successAction,
            errorAction
        )
    }

    private fun onSignupSuccess() {
        signupButton.isEnabled = true
        setResult(RESULT_OK, null)
        val intent = Intent(this, EmailConf::class.java)
        startActivity(intent)
        finish()
    }

    private fun onSignupFailed(err: String?) {
        Toast.makeText(baseContext, "Не удалось зарегистрировать", Toast.LENGTH_LONG).show()
        signupButton.isEnabled = true
        if (err == "email") {
            emailText.error = "Этот e-mail занят"
        } else if (err == "login") {
            loginText.error = "Этот логин занят"
        }
    }

    private fun validate(): Boolean {
        var valid = true
        val cal = Calendar.getInstance()
        val year = cal[Calendar.YEAR]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]
        val name = nameText.text.toString()
        val surname = surnameText.text.toString()
        val patronymic = patronymicText.text.toString()
        val birthdate = birthdateText.text.toString()
        val email = emailText.text.toString()
        val mobile = mobileText.text.toString()
        val login = loginText.text.toString()
        val password = passwordText.text.toString()
        val reEnterPassword = reEnterPasswordText.text.toString()
        if (name.isEmpty() || name.length < 2) {
            nameText.error = "Не менее 2 символов"
            valid = false
        } else {
            nameText.error = null
        }
        if (surname.isEmpty() || surname.length < 2) {
            surnameText.error = "Не менее 2 символов"
            valid = false
        } else {
            surnameText.error = null
        }
        if (patronymic.isEmpty() || patronymic.length < 2) {
            patronymicText.error = "Не менее 2 символов"
            valid = false
        } else {
            patronymicText.error = null
        }
        if (birthdate.isEmpty()) {
            birthdateText.error = "Введите дату рождения"
            valid = false
        } else {
            birthdateText.error = null
        }
        if (mYear == 0 || mYear > year || mYear == year && mMonth > month || mYear == year && mMonth == month && mDay > day) {
            birthdateText.error = "Введите корректно дату рождения"
            valid = false
        } else {
            birthdateText.error = null
        }
        if (login.isEmpty() || login.length < 2) {
            loginText.error = "Не менее 2 символов"
            valid = false
        } else {
            loginText.error = null
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.error = "Введите корректно e-mail"
            valid = false
        } else {
            emailText.error = null
        }
        if (mobile.length != 11) {
            mobileText.error = "Введите корректно номер телефона"
            valid = false
        } else {
            mobileText.error = null
        }
        if (password.isEmpty() || password.length < 4 || password.length > 14) {
            passwordText.error = "Не менее 4 и не более 14 символов"
            valid = false
        } else {
            passwordText.error = null
        }
        if (reEnterPassword.isEmpty() || reEnterPassword.length < 4 || reEnterPassword.length > 14 || reEnterPassword != password) {
            reEnterPasswordText.error = "Пароли не совпадают"
            valid = false
        } else {
            reEnterPasswordText.error = null
        }
        return valid
    }

    companion object {
        private const val TAG = "SignupActivity"
    }
}