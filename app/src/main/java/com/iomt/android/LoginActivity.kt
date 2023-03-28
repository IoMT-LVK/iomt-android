package com.iomt.android

import android.os.Bundle
import android.widget.*
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.iomt.android.jetpack.LoginView

/**
 * [AppCompatActivity] that is responsible for logging in
 */
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LoginView() }
    }
}
