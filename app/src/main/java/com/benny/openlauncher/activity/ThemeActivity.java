package com.benny.openlauncher.activity;

import android.content.Intent;
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
}
