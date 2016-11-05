package com.benny.openlauncher.util;

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
    public static View getItemView(final Context context, final DesktopCallBack callBack, final Desktop.Item item,int flags) {
        View view = null;
        switch (item.type) {
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
                        .withOnLongClickDrag(item, DragAction.Action.ACTION_SHORTCUT, new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                callBack.setLastItem(item, v);
                                return false;
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
                        .withOnLongClickDrag(item, DragAction.Action.ACTION_SHORTCUT, new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                callBack.setLastItem(item, v);
                                return false;
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                break;
            case GROUP:
                view = new AppItemView.Builder(context)
                        .withOnLongClickDrag(item, DragAction.Action.ACTION_GROUP, new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                callBack.setLastItem(item, v);
                                return false;
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .vibrateWhenLongPress()
                        .setTextColor(Color.WHITE)
                        .withOnTouchGetPosition()
                        .getView();

                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                ((AppItemView) view).setIcon(getGroupIconDrawable(context, item), false);
                ((AppItemView) view).setLabel((item.name));

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Home.groupPopup.showWindowV(item, v, callBack)) {
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

//    //Item view for dock
//    public static View getShortcutView(final Dock dock, final Desktop.Item item) {
//        AppItemView view = new AppItemView.Builder(dock.getContext())
//                .setShortcutItem(item.actions[0])
//                .withOnTouchGetPosition()
//                .vibrateWhenLongPress()
//                .withOnLongClickDrag(item, DragAction.Action.ACTION_SHORTCUT, new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        //Remove the item from settings
//                        LauncherSettings.getInstance(dock.getContext()).dockData.remove(item);
//                        //end
//
//                        dock.previousItemView = v;
//                        dock.previousItem = item;
//                        dock.removeView(v);
//                        return false;
//                    }
//                })
//                .setTextColor(Color.WHITE)
//                .getView();
//        view.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View view, DragEvent dragEvent) {
//                switch (dragEvent.getAction()) {
//                    case DragEvent.ACTION_DRAG_STARTED:
//                        switch (((DragAction) dragEvent.getLocalState()).action) {
//                            case ACTION_APP:
//                            case ACTION_APP_DRAWER:
//                            case ACTION_SHORTCUT:
//                                return true;
//                        }
//                        return false;
//                    case DragEvent.ACTION_DROP:
//                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
//                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
//                        Desktop.Item dropitem = intent.getParcelableExtra("mDragData");
//                        if (dropitem.type == Desktop.Item.Type.APP || dropitem.type == Desktop.Item.Type.SHORTCUT) {
//                            LauncherSettings.getInstance(dock.getContext()).dockData.remove(item);
//                            dock.removeView(view);
//
//                            item.addActions(dropitem.actions[0]);
//                            item.name = "Unnamed";
//                            item.type = Desktop.Item.Type.GROUP;
//                            LauncherSettings.getInstance(dock.getContext()).dockData.add(item);
//                            dock.addItemToPagePosition(item,0);
//
//                            Home.desktop.consumeRevert();
//                            Home.dock.consumeRevert();
//                        }
//                        return true;
//                    case DragEvent.ACTION_DRAG_ENDED:
//                        return true;
//                }
//                return false;
//            }
//        });
//        return view;
//    }
//
//    public static View getAppItemView(final Dock dock, final Desktop.Item item) {
//        final AppManager.App app = AppManager.getInstance(dock.getContext()).findApp(item.actions[0].getComponent().getPackageName(), item.actions[0].getComponent().getClassName());
//        if (app == null) {
//            return null;
//        }
//        AppItemView view = new AppItemView.Builder(dock.getContext())
//                .setAppItem(app)
//                .withOnClickLaunchApp(app)
//                .withOnTouchGetPosition()
//                .setLabelVisibility()
//                .vibrateWhenLongPress()
//                .withOnLongClickDrag(item, DragAction.Action.ACTION_APP, new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        //Remove the item from settings
//                        LauncherSettings.getInstance(dock.getContext()).dockData.remove(item);
//                        //end
//
//                        dock.previousItemView = v;
//                        dock.previousItem = item;
//                        dock.removeView(v);
//                        return false;
//                    }
//                })
//                .setTextColor(Color.WHITE)
//                .getView();
//
//
//        view.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View view, DragEvent dragEvent) {
//                switch (dragEvent.getAction()) {
//                    case DragEvent.ACTION_DRAG_STARTED:
//                        switch (((DragAction) dragEvent.getLocalState()).action) {
//                            case ACTION_APP:
//                            case ACTION_APP_DRAWER:
//                            case ACTION_SHORTCUT:
//                                return true;
//                        }
//                        return false;
//                    case DragEvent.ACTION_DROP:
//                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
//                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
//                        Desktop.Item dropitem = intent.getParcelableExtra("mDragData");
//                        if (dropitem.type == Desktop.Item.Type.APP || dropitem.type == Desktop.Item.Type.SHORTCUT) {
//                            LauncherSettings.getInstance(dock.getContext()).dockData.remove(item);
//                            dock.removeView(view);
//
//                            item.addActions(dropitem.actions[0]);
//                            item.name = "Unnamed";
//                            item.type = Desktop.Item.Type.GROUP;
//                            LauncherSettings.getInstance(dock.getContext()).dockData.add(item);
//                            dock.addItemToPagePosition(item,0);
//
//                            Home.desktop.consumeRevert();
//                            Home.dock.consumeRevert();
//                        }
//                        return true;
//                    case DragEvent.ACTION_DRAG_ENDED:
//                        return true;
//                }
//                return false;
//            }
//        });
//
//        return view;
//    }
//
//    public static View getGroupItemView(final Dock dock, final Desktop.Item item) {
//        final AppItemView view = new AppItemView.Builder(dock.getContext())
//                .withOnLongClickDrag(item, DragAction.Action.ACTION_GROUP, new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        //Remove the item from settings
//                        LauncherSettings.getInstance(dock.getContext()).dockData.remove(item);
//                        //end
//
//                        dock.previousItemView = v;
//                        dock.previousItem = item;
//                        dock.removeView(v);
//                        return false;
//                    }
//                })
//                .setLabelVisibility()
//                .vibrateWhenLongPress()
//                .setTextColor(Color.WHITE)
//                .withOnTouchGetPosition()
//                .getView();
//
//        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//
//        view.setIcon(getGroupIconDrawable(dock.getContext(), item), false);
//        view.setLabel((item.name));
//
//        view.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View view, DragEvent dragEvent) {
//                switch (dragEvent.getAction()) {
//                    case DragEvent.ACTION_DRAG_STARTED:
//                        switch (((DragAction) dragEvent.getLocalState()).action) {
//                            case ACTION_APP:
//                            case ACTION_APP_DRAWER:
//                            case ACTION_SHORTCUT:
//                                return true;
//                        }
//                        return false;
//                    case DragEvent.ACTION_DROP:
//                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
//                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
//                        Desktop.Item dropitem = intent.getParcelableExtra("mDragData");
//                        if ((dropitem.type == Desktop.Item.Type.APP || dropitem.type == Desktop.Item.Type.SHORTCUT) && item.actions.length < GroupPopupView.GroupDef.maxItem) {
//                            LauncherSettings.getInstance(dock.getContext()).dockData.remove(item);
//                            dock.removeView(view);
//
//                            item.addActions(dropitem.actions[0]);
//                            item.type = Desktop.Item.Type.GROUP;
//                            LauncherSettings.getInstance(dock.getContext()).dockData.add(item);
//                            dock.addItemToPagePosition(item,0);
//
//                            Home.desktop.consumeRevert();
//                            Home.dock.consumeRevert();
//                        }
//                        return true;
//                    case DragEvent.ACTION_DRAG_ENDED:
//                        return true;
//                }
//                return false;
//            }
//        });
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (Home.groupPopup.showWindowV(item, view, true)) {
//                    ((GroupIconDrawable) view.getIcon()).popUp();
//                }
//            }
//        });
//
//        return view;
//    }
//
//    //Item view for desktop
//    public static View getShortcutView(final Desktop desktop, final Desktop.Item item) {
//        AppItemView view = new AppItemView.Builder(desktop.getContext())
//                .setShortcutItem(item.actions[0])
//                .withOnTouchGetPosition()
//                .vibrateWhenLongPress()
//                .withOnLongClickDrag(item, DragAction.Action.ACTION_SHORTCUT, new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        //Remove the item from settings
//                        LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).remove(item);
//                        //end
//
//                        desktop.previousPage = desktop.getCurrentItem();
//                        desktop.previousItemView = v;
//                        desktop.previousItem = item;
//                        desktop.pages.get(desktop.getCurrentItem()).removeView(v);
//                        return false;
//                    }
//                })
//                .setTextColor(Color.WHITE)
//                .getView();
//
//        view.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View view, DragEvent dragEvent) {
//                switch (dragEvent.getAction()) {
//                    case DragEvent.ACTION_DRAG_STARTED:
//                        switch (((DragAction) dragEvent.getLocalState()).action) {
//                            case ACTION_APP:
//                            case ACTION_APP_DRAWER:
//                            case ACTION_SHORTCUT:
//                                return true;
//                        }
//                        return false;
//                    case DragEvent.ACTION_DROP:
//                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
//                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
//                        Desktop.Item dropitem = intent.getParcelableExtra("mDragData");
//                        if (dropitem.type == Desktop.Item.Type.APP || dropitem.type == Desktop.Item.Type.SHORTCUT) {
//                            LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).remove(item);
//                            desktop.pages.get(desktop.getCurrentItem()).removeView(view);
//
//                            item.addActions(dropitem.actions[0]);
//                            item.name = "Unnamed";
//                            item.type = Desktop.Item.Type.GROUP;
//                            LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).add(item);
//                            desktop.addItemToPagePosition(item, desktop.getCurrentItem());
//
//                            Home.desktop.consumeRevert();
//                            Home.dock.consumeRevert();
//                        }
//                        return true;
//                    case DragEvent.ACTION_DRAG_ENDED:
//                        return true;
//                }
//                return false;
//            }
//        });
//        return view;
//    }
//
//    public static View getAppItemView(final Desktop desktop, final Desktop.Item item) {
//        final AppManager.App app = AppManager.getInstance(desktop.getContext()).findApp(item.actions[0].getComponent().getPackageName(), item.actions[0].getComponent().getClassName());
//        if (app == null) {
//            return null;
//        }
//
//        AppItemView view = new AppItemView.Builder(desktop.getContext())
//                .setAppItem(app)
//                .withOnClickLaunchApp(app)
//                .withOnTouchGetPosition()
//                .vibrateWhenLongPress()
//                .withOnLongClickDrag(item, DragAction.Action.ACTION_APP, new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        //Remove the item from settings
//                        LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).remove(item);
//                        //end
//
//                        desktop.previousPage = desktop.getCurrentItem();
//                        desktop.previousItemView = v;
//                        desktop.previousItem = item;
//                        desktop.pages.get(desktop.getCurrentItem()).removeView(v);
//                        return false;
//                    }
//                })
//                .setTextColor(Color.WHITE)
//                .getView();
//
//
//        view.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View view, DragEvent dragEvent) {
//                switch (dragEvent.getAction()) {
//                    case DragEvent.ACTION_DRAG_STARTED:
//                        switch (((DragAction) dragEvent.getLocalState()).action) {
//                            case ACTION_APP:
//                            case ACTION_APP_DRAWER:
//                            case ACTION_SHORTCUT:
//                                return true;
//                        }
//                        return false;
//                    case DragEvent.ACTION_DROP:
//                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
//                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
//                        Desktop.Item dropitem = intent.getParcelableExtra("mDragData");
//                        if (dropitem.type == Desktop.Item.Type.APP || dropitem.type == Desktop.Item.Type.SHORTCUT) {
//                            LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).remove(item);
//                            desktop.pages.get(desktop.getCurrentItem()).removeView(view);
//
//                            item.addActions(dropitem.actions[0]);
//                            item.name = "Unnamed";
//                            item.type = Desktop.Item.Type.GROUP;
//                            LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).add(item);
//                            desktop.addItemToPagePosition(item, desktop.getCurrentItem());
//
//                            Home.desktop.consumeRevert();
//                            Home.dock.consumeRevert();
//                        }
//                        return true;
//                    case DragEvent.ACTION_DRAG_ENDED:
//                        return true;
//                }
//                return false;
//            }
//        });
//        return view;
//    }
//
//    public static View getGroupItemView(final Desktop desktop, final Desktop.Item item) {
//        final AppItemView view = new AppItemView.Builder(desktop.getContext())
//                .withOnLongClickDrag(item, DragAction.Action.ACTION_GROUP, new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        //Remove the item from settings
//                        LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).remove(item);
//                        //end
//
//                        desktop.previousPage = desktop.getCurrentItem();
//                        desktop.previousItemView = v;
//                        desktop.previousItem = item;
//                        desktop.pages.get(desktop.getCurrentItem()).removeView(v);
//                        return false;
//                    }
//                })
//                .vibrateWhenLongPress()
//                .setTextColor(Color.WHITE)
//                .withOnTouchGetPosition()
//                .getView();
//
//        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//
//        view.setIcon(getGroupIconDrawable(desktop.getContext(), item), false);
//        view.setLabel((item.name));
//
//        view.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View view, DragEvent dragEvent) {
//                switch (dragEvent.getAction()) {
//                    case DragEvent.ACTION_DRAG_STARTED:
//                        switch (((DragAction) dragEvent.getLocalState()).action) {
//                            case ACTION_APP:
//                            case ACTION_APP_DRAWER:
//                            case ACTION_SHORTCUT:
//                                return true;
//                        }
//                        return false;
//                    case DragEvent.ACTION_DROP:
//                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
//                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
//                        Desktop.Item dropitem = intent.getParcelableExtra("mDragData");
//                        if ((dropitem.type == Desktop.Item.Type.APP || dropitem.type == Desktop.Item.Type.SHORTCUT) && item.actions.length < GroupPopupView.GroupDef.maxItem) {
//                            LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).remove(item);
//                            desktop.pages.get(desktop.getCurrentItem()).removeView(view);
//
//                            item.addActions(dropitem.actions[0]);
//                            item.type = Desktop.Item.Type.GROUP;
//                            LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).add(item);
//                            desktop.addItemToPagePosition(item, desktop.getCurrentItem());
//
//                            Home.desktop.consumeRevert();
//                            Home.dock.consumeRevert();
//                        }
//                        return true;
//                    case DragEvent.ACTION_DRAG_ENDED:
//                        return true;
//                }
//                return false;
//            }
//        });
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (Home.groupPopup.showWindowV(item, view, false)) {
//                    ((GroupIconDrawable) view.getIcon()).popUp();
//                }
//            }
//        });
//
//        return view;
//    }
//
//    public static View getWidgetView(final Desktop desktop, final Desktop.Item item) {
//        AppWidgetProviderInfo appWidgetInfo = Home.appWidgetManager.getAppWidgetInfo(item.widgetID);
//        final WidgetView widgetView = (WidgetView) Home.appWidgetHost.createView(desktop.getContext(), item.widgetID, appWidgetInfo);
//        widgetView.setAppWidget(item.widgetID, appWidgetInfo);
//
//        widgetView.post(new Runnable() {
//            @Override
//            public void run() {
//                updateWidgetOption(item);
//            }
//        });
//
//        final FrameLayout widgetContainer = (FrameLayout) LayoutInflater.from(desktop.getContext()).inflate(R.layout.view_widgetcontainer, null);
//        widgetContainer.addView(widgetView);
//
//        final View ve = widgetContainer.findViewById(R.id.vertexpand);
//        ve.bringToFront();
//        final View he = widgetContainer.findViewById(R.id.horiexpand);
//        he.bringToFront();
//        final View vl = widgetContainer.findViewById(R.id.vertless);
//        vl.bringToFront();
//        final View hl = widgetContainer.findViewById(R.id.horiless);
//        hl.bringToFront();
//
//        ve.animate().scaleY(1).scaleX(1);
//        he.animate().scaleY(1).scaleX(1);
//        vl.animate().scaleY(1).scaleX(1);
//        hl.animate().scaleY(1).scaleX(1);
//
//        final Runnable action = new Runnable() {
//            @Override
//            public void run() {
//                ve.animate().scaleY(0).scaleX(0);
//                he.animate().scaleY(0).scaleX(0);
//                vl.animate().scaleY(0).scaleX(0);
//                hl.animate().scaleY(0).scaleX(0);
//            }
//        };
//        widgetContainer.postDelayed(action, 2000);
//
//        //widgetView.setOnTouchListener(Tool.getItemOnTouchListener());
//        widgetView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
//                Intent i = new Intent();
//                i.putExtra("mDragData", item);
//                i.putExtra("mX", Home.touchY);
//                i.putExtra("mY", Home.touchY);
//                i.putExtra("mW", view.getWidth() / 2);
//                i.putExtra("mH", view.getHeight() / 2);
//                ClipData data = ClipData.newIntent("mDragIntent", i);
//                view.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.ACTION_WIDGET), 0);
//
//                //Remove the item from settings
//                LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem()).remove(item);
//                //end
//
//                desktop.previousPage = desktop.getCurrentItem();
//                desktop.previousItemView = (View) view.getParent();
//                desktop.previousItem = item;
//                desktop.pages.get(desktop.getCurrentItem()).removeView((View) view.getParent());
//                return true;
//            }
//        });
//
//        ve.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (view.getScaleX() < 1) return;
//                item.spanY++;
//                scaleWidget(widgetContainer, item);
//                widgetContainer.removeCallbacks(action);
//                widgetContainer.postDelayed(action, 2000);
//            }
//        });
//        he.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (view.getScaleX() < 1) return;
//                item.spanX++;
//                scaleWidget(widgetContainer, item);
//                widgetContainer.removeCallbacks(action);
//                widgetContainer.postDelayed(action, 2000);
//            }
//        });
//        vl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (view.getScaleX() < 1) return;
//                item.spanY--;
//                scaleWidget(widgetContainer, item);
//                widgetContainer.removeCallbacks(action);
//                widgetContainer.postDelayed(action, 2000);
//            }
//        });
//        hl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (view.getScaleX() < 1) return;
//                item.spanX--;
//                scaleWidget(widgetContainer, item);
//                widgetContainer.removeCallbacks(action);
//                widgetContainer.postDelayed(action, 2000);
//            }
//        });
//
//        return widgetContainer;
//    }

    private static void scaleWidget(View view, Desktop.Item item) {
        item.spanX = Math.min(item.spanX, 4);
        item.spanX = Math.max(item.spanX, 1);
        item.spanY = Math.min(item.spanY, 4);
        item.spanY = Math.max(item.spanY, 1);

        CellContainer.LayoutParams cellPositionToLayoutPrams = Home.desktop.pages.get(Home.desktop.getCurrentItem()).cellPositionToLayoutPrams(item.x, item.y, item.spanX, item.spanY, (CellContainer.LayoutParams) view.getLayoutParams());
        if (cellPositionToLayoutPrams == null)
            Toast.makeText(Home.desktop.getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
        else {
            LauncherSettings.getInstance(Home.desktop.getContext()).desktopData.get(Home.desktop.getCurrentItem()).remove(item);
            item.x = cellPositionToLayoutPrams.x;
            item.y = cellPositionToLayoutPrams.y;
            LauncherSettings.getInstance(Home.desktop.getContext()).desktopData.get(Home.desktop.getCurrentItem()).add(item);
            view.setLayoutParams(cellPositionToLayoutPrams);

            updateWidgetOption(item);

        }

    }

    private static void updateWidgetOption(Desktop.Item item) {
        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.spanX * Home.desktop.pages.get(Home.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.spanX * Home.desktop.pages.get(Home.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.spanX * Home.desktop.pages.get(Home.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.spanY * Home.desktop.pages.get(Home.desktop.getCurrentItem()).cellHeight);
        Home.appWidgetManager.updateAppWidgetOptions(item.widgetID, newOps);
    }

    //Common methods
    public static Drawable getGroupIconDrawable(Context context, Desktop.Item item) {
        final float iconSize = Tool.convertDpToPixel(LauncherSettings.getInstance(context).generalSettings.iconSize, context);
        final Bitmap[] icons = new Bitmap[4];
        for (int i = 0; i < 4; i++) {
            if (i < item.actions.length) {
                if (item.actions[i].getStringExtra("shortCutIconID") != null)
                    icons[i] = Tool.drawableToBitmap(Tool.getIconFromID(context, item.actions[i].getStringExtra("shortCutIconID")));
                else {
                    AppManager.App app = AppManager.getInstance(context).findApp(item.actions[i].getComponent().getPackageName(), item.actions[i].getComponent().getClassName());
                    icons[i] = Tool.drawableToBitmap(app.icon);
                }
            } else {
                icons[i] = Tool.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        return new GroupIconDrawable(icons, iconSize);
    }
}
