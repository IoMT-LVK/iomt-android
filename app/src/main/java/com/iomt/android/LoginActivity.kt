package com.iomt.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * [AppCompatActivity] that is responsible for logging in
 */
class LoginActivity : AppCompatActivity() {
    private var jwt: String? = null
    private var userId: String? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var loginText: EditText
    private lateinit var passwordText: EditText
    private lateinit var loginButton: LinearLayout
    private lateinit var signupLink: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        progressBar = ProgressBar(this)
        loginText = findViewById(R.id.input_login)
        passwordText = findViewById(R.id.input_password)

        loginButton = findViewById(R.id.btn_login)
        loginButton.setOnClickListener { login() }

        signupLink = findViewById(R.id.link_signup)
        signupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    private fun login() {
        Log.d(TAG, "Login")
        if (!isValid()) {
            onLoginFailed()
            return
        }
        progressBar.isIndeterminate = true
        progressBar.visibility = ProgressBar.VISIBLE
        val login = loginText.text.toString()
        val password = passwordText.text.toString()
        val httpRequests = Requests()
        httpRequests.sendLogin(login, password) { authInfo ->
            if (authInfo.wasFailed) {
                runOnUiThread { onLoginFailed() }
            } else if (!authInfo.confirmed) {
                val intent = Intent(applicationContext, EmailConfirmation::class.java)
                runOnUiThread {
                    startActivity(intent)
                }
            } else {
                jwt = authInfo.jwt
                userId = authInfo.userId
                onLoginSuccess()
                progressBar.visibility = ProgressBar.INVISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SIGNUP && resultCode == RESULT_OK) {
            finish()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun onLoginSuccess() = runOnUiThread {
        loginButton.isEnabled = false
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        val intent = Intent(this, DeviceListActivity::class.java)
        getSharedPreferences(baseContext.getString(R.string.ACC_DATA), Context.MODE_PRIVATE).edit().apply {
            putString("JWT", jwt)
            putString("UserId", userId)
        }.apply()
        intent.putExtra("JWT", jwt)
        intent.putExtra("UserId", userId)
        startActivity(intent)
        finish()
    }

    private fun onLoginFailed() {
        Toast.makeText(baseContext, "Не удалось войти", Toast.LENGTH_LONG).show()
        loginButton.isEnabled = true
        progressBar.visibility = ProgressBar.INVISIBLE
    }

    private fun isValid(): Boolean {
        @Suppress("SAY_NO_TO_VAR")
        var valid = true
        val login = loginText.text.toString()
        val password = passwordText.text.toString()
        if (login.isEmpty()) {
            loginText.error = "Введите корректный логин"
            valid = false
        } else {
            loginText.error = null
        }
        if (password.isEmpty() || password.length < 4 || password.length > 14) {
            passwordText.error = "Не менее 4 и не более 14 символов"
            valid = false
        } else {
            passwordText.error = null
        }
        return valid
    }

    companion object {
        private const val REQUEST_SIGNUP = 0
        private const val TAG = "LoginActivity"
    }
}
