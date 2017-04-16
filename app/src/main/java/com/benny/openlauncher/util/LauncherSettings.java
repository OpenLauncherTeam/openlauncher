package com.benny.openlauncher.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;

import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.Desktop;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LauncherSettings {

    private static final String DesktopData2FileName = "desktopData2.json";
    private static final String DesktopDataFileName = "desktopData.json";
    private static final String DockData2FileName = "dockData2.json";
    private static final String DockDataFileName = "dockData.json";
    private static final String GeneralSettingsFileName = "generalSettings.json";
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

    private void readDockData(Gson gson, Desktop.DesktopMode mode) {
        dockData.clear();

        // TODO
        // DatabaseHelper db = new DatabaseHelper(context);
        // dockData = db.getDock();

        String dataName = null;
        switch (mode) {
            case Normal:
                dataName = DockDataFileName;
                break;
            case ShowAllApps:
                dataName = DockData2FileName;
                break;
        }
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(context.openFileInput(dataName)));
            reader.beginArray();
            while (reader.hasNext()) {
                Desktop.SimpleItem item = gson.fromJson(reader, Desktop.SimpleItem.class);
                Desktop.Item item1 = new Desktop.Item(item);
                dockData.add(item1);
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readDesktopData(Gson gson, Desktop.DesktopMode mode) {
        desktopData.clear();

        // TODO
        // DatabaseHelper db = new DatabaseHelper(context);
        // desktopData = db.getDesktop();

        String dataName = null;
        switch (mode) {
            case Normal:
                dataName = DesktopDataFileName;
                break;
            case ShowAllApps:
                dataName = DesktopData2FileName;
                break;
        }
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(context.openFileInput(dataName)));
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginArray();
                ArrayList<Desktop.Item> items = new ArrayList<>();
                while (reader.hasNext()) {
                    Desktop.SimpleItem item = gson.fromJson(reader, Desktop.SimpleItem.class);
                    Desktop.Item item1 = new Desktop.Item(item);
                    items.add(item1);
                }
                desktopData.add(items);
                reader.endArray();
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkIconCacheIDs(Desktop.Item item) {
        if (item.type == Desktop.Item.Type.SHORTCUT) {
            iconCacheIDs.add(item.actions[0].getStringExtra("shortCutIconID"));
        } else if (item.type == Desktop.Item.Type.GROUP) {
            for (int i = 0; i < item.actions.length; i++) {
                String ID;
                if ((ID = item.actions[i].getStringExtra("shortCutIconID")) != null) {
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

        String raw = Tool.getStringFromFile(GeneralSettingsFileName, context);
        if (raw == null)
            generalSettings = new GeneralSettings();
        else
            try {
                generalSettings = gson.fromJson(raw, GeneralSettings.class);
            } catch (JsonSyntaxException error) {
                generalSettings = new GeneralSettings();
                noError = false;
            }

        try {
            readDockData(gson, generalSettings.desktopMode);
        } catch (JsonSyntaxException error) {
            noError = false;
        }
        try {
            readDesktopData(gson, generalSettings.desktopMode);
        } catch (JsonSyntaxException error) {
            noError = false;
        }
        return noError;
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

    public void switchDesktopMode(int position) {
        Desktop.DesktopMode mode = Desktop.DesktopMode.values()[position];
        writeSettings();

        iconCacheIDs.clear();
        for (int i = 0; i < desktopData.size(); i++) {
            for (int l = 0; l < desktopData.get(i).size(); l++) {
                checkIconCacheIDs(desktopData.get(i).get(l));
            }
        }
        for (int i = 0; i < dockData.size(); i++) {
            checkIconCacheIDs(dockData.get(i));
        }

        Gson gson = new Gson();
        generalSettings.desktopMode = mode;
        generalSettings.desktopHomePage = position;
        readDesktopData(gson, mode);
        readDockData(gson, mode);

        Tool.checkForUnusedIconAndDelete(context, iconCacheIDs);

        // init all the apps to the desktop for the first time.
        if (mode == Desktop.DesktopMode.ShowAllApps && desktopData.size() == 0) {
            int pageCount = 0;
            List<AppManager.App> apps = AppManager.getInstance(context).getApps();
            int appsSize = apps.size();
            while ((appsSize = appsSize - (generalSettings.desktopGridY * generalSettings.desktopGridX)) >= (generalSettings.desktopGridY * generalSettings.desktopGridX) || (appsSize > -(generalSettings.desktopGridY * generalSettings.desktopGridX))) {
                pageCount++;
            }
            for (int i = 0; i < pageCount; i++) {
                ArrayList<Desktop.Item> items = new ArrayList<>();
                for (int x = 0; x < generalSettings.desktopGridX; x++) {
                    for (int y = 0; y < generalSettings.desktopGridY; y++) {
                        int pagePos = y * generalSettings.desktopGridY + x;
                        final int pos = generalSettings.desktopGridY * generalSettings.desktopGridX * i + pagePos;
                        if (!(pos >= apps.size())) {
                            Desktop.Item appItem = Desktop.Item.newAppItem(apps.get(pos));
                            appItem.x = x;
                            appItem.y = y;
                            items.add(appItem);
                        }
                    }
                }
                desktopData.add(items);
            }
        }
    }

    public Gson writeSettings() {
        if (generalSettings == null) {
            return null;
        }

        // TODO
        // DatabaseHelper db = new DatabaseHelper(context);
        // db.setDesktop(desktopData);
        // db.setDock(dockData);

        Gson gson = new Gson();
        List<List<Desktop.SimpleItem>> simpleDesktopData = new ArrayList<>();
        List<Desktop.SimpleItem> simpleDockData = new ArrayList<>();

        for (Desktop.Item item : dockData) {
            simpleDockData.add(new Desktop.SimpleItem(item));
        }
        for (List<Desktop.Item> pages : desktopData) {
            final ArrayList<Desktop.SimpleItem> page = new ArrayList<>();
            simpleDesktopData.add(page);
            for (Desktop.Item item : pages) {
                page.add(new Desktop.SimpleItem(item));
            }
        }
        switch (generalSettings.desktopMode) {
            case Normal:
                Tool.writeToFile(DesktopDataFileName, gson.toJson(simpleDesktopData), context);
                Tool.writeToFile(DockDataFileName, gson.toJson(simpleDockData), context);
                break;
            case ShowAllApps:
                Tool.writeToFile(DesktopData2FileName, gson.toJson(simpleDesktopData), context);
                Tool.writeToFile(DockData2FileName, gson.toJson(simpleDockData), context);
                break;
        }

        Tool.writeToFile(GeneralSettingsFileName, gson.toJson(generalSettings), context);
        return gson;
    }

    // edit this carefully as changing the type of a field will cause a parsing error when the launcher starts
    public static class GeneralSettings {
        //Icon
        public int iconSize = 58;
        public String iconPackName = "";

        //Desktop
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

        //Drawer
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

        //Dock
        public int dockColor = Color.TRANSPARENT;
        public int dockGridX = 5;
        public boolean dockShowLabel = true;

        //MiniBar
        public boolean minBarEnable = true;
        public ArrayList<String> miniBarArrangement;

        //Not used
        public LauncherAction.Theme theme = LauncherAction.Theme.Light;

        //Others
        public boolean firstLauncher = true;
    }
}
