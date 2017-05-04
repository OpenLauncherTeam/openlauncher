package com.benny.openlauncher.activity;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppUpdateReceiver;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.DialogUtils;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.ShortcutReceiver;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DragNavigationControl;
import com.benny.openlauncher.viewutil.IconListAdapter;
import com.benny.openlauncher.viewutil.QuickCenterItem;
import com.benny.openlauncher.viewutil.WidgetHost;
import com.benny.openlauncher.widget.SearchBar;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.AppItemView;
import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.DesktopOptionView;
import com.benny.openlauncher.widget.Dock;
import com.benny.openlauncher.widget.DragOptionView;
import com.benny.openlauncher.widget.GroupPopupView;
import com.benny.openlauncher.widget.LauncherLoadingIcon;
import com.benny.openlauncher.widget.MiniPopupView;
import com.benny.openlauncher.widget.PagerIndicator;
import com.benny.openlauncher.widget.SmoothViewPager;
import com.benny.openlauncher.widget.SwipeListView;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

public class Home extends Activity implements DrawerLayout.DrawerListener, Desktop.OnDesktopEditListener, DesktopOptionView.DesktopOptionViewListener {
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
    public static DatabaseHelper db;
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

    @BindView(R.id.desktop)
    public Desktop desktop;
    @BindView(R.id.searchClock)
    public FrameLayout searchClock;
    @BindView(R.id.searchBarClock)
    public TextView searchBarClock;
    @BindView(R.id.searchBar)
    public SearchBar searchBar;
    @BindView(R.id.background)
    public View background;
    @BindView(R.id.left)
    public View dragLeft;
    @BindView(R.id.right)
    public View dragRight;
    @BindView(R.id.desktopIndicator)
    public PagerIndicator desktopIndicator;
    @BindView(R.id.dock)
    public Dock dock;
    @BindView(R.id.appDrawerController)
    public AppDrawerController appDrawerController;
    @BindView(R.id.groupPopup)
    public GroupPopupView groupPopup;
    @BindView(R.id.baseLayout)
    public ConstraintLayout baseLayout;
    @BindView(R.id.minibar)
    public SwipeListView minibar;
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
    @BindView(R.id.dragOptionPanel)
    public DragOptionView dragOptionPanel;
    @BindView(R.id.desktopEditOptionPanel)
    public DesktopOptionView desktopEditOptionPanel;

    public LauncherSettings.GeneralSettings generalSettings;

    private PagerIndicator appDrawerIndicator;
    private ViewGroup myScreen;
    private FastItemAdapter<QuickCenterItem.ContactItem> quickContactFA;
    private CallLogObserver callLogObserver;

    // region for the APP_DRAWER_ANIMATION
    private int cx;
    private int cy;
    private int rad;

    private final BroadcastReceiver shortcutReceiver = new ShortcutReceiver();
    private final BroadcastReceiver appUpdateReceiver = new AppUpdateReceiver();
    private final BroadcastReceiver timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                updateDesktopClock();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomActivityOnCrash.setShowErrorDetails(true);
        CustomActivityOnCrash.setEnableAppRestart(false);
        CustomActivityOnCrash.setDefaultErrorActivityDrawable(R.drawable.rip);
        CustomActivityOnCrash.install(this);

        resources = getResources();

        launcher = this;
        db = new DatabaseHelper(this);
        AppManager.getInstance(this).clearListener();

