package com.benny.openlauncher.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallBack;
import com.benny.openlauncher.viewutil.GoodDragShadowBuilder;
import com.benny.openlauncher.viewutil.GroupIconDrawable;
import com.benny.openlauncher.viewutil.ItemViewFactory;

import static com.benny.openlauncher.activity.Home.db;

public class GroupPopupView extends FrameLayout {

    public boolean isShowing = false;

    private CardView popupParent;
    private CellContainer cellContainer;
    private PopupWindow.OnDismissListener dismissListener;
    private boolean init = false;

    public GroupPopupView(Context context) {
        super(context);
        init();
    }

    public GroupPopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (isInEditMode()) return;
        init = false;

        bringToFront();
        popupParent = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_group_popup, this, false);
        cellContainer = (CellContainer) popupParent.findViewById(R.id.group);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                popupParent.setBackgroundColor(LauncherSettings.getInstance(getContext()).generalSettings.folderColor);
            }
        }, 2000);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dismissListener != null)
                    dismissListener.onDismiss();
                setVisibility(View.INVISIBLE);
                dismissPopup();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!init) {
            init = true;
            setVisibility(View.INVISIBLE);
        }
    }

    public void dismissPopup() {
        isShowing = false;
        removeAllViews();
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }
        cellContainer.removeAllViews();
        setVisibility(View.INVISIBLE);
    }

    public boolean showWindowV(final Item item, final View itemView, final DesktopCallBack callBack) {
        if (getVisibility() == View.VISIBLE) return false;

        isShowing = true;

        setVisibility(View.VISIBLE);
        popupParent.setVisibility(View.VISIBLE);
        final Context c = itemView.getContext();

        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.items.size());
        cellContainer.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tool.dp2px(LauncherSettings.getInstance(c).generalSettings.iconSize, c);
        int textHeight = Tool.dp2px(22, c);

        int contentPadding = Tool.dp2px(5, c);

        for (int x2 = 0; x2 < cellSize[0]; x2++) {
            for (int y2 = 0; y2 < cellSize[1]; y2++) {
                if (y2 * cellSize[0] + x2 > item.items.size() - 1) {
                    continue;
                }
                final Item dropItem = item.items.get(y2 * cellSize[0] + x2);
                final Intent act = dropItem.appIntent;
                AppItemView.Builder b = new AppItemView.Builder(getContext()).withOnTouchGetPosition();
                b.setTextColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerLabelColor);
                if (act.getStringExtra("shortCutIconID") != null) {
                    b.setShortcutItem(act);
                } else {
                    AppManager.App app = AppManager.getInstance(c).findApp(act.getComponent().getPackageName(), act.getComponent().getClassName());
                    if (app != null) {
                        b.setAppItem(app);
                    }
                }
                final AppItemView view = b.getView();

                view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view2) {
                        removeItem(c, callBack, item, dropItem, (AppItemView) itemView);

                        itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                        // start the drag action
                        Intent i = new Intent();
                        i.putExtra("mDragData", dropItem);
                        ClipData data = ClipData.newIntent("mDragIntent", i);
                        if (act.getStringExtra("shortCutIconID") == null) {
                            itemView.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.APP), 0);
                        } else {
                            itemView.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.SHORTCUT), 0);
                        }

                        dismissPopup();
                        updateItem(c, callBack, item, dropItem, (AppItemView) itemView);
                        return true;
                    }
                });
                if (!view.isShortcut) {
                    final AppManager.App app = AppManager.getInstance(c).findApp(act.getComponent().getPackageName(), act.getComponent().getClassName());
                    if (app == null) {
                        removeItem(c, callBack, item, dropItem, (AppItemView) itemView);
                    } else {
                        view.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Tool.createScaleInScaleOutAnim(view, new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissPopup();
                                        setVisibility(View.INVISIBLE);
                                        Tool.startApp(c, app);
                                    }
                                });
                            }
                        });
                    }
                } else {
                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Tool.createScaleInScaleOutAnim(view, new Runnable() {
                                @Override
                                public void run() {
                                    dismissPopup();
                                    setVisibility(View.INVISIBLE);
                                    view.getContext().startActivity(act);
                                }
                            });
                        }
                    });
                }
                cellContainer.addViewToGrid(view, x2, y2, 1, 1);
            }
        }

        dismissListener = new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (((AppItemView) itemView).getIcon() instanceof GroupIconDrawable) {
                    ((GroupIconDrawable) ((AppItemView) itemView).getIcon()).popBack();
                }
            }
        };

        int popupWidth = contentPadding * 8 + popupParent.getContentPaddingLeft() + popupParent.getContentPaddingRight() + (iconSize) * cellSize[0];
        popupParent.getLayoutParams().width = popupWidth;

        int popupHeight = contentPadding * 2 + popupParent.getContentPaddingTop() + popupParent.getContentPaddingBottom() + Tool.dp2px(30, c) + (iconSize + textHeight) * cellSize[1];
        popupParent.getLayoutParams().height = popupHeight;

        int[] coordinates = new int[2];
        itemView.getLocationInWindow(coordinates);

        coordinates[0] += itemView.getWidth() / 2;
        coordinates[1] += itemView.getHeight() / 2;

        coordinates[0] -= popupWidth / 2;
        coordinates[1] -= popupHeight / 2;

        int width = getWidth();
        int height = getHeight();

        if (coordinates[0] + popupWidth > width) {
            coordinates[0] += width - (coordinates[0] + popupWidth);
        }
        if (coordinates[1] + popupHeight > height) {
            coordinates[1] += height - (coordinates[1] + popupHeight);
        }
        if (coordinates[0] < 0) {
            coordinates[0] -= itemView.getWidth() / 2;
            coordinates[0] += popupWidth / 2;
        }
        if (coordinates[1] < 0) {
            coordinates[1] -= itemView.getHeight() / 2;
            coordinates[1] += popupHeight / 2;
        }

        int x = coordinates[0];
        int y = coordinates[1];

        popupParent.setPivotX(0);
        popupParent.setPivotX(0);
        popupParent.setX(x);
        popupParent.setY(y - 200);

        addView(popupParent);
        return true;
    }

    private void removeItem(Context context, final DesktopCallBack callBack, final Item currentItem, Item dragOutItem, AppItemView currentView) {
        currentItem.items.remove(dragOutItem);

        db.updateItem(dragOutItem, 1);
        db.updateItem(currentItem);

        currentView.setIcon(ItemViewFactory.getGroupIconDrawable(context, currentItem));
    }

    public void updateItem(Context context, final DesktopCallBack callBack, final Item currentItem, Item dragOutItem, AppItemView currentView) {
        if (currentItem.items.size() == 1) {
            final AppManager.App app = AppManager.getInstance(currentView.getContext()).findApp(currentItem.items.get(0).appIntent.getComponent().getPackageName(), currentItem.items.get(0).appIntent.getComponent().getClassName());
            if (app != null) {
                Item item = db.getItem(currentItem.items.get(0).idValue);
                item.x = currentItem.x;
                item.y = currentItem.y;

                db.updateItem(item, item.x, item.y);
                db.updateItem(item, 1);
                db.deleteItem(currentItem);

                callBack.removeItem(currentView);
                callBack.addItemToCell(item, item.x, item.y);
            }
            if (Home.launcher != null) {
                Home.launcher.desktop.requestLayout();
            }
        }
    }

    static class GroupDef {
        static int maxItem = 12;

        static int[] getCellSize(int count) {
            if (count <= 1)
                return new int[]{1, 1};
            if (count <= 2)
                return new int[]{2, 1};
            if (count <= 4)
                return new int[]{2, 2};
            if (count <= 6)
                return new int[]{3, 2};
            if (count <= 9)
                return new int[]{3, 3};
            if (count <= 12)
                return new int[]{4, 3};
            return new int[]{0, 0};
        }
    }
}
