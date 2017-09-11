package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.benny.openlauncher.core.util.Tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class CellContainer extends ViewGroup {
    public int cellWidth, cellHeight, cellSpanV = 0, cellSpanH = 0;
    public boolean blockTouch = false;
    public SimpleFingerGestures gestures;
    public OnItemRearrangeListener onItemRearrangeListener;
    private boolean[][] occupied;
    private Rect[][] cells;
    private Paint mPaint;
    private Paint bgPaint;
    private Paint outlinePaint;
    private boolean hideGrid = true;
    private Long down = 0L;
    private boolean animateBackground;
    private Point preCoordinate = new Point(-1, -1);
    private Point startCoordinate = null;
    private Long peekDownTime = -1L;
    private PeekDirection peekDirection;
    private Bitmap cachedOutlineBitmap;
    private Point outlineCoordinate;

    public CellContainer(Context c) {
        super(c);
        init();
    }

    public CellContainer(Context c, AttributeSet attr) {
        super(c, attr);
        init();
    }

    public void setGridSize(int x, int y) {
        cellSpanV = y;
        cellSpanH = x;

        occupied = new boolean[cellSpanH][cellSpanV];

        for (int i = 0; i < cellSpanH; i++) {
            for (int j = 0; j < cellSpanV; j++) {
                occupied[i][j] = false;
            }
        }
        requestLayout();
    }

    public void setHideGrid(boolean hideGrid) {
        this.hideGrid = hideGrid;
        invalidate();
    }

    public void resetOccupiedSpace() {
        if (cellSpanH > 0 && cellSpanH > 0)
            occupied = new boolean[cellSpanH][cellSpanV];
    }

    @Override
    public void removeAllViews() {
        resetOccupiedSpace();
        super.removeAllViews();
    }

    private PeekDirection getPeekDirectionFromCoordinate(Point from, Point to) {
        if (from.y - to.y > 0)
            return PeekDirection.UP;
        else if (from.y - to.y < 0)
            return PeekDirection.DOWN;

        if (from.x - to.x > 0)
            return PeekDirection.LEFT;
        else if (from.x - to.x < 0)
            return PeekDirection.RIGHT;

        return null;
    }

    public void projectImageOutlineAt(Point coordinate, Bitmap bitmap) {
        if (cachedOutlineBitmap == null)
            cachedOutlineBitmap = bitmap;
        if (outlineCoordinate == null)
            outlineCoordinate = coordinate;

        invalidate();
    }

    public boolean hasCachedOutlineBitmap() {
        return cachedOutlineBitmap != null;
    }

    private void drawCachedOutlineBitmap(Canvas canvas, Rect cell) {
        canvas.drawBitmap(cachedOutlineBitmap, cell.centerX() - cachedOutlineBitmap.getWidth() / 2, cell.centerY() - cachedOutlineBitmap.getHeight() / 2, outlinePaint);
    }

    public void clearCachedOutlineBitmap() {
        cachedOutlineBitmap = null;
        outlineCoordinate = null;
        invalidate();
    }

    /**
     * Test whether a moved Item is being moved over an existing Item and if so move the
     * existing Item out of the way. <P>
     * <p>
     * TODO: Need to handle the dragged item having a size greater than 1x1
     * <p>
     * TODO: Need to handle moving the target back if the final drop location is not where the Item was moved from
     *
     * @param event - the drag event that contains the current x,y position
     */
    public DragState peekItemAndSwap(DragEvent event, Point coordinate) {
        touchPosToCoordinate(coordinate, (int) event.getX(), (int) event.getY(), 1, 1, false, true);

        if (coordinate.x == -1 || coordinate.y == -1) {
            return DragState.OutOffRange;
        }

        if (startCoordinate == null) {
            startCoordinate = coordinate;
        }
        if (!preCoordinate.equals(coordinate)) {
            peekDownTime = -1L;
        }
        if (peekDownTime == -1L) {
            peekDirection = getPeekDirectionFromCoordinate(startCoordinate, coordinate);
            peekDownTime = System.currentTimeMillis();
            preCoordinate = coordinate;
        }

        if (!occupied[coordinate.x][coordinate.y])
            return DragState.CurrentNotOccupied;
        else
            return DragState.CurrentOccupied;

//        if (!(System.currentTimeMillis() - peekDownTime > 1000L)) {
//            preCoordinate = coordinate;
//            return null;
//        }

//        View targetView = coordinateToChildView(coordinate);
//        if (targetView == null) return DragState.ItemViewNotFound;
//        LayoutParams targetParams = (LayoutParams) targetView.getLayoutParams();
//        if (targetParams.xSpan > 1 || targetParams.ySpan > 1)
//            return null;
//        occupied[targetParams.x][targetParams.y] = false;
//        Point targetPoint = findFreeSpace(targetParams.x, targetParams.y, peekDirection);
//
//        startCoordinate = null;
//        peekDownTime = -1L;
//        preCoordinate.set(-1, -1);
//
//        onItemRearrange(new Point(targetParams.x, targetParams.y), targetPoint);
//        targetParams.x = targetPoint.x;
//        targetParams.y = targetPoint.y;
//        occupied[targetPoint.x][targetPoint.y] = true;
//        requestLayout();

//        return null;
    }

    public void onItemRearrange(Point from, Point to) {
        if (onItemRearrangeListener != null)
            onItemRearrangeListener.onItemRearrange(from, to);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                down = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - down < 260L && blockTouch) {
                    performClick();
                }
                break;
        }
        if (blockTouch) return true;
        if (gestures != null)
            try {
                gestures.onTouch(this, event);
            } catch (Exception ignore) {
            }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (blockTouch) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void init() {
        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(0);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setAlpha(0);

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.WHITE);
        //outlinePaint.setFilterBitmap(true);
        outlinePaint.setAlpha(160);
    }

    public void animateBackgroundShow() {
        animateBackground = true;
        invalidate();
    }

    public void animateBackgroundHide() {
        animateBackground = false;
        invalidate();
    }

    /**
     * Optimised version of findFreeSpace(1,1)
     *
     * @return the first empty space or null if no free space found
     */
    public Point findFreeSpace() {
        for (int y = 0; y < occupied[0].length; y++) {
            for (int x = 0; x < occupied.length; x++) {
                if (!occupied[x][y]) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    /**
     * Locate the first empty space large enough for the supplied dimensions
     *
     * @param spanX - the width of the required space
     * @param spanY - the heigt of the required space
     * @return the first empty space or null if no free space found
     */
    public Point findFreeSpace(int spanX, int spanY) {
        for (int y = 0; y < occupied[0].length; y++) {
            for (int x = 0; x < occupied.length; x++) {
                if (!occupied[x][y] && !checkOccupied(new Point(x, y), spanX, spanY)) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    /**
     * Locate the first 1x1 empty space near but not equal to the supplied starting position.
     * <p>
     * TODO: check this won't return the starting point if the starting point is surrounded by two occupied cells in each direction
     *
     * @param cx            - starting x coordinate
     * @param cy            - starting y coordinate
     * @param peekDirection - direction to look first or null
     * @return the first empty space or null if no free space found
     */
    public Point findFreeSpace(int cx, int cy, PeekDirection peekDirection) {
        if (peekDirection != null) {
            Point target;
            switch (peekDirection) {
                case DOWN:
                    target = new Point(cx, cy - 1);
                    if (isValid(target.x, target.y) && !occupied[target.x][target.y])
                        return target;
                    break;
                case LEFT:
                    target = new Point(cx + 1, cy);
                    if (isValid(target.x, target.y) && !occupied[target.x][target.y])
                        return target;
                    break;
                case RIGHT:
                    target = new Point(cx - 1, cy);
                    if (isValid(target.x, target.y) && !occupied[target.x][target.y])
                        return target;
                    break;
                case UP:
                    target = new Point(cx, cy + 1);
                    if (isValid(target.x, target.y) && !occupied[target.x][target.y])
                        return target;
                    break;
            }
        }
        Queue<Point> toExplore = new LinkedList<>();
        HashSet<Point> explored = new HashSet<>();
        toExplore.add(new Point(cx, cy));
        while (!toExplore.isEmpty()) {
            Point p = toExplore.remove();
            Point cp;
            if (isValid(p.x, p.y - 1)) {
                cp = new Point(p.x, p.y - 1);
                if (!explored.contains(cp)) {
                    if (!occupied[cp.x][cp.y])
                        return cp;
                    else
                        toExplore.add(cp);
                    explored.add(p);
                }
            }

            if (isValid(p.x, p.y + 1)) {
                cp = new Point(p.x, p.y + 1);
                if (!explored.contains(cp)) {
                    if (!occupied[cp.x][cp.y])
                        return cp;
                    else
                        toExplore.add(cp);
                    explored.add(p);
                }
            }

            if (isValid(p.x - 1, p.y)) {
                cp = new Point(p.x - 1, p.y);
                if (!explored.contains(cp)) {
                    if (!occupied[cp.x][cp.y])
                        return cp;
                    else
                        toExplore.add(cp);
                    explored.add(p);
                }
            }

            if (isValid(p.x + 1, p.y)) {
                cp = new Point(p.x + 1, p.y);
                if (!explored.contains(cp)) {
                    if (!occupied[cp.x][cp.y])
                        return cp;
                    else
                        toExplore.add(cp);
                    explored.add(p);
                }
            }
        }
        return null;
    }

    public boolean isValid(int x, int y) {
        return (x >= 0 && x <= occupied.length - 1 && y >= 0 && y <= occupied[0].length - 1);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        float s = 7f;
        for (int x = 0; x < cellSpanH; x++) {
            for (int y = 0; y < cellSpanV; y++) {
                if (x >= cells.length || y >= cells[0].length) continue;

                Rect cell = cells[x][y];

                canvas.save();
                canvas.rotate(45, cell.left, cell.top);
                canvas.drawRect(cell.left - s, cell.top - s, cell.left + s, cell.top + s, mPaint);
                canvas.restore();

                canvas.save();
                canvas.rotate(45, cell.left, cell.bottom);
                canvas.drawRect(cell.left - s, cell.bottom - s, cell.left + s, cell.bottom + s, mPaint);
                canvas.restore();

                canvas.save();
                canvas.rotate(45, cell.right, cell.top);
                canvas.drawRect(cell.right - s, cell.top - s, cell.right + s, cell.top + s, mPaint);
                canvas.restore();

                canvas.save();
                canvas.rotate(45, cell.right, cell.bottom);
                canvas.drawRect(cell.right - s, cell.bottom - s, cell.right + s, cell.bottom + s, mPaint);
                canvas.restore();


            }
        }

        if (cachedOutlineBitmap != null && outlineCoordinate != null && outlineCoordinate.x != -1 && outlineCoordinate.y != -1)
            drawCachedOutlineBitmap(canvas, cells[outlineCoordinate.x][outlineCoordinate.y]);

        if (hideGrid && mPaint.getAlpha() != 0) {
            mPaint.setAlpha(Math.max(mPaint.getAlpha() - 20, 0));
            invalidate();
        } else if ((!hideGrid) && mPaint.getAlpha() != 255) {
            mPaint.setAlpha(Math.min(mPaint.getAlpha() + 20, 255));
            invalidate();
        }

        if (!animateBackground && bgPaint.getAlpha() != 0) {
            bgPaint.setAlpha(Math.max(bgPaint.getAlpha() - 10, 0));
            invalidate();
        } else if (animateBackground && bgPaint.getAlpha() != 100) {
            bgPaint.setAlpha(Math.min(bgPaint.getAlpha() + 10, 100));
            invalidate();
        }
    }

    @Override
    public void addView(View child) {
        LayoutParams lp = (CellContainer.LayoutParams) child.getLayoutParams();
        setOccupied(true, lp);
        super.addView(child);
    }

    @Override
    public void removeView(View view) {
        LayoutParams lp = (CellContainer.LayoutParams) view.getLayoutParams();
        setOccupied(false, lp);
        super.removeView(view);
    }

    public void addViewToGrid(View view, int x, int y, int xSpan, int ySpan) {
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, x, y, xSpan, ySpan);
        view.setLayoutParams(lp);
        addView(view);
    }

    public void addViewToGrid(View view) {
        addView(view);
    }

    public List<View> getAllCells() {
        ArrayList<View> views = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            views.add(getChildAt(i));
        }
        return views;
    }

    public void setOccupied(boolean b, LayoutParams lp) {
        for (int x = lp.x; x < lp.x + lp.xSpan; x++) {
            for (int y = lp.y; y < lp.y + lp.ySpan; y++) {
                occupied[x][y] = b;
            }
        }
    }

    /**
     * Check if there is sufficient unoccupied space to accept an Item with the given span with it's
     * top left corner at the given starting point
     *
     * @param start - The top left corner of the proposed Item location
     * @param spanX - The Item width in cells
     * @param spanY - The Item height in cells
     * @return - true if occupied, false if not
     */
    public boolean checkOccupied(Point start, int spanX, int spanY) {
        if ((start.x + spanX > occupied.length) || (start.y + spanY > occupied[0].length)) {
            return true;
        }
        for (int y = start.y; y < start.y + spanY; y++) {
            for (int x = start.x; x < start.x + spanX; x++) {
                if (occupied[x][y]) {
                    return true;
                }
            }
        }
        return false;
    }

    public View coordinateToChildView(Point pos) {
        if (pos == null) return null;
        for (int i = 0; i < getChildCount(); i++) {
            LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (pos.x >= lp.x && pos.y >= lp.y && pos.x < lp.x + lp.xSpan && pos.y < lp.y + lp.ySpan) {
                return getChildAt(i);
            }
        }
        return null;
    }

    public LayoutParams coordinateToLayoutParams(int mX, int mY, int xSpan, int ySpan) {
        Point pos = new Point();
        touchPosToCoordinate(pos, mX, mY, xSpan, ySpan, true);
        if (!pos.equals(-1, -1)) {
            return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, pos.x, pos.y, xSpan, ySpan);
        }
        return null;
    }

    // convert a touch event to a coordinate in the cell container
    public void touchPosToCoordinate(Point coordinate, int mX, int mY, int xSpan, int ySpan, boolean checkAvailability) {
        touchPosToCoordinate(coordinate, mX, mY, xSpan, ySpan, checkAvailability, false);
    }

    // convert a touch event to a coordinate in the cell container
    public void touchPosToCoordinate(Point coordinate, int mX, int mY, int xSpan, int ySpan, boolean checkAvailability, boolean checkBoundary) {
        mX = mX - (xSpan - 1) * cellWidth / 2;
        mY = mY - (ySpan - 1) * cellHeight / 2;

        for (int x = 0; x < cellSpanH; x++) {
            for (int y = 0; y < cellSpanV; y++) {
                Rect cell = cells[x][y];
                if (mY >= cell.top && mY <= cell.bottom && mX >= cell.left && mX <= cell.right) {
                    if (checkAvailability) {
                        if (occupied[x][y]) {
                            coordinate.set(-1, -1);
                            return;
                        }

                        int dx = x + xSpan - 1;
                        int dy = y + ySpan - 1;

                        if (dx >= cellSpanH - 1) {
                            dx = cellSpanH - 1;
                            x = dx + 1 - xSpan;
                        }
                        if (dy >= cellSpanV - 1) {
                            dy = cellSpanV - 1;
                            y = dy + 1 - ySpan;
                        }

                        for (int x2 = x; x2 < x + xSpan; x2++) {
                            for (int y2 = y; y2 < y + ySpan; y2++) {
                                if (occupied[x2][y2]) {
                                    coordinate.set(-1, -1);
                                    return;
                                }
                            }
                        }
                    }
                    if (checkBoundary) {
                        Rect offsetCell = new Rect(cell);
                        int dp2 = Tool.dp2px(6, getContext());
                        offsetCell.inset(dp2, dp2);
                        if (mY >= offsetCell.top && mY <= offsetCell.bottom && mX >= offsetCell.left && mX <= offsetCell.right) {
                            coordinate.set(-1, -1);
                            return;
                        }
                    }
                    coordinate.set(x, y);
                    return;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l - getPaddingLeft() - getPaddingRight();
        int height = b - t - getPaddingTop() - getPaddingBottom();

        if (cellSpanH == 0)
            cellSpanH = 1;
        if (cellSpanV == 0)
            cellSpanV = 1;

        cellWidth = width / cellSpanH;
        cellHeight = height / cellSpanV;

        initCellInfo(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(), height - getPaddingBottom());

        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            LayoutParams lp = (CellContainer.LayoutParams) child.getLayoutParams();

            int childWidth = lp.xSpan * cellWidth;
            int childHeight = lp.ySpan * cellHeight;
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));

            Rect upRect = cells[lp.x][lp.y];
            Rect downRect = new Rect();
            if (lp.x + lp.xSpan - 1 < cellSpanH && lp.y + lp.ySpan - 1 < cellSpanV)
                downRect = cells[lp.x + lp.xSpan - 1][lp.y + lp.ySpan - 1];

            if (lp.xSpan == 1 && lp.ySpan == 1)
                child.layout(upRect.left, upRect.top, upRect.right, upRect.bottom);
            else if (lp.xSpan > 1 && lp.ySpan > 1)
                child.layout(upRect.left, upRect.top, downRect.right, downRect.bottom);
            else if (lp.xSpan > 1)
                child.layout(upRect.left, upRect.top, downRect.right, upRect.bottom);
            else if (lp.ySpan > 1)
                child.layout(upRect.left, upRect.top, upRect.right, downRect.bottom);
        }
    }

    private void initCellInfo(int l, int t, int r, int b) {
        cells = new Rect[cellSpanH][cellSpanV];

        int curLeft = l;
        int curTop = t;
        int curRight = l + cellWidth;
        int curBottom = t + cellHeight;

        for (int i = 0; i < cellSpanH; i++) {
            if (i != 0) {
                curLeft += cellWidth;
                curRight += cellWidth;
            }
            for (int j = 0; j < cellSpanV; j++) {
                if (j != 0) {
                    curTop += cellHeight;
                    curBottom += cellHeight;
                }

                Rect rect = new Rect(curLeft, curTop, curRight, curBottom);
                cells[i][j] = rect;
            }
            curTop = t;
            curBottom = t + cellHeight;
        }
    }

    public enum DragState {
        CurrentNotOccupied, OutOffRange, ItemViewNotFound, CurrentOccupied
    }

    private enum PeekDirection {
        UP, LEFT, RIGHT, DOWN
    }

    public interface OnItemRearrangeListener {
        void onItemRearrange(Point from, Point to);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int x;
        public int y;
        public int xSpan = 1;
        public int ySpan = 1;

        public LayoutParams(int w, int h, int x, int y) {
            super(w, h);

            this.x = x;
            this.y = y;
        }

        public LayoutParams(int w, int h, int x, int y, int xSpan, int ySpan) {
            super(w, h);

            this.x = x;
            this.y = y;

            this.xSpan = xSpan;
            this.ySpan = ySpan;
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }
    }
}
