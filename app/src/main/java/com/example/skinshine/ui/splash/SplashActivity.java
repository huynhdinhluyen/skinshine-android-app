package com.example.skinshine.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skinshine.MainActivity;
import com.example.skinshine.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ẩn status bar và navigation bar cho full screen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        initAnimations();
        startMainActivityAfterDelay();
    }

    private void initAnimations() {
        ImageView logoImageView = findViewById(R.id.logoImageView);
        TextView appNameTextView = findViewById(R.id.appNameTextView);
        TextView sloganTextView = findViewById(R.id.sloganTextView);

        // Animation cho logo - scale và fade in
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_animation);
        logoImageView.startAnimation(logoAnimation);

        // Animation cho tên app - slide từ dưới lên
        Animation appNameAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_text_animation);
        appNameTextView.startAnimation(appNameAnimation);

        // Animation cho slogan - fade in với delay
        Animation sloganAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_slogan_animation);
        sloganTextView.startAnimation(sloganAnimation);
    }

    private void startMainActivityAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DELAY);
    }
}
