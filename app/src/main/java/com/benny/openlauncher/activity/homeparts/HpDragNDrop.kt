package com.benny.openlauncher.activity.homeparts

import android.graphics.Point
import android.graphics.PointF
import android.os.Handler
import android.view.View
import com.benny.openlauncher.R
import com.benny.openlauncher.activity.Home
import com.benny.openlauncher.model.Item
import com.benny.openlauncher.model.PopupIconLabelItem
import com.benny.openlauncher.util.Definitions
import com.benny.openlauncher.util.DragAction
import com.benny.openlauncher.util.Tool
import com.benny.openlauncher.widget.Desktop
import com.benny.openlauncher.widget.DragNDropLayout
import com.mikepenz.fastadapter.listeners.OnClickListener

class HpDragNDrop {

    public fun initDragNDrop(_home: Home, leftDragHandle: View, rightDragHandle: View, dragNDropView: DragNDropLayout) {
        //dragHandle's drag event
        val dragHandler = Handler()

        dragNDropView.registerDropTarget(object : DragNDropLayout.DropTargetListener(leftDragHandle) {

            val leftRunnable = object : Runnable {
                override fun run() {
                    if (_home.getDesktop().currentItem > 0)
                        _home.getDesktop().currentItem = _home.getDesktop().currentItem - 1
                    else if (_home.getDesktop().currentItem == 0)
                        _home.getDesktop().addPageLeft(true)
                    dragHandler.postDelayed(this, 1000)
                }
            }

            override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean =
                    when (action) {
                        DragAction.Action.APP,
                        DragAction.Action.WIDGET,
                        DragAction.Action.SEARCH_RESULT,
                        DragAction.Action.APP_DRAWER,
                        DragAction.Action.GROUP,
                        DragAction.Action.SHORTCUT,
                        DragAction.Action.ACTION -> {
                            true
                        }
                    }

            override fun onStartDrag(action: DragAction.Action, location: PointF) {
                if (leftDragHandle.alpha == 0f)
                    leftDragHandle.animate().alpha(0.5f)
            }

            override fun onEnter(action: DragAction.Action, location: PointF) {
                dragHandler.post(leftRunnable)
                leftDragHandle.animate().alpha(0.9f)
            }

            override fun onExit(action: DragAction.Action, location: PointF) {
                dragHandler.removeCallbacksAndMessages(null)
                leftDragHandle.animate().alpha(0.5f)
            }

            override fun onEnd() {
                dragHandler.removeCallbacksAndMessages(null)
                leftDragHandle.animate().alpha(0f)
            }
        })
        dragNDropView.registerDropTarget(object : DragNDropLayout.DropTargetListener(rightDragHandle) {

            val rightRunnable = object : Runnable {
                override fun run() {
                    if (_home.getDesktop().currentItem < _home.getDesktop().pageCount - 1)
                        _home.getDesktop().currentItem = _home.getDesktop().currentItem + 1
                    else if (_home.getDesktop().currentItem == _home.getDesktop().pageCount - 1)
                        _home.getDesktop().addPageRight(true)
                    dragHandler.postDelayed(this, 1000)
                }
            }

            override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean =
                    when (action) {
                        DragAction.Action.APP,
                        DragAction.Action.WIDGET,
                        DragAction.Action.SEARCH_RESULT,
                        DragAction.Action.APP_DRAWER,
                        DragAction.Action.GROUP,
                        DragAction.Action.SHORTCUT,
                        DragAction.Action.ACTION -> {
                            true
                        }
                    }

            override fun onStartDrag(action: DragAction.Action, location: PointF) {
                if (rightDragHandle.alpha == 0f)
                    rightDragHandle.animate().alpha(0.5f)
            }

            override fun onEnter(action: DragAction.Action, location: PointF) {
                dragHandler.post(rightRunnable)
                rightDragHandle.animate().alpha(0.9f)
            }

            override fun onExit(action: DragAction.Action, location: PointF) {
                dragHandler.removeCallbacksAndMessages(null)
                rightDragHandle.animate().alpha(0.5f)
            }

            override fun onEnd() {
                dragHandler.removeCallbacksAndMessages(null)
                rightDragHandle.animate().alpha(0f)
            }
        })

        val uninstallItemIdentifier = 83L
        val infoItemIdentifier = 84L
        val editItemIdentifier = 85L
        val removeItemIdentifier = 86L

        val uninstallItem = PopupIconLabelItem(R.string.uninstall, R.drawable.ic_delete_dark_24dp).withIdentifier(uninstallItemIdentifier)
        val infoItem = PopupIconLabelItem(R.string.info, R.drawable.ic_info_outline_dark_24dp).withIdentifier(infoItemIdentifier)
        val editItem = PopupIconLabelItem(R.string.edit, R.drawable.ic_edit_black_24dp).withIdentifier(editItemIdentifier)
        val removeItem = PopupIconLabelItem(R.string.remove, R.drawable.ic_close_dark_24dp).withIdentifier(removeItemIdentifier)

        fun showItemPopup() {
            val itemList = arrayListOf<PopupIconLabelItem>()
            when (dragNDropView.dragItem?._type) {
                Item.Type.APP, Item.Type.SHORTCUT, Item.Type.GROUP -> {
                    if (dragNDropView.dragAction == DragAction.Action.APP_DRAWER) {
                        itemList.add(uninstallItem)
                        itemList.add(infoItem)
                    } else {
                        itemList.add(editItem)
                        itemList.add(removeItem)
                        itemList.add(infoItem)
                    }
                }
                Item.Type.ACTION -> {
                    itemList.add(editItem)
                    itemList.add(removeItem)
                }
                Item.Type.WIDGET -> {
                    itemList.add(removeItem)
                }
            }

            var x = dragNDropView.dragLocation.x - Home.itemTouchX + Tool.toPx(10)
            var y = dragNDropView.dragLocation.y - Home.itemTouchY - Tool.toPx((46 * itemList.size))

            if ((x + Tool.toPx(200)) > dragNDropView.width) {
                dragNDropView.setPopupMenuShowDirection(false)
                x = dragNDropView.dragLocation.x - Home.itemTouchX + _home.getDesktop().currentPage.cellWidth - Tool.toPx(200).toFloat() - Tool.toPx(10)
            } else {
                dragNDropView.setPopupMenuShowDirection(true)
            }

            if (y < 0)
                y = dragNDropView.dragLocation.y - Home.itemTouchY + _home.getDesktop().currentPage.cellHeight + Tool.toPx(4)
            else
                y -= Tool.toPx(4)

            dragNDropView.showPopupMenuForItem(x, y, itemList, OnClickListener { v, adapter, item, position ->
                when (item.identifier) {
                    uninstallItemIdentifier -> _home.onUninstallItem(dragNDropView.dragItem!!)
                    editItemIdentifier -> HpAppEditApplier(_home).onEditItem(dragNDropView.dragItem!!)
                    removeItemIdentifier -> _home.onRemoveItem(dragNDropView.dragItem!!)
                    infoItemIdentifier -> _home.onInfoItem(dragNDropView.dragItem!!)
                }
                dragNDropView.hidePopupMenu()
                true
            })
        }

        //desktop's drag event
        dragNDropView.registerDropTarget(object : DragNDropLayout.DropTargetListener(_home.getDesktop()) {
            override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
                if (action != DragAction.Action.SEARCH_RESULT)
                    showItemPopup()
                return true
            }

            override fun onExit(action: DragAction.Action, location: PointF) {
                for (page in _home.getDesktop().pages)
                    page.clearCachedOutlineBitmap()
                dragNDropView.cancelFolderPreview()
            }

            override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
                // this statement makes sure that adding an app multiple times from the app drawer works
                // the app will get a new id every time
                if (action == DragAction.Action.APP_DRAWER) {
                    if (_home.getAppDrawerController()._isOpen) return
                    item.reset()
                }

                val x = location.x.toInt()
                val y = location.y.toInt()
                if (_home.getDesktop().addItemToPoint(item, x, y)) {
                    _home.getDesktop().consumeRevert()
                    _home.getDock().consumeRevert()
                    // add the item to the database
                    Home.db.saveItem(item, _home.getDesktop().currentItem, Definitions.ItemPosition.Desktop)
                } else {
                    val pos = Point()
                    _home.getDesktop().currentPage.touchPosToCoordinate(pos, x, y, item._spanX, item._spanY, false)
                    val itemView = _home.getDesktop().currentPage.coordinateToChildView(pos)
                    if (itemView != null && Desktop.handleOnDropOver(_home, item, itemView.tag as Item, itemView, _home.getDesktop().currentPage, _home.getDesktop().currentItem, Definitions.ItemPosition.Desktop, _home.getDesktop())) {
                        _home.getDesktop().consumeRevert()
                        _home.getDock().consumeRevert()
                    } else {
                        Tool.toast(_home, R.string.toast_not_enough_space)
                        _home.getDesktop().revertLastItem()
                        _home.getDock().revertLastItem()
                    }
                }
            }

            override fun onStartDrag(action: DragAction.Action, location: PointF) {
                _home.closeAppDrawer()
            }

            override fun onEnd() {
                Home.launcher?.getDesktopIndicator()?.hideDelay()
                for (page in _home.getDesktop().pages)
                    page.clearCachedOutlineBitmap()
            }

            override fun onMove(action: DragAction.Action, location: PointF) {
                if (action != DragAction.Action.SEARCH_RESULT && action != DragAction.Action.WIDGET)
                    _home.getDesktop().updateIconProjection(location.x.toInt(), location.y.toInt())
            }
        })

        //dock's drag event
        dragNDropView.registerDropTarget(object : DragNDropLayout.DropTargetListener(_home.getDock()) {
            override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
                val ok = (action != DragAction.Action.WIDGET)

                if (ok && isInside) {
                    //showItemPopup()
                }

                return ok
            }

            override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
                if (action == DragAction.Action.APP_DRAWER) {
                    if (_home.getAppDrawerController()._isOpen) return
                    item.reset()
                }

                val x = location.x.toInt()
                val y = location.y.toInt()
                if (_home.getDock().addItemToPoint(item, x, y)) {
                    _home.getDesktop().consumeRevert()
                    _home.getDock().consumeRevert()

                    // add the item to the database
                    Home.db.saveItem(item, 0, Definitions.ItemPosition.Dock)
                } else {
                    val pos = Point()
                    _home.getDock().touchPosToCoordinate(pos, x, y, item._spanX, item._spanY, false)
                    val itemView = _home.getDock().coordinateToChildView(pos)
                    if (itemView != null) {
                        if (Desktop.handleOnDropOver(_home, item, itemView.tag as Item, itemView, _home.getDock(), 0, Definitions.ItemPosition.Dock, _home.getDock())) {
                            _home.getDesktop().consumeRevert()
                            _home.getDock().consumeRevert()
                        } else {
                            Tool.toast(_home, R.string.toast_not_enough_space)
                            _home.getDesktop().revertLastItem()
                            _home.getDock().revertLastItem()
                        }
                    } else {
                        Tool.toast(_home, R.string.toast_not_enough_space)
                        _home.getDesktop().revertLastItem()
                        _home.getDock().revertLastItem()
                    }
                }
            }

            override fun onExit(action: DragAction.Action, location: PointF) {
                _home.getDock().clearCachedOutlineBitmap()
                dragNDropView.cancelFolderPreview()
            }

            override fun onEnd() {
                if (dragNDropView.dragAction == DragAction.Action.WIDGET)
                    _home.getDesktop().revertLastItem()
                _home.getDock().clearCachedOutlineBitmap()
            }

            override fun onMove(action: DragAction.Action, location: PointF) {
                if (action != DragAction.Action.SEARCH_RESULT)
                    _home.getDock().updateIconProjection(location.x.toInt(), location.y.toInt())
            }
        })
    }
}
