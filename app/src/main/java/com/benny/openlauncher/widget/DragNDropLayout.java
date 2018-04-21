package com.benny.openlauncher.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.model.PopupIconLabelItem;
import com.benny.openlauncher.util.DragAction.Action;
import com.benny.openlauncher.util.DragNDropHandler;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;

public final class DragNDropLayout extends FrameLayout {
    private final float DRAG_THRESHOLD;
    @Nullable
    private Action _dragAction;
    private boolean _dragExceedThreshold;
    @Nullable
    private Item _dragItem;
    @NonNull
    private PointF _dragLocation;
    private PointF _dragLocationConverted;
    private PointF _dragLocationStart;
    @Nullable
    private View _dragView;
    private boolean _dragging;
    private float _folderPreviewScale;
    private float _overlayIconScale;
    private final RecyclerView _overlayPopup;
    private final FastItemAdapter<PopupIconLabelItem> _overlayPopupAdapter;
    private boolean _overlayPopupShowing;
    private final OverlayView _overlayView;
    private final Paint _paint;
    private PointF _previewLocation;
    private final HashMap<DropTargetListener, DragFlag> _registeredDropTargetEntries;
    private boolean _showFolderPreview;
    private final SlideInLeftAnimator _slideInLeftAnimator;
    private final SlideInRightAnimator _slideInRightAnimator;
    private final int[] _tempArrayOfInt2;

    public static final class DragFlag {
        private boolean _previousOutside = true;
        private boolean _shouldIgnore;

        public final boolean getPreviousOutside() {
            return _previousOutside;
        }

        public final void setPreviousOutside(boolean v) {
            _previousOutside = v;
        }

        public final boolean getShouldIgnore() {
            return _shouldIgnore;
        }

        public final void setShouldIgnore(boolean v) {
            _shouldIgnore = v;
        }
    }

    public static class DropTargetListener {
        @NonNull
        private final View view;

        public DropTargetListener(@NonNull View view) {

            this.view = view;
        }

        @NonNull
        public final View getView() {
            return this.view;
        }

        public boolean onStart(@NonNull Action action, @NonNull PointF location, boolean isInside) {


            return false;
        }

        public void onStartDrag(@NonNull Action action, @NonNull PointF location) {


        }

        public void onDrop(@NonNull Action action, @NonNull PointF location, @NonNull Item item) {


        }

        public void onMove(@NonNull Action action, @NonNull PointF location) {


        }

        public void onEnter(@NonNull Action action, @NonNull PointF location) {


        }

        public void onExit(@NonNull Action action, @NonNull PointF location) {


        }

        public void onEnd() {
        }
    }

    @SuppressLint({"ResourceType"})
    public final class OverlayView extends View {

        public OverlayView() {
            super(DragNDropLayout.this.getContext());
            setWillNotDraw(false);
        }

        public boolean onTouchEvent(@Nullable MotionEvent event) {
            if (event == null || event.getActionMasked() != 0 || DragNDropLayout.this.getDragging() || !DragNDropLayout.this._overlayPopupShowing) {
                return super.onTouchEvent(event);
            }
            DragNDropLayout.this.hidePopupMenu();
            return true;
        }

