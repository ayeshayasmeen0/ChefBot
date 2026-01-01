package com.example.chefbot;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private TextView tvAppName, tvLoading, dot1, dot2, dot3;
    private LinearLayout chefIcon, loadingDots;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        tvAppName = findViewById(R.id.tvAppName);
        tvLoading = findViewById(R.id.tvLoading);
        chefIcon = findViewById(R.id.chefIcon);
        loadingDots = findViewById(R.id.loadingDots);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        // Start animations
        startChefIconAnimation();
        startDotsAnimation();
        startTextAnimation();

        // Navigate to main activity after delay
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 3000); // 3 seconds
    }

    private void startChefIconAnimation() {
        if (chefIcon != null) {
            // Scale animation
            chefIcon.setScaleX(0f);
            chefIcon.setScaleY(0f);
            chefIcon.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(800)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            // Rotation animation
            ObjectAnimator rotation = ObjectAnimator.ofFloat(chefIcon, "rotation", 0f, 360f);
            rotation.setDuration(1500);
            rotation.start();
        }
    }

    private void startDotsAnimation() {
        TextView[] dots = {dot1, dot2, dot3};

        for (int i = 0; i < dots.length; i++) {
            final TextView dot = dots[i];
            if (dot != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateDot(dot);
                    }
                }, i * 300);
            }
        }

        // Continue animation loop
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startDotsAnimation();
            }
        }, 1500);
    }

    private void animateDot(TextView dot) {
        if (dot != null) {
            // Scale animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 0.5f, 1.5f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 0.5f, 1.5f, 1.0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(dot, "alpha", 0.3f, 1.0f, 0.3f);

            scaleX.setDuration(600);
            scaleY.setDuration(600);
            alpha.setDuration(600);

            scaleX.start();
            scaleY.start();
            alpha.start();
        }
    }

    private void startTextAnimation() {
        if (tvAppName != null) {
            // Fade in animation
            tvAppName.setAlpha(0f);
            tvAppName.animate()
                    .alpha(1f)
                    .setDuration(1200)
                    .start();

            // Text animation
            final String[] loadingTexts = {
                    "🍳 Preparing your kitchen...",
                    "📚 Loading recipes...",
                    "🔥 Getting things ready...",
                    "✅ Almost there!"
            };

            if (tvLoading != null) {
                handler.postDelayed(new Runnable() {
                    int index = 0;
                    @Override
                    public void run() {
                        if (index < loadingTexts.length) {
                            tvLoading.setText(loadingTexts[index]);
                            index++;
                            handler.postDelayed(this, 800);
                        }
                    }
                }, 1000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}