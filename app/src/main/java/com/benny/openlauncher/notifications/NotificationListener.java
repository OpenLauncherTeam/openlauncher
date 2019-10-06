package com.benny.openlauncher.notifications;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationListener extends NotificationListenerService {
    private static Logger LOG = LoggerFactory.getLogger("NotificationListener");

    public static String UPDATE_NOTIFICATIONS_ACTION  = "update-notifications";
    public static String UPDATE_NOTIFICATIONS_COMMAND = "command";
    public static String UPDATE_NOTIFICATIONS_UPDATE  = "update";

    private boolean _isConnected = false;
    private NotificationListenerReceiver _notificationReceiver;

    private static final int EVENT_UPDATE_CURRENT_NOS = 0;

    private static HashMap<String, ArrayList<NotificationCallback>> _currentNotifications = new HashMap<>();

    public interface NotificationCallback {
        public void notificationCallback(Integer count);
    }

    @SuppressLint("HandlerLeak")
    private Handler mMonitorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_CURRENT_NOS:
                    updateCurrentNotifications();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (_notificationReceiver == null) {
            _notificationReceiver = new NotificationListenerReceiver();
            IntentFilter filter = new IntentFilter(UPDATE_NOTIFICATIONS_ACTION);
            registerReceiver(_notificationReceiver, filter);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(_notificationReceiver);
        _notificationReceiver = null;
    }

    @Override
    public void onListenerConnected() {
        LOG.debug("Listener connected");
        _isConnected = true;

        mMonitorHandler.sendMessage(mMonitorHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS));
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Some apps do not track total notifications in their StatusBarNotification; given this
        // is an onNotificationPosted, ensure we have a minimum count to display the badge, otherwise
        // it will be removed which is counter-intuitive.
        int notificationCount = sbn.getNotification().number;
        if (notificationCount == 0) {
            notificationCount = 1;
        }
        processCallback(sbn.getPackageName(), notificationCount);
     }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        processCallback(sbn.getPackageName(), 0);
    }

    private void processCallback(String packageName, int count) {
        LOG.debug("processCallback({}) -> {}", packageName, count);
        ArrayList<NotificationCallback> callbacks = _currentNotifications.get(packageName);

        if (callbacks != null) {
            for (NotificationCallback callback : callbacks) {
                callback.notificationCallback(count);
            }
        }
    }

    public static void setNotificationCallback(String packageName, NotificationCallback callback) {
        ArrayList<NotificationCallback> callbacks =_currentNotifications.get(packageName);

        if (callbacks != null) {
            callbacks.add(callback);
        } else {
            callbacks = new ArrayList<NotificationCallback>(1);
            callbacks.add(callback);
            _currentNotifications.put(packageName, callbacks);

        }
    }

    private void updateCurrentNotifications() {
        if (_isConnected) {
            try {
                StatusBarNotification[] activeNos = getActiveNotifications();

                String packageName = "";
                int notificationCount = 0;
                for (int i = 0; i < activeNos.length; i++) {
                    String pkg = activeNos[i].getPackageName();
                    if (!packageName.equals(pkg)) {
                        packageName = pkg;
                        notificationCount = 0;
                    }
                    int count = activeNos[i].getNotification().number;
                    if (count == 0) {
                        notificationCount++;
                    } else {
                        notificationCount = Math.max(notificationCount, count);
                    }

                    processCallback(packageName, notificationCount);

                }
            } catch (Exception e) {
                LOG.error("Unexpected exception when updating notifications: {}", e);
            }
        }
    }

    class NotificationListenerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(UPDATE_NOTIFICATIONS_COMMAND).equals(UPDATE_NOTIFICATIONS_UPDATE)) {
                NotificationListener.this.updateCurrentNotifications();
            }
        }
    }
}
