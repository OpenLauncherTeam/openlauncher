package com.benny.openlauncher.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
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
import com.benny.openlauncher.activity.homeparts.HpDragOption;
import com.benny.openlauncher.activity.homeparts.HpInitSetup;
import com.benny.openlauncher.activity.homeparts.HpSearchBar;
import com.benny.openlauncher.interfaces.AppDeleteListener;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.model.Item.Type;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.receivers.AppUpdateReceiver;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.util.Definitions.ItemPosition;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherAction.Action;
import com.benny.openlauncher.receivers.ShortcutReceiver;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.benny.openlauncher.viewutil.MinibarAdapter;
import com.benny.openlauncher.viewutil.WidgetHost;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.AppItemView;
import com.benny.openlauncher.widget.CellContainer;
import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.Desktop.OnDesktopEditListener;
import com.benny.openlauncher.widget.DesktopOptionView;
import com.benny.openlauncher.widget.DesktopOptionView.DesktopOptionViewListener;
import com.benny.openlauncher.widget.Dock;
import com.benny.openlauncher.widget.ItemOptionView;
import com.benny.openlauncher.widget.GroupPopupView;
import com.benny.openlauncher.widget.PagerIndicator;
import com.benny.openlauncher.widget.SearchBar;
import com.benny.openlauncher.widget.SmoothViewPager;
import com.benny.openlauncher.widget.MinibarView;

import net.gsantner.opoc.util.ContextUtils;

import java.util.ArrayList;
import java.util.List;

public final class HomeActivity extends Activity implements OnDesktopEditListener, DesktopOptionViewListener {
    public static final Companion Companion = new Companion();
    public static final int REQUEST_CREATE_APPWIDGET = 0x6475;
    public static final int REQUEST_PERMISSION_STORAGE = 0x3648;
    public static final int REQUEST_PICK_APPWIDGET = 0x2678;
    public static WidgetHost _appWidgetHost;
    public static AppWidgetManager _appWidgetManager;
    public static boolean _consumeNextResume;
    public static float _itemTouchX;
    public static float _itemTouchY;

    // static launcher variables
    public static HomeActivity _launcher;
    public static DatabaseHelper _db;

    public FrameLayout minibarFrame;
    public View statusView;
    public View navigationView;

    // receiver variables
    private static final IntentFilter _appUpdateIntentFilter = new IntentFilter();
    private static final IntentFilter _shortcutIntentFilter = new IntentFilter();
    private static final IntentFilter _timeChangedIntentFilter = new IntentFilter();
    private AppUpdateReceiver _appUpdateReceiver = new AppUpdateReceiver();
    private ShortcutReceiver _shortcutReceiver = new ShortcutReceiver();
    private BroadcastReceiver _timeChangedReceiver;

    private int cx;
    private int cy;
    private int rad;

    public static final class Companion {
        private Companion() {
        }

        public final HomeActivity getLauncher() {
            return _launcher;
        }

        public final void setLauncher(@Nullable HomeActivity v) {
            _launcher = v;
        }
    }

    static {
        _timeChangedIntentFilter.addAction("android.intent.action.TIME_TICK");
        _timeChangedIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        _timeChangedIntentFilter.addAction("android.intent.action.TIME_SET");
        _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        _appUpdateIntentFilter.addDataScheme("package");
        _shortcutIntentFilter.addAction("com.android.launcher.action.INSTALL_SHORTCUT");
    }

