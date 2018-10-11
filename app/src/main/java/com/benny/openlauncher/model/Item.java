package com.benny.openlauncher.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Item implements Parcelable {

    public static final int LOCATION_DESKTOP = 0;
    public static final int LOCATION_DOCK = 1;
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
    public Type _type;
    public Drawable _icon;
    public int _location;
    public int _x = 0;
    public int _y = 0;

    // intent for shortcuts and apps
    public Intent _intent;

    // list of items for groups
    public List<Item> _items;

    // int value for launcher action
    public int _actionValue;

    // widget specific values
    public int _widgetValue;
    public int _spanX = 1;
    public int _spanY = 1;
    // all items need these values
    private int _id;
    private String _label = "";

    public Item() {
        Random random = new Random();
        _id = random.nextInt();
    }

    public Item(Parcel parcel) {
        _id = parcel.readInt();
        _type = Type.valueOf(parcel.readString());
        _label = parcel.readString();
        _x = parcel.readInt();
        _y = parcel.readInt();
        switch (_type) {
            case APP:
            case SHORTCUT:
                _intent = Tool.getIntentFromString(parcel.readString());
                break;
            case GROUP:
                List<String> items = new ArrayList<>();
                parcel.readStringList(items);
                _items = new ArrayList<>();
                for (String item : items) {
                    _items.add(HomeActivity.Companion.getLauncher()._db.getItem(Integer.parseInt(item)));
                }
                break;
            case ACTION:
                _actionValue = parcel.readInt();
                break;
            case WIDGET:
                _widgetValue = parcel.readInt();
                _spanX = parcel.readInt();
                _spanY = parcel.readInt();
                break;
        }
        _location = parcel.readInt();

        if (Setup.appSettings().enableImageCaching()) {
            _icon = Tool.getIcon(HomeActivity.Companion.getLauncher(), Integer.toString(_id));
        } else {
            switch (_type) {
                case APP:
                case SHORTCUT:
                    App app = Setup.appLoader().findItemApp(this);
                    _icon = app != null ? app.getIcon() : null;
                    break;
                default:
                    // TODO...
                    break;
            }
        }
    }

    public static Item newAppItem(App app) {
        Item item = new Item();
        item._type = Type.APP;
        item._label = app.getLabel();
        item._icon = app.getIcon();
        item._intent = Tool.getIntentFromApp(app);
        return item;
    }

    public static Item newShortcutItem(Intent intent, Drawable icon, String name) {
        Item item = new Item();
        item._type = Type.SHORTCUT;
        item._label = name;
        item._icon = icon;
        item._spanX = 1;
        item._spanY = 1;
        item._intent = intent;
        return item;
    }

    public static Item newGroupItem() {
        Item item = new Item();
        item._type = Type.GROUP;
        item._label = "";
        item._spanX = 1;
        item._spanY = 1;
        item._items = new ArrayList<>();
        return item;
    }

    public static Item newActionItem(int action) {
        Item item = new Item();
        item._type = Type.ACTION;
        item._spanX = 1;
        item._spanY = 1;
        item._actionValue = action;
        return item;
    }

    public static Item newWidgetItem(int widgetValue) {
        Item item = new Item();
        item._type = Type.WIDGET;
        item._widgetValue = widgetValue;
        item._spanX = 1;
        item._spanY = 1;
        return item;
    }

    @Override
    public boolean equals(Object object) {
        Item itemObject = (Item) object;
        return object != null && _id == itemObject._id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(_id);
        out.writeString(_type.toString());
        out.writeString(_label);
        out.writeInt(_x);
        out.writeInt(_y);
        switch (_type) {
            case APP:
            case SHORTCUT:
                out.writeString(Tool.getIntentAsString(_intent));
                break;
            case GROUP:
                List<String> items = new ArrayList<>();
                for (Item item : _items) {
                    items.add(Integer.toString(item._id));
                }
                out.writeStringList(items);
                break;
            case ACTION:
                out.writeInt(_actionValue);
                break;
            case WIDGET:
                out.writeInt(_widgetValue);
                out.writeInt(_spanX);
                out.writeInt(_spanY);
                break;
        }
        out.writeInt(_location);
    }

    public void reset() {
        Random random = new Random();
        _id = random.nextInt();
    }

    public Integer getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public Intent getIntent() {
        return _intent;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        _label = label;
    }

    public Type getType() {
        return _type;
    }

    public List<Item> getGroupItems() {
        return _items;
    }

    public int getX() {
        return _x;
    }

    public void setX(int x) {
        _x = x;
    }

    public int getY() {
        return _y;
    }

    public void setY(int y) {
        _y = y;
    }

    public int getSpanX() {
        return _spanX;
    }

    public void setSpanX(int x) {
        _spanX = x;
    }

    public int getSpanY() {
        return _spanY;
    }

    public void setSpanY(int y) {
        _spanY = y;
    }

    public Drawable getIcon() {
        return _icon;
    }

    public enum Type {
        APP,
        SHORTCUT,
        GROUP,
        ACTION,
        WIDGET
    }

    public void setType(Type type) {
        _type = type;
    }

    public void setIcon(Drawable icon) {
        _icon = icon;
    }

    public int getLocation() {
        return _location;
    }

    public void setIntent(Intent intent) {
        _intent = intent;
    }

    public List<Item> getItems() {
        return _items;
    }

    public void setItems(List<Item> items) {
        _items = items;
    }

    public int getActionValue() {
        return _actionValue;
    }

    public void setActionValue(int actionValue) {
        _actionValue = actionValue;
    }

    public int getWidgetValue() {
        return _widgetValue;
    }

    public void setWidgetValue(int widgetValue) {
        _widgetValue = widgetValue;
    }
}
