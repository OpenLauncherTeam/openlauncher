package com.benny.openlauncher.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActivityCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.benny.openlauncher.BuildConfig;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.ACTIVITY_SERVICE;

public class Tool extends com.benny.openlauncher.core.util.Tool {
    // ensure that tool cannot be instantiated
    private Tool() {
    }

    public static String[] split(String string, String delim) {
        ArrayList<String> list = new ArrayList<String>();
        char[] charArr = string.toCharArray();
        char[] delimArr = delim.toCharArray();
        int counter = 0;
        for (int i = 0; i < charArr.length; i++) {
            int k = 0;
            for (int j = 0; j < delimArr.length; j++) {
                if (charArr[i + j] == delimArr[j]) {
                    k++;
                } else {
                    break;
                }
            }
            if (k == delimArr.length) {
                String s = "";
                while (counter < i) {
                    s += charArr[counter];
                    counter++;
                }
                counter = i = i + k;
                list.add(s);
            }
        }
        String s = "";
        if (counter < charArr.length) {
            while (counter < charArr.length) {
                s += charArr[counter];
                counter++;
            }
            list.add(s);
        }
        return list.toArray(new String[list.size()]);
    }

    public static long getContactIDFromNumber(Context context, String contactNumber) {
        String UriContactNumber = Uri.encode(contactNumber);
        long phoneContactID = new Random().nextInt();
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, UriContactNumber),
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        while (contactLookupCursor.moveToNext()) {
            phoneContactID = contactLookupCursor.getLong(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        contactLookupCursor.close();
        return phoneContactID;
    }

    public static int factorColorBrightness(int color, int brightnessFactorPercent) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= brightnessFactorPercent / 100.0;
        hsv[2] = (hsv[2]) > 255 ? 255 : hsv[2];
        color = Color.HSVToColor(hsv);
        return color;
    }

    public static Integer fetchThumbnailId(Context context, String phoneNumber) {
        final Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        final Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.Contacts.PHOTO_ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        try {
            Integer thumbnailId = null;
            if (cursor.moveToFirst()) {
                thumbnailId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
            }
            return thumbnailId;
        } finally {
            cursor.close();
        }
    }

