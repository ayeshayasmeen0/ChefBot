package com.example.chefbot;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private TextView tvAppName, tvLoading;
    private LinearLayout chefIcon;
    private View dot1, dot2, dot3;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views - CORRECT IDs
        tvAppName = findViewById(R.id.tvAppName);
        tvLoading = findViewById(R.id.tvLoading);
        chefIcon = findViewById(R.id.chefIcon);
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
            // Fade in animation
            chefIcon.setAlpha(0f);
            chefIcon.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .start();

            // Bounce animation
            ObjectAnimator bounceY = ObjectAnimator.ofFloat(chefIcon, "translationY", -30f, 0f);
            bounceY.setDuration(1500);
            bounceY.setInterpolator(new AccelerateDecelerateInterpolator());
            bounceY.start();

            // Scale animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(chefIcon, "scaleX", 0.5f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(chefIcon, "scaleY", 0.5f, 1.0f);
            scaleX.setDuration(1200);
            scaleY.setDuration(1200);
            scaleX.start();
            scaleY.start();
        }
    }

    private void startDotsAnimation() {
        View[] dots = {dot1, dot2, dot3};

        for (int i = 0; i < dots.length; i++) {
            final View dot = dots[i];
            if (dot != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateDot(dot);
                    }
                }, i * 200);
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

    private void animateDot(View dot) {
        if (dot != null) {
            // Reset to ensure clean animation
            dot.setScaleX(1f);
            dot.setScaleY(1f);
            dot.setAlpha(1f);

            // Create scale animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 0.5f, 1.5f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 0.5f, 1.5f, 1.0f);

            // Create alpha animation
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
                    .setDuration(1500)
                    .start();

            // Pulse animation after delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvAppName.animate()
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .setDuration(500)
                            .withEndAction(() -> tvAppName.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(500)
                                    .start())
                            .start();
                }
            }, 1000);
        }

        // Animate loading text
        if (tvLoading != null) {
            final String[] loadingTexts = {
                    "Preparing your recipes...",
                    "Scanning ingredients...",
                    "Finding perfect matches...",
                    "Ready to cook! 🍳"
            };

            tvLoading.setAlpha(0f);
            tvLoading.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .start();

            // Change loading text every second
            handler.postDelayed(new Runnable() {
                int index = 0;
                @Override
                public void run() {
                    if (index < loadingTexts.length) {
                        tvLoading.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction(() -> {
                                    tvLoading.setText(loadingTexts[index]);
                                    tvLoading.animate()
                                            .alpha(1f)
                                            .setDuration(300)
                                            .start();
                                })
                                .start();
                        index++;
                        if (index < loadingTexts.length) {
                            handler.postDelayed(this, 1000);
                        }
                    }
                }
            }, 1500);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}