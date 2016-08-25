package com.bennyv4.project2.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.bennyv4.project2.widget.Desktop;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class LauncherSettings {

    private static LauncherSettings ourInstance;

    public static LauncherSettings getInstance(Context c) {
        return ourInstance == null ? ourInstance = new LauncherSettings(c) : ourInstance;
    }

    public List<List<Desktop.Item>> desktopData = new ArrayList<>();

    public List<Desktop.Item> dockData = new ArrayList<>();

    private List<List<Desktop.SimpleItem>> simpleDesktopData;

    private List<Desktop.SimpleItem> simpleDockData;

    public GeneralSettings generalSettings;

    public SharedPreferences pref;

    private LauncherSettings(Context c) {
        pref = c.getSharedPreferences("launcherSettings",Context.MODE_PRIVATE);
        readSettings();
    }

    private void readSettings(){
        //pref.edit().clear().commit();

        Gson gson = new Gson();

        String raw = pref.getString("dockData",null);
        if (raw == null)
            simpleDockData = new ArrayList<>();
        else
            simpleDockData = gson.fromJson(raw,new TypeToken<ArrayList<Desktop.SimpleItem>>(){}.getType());

        raw = pref.getString("desktopData",null);
        if (raw == null)
            simpleDesktopData = new ArrayList<>();
        else
            simpleDesktopData = gson.fromJson(raw,new TypeToken<ArrayList<ArrayList<Desktop.SimpleItem>>>(){}.getType());

        raw = pref.getString("generalSettings",null);
        if (raw == null)
            generalSettings = new GeneralSettings();
        else
            generalSettings = gson.fromJson(raw,GeneralSettings.class);

        for (Desktop.SimpleItem item:simpleDockData) {
            dockData.add(new Desktop.Item(item));
        }
        for (List<Desktop.SimpleItem> pages:simpleDesktopData) {
            final ArrayList<Desktop.Item> page = new ArrayList<>();
            desktopData.add(page);
            for (Desktop.SimpleItem item:pages) {
                page.add(new Desktop.Item(item));
            }
        }
    }

    public void writeSettings(){
        Gson gson = new Gson();

        simpleDockData.clear();
        simpleDesktopData.clear();

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
        pref.edit().putString("dockData",gson.toJson(simpleDockData)).apply();
        pref.edit().putString("desktopData",gson.toJson(simpleDesktopData)).apply();
        pref.edit().putString("generalSettings",gson.toJson(generalSettings)).apply();
    }

    public static class GeneralSettings {
        public int desktopPageCount = 1;
        public int desktopHomePage;
        public int desktopGridx = 4;
        public int desktopGridy = 4;
        public int drawerGridx = 4;
        public int drawerGridy = 5;

        public int desktopGridxL = 4;
        public int desktopGridyL = 4;
        public int drawerGridxL = 5;
        public int drawerGridyL = 3;

        public int dockGridx = 5;
        public int iconSize = 58;
    }
}
