package com.benny.openlauncher.core.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;

import java.util.ArrayList;
import java.util.List;

public class BaseDatabaseHelper extends SQLiteOpenHelper implements Setup.DataManager {
    protected static final String DATABASE_HOME = "home.db";
    protected static final String TABLE_HOME = "home";
    protected static final String TABLE_GESTURE = "gesture";

    protected static final String COLUMN_TIME = "time";
    protected static final String COLUMN_TYPE = "type";
    protected static final String COLUMN_LABEL = "label";
    protected static final String COLUMN_X_POS = "x";
    protected static final String COLUMN_Y_POS = "y";
    protected static final String COLUMN_DATA = "data";
    protected static final String COLUMN_PAGE = "page";
    protected static final String COLUMN_DESKTOP = "desktop";
    protected static final String COLUMN_STATE = "state";

    protected static final String SQL_CREATE_HOME =
            "CREATE TABLE " + TABLE_HOME + " (" +
                    COLUMN_TIME + " INTEGER PRIMARY KEY," +
                    COLUMN_TYPE + " VARCHAR," +
                    COLUMN_LABEL + " VARCHAR," +
                    COLUMN_X_POS + " INTEGER," +
                    COLUMN_Y_POS + " INTEGER," +
                    COLUMN_DATA + " VARCHAR," +
                    COLUMN_PAGE + " INTEGER," +
                    COLUMN_DESKTOP + " INTEGER," +
                    COLUMN_STATE + " INTEGER)";
    protected static final String SQL_CREATE_GESTURE =
            "CREATE TABLE " + TABLE_GESTURE + " (" +
                    COLUMN_TIME + " INTEGER PRIMARY KEY," +
                    COLUMN_TYPE + " VARCHAR," +
                    COLUMN_DATA + " VARCHAR)";
    protected static final String SQL_DELETE = "DROP TABLE IF EXISTS ";
    protected static final String SQL_QUERY = "SELECT * FROM ";
    protected SQLiteDatabase db;
    protected Context context;

    public BaseDatabaseHelper(Context c) {
        super(c, DATABASE_HOME, null, 1);
        db = getWritableDatabase();
        context = c;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_HOME);
        db.execSQL(SQL_CREATE_GESTURE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(SQL_DELETE + TABLE_HOME);
        db.execSQL(SQL_DELETE + TABLE_GESTURE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createItem(Item item, int page, Definitions.ItemPosition itemPosition) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_TIME, item.getId());
        itemValues.put(COLUMN_TYPE, item.type.toString());
        itemValues.put(COLUMN_LABEL, item.getLabel());
        itemValues.put(COLUMN_X_POS, item.x);
        itemValues.put(COLUMN_Y_POS, item.y);

        Setup.logger().log(this, Log.INFO, null, "createItem: %s (ID: %d)", item != null ? item.getLabel() : "NULL", item != null ? item.getId() : -1);

        String concat = "";
        switch (item.type) {
            case APP:
                if (Setup.appSettings().enableImageCaching()) {
                    Tool.saveIcon(context, Tool.drawableToBitmap(item.getIconProvider().getDrawableSynchronously(-1)), Integer.toString(item.getId()));
                }
                itemValues.put(COLUMN_DATA, Tool.getIntentAsString(item.intent));
                break;
            case GROUP:
                for (Item tmp : item.items) {
                    concat += tmp.getId() + Definitions.INT_SEP;
                }
                itemValues.put(COLUMN_DATA, concat);
                break;
            case ACTION:
                itemValues.put(COLUMN_DATA, item.actionValue);
                break;
            case WIDGET:
                concat = Integer.toString(item.widgetValue) + Definitions.INT_SEP
                        + Integer.toString(item.spanX) + Definitions.INT_SEP
                        + Integer.toString(item.spanY);
                itemValues.put(COLUMN_DATA, concat);
                break;
        }
        itemValues.put(COLUMN_PAGE, page);
        itemValues.put(COLUMN_DESKTOP, itemPosition.ordinal());

        // item will always be visible when first added
        itemValues.put(COLUMN_STATE, 1);
        db.insert(TABLE_HOME, null, itemValues);
    }

