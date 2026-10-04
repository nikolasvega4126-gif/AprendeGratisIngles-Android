package com.aprendegratisingles.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private static final int BLUE_DARK = Color.rgb(7, 63, 141);
    private static final int BLUE = Color.rgb(13, 115, 217);
    private static final int GREEN = Color.rgb(24, 184, 75);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean finished = false;

    private final Runnable openApp = this::finishIntro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BLUE_DARK);
        getWindow().setNavigationBarColor(Color.WHITE);
        buildIntro();
        handler.postDelayed(openApp, 5500);
    }

    private void buildIntro() {
        FrameLayout root = new FrameLayout(this);
        root.setBackground(makeGradient());

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER);
        center.setPadding(dp(28), dp(28), dp(28), dp(28));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setAlpha(0f);
        logo.setScaleX(.72f);
        logo.setScaleY(.72f);
        center.addView(logo, new LinearLayout.LayoutParams(dp(170), dp(170)));

        TextView title = new TextView(this);
        title.setText("Aprende gratis inglés");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setAlpha(0f);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.setMargins(0, dp(22), 0, 0);
        center.addView(title, titleLp);

        TextView subtitle = new TextView(this);
        subtitle.setText("Aprende inglés paso a paso");
        subtitle.setTextColor(Color.rgb(224, 241, 255));
        subtitle.setTextSize(17);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setAlpha(0f);
        center.addView(subtitle);

        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dotsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dotsLp.setMargins(0, dp(28), 0, 0);
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(i == 1 ? GREEN : Color.WHITE);
            dot.setBackground(d);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(10), dp(10));
            lp.setMargins(dp(5), 0, dp(5), 0);
            dots.addView(dot, lp);
        }
        dots.setAlpha(0f);
        center.addView(dots, dotsLp);

        TextView skip = new TextView(this);
        skip.setText("Toca para continuar");
        skip.setTextColor(Color.rgb(218, 236, 255));
        skip.setTextSize(13);
        skip.setGravity(Gravity.CENTER);
        skip.setAlpha(0f);
        LinearLayout.LayoutParams skipLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        skipLp.setMargins(0, dp(32), 0, 0);
        center.addView(skip, skipLp);

        FrameLayout.LayoutParams centerLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(center, centerLp);
        setContentView(root);

        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(900).start();
        title.animate().alpha(1f).setStartDelay(450).setDuration(800).start();
        subtitle.animate().alpha(1f).setStartDelay(850).setDuration(800).start();
        dots.animate().alpha(1f).setStartDelay(1300).setDuration(700).start();
        skip.animate().alpha(.9f).setStartDelay(1800).setDuration(700).start();

        final long startedAt = System.currentTimeMillis();
        root.setOnClickListener(v -> {
            if (System.currentTimeMillis() - startedAt > 900) {
                finishIntro();
            }
        });
    }

    private GradientDrawable makeGradient() {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{BLUE_DARK, BLUE, Color.rgb(18, 151, 183)}
        );
        return g;
    }

    private void finishIntro() {
        if (finished) return;
        finished = true;
        handler.removeCallbacks(openApp);
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
