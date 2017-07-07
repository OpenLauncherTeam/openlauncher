package com.benny.openlauncher.core.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.IconProvider;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class Tool {

    public static void hideKeyboard(Context context, View view) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyboard(Context context, View view) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void visibleViews(View... views) {
        if (views == null) return;
        for (View view : views) {
            if (view == null) continue;
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator());
        }
    }

    public static void visibleViews(long duration, View... views) {
        if (views == null) return;
        for (View view : views) {
            if (view == null) continue;
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator());
        }
    }

    public static void invisibleViews(View... views) {
        if (views == null) return;
        for (final View view : views) {
            if (view == null) continue;
            view.animate().alpha(0).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public static void invisibleViews(long duration, View... views) {
        if (views == null) return;
        for (final View view : views) {
            if (view == null) continue;
            view.animate().alpha(0).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public static void goneViews(View... views) {
        if (views == null) return;
        for (final View view : views) {
            if (view == null) continue;
            view.animate().alpha(0).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.GONE);
                }
            });
        }
    }

    public static void goneViews(long duration, View... views) {
        if (views == null) return;
        for (final View view : views) {
            if (view == null) continue;
            view.animate().alpha(0).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.GONE);
                }
            });
        }
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

    public static void toast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, int str) {
        Toast.makeText(context, context.getResources().getString(str), Toast.LENGTH_SHORT).show();
    }

    public static float dp2px(float dp, Context context) {
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static int dp2px(int dp, Context context) {
        Resources resources = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics()));
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int clampInt(int target, int min, int max) {
        return Math.max(min, Math.min(max, target));
    }

    public static float clampFloat(float target, float min, float max) {
        return Math.max(min, Math.min(max, target));
    }

    public static void startApp(Context context, App app) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(app.getPackageName(), app.getClassName());
            context.startActivity(intent);

            Home.consumeNextResume = true;
        } catch (Exception e) {
            Tool.toast(context, R.string.toast_app_uninstalled);
        }
    }

    public static void startApp(Context context, Intent intent) {
        try {
            context.startActivity(intent);
            Home.consumeNextResume = true;
        } catch (Exception e) {
            Tool.toast(context, R.string.toast_app_uninstalled);
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            // single color bitmap will be created of 1x1 pixel
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Point convertPoint(Point fromPoint, View fromView, View toView) {
        int[] fromCoord = new int[2];
        int[] toCoord = new int[2];
        fromView.getLocationOnScreen(fromCoord);
        toView.getLocationOnScreen(toCoord);

        Point toPoint = new Point(fromCoord[0] - toCoord[0] + fromPoint.x,
                fromCoord[1] - toCoord[1] + fromPoint.y);

        return toPoint;
    }

    //
    public static void vibrate(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }
    //

    public static void print(Object o) {
        if (o != null) {
            Log.d("Hey", o.toString());
        }
    }

    public static void print(Object... o) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < o.length; i++) {
            sb.append(o[i].toString()).append("  ");
        }
        Log.d("Hey", sb.toString());
    }

    public static boolean isIntentActionAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.size() > 0;
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

    public static IconProvider getIcon(Context context, Item item) {
        if (item == null) {
            return null;
        }
        if (item.type == Item.Type.SHORTCUT) {
            return item.iconProvider;
        } else {
            App app = Setup.appLoader().findItemApp(item);
            if (app != null)
                return app;
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
}
