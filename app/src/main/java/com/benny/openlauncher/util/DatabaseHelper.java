package com.benny.openlauncher.util;

import android.content.ContentValues;
import android.content.Context;
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

  public void addItem(Desktop.Item item) {
    ContentValues values = new ContentValues();
    values.put(COLUMN_ID, item.idValue);
    values.put(COLUMN_TYPE, item.type.toString());
    values.put(COLUMN_LABEL, item.name);
    values.put(COLUMN_X_POS, item.x);
    values.put(COLUMN_Y_POS, item.y);

    String concat = "";
    switch (item.type) {
      case SHORTCUT:
        values.put(COLUMN_DATA, item.appIntent.toString());
        break;
      case GROUP:
        for (Desktop.Item tmp : item.items) {
          concat += tmp.idValue + "#";
        }
        values.put(COLUMN_DATA, concat);
        break;
      case ACTION:
        values.put(COLUMN_DATA, item.actionValue);
        break;
      case WIDGET:
        concat = Integer.toString(item.widgetID) + "#"
            + Integer.toString(item.spanX) + "#"
            + Integer.toString(item.spanY);
        values.put(COLUMN_DATA, concat);
        break;
    }
    db.insert(TABLE_ITEM, null, values);
  }

  public void removeItem(Desktop.Item item) {
    db.delete(TABLE_ITEM, COLUMN_ID + " = ?", new String[]{String.valueOf(item.idValue)});
    db.close();
  }

  public void updateItem(Desktop.Item item) {
    db.delete(TABLE_ITEM, COLUMN_ID + " = ?", new String[]{String.valueOf(item.idValue)});
    db.close();
  }

  public ArrayList<Desktop.Item> getDesktop() {
    ArrayList<Desktop.Item> desktop = new ArrayList<>();
    String SQL_QUERY_ALL = SQL_QUERY + TABLE_DESKTOP;
    Cursor cursor = db.rawQuery(SQL_QUERY_ALL, null);
    if (cursor.getCount() != 0) {
      cursor.moveToFirst();
      do {
        int id = Integer.parseInt(cursor.getString(0));
        String typeString = cursor.getString(1);
        String label = cursor.getString(2);
        int x = Integer.parseInt(cursor.getString(3));
        int y = Integer.parseInt(cursor.getString(4));
        String data = cursor.getString(5);

        Desktop.Item tmp = new Desktop.Item();
        tmp.idValue = id;
        tmp.name = label;
        tmp.x = x;
        tmp.y = y;

        switch (typeString) {
          case "SHORTCUT":
            tmp.type = Desktop.Item.Type.SHORTCUT;
            break;
          case "GROUP":
            tmp.type = Desktop.Item.Type.GROUP;
            break;
          case "ACTION":
            tmp.type = Desktop.Item.Type.ACTION;
            break;
          case "WIDGET":
            tmp.type = Desktop.Item.Type.WIDGET;
            break;
        }
      } while (cursor.moveToNext());
    }
    return desktop;
  }
}