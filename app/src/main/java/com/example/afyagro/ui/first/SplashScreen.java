package com.example.afyagro.ui.first;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.example.afyagro.R;
import com.example.afyagro.ui.home.MainActivity;
import com.example.afyagro.ui.home_.MainPharmerActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        int type = prefs.getInt("account_type", 0);
        boolean isLogin = prefs.getBoolean("is_login", false);


        if(isLogin) {
            Handler handler = new Handler();
            if(type == 1){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent=new Intent(SplashScreen.this, MainPharmerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },2000);
            } else if(type== 2){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent=new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },2000);
            }
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent=new Intent(SplashScreen.this, LoginScreen.class);
                    startActivity(intent);
                    finish();
                }
            },2000);
        }
    }


}
