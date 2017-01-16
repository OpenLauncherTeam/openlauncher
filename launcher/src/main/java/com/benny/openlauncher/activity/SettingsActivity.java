package com.benny.openlauncher.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.AppDrawer;
import com.benny.openlauncher.widget.Desktop;
import com.bennyv5.materialpreffragment.BaseSettingsActivity;
import com.bennyv5.materialpreffragment.MaterialPrefFragment;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.LauncherSettings;

public class SettingsActivity extends BaseSettingsActivity implements MaterialPrefFragment.OnPrefClickedListener, MaterialPrefFragment.OnPrefChangedListener {

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
            LauncherSettings.GeneralSettings generalSettings = LauncherSettings.getInstance(this).generalSettings;
            MaterialPrefFragment fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this,Color.DKGRAY, getResources().getColor(R.color.Light_TextColor), getResources().getColor(R.color.Light_Background), getResources().getColor(R.color.colorAccent), false)
                    .add(new MaterialPrefFragment.GroupTitle("Desktop"))
                    .add(new MaterialPrefFragment.ButtonPref("desktopMode", "Style", "choose different style of your desktop"))
                    .add(new MaterialPrefFragment.TBPref("desktopSearchBar", "Show search bar", "Display a search bar always on top of the desktop", generalSettings.desktopSearchBar))
                    // FIXME: 11/25/2016 This will have problem (in allappsmode) as the apps will be cut off when scale down
                    .add(new MaterialPrefFragment.NUMPref("gridsizedesktop","Vertical size", "Desktop grid size",
                            new MaterialPrefFragment.NUMPref.NUMPrefItem("horigridsizedesktop","Column", generalSettings.desktopGridX, 4, 10),
                            new MaterialPrefFragment.NUMPref.NUMPrefItem("vertgridsizedesktop","Row", generalSettings.desktopGridY, 4, 10)
                    ))
                    .add(new MaterialPrefFragment.GroupTitle("Dock"))
                    .add(new MaterialPrefFragment.ColorPref("dockBackground","Background","Dock background color",generalSettings.dockColor))
                    .add(new MaterialPrefFragment.TBPref("dockShowLabel","Show app label","show the app's name in the dock", generalSettings.dockShowLabel))
                    .add(new MaterialPrefFragment.NUMPref("gridsizedock","Vertical size", "Dock grid size",
                            new MaterialPrefFragment.NUMPref.NUMPrefItem("horigridsizedock","Column", generalSettings.dockGridX, 5, 10)
                    ))
                    .add(new MaterialPrefFragment.GroupTitle("AppDrawer"))
                    .add(new MaterialPrefFragment.ButtonPref("drawerstyle", "Style", "choose the style of the app drawer"))
                    .add(new MaterialPrefFragment.ColorPref("drawerBackground","Background","Drawer background color",generalSettings.drawerColor))
                    .add(new MaterialPrefFragment.TBPref("drawerCard", "Use Card", "Drawer card", generalSettings.drawerUseCard))
                    .add(new MaterialPrefFragment.ColorPref("drawerCardBackground","Card Background","Drawer card background color",generalSettings.drawerCardColor))
                    .add(new MaterialPrefFragment.ColorPref("drawerLabelColor","Label Color","Drawer label Color",generalSettings.drawerLabelColor))
                    .add(new MaterialPrefFragment.TBPref("appdrawersearchbar", "Search Bar", "search bar will only appear in grid drawer", generalSettings.drawerSearchBar))
                    .add(new MaterialPrefFragment.NUMPref("gridsize","Vertical size", "App drawer grid size",
                            new MaterialPrefFragment.NUMPref.NUMPrefItem("horigridsize","Column", generalSettings.drawerGridX, 1, 10),
                            new MaterialPrefFragment.NUMPref.NUMPrefItem("vertgridsize","Row", generalSettings.drawerGridY, 1, 10)
                    ))
                    .add(new MaterialPrefFragment.TBPref("drawerRememberPage", "Remember last page", "The page will not reset to the first page when reopen app drawer", !generalSettings.drawerRememberPage))
                    .add(new MaterialPrefFragment.GroupTitle("Apps"))
                    .add(new MaterialPrefFragment.NUMPref("iconsize", "Icon Size", "Size of all app icon", generalSettings.iconSize, 30, 80))
                    .add(new MaterialPrefFragment.ButtonPref("iconpack", "Icon Pack", "Select installed icon pack"))
                    .add(new MaterialPrefFragment.GroupTitle("Others"))
                    .add(new MaterialPrefFragment.ButtonPref("restart", "Restart", "Restart the launcher"))
                    .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
            setSettingsFragment(fragment);
            getSupportFragmentManager().beginTransaction().add(R.id.ll, fragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.launcherInfo:
                startActivity(new Intent(this,AboutActivity.class));
//                new MaterialDialog.Builder(this).title("About")
//                        .content(R.string.launcherInfo)
//                        .positiveText("Ok")
//                        .negativeText("Rate")
//                        .onNegative(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                String url = "https://play.google.com/store/apps/details?id=com.benny.openlauncher";
//                                Intent i = new Intent(Intent.ACTION_VIEW);
//                                i.setData(Uri.parse(url));
//                                startActivity(i);
//                            }
//                        })
//                        .neutralText("GitHub")
//                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                String url = "https://github.com/BennyKok/OpenLauncher";
//                                Intent i = new Intent(Intent.ACTION_VIEW);
//                                i.setData(Uri.parse(url));
//                                startActivity(i);
//                            }
//                        })
//                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrefChanged(String id, Object p2) {
        LauncherSettings.GeneralSettings generalSettings = LauncherSettings.getInstance(this).generalSettings;
        switch (id) {
            case "drawerRememberPage":
                generalSettings.drawerRememberPage = !(boolean) p2;
                break;
            case "desktopSearchBar":
                generalSettings.desktopSearchBar = (boolean) p2;
                if (!(boolean) p2)
                    Home.launcher.searchBar.setVisibility(View.GONE);
                else
                    Home.launcher.searchBar.setVisibility(View.VISIBLE);
                break;
            case "iconsize":
                generalSettings.iconSize = (int) p2;
                prepareRestart();
                break;
            case "horigridsize":
                generalSettings.drawerGridX = (int) p2;
                prepareRestart();
                break;
            case "vertgridsize":
                generalSettings.drawerGridY = (int) p2;
                prepareRestart();
                break;
            case "dockShowLabel":
                generalSettings.dockShowLabel = (boolean)p2;
                prepareRestart();
                break;
            case "appdrawersearchbar":
                generalSettings.drawerSearchBar = (boolean)p2;
                prepareRestart();
                break;
            case "horigridsizedesktop":
                generalSettings.desktopGridX = (int)p2;
                prepareRestart();
                break;
            case "vertgridsizedesktop":
                generalSettings.desktopGridY = (int)p2;
                prepareRestart();
                break;
            case "horigridsizedock":
                generalSettings.dockGridX = (int)p2;
                prepareRestart();
                break;
            case "dockBackground":
                generalSettings.dockColor = (int)p2;
                if (Home.launcher != null)
                    Home.launcher.dock.setBackgroundColor((int)p2);
                else
                    prepareRestart();
                break;
            case "drawerBackground":
                generalSettings.drawerColor = (int)p2;
                if (Home.launcher != null) {
                    Home.launcher.appDrawerOtter.setBackgroundColor((int) p2);
                    Home.launcher.appDrawerOtter.getBackground().setAlpha(0);
                }else
                    prepareRestart();
                break;
            case "drawerCard":
                generalSettings.drawerUseCard = (boolean)p2;
                if (Home.launcher != null) {
                    Home.launcher.appDrawerOtter.reloadDrawerCardTheme();
                }else
                    prepareRestart();
                break;
            case "drawerCardBackground":
                generalSettings.drawerCardColor = (int)p2;
                if (Home.launcher != null) {
                    Home.launcher.appDrawerOtter.reloadDrawerCardTheme();
                }else
                    prepareRestart();
                break;
            case "drawerLabelColor":
                generalSettings.drawerLabelColor = (int)p2;
                if (Home.launcher != null) {
                    Home.launcher.appDrawerOtter.reloadDrawerCardTheme();
                }else
                    prepareRestart();
                break;
        }
    }

    private void prepareRestart() {
        requireLauncherRestart = true;
    }

    @Override
    protected void onDestroy() {
        if (requireLauncherRestart && Home.launcher != null) Home.launcher.recreate();
        super.onDestroy();
    }

    @Override
    public void onPrefClicked(String id) {
        switch (id) {
            case "restart":
                if (Home.launcher != null)
                    Home.launcher.recreate();
                requireLauncherRestart = false;
                finish();
                break;
            case "iconpack":
                AppManager.getInstance(this).startPickIconPackIntent(this);
                break;
            case "drawerstyle":
                AppDrawer.startStylePicker(this);
                prepareRestart();
                break;
            case "desktopMode":
                Desktop.startStylePicker(this);
                prepareRestart();
                break;
        }
    }
}
