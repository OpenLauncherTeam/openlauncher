package com.benny.openlauncher.notifications;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class NotificationListener extends NotificationListenerService {
    private static Logger LOG = LoggerFactory.getLogger("NotificationListener");

    private boolean _isConnected = false;

    private static final int EVENT_UPDATE_CURRENT_NOS = 0;

    private static HashMap<String, NotificationCallback> _currentNotifications = new HashMap<>();

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

        mMonitorHandler.sendMessage(mMonitorHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS));
    }

    @Override
    public void onListenerConnected() {
        LOG.debug("Listener connected");
        _isConnected = true;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        processCallback(sbn.getPackageName(), sbn.getNotification().number);
     }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        processCallback(sbn.getPackageName(), 0);
    }

    private void processCallback(String packageName, int count) {
        LOG.debug("processCallback({}) -> {}", packageName, count);
        NotificationCallback callback = _currentNotifications.get(packageName);

        if (callback != null) {
            callback.notificationCallback(count);
        }
    }
    public static void setNotificationCallback(String packageName, NotificationCallback callback) {
        _currentNotifications.put(packageName, callback);
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
        } else {
            mMonitorHandler.sendMessage(mMonitorHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS));
        }
    }
}
