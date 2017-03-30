package com.benny.openlauncher.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AutoFinishActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        finish();
        super.onCreate(savedInstanceState);
    }

    public static void start(Context c) {
        c.startActivity(new Intent(c, AutoFinishActivity.class));
    }
}
