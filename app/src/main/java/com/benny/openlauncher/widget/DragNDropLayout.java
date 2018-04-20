package com.benny.openlauncher.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;
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

import org.jetbrains.annotations.NotNull;

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
    private HashMap _$_findViewCache;
    @Nullable
    private Action dragAction;
    private boolean dragExceedThreshold;
    @Nullable
    private Item dragItem;
    @NotNull
    private PointF dragLocation;
    private PointF dragLocationConverted;
    private PointF dragLocationStart;
    @Nullable
    private View dragView;
    private boolean dragging;
    private float folderPreviewScale;
    private float overlayIconScale;
    private final RecyclerView overlayPopup;
    private final FastItemAdapter<PopupIconLabelItem> overlayPopupAdapter;
    private boolean overlayPopupShowing;
    private final OverlayView overlayView;
    private final Paint paint;
    private PointF previewLocation;
    private final HashMap<DropTargetListener, DragFlag> registeredDropTargetEntries;
    private boolean showFolderPreview;
    private final SlideInLeftAnimator slideInLeftAnimator;
    private final SlideInRightAnimator slideInRightAnimator;
    private final int[] tempArrayOfInt2;

    /* compiled from: DragNDropLayout.kt */
    public static final class DragFlag {
        private boolean previousOutside = true;
        private boolean shouldIgnore;

        public final boolean getPreviousOutside() {
            return this.previousOutside;
        }

        public final void setPreviousOutside(boolean v) {
            this.previousOutside = v;
        }

        public final boolean getShouldIgnore() {
            return this.shouldIgnore;
        }

        public final void setShouldIgnore(boolean v) {
            this.shouldIgnore = v;
        }
    }

    public static class DropTargetListener {
        @NotNull
        private final View view;

        public DropTargetListener(@NotNull View view) {
            Intrinsics.checkParameterIsNotNull(view, "view");
            this.view = view;
        }

        @NotNull
        public final View getView() {
            return this.view;
        }

        public boolean onStart(@NotNull Action action, @NotNull PointF location, boolean isInside) {
            Intrinsics.checkParameterIsNotNull(action, "action");
            Intrinsics.checkParameterIsNotNull(location, "location");
            return false;
        }

        public void onStartDrag(@NotNull Action action, @NotNull PointF location) {
            Intrinsics.checkParameterIsNotNull(action, "action");
            Intrinsics.checkParameterIsNotNull(location, "location");
        }

        public void onDrop(@NotNull Action action, @NotNull PointF location, @NotNull Item item) {
            Intrinsics.checkParameterIsNotNull(action, "action");
            Intrinsics.checkParameterIsNotNull(location, "location");
            Intrinsics.checkParameterIsNotNull(item, "item");
        }

        public void onMove(@NotNull Action action, @NotNull PointF location) {
            Intrinsics.checkParameterIsNotNull(action, "action");
            Intrinsics.checkParameterIsNotNull(location, "location");
        }

        public void onEnter(@NotNull Action action, @NotNull PointF location) {
            Intrinsics.checkParameterIsNotNull(action, "action");
            Intrinsics.checkParameterIsNotNull(location, "location");
        }

        public void onExit(@NotNull Action action, @NotNull PointF location) {
            Intrinsics.checkParameterIsNotNull(action, "action");
            Intrinsics.checkParameterIsNotNull(location, "location");
        }

        public void onEnd() {
        }
    }

    @SuppressLint({"ResourceType"})
    public final class OverlayView extends View {
        private HashMap _$_findViewCache;

        public void _$_clearFindViewByIdCache() {
            if (this._$_findViewCache != null) {
                this._$_findViewCache.clear();
            }
        }

        public View _$_findCachedViewById(int i) {
            if (this._$_findViewCache == null) {
                this._$_findViewCache = new HashMap();
            }
            View view = (View) this._$_findViewCache.get(Integer.valueOf(i));
            if (view != null) {
                return view;
            }
            view = findViewById(i);
            this._$_findViewCache.put(Integer.valueOf(i), view);
            return view;
        }

        public OverlayView() {
            super(DragNDropLayout.this.getContext());
            setWillNotDraw(false);
        }

        public boolean onTouchEvent(@Nullable MotionEvent event) {
            if (event == null || event.getActionMasked() != 0 || DragNDropLayout.this.getDragging() || !DragNDropLayout.this.overlayPopupShowing) {
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
                        DragNDropLayout.this.overlayIconScale = Tool.clampFloat(DragNDropLayout.this.overlayIconScale + 0.05f, 1.0f, 1.1f);
                        float access$getOverlayIconScale$p = DragNDropLayout.this.overlayIconScale;
                        float access$getOverlayIconScale$p2 = DragNDropLayout.this.overlayIconScale;
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
                        canvas.drawBitmap(DragNDropHandler.cachedDragBitmap, x, y, DragNDropLayout.this.paint);
                        canvas.restore();
                    }
                    if (DragNDropLayout.this.getDragging()) {
                        invalidate();
                    }
                }
            }
        }
    }

    public void _$_clearFindViewByIdCache() {
        if (this._$_findViewCache != null) {
            this._$_findViewCache.clear();
        }
    }

    public View _$_findCachedViewById(int i) {
        if (this._$_findViewCache == null) {
            this._$_findViewCache = new HashMap();
        }
        View view = (View) this._$_findViewCache.get(Integer.valueOf(i));
        if (view != null) {
            return view;
        }
        view = findViewById(i);
        this._$_findViewCache.put(Integer.valueOf(i), view);
        return view;
    }

    public DragNDropLayout(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.DRAG_THRESHOLD = 20.0f;
        this.paint = new Paint(1);
        this.registeredDropTargetEntries = new HashMap();
        this.tempArrayOfInt2 = new int[2];
        this.dragLocation = new PointF();
        this.dragLocationStart = new PointF();
        this.dragLocationConverted = new PointF();
        this.overlayIconScale = 1.0f;
        this.overlayPopupAdapter = new FastItemAdapter();
        this.previewLocation = new PointF();
        this.slideInLeftAnimator = new SlideInLeftAnimator(new AccelerateDecelerateInterpolator());
        this.slideInRightAnimator = new SlideInRightAnimator(new AccelerateDecelerateInterpolator());
        this.paint.setFilterBitmap(true);
        this.paint.setColor(-1);
        this.overlayView = new OverlayView();
        this.overlayPopup = new RecyclerView(context);
        this.overlayPopup.setVisibility(View.INVISIBLE);
        this.overlayPopup.setAlpha(0.0f);
        this.overlayPopup.setOverScrollMode(2);
        this.overlayPopup.setLayoutManager(new LinearLayoutManager(context, 1, false));
        this.overlayPopup.setItemAnimator(this.slideInLeftAnimator);
        this.overlayPopup.setAdapter(this.overlayPopupAdapter);
        addView(this.overlayView, new LayoutParams(-1, -1));
        addView(this.overlayPopup, new LayoutParams(-2, -2));
        setWillNotDraw(false);
    }

    private final void setDragging(boolean v) {
        this.dragging = v;
    }

    public final boolean getDragging() {
        return this.dragging;
    }

    private final void setDragLocation(PointF v) {
        this.dragLocation = v;
    }

    @NotNull
    public final PointF getDragLocation() {
        return this.dragLocation;
    }

    private final void setDragAction(Action v) {
        this.dragAction = v;
    }

    @Nullable
    public final Action getDragAction() {
        return this.dragAction;
    }

    private final void setDragExceedThreshold(boolean v) {
        this.dragExceedThreshold = v;
    }

    public final boolean getDragExceedThreshold() {
        return this.dragExceedThreshold;
    }

    private final void setDragView(View v) {
        this.dragView = v;
    }

    @Nullable
    public final View getDragView() {
        return this.dragView;
    }

    private final void setDragItem(Item v) {
        this.dragItem = v;
    }

    @Nullable
    public final Item getDragItem() {
        return this.dragItem;
    }

    public final void showFolderPreviewAt(@NotNull View fromView, float x, float y) {
        if (!this.showFolderPreview) {
            this.showFolderPreview = true;
            convertPoint(fromView, this, x, y);
            this.folderPreviewScale = 0.0f;
            invalidate();
        }
    }

    public final void convertPoint(@NotNull View fromView, @NotNull View toView, float x, float y) {
        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        fromView.getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);
        this.previewLocation.set(((float) (fromCoordinate[0] - toCoordinate[0])) + x, ((float) (fromCoordinate[1] - toCoordinate[1])) + y);
    }

    public final void cancelFolderPreview() {
        this.showFolderPreview = false;
        this.previewLocation.set(-1.0f, -1.0f);
        invalidate();
    }

    protected void onDraw(@Nullable Canvas canvas) {
        super.onDraw(canvas);
        if (!(canvas == null || !this.showFolderPreview || this.previewLocation.equals(-1.0f, -1.0f))) {
            this.folderPreviewScale += 0.08f;
            this.folderPreviewScale = Tool.clampFloat(this.folderPreviewScale, 0.5f, 1.0f);
            canvas.drawCircle(this.previewLocation.x, this.previewLocation.y, ((float) Tool.toPx((Setup.appSettings().getDesktopIconSize() / 2) + 10)) * this.folderPreviewScale, this.paint);
        }
        if (this.showFolderPreview) {
            invalidate();
        }
    }

    @SuppressLint({"ResourceType"})
    public void onViewAdded(@Nullable View child) {
        super.onViewAdded(child);
        this.overlayView.bringToFront();
        this.overlayPopup.bringToFront();
    }

    public final void showPopupMenuForItem(float x, float y, @NotNull List<PopupIconLabelItem> popupItem, com.mikepenz.fastadapter.listeners.OnClickListener<PopupIconLabelItem> listener) {
        if (!this.overlayPopupShowing) {
            this.overlayPopupShowing = true;
            this.overlayPopup.setVisibility(View.VISIBLE);
            this.overlayPopup.setTranslationX(x);
            this.overlayPopup.setTranslationY(y);
            this.overlayPopup.setAlpha(1.0f);
            this.overlayPopupAdapter.add((List) popupItem);
            this.overlayPopupAdapter.withOnClickListener(listener);
        }
    }

    public final void setPopupMenuShowDirection(boolean left) {
        if (left) {
            this.overlayPopup.setItemAnimator(this.slideInLeftAnimator);
        } else {
            this.overlayPopup.setItemAnimator(this.slideInRightAnimator);
        }
    }

    public final void hidePopupMenu() {
        if (this.overlayPopupShowing) {
            this.overlayPopupShowing = false;
            this.overlayPopup.animate().alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    overlayPopup.setVisibility(View.INVISIBLE);
                    overlayPopupAdapter.clear();
                }
            });
            if (!this.dragging) {
                this.dragView = (View) null;
                this.dragItem = (Item) null;
                this.dragAction = (Action) null;
            }
        }
    }

    public final void startDragNDropOverlay(@NotNull View view, @NotNull Item item, @NotNull Action action) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(item, "item");
        Intrinsics.checkParameterIsNotNull(action, "action");
        this.dragging = true;
        this.dragExceedThreshold = false;
        this.overlayIconScale = 0.0f;
        this.dragView = view;
        this.dragItem = item;
        this.dragAction = action;
        this.dragLocationStart.set(this.dragLocation);
        for (Entry dropTarget : this.registeredDropTargetEntries.entrySet()) {
            convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
            DragFlag dragFlag = (DragFlag) dropTarget.getValue();
            DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
            Action action2 = this.dragAction;
            if (action2 == null) {
                Intrinsics.throwNpe();
            }
            dragFlag.setShouldIgnore(dropTargetListener.onStart(action2, this.dragLocationConverted, isViewContains(((DropTargetListener) dropTarget.getKey()).getView(), (int) this.dragLocation.x, (int) this.dragLocation.y)) ^ true);
        }
        this.overlayView.invalidate();
    }

    protected void onDetachedFromWindow() {
        cancelAllDragNDrop();
        super.onDetachedFromWindow();
    }

    public final void cancelAllDragNDrop() {
        this.dragging = false;
        if (!this.overlayPopupShowing) {
            this.dragView = (View) null;
            this.dragItem = (Item) null;
            this.dragAction = (Action) null;
        }
        for (Entry dropTarget : this.registeredDropTargetEntries.entrySet()) {
            ((DropTargetListener) dropTarget.getKey()).onEnd();
        }
    }

    public final void registerDropTarget(@NotNull DropTargetListener targetListener) {
        Intrinsics.checkParameterIsNotNull(targetListener, "targetListener");
        Map map = this.registeredDropTargetEntries;
        Pair pair = new Pair(targetListener, new DragFlag());
        map.put(pair.getFirst(), pair.getSecond());
    }

    public final void unregisterDropTarget(@NotNull DropTargetListener targetListener) {
        Intrinsics.checkParameterIsNotNull(targetListener, "targetListener");
        this.registeredDropTargetEntries.remove(targetListener);
    }

    public boolean onInterceptTouchEvent(@Nullable MotionEvent event) {
        if (event != null && event.getActionMasked() == 1 && this.dragging) {
            handleDragFinished();
        }
        if (this.dragging) {
            return true;
        }
        if (event != null) {
            this.dragLocation.set(event.getX(), event.getY());
        }
        return super.onInterceptTouchEvent(event);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(@Nullable MotionEvent event) {
        if (event != null) {
            if (this.dragging) {
                this.dragLocation.set(event.getX(), event.getY());
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
                if (this.dragging) {
                    return true;
                }
                return super.onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }

    private final void handleMovement() {
        if (!this.dragExceedThreshold && (Math.abs(this.dragLocationStart.x - this.dragLocation.x) > this.DRAG_THRESHOLD || Math.abs(this.dragLocationStart.y - this.dragLocation.y) > this.DRAG_THRESHOLD)) {
            this.dragExceedThreshold = true;
            for (Entry dropTarget : this.registeredDropTargetEntries.entrySet()) {
                if (!((DragFlag) dropTarget.getValue()).getShouldIgnore()) {
                    Action action;
                    convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
                    DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
                    action = this.dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onStartDrag(action, this.dragLocationConverted);
                }
            }
        }
        if (this.dragExceedThreshold) {
            hidePopupMenu();
        }
        for (Entry<DropTargetListener, DragFlag> dropTarget2 : this.registeredDropTargetEntries.entrySet()) {
            DropTargetListener dropTargetListener = (DropTargetListener) dropTarget2.getKey();
            Action action = this.dragAction;
            if (!((DragFlag) dropTarget2.getValue()).getShouldIgnore()) {
                convertPoint(((DropTargetListener) dropTarget2.getKey()).getView());
                if (isViewContains(((DropTargetListener) dropTarget2.getKey()).getView(), (int) this.dragLocation.x, (int) this.dragLocation.y)) {

                    dropTargetListener.onMove(action, this.dragLocationConverted);
                    if (((DragFlag) dropTarget2.getValue()).getPreviousOutside()) {
                        ((DragFlag) dropTarget2.getValue()).setPreviousOutside(false);
                        dropTargetListener = (DropTargetListener) dropTarget2.getKey();
                        action = this.dragAction;
                        dropTargetListener.onEnter(action, this.dragLocationConverted);
                    }
                } else if (!((DragFlag) dropTarget2.getValue()).getPreviousOutside()) {
                    ((DragFlag) dropTarget2.getValue()).setPreviousOutside(true);
                    dropTargetListener = (DropTargetListener) dropTarget2.getKey();
                    action = this.dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onExit(action, this.dragLocationConverted);
                }
            }
        }
    }

    private final void handleDragFinished() {
        this.dragging = false;
        for (Entry dropTarget : this.registeredDropTargetEntries.entrySet()) {
            if (!((DragFlag) dropTarget.getValue()).getShouldIgnore()) {
                if (isViewContains(((DropTargetListener) dropTarget.getKey()).getView(), (int) this.dragLocation.x, (int) this.dragLocation.y)) {
                    convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
                    DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
                    Action action = this.dragAction;
                    if (action == null) {
                        Intrinsics.throwNpe();
                    }
                    PointF pointF = this.dragLocationConverted;
                    Item item = this.dragItem;
                    if (item == null) {
                        Intrinsics.throwNpe();
                    }
                    dropTargetListener.onDrop(action, pointF, item);
                }
            }
        }
        for (Entry dropTarget2 : this.registeredDropTargetEntries.entrySet()) {
            ((DropTargetListener) dropTarget2.getKey()).onEnd();
        }
        cancelFolderPreview();
    }

    public final void convertPoint(@NotNull View toView) {
        Intrinsics.checkParameterIsNotNull(toView, "toView");
        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);
        this.dragLocationConverted.set(((float) (fromCoordinate[0] - toCoordinate[0])) + this.dragLocation.x, ((float) (fromCoordinate[1] - toCoordinate[1])) + this.dragLocation.y);
    }

    private final boolean isViewContains(View view, int rx, int ry) {
        view.getLocationOnScreen(this.tempArrayOfInt2);
        int x = this.tempArrayOfInt2[0];
        int y = this.tempArrayOfInt2[1];
        int w = view.getWidth();
        int h = view.getHeight();
        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }
}