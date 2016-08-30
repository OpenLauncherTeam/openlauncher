package com.bennyv4.project2;

import android.animation.*;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.v7.widget.CardView;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bennyv4.project2.activity.MinBarEditActivity;
import com.bennyv4.project2.util.AppManager;
import com.bennyv4.project2.util.AppUpdateReceiver;
import com.bennyv4.project2.util.DragNavigationControl;
import com.bennyv4.project2.util.IconListAdapter;
import com.bennyv4.project2.util.LauncherAction;
import com.bennyv4.project2.util.WidgetHost;
import com.bennyv4.project2.widget.DragOptionView;
import com.bennyv4.project2.util.LauncherSettings;
import com.bennyv4.project2.util.Tools;
import com.bennyv4.project2.widget.*;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

public class Home extends Activity {

    RelativeLayout baseLayout;
    AppDrawer appDrawer;
    public static Desktop desktop;
    public static Dock dock;
    FrameLayout appDrawerBtn;
    PagerIndicator desktopIndicator, appDrawerIndicator;
    Animator appDrawerAnimator;
    CardView searchBar;
    DragOptionView dragOptionView;
    LinearLayout desktopEditOptionView;
    Button addWidgetBtn;
    BroadcastReceiver appUpdateReceiver;
    ListView minBar;
    public static GroupPopupView groupPopup;

    public static WidgetHost appWidgetHost;
    public static AppWidgetManager appWidgetManager;

    public static int REQUEST_PICK_APPWIDGET = 0x6475;
    public static int REQUEST_CREATE_APPWIDGET = 0x3648;

    public static int touchX = 0, touchY = 0;

