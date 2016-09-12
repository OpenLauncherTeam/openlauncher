package com.bennyv4.project2.util;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

public class FiveSecsDelayContentProvider extends ContentProvider{
    @Override
    public boolean onCreate() {
        Tools.print("Create");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        Tools.print("I reveived");
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        Tools.print("I reveived");
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
