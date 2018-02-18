package com.benny.openlauncher.core.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.CoreHome;
import com.benny.openlauncher.core.interfaces.AbstractApp;
import com.benny.openlauncher.core.interfaces.SettingsManager;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.Definitions;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.util.DragNDropHandler;
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
    private int cx;
    private int cy;
    private TextView textView;

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
        int color = Setup.Companion.appSettings().getDesktopFolderColor();
        int alpha = Color.alpha(color);
        popupCard.setCardBackgroundColor(color);
        // remove elevation if CardView's background is transparent to avoid weird shadows because CardView does not support transparent backgrounds
        if (alpha == 0) {
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

        textView = popupCard.findViewById(R.id.group_popup_label);
    }


    public boolean showWindowV(final Item item, final View itemView, final DesktopCallBack callBack) {
        if (isShowing || getVisibility() == View.VISIBLE) return false;
        isShowing = true;

        String label = item.getLabel();
        textView.setVisibility(label.isEmpty() ? GONE : VISIBLE);
        textView.setText(label);
        textView.setTextColor(Setup.appSettings().getFolderLabelColor());
        textView.setTypeface(null, Typeface.BOLD);

        final Context c = itemView.getContext();
        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.getGroupItems().size());
        cellContainer.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tool.dp2px(Setup.Companion.appSettings().getDesktopIconSize(), c);
        int textSize = Tool.dp2px(22, c);
        int contentPadding = Tool.dp2px(6, c);

        for (int x2 = 0; x2 < cellSize[0]; x2++) {
            for (int y2 = 0; y2 < cellSize[1]; y2++) {
                if (y2 * cellSize[0] + x2 > item.getGroupItems().size() - 1) {
                    continue;
                }
                final SettingsManager settingsManager = Setup.Companion.appSettings();
                final Item groupItem = item.getGroupItems().get(y2 * cellSize[0] + x2);
                final AbstractApp groupApp = groupItem.getType() != Item.Type.SHORTCUT ? Setup.Companion.appLoader().findItemApp(groupItem) : null;
                AppItemView appItemView = AppItemView.createAppItemViewPopup(getContext(), groupItem, groupApp, settingsManager.getDesktopIconSize(), settingsManager.getDrawerLabelFontSize());
                final View view = appItemView.getView();

                view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view2) {
                        if (Setup.Companion.appSettings().isDesktopLock()) return false;

                        removeItem(c, callBack, item, groupItem, (AppItemView) itemView);

                        DragAction.Action action = groupItem.getType() == Item.Type.SHORTCUT ? DragAction.Action.SHORTCUT : DragAction.Action.APP;

                        // start the drag action
                        DragNDropHandler.startDrag(view, groupItem, action, null);

                        dismissPopup();
                        updateItem(callBack, item, groupItem, itemView);
                        return true;
                    }
                });
                final AbstractApp app = Setup.Companion.appLoader().findItemApp(groupItem);
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
                            }, 1f);
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
        cy = popupHeight / 2 - (Setup.Companion.appSettings().isDesktopShowLabel() ? Tool.dp2px(10, getContext()) : 0);

        int[] coordinates = new int[2];
        itemView.getLocationInWindow(coordinates);

        coordinates[0] += itemView.getWidth() / 2;
        coordinates[1] += itemView.getHeight() / 2;

        coordinates[0] -= popupWidth / 2;
        coordinates[1] -= popupHeight / 2;

        int width = getWidth();
        int height = getHeight();

        if (coordinates[0] + popupWidth > width) {
            int v = width - (coordinates[0] + popupWidth);
            coordinates[0] += v;
            coordinates[0] -= contentPadding;
            cx -= v;
            cx += contentPadding;
        }
        if (coordinates[1] + popupHeight > height) {
            coordinates[1] += height - (coordinates[1] + popupHeight);
        }
        if (coordinates[0] < 0) {
            coordinates[0] -= itemView.getWidth() / 2;
            coordinates[0] += popupWidth / 2;
            coordinates[0] += contentPadding;
            cx += itemView.getWidth() / 2;
            cx -= popupWidth / 2;
            cx -= contentPadding;
        }
        if (coordinates[1] < 0) {
            coordinates[1] -= itemView.getHeight() / 2;
            coordinates[1] += popupHeight / 2;
        }

        if (item.getLocationInLauncher() == Item.Companion.getLOCATION_DOCK()) {
            coordinates[1] -= iconSize / 2;
            cy += iconSize / 2 + (Setup.Companion.appSettings().isDockShowLabel() ? 0 : Tool.dp2px(10, getContext()));
        }

        int x = coordinates[0];
        int y = coordinates[1];

        popupCard.setPivotX(0);
        popupCard.setPivotX(0);
        popupCard.setX(x);
        popupCard.setY(y);

        setVisibility(View.VISIBLE);
        popupCard.setVisibility(View.VISIBLE);
        animateFolderOpen();

        return true;
    }

    private void animateFolderOpen() {
        cellContainer.setAlpha(0);

        int finalRadius = Math.max(popupCard.getWidth(), popupCard.getHeight());
        int startRadius = Tool.dp2px(Setup.Companion.appSettings().getDesktopIconSize() / 2, getContext());

        long animDuration = 1 + (long) (210 * Setup.appSettings().getOverallAnimationSpeedModifier());
        folderAnimator = ViewAnimationUtils.createCircularReveal(popupCard, cx, cy, startRadius, finalRadius);
        folderAnimator.setStartDelay(0);
        folderAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        folderAnimator.setDuration(animDuration);
        folderAnimator.start();
        Tool.visibleViews(animDuration, animDuration, cellContainer);
    }

    public void dismissPopup() {
        if (!isShowing) return;
        if (folderAnimator == null || folderAnimator.isRunning())
            return;

        long animDuration = 1 + (long) (210 * Setup.appSettings().getOverallAnimationSpeedModifier());
        Tool.INSTANCE.invisibleViews(animDuration, cellContainer);

        int startRadius = Tool.dp2px(Setup.Companion.appSettings().getDesktopIconSize() / 2, getContext());
        int finalRadius = Math.max(popupCard.getWidth(), popupCard.getHeight());
        folderAnimator = ViewAnimationUtils.createCircularReveal(popupCard, cx, cy, finalRadius, startRadius);
        folderAnimator.setStartDelay(1 + animDuration / 2);
        folderAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        folderAnimator.setDuration(animDuration);
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

        CoreHome.Companion.getDb().saveItem(dragOutItem, Definitions.ItemState.Visible);
        CoreHome.Companion.getDb().saveItem(currentItem);

        currentView.setIconProvider(Setup.Companion.imageLoader().createIconProvider(new GroupIconDrawable(context, currentItem, Setup.Companion.appSettings().getDesktopIconSize())));
    }

    public void updateItem(final DesktopCallBack callBack, final Item currentItem, Item dragOutItem, View currentView) {
        if (currentItem.getGroupItems().size() == 1) {
            final AbstractApp app = Setup.appLoader().findItemApp(currentItem.getGroupItems().get(0));
            if (app != null) {
                //Creating a new app item fixed the folder crash bug
                Item item = Item.newAppItem(app); //CoreHome.Companion.getDb().getItem(currentItem.getGroupItems().get(0).getId());

                item.setX(currentItem.getX());
                item.setY(currentItem.getY());

                CoreHome.Companion.getDb().saveItem(item);
                CoreHome.Companion.getDb().saveItem(item, Definitions.ItemState.Visible);
                CoreHome.Companion.getDb().deleteItem(currentItem, true);

                callBack.removeItem(currentView, false);
                Tool.print("_______________________");
                callBack.addItemToCell(item, item.getX(), item.getY());
            }
            if (CoreHome.Companion.getLauncher() != null) {
                CoreHome.Companion.getLauncher().getDesktop().requestLayout();
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