    public static final int MINBAREDIT = 0x2873;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme();
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }

        AppManager.getInstance(this).clearListener();
        LauncherSettings.getInstance(this);

        View myScreen = getLayoutInflater().inflate(R.layout.activity_home, null);
        setContentView(myScreen);

        appWidgetHost = new WidgetHost(getApplicationContext(), R.id.m_AppWidgetHost);
        appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetHost.startListening();

        findViews();
        initViews();
        registerAppUpdateReceiver();

        AppManager.getInstance(this).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                desktop.initDesktopItem();
                dock.initDockItem();
                dock.addViewToGrid(appDrawerBtn, 2, 0, 1, 1);
                AppManager.getInstance(Home.this).removeAppUpdatedListener(this);
            }
        });
        AppManager.getInstance(this).init();
        System.gc();

    }

    public void pickWidget(View view) {
        int appWidgetId = appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }else if (requestCode == MINBAREDIT){
                initMinBar();
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId =
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
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

    private void findViews() {
        baseLayout = (RelativeLayout) findViewById(R.id.baseLayout);
        appDrawer = (AppDrawer) findViewById(R.id.appDrawer);
        desktop = (Desktop) findViewById(R.id.desktop);
        addWidgetBtn = (Button) findViewById(R.id.addwidgetbtn);
        dock = (Dock) findViewById(R.id.desktopDock);
        appDrawerIndicator = (PagerIndicator) findViewById(R.id.appDrawerIndicator);
        desktopIndicator = (PagerIndicator) findViewById(R.id.desktopIndicator);
        searchBar = (CardView) findViewById(R.id.searchBar);
        appDrawerBtn = (FrameLayout) getLayoutInflater().inflate(R.layout.item_appdrawerbtn, null);
        desktopEditOptionView = (LinearLayout) findViewById(R.id.desktopeditoptionpanel);
        dragOptionView = (DragOptionView) findViewById(R.id.dragOptionPanel);
        groupPopup = (GroupPopupView) findViewById(R.id.groupPopup);
        minBar = (ListView) findViewById(R.id.minbar);
    }

    private void initViews() {
        initMinBar();

        DragNavigationControl dragNavigationControl = new DragNavigationControl(findViewById(R.id.left), findViewById(R.id.right));

        appDrawer.withHome(this, appDrawerIndicator);

        desktop.listener = new Desktop.OnDesktopEditListener() {
            @Override
            public void onStart() {
                addWidgetBtn.setVisibility(View.VISIBLE);
                desktopEditOptionView.setVisibility(View.VISIBLE);

                addWidgetBtn.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                dragOptionView.setAutoHideView(null);
                desktopIndicator.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                desktopEditOptionView.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                searchBar.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                dock.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());

                addWidgetBtn.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dock.setVisibility(View.INVISIBLE);
                        searchBar.setVisibility(View.INVISIBLE);
                    }
                }, 100);
            }

            @Override
            public void onFinished() {
                addWidgetBtn.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                dragOptionView.setAutoHideView(searchBar);
                desktopIndicator.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                desktopEditOptionView.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                searchBar.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                dock.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());

                addWidgetBtn.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addWidgetBtn.setVisibility(View.INVISIBLE);
                        desktopEditOptionView.setVisibility(View.INVISIBLE);

                        dock.setVisibility(View.VISIBLE);
                        searchBar.setVisibility(View.VISIBLE);
                    }
                }, 100);
            }
        };

        desktopEditOptionView.findViewById(R.id.removepage).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                desktop.removeCurrentPage();
            }
        });
        desktopEditOptionView.findViewById(R.id.setashomepage).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LauncherSettings.getInstance(Home.this).generalSettings.desktopHomePage = desktop.getCurrentItem();
            }
        });

        desktopIndicator.setViewPager(desktop);

        int iconSize = LauncherSettings.getInstance(this).generalSettings.iconSize;
        Drawable appDrawerBtnIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.APPS)
                .setColor(Color.DKGRAY)
                .setSizeDp(iconSize / 2 - 8)
                .build();
        CardView appDrawerBtnCard = (CardView) appDrawerBtn.findViewById(R.id.card);
        appDrawerBtnCard.setRadius((Tools.convertDpToPixel((iconSize - 8) / 2, this)));
        appDrawerBtnCard.getLayoutParams().width = Tools.convertDpToPixel(iconSize - 8, this);
        appDrawerBtnCard.getLayoutParams().height = Tools.convertDpToPixel(iconSize - 8, this);

        ImageView appDrawerIcon = (ImageView) appDrawerBtn.findViewById(R.id.iv);
        appDrawerIcon.setImageDrawable(appDrawerBtnIcon);
        appDrawerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                openAppDrawer();
            }
        });
        dock.getLayoutParams().height = Tools.convertDpToPixel(22 + iconSize, this);

        dragOptionView.setAutoHideView(searchBar);
    }

    public void initMinBar() {
        final ArrayList<String> labels = new ArrayList<>();
        labels.add("Edit");

        ArrayList<Integer> icons = new ArrayList<>();
        icons.add(R.drawable.ic_mode_edit_black_24dp);

        final ArrayList<String> minBarArrangement = LauncherSettings.getInstance(this).generalSettings.minBarArrangement;
        if (minBarArrangement == null) {
            LauncherSettings.getInstance(this).generalSettings.minBarArrangement = new ArrayList<>();
            for(LauncherAction.ActionItem item : LauncherAction.actionItems){
                LauncherSettings.getInstance(this).generalSettings.minBarArrangement.add(item.label.toString());
                labels.add(item.label.toString());
                icons.add(item.icon);
            }
        } else{
            if (minBarArrangement.size() == LauncherAction.actionItems.length){
                for (String act : minBarArrangement) {
                    LauncherAction.ActionItem item = LauncherAction.getActionItemFromString(act);
                    labels.add(item.label.toString());
                    icons.add(item.icon);
                }
            }else {
                LauncherSettings.getInstance(this).generalSettings.minBarArrangement = new ArrayList<>();
                for(LauncherAction.ActionItem item : LauncherAction.actionItems){
                    LauncherSettings.getInstance(this).generalSettings.minBarArrangement.add(item.label.toString());
                    labels.add(item.label.toString());
                    icons.add(item.icon);
                }
            }
        }


        minBar.setAdapter(new IconListAdapter(this, labels, icons));
        minBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0)
                    startActivityForResult(new Intent(Home.this, MinBarEditActivity.class),MINBAREDIT);
                else
                    LauncherAction.RunAction(LauncherAction.Action.valueOf(labels.get(i)),Home.this);
            }
        });
    }

    private void registerAppUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        appUpdateReceiver = new AppUpdateReceiver();
        registerReceiver(appUpdateReceiver, filter);
    }

    //region ACTIVITYLIFECYCLE
    @Override
    protected void onDestroy() {
        appWidgetHost.stopListening();
        appWidgetHost = null;
        unregisterReceiver(appUpdateReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        appWidgetHost.startListening();
        super.onStart();
    }

    @Override
    protected void onPause() {
        LauncherSettings.getInstance(this).writeSettings();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (!desktop.inEditMode) {
            desktop.setCurrentItem(LauncherSettings.getInstance(Home.this).generalSettings.desktopHomePage);
            if (appDrawer.getVisibility() == View.VISIBLE)
                closeAppDrawer();
        } else {
            desktop.pages.get(desktop.getCurrentItem()).performClick();
        }
    }

    @Override
    protected void onResume() {
        appWidgetHost.startListening();
        if (!desktop.inEditMode) {
            desktop.setCurrentItem(LauncherSettings.getInstance(Home.this).generalSettings.desktopHomePage);
            if (appDrawer.getVisibility() == View.VISIBLE)
                closeAppDrawer();
        } else {
            desktop.pages.get(desktop.getCurrentItem()).performClick();
        }

        super.onResume();
    }
    //endregion

    //region APPDRAWERANIMATION
    public void openAppDrawer() {
        int cx = (dock.getLeft() + dock.getRight()) / 2;
        int cy = (dock.getTop() + dock.getBottom()) / 2;

        int finalRadius = Math.max(appDrawer.getWidth(), appDrawer.getHeight());

        appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(appDrawer, cx, cy, 0, finalRadius);
        appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        appDrawerAnimator.setDuration(200);
        appDrawerAnimator.setStartDelay(100);

        dock.animate().alpha(0).setDuration(100);
        desktopIndicator.animate().alpha(0).setDuration(100);
        searchBar.animate().alpha(0).setDuration(80);
        desktop.animate().alpha(0).setDuration(100);
        appDrawerBtn.animate().scaleX(0).scaleY(0).setDuration(100);

        appDrawerIndicator.animate().alpha(1).setDuration(100);

        appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                appDrawer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                searchBar.setVisibility(View.INVISIBLE);
                appDrawerBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator p1) {
            }

            @Override
            public void onAnimationRepeat(Animator p1) {
            }
        });
        appDrawerAnimator.start();
    }

    public void closeAppDrawer() {
        if (appDrawerAnimator == null || appDrawerAnimator.isRunning())
            return;

        int cx = (dock.getLeft() + dock.getRight()) / 2;
        int cy = (dock.getTop() + dock.getBottom()) / 2;

        int finalRadius = Math.max(appDrawer.getWidth(), appDrawer.getHeight());

        appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(appDrawer, cx, cy, finalRadius, 0);
        appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        appDrawerAnimator.setDuration(200);
        appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                searchBar.setVisibility(View.VISIBLE);
                appDrawerIndicator.animate().alpha(0);
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                desktopIndicator.animate().alpha(1);
                appDrawer.setVisibility(View.INVISIBLE);
                appDrawerBtn.setVisibility(View.VISIBLE);
                dock.animate().alpha(1);
                desktop.animate().alpha(1);
                if (!dragOptionView.dragging)
                    searchBar.animate().alpha(1);
                appDrawerBtn.animate().scaleX(1).scaleY(1);
            }

            @Override
            public void onAnimationCancel(Animator p1) {
            }

            @Override
            public void onAnimationRepeat(Animator p1) {
            }
        });
        appDrawerAnimator.start();
    }

    //endregion

    public void onSearch(View view) {
        Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //i.putExtra(SearchManager.QUERY,"");
        Home.this.startActivity(i);
    }

    public void onVoiceSearch(View view) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
            Home.this.startActivity(i);
        } catch (Exception e) {
            Tools.toast(Home.this, "Can not find google search app");
        }
    }
}
