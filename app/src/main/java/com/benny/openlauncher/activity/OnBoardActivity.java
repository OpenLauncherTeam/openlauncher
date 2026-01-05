package com.benny.openlauncher.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.benny.openlauncher.R;

public class OnBoardActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if intro was already shown
        if (!getSharedPreferences("quickSettings", Context.MODE_PRIVATE).getBoolean("firstStart", true)) {
            getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean(getResources().getString(R.string.pref_key__show_intro), false).commit();
        }

        // Mark intro as shown and go to HomeActivity
        getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean(getResources().getString(R.string.pref_key__show_intro), false).apply();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}
