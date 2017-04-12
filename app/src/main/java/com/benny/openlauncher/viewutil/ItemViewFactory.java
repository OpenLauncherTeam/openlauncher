package com.benny.openlauncher.viewutil;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.AppItemView;
import com.benny.openlauncher.widget.CellContainer;
import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.WidgetView;

/**
 * Created by BennyKok on 10/24/2016.
 */

public class ItemViewFactory {

    public static final int NO_FLAGS = 0x01;
    public static final int NO_LABEL = 0x02;

    public static View getItemView(final Context context, final DesktopCallBack callBack, final Desktop.Item item, int flags) {
        View view = null;
        if (item.type == null) return null;
        switch (item.type) {
            case ACTION:
                view = new AppItemView.Builder(context).vibrateWhenLongPress().setLauncherAction(item.type).setTextColor(Color.WHITE).withOnTouchGetPosition().withOnLongPressDrag(item, DragAction.Action.ACTION_LAUNCHER, new AppItemView.Builder.LongPressCallBack() {
                    @Override
                    public boolean readyForDrag(View view) {
                        return true;
                    }

                    @Override
                    public void afterDrag(View view) {
                        callBack.setLastItem(item, view);
                    }
                }).setLabelVisibility((flags & NO_LABEL) != NO_LABEL).getView();
                break;
            case APP:
                final AppManager.App app = AppManager.getInstance(context).findApp(item.actions[0].getComponent().getPackageName(), item.actions[0].getComponent().getClassName());
                if (app == null) {
                    break;
                }
                view = new AppItemView.Builder(context)
                        .setAppItem(app)
                        .withOnClickLaunchApp(app)
                        .withOnTouchGetPosition()
                        .vibrateWhenLongPress()
                        .withOnLongPressDrag(item, DragAction.Action.ACTION_APP, new AppItemView.Builder.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();

                break;
            case WIDGET:
                AppWidgetProviderInfo appWidgetInfo = Home.appWidgetManager.getAppWidgetInfo(item.widgetID);
                final WidgetView widgetView = (WidgetView) Home.appWidgetHost.createView(context, item.widgetID, appWidgetInfo);
                widgetView.setAppWidget(item.widgetID, appWidgetInfo);

                widgetView.post(new Runnable() {
                    @Override
                    public void run() {
                        updateWidgetOption(item);
                    }
                });

                final FrameLayout widgetContainer = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.view_widgetcontainer, null);
                widgetContainer.addView(widgetView);

                final View ve = widgetContainer.findViewById(R.id.vertexpand);
                ve.bringToFront();
                final View he = widgetContainer.findViewById(R.id.horiexpand);
                he.bringToFront();
                final View vl = widgetContainer.findViewById(R.id.vertless);
                vl.bringToFront();
                final View hl = widgetContainer.findViewById(R.id.horiless);
                hl.bringToFront();

                ve.animate().scaleY(1).scaleX(1);
                he.animate().scaleY(1).scaleX(1);
                vl.animate().scaleY(1).scaleX(1);
                hl.animate().scaleY(1).scaleX(1);

                final Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        ve.animate().scaleY(0).scaleX(0);
                        he.animate().scaleY(0).scaleX(0);
                        vl.animate().scaleY(0).scaleX(0);
                        hl.animate().scaleY(0).scaleX(0);
                    }
                };
                widgetContainer.postDelayed(action, 2000);

                widgetView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        Intent i = new Intent();
                        i.putExtra("mDragData", item);
                        ClipData data = ClipData.newIntent("mDragIntent", i);
                        view.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.ACTION_WIDGET), 0);

                        callBack.setLastItem(item, widgetContainer);
                        return true;
                    }
                });

                ve.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.spanY++;
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                he.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.spanX++;
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                vl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.spanY--;
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                hl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.spanX--;
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });

                view = widgetContainer;
                break;
            case SHORTCUT:
                view = new AppItemView.Builder(context)
                        .setShortcutItem(item.actions[0])
                        .withOnTouchGetPosition()
                        .vibrateWhenLongPress()

                        .withOnLongPressDrag(item, DragAction.Action.ACTION_SHORTCUT, new AppItemView.Builder.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                break;
            case GROUP:
                view = new AppItemView.Builder(context)
                        .withOnLongPressDrag(item, DragAction.Action.ACTION_GROUP, new AppItemView.Builder.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .vibrateWhenLongPress()
                        .setTextColor(Color.WHITE)
                        .withOnTouchGetPosition()
                        .getView();

                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                ((AppItemView) view).setIcon(getGroupIconDrawable(context, item));
                ((AppItemView) view).setLabel((item.name));

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Home.launcher != null && Home.launcher.groupPopup.showWindowV(item, v, callBack)) {
                            ((GroupIconDrawable) ((AppItemView) v).getIcon()).popUp();
                        }
                    }
                });
                break;
        }
        if (view != null)
            view.setTag(item);
        return view;
    }

    private static void scaleWidget(View view, Desktop.Item item) {
        item.spanX = Math.min(item.spanX, 4);
        item.spanX = Math.max(item.spanX, 1);
        item.spanY = Math.min(item.spanY, 4);
        item.spanY = Math.max(item.spanY, 1);

        CellContainer.LayoutParams cellPositionToLayoutPrams = Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellPositionToLayoutPrams(item.x, item.y, item.spanX, item.spanY, (CellContainer.LayoutParams) view.getLayoutParams());
        if (cellPositionToLayoutPrams == null)
            Toast.makeText(Home.launcher.desktop.getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
        else {
            LauncherSettings.getInstance(Home.launcher.desktop.getContext()).desktopData.get(Home.launcher.desktop.getCurrentItem()).remove(item);
            item.x = cellPositionToLayoutPrams.x;
            item.y = cellPositionToLayoutPrams.y;
            LauncherSettings.getInstance(Home.launcher.desktop.getContext()).desktopData.get(Home.launcher.desktop.getCurrentItem()).add(item);
            view.setLayoutParams(cellPositionToLayoutPrams);

            updateWidgetOption(item);

        }

    }

    private static void updateWidgetOption(Desktop.Item item) {
        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.spanX * Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.spanX * Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.spanX * Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.spanY * Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellHeight);
        Home.appWidgetManager.updateAppWidgetOptions(item.widgetID, newOps);
    }

    //Common methods
    public static Drawable getGroupIconDrawable(Context context, Desktop.Item item) {
        final float iconSize = Tool.dp2px(LauncherSettings.getInstance(context).generalSettings.iconSize, context);
        final Bitmap[] icons = new Bitmap[4];
        for (int i = 0; i < 4; i++) {
            if (i < item.actions.length) {
                if (item.actions[i].getStringExtra("shortCutIconID") != null)
                    icons[i] = Tool.drawableToBitmap(Tool.getIconFromID(context, item.actions[i].getStringExtra("shortCutIconID")));
                else {
                    AppManager.App app = AppManager.getInstance(context).findApp(item.actions[i].getComponent().getPackageName(), item.actions[i].getComponent().getClassName());
                    if (app != null)
                        icons[i] = Tool.drawableToBitmap(app.icon);
                    else
                        icons[i] = Tool.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
                }
            } else {
                icons[i] = Tool.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        return new GroupIconDrawable(icons, iconSize);
    }
}