    @Override
    public void saveItem(Item item) {
        updateItem(item);
    }

    @Override
    public void saveItem(Item item, int page, Definitions.ItemPosition itemPosition) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.getId();
        Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        if (cursor.getCount() == 0) {
            createItem(item, page, itemPosition);
        } else if (cursor.getCount() == 1) {
            updateItem(item, page, itemPosition);
        }
    }

    @Override
    public void updateSate(Item item, Definitions.ItemState state) {
        updateItem(item, state);
    }

    @Override
    public void deleteItem(Item item, boolean deleteSubItems) {
        // 1) if item is a folder => delete all sub items
        if (deleteSubItems && item.getType() == Item.Type.GROUP) {
            for (Item i : item.getGroupItems()) {
                deleteItem(i, deleteSubItems);
            }
        }
        // 2) delete the item itself
        db.delete(TABLE_HOME, COLUMN_TIME + " = ?", new String[]{String.valueOf(item.getId())});
    }

    @Override
    public List<List<Item>> getDesktop() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME;
        Cursor cursor = db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<List<Item>> desktop = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int page = Integer.parseInt(cursor.getString(6));
                int desktopVar = Integer.parseInt(cursor.getString(7));
                int stateVar = Integer.parseInt(cursor.getString(8));
                while (page >= desktop.size()) {
                    desktop.add(new ArrayList<Item>());
                }
                if (desktopVar == 1 && stateVar == 1) {
                    desktop.get(page).add(getSelectionItem(cursor));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return desktop;
    }

    @Override
    public List<Item> getDock() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME;
        Cursor cursor = db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<Item> dock = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int desktopVar = Integer.parseInt(cursor.getString(7));
                int stateVar = Integer.parseInt(cursor.getString(8));
                if (desktopVar == 0 && stateVar == 1) {
                    dock.add(getSelectionItem(cursor));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        Tool.print("database : dock size is ", dock.size());
        return dock;
    }

    @Override
    public Item getItem(int id) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + id;
        Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        Item item = null;
        if (cursor.moveToFirst()) {
            item = getSelectionItem(cursor);
        }
        cursor.close();
        return item;
    }

    public void setDesktop(List<List<Item>> desktop) {
        int pageCounter = 0;
        for (List<Item> page : desktop) {
            for (Item item : page) {
                String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.getId();
                Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
                if (cursor.getCount() == 0) {
                    createItem(item, pageCounter, Definitions.ItemPosition.Desktop);
                } else if (cursor.getCount() == 1) {
                    updateItem(item, pageCounter, Definitions.ItemPosition.Desktop);
                }
            }
            pageCounter++;
        }
    }

    public void setDock(List<Item> dock) {
        for (Item item : dock) {
            String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.getId();
            Cursor cursorItem = db.rawQuery(SQL_QUERY_SPECIFIC, null);
            if (cursorItem.getCount() == 0) {
                createItem(item, 0, Definitions.ItemPosition.Dock);
            } else if (cursorItem.getCount() == 1) {
                updateItem(item, 0, Definitions.ItemPosition.Dock);
            }
        }
    }

    public void setItem(Item item, int page, Definitions.ItemPosition itemPosition) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.getId();
        Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        if (cursor.getCount() == 0) {
            createItem(item, page, itemPosition);
        } else if (cursor.getCount() == 1) {
            updateItem(item, page, itemPosition);
        }
    }

    // update data attributes for an item
    public void updateItem(Item item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_LABEL, item.getLabel());
        itemValues.put(COLUMN_X_POS, item.x);
        itemValues.put(COLUMN_Y_POS, item.y);

        Setup.logger().log(this, Log.INFO, null, "updateItem: %s (ID: %d)", item != null ? item.getLabel() : "NULL", item != null ? item.getId() : -1);

        String concat = "";
        switch (item.type) {
            case APP:
                if (Setup.appSettings().enableImageCaching()) {
                    Tool.saveIcon(context, Tool.drawableToBitmap(item.getIconProvider().getDrawableSynchronously(Definitions.NO_SCALE)), Integer.toString(item.getId()));
                }
                itemValues.put(COLUMN_DATA, Tool.getIntentAsString(item.intent));
                break;
            case GROUP:
                for (Item tmp : item.items) {
                    concat += tmp.getId() + Definitions.INT_SEP;
                }
                itemValues.put(COLUMN_DATA, concat);
                break;
            case ACTION:
                itemValues.put(COLUMN_DATA, item.actionValue);
                break;
            case WIDGET:
                concat = Integer.toString(item.widgetValue) + Definitions.INT_SEP
                        + Integer.toString(item.spanX) + Definitions.INT_SEP
                        + Integer.toString(item.spanY);
                itemValues.put(COLUMN_DATA, concat);
                break;
        }
        db.update(TABLE_HOME, itemValues, COLUMN_TIME + " = " + item.getId(), null);
    }

    // update the state of an item
    public void updateItem(Item item, Definitions.ItemState state) {
        ContentValues itemValues = new ContentValues();
        Setup.logger().log(this, Log.INFO, null, "updateItem (state): %s (ID: %d)", item != null ? item.getLabel() : "NULL", item != null ? item.getId() : -1);
        itemValues.put(COLUMN_STATE, state.ordinal());
        db.update(TABLE_HOME, itemValues, COLUMN_TIME + " = " + item.getId(), null);
    }

    // update the fields only used by the database
    public void updateItem(Item item, int page, Definitions.ItemPosition itemPosition) {
        Setup.logger().log(this, Log.INFO, null, "updateItem (delete + create): %s (ID: %d)", item != null ? item.getLabel() : "NULL", item != null ? item.getId() : -1);
        deleteItem(item, false);
        createItem(item, page, itemPosition);
    }

    private Item getSelectionItem(Cursor cursor) {
        Item item = new Item();
        int id = Integer.parseInt(cursor.getString(0));
        Item.Type type = Item.Type.valueOf(cursor.getString(1));
        String label = cursor.getString(2);
        int x = Integer.parseInt(cursor.getString(3));
        int y = Integer.parseInt(cursor.getString(4));
        String data = cursor.getString(5);

        item.setItemId(id);
        item.setLabel(label);
        item.x = x;
        item.y = y;
        item.type = type;

        String[] dataSplit;
        switch (type) {
            case APP:
            case SHORTCUT:
                item.intent = Tool.getIntentFromString(data);
                if (Setup.appSettings().enableImageCaching()) {
                    item.iconProvider = Setup.imageLoader().createIconProvider(Tool.getIcon(Home.launcher, Integer.toString(id)));
                } else {
                    switch (type) {
                        case APP:
                        case SHORTCUT:
                            App app = Setup.appLoader().findItemApp(item);
                            item.iconProvider = app != null ? app.getIconProvider() : null;
                            break;
                        default:
                            // TODO...
                            break;
                    }
                }
                break;
            case GROUP:
                item.items = new ArrayList<>();
                dataSplit = data.split(Definitions.INT_SEP);
                for (String s : dataSplit) {
                    item.items.add(getItem(Integer.parseInt(s)));
                }
                break;
            case ACTION:
                item.actionValue = Integer.parseInt(data);
                break;
            case WIDGET:
                dataSplit = data.split(Definitions.INT_SEP);
                item.widgetValue = Integer.parseInt(dataSplit[0]);
                item.spanX = Integer.parseInt(dataSplit[1]);
                item.spanY = Integer.parseInt(dataSplit[2]);
                break;
        }
        return item;
    }

    public void deleteGesture(int id) {
        db.delete(TABLE_GESTURE, COLUMN_TIME + " = ?", new String[]{String.valueOf(id)});
    }
}