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
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.benny.launcheranim.LauncherLoadingIcon;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppUpdateReceiver;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.ShortcutReceiver;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DragNavigationControl;
import com.benny.openlauncher.viewutil.IconListAdapter;
import com.benny.openlauncher.viewutil.QuickCenterItem;
import com.benny.openlauncher.viewutil.WidgetHost;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.AppItemView;
import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.Dock;
import com.benny.openlauncher.widget.DragOptionView;
import com.benny.openlauncher.widget.GroupPopupView;
import com.benny.openlauncher.widget.MiniPopupView;
import com.benny.openlauncher.widget.PagerIndicator;
import com.benny.openlauncher.widget.SwipeListView;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

public class Home extends Activity implements DrawerLayout.DrawerListener, Desktop.OnDesktopEditListener {

    public static final int REQUEST_PICK_APPWIDGET = 0x6475;
    public static final int REQUEST_CREATE_APPWIDGET = 0x3648;
    public static final int MINIBAR_EDIT = 0x2873;
    public static final int REQUEST_PERMISSION_READ_CALL_LOG = 0x981294;
    public static final int REQUEST_PERMISSION_CALL = 0x981295;
    public static final int REQUEST_PERMISSION_STORAGE = 0x981296;
    //static members, easier to access from any activity and class.
    @Nullable
    public static Home launcher;
    public static WidgetHost appWidgetHost;
    public static AppWidgetManager appWidgetManager;
    public static Resources resources;
    //This two integer is used for the drag shadow builder to get the touch point of users' finger.
    public static int touchX = 0, touchY = 0;

    @BindView(R.id.removepage)
    TextView removepage;
    @BindView(R.id.setashomepage)
    TextView setashomepage;
    @BindView(R.id.addwidgetbtn)
    TextView addwidgetbtn;
    @BindView(R.id.addLauncherAction)
    TextView addLauncherAction;
    @BindView(R.id.openSettings)
    TextView openSettings;

    @BindView(R.id.desktop)
    public Desktop desktop;
    @BindView(R.id.searchBarClock)
    public TextView searchBarClock;
    @BindView(R.id.desktopIndicator)
    public PagerIndicator desktopIndicator;
    @BindView(R.id.desktopDock)
    public Dock desktopDock;
    @BindView(R.id.appDrawerController)
    public AppDrawerController appDrawerController;
    @BindView(R.id.dragOptionPanel)
    public DragOptionView dragOptionPanel;
    @BindView(R.id.groupPopup)
    public GroupPopupView groupPopup;
    @BindView(R.id.baseLayout)
    public ConstraintLayout baseLayout;
    @BindView(R.id.minBar)
    public SwipeListView minBar;
    @BindView(R.id.drawer_layout)
    public DrawerLayout drawerLayout;
    @BindView(R.id.quickContactRv)
    public RecyclerView quickContactRv;
    @BindView(R.id.miniPopup)
    public MiniPopupView miniPopup;
    @BindView(R.id.shortcutLayout)
    public RelativeLayout shortcutLayout;
    @BindView(R.id.loadingIcon)
    public LauncherLoadingIcon loadingIcon;
    @BindView(R.id.loadingSplash)
    public FrameLayout loadingSplash;
    @BindView(R.id.desktopEditOptionPanel)
    public LinearLayout desktopEditOptionPanel;
    @BindView(R.id.searchBar)
    public FrameLayout searchBar;

    //normal members, currently not necessary to access from elsewhere.
    private View appSearchBar;
    private PagerIndicator appDrawerIndicator;
    private ViewGroup myScreen;
    private FastItemAdapter<QuickCenterItem.ContactItem> quickContactFA;
    private CallLogObserver callLogObserver;

    //region APP_DRAWER_ANIMATION
    private int cx, cy, rad;

    private static final IntentFilter timeChangesIntentFilter;
    private static final IntentFilter appUpdateIntentFilter;
    private static final IntentFilter shortcutIntentFilter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomActivityOnCrash.setShowErrorDetails(true);
        CustomActivityOnCrash.setEnableAppRestart(false);
        CustomActivityOnCrash.setDefaultErrorActivityDrawable(R.drawable.rip);
        CustomActivityOnCrash.install(this);

