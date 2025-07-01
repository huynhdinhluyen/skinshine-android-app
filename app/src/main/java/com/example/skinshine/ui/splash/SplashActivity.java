package com.example.skinshine.ui.splash;

import android.annotation.SuppressLint;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000;
    private FirebaseAuth mAuth;

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
        mAuth = FirebaseAuth.getInstance();
        initAnimations();
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAndNavigate, SPLASH_DELAY);
    }

    private void checkUserAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToMainActivity("customer");
        } else {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String role = "customer";
                        if (documentSnapshot.exists() && documentSnapshot.getString("role") != null) {
                            role = documentSnapshot.getString("role");
                        }
                        navigateToMainActivity(role);
                    })
                    .addOnFailureListener(e -> {
                        navigateToMainActivity("customer");
                    });
        }
    }

    private void navigateToMainActivity(String userRole) {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra("USER_ROLE", userRole);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initAnimations() {
        ImageView logoImageView = findViewById(R.id.logoImageView);
        TextView appNameTextView = findViewById(R.id.appNameTextView);
        TextView sloganTextView = findViewById(R.id.sloganTextView);

        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_animation);
        logoImageView.startAnimation(logoAnimation);

        Animation appNameAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_text_animation);
        appNameTextView.startAnimation(appNameAnimation);

        Animation sloganAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_slogan_animation);
        sloganTextView.startAnimation(sloganAnimation);
    }
}