    public static Bitmap fetchThumbnail(Context context, String phoneNumber) {
        Tool.print(phoneNumber);
        Integer thumbnailId = fetchThumbnailId(context, phoneNumber);
        if (thumbnailId == null) {
            return null;
        }
        final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId);
        final Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);

        try {
            Bitmap thumbnail = null;
            if (cursor.moveToFirst()) {
                final byte[] thumbnailBytes = cursor.getBlob(0);
                if (thumbnailBytes != null) {
                    thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
                }
            }
            return thumbnail;
        } finally {
            cursor.close();
        }
    }

    public static Bitmap openPhoto(Context context, String number) {
        Tool.print(number);
        long contactId = Tool.getContactIDFromNumber(context, number);
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

        Cursor cursor = context.getContentResolver().query(photoUri, new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return null;
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Drawable getIcon(Context context, Item item) {
        if (item == null) {
            return null;
        }
        if (item.type == Item.Type.SHORTCUT) {
            return item.icon;
        }
        else {
            AppManager.App app = AppManager.getInstance(context).findApp(item.intent);
            if (app != null)
                return app.icon;
        }
        return null;
    }

    public static Drawable getIcon(Context context, String filename) {
        if (filename == null) {
            return null;
        }
        Drawable icon = null;
        Bitmap bitmap = BitmapFactory.decodeFile(context.getFilesDir() + "/icons/" + filename + ".png");
        if (bitmap != null) {
            icon = new BitmapDrawable(context.getResources(), bitmap);
        }
        return icon;
    }

    public static void saveIcon(Context context, Bitmap icon, String filename) {
        File directory = new File(context.getFilesDir() + "/icons");
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(context.getFilesDir() + "/icons/" + filename + ".png");
        removeIcon(context, filename);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            icon.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeIcon(Context context, String filename) {
        File file = new File(context.getFilesDir() + "/icons/" + filename + ".png");
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void startApp(Context context, AppManager.App app) {
        if (app.packageName.equals("com.benny.openlauncher")) {
            LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
            Home.consumeNextResume = true;
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(app.packageName, app.className);
                context.startActivity(intent);

                Home.consumeNextResume = true;
            } catch (Exception e) {
                Tool.toast(context, R.string.toast_app_uninstalled);
            }
        }
    }

    public static void startApp(Context context, Intent intent) {
        if (intent.getComponent().getPackageName().equals("com.benny.openlauncher")) {
            LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
            Home.consumeNextResume = true;
        } else {
            try {
                context.startActivity(intent);
                Home.consumeNextResume = true;
            } catch (Exception e) {
                Tool.toast(context, R.string.toast_app_uninstalled);
            }
        }
    }

    public static Intent getStartAppIntent(AppManager.App app) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(app.packageName, app.className);
        return intent;
    }

    public static View.OnTouchListener getBtnColorMaskController() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleY(1.1f).scaleX(1.1f).setDuration(50);
                        ((TextView) v).setTextColor(Color.rgb(200, 200, 200));
                        return false;
                    case MotionEvent.ACTION_UP:
                        v.animate().scaleY(1f).scaleX(1f).setDuration(50);
                        ((TextView) v).setTextColor(Color.WHITE);
                        return false;
                }
                return false;
            }
        };
    }

    public static void writeToFile(String name, String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException ignore) {
            // do nothing
        }
    }

    public static View.OnTouchListener getItemOnTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Home.touchX = (int) motionEvent.getX();
                Home.touchY = (int) motionEvent.getY();
                // use this to debug the on touch listener
                //Tool.print(Home.touchX);
                //Tool.print(Home.touchY);
                return false;
            }
        };
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String name, Context context) {
        try {
            FileInputStream fin = context.openFileInput(name);
            String ret = convertStreamToString(fin);
            fin.close();
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public static String wrapColorTag(String str, @ColorInt int color) {
        return "<font color='" + String.format("#%06X", 0xFFFFFF & color) + "'>" + str + "</font>";
    }

    @SuppressLint("DefaultLocale")
    public static String getRAM_Info(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);

        return String.format("<big><big><b>%s</b></big></big><br\\>%s / %s",
                context.getString(R.string.memory),
                Formatter.formatFileSize(context, memInfo.availMem),
                Formatter.formatFileSize(context, memInfo.totalMem)
        );
    }

    @SuppressLint("DefaultLocale")
    public static String getStorage_Info(Context context) {
        File externalFilesDir = Environment.getExternalStorageDirectory();
        if (externalFilesDir == null) {
            return "?";
        }
        StatFs stat = new StatFs(externalFilesDir.getPath());
        long blockSize = stat.getBlockSize();
        return String.format("<big><big><b>%s</b></big></big><br\\>%s / %s",
                context.getString(R.string.storage),
                Formatter.formatFileSize(context, blockSize * stat.getAvailableBlocks()),
                Formatter.formatFileSize(context, blockSize * (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? stat.getBlockCountLong() : stat.getBlockCount()))
        );
    }

    public static String getIntentAsString(Intent intent) {
        if (intent == null) {
            return "";
        } else {
            return intent.toUri(0);
        }
    }

    public static Intent getIntentFromString(String string) {
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

    @DrawableRes
    @SuppressWarnings("ConstantConditions")
    public static int getOL_LauncherIcon() {
        return BuildConfig.IS_TEST_BUILD ? R.drawable.ic_launcher_nightly : R.drawable.ic_launcher;
    }

    public static void copy(Context context, String stringIn, String stringOut) {
        try {
            File desktopData = new File(stringOut);
            desktopData.delete();
            File dockData = new File(stringOut);
            dockData.delete();
            File generalSettings = new File(stringOut);
            generalSettings.delete();
            Tool.print("deleted");

            FileInputStream in = new FileInputStream(stringIn);
            FileOutputStream out = new FileOutputStream(stringOut);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();
            Tool.print("copied");

        } catch (Exception e) {
            Toast.makeText(context, R.string.dialog__backup_app_settings__error, Toast.LENGTH_SHORT).show();
        }
    }
}
