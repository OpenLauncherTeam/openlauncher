package com.benny.openlauncher.core.widget;

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

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.IApp;
import com.benny.openlauncher.core.interfaces.IAppItemView;
import com.benny.openlauncher.core.interfaces.IItem;
import com.benny.openlauncher.core.manager.StaticSetup;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;
import com.benny.openlauncher.core.viewutil.GoodDragShadowBuilder;

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
        if (isInEditMode()) {
            return;
        }
        popupParent = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_group_popup, this, false);
        cellContainer = (CellContainer) popupParent.findViewById(R.id.group);

        bringToFront();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dismissListener != null) {
                    dismissListener.onDismiss();
                }
                dismissPopup();
            }
        });
    }

    public void dismissPopup() {
        removeAllViews();

        if (dismissListener != null) {
            dismissListener.onDismiss();
        }

        cellContainer.removeAllViews();
        setVisibility(View.INVISIBLE);
    }

    public <T extends IItem, V extends View & IAppItemView> boolean showWindowV(final T item, final View itemView, final DesktopCallBack callBack) {
        if (getVisibility() == View.VISIBLE) return false;

        //popupParent.setBackgroundColor(LauncherSettings.getInstance(getContext()).generalSettings.folderColor);

        setVisibility(View.VISIBLE);
        popupParent.setVisibility(View.VISIBLE);

        final Context c = itemView.getContext();
        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.getGroupItems().size());
        cellContainer.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tool.dp2px(StaticSetup.get().getAppSettings().getIconSize(), c);
        int textSize = Tool.dp2px(22, c);
        int contentPadding = Tool.dp2px(5, c);

        for (int x2 = 0; x2 < cellSize[0]; x2++) {
            for (int y2 = 0; y2 < cellSize[1]; y2++) {
                if (y2 * cellSize[0] + x2 > item.getGroupItems().size() - 1) {
                    continue;
                }
                final IItem groupItem = (IItem) item.getGroupItems().get(y2 * cellSize[0] + x2);
                IApp groupApp = null;
                if (groupItem.getType() != IItem.Type.SHORTCUT) {
                    groupApp = StaticSetup.get().findApp(c, groupItem.getIntent());
                }
                IAppItemView appItemView = StaticSetup.get().createAppItemViewPopup(getContext(), groupItem, groupApp);
                final View view = appItemView.getView();

                view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view2) {
                        removeItem(c, callBack, item, groupItem, (V) itemView);

                        itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                        // start the drag action
                        Intent i = new Intent();
                        i.putExtra("mDragData", groupItem);
                        ClipData data = ClipData.newIntent("mDragIntent", i);
                        if (groupItem.getType() == IItem.Type.SHORTCUT) {
                            itemView.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.APP), 0);
                        } else {
                            itemView.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.SHORTCUT), 0);
                        }

                        dismissPopup();
                        updateItem(c, callBack, item, groupItem, itemView);
                        return true;
                    }
                });
                final IApp app = StaticSetup.get().findApp(c, groupItem.getIntent());
                if (app == null) {
                    removeItem(c, callBack, item, groupItem, (V)itemView);
                } else {
                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Tool.createScaleInScaleOutAnim(view, new Runnable() {
                                @Override
                                public void run() {
                                    dismissPopup();
                                    setVisibility(View.INVISIBLE);
                                    view.getContext().startActivity(groupItem.getIntent());
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
                StaticSetup.get().onItemViewDismissed((IAppItemView)itemView);
            }
        };

        int popupWidth = contentPadding * 8 + popupParent.getContentPaddingLeft() + popupParent.getContentPaddingRight() + (iconSize) * cellSize[0];
        popupParent.getLayoutParams().width = popupWidth;

        int popupHeight = contentPadding * 2 + popupParent.getContentPaddingTop() + popupParent.getContentPaddingBottom() + Tool.dp2px(30, c) + (iconSize + textSize) * cellSize[1];
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

    private <V extends View & IAppItemView> void removeItem(Context context, final DesktopCallBack callBack, final IItem currentItem, IItem dragOutItem, V currentView) {
        currentItem.getGroupItems().remove(dragOutItem);

        Home.db.updateItem(dragOutItem, 1);
        Home.db.updateItem(currentItem);

        StaticSetup.get().updateIcon(context, currentView, currentItem);
    }

    public void updateItem(Context context, final DesktopCallBack callBack, final IItem<IItem> currentItem, IItem dragOutItem, View currentView) {
        if (currentItem.getGroupItems().size() == 1) {
            final IApp app = StaticSetup.get().findApp(context, currentItem.getGroupItems().get(0).getIntent());
            if (app != null) {
                IItem item = Home.db.getItem(currentItem.getGroupItems().get(0).getId());
                item.setX(currentItem.getX());
                item.setY(currentItem.getY());

                Home.db.updateItem(item);
                Home.db.updateItem(item, 1);
                Home.db.deleteItem(currentItem);

                callBack.removeItem(currentView);
                callBack.addItemToCell(item, item.getX(), item.getY());
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
