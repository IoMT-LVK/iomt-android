package com.iomt.android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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
        if (!validate()) {
            onLoginFailed()
            return
        }
        progressBar.isIndeterminate = true
        progressBar.visibility = ProgressBar.VISIBLE
        val login = loginText.text.toString()
        val password = passwordText.text.toString()
        val httpRequests = HTTPRequests(this)
        val success = SuccessAction { args: Array<String?>?, confirmed: Boolean ->
            if (!confirmed) {
                val intent = Intent(applicationContext, EmailConf::class.java)
                startActivity(intent)
            } else {
                jwt = args!![0]
                userId = args[1]
                Log.d(TAG, "$jwt $userId")
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        onLoginSuccess()
                        progressBar.visibility = ProgressBar.INVISIBLE
                    }, 1000
                )
            }
        }
        httpRequests.sendLogin(login, password, success, { onLoginFailed() })
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

    private fun onLoginSuccess() {
        loginButton.isEnabled = false
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        val intent = Intent(this, DeviceListActivity::class.java)
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

    private fun validate(): Boolean {
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

    /**
     * public boolean check_jwt() {
     * try {
     * SharedPreferences prefs = getSharedPreferences(this.getString(R.string.jwt), MODE_PRIVATE);
     * String jwt = prefs.getString("jwt", "");
     * }
     * } */
    companion object {
        private const val TAG = "LoginActivity"
        private const val REQUEST_SIGNUP = 0
    }
}