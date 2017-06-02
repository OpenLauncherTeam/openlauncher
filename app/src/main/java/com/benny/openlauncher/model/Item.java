package com.benny.openlauncher.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dkanada on 5/3/2017.
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
    public String name = Home.launcher.getString(R.string.request_dummy_text);
    public Drawable icon = Home.launcher.getResources().getDrawable(R.drawable.rip);
    public int x = 0;
    public int y = 0;

    // intent for shortcuts and apps
    public Intent intent;

    // list of items for groups
    public List<Item> items;

    // int value for launcher action
    public int actionValue;

    // widget specific values
    public int widgetValue;
    public int spanX = 1;
    public int spanY = 1;

    public Item() {
        Random random = new Random();
        idValue = random.nextInt();
    }

    public Item(Parcel parcel) {
        idValue = parcel.readInt();
        type = Type.valueOf(parcel.readString());
        name = parcel.readString();
        switch (type) {
            case APP:
            case SHORTCUT:
                intent = Tool.getIntentFromString(parcel.readString());
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
                widgetValue = parcel.readInt();
                spanX = parcel.readInt();
                spanY = parcel.readInt();
                break;
        }
        icon = Tool.getIcon(Home.launcher, Integer.toString(idValue));
    }

    @Override
    public boolean equals(Object object) {
        Item itemObject = (Item) object;
        return object != null && this.idValue == itemObject.idValue;
    }

    public static Item newAppItem(Context context, AppManager.App app) {
        Item item = new Item();
        item.type = Type.APP;
        item.name = app.label;
        item.icon = app.icon;
        item.intent = toIntent(app);
        return item;
    }

    public static Item newShortcutItem(Context context, Intent intent, Drawable icon, String name) {
        Item item = new Item();
        item.type = Type.SHORTCUT;
        item.name = name;
        item.icon = icon;
        item.spanX = 1;
        item.spanY = 1;
        item.intent = intent;
        return item;
    }

    public static Item newGroupItem() {
        Item item = new Item();
        item.type = Type.GROUP;
        item.name = (Home.launcher.getString(R.string.group));
        item.spanX = 1;
        item.spanY = 1;
        item.items = new ArrayList<>();
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

    public static Item newWidgetItem(int widgetValue) {
        Item item = new Item();
        item.type = Type.WIDGET;
        item.widgetValue = widgetValue;
        item.spanX = 1;
        item.spanY = 1;
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
        out.writeString(name);
        switch (type) {
            case APP:
            case SHORTCUT:
                out.writeString(Tool.getIntentAsString(this.intent));
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
                out.writeInt(widgetValue);
                out.writeInt(spanX);
                out.writeInt(spanY);
                break;
        }
    }

    public void reset() {
        Random random = new Random();
        idValue = random.nextInt();
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
