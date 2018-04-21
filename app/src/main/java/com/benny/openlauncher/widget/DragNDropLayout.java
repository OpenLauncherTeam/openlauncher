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
            return this._previousOutside;
        }

        public final void setPreviousOutside(boolean v) {
            this._previousOutside = v;
        }

        public final boolean getShouldIgnore() {
            return this._shouldIgnore;
        }

        public final void setShouldIgnore(boolean v) {
            this._shouldIgnore = v;
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
            if (!(canvas == null || DragNDropHandler.cachedDragBitmap == null)) {
                if (!DragNDropLayout.this.getDragLocation().equals(-1.0f, -1.0f)) {
                    float x = DragNDropLayout.this.getDragLocation().x - Home.Companion.getItemTouchX();
                    float y = DragNDropLayout.this.getDragLocation().y - Home.Companion.getItemTouchY();
                    if (DragNDropLayout.this.getDragging()) {
                        canvas.save();
                        DragNDropLayout.this._overlayIconScale = Tool.clampFloat(DragNDropLayout.this._overlayIconScale + 0.05f, 1.0f, 1.1f);
                        float access$getOverlayIconScale$p = DragNDropLayout.this._overlayIconScale;
                        float access$getOverlayIconScale$p2 = DragNDropLayout.this._overlayIconScale;
                        Bitmap bitmap = DragNDropHandler.cachedDragBitmap;
                        if (bitmap == null) {
                            Intrinsics.throwNpe();
                        }
                        float width = ((float) (bitmap.getWidth() / 2)) + x;
                        Bitmap bitmap2 = DragNDropHandler.cachedDragBitmap;
                        if (bitmap2 == null) {
                            Intrinsics.throwNpe();
                        }
                        canvas.scale(access$getOverlayIconScale$p, access$getOverlayIconScale$p2, width, ((float) (bitmap2.getHeight() / 2)) + y);
                        canvas.drawBitmap(DragNDropHandler.cachedDragBitmap, x, y, DragNDropLayout.this._paint);
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
        this._paint = new Paint(1);
        this._registeredDropTargetEntries = new HashMap();
        this._tempArrayOfInt2 = new int[2];
        this._dragLocation = new PointF();
        this._dragLocationStart = new PointF();
        this._dragLocationConverted = new PointF();
        this._overlayIconScale = 1.0f;
        this._overlayPopupAdapter = new FastItemAdapter();
        this._previewLocation = new PointF();
        this._slideInLeftAnimator = new SlideInLeftAnimator(new AccelerateDecelerateInterpolator());
        this._slideInRightAnimator = new SlideInRightAnimator(new AccelerateDecelerateInterpolator());
        this._paint.setFilterBitmap(true);
        this._paint.setColor(-1);
        this._overlayView = new OverlayView();
        this._overlayPopup = new RecyclerView(context);
        this._overlayPopup.setVisibility(View.INVISIBLE);
        this._overlayPopup.setAlpha(0.0f);
        this._overlayPopup.setOverScrollMode(2);
        this._overlayPopup.setLayoutManager(new LinearLayoutManager(context, 1, false));
        this._overlayPopup.setItemAnimator(this._slideInLeftAnimator);
        this._overlayPopup.setAdapter(this._overlayPopupAdapter);
        addView(this._overlayView, new LayoutParams(-1, -1));
        addView(this._overlayPopup, new LayoutParams(-2, -2));
        setWillNotDraw(false);
    }


    public final boolean getDragging() {
        return this._dragging;
    }

    @NonNull
    public final PointF getDragLocation() {
        return this._dragLocation;
    }


    @Nullable
    public final Action getDragAction() {
        return this._dragAction;
    }


    public final boolean getDragExceedThreshold() {
        return this._dragExceedThreshold;
    }

    @Nullable
    public final Item getDragItem() {
        return this._dragItem;
    }

    public final void showFolderPreviewAt(@NonNull View fromView, float x, float y) {
        if (!this._showFolderPreview) {
            this._showFolderPreview = true;
            convertPoint(fromView, this, x, y);
            this._folderPreviewScale = 0.0f;
            invalidate();
        }
    }

    public final void convertPoint(@NonNull View fromView, @NonNull View toView, float x, float y) {
        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        fromView.getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);
        this._previewLocation.set(((float) (fromCoordinate[0] - toCoordinate[0])) + x, ((float) (fromCoordinate[1] - toCoordinate[1])) + y);
    }

    public final void cancelFolderPreview() {
        this._showFolderPreview = false;
        this._previewLocation.set(-1.0f, -1.0f);
        invalidate();
    }

    protected void onDraw(@Nullable Canvas canvas) {
        super.onDraw(canvas);
        if (!(canvas == null || !this._showFolderPreview || this._previewLocation.equals(-1.0f, -1.0f))) {
            this._folderPreviewScale += 0.08f;
            this._folderPreviewScale = Tool.clampFloat(this._folderPreviewScale, 0.5f, 1.0f);
            canvas.drawCircle(this._previewLocation.x, this._previewLocation.y, ((float) Tool.toPx((Setup.appSettings().getDesktopIconSize() / 2) + 10)) * this._folderPreviewScale, this._paint);
        }
        if (this._showFolderPreview) {
            invalidate();
        }
    }

    @SuppressLint({"ResourceType"})
    public void onViewAdded(@Nullable View child) {
        super.onViewAdded(child);
        this._overlayView.bringToFront();
        this._overlayPopup.bringToFront();
    }

    public final void showPopupMenuForItem(float x, float y, @NonNull List<PopupIconLabelItem> popupItem, com.mikepenz.fastadapter.listeners.OnClickListener<PopupIconLabelItem> listener) {
        if (!this._overlayPopupShowing) {
            this._overlayPopupShowing = true;
            this._overlayPopup.setVisibility(View.VISIBLE);
            this._overlayPopup.setTranslationX(x);
            this._overlayPopup.setTranslationY(y);
            this._overlayPopup.setAlpha(1.0f);
            this._overlayPopupAdapter.add((List) popupItem);
            this._overlayPopupAdapter.withOnClickListener(listener);
        }
    }

    public final void setPopupMenuShowDirection(boolean left) {
        if (left) {
            this._overlayPopup.setItemAnimator(this._slideInLeftAnimator);
        } else {
            this._overlayPopup.setItemAnimator(this._slideInRightAnimator);
        }
    }

    public final void hidePopupMenu() {
        if (this._overlayPopupShowing) {
            this._overlayPopupShowing = false;
            this._overlayPopup.animate().alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    _overlayPopup.setVisibility(View.INVISIBLE);
                    _overlayPopupAdapter.clear();
                }
            });
            if (!this._dragging) {
                this._dragView = (View) null;
                this._dragItem = (Item) null;
                this._dragAction = (Action) null;
            }
        }
    }

    public final void startDragNDropOverlay(@NonNull View view, @NonNull Item item, @NonNull Action action) {


        this._dragging = true;
        this._dragExceedThreshold = false;
        this._overlayIconScale = 0.0f;
        this._dragView = view;
        this._dragItem = item;
        this._dragAction = action;
        this._dragLocationStart.set(this._dragLocation);
        for (Entry dropTarget : this._registeredDropTargetEntries.entrySet()) {
            convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
            DragFlag dragFlag = (DragFlag) dropTarget.getValue();
            DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
            Action action2 = this._dragAction;
            if (action2 == null) {
                Intrinsics.throwNpe();
            }
            dragFlag.setShouldIgnore(dropTargetListener.onStart(action2, this._dragLocationConverted, isViewContains(((DropTargetListener) dropTarget.getKey()).getView(), (int) this._dragLocation.x, (int) this._dragLocation.y)) ^ true);
        }
        this._overlayView.invalidate();
    }

    protected void onDetachedFromWindow() {
        cancelAllDragNDrop();
        super.onDetachedFromWindow();
    }

    public final void cancelAllDragNDrop() {
        this._dragging = false;
        if (!this._overlayPopupShowing) {
            this._dragView = (View) null;
            this._dragItem = (Item) null;
            this._dragAction = (Action) null;
        }
        for (Entry dropTarget : this._registeredDropTargetEntries.entrySet()) {
            ((DropTargetListener) dropTarget.getKey()).onEnd();
        }
    }

    public final void registerDropTarget(@NonNull DropTargetListener targetListener) {

        Map map = this._registeredDropTargetEntries;
        Pair pair = new Pair(targetListener, new DragFlag());
        map.put(pair.getFirst(), pair.getSecond());
    }

    public boolean onInterceptTouchEvent(@Nullable MotionEvent event) {
        if (event != null && event.getActionMasked() == 1 && this._dragging) {
            handleDragFinished();
        }
        if (this._dragging) {
            return true;
        }
        if (event != null) {
            this._dragLocation.set(event.getX(), event.getY());
        }
        return super.onInterceptTouchEvent(event);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(@Nullable MotionEvent event) {
        if (event != null) {
            if (this._dragging) {
                this._dragLocation.set(event.getX(), event.getY());
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
                if (this._dragging) {
                    return true;
                }
                return super.onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }

    private final void handleMovement() {
        if (!this._dragExceedThreshold && (Math.abs(this._dragLocationStart.x - this._dragLocation.x) > this.DRAG_THRESHOLD || Math.abs(this._dragLocationStart.y - this._dragLocation.y) > this.DRAG_THRESHOLD)) {
            this._dragExceedThreshold = true;
            for (Entry dropTarget : this._registeredDropTargetEntries.entrySet()) {
                if (!((DragFlag) dropTarget.getValue()).getShouldIgnore()) {
                    Action action;
                    convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
                    DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
                    action = this._dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onStartDrag(action, this._dragLocationConverted);
                }
            }
        }
        if (this._dragExceedThreshold) {
            hidePopupMenu();
        }
        for (Entry<DropTargetListener, DragFlag> dropTarget2 : this._registeredDropTargetEntries.entrySet()) {
            DropTargetListener dropTargetListener = (DropTargetListener) dropTarget2.getKey();
            Action action = this._dragAction;
            if (!((DragFlag) dropTarget2.getValue()).getShouldIgnore()) {
                convertPoint(((DropTargetListener) dropTarget2.getKey()).getView());
                if (isViewContains(((DropTargetListener) dropTarget2.getKey()).getView(), (int) this._dragLocation.x, (int) this._dragLocation.y)) {

                    dropTargetListener.onMove(action, this._dragLocationConverted);
                    if (((DragFlag) dropTarget2.getValue()).getPreviousOutside()) {
                        ((DragFlag) dropTarget2.getValue()).setPreviousOutside(false);
                        dropTargetListener = (DropTargetListener) dropTarget2.getKey();
                        action = this._dragAction;
                        dropTargetListener.onEnter(action, this._dragLocationConverted);
                    }
                } else if (!((DragFlag) dropTarget2.getValue()).getPreviousOutside()) {
                    ((DragFlag) dropTarget2.getValue()).setPreviousOutside(true);
                    dropTargetListener = (DropTargetListener) dropTarget2.getKey();
                    action = this._dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onExit(action, this._dragLocationConverted);
                }
            }
        }
    }

    private final void handleDragFinished() {
        this._dragging = false;
        for (Entry dropTarget : this._registeredDropTargetEntries.entrySet()) {
            if (!((DragFlag) dropTarget.getValue()).getShouldIgnore()) {
                if (isViewContains(((DropTargetListener) dropTarget.getKey()).getView(), (int) this._dragLocation.x, (int) this._dragLocation.y)) {
                    convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
                    DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
                    Action action = this._dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    PointF pointF = this._dragLocationConverted;
                    Item item = this._dragItem;
                    if (item == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onDrop(action, pointF, item);
                }
            }
        }
        for (Entry dropTarget2 : this._registeredDropTargetEntries.entrySet()) {
            ((DropTargetListener) dropTarget2.getKey()).onEnd();
        }
        cancelFolderPreview();
    }

    public final void convertPoint(@NonNull View toView) {

        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);
        this._dragLocationConverted.set(((float) (fromCoordinate[0] - toCoordinate[0])) + this._dragLocation.x, ((float) (fromCoordinate[1] - toCoordinate[1])) + this._dragLocation.y);
    }

    private final boolean isViewContains(View view, int rx, int ry) {
        view.getLocationOnScreen(this._tempArrayOfInt2);
        int x = this._tempArrayOfInt2[0];
        int y = this._tempArrayOfInt2[1];
        int w = view.getWidth();
        int h = view.getHeight();
        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }
}