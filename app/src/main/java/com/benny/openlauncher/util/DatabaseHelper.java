package com.benny.openlauncher.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.Desktop.Item;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE_DESKTOP = "desktop";
    private static final String TABLE_DOCK = "dock";
    private static final String TABLE_ITEM = "item";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_X_POS = "xPosition";
    private static final String COLUMN_Y_POS = "yPosition";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_PAGE = "page";

    private static final String SQL_CREATE_DESKTOP =
            "CREATE TABLE " + TABLE_DESKTOP + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_TYPE + " TEXT," +
                    COLUMN_LABEL + " TEXT," +
                    COLUMN_X_POS + " INTEGER," +
                    COLUMN_Y_POS + " INTEGER," +
                    COLUMN_DATA + " TEXT," +
                    COLUMN_PAGE + " INTEGER)";
    private static final String SQL_CREATE_DOCK =
            "CREATE TABLE " + TABLE_DOCK + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_TYPE + " TEXT," +
                    COLUMN_LABEL + " TEXT," +
                    COLUMN_X_POS + " INTEGER," +
                    COLUMN_Y_POS + " INTEGER," +
                    COLUMN_DATA + " TEXT," +
                    COLUMN_PAGE + " INTEGER)";
    private static final String SQL_CREATE_ITEM =
            "CREATE TABLE " + TABLE_ITEM + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_TYPE + " TEXT," +
                    COLUMN_LABEL + " TEXT," +
                    COLUMN_X_POS + " INTEGER," +
                    COLUMN_Y_POS + " INTEGER," +
                    COLUMN_DATA + " TEXT," +
                    COLUMN_PAGE + " INTEGER)";
    private static final String SQL_DELETE = "DROP TABLE IF EXISTS ";
    private static final String SQL_QUERY = "SELECT * FROM ";
    private SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, "launcher.db", null, 1);
        db = getWritableDatabase();
    }

    public void onCreate(SQLiteDatabase db) {
        // create tables for desktop and dock
        db.execSQL(SQL_CREATE_DESKTOP);
        db.execSQL(SQL_CREATE_DOCK);

        // create table for other items
        db.execSQL(SQL_CREATE_ITEM);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(SQL_DELETE + TABLE_DESKTOP);
        db.execSQL(SQL_DELETE + TABLE_DOCK);
        db.execSQL(SQL_DELETE + TABLE_ITEM);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createItem(Item item, int page, boolean desktop) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_ID, item.idValue);
        switch (item.type) {
            case APP:
                itemValues.put(COLUMN_TYPE, "SHORTCUT");
                break;
            default:
                itemValues.put(COLUMN_TYPE, item.type.toString());
                break;
        }
        itemValues.put(COLUMN_LABEL, item.name);
        itemValues.put(COLUMN_X_POS, item.x);
        itemValues.put(COLUMN_Y_POS, item.y);

        String concat = "";
        switch (item.type) {
            case APP:
                itemValues.put(COLUMN_DATA, getIntentAsString(item.appIntent));
                break;
            case GROUP:
                String[] array = item.items.toArray(new String[0]);
                hideItem(array);
                for (Desktop.Item tmp : item.items) {
                    concat += tmp.idValue + "#";
                }
                itemValues.put(COLUMN_DATA, concat);
                break;
            case ACTION:
                itemValues.put(COLUMN_DATA, item.actionValue);
                break;
            case WIDGET:
                concat = Integer.toString(item.widgetID) + "#"
                        + Integer.toString(item.spanX) + "#"
                        + Integer.toString(item.spanY);
                itemValues.put(COLUMN_DATA, concat);
                break;
        }
        itemValues.put(COLUMN_PAGE, page);

        // insert into database
        if (desktop) {
            db.insert(TABLE_DESKTOP, null, itemValues);
        } else {
            db.insert(TABLE_DOCK, null, itemValues);
        }
    }

    public void deleteItem(Item item) {
        db.delete(TABLE_DESKTOP, COLUMN_ID + " = ?", new String[]{String.valueOf(item.idValue)});
        db.delete(TABLE_DOCK, COLUMN_ID + " = ?", new String[]{String.valueOf(item.idValue)});
        db.delete(TABLE_ITEM, COLUMN_ID + " = ?", new String[]{String.valueOf(item.idValue)});
    }

    // TODO
    public void showItem(String[] item, boolean desktop) {
        for (String string : item) {
            ContentValues homeValues = new ContentValues();
            homeValues.put(COLUMN_ID, string);
            if (desktop) {
                db.insert(TABLE_DESKTOP, null, homeValues);
            } else {
                db.insert(TABLE_DOCK, null, homeValues);
            }
        }
    }

    // TODO
    public void hideItem(String[] item) {
        db.delete(TABLE_DESKTOP, COLUMN_ID + " = ?", item);
        db.delete(TABLE_DOCK, COLUMN_ID + " = ?", item);
    }

    public List<List<Item>> getDesktop() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_DESKTOP;
        Cursor cursor = db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<List<Item>> desktop = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int page = Integer.parseInt(cursor.getString(6));
                if (page >= desktop.size()) {
                    desktop.add(new ArrayList<Item>());
                }
                desktop.get(page).add(getSelectionItem(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return desktop;
    }

    public List<Item> getDock() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_DOCK;
        Cursor cursor = db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<Item> dock = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                dock.add(getSelectionItem(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Tool.print("Init : ", dock.size());
        return dock;
    }

    public void setDesktop(List<List<Item>> desktop) {
        for (List<Item> page : desktop) {
            int pageCounter = 0;
            for (Item item : page) {
                String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_DESKTOP + " WHERE " + COLUMN_ID + " = " + item.idValue;
                Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
                if (cursor.getCount() == 0) {
                    createItem(item, pageCounter, true);
                }
            }
            pageCounter++;
        }
    }

    public void setDock(List<Item> dock) {
        for (Item item : dock) {
            String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_DOCK + " WHERE " + COLUMN_ID + " = " + item.idValue;
            Cursor cursorItem = db.rawQuery(SQL_QUERY_SPECIFIC, null);
            if (cursorItem.getCount() == 0) {
                createItem(item, 0, false);
            }
        }
    }

    private Item getSelectionItem(Cursor cursor) {
        Item selectionItem = new Item();
        int id = Integer.parseInt(cursor.getString(0));
        Item.Type type = Item.Type.valueOf(cursor.getString(1));
        String label = cursor.getString(2);
        int x = Integer.parseInt(cursor.getString(3));
        int y = Integer.parseInt(cursor.getString(4));
        String data = cursor.getString(5);

        selectionItem.idValue = id;
        selectionItem.name = label;
        selectionItem.x = x;
        selectionItem.y = y;
        selectionItem.type = type;

        String[] dataSplit;
        switch (type) {
            case APP:
            case SHORTCUT:
                selectionItem.appIntent = getIntentFromString(data);
                break;
            case GROUP:
                dataSplit = data.split("#");
                for (String s : dataSplit) {
                    selectionItem.items.add(getItem(s));
                }
                break;
            case ACTION:
                selectionItem.actionValue = Integer.parseInt(data);
                break;
            case WIDGET:
                dataSplit = data.split("#");
                selectionItem.widgetID = Integer.parseInt(dataSplit[0]);
                selectionItem.spanX = Integer.parseInt(dataSplit[1]);
                selectionItem.spanY = Integer.parseInt(dataSplit[2]);
                break;
        }
        return selectionItem;
    }

    public Item getItem(String itemID) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_ITEM + " WHERE " + COLUMN_ID + " = " + itemID;
        Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        Item item = getSelectionItem(cursor);
        cursor.close();
        return item;
    }

    private String getIntentAsString(Intent intent) {
        return intent.toUri(0);
    }

    private static Intent getIntentFromString(String string) {
        if (string == null || string.isEmpty()) {
            return new Intent();
        } else {
            try {
                return new Intent().parseUri(string, 0);
            } catch (URISyntaxException e) {
                return new Intent();
            }
        }
    }
}