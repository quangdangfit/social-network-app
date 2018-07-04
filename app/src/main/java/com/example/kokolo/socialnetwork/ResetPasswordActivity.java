package com.example.kokolo.socialnetwork;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

public class ResetPasswordActivity extends AppCompatActivity {

    Toolbar mToolbar;

    TextView resetPasswordEmail;
    Button resetPasswordSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mToolbar = findViewById(R.id.forget_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        resetPasswordEmail = findViewById(R.id.reset_password_email);
        resetPasswordSendButton = findViewById(R.id.reset_password_send_button);
    }
}
