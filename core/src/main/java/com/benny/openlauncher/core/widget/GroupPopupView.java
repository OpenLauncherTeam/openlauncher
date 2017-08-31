package com.benny.openlauncher.core.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.PopupWindow;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.Definitions;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.util.DragDropHandler;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;
import com.benny.openlauncher.core.viewutil.GroupIconDrawable;

import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;

public class GroupPopupView extends RevealFrameLayout {

    private boolean isShowing;
    private CardView popupCard;
    private CellContainer cellContainer;
    private PopupWindow.OnDismissListener dismissListener;
    private Animator folderAnimator;
    private static final Long folderAnimationTime = 200L;
    private int cx;
    private int cy;

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
        popupCard = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_group_popup, this, false);
        // set the CardView color
        int color = Setup.appSettings().getDesktopFolderColor();
        int alpha = Color.alpha(color);
        popupCard.setCardBackgroundColor(color);
        // remove elevation if CardView's background is transparent to avoid weird shadows because CardView does not support transparent backgrounds
        if (alpha != 0) {
            popupCard.setCardElevation(0f);
        }
        cellContainer = popupCard.findViewById(R.id.group);

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

        addView(popupCard);
        popupCard.setVisibility(View.INVISIBLE);
        setVisibility(View.INVISIBLE);
    }


    public boolean showWindowV(final Item item, final View itemView, final DesktopCallBack callBack) {
        if (isShowing || getVisibility() == View.VISIBLE) return false;
        isShowing = true;

        final Context c = itemView.getContext();
        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.getGroupItems().size());
        cellContainer.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tool.dp2px(Setup.appSettings().getDesktopIconSize(), c);
        int textSize = Tool.dp2px(22, c);
        int contentPadding = Tool.dp2px(5, c);

        for (int x2 = 0; x2 < cellSize[0]; x2++) {
            for (int y2 = 0; y2 < cellSize[1]; y2++) {
                if (y2 * cellSize[0] + x2 > item.getGroupItems().size() - 1) {
                    continue;
                }
                final Item groupItem = item.getGroupItems().get(y2 * cellSize[0] + x2);
                final App groupApp = groupItem.getType() != Item.Type.SHORTCUT ? Setup.appLoader().findItemApp(groupItem) : null;
                AppItemView appItemView = AppItemView.createAppItemViewPopup(getContext(), groupItem, groupApp, Setup.appSettings().getDesktopIconSize());
                final View view = appItemView.getView();

                view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view2) {
                        if (Setup.appSettings().isDesktopLock()) return false;

                        removeItem(c, callBack, item, groupItem, (AppItemView) itemView);

                        itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                        DragAction.Action action = groupItem.getType() == Item.Type.SHORTCUT ? DragAction.Action.SHORTCUT : DragAction.Action.APP;

                        // start the drag action
                        DragDropHandler.startDrag(view, groupItem, action, null);

                        dismissPopup();
                        updateItem(c, callBack, item, groupItem, itemView);
                        return true;
                    }
                });
                final App app = Setup.appLoader().findItemApp(groupItem);
                if (app == null) {
                    removeItem(c, callBack, item, groupItem, (AppItemView) itemView);
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
                if (((AppItemView) itemView).getIconProvider().isGroupIconDrawable()) {
                    if (((AppItemView) itemView).getCurrentIcon() != null) {
                        ((GroupIconDrawable) ((AppItemView) itemView).getCurrentIcon()).popBack();
                    }
                }
            }
        };

        int popupWidth = contentPadding * 8 + popupCard.getContentPaddingLeft() + popupCard.getContentPaddingRight() + (iconSize) * cellSize[0];
        popupCard.getLayoutParams().width = popupWidth;

        int popupHeight = contentPadding * 2 + popupCard.getContentPaddingTop() + popupCard.getContentPaddingBottom() + Tool.dp2px(30, c) + (iconSize + textSize) * cellSize[1];
        popupCard.getLayoutParams().height = popupHeight;

        cx = popupWidth / 2;
        cy = popupHeight / 2;

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

        popupCard.setPivotX(0);
        popupCard.setPivotX(0);
        popupCard.setX(x);
        popupCard.setY(y - 200);

        setVisibility(View.VISIBLE);
        popupCard.setVisibility(View.VISIBLE);
        animateFolderOpen(itemView);

        return true;
    }

    private void animateFolderOpen(View itemView) {
        int finalRadius = Math.max(popupCard.getWidth(), popupCard.getHeight());
        folderAnimator = ViewAnimationUtils.createCircularReveal(popupCard, cx, cy, 0, finalRadius);
        folderAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        folderAnimator.setDuration(folderAnimationTime);
        folderAnimator.start();
    }

    public void dismissPopup() {
        if (!isShowing) return;
        if (folderAnimator == null || folderAnimator.isRunning())
            return;

        int finalRadius = Math.max(popupCard.getWidth(), popupCard.getHeight());
        folderAnimator = ViewAnimationUtils.createCircularReveal(popupCard, cx, cy, finalRadius, 0);
        folderAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        folderAnimator.setDuration(folderAnimationTime);
        folderAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                popupCard.setVisibility(View.INVISIBLE);
                isShowing = false;

                if (dismissListener != null) {
                    dismissListener.onDismiss();
                }

                cellContainer.removeAllViews();
                setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator p1) {
            }

            @Override
            public void onAnimationRepeat(Animator p1) {
            }
        });
        folderAnimator.start();
    }

    private void removeItem(Context context, final DesktopCallBack callBack, final Item currentItem, Item dragOutItem, AppItemView currentView) {
        currentItem.getGroupItems().remove(dragOutItem);

        Home.db.updateSate(dragOutItem, Definitions.ItemState.Visible);
        Home.db.saveItem(currentItem);

        currentView.setIconProvider(Setup.imageLoader().createIconProvider(new GroupIconDrawable(context, currentItem, Setup.appSettings().getDesktopIconSize())));
    }

    public void updateItem(Context context, final DesktopCallBack callBack, final Item currentItem, Item dragOutItem, View currentView) {
        if (currentItem.getGroupItems().size() == 1) {
            final App app = Setup.appLoader().findItemApp(currentItem.getGroupItems().get(0));
            if (app != null) {
                Item item = Home.db.getItem(currentItem.getGroupItems().get(0).getId());
                item.setX(currentItem.getX());
                item.setY(currentItem.getY());

                Home.db.saveItem(item);
                Home.db.updateSate(item, Definitions.ItemState.Visible);
                Home.db.deleteItem(currentItem, true);

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
