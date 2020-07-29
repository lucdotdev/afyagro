package com.example.afyagro.ui.home_;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.example.afyagro.R;
import com.example.afyagro.ui.first.LoginScreen;

import java.util.Objects;

public class MainPharmerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pharmer);
    }

    public void goToMyProduct(View view) {
        Intent intent = new Intent(this, MyProduct.class);
        startActivity(intent);

    }

    public void addAProduct(View view) {
        Intent intent = new Intent(this, AddAProduct.class);
        startActivity(intent);
    }

    public void logout(View view) {
        SharedPreferences.Editor editor = getSharedPreferences("AUTH", MODE_PRIVATE).edit();
        editor.remove("auth_id");
        editor.remove("account_type");
        editor.putBoolean("is_login", false);
        editor.apply();
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
        finish();
    }
}