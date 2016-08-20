package com.bennyv4.project2.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.bennyv4.project2.widget.Desktop;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class LauncherSettings {

    private static LauncherSettings ourInstance;

    public static LauncherSettings getInstance(Context c) {
        return ourInstance == null ? ourInstance = new LauncherSettings(c) : ourInstance;
    }

    public List<List<Desktop.Item>> desktopData;

    public List<Desktop.Item> dockData;

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
            dockData = new ArrayList<>();
        else
            dockData = gson.fromJson(raw,new TypeToken<ArrayList<Desktop.Item>>(){}.getType());

        raw = pref.getString("desktopData",null);
        if (raw == null)
            desktopData = new ArrayList<>();
        else
            desktopData = gson.fromJson(raw,new TypeToken<ArrayList<ArrayList<Desktop.Item>>>(){}.getType());

        raw = pref.getString("generalSettings",null);
        if (raw == null)
            generalSettings = new GeneralSettings();
        else
            generalSettings = gson.fromJson(raw,GeneralSettings.class);

        System.out.print(raw);
    }

    public void writeSettings(){
        Gson gson = new Gson();
        pref.edit().putString("dockData",gson.toJson(dockData)).apply();
        pref.edit().putString("desktopData",gson.toJson(desktopData)).apply();
        pref.edit().putString("generalSettings",gson.toJson(generalSettings)).apply();
    }

    public static class GeneralSettings {
        public int desktopPageCount = 1;
        public int desktopHomePage;
        public int iconSize = 58;
    }
}
