package com.benny.openlauncher.util;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.widget.AppDrawer;
import com.benny.openlauncher.widget.Desktop;

public class DialogUtils {
  public static void desktopStylePicker(final Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title(context.getString(R.string.settings_stylePicker_normal))
        .items(R.array.desktopStyleEntries)
        .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.desktopHomePage, new MaterialDialog.ListCallbackSingleChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            LauncherSettings.getInstance(context).switchDesktopMode(position);
            return true;
          }
        }).show();
  }

  public static void appDrawerStylePicker(final Context context) {
    final String[] items = new String[AppDrawer.DrawerMode.values().length];
    int enabled = 0;
    for (int i = 0; i < AppDrawer.DrawerMode.values().length; i++) {
      items[i] = AppDrawer.DrawerMode.values()[i].name();
      if (LauncherSettings.getInstance(context).generalSettings.drawerMode == AppDrawer.DrawerMode.values()[i]) {
        enabled = i;
      }
    }
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title(context.getString(R.string.settings_stylePicker_title_drawer))
        .items(context.getString(R.string.settings_stylePicker_paged), context.getString(R.string.settings_stylePicker_vertical))
        .itemsCallbackSingleChoice(enabled, new MaterialDialog.ListCallbackSingleChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            LauncherSettings.getInstance(context).generalSettings.drawerMode = AppDrawer.DrawerMode.valueOf(items[position]);
            return true;
          }
        }).show();
  }

  public static void startSingleClickPicker(final Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title(context.getString(R.string.settings_single_click_title))
        .items(R.array.singleClickEntries)
        .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.singleClick, new MaterialDialog.ListCallbackSingleChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            LauncherSettings.getInstance(context).setSingleClickGesture(position);
            return true;
          }
        }).show();
  }

  public static void startDoubleClickPicker(final Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title(context.getString(R.string.settings_double_click_title))
        .items(R.array.gestureEntries)
        .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.doubleClick, new MaterialDialog.ListCallbackSingleChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            LauncherSettings.getInstance(context).setDoubleClickGesture(position);
            return true;
          }
        }).show();
  }

  public static void startPinchPicker(final Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title(context.getString(R.string.settings_pinch_title))
        .items(R.array.gestureEntries)
        .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.pinch, new MaterialDialog.ListCallbackSingleChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            LauncherSettings.getInstance(context).setPinchGesture(position);
            return true;
          }
        }).show();
  }

  public static void startUnpinchPicker(final Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title(context.getString(R.string.settings_unpinch_title))
        .items(R.array.gestureEntries)
        .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.unpinch, new MaterialDialog.ListCallbackSingleChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            LauncherSettings.getInstance(context).setUnpinchGesture(position);
            return true;
          }
        }).show();
  }

  public static void startSwipeDownPicker(final Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title(context.getString(R.string.settings_swipe_down_title))
        .items(R.array.gestureEntries)
        .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.swipeDown, new MaterialDialog.ListCallbackSingleChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            LauncherSettings.getInstance(context).setSwipeDownGesture(position);
            return true;
          }
        }).show();
  }

  public static void startSwipeUpPicker(final Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title(context.getString(R.string.settings_swipe_up_title))
        .items(R.array.gestureEntries)
        .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.swipeUp, new MaterialDialog.ListCallbackSingleChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            LauncherSettings.getInstance(context).setSwipeUpGesture(position);
            return true;
          }
        }).show();
  }
}
