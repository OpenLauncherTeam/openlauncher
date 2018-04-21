package com.benny.openlauncher.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.BuildConfig;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.homeparts.HpAppDrawer;
import com.benny.openlauncher.activity.homeparts.HpDesktopPickAction;
import com.benny.openlauncher.activity.homeparts.HpDragNDrop;
import com.benny.openlauncher.activity.homeparts.HpInitSetup;
import com.benny.openlauncher.activity.homeparts.HpSearchBar;
import com.benny.openlauncher.interfaces.AppDeleteListener;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.manager.Setup.DataManager;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.model.Item.Type;
import com.benny.openlauncher.util.App;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.AppUpdateReceiver;
import com.benny.openlauncher.util.Definitions.ItemPosition;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherAction.Action;
import com.benny.openlauncher.util.ShortcutReceiver;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.benny.openlauncher.viewutil.IconListAdapter;
import com.benny.openlauncher.viewutil.WidgetHost;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.AppItemView;
import com.benny.openlauncher.widget.CalendarDropDownView;
import com.benny.openlauncher.widget.CellContainer;
import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.Desktop.OnDesktopEditListener;
import com.benny.openlauncher.widget.DesktopOptionView;
import com.benny.openlauncher.widget.DesktopOptionView.DesktopOptionViewListener;
import com.benny.openlauncher.widget.Dock;
import com.benny.openlauncher.widget.DragNDropLayout;
import com.benny.openlauncher.widget.DragOptionView;
import com.benny.openlauncher.widget.GroupPopupView;
import com.benny.openlauncher.widget.PagerIndicator;
import com.benny.openlauncher.widget.SearchBar;
import com.benny.openlauncher.widget.SmoothViewPager;
import com.benny.openlauncher.widget.SwipeListView;

import net.gsantner.opoc.util.ContextUtils;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.TypeCastException;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.Intrinsics;


public final class Home extends Activity implements OnDesktopEditListener, DesktopOptionViewListener, DrawerListener {
    public static final Companion Companion = new Companion();
    public static final int REQUEST_CREATE_APPWIDGET = 0x6475;
    public static final int REQUEST_PERMISSION_STORAGE = 0x3648;
    public static final int REQUEST_PICK_APPWIDGET = 0x2678;
    @Nullable
    private static Resources _resources;
    private static final IntentFilter _appUpdateIntentFilter = new IntentFilter();
    @Nullable
    private static WidgetHost _appWidgetHost;
    @NonNull
    public static AppWidgetManager _appWidgetManager;
    private static boolean _consumeNextResume;
    @NonNull
    public static DataManager _db;
    public static float _itemTouchX;
    public static float _itemTouchY;
    @Nullable
    public static Home _launcher;
    private static final IntentFilter _shortcutIntentFilter = new IntentFilter();
    private static final IntentFilter _timeChangesIntentFilter = new IntentFilter();
    private HashMap deleteMefindViewCache;
    private final AppUpdateReceiver _appUpdateReceiver = new AppUpdateReceiver();
    private int cx;
    private int cy;
    private int rad;
    private final ShortcutReceiver _shortcutReceiver = new ShortcutReceiver();
    private BroadcastReceiver _timeChangedReceiver;

    public static final class Companion {
        private Companion() {
        }

        @Nullable
        public final Home getLauncher() {
            return Home._launcher;
        }

        public final void setLauncher(@Nullable Home v) {
            Home._launcher = v;
        }

        @Nullable
        public final Resources get_resources() {
            return Home._resources;
        }

        public final void set_resources(@Nullable Resources v) {
            Home._resources = v;
        }

        @NonNull
        public final DataManager getDb() {
            DataManager dataManager = Home._db;
            if (dataManager == null) {
                Intrinsics.throwUninitializedPropertyAccessException("db");
            }
            return dataManager;
        }

        public final void setDb(@NonNull DataManager v) {

            Home._db = v;
        }

        @Nullable
        public final WidgetHost getAppWidgetHost() {
            return Home._appWidgetHost;
        }

        public final void setAppWidgetHost(@Nullable WidgetHost v) {
            Home._appWidgetHost = v;
        }

        @NonNull
        public final AppWidgetManager getAppWidgetManager() {
            AppWidgetManager appWidgetManager = Home._appWidgetManager;
            if (appWidgetManager == null) {
                Intrinsics.throwUninitializedPropertyAccessException("appWidgetManager");
            }
            return appWidgetManager;
        }

        public final void setAppWidgetManager(@NonNull AppWidgetManager v) {

            Home._appWidgetManager = v;
        }

        public final float getItemTouchX() {
            return Home._itemTouchX;
        }

        public final void setItemTouchX(float v) {
            Home._itemTouchX = v;
        }

        public final float getItemTouchY() {
            return Home._itemTouchY;
        }

        public final void setItemTouchY(float v) {
            Home._itemTouchY = v;
        }

        public final boolean getConsumeNextResume() {
            return Home._consumeNextResume;
        }

        public final void setConsumeNextResume(boolean v) {
            Home._consumeNextResume = v;
        }

        private final IntentFilter getTimeChangesIntentFilter() {
            return Home._timeChangesIntentFilter;
        }

