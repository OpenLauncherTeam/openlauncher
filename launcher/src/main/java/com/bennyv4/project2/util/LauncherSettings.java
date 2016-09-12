package com.bennyv4.project2.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.bennyv4.project2.widget.Desktop;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
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

    public Context c;

    private static final String DesktopDataFileName = "desktopData.json";
    private static final String DockDataFileName = "dockData.json";
    private static final String GeneralSettingsFileName = "generalSettings.json";

    private LauncherSettings(Context c) {
        this.c = c;
        pref = c.getSharedPreferences("LauncherSettings",Context.MODE_PRIVATE);
        readSettings();
    }

    private void readDockData(Gson gson){
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(c.openFileInput(DockDataFileName)));
            reader.beginArray();
            while (reader.hasNext()) {
                Desktop.SimpleItem item = gson.fromJson(reader,Desktop.SimpleItem.class);
                dockData.add(new Desktop.Item(item));
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readDesktopData(Gson gson){
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(c.openFileInput(DesktopDataFileName)));
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginArray();
                ArrayList<Desktop.Item> items = new ArrayList<>();
                while (reader.hasNext()) {
                    Desktop.SimpleItem item = gson.fromJson(reader,Desktop.SimpleItem.class);
                    items.add(new Desktop.Item(item));
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
        //pref.edit().clear().commit();

        Gson gson = new Gson();

        readDockData(gson);
        readDesktopData(gson);

        String raw = Tools.getStringFromFile(GeneralSettingsFileName,c);
        if (raw == null)
            generalSettings = new GeneralSettings();
        else
            generalSettings = gson.fromJson(raw,GeneralSettings.class);
    }

    public void writeSettings(){
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
        Tools.writeToFile(DockDataFileName,gson.toJson(simpleDockData),c);
        Tools.writeToFile(DesktopDataFileName,gson.toJson(simpleDesktopData),c);
        Tools.writeToFile(GeneralSettingsFileName,gson.toJson(generalSettings),c);
    }

    public static class GeneralSettings {
        public int desktopPageCount = 1;
        public int desktopHomePage;
        public int desktopGridx = 4;
        public int desktopGridy = 4;
        public int drawerGridx = 4;
        public int drawerGridy = 5;

        public boolean rememberappdrawerpage = true;

        public int drawerGridxL = 5;
        public int drawerGridyL = 3;

        public int dockGridx = 5;
        public int iconSize = 58;

        public LauncherAction.Theme theme = LauncherAction.Theme.Light;

        public ArrayList<String> minBarArrangement;
    }

}
