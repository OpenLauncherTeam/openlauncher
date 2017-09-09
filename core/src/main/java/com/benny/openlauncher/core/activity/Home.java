package com.benny.openlauncher.core.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.AppDeleteListener;
import com.benny.openlauncher.core.interfaces.AppUpdateListener;
import com.benny.openlauncher.core.interfaces.DialogListener;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.AppUpdateReceiver;
import com.benny.openlauncher.core.util.Definitions;
import com.benny.openlauncher.core.util.ShortcutReceiver;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.viewutil.DragNavigationControl;
import com.benny.openlauncher.core.viewutil.WidgetHost;
import com.benny.openlauncher.core.widget.AppDrawerController;
import com.benny.openlauncher.core.widget.AppItemView;
import com.benny.openlauncher.core.widget.CalendarDropDownView;
import com.benny.openlauncher.core.widget.Desktop;
import com.benny.openlauncher.core.widget.DesktopOptionView;
import com.benny.openlauncher.core.widget.Dock;
import com.benny.openlauncher.core.widget.DragOptionView;
import com.benny.openlauncher.core.widget.GroupPopupView;
import com.benny.openlauncher.core.widget.PagerIndicator;
import com.benny.openlauncher.core.widget.SearchBar;
import com.benny.openlauncher.core.widget.SmoothViewPager;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.List;

public abstract class Home extends Activity implements Desktop.OnDesktopEditListener, DesktopOptionView.DesktopOptionViewListener {
    public static final int REQUEST_PICK_APPWIDGET = 0x6475;
    public static final int REQUEST_CREATE_APPWIDGET = 0x3648;
    public static final int REQUEST_PERMISSION_READ_CALL_LOG = 0x981294;
    public static final int REQUEST_PERMISSION_CALL = 0x981295;
    public static final int REQUEST_PERMISSION_STORAGE = 0x981296;
    private static final IntentFilter timeChangesIntentFilter;
    private static final IntentFilter appUpdateIntentFilter;
    private static final IntentFilter shortcutIntentFilter;

    // static members, easier to access from any activity and class
    public static Home launcher;
    public static Setup.DataManager db;
    public static WidgetHost appWidgetHost;
    public static AppWidgetManager appWidgetManager;
    public static Resources resources;

    // used for the drag shadow builder
    public static int touchX = 0;
    public static int touchY = 0;
    public static boolean consumeNextResume;

    static {
        timeChangesIntentFilter = new IntentFilter();
        timeChangesIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        timeChangesIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        timeChangesIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        appUpdateIntentFilter = new IntentFilter();
        appUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        appUpdateIntentFilter.addDataScheme("package");

        shortcutIntentFilter = new IntentFilter();
        shortcutIntentFilter.addAction("com.android.launcher.action.INSTALL_SHORTCUT");
    }

