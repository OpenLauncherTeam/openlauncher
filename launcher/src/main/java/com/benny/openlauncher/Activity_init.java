package com.benny.openlauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.benny.openlauncher.activity.Home;


public class Activity_init extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Activity_init.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        finish();
    }
}