    public final DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout);
    }

    public final Desktop getDesktop() {
        return findViewById(R.id.desktop);
    }

    public final Dock getDock() {
        return findViewById(R.id.dock);
    }

    public final AppDrawerController getAppDrawerController() {
        return findViewById(R.id.appDrawerController);
    }

    public final GroupPopupView getGroupPopup() {
        return findViewById(R.id.groupPopup);
    }

    public final SearchBar getSearchBar() {
        return findViewById(R.id.searchBar);
    }

    public final View getBackground() {
        return findViewById(R.id.background_frame);
    }

    public final PagerIndicator getDesktopIndicator() {
        return findViewById(R.id.desktopIndicator);
    }

    public final ItemOptionView getDragNDropView() {
        return findViewById(R.id.dragNDropView);
    }

    public final FrameLayout getMinibarFrame() {
        return findViewById(R.id.minibar_frame);
    }

    public final View getStatusView() {
        return findViewById(R.id.status_frame);
    }

    public final View getNavigationView() {
        return findViewById(R.id.navigation_frame);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Companion.setLauncher(this);
        AppSettings appSettings = AppSettings.get();

        ContextUtils contextUtils = new ContextUtils(getApplicationContext());
        contextUtils.setAppLanguage(appSettings.getLanguage());
        super.onCreate(savedInstanceState);
        if (!Setup.wasInitialised()) {
            Setup.init(new HpInitSetup(this));
        }
        if (appSettings.isSearchBarTimeEnabled()) {
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
        _db = Setup.dataManager();

        setContentView(getLayoutInflater().inflate(R.layout.activity_home, null));

        // transparent status and navigation
        if (VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(1536);
        }
        init();
    }

    private void init() {
        _appWidgetManager = AppWidgetManager.getInstance(this);
        _appWidgetHost = new WidgetHost(getApplicationContext(), R.id.app_widget_host);
        _appWidgetHost.startListening();

        // item drag and drop
        HpDragOption hpDragOption = new HpDragOption();
        View findViewById = findViewById(R.id.leftDragHandle);
        View findViewById2 = findViewById(R.id.rightDragHandle);
        ItemOptionView itemOptionView = findViewById(R.id.dragNDropView);
        hpDragOption.initDragNDrop(this, findViewById, findViewById2, itemOptionView);

        registerBroadcastReceiver();
        initAppManager();
        initSettings();
        initViews();
    }

    protected void initAppManager() {
        Setup.appLoader().addUpdateListener(new AppManager.AppUpdatedListener() {
            @Override
            public boolean onAppUpdated(List<App> it) {
                if (getDesktop() == null) {
                    return false;
                }

                AppSettings appSettings = Setup.appSettings();
                getDesktop().initDesktop();
                if (appSettings.isAppFirstLaunch()) {
                    appSettings.setAppFirstLaunch(false);
                    appSettings.setAppShowIntro(false);
                    Item appDrawerBtnItem = Item.newActionItem(8);
                    appDrawerBtnItem._x = 2;
                    _db.saveItem(appDrawerBtnItem, 0, ItemPosition.Dock);
                }
                getDock().initDockItem(HomeActivity.this);
                return true;
            }
        });
        Setup.appLoader().addDeleteListener(new AppDeleteListener() {
            @Override
            public boolean onAppDeleted(List<App> apps) {
                getDesktop().initDesktop();
                getDock().initDockItem(HomeActivity.this);
                setToHomePage();
                return false;
            }
        });
        AppManager.getInstance(this).init();
    }

    protected void initViews() {
        new HpSearchBar(this, getSearchBar()).initSearchBar();
        getAppDrawerController().init();
        getDock().setHome(this);

        getDesktop().init();
        getDesktop().setDesktopEditListener(this);
        getDesktop().setPageIndicator(getDesktopIndicator());

        DesktopOptionView desktopOptionView = findViewById(R.id.desktopEditOptionPanel);
        AppSettings appSettings = Setup.appSettings();

        desktopOptionView.setDesktopOptionViewListener(this);
        desktopOptionView.updateLockIcon(appSettings.isDesktopLock());
        getDesktop().addOnPageChangeListener(new SmoothViewPager.OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                desktopOptionView.updateHomeIcon(appSettings.getDesktopPageCurrent() == position);
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        new HpAppDrawer(this, findViewById(R.id.appDrawerIndicator)).initAppDrawer(getAppDrawerController());
        initMinibar();
    }

    public final void initMinibar() {
        final ArrayList<LauncherAction.ActionDisplayItem> items = AppSettings.get().getMinibarArrangement();
        MinibarView minibar = findViewById(R.id.minibar);
        minibar.setAdapter(new MinibarAdapter(this, items));
        minibar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                LauncherAction.Action action = items.get(i)._action;
                LauncherAction.RunAction(items.get(i), HomeActivity.this);
                if (action == LauncherAction.Action.EditMinibar || action == LauncherAction.Action.DeviceSettings || action == LauncherAction.Action.LauncherSettings) {
                    _consumeNextResume = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getDrawerLayout().closeDrawers();
                        }
                    }, 300);
                } else {
                    getDrawerLayout().closeDrawers();
                }
            }
        });
    }

    public final void initSettings() {
        updateHomeLayout();

        AppSettings appSettings = Setup.appSettings();
        if (appSettings.isDesktopFullscreen()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // set background colors
        getDesktop().setBackgroundColor(appSettings.getDesktopBackgroundColor());
        getDock().setBackgroundColor(appSettings.getDockColor());

        // frame colors
        getMinibarFrame().setBackgroundColor(appSettings.getMinibarBackgroundColor());
        getStatusView().setBackgroundColor(appSettings.getDesktopInsetColor());
        getNavigationView().setBackgroundColor(appSettings.getDesktopInsetColor());

        // lock the minibar
        getDrawerLayout().setDrawerLockMode(appSettings.getMinibarEnable() ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void registerBroadcastReceiver() {
        registerReceiver(_appUpdateReceiver, _appUpdateIntentFilter);
        registerReceiver(_shortcutReceiver, _shortcutIntentFilter);
        if (_timeChangedReceiver != null) {
            registerReceiver(_timeChangedReceiver, _timeChangedIntentFilter);
        }
    }

    public void onRemovePage() {
        if (getDesktop().isCurrentPageEmpty()) {
            getDesktop().removeCurrentPage();
            return;
        }
        DialogHelper.alertDialog(this, getString(R.string.remove), "This page is not empty. Those items will also be removed.", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                getDesktop().removeCurrentPage();
            }
        });
    }

    public final void onStartApp(@NonNull Context context, @NonNull App app, @Nullable View view) {
        if (BuildConfig.APPLICATION_ID.equals(app._packageName)) {
            LauncherAction.RunAction(Action.LauncherSettings, context);
            _consumeNextResume = true;
        } else {
            try {
                Intent intent = Tool.getIntentFromApp(app);
                context.startActivity(intent, getActivityAnimationOpts(view));
                _consumeNextResume = true;
            } catch (Exception e) {
                Tool.toast(context, R.string.toast_app_uninstalled);
            }
        }
    }

    public final void onUninstallItem(@NonNull Item item) {
        _consumeNextResume = true;
        Setup.eventHandler().showDeletePackageDialog(this, item);
    }

    public final void onRemoveItem(@NonNull Item item) {
        Desktop desktop = getDesktop();
        View coordinateToChildView;
        switch (item._location) {
            case Item.LOCATION_DESKTOP:
                coordinateToChildView = desktop.getCurrentPage().coordinateToChildView(new Point(item._x, item._y));
                desktop.removeItem(coordinateToChildView, true);
                break;
            case Item.LOCATION_DOCK:
                Dock dock = getDock();
                coordinateToChildView = dock.coordinateToChildView(new Point(item._x, item._y));
                dock.removeItem(coordinateToChildView, true);
                break;
            default:
                break;
        }
        _db.deleteItem(item, true);
    }

    public final void onInfoItem(@NonNull Item item) {
        if (item._type == Type.APP) {
            try {
                String str = "android.settings.APPLICATION_DETAILS_SETTINGS";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("package:");
                Intent intent = item._intent;
                ComponentName component = intent.getComponent();
                stringBuilder.append(component.getPackageName());
                startActivity(new Intent(str, Uri.parse(stringBuilder.toString())));
            } catch (Exception e) {
                Tool.toast(this, R.string.toast_app_uninstalled);
            }
        }
    }

    private Bundle getActivityAnimationOpts(View view) {
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
        Tool.visibleViews(100, 20, (DesktopOptionView) findViewById(R.id.desktopEditOptionPanel));
        updateDesktopIndicator(false);
        updateDock(false);
        updateSearchBar(false);
    }

    public void onFinishDesktopEdit() {
        Tool.invisibleViews(100, 20, (DesktopOptionView) findViewById(R.id.desktopEditOptionPanel));
        getDesktopIndicator().hideDelay();
        updateDesktopIndicator(true);
        updateDock(true);
        updateSearchBar(true);
    }

    public void onSetPageAsHome() {
        AppSettings appSettings = Setup.appSettings();
        Desktop desktop = findViewById(R.id.desktop);
        appSettings.setDesktopPageCurrent(desktop.getCurrentItem());
    }

    public void onLaunchSettings() {
        _consumeNextResume = true;
        Setup.eventHandler().showLauncherSettings(this);
    }

    public void onPickDesktopAction() {
        new HpDesktopPickAction(this).onPickDesktopAction();
    }

    public void onPickWidget() {
        pickWidget();
    }

    public final void dimBackground() {
        Tool.visibleViews(getBackground());
    }

    public final void unDimBackground() {
        Tool.invisibleViews(getBackground());
    }

    public final void clearRoomForPopUp() {
        Tool.invisibleViews(getDesktop());
        updateDesktopIndicator(false);
        updateDock(false);
    }

    public final void unClearRoomForPopUp() {
        Tool.visibleViews(getDesktop());
        updateDesktopIndicator(true);
        updateDock(true);
    }

    public final void updateDock(boolean show) {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getDockEnable() && show) {
            Tool.visibleViews(100, getDock());
        } else {
            if (appSettings.getDockEnable()) {
                Tool.invisibleViews(100, getDock());
            } else {
                Tool.goneViews(100, getDock());
            }
        }
    }

    public final void updateSearchBar(boolean show) {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getSearchBarEnable() && show) {
            Tool.visibleViews(100, getSearchBar());
        } else {
            if (appSettings.getSearchBarEnable()) {
                Tool.invisibleViews(100, getSearchBar());
            } else {
                Tool.goneViews(getSearchBar());
            }
        }
    }

    public final void updateDesktopIndicator(boolean show) {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.isDesktopShowIndicator() && show) {
            Tool.visibleViews(100, getDesktopIndicator());
        } else {
            Tool.goneViews(100, getDesktopIndicator());
        }
    }

    public final void updateSearchClock() {
        TextView textView = getSearchBar()._searchClock;

        if (textView.getText() != null) {
            try {
                getSearchBar().updateClock();
            } catch (Exception e) {
                getSearchBar()._searchClock.setText(R.string.bad_format);
            }
        }
    }

    public final void updateHomeLayout() {
        updateSearchBar(true);
        updateDock(true);
        updateDesktopIndicator(true);
    }

    private void pickWidget() {
        _consumeNextResume = true;
        int appWidgetId = _appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent("android.appwidget.action.APPWIDGET_PICK");
        pickIntent.putExtra("appWidgetId", appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt("appWidgetId", -1);
        AppWidgetProviderInfo appWidgetInfo = _appWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra("appWidgetId", appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    private void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = _appWidgetManager.getAppWidgetInfo(appWidgetId);
        Item item = Item.newWidgetItem(appWidgetId);
        Desktop desktop = getDesktop();
        List<CellContainer> pages = desktop.getPages();
        item._spanX = (appWidgetInfo.minWidth - 1) / pages.get(desktop.getCurrentItem()).getCellWidth() + 1;
        item._spanY = (appWidgetInfo.minHeight - 1) / pages.get(desktop.getCurrentItem()).getCellHeight() + 1;
        Point point = desktop.getCurrentPage().findFreeSpace(item._spanX, item._spanY);
        if (point != null) {
            item._x = point.x;
            item._y = point.y;

            // add item to database
            _db.saveItem(item, desktop.getCurrentItem(), Definitions.ItemPosition.Desktop);
            desktop.addItemToPage(item, desktop.getCurrentItem());
        } else {
            Tool.toast(this, R.string.toast_not_enough_space);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra("appWidgetId", -1);
            if (appWidgetId != -1) {
                _appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    @Override
    public void onBackPressed() {
        handleLauncherResume(false);
        getDrawerLayout().closeDrawers();
    }

    @Override
    protected void onStart() {
        _appWidgetHost.startListening();
        _launcher = this;

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _appWidgetHost.startListening();
        _launcher = this;

        // handle restart if something needs to be reset
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getAppRestartRequired()) {
            appSettings.setAppRestartRequired(false);
            recreate();
            return;
        }

        // handle launcher rotation
        if (appSettings.isDesktopRotate()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Intent intent = getIntent();
        handleLauncherResume(Intent.ACTION_MAIN.equals(intent.getAction()));
    }

    @Override
    protected void onDestroy() {
        _appWidgetHost.stopListening();
        _launcher = null;

        unregisterReceiver(_appUpdateReceiver);
        unregisterReceiver(_shortcutReceiver);
        unregisterReceiver(_timeChangedReceiver);
        super.onDestroy();
    }

    private void handleLauncherResume(boolean wasHomePressed) {
        if (!_consumeNextResume || wasHomePressed) {
            getGroupPopup().dismissPopup();
            getDragNDropView().hidePopupMenu();
            getSearchBar().collapse();
            if (!getSearchBar().collapse()) {
                if (getDesktop().getInEditMode()) {
                    CellContainer currentPage = getDesktop().getPages().get(getDesktop().getCurrentItem());
                    currentPage.performClick();
                } else if (getAppDrawerController().getDrawer().getVisibility() == View.VISIBLE) {
                    closeAppDrawer();
                } else {
                    setToHomePage();
                }
            }
        } else {
            _consumeNextResume = false;
        }
    }

    private void setToHomePage() {
        AppSettings appSettings = Setup.appSettings();
        getDesktop().setCurrentItem(appSettings.getDesktopPageCurrent());
    }

    public final void openAppDrawer() {
        openAppDrawer(null, 0, 0);
    }

    public final void openAppDrawer(View view, int x, int y) {
        if (!(x > 0 && y > 0) && view != null) {
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
