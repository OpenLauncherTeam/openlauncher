package com.benny.openlauncher;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.core.interfaces.IAppDeleteListener;
import com.benny.openlauncher.core.interfaces.IAppItemView;
import com.benny.openlauncher.core.interfaces.IAppUpdateListener;
import com.benny.openlauncher.core.interfaces.IDatabaseHelper;
import com.benny.openlauncher.core.interfaces.IDialogHandler;
import com.benny.openlauncher.core.interfaces.IItem;
import com.benny.openlauncher.core.interfaces.ISettingsManager;
import com.benny.openlauncher.core.manager.StaticSetup;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;
import com.benny.openlauncher.core.widget.Desktop;
import com.benny.openlauncher.model.AppItem;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.benny.openlauncher.viewutil.GroupIconDrawable;
import com.benny.openlauncher.viewutil.ItemViewFactory;
import com.benny.openlauncher.widget.AppItemView;

import java.util.ArrayList;
import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

/**
 * Created by gregor on 07.05.17.
 */

public class App extends Application {
    private static App instance;

    public static App get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initStaticSetup();
    }

    private void initStaticSetup()
    {
        final IDialogHandler dialogHandler = new IDialogHandler() {
            @Override
            public void showPickAction(Context context, final IOnAddAppDrawerItem resultHandler) {
                DialogHelper.addActionItemDialog(context, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                        switch (which) {
                            case 0:
                                resultHandler.onAdd();
                                break;
                        }
                    }
                });
            }

            @Override
            public void showEditDialog(Context context, final IItem item, IOnEditDialog resultHandler) {
                DialogHelper.editItemDialog("Edit Item", item.getLabel(), context, new DialogHelper.onItemEditListener() {
                    @Override
                    public void itemLabel(String label) {
                        item.setLabel(label);
                        Home.db.updateItem(item);

                        Home.launcher.desktop.addItemToCell(item, item.getX(), item.getY());
                        Home.launcher.desktop.removeItem(Home.launcher.desktop.getCurrentPage().coordinateToChildView(new Point(item.getX(), item.getY())));
                    }
                });
            }

            @Override
            public void showDeletePackageDialog(Context context, DragEvent dragEvent) {
                DialogHelper.deletePackageDialog(context, dragEvent);
            }
        };
        StaticSetup.init(new StaticSetup<Home, AppManager.App, Item, AppItem, AppItemView>() {
            @Override
            public Class getItemClass() {
                return Item.class;
            }

            @Override
            public ISettingsManager getAppSettings() {
                return AppSettings.get();
            }

            @Override
            public IDatabaseHelper createDatabaseHelper(Context context) {
                return new DatabaseHelper(context);
            }

            @Override
            public List<AppManager.App> getAllApps(Context context) {
                return AppManager.getInstance(context).getApps();
            }

            @Override
            public List<AppItem> createAllAppItems(Context context) {
                List<AppItem> items = new ArrayList<AppItem>();
                List<AppManager.App> apps = getAllApps(context);
                for (AppManager.App app : apps)
                    items.add(createAppItem(app));
                return items;
            }

            @Override
            public AppItem createAppItem(AppManager.App app) {
                return new AppItem(app);
            }

            @Override
            public View createAppItemView(Context context, final Home home, AppManager.App app, IAppItemView.LongPressCallBack longPressCallBack) {
                return new AppItemView.Builder(context)
                        .setAppItem(app)
                        .withOnTouchGetPosition()
                        .withOnLongClick(app, DragAction.Action.APP_DRAWER, new IAppItemView.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return AppSettings.get().getDesktopStyle() != Desktop.DesktopMode.SHOW_ALL_APPS;
                            }

                            @Override
                            public void afterDrag(View view) {
                                home.closeAppDrawer();
                            }
                        })
                        .setLabelVisibility(AppSettings.get().isDrawerShowLabel())
                        .setTextColor(AppSettings.get().getDrawerLabelColor())
                        .getView();
            }

            @Override
            public IAppItemView createAppItemViewPopup(Context context, Item groupItem, AppManager.App item) {
                AppItemView.Builder b = new AppItemView.Builder(context).withOnTouchGetPosition();
                b.setTextColor(AppSettings.get().getDrawerLabelColor());
                if (groupItem.type == Item.Type.SHORTCUT) {
                    b.setShortcutItem(groupItem);
                } else {
                    AppManager.App app = AppManager.getInstance(context).findApp(groupItem.intent);
                    if (app != null) {
                        b.setAppItem(groupItem, app);
                    }
                }
                final AppItemView view = b.getView();
                return view;
            }

            @Override
            public Item newGroupItem() {
                return Item.newGroupItem();
            }

            @Override
            public Item newWidgetItem(int appWidgetId) {
                return Item.newWidgetItem(appWidgetId);
            }

            @Override
            public Item newActionItem(int action) {
                return Item.newActionItem(action);
            }

            @Override
            public View getItemView(Context context, ISettingsManager appSettings, Item item, DesktopCallBack callBack) {
                int flag = appSettings.isDesktopShowLabel() ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
                View itemView = ItemViewFactory.getItemView(context, callBack, item, flag);
                return itemView;
            }

            @Override
            public SimpleFingerGestures.OnFingerGestureListener getDesktopGestureListener(final Desktop desktop) {
                return new SimpleFingerGestures.OnFingerGestureListener() {
                    @Override
                    public boolean onSwipeUp(int i, long l, double v) {
                        LauncherAction.ActionItem gesture = ((DatabaseHelper)Home.db).getGesture(1);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }

                    @Override
                    public boolean onSwipeDown(int i, long l, double v) {
                        LauncherAction.ActionItem gesture = ((DatabaseHelper)Home.db).getGesture(2);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }

                    @Override
                    public boolean onSwipeLeft(int i, long l, double v) {
                        return false;
                    }

                    @Override
                    public boolean onSwipeRight(int i, long l, double v) {
                        return false;
                    }

                    @Override
                    public boolean onPinch(int i, long l, double v) {
                        LauncherAction.ActionItem gesture = ((DatabaseHelper)Home.db).getGesture(3);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }

                    @Override
                    public boolean onUnpinch(int i, long l, double v) {
                        LauncherAction.ActionItem gesture = ((DatabaseHelper)Home.db).getGesture(4);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(int i) {
                        LauncherAction.ActionItem gesture = ((DatabaseHelper)Home.db).getGesture(0);
                        if (gesture != null && AppSettings.get().isGestureFeedback())
                            Tool.vibrate(desktop);
                        LauncherAction.RunAction(gesture, desktop.getContext());
                        return true;
                    }
                };
            }

            @Override
            public Item createShortcut(Intent intent, Drawable icon, String name) {
                return Item.newShortcutItem(intent, icon, name);
            }

            @Override
            public void showLauncherSettings(Context context) {
                LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
            }

            @Override
            public IDialogHandler getDialogHandler() {
                return dialogHandler;
            }

            @Override
            public void addAppUpdatedListener(Context c, IAppUpdateListener listener) {
                AppManager.getInstance(c).addAppUpdatedListener(listener);
            }

            @Override
            public void removeAppUpdatedListener(Context c, IAppUpdateListener<AppManager.App> listener) {
                AppManager.getInstance(c).removeAppUpdatedListener(listener);
            }

            @Override
            public void addAppDeletedListener(Context c, IAppDeleteListener<AppManager.App> listener) {
                AppManager.getInstance(c).addAppDeletedListener(listener);
            }

            @Override
            public void onAppUpdated(Context p1, Intent p2) {
                AppManager.getInstance(p1).onReceive(p1, p2);
            }

            @Override
            public AppManager.App findApp(Context c, Intent intent) {
                return AppManager.getInstance(c).findApp(intent);
            }

            @Override
            public void updateIcon(Context context, AppItemView currentView, Item currentItem) {
                currentView.setIcon(new GroupIconDrawable(context, currentItem));
            }

            @Override
            public void onItemViewDismissed(IAppItemView itemView) {
                if (itemView.getIcon() instanceof GroupIconDrawable) {
                    ((GroupIconDrawable) itemView.getIcon()).popBack();
                }
            }
        });
    }
}