package com.iomt.android;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private int mYear, mMonth, mDay;
    ProgressDialog progressDialog;

    @BindView(R.id.input_name) EditText nameText;
    @BindView(R.id.input_surname) EditText surnameText;
    @BindView(R.id.input_patronymic) EditText patronymicText;
    @BindView(R.id.input_birthdate) EditText birthdateText;
    @BindView(R.id.input_email) EditText emailText;
    @BindView(R.id.input_mobile) EditText mobileText;
    @BindView(R.id.input_login) EditText loginText;
    @BindView(R.id.input_password) EditText passwordText;
    @BindView(R.id.input_reEnterPassword) EditText reEnterPasswordText;
    @BindView(R.id.btn_signup) LinearLayout signupButton;
    @BindView(R.id.link_login) TextView loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        signupButton.setOnClickListener(v -> signup());

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });

        birthdateText.setOnClickListener(v -> callDatePicker());
    }

    private void callDatePicker() {
        final Calendar cal = Calendar.getInstance();
        if (mYear == 0) {
            mYear = cal.get(Calendar.YEAR);
            mMonth = cal.get(Calendar.MONTH);
            mDay = cal.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                R.style.MySpinnerDatePickerStyle,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String zm = monthOfYear + 1 >= 10 ? "":"0";
                    String zd = dayOfMonth >= 10 ? "":"0";
                    String editTextDateParam = zd + dayOfMonth + "." + zm + (monthOfYear + 1) + "." + year;
                    birthdateText.setText(editTextDateParam);
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed("");
            return;
        }

        signupButton.setEnabled(false);

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Создание аккаунта...");
        progressDialog.show();

        String name = nameText.getText().toString();
        String surname = surnameText.getText().toString();
        String patronymic = patronymicText.getText().toString();
        String birthdate = birthdateText.getText().toString();
        String email = emailText.getText().toString();
        String mobile = mobileText.getText().toString();
        String login = loginText.getText().toString();
        String password = passwordText.getText().toString();

        Action successAction = (String[] args) -> new android.os.Handler(Looper.getMainLooper()).postDelayed(
            () -> {
                assert args[0] != null;
                if (args[0].length() != 0) {
                    onSignupFailed(args[0] );
                } else {
                    onSignupSuccess();
                }
                progressDialog.dismiss();
            }, 1000);
        ErrorAction errorAction = () -> onSignupFailed("");
        HTTPRequests httpRequests = new HTTPRequests(this);
        httpRequests.send_reg(name,
                surname,
                patronymic,
                birthdate,
                email,
                mobile,
                login,
                password,
                successAction,
                errorAction);
    }

    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        Intent intent = new Intent(this, EmailConf.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed(String err) {
        Toast.makeText(getBaseContext(), "Не удалось зарегистрировать", Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
        if (err.equals("email")) {
            emailText.setError("Этот e-mail занят");
        } else if (err.equals("login")) {
            loginText.setError("Этот логин занят");
        }
    }

    public boolean validate() {
        boolean valid = true;

        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String name = nameText.getText().toString();
        String surname = surnameText.getText().toString();
        String patronymic = patronymicText.getText().toString();
        String birthdate = birthdateText.getText().toString();
        String email = emailText.getText().toString();
        String mobile = mobileText.getText().toString();
        String login = loginText.getText().toString();
        String password = passwordText.getText().toString();
        String reEnterPassword = reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 2) {
            nameText.setError("Не менее 2 символов");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (surname.isEmpty() || surname.length() < 2) {
            surnameText.setError("Не менее 2 символов");
            valid = false;
        } else {
            surnameText.setError(null);
        }

        if (patronymic.isEmpty() || patronymic.length() < 2) {
            patronymicText.setError("Не менее 2 символов");
            valid = false;
        } else {
            patronymicText.setError(null);
        }

        if (birthdate.isEmpty()) {
            birthdateText.setError("Введите дату рождения");
            valid = false;
        } else {
            birthdateText.setError(null);
        }

        if (mYear == 0 || mYear > year || mYear == year && mMonth > month || mYear == year && mMonth == month && mDay > day) {
            birthdateText.setError("Введите корректно дату рождения");
            valid = false;
        } else {
            birthdateText.setError(null);
        }

        if (login.isEmpty() || login.length() < 2) {
            loginText.setError("Не менее 2 символов");
            valid = false;
        } else {
            loginText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Введите корректно e-mail");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (mobile.length() != 11) {
            mobileText.setError("Введите корректно номер телефона");
            valid = false;
        } else {
            mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 14) {
            passwordText.setError("Не менее 4 и не более 14 символов");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 14 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError("Пароли не совпадают");
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        return valid;
    }
}