        private final IntentFilter getAppUpdateIntentFilter() {
            return Home._appUpdateIntentFilter;
        }

        private final IntentFilter getShortcutIntentFilter() {
            return Home._shortcutIntentFilter;
        }
    }


    public View _$_findCachedViewById(int i) {
        if (this.deleteMefindViewCache == null) {
            this.deleteMefindViewCache = new HashMap();
        }
        View view = (View) this.deleteMefindViewCache.get(Integer.valueOf(i));
        if (view != null) {
            return view;
        }
        view = findViewById(i);
        this.deleteMefindViewCache.put(Integer.valueOf(i), view);
        return view;
    }

    @JvmOverloads
    public final void openAppDrawer() {
        openAppDrawer$default(this, null, 0, 0, 7, null);
    }

    @JvmOverloads
    public final void openAppDrawer(@Nullable View view) {
        openAppDrawer$default(this, view, 0, 0, 6, null);
    }

    @JvmOverloads
    public final void updateDock(boolean z) {
        updateDock$default(this, z, 0, 2, null);
    }

    static {
        Companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIME_TICK");
        Companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIMEZONE_CHANGED");
        Companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIME_SET");
        Companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_ADDED");
        Companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_REMOVED");
        Companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_CHANGED");
        Companion.getAppUpdateIntentFilter().addDataScheme("package");
        Companion.getShortcutIntentFilter().addAction("com.android.launcher.action.INSTALL_SHORTCUT");
    }

