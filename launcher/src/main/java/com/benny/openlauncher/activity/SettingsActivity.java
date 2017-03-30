package com.benny.openlauncher.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.fragment.SettingsFragment;
import com.benny.openlauncher.hideApps.Activity_hideApps;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DialogUtils;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.AppDrawer;
import com.bennyv5.materialpreffragment.BaseSettingsActivity;
import com.bennyv5.materialpreffragment.MaterialPrefFragment;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.LauncherSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SettingsActivity extends BaseSettingsActivity implements MaterialPrefFragment.OnPrefClickedListener, MaterialPrefFragment.OnPrefChangedListener, SettingsFragment.OnSettingsSelectedInterface {

    private boolean requireLauncherRestart = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Tool.setTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.tb));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.settings);

        //SettingsFragment settings = new SettingsFragment();

        MaterialPrefFragment settings = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this, Color.DKGRAY, ContextCompat.getColor(this, R.color.Light_TextColor), ContextCompat.getColor(this, R.color.Light_Background), ContextCompat.getColor(this, R.color.colorAccent), false)
                .add(new MaterialPrefFragment.ButtonPref("desktopSettings", getResources().getDrawable(R.drawable.ic_desktop_windows_black_24dp), getString(R.string.settings_group_desktop), null))
                .add(new MaterialPrefFragment.ButtonPref("dockSettings", getResources().getDrawable(R.drawable.ic_dock_black_24dp), getString(R.string.settings_group_dock), null))
                .add(new MaterialPrefFragment.ButtonPref("drawerSettings", getResources().getDrawable(R.drawable.ic_apps_black_24dp), getString(R.string.settings_group_drawer), null))
                .add(new MaterialPrefFragment.ButtonPref("inputSettings", getResources().getDrawable(R.drawable.ic_gesture_black_24dp), getString(R.string.settings_group_input), null))
                .add(new MaterialPrefFragment.ButtonPref("colorsSettings", getResources().getDrawable(R.drawable.ic_color_lens_black_24dp), getString(R.string.settings_group_color), null))
                .add(new MaterialPrefFragment.ButtonPref("iconsSettings", getResources().getDrawable(R.drawable.ic_android_black_24dp), getString(R.string.settings_group_icons), null))
                .add(new MaterialPrefFragment.ButtonPref("otherSettings", getResources().getDrawable(R.drawable.ic_more_horiz_black_24dp), getString(R.string.settings_group_other), null))
                .setOnPrefClickedListener(this));

        getSupportFragmentManager().beginTransaction().add(R.id.ll, settings).commit();
    }

    @Override
    public void onSettingsSelected(int settingsCategory) {
        LauncherSettings.GeneralSettings generalSettings = LauncherSettings.getInstance(this).generalSettings;
        MaterialPrefFragment fragment;
        switch (settingsCategory) {
            default:
                fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this, Color.DKGRAY, ContextCompat.getColor(this, R.color.Light_TextColor), ContextCompat.getColor(this, R.color.Light_Background), ContextCompat.getColor(this, R.color.colorAccent), true)
                        .add(new MaterialPrefFragment.ButtonPref("desktopMode", (getString(R.string.settings_desktopStyle)), (getString(R.string.settings_desktopStyle_summary))))
                        .add(new MaterialPrefFragment.ButtonPref("editMiniBar", "MiniBar arrangement", "Rearrange minibar item"))
                        // FIXME: 11/25/2016 This will have problem (in allappsmode) as the apps will be cut off when scale down
                        .add(new MaterialPrefFragment.NUMPref("gridSizeDesktop", (getString(R.string.settings_desktopSize)), (getString(R.string.settings_desktopSize_summary)),
                                new MaterialPrefFragment.NUMPref.NUMPrefItem("hGridSizeDesktop", (getString(R.string.settings_column)), generalSettings.desktopGridX, 4, 10),
                                new MaterialPrefFragment.NUMPref.NUMPrefItem("vGridSizeDesktop", (getString(R.string.settings_row)), generalSettings.desktopGridY, 4, 10)
                        ))
                        .add(new MaterialPrefFragment.TBPref("desktopSearchBar", (getString(R.string.settings_desktopSearch)), (getString(R.string.settings_desktopSearch_summary)), generalSettings.desktopSearchBar))
                        .add(new MaterialPrefFragment.TBPref("fullscreen", (getString(R.string.settings_desktopFull)), (getString(R.string.settings_desktopFull_summary)), generalSettings.fullscreen))
                        .add(new MaterialPrefFragment.TBPref("showIndicator", (getString(R.string.settings_desktopIndicator)), (getString(R.string.settings_desktopIndicator_summary)), generalSettings.showIndicator))
                        .add(new MaterialPrefFragment.TBPref("desktopShowLabel", (getString(R.string.settings_dockLabel)), (getString(R.string.settings_desktopLabel_summary)), generalSettings.desktopShowLabel))
                        .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
                getSupportActionBar().setTitle(R.string.settings_group_desktop);
                break;
            case 1:
                fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this, Color.DKGRAY, ContextCompat.getColor(this, R.color.Light_TextColor), ContextCompat.getColor(this, R.color.Light_Background), ContextCompat.getColor(this, R.color.colorAccent), true)
                        .add(new MaterialPrefFragment.NUMPref("gridSizeDock", (getString(R.string.settings_dockSize)), (getString(R.string.settings_dockSize_summary)),
                                new MaterialPrefFragment.NUMPref.NUMPrefItem("hGridSizeDock", (getString(R.string.settings_column)), generalSettings.dockGridX, 4, 10)
                        ))
                        .add(new MaterialPrefFragment.TBPref("dockShowLabel", (getString(R.string.settings_dockLabel)), (getString(R.string.settings_dockLabel_summary)), generalSettings.dockShowLabel))
                        .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
                getSupportActionBar().setTitle(R.string.settings_group_dock);
                break;
            case 2:
                fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this, Color.DKGRAY, ContextCompat.getColor(this, R.color.Light_TextColor), ContextCompat.getColor(this, R.color.Light_Background), ContextCompat.getColor(this, R.color.colorAccent), true)
                        .add(new MaterialPrefFragment.ButtonPref("drawerStyle", (getString(R.string.settings_drawerStyle)), (getString(R.string.settings_drawerStyle_summary))))
                        .add(new MaterialPrefFragment.NUMPref("gridSize", (getString(R.string.settings_drawerSize)), (getString(R.string.settings_drawerSize_summary)),
                                new MaterialPrefFragment.NUMPref.NUMPrefItem("hGridSize", (getString(R.string.settings_column)), generalSettings.drawerGridX, 1, 10),
                                new MaterialPrefFragment.NUMPref.NUMPrefItem("vGridSize", (getString(R.string.settings_row)), generalSettings.drawerGridY, 1, 10)
                        ))
                        .add(new MaterialPrefFragment.TBPref("drawerCard", (getString(R.string.settings_drawerCard)), (getString(R.string.settings_drawerCard_summary)), generalSettings.drawerUseCard))
                        .add(new MaterialPrefFragment.TBPref("drawerSearchBar", (getString(R.string.settings_drawerSearch)), (getString(R.string.settings_drawerSearch_summary)), generalSettings.drawerSearchBar))
                        .add(new MaterialPrefFragment.TBPref("drawerLight", (getString(R.string.settings_drawerSearchIcon)), (getString(R.string.settings_drawerSearchIcon_summary)), generalSettings.drawerLight))
                        .add(new MaterialPrefFragment.TBPref("drawerRememberPage", (getString(R.string.settings_drawerPage)), (getString(R.string.settings_drawerPage_summary)), !generalSettings.drawerRememberPage))
                        .add(new MaterialPrefFragment.TBPref("drawerShowIndicator", (getString(R.string.settings_drawerIndicator)), (getString(R.string.settings_drawerIndicator_summary)), generalSettings.drawerShowIndicator))
                        .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
                getSupportActionBar().setTitle(R.string.settings_group_drawer);
                break;
            case 3:
                fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this, Color.DKGRAY, ContextCompat.getColor(this, R.color.Light_TextColor), ContextCompat.getColor(this, R.color.Light_Background), ContextCompat.getColor(this, R.color.colorAccent), true)
                        .add(new MaterialPrefFragment.TBPref("swipe", (getString(R.string.settings_desktopSwipe)), (getString(R.string.settings_desktopSwipe_summary)), generalSettings.swipe))
                        .add(new MaterialPrefFragment.ButtonPref("singleClick", (getString(R.string.settings_singleClick)), (getString(R.string.settings_singleClick_summary))))
                        .add(new MaterialPrefFragment.ButtonPref("doubleClick", (getString(R.string.settings_doubleClick)), (getString(R.string.settings_doubleClick_summary))))
                        .add(new MaterialPrefFragment.ButtonPref("pinch", (getString(R.string.settings_pinch)), (getString(R.string.settings_pinch_summary))))
                        .add(new MaterialPrefFragment.ButtonPref("unPinch", (getString(R.string.settings_unpinch)), (getString(R.string.settings_unpinch_summary))))
                        .add(new MaterialPrefFragment.ButtonPref("swipeDown", (getString(R.string.settings_swipeDown)), (getString(R.string.settings_swipeDown_summary))))
                        .add(new MaterialPrefFragment.ButtonPref("swipeUp", (getString(R.string.settings_swipeUp)), (getString(R.string.settings_swipeUp_summary))))
                        .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
                getSupportActionBar().setTitle(R.string.settings_group_input);
                break;
            case 4:
                fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this, Color.DKGRAY, ContextCompat.getColor(this, R.color.Light_TextColor), ContextCompat.getColor(this, R.color.Light_Background), ContextCompat.getColor(this, R.color.colorAccent), true)
                        .add(new MaterialPrefFragment.ColorPref("dockBackground", (getString(R.string.settings_colorDock)), (getString(R.string.settings_colorDock_summary)), generalSettings.dockColor))
                        .add(new MaterialPrefFragment.ColorPref("drawerBackground", (getString(R.string.settings_colorDrawer)), (getString(R.string.settings_colorDrawer_summary)), generalSettings.drawerColor))
                        .add(new MaterialPrefFragment.ColorPref("drawerCardBackground", (getString(R.string.settings_colorFolder)), (getString(R.string.settings_colorFolder_summary)), generalSettings.drawerCardColor))
                        .add(new MaterialPrefFragment.ColorPref("folderColor", (getString(R.string.settings_colorAppFolder)), (getString(R.string.settings_colorAppFolder_summary)), generalSettings.folderColor))
                        .add(new MaterialPrefFragment.ColorPref("drawerLabelColor", (getString(R.string.settings_colorLabel)), (getString(R.string.settings_colorLabel_summary)), generalSettings.drawerLabelColor))
                        .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
                getSupportActionBar().setTitle(R.string.settings_group_color);
                break;
            case 5:
                fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this, Color.DKGRAY, ContextCompat.getColor(this, R.color.Light_TextColor), ContextCompat.getColor(this, R.color.Light_Background), ContextCompat.getColor(this, R.color.colorAccent), true)
                        .add(new MaterialPrefFragment.NUMPref("iconSize", (getString(R.string.settings_iconSize)), (getString(R.string.settings_iconSize_summary)), generalSettings.iconSize, 30, 80))
                        .add(new MaterialPrefFragment.ButtonPref("iconPack", (getString(R.string.settings_iconPack)), (getString(R.string.settings_iconPack_summary))))
                        .add(new MaterialPrefFragment.ButtonPref("iconHide", (getString(R.string.settings_iconHide)), (getString(R.string.settings_iconHide_summary))))
                        .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
                getSupportActionBar().setTitle(R.string.settings_group_icons);
                break;
            case 6:
                fragment = MaterialPrefFragment.newInstance(new MaterialPrefFragment.Builder(this, Color.DKGRAY, ContextCompat.getColor(this, R.color.Light_TextColor), ContextCompat.getColor(this, R.color.Light_Background), ContextCompat.getColor(this, R.color.colorAccent), true)
                        .add(new MaterialPrefFragment.ButtonPref("backup", (getString(R.string.settings_backup)), (getString(R.string.settings_backup_summary))))
                        .add(new MaterialPrefFragment.ButtonPref("restart", getString(R.string.settings_othersRestart), getString(R.string.settings_othersRestart_summary)))
                        .setOnPrefChangedListener(this).setOnPrefClickedListener(this));
                getSupportActionBar().setTitle(R.string.settings_group_other);
                break;
        }
        setSettingsFragment(fragment);
        getSupportFragmentManager().beginTransaction().replace(R.id.ll, fragment).addToBackStack("notMenu").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.launcherInfo:
                Intent intent = new Intent(this, AboutActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrefChanged(String id, Object value) {
        LauncherSettings.GeneralSettings generalSettings = LauncherSettings.getInstance(this).generalSettings;
        switch (id) {
            case "drawerRememberPage":
                generalSettings.drawerRememberPage = !(boolean) value;
                break;
            case "desktopSearchBar":
                generalSettings.desktopSearchBar = (boolean) value;
                if (!(boolean) value)
                    Home.launcher.searchBar.setVisibility(View.GONE);
                else
                    Home.launcher.searchBar.setVisibility(View.VISIBLE);
                break;
            case "fullscreen":
                generalSettings.fullscreen = (boolean) value;
                prepareRestart();
                break;
            case "swipe":
                generalSettings.swipe = (boolean) value;
                break;
            case "singleClick":
                generalSettings.singleClick = (int) value;
                break;
            case "doubleClick":
                generalSettings.doubleClick = (int) value;
                break;
            case "showIndicator":
                generalSettings.showIndicator = (boolean) value;
                prepareRestart();
                break;
            case "iconSize":
                generalSettings.iconSize = (int) value;
                prepareRestart();
                break;
            case "hGridSize":
                generalSettings.drawerGridX = (int) value;
                prepareRestart();
                break;
            case "vGridSize":
                generalSettings.drawerGridY = (int) value;
                prepareRestart();
                break;
            case "dockShowLabel":
                generalSettings.dockShowLabel = (boolean) value;
                prepareRestart();
                break;
            case "drawerSearchBar":
                generalSettings.drawerSearchBar = (boolean) value;
                prepareRestart();
                break;
            case "drawerLight":
                generalSettings.drawerLight = (boolean) value;
                prepareRestart();
                break;
            case "hGridSizeDesktop":
                generalSettings.desktopGridX = (int) value;
                prepareRestart();
                break;
            case "vGridSizeDesktop":
                generalSettings.desktopGridY = (int) value;
                prepareRestart();
                break;
            case "hGridSizeDock":
                generalSettings.dockGridX = (int) value;
                prepareRestart();
                break;
            case "dockBackground":
                generalSettings.dockColor = (int) value;
                if (Home.launcher != null)
                    Home.launcher.dock.setBackgroundColor((int) value);
                else
                    prepareRestart();
                break;
            case "drawerBackground":
                generalSettings.drawerColor = (int) value;
                if (Home.launcher != null) {
                    Home.launcher.appDrawerContainer.setBackgroundColor((int) value);
                    Home.launcher.appDrawerContainer.getBackground().setAlpha(0);
                } else
                    prepareRestart();
                break;
            case "drawerCard":
                generalSettings.drawerUseCard = (boolean) value;
                if (Home.launcher != null) {
                    Home.launcher.appDrawerContainer.reloadDrawerCardTheme();
                } else
                    prepareRestart();
                break;
            case "drawerCardBackground":
                generalSettings.drawerCardColor = (int) value;
                if (Home.launcher != null) {
                    Home.launcher.appDrawerContainer.reloadDrawerCardTheme();
                    prepareRestart();
                } else
                    prepareRestart();
                break;
            case "drawerLabelColor":
                generalSettings.drawerLabelColor = (int) value;
                if (Home.launcher != null) {
                    Home.launcher.appDrawerContainer.reloadDrawerCardTheme();
                    prepareRestart();
                } else
                    prepareRestart();
                break;
          case "folderColor":
            generalSettings.folderColor = (int) value;
            if (Home.launcher != null) {
              Home.launcher.appDrawerContainer.reloadDrawerCardTheme();
              prepareRestart();
            } else
              prepareRestart();
            break;
            case "desktopShowLabel":
                generalSettings.desktopShowLabel = (boolean) value;
                prepareRestart();
                break;
            case "drawerShowIndicator":
                generalSettings.drawerShowIndicator = (boolean) value;
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
            case "desktopSettings":
                onSettingsSelected(0);
                break;
            case "dockSettings":
                onSettingsSelected(1);
                break;
            case "drawerSettings":
                onSettingsSelected(2);
                break;
            case "inputSettings":
                onSettingsSelected(3);
                break;
            case "colorsSettings":
                onSettingsSelected(4);
                break;
            case "iconsSettings":
                onSettingsSelected(5);
                break;
            case "otherSettings":
                onSettingsSelected(6);
                break;
            case "restart":
                if (Home.launcher != null)
                    Home.launcher.recreate();
                requireLauncherRestart = false;
                finish();
                break;
            case "editMiniBar":
                LauncherAction.RunAction(LauncherAction.Action.EditMinBar, this);
                break;
            case "iconPack":
                AppManager.getInstance(this).startPickIconPackIntent(this);
                break;
            case "drawerStyle":
                DialogUtils.appDrawerStylePicker(this);
                prepareRestart();
                break;
            case "desktopMode":
                DialogUtils.desktopStylePicker(this);
                prepareRestart();
                break;
            case "singleClick":
                DialogUtils.startSingleClickPicker(this);
                prepareRestart();
                break;
            case "doubleClick":
                DialogUtils.startDoubleClickPicker(this);
                prepareRestart();
                break;
            case "pinch":
                DialogUtils.startPinchPicker(this);
                prepareRestart();
                break;
            case "unPinch":
                DialogUtils.startUnpinchPicker(this);
                prepareRestart();
                break;
            case "swipeDown":
                DialogUtils.startSwipeDownPicker(this);
                prepareRestart();
                break;
            case "swipeUp":
                DialogUtils.startSwipeUpPicker(this);
                prepareRestart();
                break;
            case "iconHide":
                Intent intent = new Intent(SettingsActivity.this, Activity_hideApps.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            case "backup":
                if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    final CharSequence[] options = {
                            getString(R.string.settings_backup_titleBackup),
                            getString(R.string.settings_backup_titleRestore)};

                    MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
                    builder.title(R.string.settings_backup)
                            .positiveText(R.string.cancel)
                            .items(options)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int item, CharSequence text) {
                                    PackageManager m = getPackageManager();
                                    String s = getPackageName();

                                    if (options[item].equals(getString(R.string.settings_backup_titleBackup))) {
                                        File directory = new File(Environment.getExternalStorageDirectory() + "/launcher.backup/");
                                        if (!directory.exists()) {
                                            //noinspection ResultOfMethodCallIgnored
                                            directory.mkdirs();
                                        }

                                        try {
                                            PackageInfo p = m.getPackageInfo(s, 0);
                                            s = p.applicationInfo.dataDir;
                                            copy(s + "/files/desktopData.json", directory + "/desktopData.json");
                                            copy(s + "/files/dockData.json", directory + "/dockData.json");
                                            copy(s + "/files/generalSettings.json", directory + "/generalSettings.json");
                                            Toast.makeText(SettingsActivity.this, R.string.settings_backup_success, Toast.LENGTH_SHORT).show();

                                        } catch (Exception e) {
                                            Toast.makeText(SettingsActivity.this, R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    if (options[item].equals(getString(R.string.settings_backup_titleRestore))) {
                                        File directory = new File(Environment.getExternalStorageDirectory() + "/launcher.backup/");

                                        try {
                                            PackageInfo p = m.getPackageInfo(s, 0);
                                            s = p.applicationInfo.dataDir;
                                            copy(directory + "/desktopData.json", s + "/files/desktopData.json");
                                            copy(directory + "/dockData.json", s + "/files/dockData.json");
                                            copy(directory + "/generalSettings.json", s + "/files/generalSettings.json");
                                            Toast.makeText(SettingsActivity.this, R.string.settings_backup_success, Toast.LENGTH_SHORT).show();

                                        } catch (Exception e) {
                                            Toast.makeText(SettingsActivity.this, R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
                                        }
                                        //This will stop your application and take out from it.
                                        System.exit(1); // kill off the crashed app
                                    }
                                }
                            });
                    builder.show();
                } else {
                    Tool.toast(this, (getString(R.string.settings_iconPack_toast)));
                    ActivityCompat.requestPermissions(Home.launcher, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Home.REQUEST_PERMISSION_STORAGE);
                }
                break;
        }
    }

    private void copy(String stringIn, String stringOut) {

        try {
            File desktopData = new File(stringOut);
            desktopData.delete();
            File dockData = new File(stringOut);
            dockData.delete();
            File generalSettings = new File(stringOut);
            generalSettings.delete();
            Tool.print("deleted");


            FileInputStream in = new FileInputStream(stringIn);
            FileOutputStream out = new FileOutputStream(stringOut);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();
            Tool.print("copied");

        } catch (Exception e) {
            Toast.makeText(SettingsActivity.this, R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportActionBar().setTitle(getString(R.string.settings));
    }
}
