package com.benny.openlauncher.util;

import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Tool {
    private Tool() {
    }

    public static float dp2px(float dp, Context context) {
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static int dp2px(int dp, Context context) {
        Resources resources = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics()));
    }

    public static void toast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, int str) {
        Toast.makeText(context, context.getResources().getString(str), Toast.LENGTH_SHORT).show();
    }

    public static void print(Object o) {
        if (o != null)
            Log.d("Hey", o.toString());
    }

    public static String[] split(String string, String delem) {
        ArrayList<String> list = new ArrayList<String>();
        char[] charArr = string.toCharArray();
        char[] delemArr = delem.toCharArray();
        int counter = 0;
        for (int i = 0; i < charArr.length; i++) {
            int k = 0;
            for (int j = 0; j < delemArr.length; j++) {
                if (charArr[i + j] == delemArr[j]) {
                    k++;
                } else {
                    break;
                }
            }
            if (k == delemArr.length) {
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

    public static Integer fetchThumbnailId(Context context,String phoneNumber) {

        final Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        final Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.Contacts.PHOTO_ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        try {
            Integer thumbnailId = null;
            if (cursor.moveToFirst()) {
                thumbnailId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
            }
            return thumbnailId;
        }
        finally {
            cursor.close();
        }

    }

    public static Bitmap fetchThumbnail(Context context,String phoneNumber) {
        Tool.print(phoneNumber);
        long thumbnailId = fetchThumbnailId(context, phoneNumber);
        final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId);
        final Cursor cursor = context.getContentResolver().query(uri, new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);

        try {
            Bitmap thumbnail = null;
            if (cursor.moveToFirst()) {
                final byte[] thumbnailBytes = cursor.getBlob(0);
                if (thumbnailBytes != null) {
                    thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
                }
            }
            return thumbnail;
        }
        finally {
            cursor.close();
        }

    }

    public static Bitmap openPhoto(Context context, String number) {
        Tool.print(number);
        long contactId = Tool.getContactIDFromNumber(context,number);
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

    public static void checkForUnusedIconAndDelete(Context context, ArrayList<String> IDs) {
        File dir = new File(context.getFilesDir() + "/iconCache");
        if (dir.exists()) {
            ArrayList<String> availableIDs = new ArrayList<>();
            File[] iconCaches = dir.listFiles();
            for (int i = 0; i < iconCaches.length; i++) {
                availableIDs.add(iconCaches[i].getName());
            }
            availableIDs.removeAll(IDs);
            for (int i = 0; i < availableIDs.size(); i++) {
                for (int j = 0; j < iconCaches.length; j++) {
                    if (iconCaches[j].getName().equals(availableIDs.get(i))) {
                        iconCaches[j].delete();
                        continue;
                    }
                }
            }
        }
    }

    public static Drawable getIconFromID(Context context, String ID) {
        if (ID == null)
            return null;
        Drawable icon = null;
        Bitmap bitmap = BitmapFactory.decodeFile(context.getFilesDir() + "/iconCache/" + ID);
        if (bitmap != null) {
            icon = new BitmapDrawable(context.getResources(), bitmap);
        }
        return icon;
    }

    public static String saveIconAndReturnID(Context context, Bitmap bitmap) {
        int i = 0;
        String filename = Integer.toString(i);

        File dir = new File(context.getFilesDir() + "/iconCache");
        if (!dir.exists())
            dir.mkdirs();

        File f = new File(context.getFilesDir() + "/iconCache/" + filename);

        while (f.exists()) {
            i++;
            filename = Integer.toString(i);
            f = new File(context.getFilesDir() + "/iconCache/" + filename);
        }

        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return String.valueOf(i);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null)
            return null;

        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void startApp(Context c, AppManager.App app) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(app.packageName, app.className);
        try {
            c.startActivity(intent);
        } catch (Exception e) {
            Tool.toast(c, R.string.toast_appuninstalled);
        }
    }

    public static int clampInt(int target, int min, int max) {
        return Math.max(min, Math.min(max, target));
    }

    public static float clampFloat(float target, float min, float max) {
        return Math.max(min, Math.min(max, target));
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

        }
    }

    public static View.OnTouchListener getItemOnTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Home.touchX = (int) motionEvent.getX();
                Home.touchY = (int) motionEvent.getY();
                Tool.print(Home.touchX);
                Tool.print(Home.touchY);
                return false;
            }
        };
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
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

    public static void setTheme(Activity act) {
//        switch (LauncherSettings.getInstance(act).generalSettings.theme){
//            case Light:
//                act.setTheme(R.style.NormalActivity_Light);
//                break;
//            case Dark:
//                act.setTheme(R.style.NormalActivity_Dark);
//                break;
//        }
    }

    public static void setHomeTheme(Activity act) {
//        switch (LauncherSettings.getInstance(act).generalSettings.theme){
//            case Light:
//                act.setTheme(R.style.Home_Light);
//                break;
//            case Dark:
//                act.setTheme(R.style.Home_Dark);
//                break;
//        }
    }

    public static String wrapColorTag(String str, @ColorInt int color) {
        return "<font color='" + String.format("#%06X", 0xFFFFFF & color) + "'>" + str + "</font>";
    }

    public static void askForText(String title, String defaultText, Context c, final OnTextGotListener listener) {
        new MaterialDialog.Builder(c)
                .title(title)
                .input(null, defaultText, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        listener.hereIsTheText(input.toString());
                    }
                })
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .show();
    }

    public interface OnTextGotListener {
        void hereIsTheText(String str);
    }

    public static void createScaleInScaleOutAnim(final View view, final Runnable endAction) {
        view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).setInterpolator(new AccelerateDecelerateInterpolator());
        new Handler().postDelayed(new Runnable() {
            public void run() {
                view.animate().scaleX(1f).scaleY(1f).setDuration(80).setInterpolator(new AccelerateDecelerateInterpolator());
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        endAction.run();
                    }
                }, 80);
            }
        }, 80);
    }
}
