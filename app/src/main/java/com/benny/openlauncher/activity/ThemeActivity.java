package com.benny.openlauncher.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;

public abstract class ThemeActivity extends AppCompatActivity {

    private AppSettings appSettings;
    private String currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appSettings = AppSettings.get();
        currentTheme = appSettings.getTheme();
        if (appSettings.getTheme().equals("0")) {
            setTheme(R.style.NormalActivity_Light);
        } else {
            setTheme(R.style.NormalActivity_Dark);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(dark(AppSettings.get().getPrimaryColor(), 0.8));
            getWindow().setNavigationBarColor(AppSettings.get().getPrimaryColor());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!appSettings.getTheme().equals(currentTheme)) {
            restart();
        }
    }

    protected void restart() {
        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public int dark(int color, double factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, Math.max((int) (r * factor), 0), Math.max((int) (g * factor), 0), Math.max((int) (b * factor), 0));
    }
}
