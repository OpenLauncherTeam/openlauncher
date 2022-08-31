package com.benny.openlauncher.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.Definitions.ItemPosition;
import com.benny.openlauncher.util.Definitions.ItemState;

import java.util.ArrayList;
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
    private static final String COLUMN_DESKTOP = "desktop";
    private static final String COLUMN_STATE = "state";

    private static final String SQL_DELETE = "DROP TABLE IF EXISTS ";
    private static final String SQL_QUERY = "SELECT * FROM ";
    private static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_HOME + " ("
                    + COLUMN_TIME + " INTEGER PRIMARY KEY,"
                    + COLUMN_TYPE + " VARCHAR,"
                    + COLUMN_LABEL + " VARCHAR,"
                    + COLUMN_X_POS + " INTEGER,"
                    + COLUMN_Y_POS + " INTEGER,"
                    + COLUMN_DATA + " VARCHAR,"
                    + COLUMN_PAGE + " INTEGER,"
                    + COLUMN_DESKTOP + " INTEGER,"
                    + COLUMN_STATE + " INTEGER)";

    protected SQLiteDatabase _db;
    protected Context _context;

    public DatabaseHelper(Context c) {
        super(c, DATABASE_HOME, null, 1);
        _db = getWritableDatabase();
        _context = c;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(SQL_DELETE + TABLE_HOME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createItem(Item item, int page, Definitions.ItemPosition itemPosition) {
        Log.i(this.getClass().getName(), String.format("createItem: %s (ID: %d)", item.getLabel(), item.getId()));
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_TIME, item.getId());
        itemValues.put(COLUMN_TYPE, item.getType().toString());
        itemValues.put(COLUMN_LABEL, item.getLabel());
        itemValues.put(COLUMN_X_POS, item.getX());
        itemValues.put(COLUMN_Y_POS, item.getY());

        String concat = "";
        switch (item.getType()) {
            case APP:
            case SHORTCUT:
                Tool.saveIcon(_context, Tool.drawableToBitmap(item.getIcon()), Integer.toString(item.getId()));
                itemValues.put(COLUMN_DATA, Tool.getIntentAsString(item.getIntent()));
                break;
            case GROUP:
                for (Item tmp : item.getItems()) {
                    if (tmp != null) {
                        concat += tmp.getId() + Definitions.DELIMITER;
                    }
                }
                itemValues.put(COLUMN_DATA, concat);
                break;
            case ACTION:
                itemValues.put(COLUMN_DATA, item.getActionValue());
                break;
            case WIDGET:
                concat = item.getWidgetValue()
                        + Definitions.DELIMITER
                        + item.getSpanX()
                        + Definitions.DELIMITER
                        + item.getSpanY();
                itemValues.put(COLUMN_DATA, concat);
                break;
        }
        itemValues.put(COLUMN_PAGE, page);
        itemValues.put(COLUMN_DESKTOP, itemPosition.ordinal());

        // item will always be visible when first added
        itemValues.put(COLUMN_STATE, 1);
        _db.insert(TABLE_HOME, null, itemValues);
    }

    public void saveItem(Item item) {
        updateItem(item);
    }

    public void saveItem(Item item, Definitions.ItemState state) {
        updateItem(item, state);
    }

    public void saveItem(Item item, int page, Definitions.ItemPosition itemPosition) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.getId();
        Cursor cursor = _db.rawQuery(SQL_QUERY_SPECIFIC, null);
        if (cursor.getCount() == 0) {
            createItem(item, page, itemPosition);
        } else if (cursor.getCount() == 1) {
            updateItem(item, page, itemPosition);
        }
    }

    public void deleteItem(Item item, boolean deleteSubItems) {
        // if the item is a group then remove all entries
        if (deleteSubItems && item.getType() == Item.Type.GROUP) {
            for (Item i : item.getGroupItems()) {
                deleteItem(i, deleteSubItems);
            }
        }

        // delete the item itself
        _db.delete(TABLE_HOME, COLUMN_TIME + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public void deleteItems(App app) {
        _db.delete(TABLE_HOME,
                COLUMN_TYPE + " = '" + Item.Type.WIDGET + "' AND " + COLUMN_LABEL + " LIKE ?",
                new String[]{app.getPackageName() + Definitions.DELIMITER + "%"});
        _db.delete(TABLE_HOME,
                COLUMN_TYPE + " = '" + Item.Type.APP + "' AND " + COLUMN_DATA + " = ?",
                new String[]{Tool.getIntentAsString(Tool.getIntentFromApp(app))});
    }

    public List<List<Item>> getDesktop() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME;
        Cursor cursor = _db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<List<Item>> desktop = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int pageColumnIndex = cursor.getColumnIndex(COLUMN_PAGE);
            int desktopColumnIndex = cursor.getColumnIndex(COLUMN_DESKTOP);
            int stateColumnIndex = cursor.getColumnIndex(COLUMN_STATE);
            do {
                int page = Integer.parseInt(cursor.getString(pageColumnIndex));
                int desktopVar = Integer.parseInt(cursor.getString(desktopColumnIndex));
                int stateVar = Integer.parseInt(cursor.getString(stateColumnIndex));
                while (page >= desktop.size()) {
                    desktop.add(new ArrayList<>());
                }
                if (desktopVar == ItemPosition.Desktop.ordinal() && stateVar == ItemState.Visible.ordinal()) {
                    desktop.get(page).add(getSelection(cursor));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return desktop;
    }

    public List<Item> getDock() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME;
        Cursor cursor = _db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<Item> dock = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int desktopColumnIndex = cursor.getColumnIndex(COLUMN_DESKTOP);
            int stateColumnIndex = cursor.getColumnIndex(COLUMN_STATE);
            do {
                int desktopVar = Integer.parseInt(cursor.getString(desktopColumnIndex));
                int stateVar = Integer.parseInt(cursor.getString(stateColumnIndex));
                if (desktopVar == ItemPosition.Dock.ordinal() && stateVar == ItemState.Visible.ordinal()) {
                    dock.add(getSelection(cursor));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dock;
    }

    public Item getItem(int id) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + id;
        Cursor cursor = _db.rawQuery(SQL_QUERY_SPECIFIC, null);
        Item item = null;
        if (cursor.moveToFirst()) {
            item = getSelection(cursor);
        }
        cursor.close();
        return item;
    }

    // update data attribute for an item
    public void updateItem(Item item) {
        Log.i(this.getClass().getName(), String.format("updateItem: %s %d", item.getLabel(), item.getId()));

        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_LABEL, item.getLabel());
        itemValues.put(COLUMN_X_POS, item.getX());
        itemValues.put(COLUMN_Y_POS, item.getY());

        String concat = "";
        switch (item.getType()) {
            case APP:
            case SHORTCUT:
                Tool.saveIcon(_context, Tool.drawableToBitmap(item.getIcon()), Integer.toString(item.getId()));
                itemValues.put(COLUMN_DATA, Tool.getIntentAsString(item.getIntent()));
                break;
            case GROUP:
                for (Item tmp : item.getItems()) {
                    concat += tmp.getId() + Definitions.DELIMITER;
                }
                itemValues.put(COLUMN_DATA, concat);
                break;
            case ACTION:
                itemValues.put(COLUMN_DATA, item.getActionValue());
                break;
            case WIDGET:
                concat = item.getWidgetValue()
                        + Definitions.DELIMITER
                        + item.getSpanX()
                        + Definitions.DELIMITER
                        + item.getSpanY();
                itemValues.put(COLUMN_DATA, concat);
                break;
        }
        _db.update(TABLE_HOME, itemValues, COLUMN_TIME + " = " + item.getId(), null);
    }

    // update the state of an item
    public void updateItem(Item item, Definitions.ItemState state) {
        Log.i(this.getClass().getName(), String.format("updateItem: %s %d", item.getLabel(), item.getId()));

        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_STATE, state.ordinal());

        _db.update(TABLE_HOME, itemValues, COLUMN_TIME + " = " + item.getId(), null);
    }

    // update the fields only used by the database
    public void updateItem(Item item, int page, Definitions.ItemPosition itemPosition) {
        Log.i(this.getClass().getName(), String.format("updateItem: %s %d", item.getLabel(), item.getId()));

        deleteItem(item, false);
        createItem(item, page, itemPosition);
    }

    private Item getSelection(Cursor cursor) {
        Item item = new Item();
        int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
        Item.Type type = Item.Type.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
        String label = cursor.getString(cursor.getColumnIndex(COLUMN_LABEL));
        int x = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_X_POS)));
        int y = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_Y_POS)));
        String data = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));

        item.setId(id);
        item.setLabel(label);
        item.setX(x);
        item.setY(y);
        item.setType(type);

        String[] dataSplit;
        switch (type) {
            case APP: {
                item.setIntent(Tool.getIntentFromString(data));
                item.setShortcutInfo(Tool.getShortcutInfo(_context, item.getIntent().getComponent().getPackageName()));
                App app = Setup.get().getAppLoader().findItemApp(item);
                item.setIcon(app != null ? app.getIcon() : null);
                break;
            }
            case SHORTCUT: {
                item.setIntent(Tool.getIntentFromString(data));
                item.setIcon(Tool.getIcon(_context, Integer.toString(item.getId())));
                if (item.getIcon() == null) {
                    App app = Setup.get().getAppLoader().findItemApp(item);
                    item.setIcon(app != null ? app.getIcon() : null);
                }
                break;
            }
            case GROUP: {
                item.setItems(new ArrayList<>());
                dataSplit = data.split(Definitions.DELIMITER);
                for (String string : dataSplit) {
                    if (string.isEmpty()) continue;
                    Item groupItem = getItem(Integer.parseInt(string));
                    if (groupItem != null) {
                        item.getItems().add(groupItem);
                    }
                }
                break;
            }
            case ACTION: {
                item.setActionValue(Integer.parseInt(data));
                break;
            }
            case WIDGET: {
                dataSplit = data.split(Definitions.DELIMITER);
                item.setWidgetValue(Integer.parseInt(dataSplit[0]));
                item.setSpanX(Integer.parseInt(dataSplit[1]));
                item.setSpanY(Integer.parseInt(dataSplit[2]));
                break;
            }
        }

        return item;
    }

    public void addPage(int position) {
        _db.execSQL("UPDATE " + TABLE_HOME + " SET " + COLUMN_PAGE + " = " + COLUMN_PAGE + " + 1 WHERE " + COLUMN_PAGE + " >= ?",
                new String[] {String.valueOf(position)});
    }

    public void removePage(int position) {
        _db.execSQL("UPDATE " + TABLE_HOME + " SET " + COLUMN_PAGE + " = " + COLUMN_PAGE + " - 1 WHERE " + COLUMN_PAGE + " > ?",
                new String[] {String.valueOf(position)});
    }

    public void open() {
        _db = getWritableDatabase();
    }
}
