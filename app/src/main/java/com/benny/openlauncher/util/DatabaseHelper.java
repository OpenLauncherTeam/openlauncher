package com.benny.openlauncher.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.benny.openlauncher.widget.Desktop;

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

  public DatabaseHelper(Context context) {
    super(context, "openlauncher.db", null, 1);
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
    SQLiteDatabase db = getWritableDatabase();
    values.put(COLUMN_ID, item.idValue);
    values.put(COLUMN_TYPE, item.type.toString());
    values.put(COLUMN_LABEL, item.name);
    values.put(COLUMN_X_POS, item.x);
    values.put(COLUMN_Y_POS, item.y);

    String concat = "";
    switch (item.type) {
      case SHORTCUT:
        values.put(COLUMN_Y_POS, item.appIntent.toString());
        break;
      case GROUP:
        for (Desktop.Item tmp : item.items) {
          concat += tmp.idValue + "#";
        }
        values.put(COLUMN_Y_POS, concat);
        break;
      case LAUNCHER_APP_DRAWER:
        values.put(COLUMN_Y_POS, item.actionValue);
        break;
      case WIDGET:
        concat = Integer.toString(item.widgetID) + "#"
            + Integer.toString(item.spanX) + "#"
            + Integer.toString(item.spanY);
        values.put(COLUMN_Y_POS, concat);
        break;
    }
    db.insert(TABLE_ITEM, null, values);
  }
}