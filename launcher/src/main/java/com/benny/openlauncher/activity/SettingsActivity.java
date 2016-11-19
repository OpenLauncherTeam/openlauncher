package com.benny.openlauncher.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.AppDrawer;
import com.benny.openlauncher.widget.Desktop;
import com.bennyv5.materialpreffragment.MaterialPrefFragment;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.LauncherSettings;

public class SettingsActivity extends AppCompatActivity implements MaterialPrefFragment.OnPrefClickedListener, MaterialPrefFragment.OnPrefChangedListener {

    private boolean requireLauncherRestart = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Tool.setTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.tb));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) {
            Fragment fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(getResources().getColor(R.color.Light_TextColor), getResources().getColor(R.color.Light_TextColorSec), getResources().getColor(R.color.Light_Background), getResources().getColor(R.color.colorAccent), false)
                    .add(new MaterialPrefFragment.GroupTitle("Desktop"))
                    .add(new MaterialPrefFragment.ButtonPref("desktopMode", "Style", "choose different style of your desktop"))
                    .add(new MaterialPrefFragment.TBPref("desktopSearchBar", "Show search bar", "Display a search bar always on top of the desktop", LauncherSettings.getInstance(this).generalSettings.desktopSearchBar))
                    .add(new MaterialPrefFragment.GroupTitle("Dock"))
                    .add(new MaterialPrefFragment.TBPref("dockShowLabel","Show app label","show the app's name in the dock",LauncherSettings.getInstance(this).generalSettings.dockShowLabel))
                    .add(new MaterialPrefFragment.GroupTitle("AppDrawer"))
                    .add(new MaterialPrefFragment.ButtonPref("drawerstyle", "Style", "choose the style of the app drawer"))
                    .add(new MaterialPrefFragment.TBPref("appdrawersearchbar", "Search Bar", "search bar will only appear in grid drawer", LauncherSettings.getInstance(this).generalSettings.drawerSearchBar))
                    .add(new MaterialPrefFragment.NUMPref("horigridsize", "Grid column", "App drawer grid size", LauncherSettings.getInstance(this).generalSettings.drawerGridX, 1, 10))
                    .add(new MaterialPrefFragment.NUMPref("vertigridsize", "Grid row", "App drawer grid size", LauncherSettings.getInstance(this).generalSettings.drawerGridY, 1, 10))
                    .add(new MaterialPrefFragment.TBPref("drawerRememberPage", "Remember last page", "The page will not reset to the first page when reopen app drawer", !LauncherSettings.getInstance(this).generalSettings.drawerRememberPage))
                    .add(new MaterialPrefFragment.GroupTitle("Apps"))
                    .add(new MaterialPrefFragment.NUMPref("iconsize", "Icon Size", "Size of all app icon", LauncherSettings.getInstance(this).generalSettings.iconSize, 30, 80))
                    .add(new MaterialPrefFragment.ButtonPref("iconpack", "Icon Pack", "Select installed icon pack"))
                    .add(new MaterialPrefFragment.GroupTitle("Others"))
                    .add(new MaterialPrefFragment.ButtonPref("restart", "Restart", "Restart the launcher"))
                    .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
            getSupportFragmentManager().beginTransaction().add(R.id.ll, fragment).commit();
        }

    }

    @Override
    public void onPrefChanged(String id, Object p2) {
        switch (id) {
            case "drawerRememberPage":
                LauncherSettings.getInstance(this).generalSettings.drawerRememberPage = !(boolean) p2;
                break;
            case "desktopSearchBar":
                LauncherSettings.getInstance(this).generalSettings.desktopSearchBar = (boolean) p2;
                if (!(boolean) p2)
                    Home.launcher.searchBar.setVisibility(View.GONE);
                else
                    Home.launcher.searchBar.setVisibility(View.VISIBLE);
                break;
            case "iconsize":
                LauncherSettings.getInstance(this).generalSettings.iconSize = (int) p2;
                requireLauncherRestart = true;
                break;
            case "horigridsize":
                LauncherSettings.getInstance(this).generalSettings.drawerGridX = (int) p2;
                requireLauncherRestart = true;
                break;
            case "vertgridsize":
                LauncherSettings.getInstance(this).generalSettings.drawerGridY = (int) p2;
                requireLauncherRestart = true;
                break;
            case "dockShowLabel":
                LauncherSettings.getInstance(this).generalSettings.dockShowLabel = (boolean)p2;
                requireLauncherRestart = true;
                break;
            case "appdrawersearchbar":
                LauncherSettings.getInstance(this).generalSettings.drawerSearchBar = (boolean)p2;
                requireLauncherRestart = true;
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (requireLauncherRestart) Home.launcher.recreate();
        super.onDestroy();
    }

    @Override
    public void onPrefClicked(String id) {
        switch (id) {
            case "restart":
                Home.launcher.recreate();
                requireLauncherRestart = false;
                finish();
                break;
            case "iconpack":
                AppManager.getInstance(this).startPickIconPackIntent(this);
                break;
            case "drawerstyle":
                AppDrawer.startStylePicker(this);
                requireLauncherRestart = true;
                break;
            case "desktopMode":
                Desktop.startStylePicker(this);
                requireLauncherRestart = true;
                break;
        }
    }
}
