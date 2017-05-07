package com.benny.openlauncher.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;
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

    public void setDesktopMode(int position) {
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

        AppSettings.get().setDesktopPageCurrent(position);

        Tool.checkForUnusedIconAndDelete(context, iconCacheIDs);

        Home.launcher.desktop.initDesktopShowAll(context);
    }

    // edit this carefully as changing the type of a field will cause a parsing error when the launcher starts
    public static class GeneralSettings {
        // app drawer
        public ArrayList<String> hiddenList;
    }
}
