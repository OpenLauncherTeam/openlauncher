package com.benny.openlauncher.widget;

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
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.Definitions.ItemPosition;
import com.benny.openlauncher.util.Definitions.ItemState;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.DragHandler;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallback;
import com.benny.openlauncher.viewutil.GroupDrawable;
import com.benny.openlauncher.viewutil.ItemViewFactory;

import net.gsantner.opoc.util.ContextUtils;

import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;

public class GroupPopupView extends RevealFrameLayout {
    private boolean _isShowing;
    private CardView _popupCard;
    private CellContainer _cellContainer;
    private PopupWindow.OnDismissListener _dismissListener;
    private Animator _folderAnimator;
    private int _cx;
    private int _cy;
    private TextView _textViewGroupName;

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
        _popupCard = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_group_popup, this, false);
        // set the CardView color
        int color = Setup.appSettings().getDesktopFolderColor();
        int alpha = Color.alpha(color);
        _popupCard.setCardBackgroundColor(color);
        // remove elevation if CardView's background is transparent to avoid weird shadows because CardView does not support transparent backgrounds
        if (alpha == 0) {
            _popupCard.setCardElevation(0f);
        }
        _cellContainer = _popupCard.findViewById(R.id.group);

        bringToFront();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_dismissListener != null) {
                    _dismissListener.onDismiss();
                }
                collapse();
            }
        });

        addView(_popupCard);
        _popupCard.setVisibility(View.INVISIBLE);
        setVisibility(View.INVISIBLE);

        _textViewGroupName = _popupCard.findViewById(R.id.group_popup_label);
    }


    public boolean showPopup(final Item item, final View itemView, final DesktopCallback callback) {
        if (_isShowing || getVisibility() == View.VISIBLE) return false;
        _isShowing = true;

        ContextUtils cu = new ContextUtils(_textViewGroupName.getContext());
        String label = item.getLabel();
        _textViewGroupName.setVisibility(label.isEmpty() ? GONE : VISIBLE);
        _textViewGroupName.setText(label);
        _textViewGroupName.setTextColor(cu.shouldColorOnTopBeLight(Setup.appSettings().getDesktopFolderColor()) ? Color.WHITE : Color.BLACK);
        _textViewGroupName.setTypeface(null, Typeface.BOLD);
        cu.freeContextRef();

        final Context context = itemView.getContext();
        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.getGroupItems().size());
        _cellContainer.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tool.dp2px(Setup.appSettings().getDesktopIconSize());
        int textSize = Tool.dp2px(22);
        int contentPadding = Tool.dp2px(6);

        boolean appsChanged = false;

        for (int x2 = 0; x2 < cellSize[0]; x2++) {
            for (int y2 = 0; y2 < cellSize[1]; y2++) {
                if (y2 * cellSize[0] + x2 > item.getGroupItems().size() - 1) {
                    continue;
                }
                final Item groupItem = item.getGroupItems().get(y2 * cellSize[0] + x2);
                if (groupItem == null) {
                    continue;
                }
                final App app = Setup.appLoader().findItemApp(groupItem);
                if (app == null || AppSettings.get().getHiddenAppsList().contains(app.getComponentName())) {
                    deleteItem(context, item, groupItem, (AppItemView) itemView);
                    appsChanged = true;
                    continue;
                } else {
                    final View view = ItemViewFactory.getItemView(getContext(), callback, DragAction.Action.DESKTOP, groupItem);
                    view.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view2) {
                            if (Setup.appSettings().getDesktopLock()) {
                                if (HomeActivity.Companion.getLauncher() != null) {
                                    HomeActivity._launcher.getItemOptionView().showItemPopupForLockedDesktop(groupItem, HomeActivity.Companion.getLauncher());
                                    return true;
                                }
                                return false;
                            } else {
                                removeItem(context, item, groupItem, (AppItemView) itemView);

                                // start the drag action
                                DragHandler.startDrag(view, groupItem, DragAction.Action.DESKTOP, null);

                                collapse();

                                // update group icon or
                                // convert group item into app item if there is only one item left
                                updateItem(callback, item, itemView);
                                return true;
                            }
                        }
                    });
                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Tool.createScaleInScaleOutAnim(view, new Runnable() {
                                @Override
                                public void run() {
                                    collapse();
                                    setVisibility(View.INVISIBLE);
                                    view.getContext().startActivity(groupItem.getIntent());
                                }
                            });
                        }
                    });
                    _cellContainer.addViewToGrid(view, x2, y2, 1, 1);
                }
            }
        }

        _dismissListener = new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (((AppItemView) itemView).getIcon() != null) {
                    ((GroupDrawable) ((AppItemView) itemView).getIcon()).popBack();
                }
            }
        };

        int popupWidth = contentPadding * 8 + _popupCard.getContentPaddingLeft() + _popupCard.getContentPaddingRight() + (iconSize) * cellSize[0];
        _popupCard.getLayoutParams().width = popupWidth;

        int popupHeight = contentPadding * 2 + _popupCard.getContentPaddingTop() + _popupCard.getContentPaddingBottom() + Tool.dp2px(30) + (iconSize + textSize) * cellSize[1];
        _popupCard.getLayoutParams().height = popupHeight;

        _cx = popupWidth / 2;
        _cy = popupHeight / 2 - (Setup.appSettings().getDesktopShowLabel() ? Tool.dp2px(10) : 0);

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
            _cx -= v;
            _cx += contentPadding;
        }
        if (coordinates[1] + popupHeight > height) {
            coordinates[1] += height - (coordinates[1] + popupHeight);
        }
        if (coordinates[0] < 0) {
            coordinates[0] -= itemView.getWidth() / 2;
            coordinates[0] += popupWidth / 2;
            coordinates[0] += contentPadding;
            _cx += itemView.getWidth() / 2;
            _cx -= popupWidth / 2;
            _cx -= contentPadding;
        }
        if (coordinates[1] < 0) {
            coordinates[1] -= itemView.getHeight() / 2;
            coordinates[1] += popupHeight / 2;
        }

        if (item._location == ItemPosition.Dock) {
            coordinates[1] -= iconSize / 2;
            _cy += iconSize / 2 + (Setup.appSettings().getDockShowLabel() ? 0 : Tool.dp2px(10));
        }

        int x = coordinates[0];
        int y = coordinates[1];

        _popupCard.setPivotX(0);
        _popupCard.setPivotX(0);
        _popupCard.setX(x);
        _popupCard.setY(y);

        setVisibility(View.VISIBLE);
        _popupCard.setVisibility(View.VISIBLE);
        expand();

        if (appsChanged) {
            updateItem(callback, item, itemView);
            Toast.makeText(context, R.string.toast_update_group_due_to_missing_items, Toast.LENGTH_LONG).show();
        }

        return true;
    }

    private void expand() {
        _cellContainer.setAlpha(0);

        int finalRadius = Math.max(_popupCard.getWidth(), _popupCard.getHeight());
        int startRadius = Tool.dp2px(Setup.appSettings().getDesktopIconSize() / 2);

        long animDuration = Setup.appSettings().getAnimationSpeed() * 10;
        _folderAnimator = ViewAnimationUtils.createCircularReveal(_popupCard, _cx, _cy, startRadius, finalRadius);
        _folderAnimator.setStartDelay(0);
        _folderAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        _folderAnimator.setDuration(animDuration);
        _folderAnimator.start();
        Tool.visibleViews(animDuration, _cellContainer);
    }

    public void collapse() {
        if (!_isShowing) return;
        if (_folderAnimator == null || _folderAnimator.isRunning())
            return;

        long animDuration = Setup.appSettings().getAnimationSpeed() * 10;
        Tool.invisibleViews(animDuration, _cellContainer);

        int startRadius = Tool.dp2px(Setup.appSettings().getDesktopIconSize() / 2);
        int finalRadius = Math.max(_popupCard.getWidth(), _popupCard.getHeight());
        _folderAnimator = ViewAnimationUtils.createCircularReveal(_popupCard, _cx, _cy, finalRadius, startRadius);
        _folderAnimator.setStartDelay(1 + animDuration / 2);
        _folderAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        _folderAnimator.setDuration(animDuration);
        _folderAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                _popupCard.setVisibility(View.INVISIBLE);
                _isShowing = false;

                if (_dismissListener != null) {
                    _dismissListener.onDismiss();
                }

                _cellContainer.removeAllViews();
                setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator p1) {
            }

            @Override
            public void onAnimationRepeat(Animator p1) {
            }
        });
        _folderAnimator.start();
    }

    private void removeItem(Context context, final Item currentItem, Item dragOutItem, AppItemView currentView) {
        currentItem.getGroupItems().remove(dragOutItem);

        HomeActivity._db.saveItem(dragOutItem, ItemState.Visible);
        HomeActivity._db.saveItem(currentItem);

        currentView.setIcon(new GroupDrawable(context, currentItem, Setup.appSettings().getDesktopIconSize()));
    }

    private void deleteItem(Context context, final Item currentItem, Item dragOutItem, AppItemView currentView) {
        currentItem.getGroupItems().remove(dragOutItem);

        HomeActivity._db.deleteItem(dragOutItem, false);
        HomeActivity._db.saveItem(currentItem);

        currentView.setIcon(new GroupDrawable(context, currentItem, Setup.appSettings().getDesktopIconSize()));
    }

    public void updateItem(DesktopCallback callback, final Item currentItem, View currentView) {
        if (currentItem.getGroupItems().size() == 1) {
            final App app = Setup.appLoader().findItemApp(currentItem.getGroupItems().get(0));
            if (app != null) {
                Item item = HomeActivity._db.getItem(currentItem.getGroupItems().get(0).getId());
                item.setX(currentItem.getX());
                item.setY(currentItem.getY());
                item._location = ItemPosition.Desktop;

                // update db
                HomeActivity._db.saveItem(item);
                HomeActivity._db.saveItem(item, HomeActivity._launcher.getDesktop().getCurrentItem(), ItemPosition.Desktop);
                HomeActivity._db.saveItem(item, ItemState.Visible);
                HomeActivity._db.deleteItem(currentItem, false);

                // update launcher
                callback.removeItem(currentView, false);
                callback.addItemToCell(item, item.getX(), item.getY());
            }
        } else {
            callback.removeItem(currentView, false);
            callback.addItemToCell(currentItem, currentItem.getX(), currentItem.getY());
        }
    }

    static class GroupDef {
        static int _maxItem = 12;

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
