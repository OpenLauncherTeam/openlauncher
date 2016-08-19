package com.bennyv4.project2;

import android.animation.*;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.v7.app.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bennyv4.project2.util.AppManager;
import com.bennyv4.project2.util.AppUpdateReceiver;
import com.bennyv4.project2.util.LauncherSettings;
import com.bennyv4.project2.util.Tools;
import com.bennyv4.project2.widget.*;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.viewpagerindicator.CirclePageIndicator;

import net.steamcrafted.materialiconlib.*;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity implements MaterialSearchBar.OnSearchActionListener{

    RelativeLayout baseLayout;
    AppDrawer appDrawer;
    public static Desktop desktop;
    public static Dock dock;
    FrameLayout appDrawerBtn;
    CirclePageIndicator appDrawerIndicator, desktopIndicator;
    Animator appDrawerAnimator;
    MaterialSearchBar searchBar;
    DragOptionPanel dragOptionPanel;
    BroadcastReceiver appUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LauncherSettings.getInstance(this);
        setContentView(R.layout.activity_home);
        findViews();
        initViews();
        registerAppUpdateReceiver();
    }

    private void findViews() {
        baseLayout = (RelativeLayout) findViewById(R.id.baseLayout);
        appDrawer = (AppDrawer) findViewById(R.id.appDrawer);
        desktop = (Desktop) findViewById(R.id.desktop);
        dock = (Dock) findViewById(R.id.desktopDock);
        appDrawerIndicator = (CirclePageIndicator) findViewById(R.id.appDrawerIndicator);
        desktopIndicator = (CirclePageIndicator) findViewById(R.id.desktopIndicator);
        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        appDrawerBtn = (FrameLayout) getLayoutInflater().inflate(R.layout.item_appdrawerbtn, null);
        dragOptionPanel = (DragOptionPanel) findViewById(R.id.dragOptionPanel);
    }

    private void initViews() {
        appDrawer.withHome(this, appDrawerIndicator);
        desktopIndicator.setViewPager(desktop);

        Drawable appDrawerBtnIcon = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.APPS)
                .setColor(Color.DKGRAY)
                .setSizeDp(25)
                .build();

        ImageView appDrawerIcon = (ImageView) appDrawerBtn.findViewById(R.id.iv);
        appDrawerIcon.setImageDrawable(appDrawerBtnIcon);
        appDrawerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View p1) {
                openAppDrawer();
            }
        });
        dock.addViewToGrid(appDrawerBtn, 2, 0);

        List<String> history;
        if ((history = LauncherSettings.getInstance(this).generalSettings.searchHistory) != null)
            searchBar.setLastSuggestions(history);
        searchBar.setOnSearchActionListener(this);

        dragOptionPanel.setAutoHideView(searchBar);

        AppManager.getInstance(this).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            boolean fired = false;
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                if (fired)return;
                fired = true;
                initDesktopItem();
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

    private void initDesktopItem(){
        for (Desktop.Item item : LauncherSettings.getInstance(this).dockData) {
            dock.addAppToPosition(item);
        }
        for (int i = 0 ; i < LauncherSettings.getInstance(this).desktopData.size() ; i++){
            for (int j = 0 ; j < LauncherSettings.getInstance(this).desktopData.get(i).size() ; j++){
                desktop.addAppToPagePosition(LauncherSettings.getInstance(this).desktopData.get(i).get(j),i);
            }
        }
    }

    //region ACTIVITYLIFECYCLE
    @Override
    protected void onDestroy() {
        unregisterReceiver(appUpdateReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        LauncherSettings.getInstance(this).generalSettings.searchHistory = (ArrayList<String>) searchBar.getLastSuggestions();
        LauncherSettings.getInstance(this).writeSettings();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (appDrawer.getVisibility() == View.VISIBLE)
            closeAppDrawer();
    }

    @Override
    protected void onResume() {
        if (appDrawer.getVisibility() == View.VISIBLE)
            closeAppDrawer();
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
        searchBar.animate().alpha(0).setDuration(80);
        desktop.animate().alpha(0).setDuration(100);
        appDrawerBtn.animate().scaleX(0).scaleY(0).setDuration(100);

        appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                appDrawer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                appDrawerIndicator.setVisibility(View.VISIBLE);
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
                appDrawerIndicator.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                appDrawer.setVisibility(View.INVISIBLE);
                appDrawerBtn.setVisibility(View.VISIBLE);
                dock.animate().alpha(1);
                desktop.animate().alpha(1);
                if (!dragOptionPanel.dragging)
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

    //region SEARCHBARACTION
    @Override
    public void onSearchStateChanged(boolean b) {

    }

    @Override
    public void onSearchConfirmed(CharSequence charSequence) {
        Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        i.putExtra(SearchManager.QUERY, charSequence.toString());
        Home.this.startActivity(i);
        searchBar.disableSearch();
        searchBar.disableSearch();
    }

    @Override
    public void onSpeechIconSelected() {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
            Home.this.startActivity(i);
        }
        catch (Exception e) {
            Tools.toast(Home.this, "Can not find google search app");
        }
    }
    //endregion
}
