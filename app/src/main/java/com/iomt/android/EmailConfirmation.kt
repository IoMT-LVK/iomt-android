package com.iomt.android

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * [AppCompatActivity] responsible for email confirmation
 */
class EmailConfirmation : AppCompatActivity() {
    private lateinit var backButton: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_conf)
        backButton = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }
}
