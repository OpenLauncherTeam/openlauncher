package com.bennyv4.project2.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context p1, Intent p2) {
        AppManager.getInstance(p1).onReceive(p1, p2);
    }
}

