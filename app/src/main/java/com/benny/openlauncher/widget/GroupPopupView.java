package com.benny.openlauncher.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallBack;
import com.benny.openlauncher.viewutil.GoodDragShadowBuilder;
import com.benny.openlauncher.viewutil.GroupIconDrawable;
import com.benny.openlauncher.viewutil.ItemViewFactory;

import static com.benny.openlauncher.activity.Home.resources;

public class GroupPopupView extends FrameLayout {

    public boolean isShowing = false;
    private CardView popupParent;
    private CellContainer cellContainer;
    private TextView title;

    private boolean init = false;

    private PopupWindow.OnDismissListener dismissListener;

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
        popupParent = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_grouppopup, this, false);
        cellContainer = (CellContainer) popupParent.findViewById(R.id.cc);
        title = (TextView) popupParent.findViewById(R.id.tv);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                title.setTextColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerLabelColor);
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
        if (dismissListener != null)
            dismissListener.onDismiss();
        cellContainer.removeAllViews();
        setVisibility(View.INVISIBLE);
    }

    public boolean showWindowV(final Desktop.Item item, final View itemView, final DesktopCallBack callBack) {
        if (getVisibility() == View.VISIBLE) return false;

        isShowing = true;

        setVisibility(View.VISIBLE);
        popupParent.setVisibility(View.VISIBLE);
        final Context c = itemView.getContext();

        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.actions.length);
        cellContainer.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tool.dp2px(LauncherSettings.getInstance(c).generalSettings.iconSize, c);
        int textHeight = Tool.dp2px(22, c);

        int contentPadding = Tool.dp2px(5, c);

        for (int x2 = 0; x2 < cellSize[0]; x2++) {
            for (int y2 = 0; y2 < cellSize[1]; y2++) {
                if (y2 * cellSize[0] + x2 > item.actions.length - 1) continue;
                final Intent act = item.actions[y2 * cellSize[0] + x2];
                AppItemView.Builder b = new AppItemView.Builder(getContext()).withOnTouchGetPosition();
                b.setTextColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerLabelColor);
                if (act.getStringExtra("shortCutIconID") != null) {
                    b.setShortcutItem(act);
                } else {
                    AppManager.App app = AppManager.getInstance(c).findApp(act.getComponent().getPackageName(), act.getComponent().getClassName());
                    if (app != null)
                        b.setAppItem(app);
                }
                final AppItemView view = b.getView();

                view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view2) {
                        removeItem(c, callBack, item, act, (AppItemView) itemView);

                        itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        Intent i = new Intent();

                        if (act.getStringExtra("shortCutIconID") == null) {
                            i.putExtra("mDragData", Desktop.Item.newAppItem(AppManager.getInstance(c).findApp(act.getComponent().getPackageName(), act.getComponent().getClassName())));
                            ClipData data = ClipData.newIntent("mDragIntent", i);
                            itemView.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.ACTION_APP), 0);
                        } else {
                            i.putExtra("mDragData", Desktop.Item.newShortcutItem(act));
                            ClipData data = ClipData.newIntent("mDragIntent", i);
                            itemView.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.ACTION_SHORTCUT), 0);
                        }
                        dismissPopup();
                        return true;
                    }
                });
                if (!view.isShortcut) {
                    final AppManager.App app = AppManager.getInstance(c).findApp(act.getComponent().getPackageName(), act.getComponent().getClassName());
                    if (app == null) {
                        removeItem(c, callBack, item, act, (AppItemView) itemView);
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
        title.setText(item.name);
        title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Tool.DialogHelper.promptInputText(resources.getString(R.string.toast_rename), item.name, getContext(), new Tool.DialogHelper.OnTextResultListener() {
                    @Override
                    public void hereIsTheText(String str) {
                        if (str.isEmpty()) return;
                        callBack.removeItemFromSettings(item);
                        item.name = str;
                        callBack.addItemToSettings(item);

                        title.setText(str);
                    }
                }).show();
            }
        });

        dismissListener = new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppItemView otv = ((AppItemView) itemView);
                if (!otv.getLabel().isEmpty())
                    otv.setLabel(title.getText().toString());
                if (((AppItemView) itemView).getIcon() instanceof GroupIconDrawable)
                    ((GroupIconDrawable) ((AppItemView) itemView).getIcon()).popBack();
            }
        };

        int popupWidth = contentPadding * 8 + popupParent.getContentPaddingLeft() + popupParent.getContentPaddingRight() + (iconSize) * cellSize[0];
        popupParent.getLayoutParams().width = popupWidth;
        int popupHeight = contentPadding * 2 + popupParent.getContentPaddingTop() + popupParent.getContentPaddingBottom() + Tool.dp2px(30, c)
                + (iconSize + textHeight) * cellSize[1];
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

    private void removeItem(Context context, final DesktopCallBack callBack, final Desktop.Item currentItem, Intent dragOutItem, AppItemView currentView) {
        callBack.removeItemFromSettings(currentItem);
        currentItem.removeActions(dragOutItem);
        if (currentItem.actions.length == 1) {
            currentItem.type = Desktop.Item.Type.APP;


            AppItemView.Builder builder = new AppItemView.Builder(currentView);
            final AppManager.App app = AppManager.getInstance(currentView.getContext()).findApp(currentItem.actions[0].getComponent().getPackageName(), currentItem.actions[0].getComponent().getClassName());

            if (app != null) {
                currentItem.name = app.label;
                builder.setAppItem(app).withOnClickLaunchApp(app).withOnLongPressDrag(currentItem, DragAction.Action.ACTION_APP, new AppItemView.Builder.LongPressCallBack() {
                    @Override
                    public boolean readyForDrag(View view) {
                        return true;
                    }

                    @Override
                    public void afterDrag(View view) {
                        callBack.setLastItem(currentItem, view);
                    }
                });
            }
            if (Home.launcher != null)
                Home.launcher.desktop.requestLayout();
        } else {
            currentView.setIcon(ItemViewFactory.getGroupIconDrawable(context, currentItem));
        }
        callBack.addItemToSettings(currentItem);
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
