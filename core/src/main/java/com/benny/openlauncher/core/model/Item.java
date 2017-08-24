package com.benny.openlauncher.core.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.LabelProvider;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.util.BaseIconProvider;
import com.benny.openlauncher.core.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Item implements LabelProvider, Parcelable {

    public enum Type {
        APP,
        SHORTCUT,
        GROUP,
        ACTION,
        WIDGET
    }

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
    private int idValue;
    public Type type;
    private String name = "";
    public BaseIconProvider iconProvider = null;
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
        x = parcel.readInt();
        y = parcel.readInt();
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
        if (Setup.appSettings().enableImageCaching()) {
            iconProvider = Setup.imageLoader().createIconProvider(Tool.getIcon(Home.launcher, Integer.toString(idValue)));
        } else {
            switch (type) {
                case APP:
                case SHORTCUT:
                    App app = Setup.appLoader().findItemApp(this);
                    iconProvider = app != null ? app.getIconProvider() : null;
                    break;
                default:
                    // TODO...
                    break;
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        Item itemObject = (Item) object;
        return object != null && this.idValue == itemObject.idValue;
    }

    public static Item newAppItem(App app) {
        Item item = new Item();
        item.type = Type.APP;
        item.name = app.getLabel();
        item.iconProvider = app.getIconProvider();
        item.intent = toIntent(app);
        return item;
    }

    public static Item newShortcutItem(Intent intent, Drawable icon, String name) {
        Item item = new Item();
        item.type = Type.SHORTCUT;
        item.name = name;
        item.iconProvider = Setup.imageLoader().createIconProvider(icon);
        item.spanX = 1;
        item.spanY = 1;
        item.intent = intent;
        return item;
    }

    public static Item newGroupItem() {
        Item item = new Item();
        item.type = Type.GROUP;
        item.name = Home.launcher.getString(R.string.folder);
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
        out.writeInt(x);
        out.writeInt(y);
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

    public Integer getId() {
        return idValue;
    }

    public void setItemId(int id) {
        idValue = id;
    }

    public Intent getIntent() {
        return intent;
    }

    @Override
    public String getLabel() {
        return name;
    }

    public void setLabel(String label) {
        this.name = label;
    }

    public Type getType() {
        return type;
    }

    public List<Item> getGroupItems() {
        return items;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpanX() {
        return spanX;
    }

    public int getSpanY() {
        return spanY;
    }

    public void setSpanX(int x) {
        spanX = x;
    }

    public void setSpanY(int y) {
        spanY = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    private static Intent toIntent(App app) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(app.getPackageName(), app.getClassName());
        return intent;
    }

    public BaseIconProvider getIconProvider() {
        return iconProvider;
    }
}
