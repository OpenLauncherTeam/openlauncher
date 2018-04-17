package com.benny.openlauncher.core.viewutil;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.core.activity.CoreHome;
import com.benny.openlauncher.core.interfaces.AbstractApp;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.util.DragNDropHandler;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.widget.AppItemView;
import com.benny.openlauncher.core.widget.CellContainer;
import com.benny.openlauncher.core.widget.WidgetView;

public class ItemViewFactory {

    public static final int NO_FLAGS = 0x01;
    public static final int NO_LABEL = 0x02;

    public static View getItemView(Context context, Item item, boolean showLabels, DesktopCallBack callBack, int iconSize) {
        int flag = showLabels ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
        return getItemView(context, callBack, item, iconSize, flag);
    }

    private static View getItemView(final Context context, final DesktopCallBack callBack, final Item item, int iconSize, int flags) {
        View view = null;
        switch (item.getType()) {
            case APP:
                final AbstractApp app = Setup.Companion.appLoader().findItemApp(item);
                if (app == null) {
                    break;
                }
                view = new AppItemView.Builder(context, iconSize)
                        .setAppItem(item, app)
                        .withOnTouchGetPosition(item, Setup.Companion.itemGestureCallback())
                        .vibrateWhenLongPress()
                        .withOnLongClick(item, DragAction.Action.APP, new AppItemView.LongPressCallBack() {
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
            case SHORTCUT:
                view = new AppItemView.Builder(context, iconSize)
                        .setShortcutItem(item)
                        .withOnTouchGetPosition(item, Setup.Companion.itemGestureCallback())
                        .vibrateWhenLongPress()
                        .withOnLongClick(item, DragAction.Action.SHORTCUT, new AppItemView.LongPressCallBack() {
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
                view = new AppItemView.Builder(context, iconSize)
                        .setGroupItem(context, callBack, item, iconSize)
                        .withOnTouchGetPosition(item, Setup.Companion.itemGestureCallback())
                        .vibrateWhenLongPress()
                        .withOnLongClick(item, DragAction.Action.GROUP, new AppItemView.LongPressCallBack() {
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
                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                break;
            case ACTION:
                view = new AppItemView.Builder(context, iconSize)
                        .setActionItem(item)
                        .withOnTouchGetPosition(item, Setup.Companion.itemGestureCallback())
                        .vibrateWhenLongPress()
                        .withOnLongClick(item, DragAction.Action.ACTION, new AppItemView.LongPressCallBack() {
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
                if (CoreHome.Companion.getAppWidgetHost() == null) break;
                final AppWidgetProviderInfo appWidgetInfo = CoreHome.Companion.getAppWidgetManager().getAppWidgetInfo(item.getWidgetValue());
                final WidgetView widgetView = (WidgetView) CoreHome.Companion.getAppWidgetHost().createView(context, item.getWidgetValue(), appWidgetInfo);

                widgetView.setAppWidget(item.getWidgetValue(), appWidgetInfo);
                widgetView.post(new Runnable() {
                    @Override
                    public void run() {
                        updateWidgetOption(item);
                    }
                });

                final FrameLayout widgetContainer = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.view_widget_container, null);
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
                widgetView.setOnTouchListener(Tool.getItemOnTouchListener(item, Setup.Companion.itemGestureCallback()));
                widgetView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (Setup.Companion.appSettings().isDesktopLock()) {
                            return false;
                        }
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        DragNDropHandler.startDrag(view, item, DragAction.Action.WIDGET, null);

                        callBack.setLastItem(item, widgetContainer);
                        return true;
                    }
                });

                ve.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.setSpanY(item.getSpanY() + 1);
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                he.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.setSpanX(item.getSpanX() + 1);
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                vl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.setSpanY(item.getSpanY() - 1);
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                hl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.setSpanX(item.getSpanX() - 1);
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                view = widgetContainer;
                break;
        }
        if (view != null) {
            view.setTag(item);
        }
        return view;
    }

    private static void scaleWidget(View view, Item item) {
        item.setSpanX(Math.min(item.getSpanX(), CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().getCellSpanH()));
        item.setSpanX(Math.max(item.getSpanX(), 1));
        item.setSpanY(Math.min(item.getSpanY(), CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().getCellSpanV()));
        item.setSpanY(Math.max(item.getSpanY(), 1));

        CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(false, (CellContainer.LayoutParams) view.getLayoutParams());
        if (!CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().checkOccupied(new Point(item.getX(), item.getY()), item.getSpanX(), item.getSpanY())) {
            CellContainer.LayoutParams newWidgetLayoutParams = new CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT, CellContainer.LayoutParams.WRAP_CONTENT, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());

            // update occupied array
            CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, newWidgetLayoutParams);

            // update the view
            view.setLayoutParams(newWidgetLayoutParams);
            updateWidgetOption(item);

            // update the widget size in the database
            CoreHome.Companion.getDb().saveItem(item);
        } else {
            Toast.makeText(CoreHome.Companion.getLauncher().getDesktop().getContext(), R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show();

            // add the old layout params to the occupied array
            CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, (CellContainer.LayoutParams) view.getLayoutParams());
        }
    }

    private static void updateWidgetOption(Item item) {
        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.getSpanX() * CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth());
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.getSpanX() * CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth());
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.getSpanY() * CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight());
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.getSpanY() * CoreHome.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight());
        CoreHome.Companion.getAppWidgetManager().updateAppWidgetOptions(item.getWidgetValue(), newOps);
    }
}
