package com.benny.openlauncher.activity;

import android.animation.Animator;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppUpdateReceiver;
import com.benny.openlauncher.util.DragNavigationControl;
import com.benny.openlauncher.util.IconListAdapter;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.QuickCenterItem;
import com.benny.openlauncher.util.ShortcutReceiver;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.util.WidgetHost;
import com.benny.openlauncher.widget.AppDrawer;
import com.benny.openlauncher.widget.PagedAppDrawer;
import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.Dock;
import com.benny.openlauncher.widget.DragOptionView;
import com.benny.openlauncher.widget.GroupPopupView;
import com.benny.openlauncher.widget.PagerIndicator;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.HeaderAdapter;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Home extends Activity {

    //static members, easier to access from any activity and class.
    @Nullable
    public static Home launcher;
    @Nullable
    public static Desktop desktop;
    @Nullable
    public static Dock dock;
    @Nullable
    public static View searchBar;
    @Nullable
    public static GroupPopupView groupPopup;

    //QuickCenter
    @Nullable
    public FastItemAdapter<QuickCenterItem.NoteItem> noteAdapter;
    @Nullable
    public ArrayList<QuickCenterItem.NoteContent> notes = new ArrayList<>();

    @Nullable
    public static WidgetHost appWidgetHost;
    @Nullable
    public static AppWidgetManager appWidgetManager;

    //This two integer is used for the drag shadow builder to get the touch point os users' finger.
    public static int touchX = 0, touchY = 0;

    //normal members, currently not necessary to access from elsewhere.
    private ConstraintLayout baseLayout;
    private AppDrawer appDrawerOtter;
    private View appDrawer;
    private FrameLayout appDrawerBtn;
    private PagerIndicator desktopIndicator, appDrawerIndicator;
    private Animator appDrawerAnimator;
    private DragOptionView dragOptionView;
    private ViewGroup desktopEditOptionView;
    private RecyclerView quickCenter;
    private BroadcastReceiver appUpdateReceiver , shortcutReceiver;
    private TextView searchbarclock;
    private ListView minBar;
    private DrawerLayout myScreen;

    public static final int REQUEST_PICK_APPWIDGET = 0x6475;
    public static final int REQUEST_CREATE_APPWIDGET = 0x3648;

    public static final int MINBAREDIT = 0x2873;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Tool.setHomeTheme(this);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        launcher = this;
        AppManager.getInstance(this).clearListener();
        LauncherSettings.getInstance(this);

        myScreen = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_home, null);
        myScreen.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        setContentView(myScreen);

        appWidgetHost = new WidgetHost(getApplicationContext(), R.id.m_AppWidgetHost);
        appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetHost.startListening();

        findViews();
        initViews();
        registerAppUpdateReceiver();
        registerShortcutReceiver();

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

        initSettings();

        System.runFinalization();
        System.gc();
    }

    //region INIT
    private void findViews() {
        searchbarclock = (TextView) findViewById(R.id.searchbarclock);
        quickCenter = (RecyclerView) findViewById(R.id.rv);
        baseLayout = (ConstraintLayout) findViewById(R.id.baseLayout);
        appDrawerOtter = (AppDrawer) findViewById(R.id.appDrawerOtter);
        appDrawer = appDrawerOtter.getChildAt(0);
        desktop = (Desktop) findViewById(R.id.desktop);
        dock = (Dock) findViewById(R.id.desktopDock);
        appDrawerIndicator = (PagerIndicator) findViewById(R.id.appDrawerIndicator);
        desktopIndicator = (PagerIndicator) findViewById(R.id.desktopIndicator);
        searchBar = findViewById(R.id.searchBar);
        appDrawerBtn = (FrameLayout) getLayoutInflater().inflate(R.layout.item_appdrawerbtn, null);
        desktopEditOptionView = (ViewGroup) findViewById(R.id.desktopeditoptionpanel);
        dragOptionView = (DragOptionView) findViewById(R.id.dragOptionPanel);
        groupPopup = (GroupPopupView) findViewById(R.id.groupPopup);

        minBar = (ListView) findViewById(R.id.minbar);
    }

    private void initViews() {
        initMinBar();
        initQuickCenter();

        DragNavigationControl dragNavigationControl = new DragNavigationControl(findViewById(R.id.left),findViewById(R.id.right));

        String date = Calendar.getInstance(Locale.getDefault()).getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault()) + " " +
                String.valueOf(Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_MONTH));
        String date2 = Calendar.getInstance(Locale.getDefault()).getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault()) + ", " +
                String.valueOf(Calendar.getInstance(Locale.getDefault()).get(Calendar.YEAR));
        searchbarclock.setText(Html.fromHtml(date+"<br><small><small><small><small><small>"+date2+"</small></small></small></small></small>"));
        searchbarclock.postDelayed(new Runnable() {
            @Override
            public void run() {
                String date = Calendar.getInstance(Locale.getDefault()).getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault()) + " " +
                        String.valueOf(Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_MONTH));
                String date2 = Calendar.getInstance(Locale.getDefault()).getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault()) + ", " +
                        String.valueOf(Calendar.getInstance(Locale.getDefault()).get(Calendar.YEAR));
                searchbarclock.setText(Html.fromHtml(date+"<br><small><small><small><small><small>"+date2+"</small></small></small></small></small>"));
                searchbarclock.postDelayed(this,60000);
            }
        },60000);

        appDrawerOtter.withHome(this);

        desktop.listener = new Desktop.OnDesktopEditListener() {
            @Override
            public void onStart() {
                desktopEditOptionView.setVisibility(View.VISIBLE);

                dragOptionView.setAutoHideView(null);
                desktopIndicator.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                desktopEditOptionView.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                searchBar.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                dock.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());

                desktopEditOptionView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dock.setVisibility(View.INVISIBLE);
                        if (LauncherSettings.getInstance(Home.this).generalSettings.showsearchbar)
                            searchBar.setVisibility(View.INVISIBLE);
                    }
                }, 100);
            }

            @Override
            public void onFinished() {
                dragOptionView.setAutoHideView(searchBar);
                desktopIndicator.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                desktopEditOptionView.animate().alpha(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                searchBar.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                dock.animate().alpha(1).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());

                desktopEditOptionView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        desktopEditOptionView.setVisibility(View.INVISIBLE);

                        dock.setVisibility(View.VISIBLE);
                        if (LauncherSettings.getInstance(Home.this).generalSettings.showsearchbar)
                            searchBar.setVisibility(View.VISIBLE);
                    }
                }, 100);
            }
        };

        View btn1 = desktopEditOptionView.findViewById(R.id.removepage);
        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                desktop.removeCurrentPage();
            }
        });
        btn1.setOnTouchListener(Tool.getBtnColorMaskController());
        View btn2 = desktopEditOptionView.findViewById(R.id.setashomepage);
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LauncherSettings.getInstance(Home.this).generalSettings.desktopHomePage = desktop.getCurrentItem();
            }
        });
        btn2.setOnTouchListener(Tool.getBtnColorMaskController());
        View btn3 = desktopEditOptionView.findViewById(R.id.addwidgetbtn);
        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                pickWidget(view);
            }
        });
        btn3.setOnTouchListener(Tool.getBtnColorMaskController());

        desktopIndicator.setViewPager(desktop);
        desktop.setPageIndicator(desktopIndicator);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);

        int iconSize = LauncherSettings.getInstance(this).generalSettings.iconSize;
        Drawable appDrawerBtnIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.APPS)
                .setColor(typedValue.data)
                .setSizeDp(iconSize / 2 - 8)
                .build();

        View appDrawerBtnCard = appDrawerBtn.findViewById(R.id.card);
        appDrawerBtnCard.getLayoutParams().width = Tool.convertDpToPixel(iconSize - 8, this);
        appDrawerBtnCard.getLayoutParams().height = Tool.convertDpToPixel(iconSize - 8, this);

        ImageView appDrawerIcon = (ImageView) appDrawerBtn.findViewById(R.id.iv);
        appDrawerIcon.setImageDrawable(appDrawerBtnIcon);

        appDrawerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                openAppDrawer();
            }
        });
        dock.getLayoutParams().height = Tool.convertDpToPixel(22 + iconSize, this);

        dragOptionView.setAutoHideView(searchBar);
    }

    private void initSettings() {
        if (!LauncherSettings.getInstance(this).generalSettings.showsearchbar){
            searchBar.setVisibility(View.GONE);
        }
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
                LauncherSettings.getInstance(this).generalSettings.minBarArrangement.add("0"+item.label.toString());
                labels.add(item.label.toString());
                icons.add(item.icon);
            }
        } else{
            if (minBarArrangement.size() == LauncherAction.actionItems.length){
                for (String act : minBarArrangement) {
                    if (act.charAt(0) == '0') {
                        LauncherAction.ActionItem item = LauncherAction.getActionItemFromString(act.substring(1));
                        labels.add(item.label.toString());
                        icons.add(item.icon);
                    }
                }
            }else {
                LauncherSettings.getInstance(this).generalSettings.minBarArrangement = new ArrayList<>();
                for(LauncherAction.ActionItem item : LauncherAction.actionItems){
                    LauncherSettings.getInstance(this).generalSettings.minBarArrangement.add("0"+item.label.toString());
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
                else{
                    LauncherAction.Action action = LauncherAction.Action.valueOf(labels.get(i));
                    LauncherAction.RunAction(action,Home.this,Home.this);
                    if (action != LauncherAction.Action.LauncherSettings)
                        myScreen.closeDrawers();
                }
            }
        });
    }

    private void initQuickCenter() {
        quickCenter.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        HeaderAdapter<QuickCenterItem.SearchHeader> header = new HeaderAdapter<>();
        noteAdapter = new FastItemAdapter<>();

        quickCenter.setAdapter(header.wrap(noteAdapter));

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Gson gson = new Gson();
                notes.clear();
                try {
                    JsonReader reader = new JsonReader(new InputStreamReader(Home.this.openFileInput("noteData.json")));
                    reader.beginArray();
                    while (reader.hasNext()) {
                        QuickCenterItem.NoteContent content = gson.fromJson(reader,QuickCenterItem.NoteContent.class);
                        notes.add(content);
                    }
                    reader.endArray();
                    reader.close();
                } catch (IOException e) {e.printStackTrace();}
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                for (int i = 0; i < notes.size(); i++) {
                    noteAdapter.add(new QuickCenterItem.NoteItem(notes.get(i).date,notes.get(i).content,noteAdapter));
                }
                super.onPostExecute(aVoid);
            }
        }.execute();
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

    private void registerShortcutReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutReceiver = new ShortcutReceiver();
        registerReceiver(shortcutReceiver, filter);
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

    @Override
    protected void onStart() {
        if (appWidgetHost != null)
        appWidgetHost.startListening();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        Gson gson = LauncherSettings.getInstance(this).writeSettings();
        Tool.writeToFile("noteData.json",gson.toJson(notes),Home.this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        myScreen.closeDrawers();
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
        if (appWidgetHost != null)
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
        int margin = ((FrameLayout.LayoutParams)appDrawer.getLayoutParams()).leftMargin;// + ((FrameLayout.LayoutParams)appDrawer.getLayoutParams()).rightMargin;
        cx -= margin;
        int marginh = ((FrameLayout.LayoutParams)appDrawer.getLayoutParams()).topMargin;// + ((FrameLayout.LayoutParams)appDrawer.getLayoutParams()).rightMargin;
        cy -= marginh;

        int finalRadius = Math.max(appDrawer.getWidth(), appDrawer.getHeight());

        appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(appDrawer, cx, cy, 0, finalRadius);
        appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        appDrawerAnimator.setDuration(180);
        appDrawerAnimator.setStartDelay(100);

        dock.animate().alpha(0).setDuration(100);
        desktopIndicator.animate().alpha(0).setDuration(100);
        searchBar.animate().alpha(0).setDuration(80);
        desktop.animate().alpha(0).setDuration(100);
        appDrawerBtn.animate().scaleX(0).scaleY(0).setDuration(100);

        if (appDrawerIndicator != null)
        appDrawerIndicator.animate().alpha(1).setDuration(100);

        appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                appDrawer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                if (LauncherSettings.getInstance(Home.this).generalSettings.showsearchbar)
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
        appDrawer.setPivotX(cx);
        appDrawer.setPivotY(cy);
        appDrawerAnimator.start();
        appDrawer.setScaleX(0.95f);
        appDrawer.setScaleY(0.95f);
        appDrawer.animate().setStartDelay(100).scaleX(1).scaleY(1).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100L);
    }

    public void closeAppDrawer() {
        if (appDrawerAnimator == null || appDrawerAnimator.isRunning())
            return;

        int cx = (dock.getLeft() + dock.getRight()) / 2;
        int cy = (dock.getTop() + dock.getBottom()) / 2;
        int margin = ((FrameLayout.LayoutParams)appDrawer.getLayoutParams()).leftMargin;// + ((FrameLayout.LayoutParams)appDrawer.getLayoutParams()).rightMargin;
        cx -= margin;
        int marginh = ((FrameLayout.LayoutParams)appDrawer.getLayoutParams()).topMargin;// + ((FrameLayout.LayoutParams)appDrawer.getLayoutParams()).rightMargin;
        cy -= marginh;

        int finalRadius = Math.max(appDrawer.getWidth(), appDrawer.getHeight());

        appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(appDrawer, cx, cy, finalRadius, 0);
        appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        appDrawerAnimator.setDuration(180);
        appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                if (LauncherSettings.getInstance(Home.this).generalSettings.showsearchbar)
                    searchBar.setVisibility(View.VISIBLE);
                if (appDrawerIndicator != null)
                appDrawerIndicator.animate().alpha(0);
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                if (LauncherSettings.getInstance(Home.this).generalSettings.rememberappdrawerpage)
                    appDrawerOtter.scrollToStart();
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
        appDrawer.animate().setStartDelay(0).scaleX(0.95f).scaleY(0.95f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100L);
    }

    //endregion


    //region VIEWONCLICK
    public void onAddNote(View view){
        Tool.askForText("Note", null, this, new Tool.OnTextGotListener() {
            @Override
            public void hereIsTheText(String str) {
                if (str.isEmpty())return;
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String time = df.format(Calendar.getInstance().getTime());

                notes.add(new QuickCenterItem.NoteContent(time,str));
                noteAdapter.add(new QuickCenterItem.NoteItem(time,str,noteAdapter));
            }
        });
    }

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

    public void onVoiceSearch(View view) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
            Home.this.startActivity(i);
        } catch (Exception e) {
            Tool.toast(Home.this, "Can not find google search app");
        }
    }
    //endregion
}
