package com.benny.openlauncher.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.Desktop;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class LauncherSettings {
    private static final String settingsFilename = "generalSettings.json";
    private static LauncherSettings ourInstance;
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

    private void checkIconCacheIDs(Item item) {
        if (item.type == Item.Type.SHORTCUT) {
            iconCacheIDs.add(item.appIntent.getStringExtra("shortCutIconID"));
        } else if (item.type == Item.Type.GROUP) {
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
        return noError;
    }

    public Gson writeSettings() {
        if (generalSettings == null) {
            return null;
        }
        Gson gson = new Gson();
        Tool.writeToFile(settingsFilename, gson.toJson(generalSettings), context);
        return gson;
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
        List<List<Item>> desktop = Home.launcher.db.getDesktop();
        List<Item> dock = Home.launcher.db.getDock();
        iconCacheIDs.clear();
        for (int i = 0; i < desktop.size(); i++) {
            for (int l = 0; l < desktop.get(i).size(); l++) {
                checkIconCacheIDs(desktop.get(i).get(l));
            }
        }
        for (int i = 0; i < dock.size(); i++) {
            checkIconCacheIDs(dock.get(i));
        }

        generalSettings.desktopMode = mode;
        generalSettings.desktopHomePage = position;

        Tool.checkForUnusedIconAndDelete(context, iconCacheIDs);

        Home.launcher.desktop.initDesktopShowAll(context);
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
        public int doubleClick = 0;
        public int pinch = 0;
        public int unPinch = 0;
        public int swipeDown = 0;
        public int swipeUp = 0;
        public boolean showIndicator = true;
        public boolean desktopShowLabel = true;
        public boolean desktopLock;

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

        //This is a typo, should be firstLaunch...
        public boolean firstLauncher = true;
    }
}
