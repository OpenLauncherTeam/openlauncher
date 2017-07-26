package com.benny.openlauncher.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.benny.openlauncher.core.util.*;

public class DatabaseHelper extends BaseDatabaseHelper {

    public DatabaseHelper(Context c) {
        super(c);
    }

    public LauncherAction.ActionItem getGesture(int value) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_GESTURE + " WHERE " + COLUMN_TIME + " = " + value;
        Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        LauncherAction.ActionItem item = null;
        if (cursor.moveToFirst()) {
            LauncherAction.Action type = LauncherAction.Action.valueOf(cursor.getString(1));
            Intent intent = com.benny.openlauncher.core.util.Tool.getIntentFromString(cursor.getString(2));
            item = new LauncherAction.ActionItem(type, intent);
        }
        cursor.close();
        return item;
    }

    public void setGesture(int id, LauncherAction.ActionItem actionItem) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, id);
        values.put(COLUMN_TYPE, actionItem.action.toString());
        values.put(COLUMN_DATA, com.benny.openlauncher.core.util.Tool.getIntentAsString(actionItem.extraData));

        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_GESTURE + " WHERE " + COLUMN_TIME + " = " + id;
        Cursor cursorItem = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        if (cursorItem.getCount() == 0) {
            db.insert(TABLE_GESTURE, null, values);
        } else if (cursorItem.getCount() == 1) {
            db.update(TABLE_GESTURE, values, COLUMN_TIME + " = " + id, null);
        }
    }
}