        protected void onDraw(@Nullable Canvas canvas) {
            super.onDraw(canvas);
            if (!(canvas == null || DragNDropHandler._cachedDragBitmap == null)) {
                if (!DragNDropLayout.this.getDragLocation().equals(-1.0f, -1.0f)) {
                    float x = DragNDropLayout.this.getDragLocation().x - Home.Companion.getItemTouchX();
                    float y = DragNDropLayout.this.getDragLocation().y - Home.Companion.getItemTouchY();
                    if (DragNDropLayout.this.getDragging()) {
                        canvas.save();
                        DragNDropLayout.this._overlayIconScale = Tool.clampFloat(DragNDropLayout.this._overlayIconScale + 0.05f, 1.0f, 1.1f);
                        float access$getOverlayIconScale$p = DragNDropLayout.this._overlayIconScale;
                        float access$getOverlayIconScale$p2 = DragNDropLayout.this._overlayIconScale;
                        Bitmap bitmap = DragNDropHandler._cachedDragBitmap;
                        if (bitmap == null) {
                            Intrinsics.throwNpe();
                        }
                        float width = ((float) (bitmap.getWidth() / 2)) + x;
                        Bitmap bitmap2 = DragNDropHandler._cachedDragBitmap;
                        if (bitmap2 == null) {
                            Intrinsics.throwNpe();
                        }
                        canvas.scale(access$getOverlayIconScale$p, access$getOverlayIconScale$p2, width, ((float) (bitmap2.getHeight() / 2)) + y);
                        canvas.drawBitmap(DragNDropHandler._cachedDragBitmap, x, y, DragNDropLayout.this._paint);
                        canvas.restore();
                    }
                    if (DragNDropLayout.this.getDragging()) {
                        invalidate();
                    }
                }
            }
        }
    }

