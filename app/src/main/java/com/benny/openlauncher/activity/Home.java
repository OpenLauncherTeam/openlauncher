package com.benny.openlauncher.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.App;
import com.benny.openlauncher.R;
import com.benny.openlauncher.core.interfaces.DialogListener;
import com.benny.openlauncher.core.interfaces.SettingsManager;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.DatabaseHelper;
import com.benny.openlauncher.core.util.BaseIconProvider;
import com.benny.openlauncher.core.util.SimpleIconProvider;
import com.benny.openlauncher.core.viewutil.DesktopGestureListener;
import com.benny.openlauncher.core.viewutil.ItemGestureListener;
import com.benny.openlauncher.core.widget.Desktop;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.benny.openlauncher.viewutil.IconListAdapter;
import com.benny.openlauncher.viewutil.QuickCenterItem;
import com.benny.openlauncher.widget.LauncherLoadingIcon;
import com.benny.openlauncher.widget.MiniPopupView;
import com.benny.openlauncher.widget.SwipeListView;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

import net.gsantner.opoc.util.ContextUtils;

public class Home extends com.benny.openlauncher.core.activity.Home implements DrawerLayout.DrawerListener {
    @BindView(R.id.minibar)
    public SwipeListView minibar;
    @BindView(R.id.minibar_background)
    public FrameLayout minibarBackground;
    @BindView(R.id.drawer_layout)
    public DrawerLayout drawerLayout;
    @BindView(R.id.miniPopup)
    public MiniPopupView miniPopup;
    @BindView(R.id.shortcutLayout)
    public RelativeLayout shortcutLayout;
    @BindView(R.id.loadingIcon)
    public LauncherLoadingIcon loadingIcon;
    @BindView(R.id.loadingSplash)
    public FrameLayout loadingSplash;
    private Unbinder unbinder;
    private FastItemAdapter<QuickCenterItem.ContactItem> quickContactFA;
    private CallLogObserver callLogObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ContextUtils(getApplicationContext()).setAppLanguage(AppSettings.get().getLanguage()); // before setContentView
        super.onCreate(savedInstanceState);

