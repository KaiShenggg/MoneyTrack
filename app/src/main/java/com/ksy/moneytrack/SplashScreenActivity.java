package com.ksy.moneytrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Hide the action bar
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();


        // Use a Handler to post a delayed Runnable to transition to the main activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                intent = new Intent(SplashScreenActivity.this, MainActivity.class);

                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }
}