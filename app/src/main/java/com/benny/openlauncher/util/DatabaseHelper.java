package com.benny.openlauncher.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.Desktop.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_HOME = "home.db";
    private static final String TABLE_HOME = "home";

    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_X_POS = "x";
    private static final String COLUMN_Y_POS = "y";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_PAGE = "page";
    private static final String COLUMN_STATE = "state";

    private static final String SQL_CREATE_DESKTOP =
            "CREATE TABLE " + TABLE_HOME + " (" +
                    COLUMN_TIME + " INTEGER PRIMARY KEY," +
                    COLUMN_TYPE + " VARCHAR," +
                    COLUMN_LABEL + " VARCHAR," +
                    COLUMN_X_POS + " INTEGER," +
                    COLUMN_Y_POS + " INTEGER," +
                    COLUMN_DATA + " VARCHAR," +
                    COLUMN_PAGE + " INTEGER," +
                    COLUMN_STATE + " INTEGER)";
    private static final String SQL_DELETE = "DROP TABLE IF EXISTS ";
    private static final String SQL_QUERY = "SELECT * FROM ";
    private SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_HOME, null, 1);
        db = getWritableDatabase();
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DESKTOP);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(SQL_DELETE + TABLE_HOME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createItem(Item item, int page, int state) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_TIME, item.idValue);
        itemValues.put(COLUMN_TYPE, item.type.toString());
        itemValues.put(COLUMN_LABEL, item.name);
        itemValues.put(COLUMN_X_POS, item.x);
        itemValues.put(COLUMN_Y_POS, item.y);

        String concat = "";
        switch (item.type) {
            case APP:
                itemValues.put(COLUMN_DATA, Tool.getIntentAsString(item.appIntent));
                break;
            case GROUP:
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
        itemValues.put(COLUMN_STATE, state);
        db.insert(TABLE_HOME, null, itemValues);
    }

    public void deleteItem(Item item) {
        db.delete(TABLE_HOME, COLUMN_TIME + " = ?", new String[]{String.valueOf(item.idValue)});
    }

    public void updateItem(Item item, int page, int state) {
        deleteItem(item);
        item.idValue = (int) new Date().getTime();
        createItem(item, page, state);
    }

    public List<List<Item>> getDesktop() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME;
        Cursor cursor = db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<List<Item>> desktop = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int page = Integer.parseInt(cursor.getString(6));
                int state = Integer.parseInt(cursor.getString(7));
                if (page >= desktop.size()) {
                    desktop.add(new ArrayList<Item>());
                }
                if (state == 0) {
                    desktop.get(page).add(getSelectionItem(cursor));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return desktop;
    }

    public List<Item> getDock() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME;
        Cursor cursor = db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<Item> dock = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int state = Integer.parseInt(cursor.getString(7));
                if (state == 1) {
                    dock.add(getSelectionItem(cursor));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        Tool.print("database : dock size is ", dock.size());
        return dock;
    }

    public void setDesktop(List<List<Item>> desktop) {
        for (List<Item> page : desktop) {
            int pageCounter = 0;
            for (Item item : page) {
                String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.idValue;
                Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
                if (cursor.getCount() == 0) {
                    createItem(item, pageCounter, 0);
                } else if (cursor.getCount() == 1) {
                    updateItem(item, pageCounter, 0);
                }
            }
            pageCounter++;
        }
    }

    public void setDock(List<Item> dock) {
        for (Item item : dock) {
            String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.idValue;
            Cursor cursorItem = db.rawQuery(SQL_QUERY_SPECIFIC, null);
            if (cursorItem.getCount() == 0) {
                createItem(item, 0, 1);
            } else if (cursorItem.getCount() == 1) {
                updateItem(item, 0, 1);
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
                selectionItem.appIntent = Tool.getIntentFromString(data);
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

    // the methods below are used for updating app states for groups
    public void showItem(String string) {
    }

    public void hideItem(String string) {
    }

    public Item getItem(String itemID) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + itemID;
        Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        Item item = getSelectionItem(cursor);
        cursor.close();
        return item;
    }
}