package com.benny.openlauncher.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.benny.openlauncher.widget.Desktop;

import java.util.ArrayList;

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

  private static final String SQL_CREATE_DESKTOP =
      "CREATE TABLE " + DatabaseHelper.TABLE_DESKTOP + " (" +
          DatabaseHelper.COLUMN_ID + " INTEGER PRIMARY KEY)";
  private static final String SQL_CREATE_DOCK =
      "CREATE TABLE " + DatabaseHelper.TABLE_DOCK + " (" +
          DatabaseHelper.COLUMN_ID + " INTEGER PRIMARY KEY)";
  private static final String SQL_CREATE_ITEM =
      "CREATE TABLE " + DatabaseHelper.TABLE_ITEM + " (" +
          DatabaseHelper.COLUMN_ID + " INTEGER PRIMARY KEY" +
          DatabaseHelper.COLUMN_TYPE + " TEXT PRIMARY KEY" +
          DatabaseHelper.COLUMN_LABEL + " TEXT PRIMARY KEY" +
          DatabaseHelper.COLUMN_X_POS + " INTEGER PRIMARY KEY" +
          DatabaseHelper.COLUMN_Y_POS + " INTEGER PRIMARY KEY" +
          DatabaseHelper.COLUMN_DATA + " TEXT PRIMARY KEY";
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
    // create table for all items
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

  public void addItem(boolean desktop, Desktop.Item item) {
    ContentValues itemValues = new ContentValues();
    itemValues.put(COLUMN_ID, item.idValue);
    itemValues.put(COLUMN_TYPE, item.type.toString());
    itemValues.put(COLUMN_LABEL, item.name);
    itemValues.put(COLUMN_X_POS, item.x);
    itemValues.put(COLUMN_Y_POS, item.y);

    String concat = "";
    switch (item.type) {
      case SHORTCUT:
        // TODO: might need to fix this - need testing
        itemValues.put(COLUMN_DATA, item.appIntent.toString());
        break;
      case GROUP:
        for (String tmp : item.items) {
          concat += tmp + "#";
        }
        itemValues.put(COLUMN_DATA, concat);
        // TODO: update individual items in group
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
    db.insert(TABLE_ITEM, null, itemValues);

    // adds reference to home
    ContentValues homeValues = new ContentValues();
    itemValues.put(COLUMN_ID, item.idValue);
    if (desktop) {
      db.insert(TABLE_DESKTOP, null, homeValues);
    } {
      db.insert(TABLE_DOCK, null, homeValues);
    }
  }

  public void removeItem(Desktop.Item item) {
    db.delete(TABLE_ITEM, COLUMN_ID + " = ?", new String[]{String.valueOf(item.idValue)});
    db.delete(TABLE_DESKTOP, COLUMN_ID + " = ?", new String[]{String.valueOf(item.idValue)});
    db.delete(TABLE_DOCK, COLUMN_ID + " = ?", new String[]{String.valueOf(item.idValue)});
    db.close();
  }

  public void updateItem(Desktop.Item item) {
    // removes the item and creates a new one in the database
    // TODO: finish this
    removeItem(item);
    db.close();
  }

  public ArrayList<Desktop.Item> getDesktop(PackageManager pm) {
    ArrayList<Desktop.Item> desktop = new ArrayList<>();
    String SQL_QUERY_ALL = SQL_QUERY + TABLE_DESKTOP;
    Cursor cursorOne = db.rawQuery(SQL_QUERY_ALL, null);
    if (cursorOne.getCount() != 0) {
      cursorOne.moveToFirst();
      do {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_ITEM + " WHERE " + COLUMN_ID + " = " + cursorOne.getString(0);
        Cursor cursorTwo = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        int id = Integer.parseInt(cursorTwo.getString(0));
        String type = cursorTwo.getString(1);
        String label = cursorTwo.getString(2);
        int x = Integer.parseInt(cursorTwo.getString(3));
        int y = Integer.parseInt(cursorTwo.getString(4));
        String data = cursorTwo.getString(5);
        cursorTwo.close();

        Desktop.Item tmp = new Desktop.Item();
        tmp.idValue = id;
        tmp.name = label;
        tmp.x = x;
        tmp.y = y;

        // TODO: add data to item
        String[] dataSplit;
        switch (type) {
          case "SHORTCUT":
            tmp.type = Desktop.Item.Type.SHORTCUT;
            tmp.appIntent = pm.getLaunchIntentForPackage(data);
            break;
          case "GROUP":
            tmp.type = Desktop.Item.Type.GROUP;
            dataSplit = data.split("#");
            for (String tmpString : dataSplit) {
              tmp.items.add(tmpString);
            }
            break;
          case "ACTION":
            tmp.type = Desktop.Item.Type.ACTION;
            // grab the action value
            tmp.actionValue = Integer.parseInt(data);
            break;
          case "WIDGET":
            tmp.type = Desktop.Item.Type.WIDGET;
            // split the data into widgetID, spanX, and spanY
            dataSplit = data.split("#");
            tmp.widgetID = Integer.parseInt(dataSplit[0]);
            tmp.spanX = Integer.parseInt(dataSplit[1]);
            tmp.spanY = Integer.parseInt(dataSplit[2]);
            break;
        }
        desktop.add(tmp);
      } while (cursorOne.moveToNext());
      cursorOne.close();
    }
    return desktop;
  }
}