package com.benny.openlauncher.activity.homeparts;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.model.PopupIconLabelItem;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.DragAction.Action;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.CellContainer;
import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.DragNDropLayout;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HpDragNDrop {
    private final int uninstallItemIdentifier = 83;
    private final int infoItemIdentifier = 84;
    private final int editItemIdentifier = 85;
    private final int removeItemIdentifier = 86;

    private PopupIconLabelItem uninstallItem = new PopupIconLabelItem(R.string.uninstall, R.drawable.ic_delete_dark_24dp).withIdentifier(uninstallItemIdentifier);
    private PopupIconLabelItem infoItem = new PopupIconLabelItem(R.string.info, R.drawable.ic_info_outline_dark_24dp).withIdentifier(infoItemIdentifier);
    private PopupIconLabelItem editItem = new PopupIconLabelItem(R.string.edit, R.drawable.ic_edit_black_24dp).withIdentifier(editItemIdentifier);
    private PopupIconLabelItem removeItem = new PopupIconLabelItem(R.string.remove, R.drawable.ic_close_dark_24dp).withIdentifier(removeItemIdentifier);


    public void initDragNDrop(@NotNull final Home _home, @NotNull final View leftDragHandle, @NotNull final View rightDragHandle, @NotNull final DragNDropLayout dragNDropView) {
        //dragHandle's drag event
        final Handler dragHandler = new Handler();

        dragNDropView.registerDropTarget(new DragNDropLayout.DropTargetListener(leftDragHandle) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int i = _home.getDesktop().getCurrentItem();
                    if (i > 0) {
                        _home.getDesktop().setCurrentItem(i - 1);
                    } else if (i == 0) {
                        _home.getDesktop().addPageLeft(true);
                    }
                    dragHandler.postDelayed(this, 1000);
                }
            };

            @Override
            public boolean onStart(@NonNull Action action, @NonNull PointF location, boolean isInside) {
                switch (action) {
                    case APP:
                    case WIDGET:
                    case SEARCH_RESULT:
                    case APP_DRAWER:
                    case GROUP:
                    case SHORTCUT:
                    case ACTION:
                        return true;
                }
                return false;
            }

            @Override
            public void onStartDrag(@NonNull Action action, @NonNull PointF location) {
                if (leftDragHandle.getAlpha() >= -0.01 && leftDragHandle.getAlpha() <= 0.01)
                    leftDragHandle.animate().alpha(0.5f);
            }

            @Override
            public void onEnter(@NonNull Action action, @NonNull PointF location) {
                dragHandler.post(runnable);
                leftDragHandle.animate().alpha(0.9f);
            }

            @Override
            public void onExit(@NonNull Action action, @NonNull PointF location) {
                dragHandler.removeCallbacksAndMessages(null);
                leftDragHandle.animate().alpha(0.5f);
            }

            @Override
            public void onEnd() {
                dragHandler.removeCallbacksAndMessages(null);
                leftDragHandle.animate().alpha(0f);
            }
        });


        dragNDropView.registerDropTarget(new DragNDropLayout.DropTargetListener(rightDragHandle) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int i = _home.getDesktop().getCurrentItem();
                    if (i < _home.getDesktop().getPageCount() - 1) {
                        _home.getDesktop().setCurrentItem(i + 1);
                    } else if (i == _home.getDesktop().getPageCount() - 1) {
                        _home.getDesktop().addPageRight(true);
                    }
                    dragHandler.postDelayed(this, 1000);
                }
            };

            @Override
            public boolean onStart(@NonNull Action action, @NonNull PointF location, boolean isInside) {
                switch (action) {
                    case APP:
                    case WIDGET:
                    case SEARCH_RESULT:
                    case APP_DRAWER:
                    case GROUP:
                    case SHORTCUT:
                    case ACTION:
                        return true;
                }
                return false;
            }

            @Override
            public void onStartDrag(@NonNull Action action, @NonNull PointF location) {
                if (rightDragHandle.getAlpha() >= -0.01 && rightDragHandle.getAlpha() <= 0.01)
                    rightDragHandle.animate().alpha(0.5f);
            }

            @Override
            public void onEnter(@NonNull Action action, @NonNull PointF location) {
                dragHandler.post(runnable);
                rightDragHandle.animate().alpha(0.9f);
            }

            @Override
            public void onExit(@NonNull Action action, @NonNull PointF location) {
                dragHandler.removeCallbacksAndMessages(null);
                rightDragHandle.animate().alpha(0.5f);
            }

            @Override
            public void onEnd() {
                dragHandler.removeCallbacksAndMessages(null);
                rightDragHandle.animate().alpha(0f);
            }
        });


        //desktop's drag event
        dragNDropView.registerDropTarget(new DragNDropLayout.DropTargetListener(_home.getDesktop()) {
            @Override
            public boolean onStart(@NonNull Action action, @NonNull PointF location, boolean isInside) {
                if (!DragAction.Action.SEARCH_RESULT.equals(action))
                    showItemPopup(dragNDropView, _home);
                return true;
            }

            @Override
            public void onStartDrag(@NonNull Action action, @NonNull PointF location) {
                _home.closeAppDrawer();
            }

            @Override
            public void onDrop(@NonNull Action action, @NonNull PointF location, @NonNull Item item) {
                // this statement makes sure that adding an app multiple times from the app drawer works
                // the app will get a new id every time
                if (DragAction.Action.APP_DRAWER.equals(action)) {
                    if (_home.getAppDrawerController()._isOpen) {
                        return;
                    }
                    item.reset();
                }

                int x = (int) location.x;
                int y = (int) location.y;
                if (_home.getDesktop().addItemToPoint(item, x, y)) {
                    _home.getDesktop().consumeRevert();
                    _home.getDock().consumeRevert();
                    // add the item to the database
                    Home._db.saveItem(item, _home.getDesktop().getCurrentItem(), Definitions.ItemPosition.Desktop);
                } else {
                    Point pos = new Point();
                    _home.getDesktop().getCurrentPage().touchPosToCoordinate(pos, x, y, item._spanX, item._spanY, false);
                    View itemView = _home.getDesktop().getCurrentPage().coordinateToChildView(pos);
                    if (itemView != null && Desktop.handleOnDropOver(_home, item, (Item) itemView.getTag(), itemView, _home.getDesktop().getCurrentPage(), _home.getDesktop().getCurrentItem(), Definitions.ItemPosition.Desktop, _home.getDesktop())) {
                        _home.getDesktop().consumeRevert();
                        _home.getDock().consumeRevert();
                    } else {
                        Tool.toast(_home, R.string.toast_not_enough_space);
                        _home.getDesktop().revertLastItem();
                        _home.getDock().revertLastItem();
                    }
                }
            }

            @Override
            public void onMove(@NonNull Action action, @NonNull PointF location) {
                Home.Companion.getLauncher().getDesktopIndicator().hideDelay();
                for (CellContainer page : _home.getDesktop().getPages()) {
                    page.clearCachedOutlineBitmap();
                }
            }

            @Override
            public void onExit(@NonNull Action action, @NonNull PointF location) {
                for (CellContainer page : _home.getDesktop().getPages()) {
                    page.clearCachedOutlineBitmap();
                }
                dragNDropView.cancelFolderPreview();
            }

            @Override
            public void onEnd() {
                Home.Companion.getLauncher().getDesktopIndicator().hideDelay();
                for (CellContainer page : _home.getDesktop().getPages()) {
                    page.clearCachedOutlineBitmap();
                }
            }
        });

        //dock's drag event

        dragNDropView.registerDropTarget(new DragNDropLayout.DropTargetListener(_home.getDock()) {
            @Override
            public boolean onStart(@NonNull Action action, @NonNull PointF location, boolean isInside) {
                boolean ok = !DragAction.Action.WIDGET.equals(action);
                if (ok && isInside) {
                    //showItemPopup()
                }
                return ok;
            }

            @Override
            public void onStartDrag(@NonNull Action action, @NonNull PointF location) {
                super.onStartDrag(action, location);
            }

            @Override
            public void onDrop(@NonNull Action action, @NonNull PointF location, @NonNull Item item) {
                if (DragAction.Action.APP_DRAWER.equals(action)) {
                    if (_home.getAppDrawerController()._isOpen) {
                        return;
                    }
                    item.reset();
                }

                int x = (int) location.x;
                int y = (int) location.y;
                if (_home.getDock().addItemToPoint(item, x, y)) {
                    _home.getDesktop().consumeRevert();
                    _home.getDock().consumeRevert();

                    // add the item to the database
                    Home._db.saveItem(item, 0, Definitions.ItemPosition.Dock);
                } else {
                    Point pos = new Point();
                    _home.getDock().touchPosToCoordinate(pos, x, y, item._spanX, item._spanY, false);
                    View itemView = _home.getDock().coordinateToChildView(pos);
                    if (itemView != null) {
                        if (Desktop.handleOnDropOver(_home, item, (Item) itemView.getTag(), itemView, _home.getDock(), 0, Definitions.ItemPosition.Dock, _home.getDock())) {
                            _home.getDesktop().consumeRevert();
                            _home.getDock().consumeRevert();
                        } else {
                            Tool.toast(_home, R.string.toast_not_enough_space);
                            _home.getDesktop().revertLastItem();
                            _home.getDock().revertLastItem();
                        }
                    } else {
                        Tool.toast(_home, R.string.toast_not_enough_space);
                        _home.getDesktop().revertLastItem();
                        _home.getDock().revertLastItem();
                    }
                }
            }

            @Override
            public void onMove(@NonNull Action action, @NonNull PointF location) {
                if (action != DragAction.Action.SEARCH_RESULT) {
                    _home.getDock().updateIconProjection((int) location.x, (int) location.y);
                }
            }

            @Override
            public void onEnter(@NonNull Action action, @NonNull PointF location) {
                super.onEnter(action, location);
            }

            @Override
            public void onExit(@NonNull Action action, @NonNull PointF location) {
                _home.getDock().clearCachedOutlineBitmap();
                dragNDropView.cancelFolderPreview();
            }

            @Override
            public void onEnd() {
                if (DragAction.Action.WIDGET.equals(dragNDropView.getDragAction())) {
                    _home.getDesktop().revertLastItem();
                }
                _home.getDock().clearCachedOutlineBitmap();
            }
        });
    }


    void showItemPopup(@NotNull final DragNDropLayout dragNDropView, final Home home) {
        ArrayList<PopupIconLabelItem> itemList = new ArrayList<>();
        switch (dragNDropView.getDragItem().getType()) {
            case APP:
            case SHORTCUT:
            case GROUP: {
                if (DragAction.Action.APP_DRAWER.equals(dragNDropView.getDragAction())) {
                    itemList.add(uninstallItem);
                    itemList.add(infoItem);
                } else {
                    itemList.add(editItem);
                    itemList.add(removeItem);
                    itemList.add(infoItem);
                }
                break;
            }
            case ACTION: {
                itemList.add(editItem);
                itemList.add(removeItem);
                break;
            }
            case WIDGET: {
                itemList.add(removeItem);
                break;
            }
        }

        float x = dragNDropView.getDragLocation().x - Home._itemTouchX + Tool.toPx(10);
        float y = dragNDropView.getDragLocation().y - Home._itemTouchY - Tool.toPx((46 * itemList.size()));

        if ((x + Tool.toPx(200)) > dragNDropView.getWidth()) {
            dragNDropView.setPopupMenuShowDirection(false);
            x = dragNDropView.getDragLocation().x - Home._itemTouchX + home.getDesktop().getCurrentPage().getCellWidth() - Tool.toPx(200) - Tool.toPx(10);
        } else {
            dragNDropView.setPopupMenuShowDirection(true);
        }

        if (y < 0)
            y = dragNDropView.getDragLocation().y - Home._itemTouchY + home.getDesktop().getCurrentPage().getCellHeight() + Tool.toPx(4);
        else
            y -= Tool.toPx(4);

        dragNDropView.showPopupMenuForItem(x, y, itemList, new OnClickListener<PopupIconLabelItem>() {
            @Override
            public boolean onClick(View v, IAdapter<PopupIconLabelItem> adapter, PopupIconLabelItem item, int position) {
                Item dragItem;
                if ((dragItem = dragNDropView.getDragItem()) != null) {
                    switch ((int) item.getIdentifier()) {
                        case uninstallItemIdentifier: {
                            home.onUninstallItem(dragItem);
                            break;
                        }
                        case editItemIdentifier: {
                            new HpAppEditApplier(home).onEditItem(dragItem);
                            break;
                        }
                        case removeItemIdentifier: {
                            home.onRemoveItem(dragItem);
                            break;
                        }
                        case infoItemIdentifier: {
                            home.onInfoItem(dragItem);
                            break;
                        }
                    }
                }
                dragNDropView.hidePopupMenu();
                return true;
            }
        });
    }
}

