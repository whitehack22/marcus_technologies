package com.example.marcustech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class LoginOption extends AppCompatActivity {

    Button userBtn, adminBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_option);

        userBtn  = findViewById(R.id.user_login_btn);
        adminBtn = findViewById(R.id.admin_login_btn);




        userBtn.setOnClickListener(v -> {
            Intent intent=new Intent(LoginOption.this, UserLogin.class);
            startActivity(intent);
            finish();
        });

        adminBtn.setOnClickListener(v -> {
            Intent intent=new Intent(LoginOption.this, AdminLogin.class);
            startActivity(intent);
            finish();
        });
    }
}