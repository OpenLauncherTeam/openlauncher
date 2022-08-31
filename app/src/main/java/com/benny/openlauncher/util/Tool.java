package com.benny.openlauncher.util;

import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.App;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class Tool {
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) return;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) return;
        inputMethodManager.toggleSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void vibrate(View view) {
        Vibrator vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            // some manufacturers do not vibrate on long press
            // might as well make this a fallback method
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, 160));
        } else {
            vibrator.vibrate(50);
        }
    }

    public static void visibleViews(long duration, View... views) {
        if (views == null) return;
        for (View view : views) {
            if (view == null) continue;
            view.animate().alpha(1).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator()).withStartAction(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.VISIBLE);
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

    public static void createScaleInScaleOutAnim(final View view, final Runnable action) {
        final int animTime = Setup.appSettings().getAnimationSpeed() * 4;
        ViewPropertyAnimator animateScaleIn = view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(animTime);
        animateScaleIn.setInterpolator(new AccelerateDecelerateInterpolator());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewPropertyAnimator animateScaleOut = view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(animTime);
                animateScaleOut.setInterpolator(new AccelerateDecelerateInterpolator());
                new Handler().postDelayed(new Runnable() {
                    public final void run() {
                        action.run();
                    }
                }, animTime);
            }
        }, animTime);
    }

    public static void toast(Context context, int str) {
        Toast.makeText(context, context.getResources().getString(str), Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static boolean isPackageInstalled(@NonNull String packageName, @NonNull PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int dp2px(float dp) {
        Resources resources = Resources.getSystem();
        float px = dp * resources.getDisplayMetrics().density;
        return (int) Math.ceil(px);
    }

    public static int sp2px(float sp) {
        Resources resources = Resources.getSystem();
        float px = sp * resources.getDisplayMetrics().scaledDensity;
        return (int) Math.ceil(px);
    }

    public static int clampInt(int target, int min, int max) {
        return Math.max(min, Math.min(max, target));
    }

    public static float clampFloat(float target, float min, float max) {
        return Math.max(min, Math.min(max, target));
    }

    public static void startApp(Context context, App app, View view) {
        HomeActivity launcher = HomeActivity.Companion.getLauncher();
        launcher.onStartApp(context, app, view);
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
            // single color bitmap will be created
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Point convertPoint(Point fromPoint, View fromView, View toView) {
        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        fromView.getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);

        Point toPoint = new Point(fromCoordinate[0] - toCoordinate[0] + fromPoint.x, fromCoordinate[1] - toCoordinate[1] + fromPoint.y);
        return toPoint;
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
        try {
            return Intent.parseUri(string, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Intent getIntentFromApp(App app) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(app.getPackageName(), app.getClassName());
        return intent;
    }

    public static Drawable getIcon(Context context, String filename) {
        Bitmap bitmap = BitmapFactory.decodeFile(context.getFilesDir() + "/icons/" + filename + ".png");
        if (bitmap != null) return new BitmapDrawable(context.getResources(), bitmap);
        return null;
    }

    public static void saveIcon(Context context, Bitmap icon, String filename) {
        File directory = new File(context.getFilesDir() + "/icons/");
        if (!directory.exists()) directory.mkdir();
        File file = new File(directory, filename + ".png");
        try {
            file.createNewFile();
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

    @Nullable
    public static List<ShortcutInfo> getShortcutInfo(@NonNull Context context, @NonNull String packageName) {
        List<ShortcutInfo> shortcutInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            LauncherApps.ShortcutQuery shortcutQuery = new LauncherApps.ShortcutQuery();
            shortcutQuery.setQueryFlags(FLAG_MATCH_DYNAMIC | FLAG_MATCH_MANIFEST | FLAG_MATCH_PINNED);
            shortcutQuery.setPackage(packageName);
            try {
                shortcutInfo = launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle());
            } catch (SecurityException e) {
                Log.w(Tool.class.getSimpleName(), "Can't get shortcuts info. App is not set as default launcher");
            }
        }
        return shortcutInfo;
    }
}