        CustomActivityOnCrash.setShowErrorDetails(true);
        CustomActivityOnCrash.setEnableAppRestart(false);
        CustomActivityOnCrash.setDefaultErrorActivityDrawable(R.drawable.rip);
        CustomActivityOnCrash.install(this);
    }

    @Override
    public void onStartApp(Context context, Intent intent) {
        if (intent.getComponent().getPackageName().equals("com.benny.openlauncher")) {
            LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
            consumeNextResume = true;
        } else
            super.onStartApp(context, intent);
    }

    @Override
    public void onStartApp(Context context, com.benny.openlauncher.core.interfaces.App app) {
        if (app.getPackageName().equals("com.benny.openlauncher")) {
            LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
            consumeNextResume = true;
        } else
            super.onStartApp(context, app);
    }

    @Override
    protected void bindViews() {
        super.bindViews();
        unbinder = ButterKnife.bind(this);

        loadingSplash.animate().alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                myScreen.removeView(loadingSplash);
            }
        });
    }

    @Override
    protected void unbindViews() {
        super.unbindViews();
        if (unbinder != null)
            unbinder.unbind();
    }

    @Override
    protected void initAppManager() {
        super.initAppManager();
        AppManager.getInstance(this).init();
    }

    @Override
    protected void initViews() {
        super.initViews();

        initMinibar();
        initQuickCenter();
    }

    @Override
    protected void initSettings() {
        super.initSettings();
        drawerLayout.setDrawerLockMode(AppSettings.get().getMinibarEnable() ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onRemovePage() {
        if (!desktop.isCurrentPageEmpty())
            DialogHelper.alertDialog(this, getString(R.string.remove), "This page is not empty. Those item will also be removed.", new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Home.super.onRemovePage();
                }
            });
        else
            Home.super.onRemovePage();
    }

    public void initMinibar() {
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<Integer> icons = new ArrayList<>();

        for (String act : AppSettings.get().getMinibarArrangement()) {
            if (act.length() > 1 && act.charAt(0) == '0') {
                LauncherAction.ActionDisplayItem item = LauncherAction.getActionItemFromString(act.substring(1));
                if (item != null) {
                    labels.add(item.label.toString());
                    icons.add(item.icon);
                }
            }
        }

        minibar.setAdapter(new IconListAdapter(this, labels, icons));
        minibar.setOnSwipeRight(new SwipeListView.OnSwipeRight() {
            @Override
            public void onSwipe(int pos, float x, float y) {
                miniPopup.showActionWindow(LauncherAction.Action.valueOf(labels.get(pos)), x, y + (shortcutLayout.getHeight() - minibar.getHeight()) / 2);
            }
        });
        minibar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LauncherAction.Action action = LauncherAction.Action.valueOf(labels.get(i));
                if (action == LauncherAction.Action.DeviceSettings || action == LauncherAction.Action.LauncherSettings || action == LauncherAction.Action.EditMinBar) {
                    consumeNextResume = true;
                }
                LauncherAction.RunAction(action, Home.this);
                if (action != LauncherAction.Action.DeviceSettings && action != LauncherAction.Action.LauncherSettings && action != LauncherAction.Action.EditMinBar) {
                    drawerLayout.closeDrawers();
                }
            }
        });
        // frame layout spans the entire side while the minibar container has gaps at the top and bottom
        minibarBackground.setBackgroundColor(AppSettings.get().getMinibarBackgroundColor());
    }

    private void initQuickCenter() {
        RecyclerView quickContact = (RecyclerView) findViewById(R.id.quickContactRv);
        quickContact.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        quickContactFA = new FastItemAdapter<>();
        quickContact.setAdapter(quickContactFA);

        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            callLogObserver = new CallLogObserver(new Handler());
            getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);

            // get the call history for the adapter
            callLogObserver.onChange(true);
        } else {
            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_PERMISSION_READ_CALL_LOG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // check if reading the call log is permitted
        if (requestCode == REQUEST_PERMISSION_READ_CALL_LOG && callLogObserver != null) {
            callLogObserver = new CallLogObserver(new Handler());
            getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);

            // get the call history for the adapter
            callLogObserver.onChange(true);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        if (callLogObserver != null)
            getApplicationContext().getContentResolver().unregisterContentObserver(callLogObserver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        drawerLayout.closeDrawers();
        super.onBackPressed();
    }

    // search button in the search bar is clicked
    public void onSearch(View view) {
        Intent i;
        try {
            i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchActivity");
            Home.this.startActivity(i);
        } catch (Exception e) {
            i = new Intent(Intent.ACTION_WEB_SEARCH);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        Home.this.startActivity(i);
    }

    // voice button in the search bar clicked
    public void onVoiceSearch(View view) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
            Home.this.startActivity(i);
        } catch (Exception e) {
            Tool.toast(Home.this, "Can not find google search app");
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        switch (newState) {
            case DrawerLayout.STATE_DRAGGING:
            case DrawerLayout.STATE_SETTLING:
                if (shortcutLayout.getAlpha() == 1)
                    shortcutLayout.animate().setDuration(180L).alpha(0).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            shortcutLayout.setVisibility(View.INVISIBLE);
                        }
                    });
                break;
            case DrawerLayout.STATE_IDLE:
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    shortcutLayout.setVisibility(View.VISIBLE);
                    shortcutLayout.setAlpha(0);
                    shortcutLayout.animate().setDuration(180L).alpha(1).setInterpolator(new AccelerateDecelerateInterpolator());
                }
                break;
        }
    }

    @Override
    protected void onHandleLauncherPause() {
        super.onHandleLauncherPause();
    }

    @Override
    protected void initStaticHelper() {

        final SettingsManager settingsManager = AppSettings.get();
        final Setup.ImageLoader imageLoader = new Setup.ImageLoader() {
            @Override
            public BaseIconProvider createIconProvider(Drawable drawable) {
                return new SimpleIconProvider(drawable);
            }

            @Override
            public BaseIconProvider createIconProvider(int icon) {
                return new SimpleIconProvider(icon);
            }
        };
        final DesktopGestureListener.DesktopGestureCallback desktopGestureCallback = new DesktopGestureListener.DesktopGestureCallback() {
            @Override
            public boolean onDrawerGesture(Desktop desktop, DesktopGestureListener.Type event) {
                switch (event) {
                    case SwipeUp: {
                        if (Integer.parseInt(AppSettings.get().getGestureSwipeUp()) != 0) {
                            LauncherAction.ActionItem gesture = LauncherAction.getActionItem(Integer.parseInt(AppSettings.get().getGestureSwipeUp()) - 1);
                            if (gesture != null && AppSettings.get().isGestureFeedback()) {
                                Tool.vibrate(desktop);
                            }
                            LauncherAction.RunAction(gesture, desktop.getContext());
                        }
                        return true;
                    }
                    case SwipeDown: {
                        if (Integer.parseInt(AppSettings.get().getGestureSwipeDown()) != 0) {
                            LauncherAction.ActionItem gesture = LauncherAction.getActionItem(Integer.parseInt(AppSettings.get().getGestureSwipeDown()) - 1);
                            if (gesture != null && AppSettings.get().isGestureFeedback()) {
                                Tool.vibrate(desktop);
                            }
                            LauncherAction.RunAction(gesture, desktop.getContext());
                        }
                        return true;
                    }
                    case SwipeLeft:
                        return false;
                    case SwipeRight:
                        return false;
                    case Pinch: {
                        if (Integer.parseInt(AppSettings.get().getGesturePinch()) != 0) {
                            LauncherAction.ActionItem gesture = LauncherAction.getActionItem(Integer.parseInt(AppSettings.get().getGesturePinch()) - 1);
                            if (gesture != null && AppSettings.get().isGestureFeedback()) {
                                Tool.vibrate(desktop);
                            }
                            LauncherAction.RunAction(gesture, desktop.getContext());
                        }
                        return true;
                    }
                    case Unpinch: {
                        if (Integer.parseInt(AppSettings.get().getGestureUnpinch()) != 0) {
                            LauncherAction.ActionItem gesture = LauncherAction.getActionItem(Integer.parseInt(AppSettings.get().getGestureUnpinch()) - 1);
                            if (gesture != null && AppSettings.get().isGestureFeedback()) {
                                Tool.vibrate(desktop);
                            }
                            LauncherAction.RunAction(gesture, desktop.getContext());
                        }
                        return true;
                    }
                    case DoubleTap: {
                        if (Integer.parseInt(AppSettings.get().getGestureDoubleTap()) != 0) {
                            LauncherAction.ActionItem gesture = LauncherAction.getActionItem(Integer.parseInt(AppSettings.get().getGestureDoubleTap()) - 1);
                            if (gesture != null && AppSettings.get().isGestureFeedback()) {
                                Tool.vibrate(desktop);
                            }
                            LauncherAction.RunAction(gesture, desktop.getContext());
                        }
                        return true;
                    }
                    default: {
                        throw new RuntimeException("Type not handled!");
                    }
                }
            }
        };
        final ItemGestureListener.ItemGestureCallback itemGestureCallback = null;
        final Setup.DataManager dataManager = new DatabaseHelper(this);
        final AppManager appLoader = AppManager.getInstance(this);
        final Setup.EventHandler eventHandler = new Setup.EventHandler() {
            @Override
            public void showLauncherSettings(Context context) {
                LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
            }

            @Override
            public void showPickAction(Context context, final DialogListener.OnAddAppDrawerItemListener listener) {
                DialogHelper.addActionItemDialog(context, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                listener.onAdd();
                                break;
                        }
                    }
                });
            }

            @Override
            public void showEditDialog(Context context, final Item item, DialogListener.OnEditDialogListener listener) {
                DialogHelper.editItemDialog("Edit Item", item.getLabel(), context, new DialogHelper.onItemEditListener() {
                    @Override
                    public void itemLabel(String label) {
                        item.setLabel(label);
                        Home.db.saveItem(item);

                        Home.launcher.desktop.addItemToCell(item, item.getX(), item.getY());
                        Home.launcher.desktop.removeItem(Home.launcher.desktop.getCurrentPage().coordinateToChildView(new Point(item.getX(), item.getY())));
                    }
                });
            }

            @Override
            public void showDeletePackageDialog(Context context, DragEvent dragEvent) {
                DialogHelper.deletePackageDialog(context, dragEvent);
            }
        };
        final Setup.Logger logger = new Setup.Logger() {
            @Override
            public void log(Object source, int priority, String tag, String msg, Object... args) {
                Log.println(priority, tag, String.format(msg, args));
            }
        };
        Setup.init(new Setup<AppManager.App>() {
            @Override
            public Context getAppContext() {
                return App.Companion.get();
            }

            @Override
            public SettingsManager getAppSettings() {
                return settingsManager;
            }

            @Override
            public DesktopGestureListener.DesktopGestureCallback getDesktopGestureCallback() {
                return desktopGestureCallback;
            }

            @Override
            public ItemGestureListener.ItemGestureCallback getItemGestureCallback() {
                return itemGestureCallback;
            }

            @Override
            public ImageLoader getImageLoader() {
                return imageLoader;
            }

            @Override
            public DataManager getDataManager() {
                return dataManager;
            }

            @Override
            public AppManager getAppLoader() {
                return appLoader;
            }

            @Override
            public EventHandler getEventHandler() {
                return eventHandler;
            }

            @Override
            public Logger getLogger() {
                return logger;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean user = AppSettings.get().getBool(R.string.pref_key__desktop_rotate, false);
        boolean system = false;
        try {
            system = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1;
        } catch (Settings.SettingNotFoundException e) {
            Log.d(Home.class.getSimpleName(), "Unable to read settings", e);
        }
        boolean rotate;
        if (getResources().getBoolean(R.bool.isTablet)) { // tables has no user option to disable rotate
            rotate = system;
        } else {
            rotate = user && system;
        }
        if (rotate)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //This was originally used for a quick recent contact shortcut view, but now the view is removed from the main layout
    public class CallLogObserver extends ContentObserver {

        private final String columns[] = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME};

        public CallLogObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        public void logCallLog() {
            if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                Tool.print("Manifest.permission.READ_CALL_LOG : PERMISSION_DENIED");
                ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_PERMISSION_READ_CALL_LOG);
            } else {
                Cursor c = managedQuery(CallLog.Calls.CONTENT_URI, columns, null, null, CallLog.Calls.DATE + " DESC LIMIT 15");
                int number = c.getColumnIndex(CallLog.Calls.NUMBER);
                int name = c.getColumnIndex(CallLog.Calls.CACHED_NAME);

                Tool.print("Manifest.permission.READ_CALL_LOG : PERMISSION_GRANTED");
                quickContactFA.clear();
                while (c.moveToNext()) {
                    String phone = c.getString(number);
                    String uri = "tel:" + phone;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(uri));
                    String caller = c.getString(name);
                    quickContactFA.add(new QuickCenterItem.ContactItem(
                            new QuickCenterItem.ContactContent(caller, phone, intent,
                                    Tool.fetchThumbnail(Home.this, phone))));
                }
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            logCallLog();
        }
    }
}
