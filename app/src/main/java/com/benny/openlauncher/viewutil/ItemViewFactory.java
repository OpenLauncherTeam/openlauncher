package com.benny.openlauncher.viewutil;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.notifications.NotificationListener;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.DragHandler;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.AppItemView;
import com.benny.openlauncher.widget.CellContainer;
import com.benny.openlauncher.widget.WidgetContainer;
import com.benny.openlauncher.widget.WidgetView;

public class ItemViewFactory {
    public static View getItemView(final Context context, final DesktopCallback callback, final DragAction.Action type, final Item item) {
        View view = null;
        if (item.getType().equals(Item.Type.WIDGET)) {
            view = getWidgetView(context, callback, type, item);
        } else {
            AppItemView.Builder builder = new AppItemView.Builder(context);
            builder.setIconSize(Setup.appSettings().getIconSize());
            builder.vibrateWhenLongPress(Setup.appSettings().getGestureFeedback());
            builder.withOnLongClick(item, type, callback);
            switch (type) {
                case DRAWER:
                    builder.setLabelVisibility(Setup.appSettings().getDrawerShowLabel());
                    builder.setTextColor(Setup.appSettings().getDrawerLabelColor());
                    break;
                case DESKTOP:
                default:
                    builder.setLabelVisibility(Setup.appSettings().getDesktopShowLabel());
                    builder.setTextColor(Color.WHITE);
                    break;
            }
            switch (item.getType()) {
                case APP:
                    final App app = Setup.appLoader().findItemApp(item);
                    if (app == null) break;
                    view = builder.setAppItem(item).getView();

                    if (Setup.appSettings().getNotificationStatus()) {
                        NotificationListener.setNotificationCallback(app.getPackageName(),
                            (NotificationListener.NotificationCallback) view);
                    }
                    break;
                case SHORTCUT:
                    view = builder.setShortcutItem(item).getView();
                    break;
                case GROUP:
                    view = builder.setGroupItem(context, callback, item).getView();
                    view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    break;
                case ACTION:
                    view = builder.setActionItem(item).getView();
                    break;
            }
        }

        // TODO find out why tag is set here
        if (view != null) {
            view.setTag(item);
        }

        return view;
    }

    public static View getWidgetView(final Context context, final DesktopCallback callback, final DragAction.Action type, final Item item) {
        if (HomeActivity._appWidgetHost == null) return null;

        final AppWidgetProviderInfo appWidgetInfo = HomeActivity._appWidgetManager.getAppWidgetInfo(item.getWidgetValue());
        final WidgetView widgetView = (WidgetView) HomeActivity._appWidgetHost.createView(context, item.getWidgetValue(), appWidgetInfo);
        widgetView.setAppWidget(item.getWidgetValue(), appWidgetInfo);

        final WidgetContainer widgetContainer = new WidgetContainer(context, widgetView, item);

        // TODO move this to standard DragHandler.getLongClick() method
        // needs to be set on widgetView but use widgetContainer inside
        widgetView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (Setup.appSettings().getDesktopLock()) {
                    return false;
                }
                if (Setup.appSettings().getGestureFeedback()) {
                    Tool.vibrate(view);
                }
                DragHandler.startDrag(widgetContainer, item, DragAction.Action.DESKTOP, callback);
                return true;
            }
        });

        widgetView.post(new Runnable() {
            @Override
            public void run() {
                widgetContainer.updateWidgetOption(item);
            }
        });

        return widgetContainer;
    }
}
