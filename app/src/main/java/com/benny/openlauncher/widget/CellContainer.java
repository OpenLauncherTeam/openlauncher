package com.benny.openlauncher.widget;

import android.content.Context;
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

import com.benny.openlauncher.util.Tool;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class CellContainer extends ViewGroup {
    private boolean[][] occupied;

    private Rect[][] cells;

    public int cellWidth, cellHeight, cellSpanV = 0, cellSpanH = 0;

    private Paint mPaint;

    private Paint bgPaint;

    private boolean hideGrid = true;

    public boolean blockTouch = false;

    private Long down = 0L;

    public SimpleFingerGestures gestures;

    private boolean animateBackground;

    public OnItemRearrangeListener onItemRearrangeListener;

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

    private Point preCoordinate = new Point(-1, -1);
    private Long peekDownTime = -1L;
    private PeekDirection peekDirection;

    private PeekDirection getPeekDirectionFromCoordinate(Point from, Point to) {
        if (from.y - to.y > 0)
            return PeekDirection.T;
        else if (from.y - to.y < 0)
            return PeekDirection.B;

        if (from.x - to.x > 0)
            return PeekDirection.L;
        else if (from.x - to.x < 0)
            return PeekDirection.R;

        return null;
    }

    public void peekItemAndSwap(DragEvent event) {
        Point coordinate = touchPosToCoordinate((int) event.getX(), (int) event.getY(), 1, 1, false);

        if (coordinate == null) return;
        if (!preCoordinate.equals(coordinate)) {
            peekDirection = getPeekDirectionFromCoordinate(preCoordinate, coordinate);
            peekDownTime = -1L;
        }
        if (peekDownTime == -1L) {
            peekDownTime = System.currentTimeMillis();
            preCoordinate = coordinate;
        }
        if (!(System.currentTimeMillis() - peekDownTime > 600L)) {
            preCoordinate = coordinate;
            return;
        }

        if (!occupied[coordinate.x][coordinate.y]) return;
        View targetView = coordinateToChildView(coordinate);
        if (targetView == null) return;
        LayoutParams targetParams = (LayoutParams) targetView.getLayoutParams();
        if (targetParams.xSpan > 1 || targetParams.ySpan > 1)
            return;
        occupied[targetParams.x][targetParams.y] = false;
        Point targetPoint = findFreeSpace(targetParams.x, targetParams.y, peekDirection);
        Tool.print(targetPoint);
        onItemRearrange(new Point(targetParams.x, targetParams.y), targetPoint);
        targetParams.x = targetPoint.x;
        targetParams.y = targetPoint.y;
        occupied[targetPoint.x][targetPoint.y] = true;
        requestLayout();
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
        if (blockTouch)
            return true;
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
    }

    public void animateBackgroundShow() {
        animateBackground = true;
        invalidate();
    }

    public void animateBackgroundHide() {
        animateBackground = false;
        invalidate();
    }

    public Point findFreeSpace() {
        for (int y = 0; y < occupied[0].length; y++) {
            for (int x = 0; x < occupied.length; x++) {
                if (!occupied[x][y])
                    return new Point(x, y);
            }
        }
        return new Point(0, 0);
    }

    public boolean isValid(int x, int y) {
        return (x >= 0 && x <= occupied.length - 1 && y >= 0 && y <= occupied[0].length - 1);

    }

    public Point findFreeSpace(int cx, int cy, PeekDirection peekDirection) {
        if (peekDirection != null) {
            Point target;
            switch (peekDirection) {
                case B:
                    target = new Point(cx, cy - 1);
                    if (isValid(target.x, target.y) && !occupied[target.x][target.y])
                        return target;
                    break;
                case L:
                    target = new Point(cx + 1, cy);
                    if (isValid(target.x, target.y) && !occupied[target.x][target.y])
                        return target;
                    break;
                case R:
                    target = new Point(cx - 1, cy);
                    if (isValid(target.x, target.y) && !occupied[target.x][target.y])
                        return target;
                    break;
                case T:
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

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        float s = 7f;
        for (int x = 0; x < cellSpanH; x++) {
            for (int y = 0; y < cellSpanV; y++) {
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

    private void setOccupied(boolean b, LayoutParams lp) {
        for (int x = lp.x; x < lp.x + lp.xSpan; x++) {
            for (int y = lp.y; y < lp.y + lp.ySpan; y++) {
                occupied[x][y] = b;
            }
        }
    }

    public void addViewToGrid(View view, int x, int y, int xSpan, int ySpan) {
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, x, y, xSpan, ySpan);
        view.setLayoutParams(lp);
        addView(view);
    }

    public void addViewToGrid(View view) {
        addView(view);
    }

    public LayoutParams cellPositionToLayoutPrams(int cellx, int celly, int xSpan, int ySpan, LayoutParams current) {
        setOccupied(false, current);
        if (occupied[cellx][celly]) {
            setOccupied(true, current);
            return null;
        }

        int dx;
        int dy;
        dx = cellx + xSpan - 1;
        if (dx >= cellSpanH - 1) {
            dx = cellSpanH - 1;
            cellx = dx + 1 - xSpan;
        }
        dy = celly + ySpan - 1;
        if (dy >= cellSpanV - 1) {
            dy = cellSpanV - 1;
            celly = dy + 1 - ySpan;
        }

        for (int x2 = cellx; x2 < cellx + xSpan; x2++) {
            for (int y2 = celly; y2 < celly + ySpan; y2++) {
                if (occupied[x2][y2]) {
                    setOccupied(true, current);
                    return null;
                }
            }
        }
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, cellx, celly, xSpan, ySpan);
    }

    public LayoutParams cellPositionToLayoutPrams(int cellx, int celly, int xSpan, int ySpan) {
        if (occupied[cellx][celly]) {
            return null;
        }

        int dx;
        int dy;
        dx = cellx + xSpan - 1;
        if (dx >= cellSpanH - 1) {
            dx = cellSpanH - 1;
            cellx = dx + 1 - xSpan;
        }
        dy = celly + ySpan - 1;
        if (dy >= cellSpanV - 1) {
            dy = cellSpanV - 1;
            celly = dy + 1 - ySpan;
        }

        for (int x2 = cellx; x2 < cellx + xSpan; x2++) {
            for (int y2 = celly; y2 < celly + ySpan; y2++) {
                if (occupied[x2][y2]) {
                    return null;
                }
            }
        }
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, cellx, celly, xSpan, ySpan);
    }

    public View coordinateToChildView(Point pos) {
        if (pos == null) return null;
        for (int i = 0; i < getChildCount(); i++) {
            LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.x == pos.x && lp.y == pos.y)
                return getChildAt(i);
        }
        return null;
    }

    public Point touchPosToCoordinate(int mX, int mY, int xSpan, int ySpan, boolean checkAvailability) {
        for (int x = 0; x < cellSpanH; x++) {
            for (int y = 0; y < cellSpanV; y++) {
                Rect cell = cells[x][y];
                if (mY >= cell.top && mY <= cell.bottom && mX >= cell.left && mX <= cell.right) {
                    if (checkAvailability) {
                        if (occupied[x][y]) {
                            return null;
                        }

                        int dx;
                        int dy;
                        dx = x + xSpan - 1;
                        if (dx >= cellSpanH - 1) {
                            dx = cellSpanH - 1;
                            x = dx + 1 - xSpan;
                        }
                        dy = y + ySpan - 1;
                        if (dy >= cellSpanV - 1) {
                            dy = cellSpanV - 1;
                            y = dy + 1 - ySpan;
                        }

                        for (int x2 = x; x2 < x + xSpan; x2++) {
                            for (int y2 = y; y2 < y + ySpan; y2++) {
                                if (occupied[x2][y2]) {
                                    return null;
                                }
                            }
                        }
                    }

                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    public LayoutParams positionToLayoutPrams(int mX, int mY, int xSpan, int ySpan) {
        Point pos = touchPosToCoordinate(mX, mY, xSpan, ySpan, true);
        if (pos != null)
            return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, pos.x, pos.y, xSpan, ySpan);
        else return null;
    }

    public LayoutParams positionToLayoutPrams(Point pos, int xSpan, int ySpan) {
        if (pos != null)
            return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, pos.x, pos.y, xSpan, ySpan);
        else return null;
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

    private enum PeekDirection {
        T, L, R, B
    }

    public interface OnItemRearrangeListener {
        void onItemRearrange(Point from, Point to);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int x, y, xSpan = 1, ySpan = 1;

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