        myScreen = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_home, myScreen);
        setContentView(myScreen);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        if (LauncherSettings.getInstance(Home.this).init) {
            generalSettings = LauncherSettings.getInstance(Home.this).generalSettings;
            loadingSplash.animate().alpha(0).withEndAction(new Runnable() {
                @Override
                public void run() {
                    myScreen.removeView(loadingSplash);
                }
            });
            init();
        } else {
            loadingIcon.setLoading(true);
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    boolean ok = LauncherSettings.getInstance(Home.this).readSettings();
                    generalSettings = LauncherSettings.getInstance(Home.this).generalSettings;
                    return ok;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    loadingIcon.setLoading(false);
                    loadingSplash.animate().alpha(0).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            myScreen.removeView(loadingSplash);
                        }
                    });
                    init();
                    if (!result && Home.launcher != null) {
                        DialogUtils.alert(Home.launcher, "Some settings can't be read.", "Developer's mistake, hope you understand! Some of the settings will be reset to their default value.").show();
                    }
                }
            }.execute();
        }
    }

    private void init() {
        drawerLayout.addDrawerListener(this);

        appWidgetHost = new WidgetHost(getApplicationContext(), R.id.m_AppWidgetHost);
        appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetHost.startListening();

        initViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = Tool.getStatusBarHeight(this);
            searchClock.setPadding(0, statusBarHeight, 0, 0);
            shortcutLayout.setPadding(0, statusBarHeight, 0, 0);
            //desktopEditOptionPanel.setPadding(0, 0, 0, navBarHeight);
            //searchBar.setPadding(0, statusBarHeight, 0, 0);
            //dock.getLayoutParams().height += navBarHeight;
            //dock.setPadding(dock.getPaddingLeft(), dock.getPaddingTop(), dock.getPaddingRight(), dock.getPaddingBottom() + navBarHeight);
            //appDrawerController.setPadding(0, statusBarHeight, 0, navBarHeight);
        }

        registerBroadcastReceiver();

        // add all of the data for the desktop and dock
        AppManager.getInstance(this).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                LauncherSettings launcherSettings = LauncherSettings.getInstance(Home.this);
                if (launcherSettings.generalSettings.desktopMode != Desktop.DesktopMode.ShowAllApps) {
                    if (launcherSettings.generalSettings.firstLauncher) {
                        launcherSettings.generalSettings.firstLauncher = false;

                        // create a new app drawer button
                        Item appDrawerBtnItem = Item.newActionItem(8);

                        // center the button
                        appDrawerBtnItem.x = 2;
                        db.setItem(appDrawerBtnItem, 0, 0);
                    }
                }
                if (launcherSettings.generalSettings.desktopMode == Desktop.DesktopMode.Normal) {
                    desktop.initDesktopNormal(Home.this);
                } else {
                    desktop.initDesktopShowAll(Home.this);
                }
                dock.initDockItem(Home.this);

                AppManager.getInstance(Home.this).removeAppUpdatedListener(this);
            }
        });
        AppManager.getInstance(this).addAppDeletedListener(new AppManager.AppDeletedListener() {
            @Override
            public void onAppDeleted(AppManager.App app) {
                if (generalSettings.desktopMode == Desktop.DesktopMode.Normal) {
                    desktop.initDesktopNormal(Home.this);
                } else {
                    desktop.initDesktopShowAll(Home.this);
                }
                dock.initDockItem(Home.this);
            }
        });

        AppManager.getInstance(this).init();

        initSettings();

        System.runFinalization();
        System.gc();
    }

    // called to initialize the views
    private void initViews() {
        initMinibar();
        initQuickCenter();
        initSearchBar();

        DragNavigationControl.init(this, dragLeft, dragRight);

        appDrawerController.init();
        appDrawerIndicator = (PagerIndicator) findViewById(R.id.appDrawerIndicator);

        appDrawerController.setHome(this);
        dragOptionPanel.setHome(this);

        desktop.init(this);
        desktop.setDesktopEditListener(this);

        desktopEditOptionPanel.setDesktopOptionViewListener(this);
        desktopEditOptionPanel.setDesktopLocked(generalSettings.desktopLock);
        desktop.addOnPageChangeListener(new SmoothViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                desktopEditOptionPanel.setStarButtonColored(generalSettings.desktopHomePage == position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        desktop.setPageIndicator(desktopIndicator);

        initDock();

        dragOptionPanel.setAutoHideView(searchClock, searchBar);

        appDrawerController.setCallBack(new AppDrawerController.CallBack() {
            @Override
            public void onStart() {
                Tool.visibleViews(appDrawerIndicator);
                Tool.invisibleViews(dock, desktopIndicator, desktop);
            }

            @Override
            public void onEnd() {
                Tool.invisibleViews(searchClock, searchBar);
            }
        }, new AppDrawerController.CallBack() {
            @Override
            public void onStart() {
                updateSearchBarVisibility();

                if (appDrawerIndicator != null) {
                    appDrawerIndicator.animate().alpha(0).setDuration(100);
                }

                Tool.visibleViews(dock, desktop, desktopIndicator);
            }

            @Override
            public void onEnd() {
                if (!generalSettings.drawerRememberPage) {
                    appDrawerController.scrollToStart();
                }
                appDrawerController.getDrawer().setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initDock() {
        int iconSize = generalSettings.iconSize;
        dock.init();
        if (generalSettings.dockShowLabel) {
            dock.getLayoutParams().height = Tool.dp2px(16 + iconSize + 14 + 10, this) + Dock.bottomInset;
        } else {
            dock.getLayoutParams().height = Tool.dp2px(16 + iconSize + 10, this) + Dock.bottomInset;
        }
    }

    @Override
    public void onDesktopEdit() {
        dragOptionPanel.setAutoHideView(null);

        Tool.visibleViews(100, desktopEditOptionPanel);
        Tool.invisibleViews(100, generalSettings.desktopSearchBar ? searchClock : null, generalSettings.desktopSearchBar ?  searchBar : null, dock, desktopIndicator);

        updateSearchBarVisibility();
    }

    @Override
    public void onFinishDesktopEdit() {
        dragOptionPanel.setAutoHideView(searchClock, searchBar);

        Tool.visibleViews(100, desktopIndicator, dock);
        Tool.invisibleViews(100, desktopEditOptionPanel);

        updateSearchBarVisibility();
    }

    public void updateSearchBarVisibility() {
        if (generalSettings.desktopSearchBar) {
            searchClock.setVisibility(View.VISIBLE);
            searchClock.setAlpha(1);
            searchBar.setVisibility(View.VISIBLE);
            searchBar.setAlpha(1);

            desktop.setPadding(0, 0, 0, 0);
            ((ViewGroup.MarginLayoutParams) dragLeft.getLayoutParams()).topMargin = 0;
            ((ViewGroup.MarginLayoutParams) dragRight.getLayoutParams()).topMargin = 0;
        } else {
            searchClock.setVisibility(View.GONE);
            searchBar.setVisibility(View.GONE);

            desktop.setPadding(0, Desktop.topInsert, 0, 0);
            ((ViewGroup.MarginLayoutParams) dragLeft.getLayoutParams()).topMargin = Desktop.topInsert;
            ((ViewGroup.MarginLayoutParams) dragRight.getLayoutParams()).topMargin = Desktop.topInsert;
        }
    }

    private void updateDesktopClock() {
        if (searchBarClock != null) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            String date = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String date2 = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + ", " + String.valueOf(calendar.get(Calendar.YEAR));
            searchBarClock.setText(Html.fromHtml(date + "<br><small><small><small><small><small>" + date2 + "</small></small></small></small></small>"));
        }
    }

    @Override
    public void onRemovePage() {
        desktop.removeCurrentPage();
    }

    @Override
    public void onSetPageAsHome() {
        generalSettings.desktopHomePage = desktop.getCurrentItem();
    }

    @Override
    public void onLaunchSettings() {
        consumeNextResume = true;
        LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, this);
    }

    @Override
    public void onPickDesktopAction() {
        DialogUtils.addActionItemDialog(this, new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        Point pos = desktop.getCurrentPage().findFreeSpace();
                        desktop.addItemToCell(Item.newActionItem(8), pos.x, pos.y);
                        break;
                }
            }
        });
    }

    @Override
    public void onPickWidget() {
        pickWidget();
    }

    private void initSettings() {
        updateSearchBarVisibility();

        if (generalSettings.fullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        dock.setBackgroundColor(generalSettings.dockColor);

        appDrawerController.setBackgroundColor(generalSettings.drawerColor);
        appDrawerController.getBackground().setAlpha(0);
        appDrawerController.reloadDrawerCardTheme();

        switch (generalSettings.drawerMode) {
            case Paged:
                if (!generalSettings.drawerShowIndicator) {
                    appDrawerController.getChildAt(1).setVisibility(View.GONE);
                }
                break;
            case Vertical:
                // handled in the AppDrawerVertical class
                break;
        }
        drawerLayout.setDrawerLockMode(generalSettings.minBarEnable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void initMinibar() {
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<Integer> icons = new ArrayList<>();
        final ArrayList<String> minBarArrangement = generalSettings.miniBarArrangement;

        if (minBarArrangement == null) {
            generalSettings.miniBarArrangement = new ArrayList<>();
            for (LauncherAction.ActionItem item : LauncherAction.actionItems) {
                generalSettings.miniBarArrangement.add("0" + item.label.toString());
                labels.add(item.label.toString());
                icons.add(item.icon);
            }
        } else {
            if (minBarArrangement.size() == LauncherAction.actionItems.length) {
                for (String act : minBarArrangement) {
                    if (act.charAt(0) == '0') {
                        LauncherAction.ActionItem item = LauncherAction.getActionItemFromString(act.substring(1));
                        labels.add(item.label.toString());
                        icons.add(item.icon);
                    }
                }
            } else {
                generalSettings.miniBarArrangement = new ArrayList<>();
                for (LauncherAction.ActionItem item : LauncherAction.actionItems) {
                    generalSettings.miniBarArrangement.add("0" + item.label.toString());
                    labels.add(item.label.toString());
                    icons.add(item.icon);
                }
            }
        }


        minibar.setPadding(0, Tool.getStatusBarHeight(this), 0, Tool.getNavBarHeight(this));
        minibar.setAdapter(new IconListAdapter(this, labels, icons));
        minibar.setOnSwipeRight(new SwipeListView.OnSwipeRight() {
            @Override
            public void onSwipe(int pos, float x, float y) {
                miniPopup.showActionWindow(LauncherAction.Action.valueOf(labels.get(pos)), x, y + (shortcutLayout.getHeight() - minibar.getHeight()) / 2 - Tool.getNavBarHeight(Home.this));
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

    private void initSearchBar() {
        searchBar.setCallBack(new SearchBar.CallBack() {
            @Override
            public void onInternetSearch(String string) {

            }

            @Override
            public void onExpand() {
                Tool.invisibleViews(searchBarClock, dock, desktop, desktopIndicator);
                Tool.visibleViews(background);

                searchBar.searchBox.setFocusable(true);
                searchBar.searchBox.setFocusableInTouchMode(true);
                searchBar.searchBox.requestFocus();

                Tool.showKeyboard(Home.this, searchBar.searchBox);
            }

            @Override
            public void onCollapse() {
                Tool.visibleViews(generalSettings.desktopSearchBar ? searchBarClock : null, dock, desktop, desktopIndicator);
                Tool.invisibleViews(background);

                searchBar.searchBox.clearFocus();

                Tool.hideKeyboard(Home.this, searchBar.searchBox);
            }
        });

        // this view is just a text view of the current date
        updateDesktopClock();
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

    private void registerBroadcastReceiver() {
        registerReceiver(appUpdateReceiver, appUpdateIntentFilter);
        registerReceiver(timeChangedReceiver, timeChangesIntentFilter);
        registerReceiver(shortcutReceiver, shortcutIntentFilter);
    }

    public void pickWidget() {
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

    public void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        Item item = Item.newWidgetItem(appWidgetId);
        item.spanX = 4;
        item.spanY = 1;
        item.x = 0;
        item.y = 0;
        desktop.addItemToPage(item, desktop.getCurrentItem());
    }

    @Override
    protected void onDestroy() {
        if (appWidgetHost != null)
            appWidgetHost.stopListening();
        appWidgetHost = null;
        unregisterReceiver(appUpdateReceiver);
        unregisterReceiver(shortcutReceiver);
        if (callLogObserver != null)
            getApplicationContext().getContentResolver().unregisterContentObserver(callLogObserver);
        launcher = null;
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
    protected void onStop() {
        // save the settings in json
        LauncherSettings.getInstance(this).writeSettings();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        drawerLayout.closeDrawers();
        handleLauncherPause();
    }

    @Override
    protected void onResume() {
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

        searchBar.collapse();

        if (desktop != null) {
            if (!desktop.inEditMode) {
                if (appDrawerController.getDrawer() != null && appDrawerController.getDrawer().getVisibility() == View.VISIBLE)
                    closeAppDrawer();
                else if (generalSettings != null && !groupPopup.isShowing)
                    desktop.setCurrentItem(generalSettings.desktopHomePage);
            } else {
                desktop.pages.get(desktop.getCurrentItem()).performClick();
            }
        }


        if (groupPopup != null) {
            groupPopup.dismissPopup();
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
                if (!appItemView.isNoLabel()) {
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
