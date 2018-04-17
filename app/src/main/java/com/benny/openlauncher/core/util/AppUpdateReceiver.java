package com.benny.openlauncher.core.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.benny.openlauncher.core.manager.Setup;

public class AppUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context p1, Intent p2) {
        Setup.Companion.appLoader().onAppUpdated(p1, p2);
    }
}