        resources = getResources();

        launcher = this;
        AppManager.getInstance(this).clearListener();

        myScreen = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_home, myScreen);
        setContentView(myScreen);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        if (LauncherSettings.getInstance(Home.this).init) {
            myScreen.removeView(loadingSplash);
            init();
        } else {
            loadingIcon.setLoading(true);
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    return LauncherSettings.getInstance(Home.this).readSettings();
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
                    if (!result) {
                        if (Home.launcher != null)
                            Tool.DialogHelper.alert(Home.launcher, "Some settings can't be read", "Developer's mistake, hope you understand! Some of the settings will be reset to default value.").show();
                    }
                }
            }.execute();
        }
    }

    //region INIT

    private void init() {
        drawerLayout.addDrawerListener(this);

        appWidgetHost = new WidgetHost(getApplicationContext(), R.id.m_AppWidgetHost);
        appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetHost.startListening();

        initViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int navBarHeight = Tool.getNavBarHeight(this);
            int statusBarHeight = Tool.getStatusBarHeight(this);

            findViewById(R.id.shortcutLayout).setPadding(0, statusBarHeight, 0, navBarHeight);
            searchBar.setPadding(0, statusBarHeight, 0, 0);

            desktopDock.getLayoutParams().height += navBarHeight;
            desktopDock.setPadding(desktopDock.getPaddingLeft(), desktopDock.getPaddingTop(), desktopDock.getPaddingRight(), desktopDock.getPaddingBottom() + navBarHeight);

            appDrawerController.setPadding(0, statusBarHeight, 0, navBarHeight);
            desktopEditOptionPanel.setPadding(0, 0, 0, navBarHeight);
        }

        registerBroadcastReceiver();

        //We init all the desktop and desktopDock app data
        AppManager.getInstance(this).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                LauncherSettings launcherSettings = LauncherSettings.getInstance(Home.this);
                if (launcherSettings.generalSettings.desktopMode != Desktop.DesktopMode.ShowAllApps) {
                    if (launcherSettings.generalSettings.firstLauncher) {
                        launcherSettings.generalSettings.firstLauncher = false;
                        Desktop.Item appDrawerBtnItem = Desktop.Item.newAppDrawerBtn();
                        //We center it
                        appDrawerBtnItem.x = 2;
                        launcherSettings.dockData.add(appDrawerBtnItem);
                    }
                }
                desktop.initDesktopItem(Home.this);
                desktopDock.initDockItem(Home.this);

                AppManager.getInstance(Home.this).removeAppUpdatedListener(this);
            }
        });
        AppManager.getInstance(this).addAppDeletedListener(new AppManager.AppDeletedListener() {
            @Override
            public void onAppDeleted(AppManager.App app) {
                desktop.initDesktopItem(Home.this);
                desktopDock.initDockItem(Home.this);
            }
        });

        AppManager.getInstance(this).init();

        initSettings();

        System.runFinalization();
        System.gc();
    }

    /**
     * This should be only called to init the view at Activity start up
     */
    private void initViews() {
        initMinBar();
        initQuickCenter();

        DragNavigationControl.init(this, findViewById(R.id.left), findViewById(R.id.right));

        updateDesktopClock();

        appDrawerController.init();
        appSearchBar = findViewById(R.id.appSearchBar);
        appDrawerIndicator = (PagerIndicator) findViewById(R.id.appDrawerIndicator);

        appDrawerController.setHome(this);
        dragOptionPanel.setHome(this);

        desktop.init(this);
        desktop.setDesktopEditListener(this);

        initDesktopEditViews();

        desktopIndicator.setViewPager(desktop);
        desktop.setPageIndicator(desktopIndicator);

        initDock();

        dragOptionPanel.setAutoHideView(searchBar);

        appDrawerController.setCallBack(new AppDrawerController.CallBack() {
            @Override
            public void onStart() {
                if (appSearchBar != null) {
                    if (LauncherSettings.getInstance(Home.this).generalSettings.desktopSearchBar)
                        searchBar.animate().alpha(0).setDuration(100);
                    appSearchBar.setAlpha(0);
                    appSearchBar.setVisibility(View.VISIBLE);
                    appSearchBar.animate().setStartDelay(100).alpha(1).setDuration(100);
                }
                if (appDrawerIndicator != null) {
                    appDrawerIndicator.setVisibility(View.VISIBLE);
                    appDrawerIndicator.animate().alpha(1).setDuration(100);
                }
            }

            @Override
            public void onEnd() {
                if (LauncherSettings.getInstance(Home.this).generalSettings.desktopSearchBar) {
                    searchBar.setVisibility(View.INVISIBLE);
                } else {
                    searchBar.setVisibility(View.GONE);
                }
                desktopDock.setVisibility(View.INVISIBLE);
                desktopIndicator.setVisibility(View.INVISIBLE);
                desktop.setVisibility(View.INVISIBLE);
            }
        }, new AppDrawerController.CallBack() {
            @Override
            public void onStart() {
                if (LauncherSettings.getInstance(Home.this).generalSettings.desktopSearchBar) {
                    searchBar.setVisibility(View.VISIBLE);
                } else {
                    searchBar.setVisibility(View.GONE);
                }
                desktopDock.setVisibility(View.VISIBLE);
                desktopIndicator.setVisibility(View.VISIBLE);
                desktop.setVisibility(View.VISIBLE);

                if (appDrawerIndicator != null)
                    appDrawerIndicator.animate().alpha(0).setDuration(100);
                if (appSearchBar != null) {
                    appSearchBar.animate().setStartDelay(0).alpha(0).setDuration(100).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            if (LauncherSettings.getInstance(Home.this).generalSettings.desktopSearchBar) {
                                searchBar.setVisibility(View.VISIBLE);
                            } else {
                                searchBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onEnd() {
                if (!LauncherSettings.getInstance(Home.this).generalSettings.drawerRememberPage) {
                    appDrawerController.scrollToStart();
                }
                appDrawerController.getDrawer().setVisibility(View.INVISIBLE);
                if (!dragOptionPanel.dragging && appSearchBar != null) {
                    searchBar.animate().alpha(1);
                }
                if (appSearchBar != null) {
                    appSearchBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initDesktopEditViews() {
        removepage.setOnTouchListener(Tool.getBtnColorMaskController());
        setashomepage.setOnTouchListener(Tool.getBtnColorMaskController());
        addwidgetbtn.setOnTouchListener(Tool.getBtnColorMaskController());
        addLauncherAction.setOnTouchListener(Tool.getBtnColorMaskController());
        openSettings.setOnTouchListener(Tool.getBtnColorMaskController());
    }

    private void initDock() {
        int iconSize = LauncherSettings.getInstance(this).generalSettings.iconSize;
        desktopDock.init();
        if (LauncherSettings.getInstance(this).generalSettings.dockShowLabel) {
            desktopDock.getLayoutParams().height = Tool.dp2px(36 + iconSize + 14, this);
            if (LauncherSettings.getInstance(this).generalSettings.drawerMode == AppDrawerController.DrawerMode.Paged)
                desktopDock.setPadding(desktopDock.getPaddingLeft(), desktopDock.getPaddingTop(), desktopDock.getPaddingRight(), desktopDock.getPaddingBottom() + Tool.dp2px(9, this));
        } else
            desktopDock.getLayoutParams().height = Tool.dp2px(36 + iconSize, this);
    }

    @Override
    public void onDesktopEdit() {
        desktopEditOptionPanel.setVisibility(View.VISIBLE);

        dragOptionPanel.setAutoHideView(null);
        desktopIndicator.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
        desktopEditOptionPanel.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
        searchBar.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
        desktopDock.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());

        desktopEditOptionPanel.postDelayed(new Runnable() {
            @Override
            public void run() {
                desktopDock.setVisibility(View.INVISIBLE);
                if (LauncherSettings.getInstance(Home.this).generalSettings.desktopSearchBar) {
                    searchBar.setVisibility(View.VISIBLE);
                } else {
                    searchBar.setVisibility(View.GONE);
                }
            }
        }, 100);
    }

    @Override
    public void onFinishDesktopEdit() {
        dragOptionPanel.setAutoHideView(searchBar);
        desktopIndicator.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
        desktopEditOptionPanel.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
        searchBar.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
        desktopDock.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());

        desktopEditOptionPanel.postDelayed(new Runnable() {
            @Override
            public void run() {
                desktopEditOptionPanel.setVisibility(View.INVISIBLE);

                desktopDock.setVisibility(View.VISIBLE);
                if (LauncherSettings.getInstance(Home.this).generalSettings.desktopSearchBar) {
                    searchBar.setVisibility(View.VISIBLE);
                } else {
                    searchBar.setVisibility(View.GONE);
                }
            }
        }, 100);
    }

    private void updateDesktopClock() {
        if (searchBarClock != null) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            String date = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String date2 = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + ", " + String.valueOf(calendar.get(Calendar.YEAR));
            searchBarClock.setText(Html.fromHtml(date + "<br><small><small><small><small><small>" + date2 + "</small></small></small></small></small>"));
        }
    }

    @OnClick({R.id.removepage, R.id.setashomepage, R.id.addwidgetbtn, R.id.addLauncherAction, R.id.openSettings})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.removepage:
                desktop.removeCurrentPage();
                break;
            case R.id.setashomepage:
                LauncherSettings.getInstance(Home.this).generalSettings.desktopHomePage = desktop.getCurrentItem();
                break;
            case R.id.addwidgetbtn:
                pickWidget(view);
                break;
            case R.id.addLauncherAction:
                addLauncherAction(view);
                break;
            case R.id.openSettings:
                LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, this);
                break;
        }
    }

    private void addLauncherAction(View view) {
        PopupMenu pm = new PopupMenu(this, view);
        pm.inflate(R.menu.launcher_action);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        pm.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appDrawer:
                Point pos = desktop.getCurrentPage().findFreeSpace();
                desktop.addItemToPosition(Desktop.Item.newAppDrawerBtn(), pos.x, pos.y);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSettings() {
        LauncherSettings.GeneralSettings generalSettings = LauncherSettings.getInstance(Home.this).generalSettings;
        if (generalSettings.desktopSearchBar) {
            searchBar.setVisibility(View.VISIBLE);
        } else {
            searchBar.setVisibility(View.GONE);
        }

        if (generalSettings.fullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        desktopDock.setBackgroundColor(generalSettings.dockColor);

        appDrawerController.setBackgroundColor(generalSettings.drawerColor);
        appDrawerController.getBackground().setAlpha(0);
        appDrawerController.reloadDrawerCardTheme();

        switch (generalSettings.drawerMode) {
            case Paged:
                if (generalSettings.drawerShowIndicator)
                    appDrawerController.getChildAt(1).setVisibility(View.GONE);
                break;
            case Vertical:
                //This were handled at the AppDrawer_Vertical class
                break;
        }

        drawerLayout.setDrawerLockMode(generalSettings.minBarEnable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (!LauncherSettings.getInstance(this).generalSettings.showIndicator) {
            desktopIndicator.setViewPager(null);
        }
    }

    public void initMinBar() {
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<Integer> icons = new ArrayList<>();
        final ArrayList<String> minBarArrangement = LauncherSettings.getInstance(this).generalSettings.miniBarArrangement;

        if (minBarArrangement == null) {
            LauncherSettings.getInstance(this).generalSettings.miniBarArrangement = new ArrayList<>();
            for (LauncherAction.ActionItem item : LauncherAction.actionItems) {
                LauncherSettings.getInstance(this).generalSettings.miniBarArrangement.add("0" + item.label.toString());
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
                LauncherSettings.getInstance(this).generalSettings.miniBarArrangement = new ArrayList<>();
                for (LauncherAction.ActionItem item : LauncherAction.actionItems) {
                    LauncherSettings.getInstance(this).generalSettings.miniBarArrangement.add("0" + item.label.toString());
                    labels.add(item.label.toString());
                    icons.add(item.icon);
                }
            }
        }


        minBar.setPadding(0, Tool.getStatusBarHeight(this), 0, Tool.getNavBarHeight(this));
        minBar.setAdapter(new IconListAdapter(this, labels, icons));
        minBar.setOnSwipeRight(new SwipeListView.OnSwipeRight() {
            @Override
            public void onSwipe(int pos, float x, float y) {
                miniPopup.showActionWindow(LauncherAction.Action.valueOf(labels.get(pos)), x, y + (shortcutLayout.getHeight() - minBar.getHeight()) / 2 - Tool.getNavBarHeight(Home.this));
            }
        });
        minBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LauncherAction.Action action = LauncherAction.Action.valueOf(labels.get(i));
                LauncherAction.RunAction(action, Home.this);
                if (action == LauncherAction.Action.EditMinBar || action == LauncherAction.Action.LauncherSettings || action == LauncherAction.Action.DeviceSettings) {
//                    (new Handler()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            drawerLayout.closeDrawers();
//                        }
//                    }, 500);
                } else {
                    drawerLayout.closeDrawers();
                }
            }
        });
    }

    private void initQuickCenter() {
        ////////////////////////////////////Quick Contact///////////////////////////////////////////
        RecyclerView quickContact = (RecyclerView) findViewById(R.id.quickContactRv);
        quickContact.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        quickContactFA = new FastItemAdapter<>();
        quickContact.setAdapter(quickContactFA);

        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            callLogObserver = new CallLogObserver(new Handler());
            getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);
            //First call get the call history for the adapter
            callLogObserver.onChange(true);
        } else {
            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_PERMISSION_READ_CALL_LOG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_READ_CALL_LOG && callLogObserver != null) {
            //Read call log permitted
            callLogObserver = new CallLogObserver(new Handler());
            getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);
            //First call get the call history for the adapter
            callLogObserver.onChange(true);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void registerBroadcastReceiver() {
        registerReceiver(appUpdateReceiver, appUpdateIntentFilter);
        registerReceiver(timeChangedReceiver, timeChangesIntentFilter);
        registerReceiver(shortcutReceiver, shortcutIntentFilter);
    }
    //endregion

    //region WIDGET
    public void pickWidget(View view) {
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
        Desktop.Item item = Desktop.Item.newWidgetItem(appWidgetId);
        item.spanX = 4;
        item.spanY = 1;
        //Add the item to settings
        item.x = 0;
        item.y = 0;
        if (LauncherSettings.getInstance(this).desktopData.size() < desktop.getCurrentItem() + 1)
            LauncherSettings.getInstance(this).desktopData.add(desktop.getCurrentItem(), new ArrayList<Desktop.Item>());
        LauncherSettings.getInstance(this).desktopData.get(desktop.getCurrentItem()).add(item);
        //end
        desktop.addItemToPagePosition(item, desktop.getCurrentItem());
    }
    //endregion

    //region ACTIVITYLIFECYCLE
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
            int appWidgetId =
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    @Override
    protected void onStart() {
        if (appWidgetHost != null)
            appWidgetHost.startListening();
        super.onStart();
    }

    @Override
    protected void onStop() {
        // save the settings when the launcher is stopped
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
        if (appWidgetHost != null)
            appWidgetHost.startListening();
        handleLauncherPause();
        super.onResume();
    }

    private void handleLauncherPause() {
        if (desktop != null)
            if (!desktop.inEditMode) {
                if (appDrawerController.getDrawer() != null && appDrawerController.getDrawer().getVisibility() == View.VISIBLE)
                    closeAppDrawer();
                else if (LauncherSettings.getInstance(Home.this).generalSettings != null && !groupPopup.isShowing)
                    desktop.setCurrentItem(LauncherSettings.getInstance(Home.this).generalSettings.desktopHomePage);
            } else {
                desktop.pages.get(desktop.getCurrentItem()).performClick();
            }

        if (groupPopup != null) {
            groupPopup.dismissPopup();
        }
    }
    //endregion

    /**
     * Call this to open the app drawer with animation
     */
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

    /**
     * When the search button of the search bar clicked
     * However the search bar is removed out for now
     */
    public void onSearch(View view) {

        Intent i;
        try {
            i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchActivity");
            Home.this.startActivity(i);
        } catch (Exception e) {
            i = new Intent(Intent.ACTION_WEB_SEARCH);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //i.putExtra(SearchManager.QUERY,"");
        }
        Home.this.startActivity(i);
    }

    /**
     * When the voice button of the search bar clicked
     * However the search bar is removed out for now
     */
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
