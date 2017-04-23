package com.benny.openlauncher.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.Desktop;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class LauncherSettings {
    private static final String settingsFilename = "generalSettings.json";
    private static LauncherSettings ourInstance;
    public List<List<Desktop.Item>> desktopData = new ArrayList<>();
    public List<Desktop.Item> dockData = new ArrayList<>();
    public GeneralSettings generalSettings;
    public SharedPreferences pref;
    public Context context;
    private ArrayList<String> iconCacheIDs = new ArrayList<>();
    public boolean init = false;

    private LauncherSettings(Context c) {
        this.context = c;
    }

    public static LauncherSettings getInstance(Context c) {
        return ourInstance == null ? ourInstance = new LauncherSettings(c) : ourInstance;
    }

    private void checkIconCacheIDs(Desktop.Item item) {
        if (item.type == Desktop.Item.Type.SHORTCUT) {
            iconCacheIDs.add(item.appIntent.getStringExtra("shortCutIconID"));
        } else if (item.type == Desktop.Item.Type.GROUP) {
            for (int i = 0; i < item.items.size(); i++) {
                String ID;
                if ((ID = item.items.get(i).appIntent.getStringExtra("shortCutIconID")) != null) {
                    iconCacheIDs.add(ID);
                }
            }
        }
    }

    public boolean readSettings() {
        boolean noError = true;
        pref = context.getSharedPreferences("LauncherSettings", Context.MODE_PRIVATE);
        iconCacheIDs.clear();
        init = true;

        Gson gson = new Gson();

        String raw = Tool.getStringFromFile(settingsFilename, context);
        if (raw == null) {
            generalSettings = new GeneralSettings();
        } else {
            try {
                generalSettings = gson.fromJson(raw, GeneralSettings.class);
            } catch (JsonSyntaxException error) {
                generalSettings = new GeneralSettings();
                noError = false;
            }
        }

        readDesktopData();
        readDockData();

        return noError;
    }

    public Gson writeSettings() {
        if (generalSettings == null) {
            return null;
        }

        Home.launcher.db.setDesktop(desktopData);
        Home.launcher.db.setDock(dockData);

        Gson gson = new Gson();
        Tool.writeToFile(settingsFilename, gson.toJson(generalSettings), context);
        return gson;
    }

    private void readDockData() {
        dockData = Home.launcher.db.getDock();
    }

    private void readDesktopData() {
        desktopData = Home.launcher.db.getDesktop();
    }

    public void setSingleClickGesture(int value) {
        generalSettings.singleClick = value;
    }

    public void setDoubleClickGesture(int value) {
        generalSettings.doubleClick = value;
    }

    public void setPinchGesture(int value) {
        generalSettings.pinch = value;
    }

    public void setUnPinchGesture(int value) {
        generalSettings.unPinch = value;
    }

    public void setSwipeDownGesture(int value) {
        generalSettings.swipeDown = value;
    }

    public void setSwipeUpGesture(int value) {
        generalSettings.swipeUp = value;
    }

    public void setDesktopMode(int position) {
        Desktop.DesktopMode mode = Desktop.DesktopMode.values()[position];

        // check icon cache for all items
        iconCacheIDs.clear();
        for (int i = 0; i < desktopData.size(); i++) {
            for (int l = 0; l < desktopData.get(i).size(); l++) {
                checkIconCacheIDs(desktopData.get(i).get(l));
            }
        }
        for (int i = 0; i < dockData.size(); i++) {
            checkIconCacheIDs(dockData.get(i));
        }

        generalSettings.desktopMode = mode;
        generalSettings.desktopHomePage = position;

        Tool.checkForUnusedIconAndDelete(context, iconCacheIDs);

        Home.launcher.desktop.initShowAllApps(context);
    }

    // edit this carefully as changing the type of a field will cause a parsing error when the launcher starts
    public static class GeneralSettings {
        // icons
        public int iconSize = 58;
        public String iconPackName = "";

        // desktop
        public Desktop.DesktopMode desktopMode = Desktop.DesktopMode.Normal;
        public int desktopHomePage;
        public int desktopGridX = 4;
        public int desktopGridY = 4;
        public boolean desktopSearchBar = true;
        public boolean fullscreen = false;
        public boolean swipe = false;
        public int singleClick = 0;
        public int doubleClick = 0;
        public int pinch = 0;
        public int unPinch = 0;
        public int swipeDown = 0;
        public int swipeUp = 0;
        public boolean showIndicator = true;
        public boolean desktopShowLabel = true;

        // app drawer
        public int drawerColor = Color.TRANSPARENT;
        public boolean drawerUseCard = true;
        public int drawerCardColor = Color.WHITE;
        public int folderColor = Color.WHITE;
        public int drawerLabelColor = Color.DKGRAY;
        public AppDrawerController.DrawerMode drawerMode = AppDrawerController.DrawerMode.Paged;
        public int drawerGridX = 4;
        public int drawerGridY = 5;
        public int drawerGridX_L = 5;
        public int drawerGridY_L = 3;
        public boolean drawerSearchBar = true;
        public boolean drawerRememberPage = true;
        public ArrayList<String> hiddenList;
        public boolean drawerShowIndicator = true;
        public boolean drawerLight = true;

        // dock
        public int dockColor = Color.TRANSPARENT;
        public int dockGridX = 5;
        public boolean dockShowLabel = true;

        // minibar
        public boolean minBarEnable = true;
        public ArrayList<String> miniBarArrangement;

        // other
        public LauncherAction.Theme theme = LauncherAction.Theme.Light;
        public boolean firstLauncher = true;
    }
}
