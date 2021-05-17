package com.iomt.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private String JWT;
    private String UserId;
    ProgressDialog progressDialog;


    @BindView(R.id.input_login) EditText loginText;
    @BindView(R.id.input_password) EditText passwordText;
    @BindView(R.id.btn_login) LinearLayout loginButton;
    @BindView(R.id.link_signup) TextView signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginButton.setOnClickListener(v -> login());

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }


        progressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Аутентификация...");
        progressDialog.show();

        String login = loginText.getText().toString();
        String password = passwordText.getText().toString();
        HTTPRequests httpRequests = new HTTPRequests(this);
        SuccessAction success = (String[] args, boolean confirmed) -> {
            if (!confirmed) {
                Intent intent = new Intent(getApplicationContext(), EmailConf.class);
                startActivity(intent);
            } else {
                JWT = args[0];
                UserId = args[1];
                Log.d(TAG,  JWT + " " + UserId);
                new android.os.Handler(Looper.getMainLooper()).postDelayed(
                        () -> {
                            onLoginSuccess();
                            progressDialog.dismiss();
                        }, 1000);
            }
        };
        httpRequests.send_login(login, password, success, this::onLoginFailed);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        Intent intent = new Intent(this, DeviceListActivity.class);
        intent.putExtra("JWT", JWT);
        intent.putExtra("UserId", UserId);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Не удалось войти", Toast.LENGTH_LONG).show();

        loginButton.setEnabled(true);
        if (progressDialog != null) {
            progressDialog.cancel();
        }
    }

    public boolean validate() {
        boolean valid = true;

        String login = loginText.getText().toString();
        String password = passwordText.getText().toString();

        if (login.isEmpty()) {
            loginText.setError("Введите корректный логин");
            valid = false;
        } else {
            loginText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 14) { //// 4
            passwordText.setError("Не менее 4 и не более 14 символов");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

/**
    public boolean check_jwt() {
        try {
            SharedPreferences prefs = getSharedPreferences(this.getString(R.string.jwt), MODE_PRIVATE);
            String jwt = prefs.getString("jwt", "");
        }
    }**/
}