    public DragNDropLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.DRAG_THRESHOLD = 20.0f;
        _paint = new Paint(1);
        _registeredDropTargetEntries = new HashMap();
        _tempArrayOfInt2 = new int[2];
        _dragLocation = new PointF();
        _dragLocationStart = new PointF();
        _dragLocationConverted = new PointF();
        _overlayIconScale = 1.0f;
        _overlayPopupAdapter = new FastItemAdapter();
        _previewLocation = new PointF();
        _slideInLeftAnimator = new SlideInLeftAnimator(new AccelerateDecelerateInterpolator());
        _slideInRightAnimator = new SlideInRightAnimator(new AccelerateDecelerateInterpolator());
        _paint.setFilterBitmap(true);
        _paint.setColor(-1);
        _overlayView = new OverlayView();
        _overlayPopup = new RecyclerView(context);
        _overlayPopup.setVisibility(View.INVISIBLE);
        _overlayPopup.setAlpha(0.0f);
        _overlayPopup.setOverScrollMode(2);
        _overlayPopup.setLayoutManager(new LinearLayoutManager(context, 1, false));
        _overlayPopup.setItemAnimator(_slideInLeftAnimator);
        _overlayPopup.setAdapter(_overlayPopupAdapter);
        addView(_overlayView, new LayoutParams(-1, -1));
        addView(_overlayPopup, new LayoutParams(-2, -2));
        setWillNotDraw(false);
    }


    public final boolean getDragging() {
        return _dragging;
    }

    @NonNull
    public final PointF getDragLocation() {
        return _dragLocation;
    }


    @Nullable
    public final Action getDragAction() {
        return _dragAction;
    }


    public final boolean getDragExceedThreshold() {
        return _dragExceedThreshold;
    }

    @Nullable
    public final Item getDragItem() {
        return _dragItem;
    }

    public final void showFolderPreviewAt(@NonNull View fromView, float x, float y) {
        if (!_showFolderPreview) {
            _showFolderPreview = true;
            convertPoint(fromView, this, x, y);
            _folderPreviewScale = 0.0f;
            invalidate();
        }
    }

    public final void convertPoint(@NonNull View fromView, @NonNull View toView, float x, float y) {
        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        fromView.getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);
        _previewLocation.set(((float) (fromCoordinate[0] - toCoordinate[0])) + x, ((float) (fromCoordinate[1] - toCoordinate[1])) + y);
    }

    public final void cancelFolderPreview() {
        _showFolderPreview = false;
        _previewLocation.set(-1.0f, -1.0f);
        invalidate();
    }

    protected void onDraw(@Nullable Canvas canvas) {
        super.onDraw(canvas);
        if (!(canvas == null || !_showFolderPreview || _previewLocation.equals(-1.0f, -1.0f))) {
            _folderPreviewScale += 0.08f;
            _folderPreviewScale = Tool.clampFloat(_folderPreviewScale, 0.5f, 1.0f);
            canvas.drawCircle(_previewLocation.x, _previewLocation.y, ((float) Tool.toPx((Setup.appSettings().getDesktopIconSize() / 2) + 10)) * _folderPreviewScale, _paint);
        }
        if (_showFolderPreview) {
            invalidate();
        }
    }

    @SuppressLint({"ResourceType"})
    public void onViewAdded(@Nullable View child) {
        super.onViewAdded(child);
        _overlayView.bringToFront();
        _overlayPopup.bringToFront();
    }

    public final void showPopupMenuForItem(float x, float y, @NonNull List<PopupIconLabelItem> popupItem, com.mikepenz.fastadapter.listeners.OnClickListener<PopupIconLabelItem> listener) {
        if (!_overlayPopupShowing) {
            _overlayPopupShowing = true;
            _overlayPopup.setVisibility(View.VISIBLE);
            _overlayPopup.setTranslationX(x);
            _overlayPopup.setTranslationY(y);
            _overlayPopup.setAlpha(1.0f);
            _overlayPopupAdapter.add((List) popupItem);
            _overlayPopupAdapter.withOnClickListener(listener);
        }
    }

    public final void setPopupMenuShowDirection(boolean left) {
        if (left) {
            _overlayPopup.setItemAnimator(_slideInLeftAnimator);
        } else {
            _overlayPopup.setItemAnimator(_slideInRightAnimator);
        }
    }

    public final void hidePopupMenu() {
        if (_overlayPopupShowing) {
            _overlayPopupShowing = false;
            _overlayPopup.animate().alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    _overlayPopup.setVisibility(View.INVISIBLE);
                    _overlayPopupAdapter.clear();
                }
            });
            if (!_dragging) {
                _dragView = (View) null;
                _dragItem = (Item) null;
                _dragAction = (Action) null;
            }
        }
    }

    public final void startDragNDropOverlay(@NonNull View view, @NonNull Item item, @NonNull Action action) {


        _dragging = true;
        _dragExceedThreshold = false;
        _overlayIconScale = 0.0f;
        _dragView = view;
        _dragItem = item;
        _dragAction = action;
        _dragLocationStart.set(_dragLocation);
        for (Entry dropTarget : _registeredDropTargetEntries.entrySet()) {
            convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
            DragFlag dragFlag = (DragFlag) dropTarget.getValue();
            DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
            Action action2 = _dragAction;
            if (action2 == null) {
                Intrinsics.throwNpe();
            }
            dragFlag.setShouldIgnore(dropTargetListener.onStart(action2, _dragLocationConverted, isViewContains(((DropTargetListener) dropTarget.getKey()).getView(), (int) _dragLocation.x, (int) _dragLocation.y)) ^ true);
        }
        _overlayView.invalidate();
    }

    protected void onDetachedFromWindow() {
        cancelAllDragNDrop();
        super.onDetachedFromWindow();
    }

    public final void cancelAllDragNDrop() {
        _dragging = false;
        if (!_overlayPopupShowing) {
            _dragView = (View) null;
            _dragItem = (Item) null;
            _dragAction = (Action) null;
        }
        for (Entry dropTarget : _registeredDropTargetEntries.entrySet()) {
            ((DropTargetListener) dropTarget.getKey()).onEnd();
        }
    }

    public final void registerDropTarget(@NonNull DropTargetListener targetListener) {

        Map map = _registeredDropTargetEntries;
        Pair pair = new Pair(targetListener, new DragFlag());
        map.put(pair.getFirst(), pair.getSecond());
    }

    public boolean onInterceptTouchEvent(@Nullable MotionEvent event) {
        if (event != null && event.getActionMasked() == 1 && _dragging) {
            handleDragFinished();
        }
        if (_dragging) {
            return true;
        }
        if (event != null) {
            _dragLocation.set(event.getX(), event.getY());
        }
        return super.onInterceptTouchEvent(event);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(@Nullable MotionEvent event) {
        if (event != null) {
            if (_dragging) {
                _dragLocation.set(event.getX(), event.getY());
                switch (event.getActionMasked()) {
                    case 1:
                        handleDragFinished();
                        break;
                    case 2:
                        handleMovement();
                        break;
                    default:
                        break;
                }
                if (_dragging) {
                    return true;
                }
                return super.onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }

    private final void handleMovement() {
        if (!_dragExceedThreshold && (Math.abs(_dragLocationStart.x - _dragLocation.x) > this.DRAG_THRESHOLD || Math.abs(_dragLocationStart.y - _dragLocation.y) > this.DRAG_THRESHOLD)) {
            _dragExceedThreshold = true;
            for (Entry dropTarget : _registeredDropTargetEntries.entrySet()) {
                if (!((DragFlag) dropTarget.getValue()).getShouldIgnore()) {
                    Action action;
                    convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
                    DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
                    action = _dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onStartDrag(action, _dragLocationConverted);
                }
            }
        }
        if (_dragExceedThreshold) {
            hidePopupMenu();
        }
        for (Entry<DropTargetListener, DragFlag> dropTarget2 : _registeredDropTargetEntries.entrySet()) {
            DropTargetListener dropTargetListener = (DropTargetListener) dropTarget2.getKey();
            Action action = _dragAction;
            if (!((DragFlag) dropTarget2.getValue()).getShouldIgnore()) {
                convertPoint(((DropTargetListener) dropTarget2.getKey()).getView());
                if (isViewContains(((DropTargetListener) dropTarget2.getKey()).getView(), (int) _dragLocation.x, (int) _dragLocation.y)) {

                    dropTargetListener.onMove(action, _dragLocationConverted);
                    if (((DragFlag) dropTarget2.getValue()).getPreviousOutside()) {
                        ((DragFlag) dropTarget2.getValue()).setPreviousOutside(false);
                        dropTargetListener = (DropTargetListener) dropTarget2.getKey();
                        action = _dragAction;
                        dropTargetListener.onEnter(action, _dragLocationConverted);
                    }
                } else if (!((DragFlag) dropTarget2.getValue()).getPreviousOutside()) {
                    ((DragFlag) dropTarget2.getValue()).setPreviousOutside(true);
                    dropTargetListener = (DropTargetListener) dropTarget2.getKey();
                    action = _dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onExit(action, _dragLocationConverted);
                }
            }
        }
    }

    private final void handleDragFinished() {
        _dragging = false;
        for (Entry dropTarget : _registeredDropTargetEntries.entrySet()) {
            if (!((DragFlag) dropTarget.getValue()).getShouldIgnore()) {
                if (isViewContains(((DropTargetListener) dropTarget.getKey()).getView(), (int) _dragLocation.x, (int) _dragLocation.y)) {
                    convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
                    DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
                    Action action = _dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    PointF pointF = _dragLocationConverted;
                    Item item = _dragItem;
                    if (item == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onDrop(action, pointF, item);
                }
            }
        }
        for (Entry dropTarget2 : _registeredDropTargetEntries.entrySet()) {
            ((DropTargetListener) dropTarget2.getKey()).onEnd();
        }
        cancelFolderPreview();
    }

    public final void convertPoint(@NonNull View toView) {

        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);
        _dragLocationConverted.set(((float) (fromCoordinate[0] - toCoordinate[0])) + _dragLocation.x, ((float) (fromCoordinate[1] - toCoordinate[1])) + _dragLocation.y);
    }

    private final boolean isViewContains(View view, int rx, int ry) {
        view.getLocationOnScreen(_tempArrayOfInt2);
        int x = _tempArrayOfInt2[0];
        int y = _tempArrayOfInt2[1];
        int w = view.getWidth();
        int h = view.getHeight();
        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }
}