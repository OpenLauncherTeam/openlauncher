package com.benny.openlauncher.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.benny.openlauncher.widget.AppDrawer;
import com.benny.openlauncher.widget.Desktop;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LauncherSettings {

    private static LauncherSettings ourInstance;

    public static LauncherSettings getInstance(Context c) {
        return ourInstance == null ? ourInstance = new LauncherSettings(c) : ourInstance;
    }

    public List<List<Desktop.Item>> desktopData = new ArrayList<>();

    public List<Desktop.Item> dockData = new ArrayList<>();

    public GeneralSettings generalSettings;

    public SharedPreferences pref;

    public Context context;

    private static final String DesktopDataFileName = "desktopData.json";
    private static final String DockDataFileName = "dockData.json";
    private static final String GeneralSettingsFileName = "generalSettings.json";

    private ArrayList<String> iconCacheIDs = new ArrayList<>();

    private LauncherSettings(Context c) {
        this.context = c;
        pref = c.getSharedPreferences("LauncherSettings",Context.MODE_PRIVATE);
        iconCacheIDs.clear();

        readSettings();
    }

    private void readDockData(Gson gson){
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(context.openFileInput(DockDataFileName)));
            reader.beginArray();
            while (reader.hasNext()) {
                Desktop.SimpleItem item = gson.fromJson(reader,Desktop.SimpleItem.class);
                Desktop.Item item1 = new Desktop.Item(item);
                dockData.add(item1);

                //We get all the icon cache id
                if (item1.type == Desktop.Item.Type.SHORTCUT){
                    iconCacheIDs.add(item1.actions[0].getStringExtra("shortCutIconID"));
                }
                if (item1.type == Desktop.Item.Type.GROUP){
                    for (int i = 0; i < item1.actions.length; i++) {
                        String ID;
                        if ((ID = item1.actions[i].getStringExtra("shortCutIconID")) != null){
                            iconCacheIDs.add(ID);
                        }
                    }
                }
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readDesktopData(Gson gson){
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(context.openFileInput(DesktopDataFileName)));
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginArray();
                ArrayList<Desktop.Item> items = new ArrayList<>();
                while (reader.hasNext()) {
                    Desktop.SimpleItem item = gson.fromJson(reader,Desktop.SimpleItem.class);
                    Desktop.Item item1 = new Desktop.Item(item);
                    items.add(item1);

                    //We get all the icon cache id
                    if (item.type == Desktop.Item.Type.SHORTCUT){
                        iconCacheIDs.add(item1.actions[0].getStringExtra("shortCutIconID"));
                    }
                    if (item1.type == Desktop.Item.Type.GROUP){
                        for (int i = 0; i < item1.actions.length; i++) {
                            String ID;
                            if ((ID = item1.actions[i].getStringExtra("shortCutIconID")) != null){
                                iconCacheIDs.add(ID);
                            }
                        }
                    }
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

    private void readSettings(){
        Gson gson = new Gson();

        readDockData(gson);
        readDesktopData(gson);
        Tool.checkForUnusedIconAndDelete(context, iconCacheIDs);

        String raw = Tool.getStringFromFile(GeneralSettingsFileName, context);
        if (raw == null)
            generalSettings = new GeneralSettings();
        else
            generalSettings = gson.fromJson(raw,GeneralSettings.class);
    }

    public Gson writeSettings(){
        Gson gson = new Gson();

        List<List<Desktop.SimpleItem>> simpleDesktopData = new ArrayList<>();
        List<Desktop.SimpleItem> simpleDockData = new ArrayList<>();

        for (Desktop.Item item:dockData) {
            simpleDockData.add(new Desktop.SimpleItem(item));
        }
        for (List<Desktop.Item> pages:desktopData) {
            final ArrayList<Desktop.SimpleItem> page = new ArrayList<>();
            simpleDesktopData.add(page);
            for (Desktop.Item item:pages) {
                page.add(new Desktop.SimpleItem(item));
            }
        }
        Tool.writeToFile(DockDataFileName,gson.toJson(simpleDockData), context);
        Tool.writeToFile(DesktopDataFileName,gson.toJson(simpleDesktopData), context);
        Tool.writeToFile(GeneralSettingsFileName,gson.toJson(generalSettings), context);

        return gson;
    }

    public static class GeneralSettings {
        public int desktopPageCount = 1;
        public int desktopHomePage;
        public int desktopGridx = 4;
        public int desktopGridy = 4;
        public int drawerGridx = 4;
        public int drawerGridy = 5;

        public boolean appDrawerSearchbar = true;

        public AppDrawer.DrawerMode drawerMode = AppDrawer.DrawerMode.Paged;

        public boolean rememberappdrawerpage = true;
        public boolean showsearchbar = true;

        public int drawerGridxL = 5;
        public int drawerGridyL = 3;

        public int dockGridx = 5;
        public int iconSize = 58;

        public boolean dockshowlabel = true;

        public String iconPackName = "";

        public LauncherAction.Theme theme = LauncherAction.Theme.Light;

        public ArrayList<String> minBarArrangement;
    }

}
