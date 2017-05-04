package com.benny.openlauncher.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.Desktop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kanadill on 5/3/2017.
 */

public class Item implements Parcelable {
    public static final Creator<Item> CREATOR = new Creator<Item>() {

        @Override
        public Item createFromParcel(Parcel parcel) {
            return new Item(parcel);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    // all items need these values
    public int idValue;
    public Type type;
    public String name;
    public int x = 0;
    public int y = 0;

    // intent for shortcuts and apps
    public Intent appIntent;

    // list of items for groups
    public List<Item> items;

    // int value for launcher action
    public int actionValue;

    // widget specific values
    public int widgetID;
    public int spanX = 1;
    public int spanY = 1;

    public Item() {
        Random random = new Random();
        idValue = random.nextInt();
    }

    public void resetID() {
        Random random = new Random();
        idValue = random.nextInt();
    }

    public Item(Parcel parcel) {
        idValue = parcel.readInt();
        type = Type.valueOf(parcel.readString());
        switch (type) {
            case APP:
            case SHORTCUT:
                appIntent = Tool.getIntentFromString(parcel.readString());
                break;
            case GROUP:
                List<String> labels = new ArrayList<>();
                parcel.readStringList(labels);
                items = new ArrayList<>();
                for (String s : labels) {
                    items.add(Home.launcher.db.getItem(Integer.parseInt(s)));
                }
                break;
            case ACTION:
                actionValue = parcel.readInt();
                break;
            case WIDGET:
                widgetID = parcel.readInt();
                spanX = parcel.readInt();
                spanY = parcel.readInt();
                break;
        }
        name = parcel.readString();
    }

    @Override
    public boolean equals(Object object) {
        Item itemObject = (Item) object;
        return object != null && this.idValue == itemObject.idValue;
    }

    public static Item newAppItem(AppManager.App app) {
        Item item = new Item();
        item.type = Type.APP;
        item.appIntent = toIntent(app);
        return item;
    }

    public static Item newWidgetItem(int widgetID) {
        Item item = new Item();
        item.type = Type.WIDGET;
        item.widgetID = widgetID;
        item.spanX = 1;
        item.spanY = 1;
        return item;
    }

    public static Item newShortcutItem(Context context, String name, Intent intent, Bitmap icon) {
        Item item = new Item();
        item.type = Type.SHORTCUT;
        item.spanX = 1;
        item.spanY = 1;
        item.appIntent = intent;

        String iconID = Tool.saveIconAndReturnID(context, icon);
        intent.putExtra("shortCutIconID", iconID);
        intent.putExtra("shortCutName", name);
        return item;
    }

    public static Item newShortcutItem(Intent intent) {
        Item item = new Item();
        item.type = Type.SHORTCUT;
        item.spanX = 1;
        item.spanY = 1;
        item.appIntent = intent;
        return item;
    }

    public static Item newActionItem(int action) {
        Item item = new Item();
        item.type = Type.ACTION;
        item.spanX = 1;
        item.spanY = 1;
        item.actionValue = action;
        return item;
    }

    public static Item newGroupItem() {
        Item item = new Item();
        item.type = Type.GROUP;
        item.spanX = 1;
        item.spanY = 1;
        item.items = new ArrayList<>();
        return item;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(idValue);
        out.writeString(type.toString());
        switch (type) {
            case APP:
            case SHORTCUT:
                out.writeString(Tool.getIntentAsString(this.appIntent));
                break;
            case GROUP:
                List<String> labels = new ArrayList<>();
                for (Item i : items) {
                    labels.add(Integer.toString(i.idValue));
                }
                out.writeStringList(labels);
                break;
            case ACTION:
                out.writeInt(actionValue);
                break;
            case WIDGET:
                out.writeInt(widgetID);
                out.writeInt(spanX);
                out.writeInt(spanY);
                break;
        }
        out.writeString(name);
    }

    private static Intent toIntent(AppManager.App app) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(app.packageName, app.className);
        return intent;
    }

    public enum Type {
        APP,
        SHORTCUT,
        GROUP,
        ACTION,
        WIDGET
    }
}