    @NonNull
    public final DrawerLayout getDrawerLayout() {
        DrawerLayout drawerLayout = (DrawerLayout) _$_findCachedViewById(R.id.drawer_layout);
        return drawerLayout;
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Companion.setLauncher(this);
        Companion.set_resources(getResources());
        ContextUtils contextUtils = new ContextUtils(getApplicationContext());
        AppSettings appSettings = AppSettings.get();

        contextUtils.setAppLanguage(appSettings.getLanguage());
        super.onCreate(savedInstanceState);
        if (!Setup.wasInitialised()) {
            Setup.init(new HpInitSetup(this));
        }
        AppSettings appSettings2 = Setup.appSettings();

        if (appSettings2.isSearchBarTimeEnabled()) {
            _timeChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                        updateSearchClock();
                    }
                }
            };
        }
        Companion.setLauncher(this);
        Companion companion = Companion;
        DataManager dataManager = Setup.dataManager();

        companion.setDb(dataManager);
        setContentView(getLayoutInflater().inflate(R.layout.activity_home, null));
        if (VERSION.SDK_INT >= 21) {
            Window window = getWindow();

            View decorView = window.getDecorView();

            decorView.setSystemUiVisibility(1536);
        }
        init();
    }

    public final void onStartApp(@NonNull Context context, @NonNull Intent intent, @Nullable View view) {


        ComponentName component = intent.getComponent();
        if (component == null) {
            Intrinsics.throwNpe();
        }
        if (Intrinsics.areEqual(component.getPackageName(), BuildConfig.APPLICATION_ID)) {
            LauncherAction.RunAction(Action.LauncherSettings, context);
            Companion.setConsumeNextResume(true);
        } else {
            try {
                context.startActivity(intent, getActivityAnimationOpts(view));
                Companion.setConsumeNextResume(true);
            } catch (Exception e) {
                Tool.toast(context, (int) R.string.toast_app_uninstalled);
            }
        }
    }

    public final void onStartApp(@NonNull Context context, @NonNull App app, @Nullable View view) {


        if (Intrinsics.areEqual(app._packageName, BuildConfig.APPLICATION_ID)) {
            LauncherAction.RunAction(Action.LauncherSettings, context);
            Companion.setConsumeNextResume(true);
        } else {
            try {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(app._packageName, app._className);
                context.startActivity(intent, getActivityAnimationOpts(view));
                Companion.setConsumeNextResume(true);
            } catch (Exception e) {
                Tool.toast(context, (int) R.string.toast_app_uninstalled);
            }
        }
    }

    protected void initAppManager() {
        Setup.appLoader().addUpdateListener(new AppManager.AppUpdatedListener() {
            @Override
            public boolean onAppUpdated(List<App> it) {
                if ((getDesktop() == null)) {
                    return false;
                }
                AppSettings appSettings = Setup.appSettings();

                if (appSettings.getDesktopStyle() != 1) {
                    appSettings = Setup.appSettings();

                    if (appSettings.isAppFirstLaunch()) {
                        appSettings = Setup.appSettings();

                        appSettings.setAppFirstLaunch(false);
                        Item appDrawerBtnItem = Item.newActionItem(8);
                        appDrawerBtnItem._x = 2;
                        Home.Companion.getDb().saveItem(appDrawerBtnItem, 0, ItemPosition.Dock);
                    }
                }
                appSettings = Setup.appSettings();

                if (appSettings.getDesktopStyle() == 0) {
                    getDesktop().initDesktopNormal(Home.this);
                } else {
                    appSettings = Setup.appSettings();

                    if (appSettings.getDesktopStyle() == 1) {
                        getDesktop().initDesktopShowAll(Home.this, Home.this);
                    }
                }
                getDock().initDockItem(Home.this);
                return true;
            }
        });
        Setup.appLoader().addDeleteListener(new AppDeleteListener() {
            @Override
            public boolean onAppDeleted(List<App> apps) {
                AppSettings appSettings = Setup.appSettings();

                if (appSettings.getDesktopStyle() == 0) {
                    ((Desktop) _$_findCachedViewById(R.id.desktop)).initDesktopNormal(Home.this);
                } else {
                    appSettings = Setup.appSettings();

                    if (appSettings.getDesktopStyle() == 1) {
                        ((Desktop) _$_findCachedViewById(R.id.desktop)).initDesktopShowAll(Home.this, Home.this);
                    }
                }
                ((Dock) _$_findCachedViewById(R.id.dock)).initDockItem(Home.this);
                setToHomePage();
                return false;
            }
        });
        AppManager.getInstance(this).init();
    }

    protected void initViews() {
        new HpSearchBar(this, (SearchBar) _$_findCachedViewById(R.id.searchBar), (CalendarDropDownView) _$_findCachedViewById(R.id.calendarDropDownView)).initSearchBar();
        initDock();
        ((AppDrawerController) _$_findCachedViewById(R.id.appDrawerController)).init();
        ((AppDrawerController) _$_findCachedViewById(R.id.appDrawerController)).setHome(this);
        ((DragOptionView) _$_findCachedViewById(R.id.dragOptionPanel)).setHome(this);
        ((Desktop) _$_findCachedViewById(R.id.desktop)).init();
        Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);

        desktop.setDesktopEditListener(this);
        ((DesktopOptionView) _$_findCachedViewById(R.id.desktopEditOptionPanel)).setDesktopOptionViewListener(this);
        DesktopOptionView desktopOptionView = (DesktopOptionView) _$_findCachedViewById(R.id.desktopEditOptionPanel);
        AppSettings appSettings = Setup.appSettings();

        desktopOptionView.updateLockIcon(appSettings.isDesktopLock());
        ((Desktop) _$_findCachedViewById(R.id.desktop)).addOnPageChangeListener(new SmoothViewPager.OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                DesktopOptionView desktopOptionView = (DesktopOptionView) _$_findCachedViewById(R.id.desktopEditOptionPanel);
                AppSettings appSettings = Setup.appSettings();

                desktopOptionView.updateHomeIcon(appSettings.getDesktopPageCurrent() == position);
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
        if (desktop == null) {
            Intrinsics.throwNpe();
        }
        desktop.setPageIndicator((PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator));
        ((DragOptionView) _$_findCachedViewById(R.id.dragOptionPanel)).setAutoHideView((SearchBar) _$_findCachedViewById(R.id.searchBar));
        new HpAppDrawer(this, (PagerIndicator) _$_findCachedViewById(R.id.appDrawerIndicator), (DragOptionView) _$_findCachedViewById(R.id.dragOptionPanel)).initAppDrawer((AppDrawerController) _$_findCachedViewById(R.id.appDrawerController));
        initMinibar();
    }

    public final void initSettings() {
        updateHomeLayout();
        AppSettings appSettings = Setup.appSettings();

        if (appSettings.isDesktopFullscreen()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
        if (desktop == null) {
            Intrinsics.throwNpe();
        }
        AppSettings appSettings2 = Setup.appSettings();

        desktop.setBackgroundColor(appSettings2.getDesktopBackgroundColor());
        Dock dock = (Dock) _$_findCachedViewById(R.id.dock);
        if (dock == null) {
            Intrinsics.throwNpe();
        }
        appSettings2 = Setup.appSettings();

        dock.setBackgroundColor(appSettings2.getDockColor());
        DrawerLayout drawerLayout = (DrawerLayout) _$_findCachedViewById(R.id.drawer_layout);
        appSettings2 = AppSettings.get();

        getDrawerLayout().setDrawerLockMode(AppSettings.get().getMinibarEnable() ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }


    public void onRemovePage() {
        if (getDesktop().isCurrentPageEmpty()) {
            Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
            if (desktop == null) {
                Intrinsics.throwNpe();
            }
            desktop.removeCurrentPage();
            return;
        }
        DialogHelper.alertDialog(this, getString(R.string.remove), "This page is not empty. Those item will also be removed.", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
                if (desktop == null) {
                    Intrinsics.throwNpe();
                }
                desktop.removeCurrentPage();
            }
        });
    }

    public final void initMinibar() {
        final ArrayList<String> labels = new ArrayList<String>();
        final ArrayList<Integer> icons = new ArrayList<>();

        for (String act : AppSettings.get().getMinibarArrangement()) {
            if (act.length() > 1 && act.charAt(0) == '0') {
                LauncherAction.ActionDisplayItem item = LauncherAction.getActionItemFromString(act.substring(1));
                if (item != null) {
                    labels.add(item._label.toString());
                    icons.add(item._icon);
                }
            }
        }

        SwipeListView minibar = findViewById(R.id.minibar);

        minibar.setAdapter(new IconListAdapter(this, labels, icons));
        minibar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                LauncherAction.Action action = LauncherAction.Action.valueOf(labels.get(i));
                if (action == LauncherAction.Action.DeviceSettings || action == LauncherAction.Action.LauncherSettings || action == LauncherAction.Action.EditMinBar) {
                    _consumeNextResume = true;
                }
                LauncherAction.RunAction(action, Home.this);
                if (action != LauncherAction.Action.DeviceSettings && action != LauncherAction.Action.LauncherSettings && action != LauncherAction.Action.EditMinBar) {
                    getDrawerLayout().closeDrawers();
                }
            }
        });
        // frame layout spans the entire side while the minibar container has gaps at the top and bottom
        ((FrameLayout) minibar.getParent()).setBackgroundColor(AppSettings.get().getMinibarBackgroundColor());
    }

    public void onBackPressed() {
        handleLauncherPause(false);
        ((DrawerLayout) _$_findCachedViewById(R.id.drawer_layout)).closeDrawers();
    }

    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    public void onDrawerOpened(@NonNull View drawerView) {

    }

    public void onDrawerClosed(@NonNull View drawerView) {

    }

    public void onDrawerStateChanged(int newState) {
    }

    protected void onResume() {
        super.onResume();
        AppSettings appSettings = Setup.appSettings();

        boolean rotate = false;
        if (appSettings.getAppRestartRequired()) {
            appSettings = Setup.appSettings();

            appSettings.setAppRestartRequired(false);
            PendingIntent restartIntentP = PendingIntent.getActivity(this, 123556, new Intent(this, Home.class), PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (mgr == null) {
                throw new TypeCastException("null cannot be cast to non-null _type android.app.AlarmManager");
            }
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + ((long) 100), restartIntentP);
            System.exit(0);
            return;
        }
        Companion.setLauncher(this);
        WidgetHost appWidgetHost = Companion.getAppWidgetHost();
        if (appWidgetHost != null) {
            appWidgetHost.startListening();
        }
        Intent intent = getIntent();

        handleLauncherPause(Intrinsics.areEqual(intent.getAction(), (Object) "android.intent.action.MAIN"));
        boolean user = AppSettings.get().getBool(R.string.pref_key__desktop_rotate, false);
        boolean system = false;
        try {
            system = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1;
        } catch (SettingNotFoundException e) {
            Log.d(Home.class.getSimpleName(), "Unable to read settings", e);
        }
        boolean rotate2 = false;
        if (getResources().getBoolean(R.bool.isTablet)) {
            rotate = system;
        } else if (user && system) {
            rotate = true;
        }
        if (rotate) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @NonNull
    public final Desktop getDesktop() {
        Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);

        return desktop;
    }

    @NonNull
    public final Dock getDock() {
        Dock dock = (Dock) _$_findCachedViewById(R.id.dock);

        return dock;
    }

    @NonNull
    public final AppDrawerController getAppDrawerController() {
        AppDrawerController appDrawerController = (AppDrawerController) _$_findCachedViewById(R.id.appDrawerController);

        return appDrawerController;
    }

    @NonNull
    public final GroupPopupView getGroupPopup() {
        GroupPopupView groupPopupView = (GroupPopupView) _$_findCachedViewById(R.id.groupPopup);

        return groupPopupView;
    }

    @NonNull
    public final SearchBar getSearchBar() {
        SearchBar searchBar = (SearchBar) _$_findCachedViewById(R.id.searchBar);

        return searchBar;
    }

    @NonNull
    public final View getBackground() {
        View _$_findCachedViewById = _$_findCachedViewById(R.id.background);

        return _$_findCachedViewById;
    }

    @NonNull
    public final PagerIndicator getDesktopIndicator() {
        PagerIndicator pagerIndicator = (PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator);

        return pagerIndicator;
    }

    @NonNull
    public final DragNDropLayout getDragNDropView() {
        DragNDropLayout dragNDropLayout = (DragNDropLayout) _$_findCachedViewById(R.id.dragNDropView);

        return dragNDropLayout;
    }

    private final void init() {
        Companion.setAppWidgetHost(new WidgetHost(getApplicationContext(), R.id.app_widget_host));
        Companion companion = Companion;
        AppWidgetManager instance = AppWidgetManager.getInstance(this);

        companion.setAppWidgetManager(instance);
        WidgetHost appWidgetHost = Companion.getAppWidgetHost();
        if (appWidgetHost == null) {
            Intrinsics.throwNpe();
        }
        appWidgetHost.startListening();
        initViews();
        HpDragNDrop hpDragNDrop = new HpDragNDrop();
        View _$_findCachedViewById = _$_findCachedViewById(R.id.leftDragHandle);

        View _$_findCachedViewById2 = _$_findCachedViewById(R.id.rightDragHandle);

        DragNDropLayout dragNDropLayout = (DragNDropLayout) _$_findCachedViewById(R.id.dragNDropView);

        hpDragNDrop.initDragNDrop(this, _$_findCachedViewById, _$_findCachedViewById2, dragNDropLayout);
        registerBroadcastReceiver();
        initAppManager();
        initSettings();
        System.runFinalization();
        System.gc();
    }

    public final void onUninstallItem(@NonNull Item item) {

        Companion.setConsumeNextResume(true);
        Setup.eventHandler().showDeletePackageDialog(this, item);
    }

    public final void onRemoveItem(@NonNull Item item) {

        View coordinateToChildView;
        switch (item._locationInLauncher) {
            case 0:
                Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
                Desktop desktop2 = (Desktop) _$_findCachedViewById(R.id.desktop);

                coordinateToChildView = desktop2.getCurrentPage().coordinateToChildView(new Point(item._x, item._y));
                if (coordinateToChildView == null) {
                    Intrinsics.throwNpe();
                }
                desktop.removeItem(coordinateToChildView, true);
                break;
            case 1:
                Dock dock = (Dock) _$_findCachedViewById(R.id.dock);
                coordinateToChildView = ((Dock) _$_findCachedViewById(R.id.dock)).coordinateToChildView(new Point(item._x, item._y));
                if (coordinateToChildView == null) {
                    Intrinsics.throwNpe();
                }
                dock.removeItem(coordinateToChildView, true);
                break;
            default:
                break;
        }
        Companion.getDb().deleteItem(item, true);
    }

    public final void onInfoItem(@NonNull Item item) {

        if (item._type == Type.APP) {
            try {
                String str = "android.settings.APPLICATION_DETAILS_SETTINGS";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("package:");
                Intent intent = item._intent;
                if (intent == null) {
                    Intrinsics.throwNpe();
                }
                ComponentName component = intent.getComponent();
                if (component == null) {
                    Intrinsics.throwNpe();
                }
                stringBuilder.append(component.getPackageName());
                startActivity(new Intent(str, Uri.parse(stringBuilder.toString())));
            } catch (Exception e) {
                Tool.toast((Context) this, (int) R.string.toast_app_uninstalled);
            }
        }
    }

    private final Bundle getActivityAnimationOpts(View view) {
        Bundle bundle = null;
        if (view == null) {
            return null;
        }
        ActivityOptions opts = null;
        if (VERSION.SDK_INT >= 23) {
            int left = 0;
            int top = 0;
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();
            if (view instanceof AppItemView) {
                width = (int) ((AppItemView) view).getIconSize();
                left = (int) ((AppItemView) view).getDrawIconLeft();
                top = (int) ((AppItemView) view).getDrawIconTop();
            }
            opts = ActivityOptions.makeClipRevealAnimation(view, left, top, width, height);
        } else if (VERSION.SDK_INT < 21) {
            opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        if (opts != null) {
            bundle = opts.toBundle();
        }
        return bundle;
    }

    public void onDesktopEdit() {
        Tool.visibleViews(100, 20, (DesktopOptionView) _$_findCachedViewById(R.id.desktopEditOptionPanel));
        hideDesktopIndicator();
        updateDock$default(this, false, 0, 2, null);
        updateSearchBar(false);
    }

    public void onFinishDesktopEdit() {
        Tool.invisibleViews(100, 20, (DesktopOptionView) _$_findCachedViewById(R.id.desktopEditOptionPanel));
        ((PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator)).hideDelay();
        showDesktopIndicator();
        updateDock$default(this, true, 0, 2, null);
        updateSearchBar(true);
    }

    public void onSetPageAsHome() {
        AppSettings appSettings = Setup.appSettings();

        Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
        if (desktop == null) {
            Intrinsics.throwNpe();
        }
        appSettings.setDesktopPageCurrent(desktop.getCurrentItem());
    }

    public void onLaunchSettings() {
        Companion.setConsumeNextResume(true);
        Setup.eventHandler().showLauncherSettings(this);
    }

    public void onPickDesktopAction() {
        new HpDesktopPickAction(this).onPickDesktopAction();
    }

    public void onPickWidget() {
        pickWidget();
    }

    private final void initDock() {
        int iconSize = Setup.appSettings().getDockIconSize();
        Dock dock = (Dock) _$_findCachedViewById(R.id.dock);
        if (dock == null) {
            Intrinsics.throwNpe();
        }
        dock.setHome(this);
        dock.init();
        AppSettings appSettings = Setup.appSettings();

        if (appSettings.isDockShowLabel()) {
            dock = (Dock) _$_findCachedViewById(R.id.dock);
            if (dock == null) {
                Intrinsics.throwNpe();
            }
            dock.getLayoutParams().height = Tool.dp2px(((16 + iconSize) + 14) + 10, (Context) this) + dock.getBottomInset();
        } else {
            dock = (Dock) _$_findCachedViewById(R.id.dock);
            if (dock == null) {
                Intrinsics.throwNpe();
            }
            dock.getLayoutParams().height = Tool.dp2px((16 + iconSize) + 10, (Context) this) + dock.getBottomInset();
        }
    }

    public final void dimBackground() {
        Tool.visibleViews(_$_findCachedViewById(R.id.background));
    }

    public final void unDimBackground() {
        Tool.invisibleViews(_$_findCachedViewById(R.id.background));
    }

    public final void clearRoomForPopUp() {
        Tool.invisibleViews((Desktop) _$_findCachedViewById(R.id.desktop));
        hideDesktopIndicator();
        updateDock$default(this, false, 0, 2, null);
    }

    public final void unClearRoomForPopUp() {
        Tool.visibleViews((Desktop) _$_findCachedViewById(R.id.desktop));
        showDesktopIndicator();
        updateDock$default(this, true, 0, 2, null);
    }

    @JvmOverloads
    public static /* bridge */ /* synthetic */ void updateDock$default(Home home, boolean z, long j, int i, Object obj) {
        if ((i & 2) != 0) {
            j = 0;
        }
        home.updateDock(z, j);
    }

    @JvmOverloads
    public final void updateDock(boolean show, long delay) {
        AppSettings appSettings = Setup.appSettings();

        Desktop desktop;
        LayoutParams layoutParams;
        PagerIndicator pagerIndicator;
        if (appSettings.getDockEnable() && show) {
            Tool.visibleViews(100, delay, (Dock) _$_findCachedViewById(R.id.dock));
            desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
            if (desktop == null) {
                Intrinsics.throwNpe();
            }
            layoutParams = desktop.getLayoutParams();
            if (layoutParams == null) {
                throw new TypeCastException("null cannot be cast to non-null _type android.view.ViewGroup.MarginLayoutParams");
            }
            ((MarginLayoutParams) layoutParams).bottomMargin = Tool.dp2px(4, (Context) this);
            pagerIndicator = (PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator);
            if (pagerIndicator == null) {
                Intrinsics.throwNpe();
            }
            layoutParams = pagerIndicator.getLayoutParams();
            if (layoutParams == null) {
                throw new TypeCastException("null cannot be cast to non-null _type android.view.ViewGroup.MarginLayoutParams");
            }
            ((MarginLayoutParams) layoutParams).bottomMargin = Tool.dp2px(4, (Context) this);
        } else {
            appSettings = Setup.appSettings();

            if (appSettings.getDockEnable()) {
                Tool.invisibleViews(100, (Dock) _$_findCachedViewById(R.id.dock));
            } else {
                Tool.goneViews(100, (Dock) _$_findCachedViewById(R.id.dock));
                pagerIndicator = (PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator);
                if (pagerIndicator == null) {
                    Intrinsics.throwNpe();
                }
                layoutParams = pagerIndicator.getLayoutParams();
                if (layoutParams == null) {
                    throw new TypeCastException("null cannot be cast to non-null _type android.view.ViewGroup.MarginLayoutParams");
                }
                ((MarginLayoutParams) layoutParams).bottomMargin = Desktop._bottomInset + Tool.dp2px(4, (Context) this);
                desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
                if (desktop == null) {
                    Intrinsics.throwNpe();
                }
                layoutParams = desktop.getLayoutParams();
                if (layoutParams == null) {
                    throw new TypeCastException("null cannot be cast to non-null _type android.view.ViewGroup.MarginLayoutParams");
                }
                ((MarginLayoutParams) layoutParams).bottomMargin = Tool.dp2px(4, (Context) this);
            }
        }
    }

    public final void updateSearchBar(boolean show) {
        AppSettings appSettings = Setup.appSettings();

        if (appSettings.getSearchBarEnable() && show) {
            Tool.visibleViews(100, (SearchBar) _$_findCachedViewById(R.id.searchBar));
        } else {
            appSettings = Setup.appSettings();

            if (appSettings.getSearchBarEnable()) {
                Tool.invisibleViews(100, (SearchBar) _$_findCachedViewById(R.id.searchBar));
            } else {
                Tool.goneViews((SearchBar) _$_findCachedViewById(R.id.searchBar));
            }
        }
    }

    public final void updateDesktopIndicatorVisibility() {
        AppSettings appSettings = Setup.appSettings();

        if (appSettings.isDesktopShowIndicator()) {
            Tool.visibleViews(100, (PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator));
            return;
        }
        Tool.goneViews(100, (PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator));
    }

    public final void hideDesktopIndicator() {
        AppSettings appSettings = Setup.appSettings();

        if (appSettings.isDesktopShowIndicator()) {
            Tool.invisibleViews(100, (PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator));
        }
    }

    public final void showDesktopIndicator() {
        AppSettings appSettings = Setup.appSettings();

        if (appSettings.isDesktopShowIndicator()) {
            Tool.visibleViews(100, (PagerIndicator) _$_findCachedViewById(R.id.desktopIndicator));
        }
    }

    public final void updateSearchClock() {
        SearchBar searchBar = (SearchBar) _$_findCachedViewById(R.id.searchBar);
        if (searchBar == null) {
            Intrinsics.throwNpe();
        }
        TextView textView = searchBar._searchClock;

        if (textView.getText() != null) {
            try {
                searchBar = (SearchBar) _$_findCachedViewById(R.id.searchBar);
                if (searchBar == null) {
                    Intrinsics.throwNpe();
                }
                searchBar.updateClock();
            } catch (Exception e) {
                ((SearchBar) _$_findCachedViewById(R.id.searchBar))._searchClock.setText(R.string.bad_format);
            }
        }
    }

    public final void updateHomeLayout() {
        updateSearchBar(true);
        updateDock$default(this, true, 0, 2, null);
        updateDesktopIndicatorVisibility();
        AppSettings appSettings = Setup.appSettings();

        if (!appSettings.getSearchBarEnable()) {
            View _$_findCachedViewById = _$_findCachedViewById(R.id.leftDragHandle);
            if (_$_findCachedViewById == null) {
                Intrinsics.throwNpe();
            }
            LayoutParams layoutParams = _$_findCachedViewById.getLayoutParams();
            if (layoutParams == null) {
                throw new TypeCastException("null cannot be cast to non-null _type android.view.ViewGroup.MarginLayoutParams");
            }
            ((MarginLayoutParams) layoutParams).topMargin = Desktop._topInset;
            _$_findCachedViewById = _$_findCachedViewById(R.id.rightDragHandle);
            if (_$_findCachedViewById == null) {
                Intrinsics.throwNpe();
            }
            layoutParams = _$_findCachedViewById.getLayoutParams();
            if (layoutParams == null) {
                throw new TypeCastException("null cannot be cast to non-null _type android.view.ViewGroup.MarginLayoutParams");
            }
            Desktop desktop;
            ((MarginLayoutParams) layoutParams).topMargin = Desktop._topInset;
            desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
            if (desktop == null) {
                Intrinsics.throwNpe();
            }
            desktop.setPadding(0, Desktop._topInset, 0, 0);
        }
        appSettings = Setup.appSettings();

        if (!appSettings.getDockEnable()) {
            getDesktop().setPadding(0, 0, 0, Desktop._bottomInset);
        }
    }

    private final void registerBroadcastReceiver() {
        registerReceiver(_appUpdateReceiver, Companion.getAppUpdateIntentFilter());
        if (_timeChangedReceiver != null) {
            registerReceiver(_timeChangedReceiver, Companion.getTimeChangesIntentFilter());
        }
        registerReceiver(_shortcutReceiver, Companion.getShortcutIntentFilter());
    }

    private final void pickWidget() {
        Companion.setConsumeNextResume(true);
        int appWidgetId = Companion.getAppWidgetHost().allocateAppWidgetId();
        Intent pickIntent = new Intent("android.appwidget.action.APPWIDGET_PICK");
        pickIntent.putExtra("appWidgetId", appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    private final void configureWidget(Intent data) {
        if (data == null) {
            Intrinsics.throwNpe();
        }
        Bundle extras = data.getExtras();
        if (extras == null) {
            Intrinsics.throwNpe();
        }
        int appWidgetId = extras.getInt("appWidgetId", -1);
        AppWidgetProviderInfo appWidgetInfo = Companion.getAppWidgetManager().getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra("appWidgetId", appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    private final void createWidget(Intent data) {
        if (data == null) {
            Intrinsics.throwNpe();
        }
        Bundle extras = data.getExtras();
        if (extras == null) {
            Intrinsics.throwNpe();
        }
        int appWidgetId = extras.getInt("appWidgetId", -1);
        AppWidgetProviderInfo appWidgetInfo = Companion.getAppWidgetManager().getAppWidgetInfo(appWidgetId);
        Item item = Item.newWidgetItem(appWidgetId);
        int i = appWidgetInfo.minWidth - 1;
        Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
        if (desktop == null) {
            Intrinsics.throwNpe();
        }
        List pages = desktop.getPages();
        Home launcher = Companion.getLauncher();
        if (launcher == null) {
            Intrinsics.throwNpe();
        }
        Desktop desktop2 = (Desktop) launcher._$_findCachedViewById(R.id.desktop);
        if (desktop2 == null) {
            Intrinsics.throwNpe();
        }
        Object obj = pages.get(desktop2.getCurrentItem());

        item._spanX = (i / ((CellContainer) obj).getCellWidth()) + 1;
        i = appWidgetInfo.minHeight - 1;
        desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
        if (desktop == null) {
            Intrinsics.throwNpe();
        }
        pages = desktop.getPages();
        launcher = Companion.getLauncher();
        if (launcher == null) {
            Intrinsics.throwNpe();
        }
        desktop2 = (Desktop) launcher._$_findCachedViewById(R.id.desktop);
        if (desktop2 == null) {
            Intrinsics.throwNpe();
        }
        obj = pages.get(desktop2.getCurrentItem());

        item._spanY = (i / ((CellContainer) obj).getCellHeight()) + 1;
        Desktop desktop3 = (Desktop) _$_findCachedViewById(R.id.desktop);
        if (desktop3 == null) {
            Intrinsics.throwNpe();
        }
        Point point = desktop3.getCurrentPage().findFreeSpace(item._spanX, item._spanY);
        if (point != null) {
            item._x = point.x;
            item._y = point.y;
            DataManager db = Companion.getDb();
            desktop2 = (Desktop) _$_findCachedViewById(R.id.desktop);
            if (desktop2 == null) {
                Intrinsics.throwNpe();
            }
            db.saveItem(item, desktop2.getCurrentItem(), ItemPosition.Desktop);
            desktop = (Desktop) _$_findCachedViewById(R.id.desktop);
            if (desktop == null) {
                Intrinsics.throwNpe();
            }
            desktop2 = (Desktop) _$_findCachedViewById(R.id.desktop);
            if (desktop2 == null) {
                Intrinsics.throwNpe();
            }
            desktop.addItemToPage(item, desktop2.getCurrentItem());
        } else {
            Tool.toast((Context) this, (int) R.string.toast_not_enough_space);
        }
    }

    protected void onDestroy() {
        WidgetHost appWidgetHost = Companion.getAppWidgetHost();
        if (appWidgetHost != null) {
            appWidgetHost.stopListening();
        }
        Companion.setAppWidgetHost((WidgetHost) null);
        unregisterReceiver(_appUpdateReceiver);
        if (_timeChangedReceiver != null) {
            unregisterReceiver(_timeChangedReceiver);
        }
        unregisterReceiver(_shortcutReceiver);
        Companion.setLauncher((Home) null);
        super.onDestroy();
    }

    public void onLowMemory() {
        System.runFinalization();
        System.gc();
        super.onLowMemory();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == -1) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == 0 && data != null) {
            int appWidgetId = data.getIntExtra("appWidgetId", -1);
            if (appWidgetId != -1) {
                WidgetHost appWidgetHost = Companion.getAppWidgetHost();
                if (appWidgetHost != null) {
                    appWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            }
        }
    }

    protected void onStart() {
        Companion.setLauncher(this);
        WidgetHost appWidgetHost = Companion.getAppWidgetHost();
        if (appWidgetHost != null) {
            appWidgetHost.startListening();
        }
        super.onStart();
    }

    private final void handleLauncherPause(boolean wasHomePressed) {
        if (!Companion.getConsumeNextResume() || wasHomePressed) {
            onHandleLauncherPause();
        } else {
            Companion.setConsumeNextResume(false);
        }
    }

    protected void onHandleLauncherPause() {
        ((GroupPopupView) _$_findCachedViewById(R.id.groupPopup)).dismissPopup();
        ((CalendarDropDownView) _$_findCachedViewById(R.id.calendarDropDownView)).animateHide();
        ((DragNDropLayout) _$_findCachedViewById(R.id.dragNDropView)).hidePopupMenu();
        if (!((SearchBar) _$_findCachedViewById(R.id.searchBar)).collapse()) {
            if (((Desktop) _$_findCachedViewById(R.id.desktop)) != null) {
                Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);

                if (desktop.getInEditMode()) {
                    desktop = (Desktop) _$_findCachedViewById(R.id.desktop);

                    List pages = desktop.getPages();
                    Desktop desktop2 = (Desktop) _$_findCachedViewById(R.id.desktop);

                    ((CellContainer) pages.get(desktop2.getCurrentItem())).performClick();
                } else {
                    AppDrawerController appDrawerController = (AppDrawerController) _$_findCachedViewById(R.id.appDrawerController);

                    View drawer = appDrawerController.getDrawer();

                    if (drawer.getVisibility() == View.VISIBLE) {
                        closeAppDrawer();
                    } else {
                        setToHomePage();
                    }
                }
            }
        }
    }

    private final void setToHomePage() {
        Desktop desktop = (Desktop) _$_findCachedViewById(R.id.desktop);

        AppSettings appSettings = Setup.appSettings();

        desktop.setCurrentItem(appSettings.getDesktopPageCurrent());
    }

    @JvmOverloads
    public static /* bridge */ /* synthetic */ void openAppDrawer$default(Home home, View view, int i, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            view = (Desktop) home._$_findCachedViewById(R.id.desktop);
        }
        if ((i3 & 2) != 0) {
            i = -1;
        }
        if ((i3 & 4) != 0) {
            i2 = -1;
        }
        home.openAppDrawer(view, i, i2);
    }

    @JvmOverloads
    public final void openAppDrawer(@Nullable View view, int x, int y) {
        if (!(x > 0 && y > 0)) {
            int[] pos = new int[2];
            view.getLocationInWindow(pos);
            cx = pos[0];
            cy = pos[1];

            cx += view.getWidth() / 2f;
            cy += view.getHeight() / 2f;
            if (view instanceof AppItemView) {
                AppItemView appItemView = (AppItemView) view;
                if (appItemView != null && appItemView.getShowLabel()) {
                    cy -= Tool.dp2px(14, this) / 2f;
                }
                rad = (int) (appItemView.getIconSize() / 2f - Tool.toPx(4));
            }
            cx -= ((MarginLayoutParams) getAppDrawerController().getDrawer().getLayoutParams()).getMarginStart();
            cy -= ((MarginLayoutParams) getAppDrawerController().getDrawer().getLayoutParams()).topMargin;
            cy -= getAppDrawerController().getPaddingTop();
        } else {
            cx = x;
            cy = y;
            rad = 0;
        }
        int finalRadius = Math.max(getAppDrawerController().getDrawer().getWidth(), getAppDrawerController().getDrawer().getHeight());
        getAppDrawerController().open(cx, cy, rad, finalRadius);
    }

    public final void closeAppDrawer() {
        int finalRadius = Math.max(getAppDrawerController().getDrawer().getWidth(), getAppDrawerController().getDrawer().getHeight());
        getAppDrawerController().close(cx, cy, rad, finalRadius);
    }
}