    private final BroadcastReceiver shortcutReceiver = new ShortcutReceiver();
    private final BroadcastReceiver appUpdateReceiver = new AppUpdateReceiver();
    public Desktop desktop;
    private BroadcastReceiver timeChangedReceiver = null;
    public View background;
    public View dragLeft;
    public View dragRight;
    public PagerIndicator desktopIndicator;
    public Dock dock;
    public AppDrawerController appDrawerController;
    public ConstraintLayout baseLayout;
    public DragOptionView dragOptionView;
    public DesktopOptionView desktopEditOptionView;
    public CalendarDropDownView calendarDropDownView;
    private PagerIndicator appDrawerIndicator;
    protected ViewGroup myScreen;
    public SearchBar searchBar;
    public GroupPopupView groupPopup;
    // region for the APP_DRAWER_ANIMATION
    private int cx;
    private int cy;
    private int rad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Setup.wasInitialised())
            initStaticHelper();

        if (Setup.appSettings().isSearchBarTimeEnabled()) {
            timeChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(Intent.ACTION_TIME_TICK)) {
                        updateSearchClock();
                    }
                }
            };
        }

        resources = getResources();

        launcher = this;
        db = Setup.dataManager();

        myScreen = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_home, myScreen);
        setContentView(myScreen);

        bindViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        init();
    }

    protected abstract void initStaticHelper();

    protected void bindViews() {
        desktop = findViewById(R.id.desktop);
        background = findViewById(R.id.background);
        dragLeft = findViewById(R.id.left);
        dragRight = findViewById(R.id.right);
        desktopIndicator = findViewById(R.id.desktopIndicator);
        dock = findViewById(R.id.dock);
        appDrawerController = findViewById(R.id.appDrawerController);
        calendarDropDownView = findViewById(R.id.calendarDropDownView);
        baseLayout = findViewById(R.id.baseLayout);
        dragOptionView = findViewById(R.id.dragOptionPanel);
        desktopEditOptionView = findViewById(R.id.desktopEditOptionPanel);
        searchBar = findViewById(R.id.searchBar);
        groupPopup = findViewById(R.id.groupPopup);
    }

    protected void unbindViews() {
        desktop = null;
        background = null;
        dragLeft = null;
        dragRight = null;
        desktopIndicator = null;
        dock = null;
        appDrawerController = null;
        calendarDropDownView = null;
        baseLayout = null;
        dragOptionView = null;
        desktopEditOptionView = null;
        searchBar = null;
        groupPopup = null;
    }

    private void init() {
        appWidgetHost = new WidgetHost(getApplicationContext(), R.id.app_widget_host);
        appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetHost.startListening();

        initViews();

        registerBroadcastReceiver();

        // add all of the data for the desktop and dock
        initAppManager();

        initSettings();

        System.runFinalization();
        System.gc();
    }

    // called to initialize the views
    protected void initViews() {
        initSearchBar();
        initDock();

        DragNavigationControl.init(this, dragLeft, dragRight);

        appDrawerController.init();
        appDrawerIndicator = findViewById(R.id.appDrawerIndicator);

        appDrawerController.setHome(this);
        dragOptionView.setHome(this);

        desktop.init();
        desktop.setDesktopEditListener(this);

        desktopEditOptionView.setDesktopOptionViewListener(this);
        desktopEditOptionView.updateLockIcon(Setup.appSettings().isDesktopLock());
        desktop.addOnPageChangeListener(new SmoothViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                desktopEditOptionView.updateHomeIcon(Setup.appSettings().getDesktopPageCurrent() == position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        desktop.setPageIndicator(desktopIndicator);

        dragOptionView.setAutoHideView(searchBar);

        appDrawerController.setCallBack(new AppDrawerController.CallBack() {
            @Override
            public void onStart() {
                Tool.visibleViews(appDrawerIndicator);
                Tool.invisibleViews(desktop);
                hideDesktopIndicator();
                updateDock(false);
                updateSearchBar(false);
            }

            @Override
            public void onEnd() {
            }
        }, new AppDrawerController.CallBack() {
            @Override
            public void onStart() {
                Tool.invisibleViews(appDrawerIndicator);
                Tool.visibleViews(desktop);
                showDesktopIndicator();
                updateDock(true, 200);
                updateSearchBar(!dragOptionView.isDraggedFromDrawer);
                dragOptionView.isDraggedFromDrawer = false;
            }

            @Override
            public void onEnd() {
                if (!Setup.appSettings().isDrawerRememberPosition()) {
                    appDrawerController.scrollToStart();
                }
                appDrawerController.getDrawer().setVisibility(View.INVISIBLE);
            }
        });
    }

    public void onStartApp(Context context, Intent intent) {
        try {
            context.startActivity(intent);
            Home.consumeNextResume = true;
        } catch (Exception e) {
            Tool.toast(context, R.string.toast_app_uninstalled);
        }
    }

    public void onStartApp(Context context, App app) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(app.getPackageName(), app.getClassName());
            context.startActivity(intent);

            Home.consumeNextResume = true;
        } catch (Exception e) {
            Tool.toast(context, R.string.toast_app_uninstalled);
        }
    }

    protected void initAppManager() {
        Setup.appLoader().addUpdateListener(new AppUpdateListener<App>() {
            @Override
            public boolean onAppUpdated(List<App> apps) {
                if (Setup.appSettings().getDesktopStyle() != Desktop.DesktopMode.SHOW_ALL_APPS) {
                    if (Setup.appSettings().isAppFirstLaunch()) {
                        Setup.appSettings().setAppFirstLaunch(false);

                        // create a new app drawer button
                        Item appDrawerBtnItem = Item.newActionItem(Definitions.ACTION_LAUNCHER);

                        // center the button
                        appDrawerBtnItem.setX(Definitions.DOCK_DEFAULT_CENTER_ITEM_INDEX_X);
                        db.saveItem(appDrawerBtnItem, 0, Definitions.ItemPosition.Dock);
                    }
                }
                if (Setup.appSettings().getDesktopStyle() == Desktop.DesktopMode.NORMAL) {
                    desktop.initDesktopNormal(Home.this);
                } else if (Setup.appSettings().getDesktopStyle() == Desktop.DesktopMode.SHOW_ALL_APPS) {
                    desktop.initDesktopShowAll(Home.this, Home.this);
                }
                dock.initDockItem(Home.this);

                // remove this listener
                return true;
            }
        });
        Setup.appLoader().addDeleteListener(new AppDeleteListener<App>() {
            @Override
            public boolean onAppDeleted(List<App> app) {
                if (Setup.appSettings().getDesktopStyle() == Desktop.DesktopMode.NORMAL) {
                    desktop.initDesktopNormal(Home.this);
                } else if (Setup.appSettings().getDesktopStyle() == Desktop.DesktopMode.SHOW_ALL_APPS) {
                    desktop.initDesktopShowAll(Home.this, Home.this);
                }
                dock.initDockItem(Home.this);
                return false;
            }
        });
    }

    @Override
    public void onDesktopEdit() {
        //dragOptionView.resetAutoHideView();

        Tool.visibleViews(100, desktopEditOptionView);

        hideDesktopIndicator();
        updateDock(false);
        updateSearchBar(false);
    }

    @Override
    public void onFinishDesktopEdit() {
        showDesktopIndicator();
        Tool.invisibleViews(100, desktopEditOptionView);
        updateDock(true);
        updateSearchBar(true);

        //dragOptionView.setAutoHideView(searchBar);
    }

    @Override
    public void onRemovePage() {
        desktop.removeCurrentPage();
    }

    @Override
    public void onSetPageAsHome() {
        Setup.appSettings().setDesktopPageCurrent(desktop.getCurrentItem());
    }

    @Override
    public void onLaunchSettings() {
        consumeNextResume = true;
        Setup.eventHandler().showLauncherSettings(this);
    }

    @Override
    public void onPickDesktopAction() {
        Setup.eventHandler().showPickAction(this, new DialogListener.OnAddAppDrawerItemListener() {
            @Override
            public void onAdd() {
                Point pos = desktop.getCurrentPage().findFreeSpace();
                if (pos != null)
                    desktop.addItemToCell(Item.newActionItem(Definitions.ACTION_LAUNCHER), pos.x, pos.y);
                else
                    Tool.toast(Home.this, R.string.toast_not_enough_space);
            }
        });
    }

    @Override
    public void onPickWidget() {
        pickWidget();
    }

    protected void initSettings() {
        updateHomeLayout();

        if (Setup.appSettings().isDesktopFullscreen()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        desktop.setBackgroundColor(Setup.appSettings().getDesktopBackgroundColor());
        dock.setBackgroundColor(Setup.appSettings().getDockColor());

        appDrawerController.setBackgroundColor(Setup.appSettings().getDrawerBackgroundColor());
        appDrawerController.getBackground().setAlpha(0);
        appDrawerController.reloadDrawerCardTheme();

        switch (Setup.appSettings().getDrawerStyle()) {
            case AppDrawerController.DrawerMode.HORIZONTAL_PAGED:
                if (!Setup.appSettings().isDrawerShowIndicator()) {
                    appDrawerController.getChildAt(1).setVisibility(View.GONE);
                }
                break;
            case AppDrawerController.DrawerMode.VERTICAL:
                // handled in the AppDrawerVertical class
                break;
        }
    }

    private void initDock() {
        int iconSize = Setup.appSettings().getDockIconSize();
        dock.init();
        if (Setup.appSettings().isDockShowLabel()) {
            dock.getLayoutParams().height = Tool.dp2px(16 + iconSize + 14 + 10, this) + Dock.bottomInset;
        } else {
            dock.getLayoutParams().height = Tool.dp2px(16 + iconSize + 10, this) + Dock.bottomInset;
        }
    }

    public void dimBackground() {
        Tool.visibleViews(background);
    }

    public void unDimBackground() {
        Tool.invisibleViews(background);
    }

    public void clearRoomForPopUp() {
        Tool.invisibleViews(desktop);
        hideDesktopIndicator();
        updateDock(false);
    }

    public void unClearRoomForPopUp() {
        Tool.visibleViews(desktop);
        showDesktopIndicator();
        updateDock(true);
    }

    protected void initSearchBar() {
        searchBar.setCallback(new SearchBar.CallBack() {
            @Override
            public void onInternetSearch(String string) {
                Intent intent = new Intent();

                if (Tool.isIntentActionAvailable(getApplicationContext(), Intent.ACTION_WEB_SEARCH) && !Setup.appSettings().getSearchBarForceBrowser()) {
                    intent.setAction(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, string);
                } else {
                    String baseUri = Setup.appSettings().getSearchBarBaseURI();
                    String searchUri = baseUri.contains("{query}") ? baseUri.replace("{query}", string) : (baseUri + string);

                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(searchUri));
                }

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onExpand() {
                clearRoomForPopUp();
                dimBackground();

                searchBar.searchInput.setFocusable(true);
                searchBar.searchInput.setFocusableInTouchMode(true);
                searchBar.searchInput.post(new Runnable() {
                    @Override
                    public void run() {
                        searchBar.searchInput.requestFocus();
                    }
                });

                Tool.showKeyboard(Home.this, searchBar.searchInput);
            }

            @Override
            public void onCollapse() {
                unClearRoomForPopUp();
                unDimBackground();

                searchBar.searchInput.clearFocus();

                Tool.hideKeyboard(Home.this, searchBar.searchInput);
            }
        });
        searchBar.searchClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarDropDownView.animateShow();
            }
        });

        // this view is just a text view of the current date
        updateSearchClock();
    }

    public void updateDock(boolean show) {
        updateDock(show, 0);
    }

    public void updateDock(boolean show, long delay) {
        if (Setup.appSettings().getDockEnable() && show) {
            Tool.visibleViews(100, delay, dock);
            ((ViewGroup.MarginLayoutParams) desktop.getLayoutParams()).bottomMargin = Tool.dp2px(4, this);
            ((ViewGroup.MarginLayoutParams) desktopIndicator.getLayoutParams()).bottomMargin = Tool.dp2px(4, this);
        } else {
            if (Setup.appSettings().getDockEnable()) {
                Tool.invisibleViews(100, dock);
            } else {
                Tool.goneViews(100, dock);
                ((ViewGroup.MarginLayoutParams) desktopIndicator.getLayoutParams()).bottomMargin = Desktop.bottomInset + Tool.dp2px(4, this);
                ((ViewGroup.MarginLayoutParams) desktop.getLayoutParams()).bottomMargin = Tool.dp2px(4, this);
            }
        }
    }

    public void updateSearchBar(boolean show) {
        if (Setup.appSettings().getSearchBarEnable() && show) {
            Tool.visibleViews(100, searchBar);
        } else {
            if (Setup.appSettings().getSearchBarEnable()) {
                Tool.invisibleViews(100, searchBar);
            } else {
                Tool.goneViews(searchBar);
            }
        }
    }

    public void updateDesktopIndicatorVisibility() {
        if (Setup.appSettings().isDesktopShowIndicator()) {
            Tool.visibleViews(100, desktopIndicator);
        } else {
            Tool.goneViews(100, desktopIndicator);
        }
    }

    public void hideDesktopIndicator() {
        if (Setup.appSettings().isDesktopShowIndicator())
            Tool.invisibleViews(100, desktopIndicator);
    }

    public void showDesktopIndicator() {
        if (Setup.appSettings().isDesktopShowIndicator())
            Tool.visibleViews(100, desktopIndicator);
    }

    private void updateSearchClock() {
        if (searchBar.searchClock.getText() != null) {
            searchBar.updateClock();
        }
    }

    public void updateHomeLayout() {
        updateSearchBar(true);
        updateDock(true);

        updateDesktopIndicatorVisibility();

        if (!Setup.appSettings().getSearchBarEnable()) {
            ((ViewGroup.MarginLayoutParams) dragLeft.getLayoutParams()).topMargin = Desktop.topInset;
            ((ViewGroup.MarginLayoutParams) dragRight.getLayoutParams()).topMargin = Desktop.topInset;
            desktop.setPadding(0, Desktop.topInset, 0, 0);
        }

        if (!Setup.appSettings().getDockEnable()) {
            desktop.setPadding(0, 0, 0, Desktop.bottomInset);
        }
    }

    private void registerBroadcastReceiver() {
        registerReceiver(appUpdateReceiver, appUpdateIntentFilter);
        if (timeChangedReceiver != null) {
            registerReceiver(timeChangedReceiver, timeChangesIntentFilter);
        }
        registerReceiver(shortcutReceiver, shortcutIntentFilter);
    }

    private void pickWidget() {
        consumeNextResume = true;
        int appWidgetId = appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    private void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        Item item = Item.newWidgetItem(appWidgetId);
        item.setSpanX(((appWidgetInfo.minWidth - 1) / desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellWidth) + 1);
        item.setSpanY(((appWidgetInfo.minHeight - 1) / desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellHeight) + 1);
        Point point = desktop.getCurrentPage().findFreeSpace(item.getSpanX(), item.getSpanY());
        if (point != null) {
            item.setX(point.x);
            item.setY(point.y);

            // add item to database
            db.saveItem(item, desktop.getCurrentItem(), Definitions.ItemPosition.Desktop);
            desktop.addItemToPage(item, desktop.getCurrentItem());
        } else {
            Tool.toast(Home.this, R.string.toast_not_enough_space);
        }
    }

    @Override
    protected void onDestroy() {
        if (appWidgetHost != null)
            appWidgetHost.stopListening();
        appWidgetHost = null;
        unregisterReceiver(appUpdateReceiver);
        if (timeChangedReceiver != null) {
            unregisterReceiver(timeChangedReceiver);
        }
        unregisterReceiver(shortcutReceiver);
        launcher = null;

        unbindViews();

        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        System.runFinalization();
        System.gc();
        super.onLowMemory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    @Override
    protected void onStart() {
        launcher = this;
        if (appWidgetHost != null) {
            appWidgetHost.startListening();
        }
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        handleLauncherPause();
    }

    @Override
    protected void onResume() {
        if (Setup.appSettings().getAppRestartRequired()) {
            Setup.appSettings().setAppRestartRequired(false);

            Intent restartIntent = new Intent(this, Home.class);
            PendingIntent restartIntentP = PendingIntent.getActivity(this, 123556, restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntentP);
            System.exit(0);
            return;
        }

        launcher = this;
        if (appWidgetHost != null) {
            appWidgetHost.startListening();
        }

        handleLauncherPause();
        super.onResume();
    }

    private void handleLauncherPause() {
        if (consumeNextResume) {
            consumeNextResume = false;
            return;
        }

        onHandleLauncherPause();
    }

    protected void onHandleLauncherPause() {
        searchBar.collapse();
        groupPopup.dismissPopup();
        calendarDropDownView.animateHide();

        if (desktop != null) {
            if (!desktop.inEditMode) {
                if (appDrawerController.getDrawer() != null && appDrawerController.getDrawer().getVisibility() == View.VISIBLE) {
                    closeAppDrawer();
                } else {
                    desktop.setCurrentItem(Setup.appSettings().getDesktopPageCurrent());
                }
            } else {
                desktop.pages.get(desktop.getCurrentItem()).performClick();
            }
        }
    }

    // open the app drawer with animation
    public void openAppDrawer() {
        openAppDrawer(desktop, -1, -1);
    }

    public void openAppDrawer(View view) {
        openAppDrawer(view, -1, -1);
    }

    public void openAppDrawer(View view, int x, int y) {
        if (!(x > 0 && y > 0)) {
            int[] pos = new int[2];
            view.getLocationInWindow(pos);
            cx = pos[0];
            cy = pos[1];

            cx += view.getWidth() / 2;
            cy += view.getHeight() / 2;
            if (view instanceof AppItemView) {
                AppItemView appItemView = (AppItemView) view;
                if (!appItemView.getShowLabel()) {
                    cy -= Tool.dp2px(14, this) / 2;
                }
                rad = (int) (appItemView.getIconSize() / 2 - Tool.dp2px(4, view.getContext()));
            }
            cx -= ((ViewGroup.MarginLayoutParams) appDrawerController.getDrawer().getLayoutParams()).leftMargin;
            cy -= ((ViewGroup.MarginLayoutParams) appDrawerController.getDrawer().getLayoutParams()).topMargin;
            cy -= appDrawerController.getPaddingTop();
        } else {
            cx = x;
            cy = y;
            rad = 0;
        }
        int finalRadius = Math.max(appDrawerController.getDrawer().getWidth(), appDrawerController.getDrawer().getHeight());
        appDrawerController.open(cx, cy, rad, finalRadius);
    }

    public void closeAppDrawer() {
        int finalRadius = Math.max(appDrawerController.getDrawer().getWidth(), appDrawerController.getDrawer().getHeight());
        appDrawerController.close(cx, cy, rad, finalRadius);
    }
}
