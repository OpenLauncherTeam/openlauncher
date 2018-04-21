package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.Tool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CellContainer extends ViewGroup {
    private HashMap _$_findViewCache;
    private boolean animateBackground;
    private final Paint bgPaint = new Paint(1);
    private boolean blockTouch;
    private Bitmap cachedOutlineBitmap;
    private int cellHeight;
    private int cellSpanH;
    private int cellSpanV;
    private int cellWidth;
    private Rect[][] cells;
    private Point currentOutlineCoordinate = new Point(-1, -1);
    private Long down = Long.valueOf(0);
    @Nullable
    private SimpleFingerGestures gestures;
    private boolean hideGrid = true;
    private final Paint mPaint = new Paint(1);
    private boolean[][] occupied;
    @Nullable
    private OnItemRearrangeListener onItemRearrangeListener;
    private final Paint outlinePaint = new Paint(1);
    private PeekDirection peekDirection;
    private Long peekDownTime = Long.valueOf(-1);
    private Point preCoordinate = new Point(-1, -1);
    private Point startCoordinate = new Point();
    @NotNull
    private final Rect tempRect = new Rect();

    public enum DragState {
        CurrentNotOccupied, OutOffRange, ItemViewNotFound, CurrentOccupied
    }

    public static final class LayoutParams extends android.view.ViewGroup.LayoutParams {
        private int x;
        private int xSpan = 1;
        private int y;
        private int ySpan = 1;

        public final int getX() {
            return this.x;
        }

        public final void setX(int v) {
            this.x = v;
        }

        public final int getY() {
            return this.y;
        }

        public final void setY(int v) {
            this.y = v;
        }

        public final int getXSpan() {
            return this.xSpan;
        }

        public final void setXSpan(int v) {
            this.xSpan = v;
        }

        public final int getYSpan() {
            return this.ySpan;
        }

        public final void setYSpan(int v) {
            this.ySpan = v;
        }

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

    public interface OnItemRearrangeListener {
        void onItemRearrange(@NotNull Point point, @NotNull Point point2);
    }

    public enum PeekDirection {
        UP, LEFT, RIGHT, DOWN
    }

    public final int getCellWidth() {
        return this.cellWidth;
    }

    public final int getCellHeight() {
        return this.cellHeight;
    }

    public final int getCellSpanV() {
        return this.cellSpanV;
    }

    public final int getCellSpanH() {
        return this.cellSpanH;
    }

    public final void setBlockTouch(boolean v) {
        this.blockTouch = v;
    }

    public final void setGestures(@Nullable SimpleFingerGestures v) {
        this.gestures = v;
    }

    public final void setOnItemRearrangeListener(@Nullable OnItemRearrangeListener v) {
        this.onItemRearrangeListener = v;
    }

    @NotNull
    public final List<View> getAllCells() {
        ArrayList views = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            views.add(getChildAt(i));
        }
        return views;
    }

    public CellContainer(@NotNull Context c) {
        this(c, null);
    }

    public CellContainer(@NotNull Context c, @Nullable AttributeSet attr) {
        super(c, attr);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth(2.0f);
        this.mPaint.setStrokeJoin(Join.ROUND);
        this.mPaint.setColor(-1);
        this.mPaint.setAlpha(0);
        this.bgPaint.setStyle(Style.FILL);
        this.bgPaint.setColor(-1);
        this.bgPaint.setAlpha(0);
        this.outlinePaint.setColor(-1);
        this.outlinePaint.setAlpha(0);
        init();
    }

    public final void setGridSize(int x, int y) {
        this.cellSpanV = y;
        this.cellSpanH = x;

        occupied = new boolean[cellSpanH][cellSpanV];
        for (int i = 0; i < cellSpanH; i++) {
            for (int j = 0; j < cellSpanV; j++) {
                occupied[i][j] = false;
            }
        }
        requestLayout();
    }

    public final void setHideGrid(boolean hideGrid) {
        this.hideGrid = hideGrid;
        invalidate();
    }

    public final void resetOccupiedSpace() {
        if (this.cellSpanH > 0 && this.cellSpanV > 0) {
            occupied = new boolean[cellSpanH][cellSpanV];
        }
    }

    public void removeAllViews() {
        resetOccupiedSpace();
        super.removeAllViews();
    }

    public final void projectImageOutlineAt(@NotNull Point newCoordinate, @Nullable Bitmap bitmap) {
        this.cachedOutlineBitmap = bitmap;
        if (!currentOutlineCoordinate.equals(newCoordinate)) {
            this.outlinePaint.setAlpha(0);
        }
        this.currentOutlineCoordinate.set(newCoordinate.x, newCoordinate.y);
        invalidate();
    }

    private final void drawCachedOutlineBitmap(Canvas canvas, Rect cell) {
        if (this.cachedOutlineBitmap != null) {
            Bitmap bitmap = this.cachedOutlineBitmap;
            float centerX = (float) cell.centerX();
            Bitmap bitmap2 = this.cachedOutlineBitmap;
            float f = (float) 2;
            centerX -= ((float) bitmap2.getWidth()) / f;
            float centerY = (float) cell.centerY();
            Bitmap bitmap3 = this.cachedOutlineBitmap;
            canvas.drawBitmap(bitmap, centerX, centerY - (((float) bitmap3.getWidth()) / f), this.outlinePaint);
        }
    }

    public final void clearCachedOutlineBitmap() {
        this.outlinePaint.setAlpha(0);
        this.cachedOutlineBitmap = (Bitmap) null;
        invalidate();
    }

    @NotNull
    public final DragState peekItemAndSwap(@NotNull DragEvent event, @NotNull Point coordinate) {
        return peekItemAndSwap((int) event.getX(), (int) event.getY(), coordinate);
    }

    @NotNull
    public final DragState peekItemAndSwap(int x, int y, @NotNull Point coordinate) {
        touchPosToCoordinate(coordinate, x, y, 1, 1, false, false);
        if (coordinate.x != -1) {
            if (coordinate.y != -1) {
                DragState dragState;
                if (this.startCoordinate == null) {
                    this.startCoordinate = coordinate;
                }
                if (!preCoordinate.equals(coordinate)) {
                    this.peekDownTime = Long.valueOf(-1);
                }
                Long l = this.peekDownTime;
                if (l != null && l == -1) {
                    this.peekDirection = getPeekDirectionFromCoordinate(this.startCoordinate, coordinate);
                    this.peekDownTime = Long.valueOf(System.currentTimeMillis());
                    this.preCoordinate = coordinate;

                }
                boolean[][] zArr = this.occupied;
                if (zArr[coordinate.x][coordinate.y]) {
                    dragState = DragState.CurrentOccupied;
                } else {
                    dragState = DragState.CurrentNotOccupied;
                }
                return dragState;
            }
        }
        return DragState.OutOffRange;
    }

    private final PeekDirection getPeekDirectionFromCoordinate(Point from, Point to) {
        if (from.y - to.y > 0) {
            return PeekDirection.UP;
        }
        if (from.y - to.y < 0) {
            return PeekDirection.DOWN;
        }
        if (from.x - to.x > 0) {
            return PeekDirection.LEFT;
        }
        if (from.x - to.x < 0) {
            return PeekDirection.RIGHT;
        }
        return null;
    }

    public boolean onTouchEvent(@NotNull MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case 0:
                this.down = System.currentTimeMillis();
                break;
            case 1:
                long currentTimeMillis = System.currentTimeMillis();
                Long l = this.down;
                if (currentTimeMillis - l.longValue() < 260 && this.blockTouch) {
                    performClick();
                    break;
                }
            default:
                break;
        }
        if (this.blockTouch) {
            return true;
        }
        if (this.gestures == null) {
            Tool.print((Object) "gestures is null");
            return super.onTouchEvent(event);
        }
        SimpleFingerGestures simpleFingerGestures = this.gestures;
        simpleFingerGestures.onTouch(this, event);
        return super.onTouchEvent(event);
    }

    public boolean onInterceptTouchEvent(@NotNull MotionEvent ev) {
        if (this.blockTouch) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void init() {
        setWillNotDraw(false);
    }

    public final void animateBackgroundShow() {
        this.animateBackground = true;
        invalidate();
    }

    public final void animateBackgroundHide() {
        this.animateBackground = false;
        invalidate();
    }

    @Nullable
    public final Point findFreeSpace() {
        boolean[][] zArr = this.occupied;
        int length = zArr[0].length;
        for (int y = 0; y < length; y++) {
            boolean[][] zArr2 = this.occupied;
            int length2 = zArr2.length;
            for (int x = 0; x < length2; x++) {
                boolean[][] zArr3 = this.occupied;
                if (!zArr3[x][y]) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    @Nullable
    public final Point findFreeSpace(int spanX, int spanY) {
        boolean[][] zArr = this.occupied;
        int length = zArr[0].length;
        int y = 0;
        while (y < length) {
            boolean[][] zArr2 = this.occupied;
            int length2 = zArr2.length;
            int x = 0;
            while (x < length2) {
                boolean[][] zArr3 = this.occupied;
                if (!zArr3[x][y] && !checkOccupied(new Point(x, y), spanX, spanY)) {
                    return new Point(x, y);
                }
                x++;
            }
            y++;
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0f, 0f, getWidth(), getHeight(), bgPaint);

        if (cells == null)
            return;

        float s = 7f;
        for (int x = 0; x < cellSpanH; x++) {
            for (int y = 0; y < cellSpanV; y++) {
                if (x >= cells.length || y >= cells[0].length)
                    continue;

                Rect cell = cells[x][y];

                canvas.save();
                canvas.rotate(45f, cell.left, cell.top);
                canvas.drawRect(cell.left - s, cell.top - s, cell.left + s, cell.top + s, mPaint);
                canvas.restore();

                canvas.save();
                canvas.rotate(45f, cell.left, cell.bottom);
                canvas.drawRect(cell.left - s, cell.bottom - s, cell.left + s, cell.bottom + s, mPaint);
                canvas.restore();

                canvas.save();
                canvas.rotate(45f, cell.right, cell.top);
                canvas.drawRect(cell.right - s, cell.top - s, cell.right + s, cell.top + s, mPaint);
                canvas.restore();

                canvas.save();
                canvas.rotate(45f, cell.right, cell.bottom);
                canvas.drawRect(cell.right - s, cell.bottom - s, cell.right + s, cell.bottom + s, mPaint);
                canvas.restore();
            }
        }

        //Animating alpha and drawing projected image
        Home home = Home.Companion.getLauncher();
        if (home != null && home.getDragNDropView().getDragExceedThreshold() && currentOutlineCoordinate.x != -1 && currentOutlineCoordinate.y != -1) {
            if (outlinePaint.getAlpha() != 160)
                outlinePaint.setAlpha(Math.min(outlinePaint.getAlpha() + 20, 160));
            drawCachedOutlineBitmap(canvas, cells[currentOutlineCoordinate.x][currentOutlineCoordinate.y]);

            if (outlinePaint.getAlpha() <= 160)
                invalidate();
        }

        //Animating alpha
        if (hideGrid && mPaint.getAlpha() != 0) {
            mPaint.setAlpha(Math.max(mPaint.getAlpha() - 20, 0));
            invalidate();
        } else if (!hideGrid && mPaint.getAlpha() != 255) {
            mPaint.setAlpha(Math.min(mPaint.getAlpha() + 20, 255));
            invalidate();
        }

        //Animating alpha
        if (!animateBackground && bgPaint.getAlpha() != 0) {
            bgPaint.setAlpha(Math.max(bgPaint.getAlpha() - 10, 0));
            invalidate();
        } else if (animateBackground && bgPaint.getAlpha() != 100) {
            bgPaint.setAlpha(Math.min(bgPaint.getAlpha() + 10, 100));
            invalidate();
        }
    }

    public void addView(@NotNull View child) {
        LayoutParams lp = (CellContainer.LayoutParams) child.getLayoutParams();
        setOccupied(true, lp);
        super.addView(child);
    }

    public void removeView(@NotNull View view) {
        LayoutParams lp = (CellContainer.LayoutParams) view.getLayoutParams();
        setOccupied(false, lp);
        super.removeView(view);
    }

    public final void addViewToGrid(@NotNull View view, int x, int y, int xSpan, int ySpan) {
        view.setLayoutParams(new LayoutParams(-2, -2, x, y, xSpan, ySpan));
        addView(view);
    }

    public final void addViewToGrid(@NotNull View view) {
        addView(view);
    }

    public final void setOccupied(boolean b, @NotNull LayoutParams lp) {
        Tool.print("Setting");
        int x = lp.getX() + lp.getXSpan();
        for (int x2 = lp.getX(); x2 < x; x2++) {
            int y = lp.getY() + lp.getYSpan();
            for (int y2 = lp.getY(); y2 < y; y2++) {
                Object[] objArr = new Object[2];
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Setting ok (");
                stringBuilder.append(String.valueOf(b));
                objArr[0] = stringBuilder.toString();
                objArr[1] = ")";
                Tool.print(objArr);
                boolean[][] zArr = this.occupied;
                zArr[x2][y2] = b;
            }
        }
    }

    public final boolean checkOccupied(@NotNull Point start, int spanX, int spanY) {
        int i = start.x + spanX;
        boolean[][] zArr = this.occupied;
        if (i <= ((Object[]) zArr).length) {
            i = start.y + spanY;
            zArr = this.occupied;
            if (i <= zArr[0].length) {
                int i2 = start.y + spanY;
                for (i = start.y; i < i2; i++) {
                    int i3 = start.x + spanX;
                    for (int x = start.x; x < i3; x++) {
                        boolean[][] zArr2 = this.occupied;
                        if (zArr2[x][i]) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    @Nullable
    public final View coordinateToChildView(@Nullable Point pos) {
        if (pos == null) {
            return null;
        }
        for (int i = 0; i < getChildCount(); i++) {
            LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (pos.x >= lp.x && pos.y >= lp.y && pos.x < lp.x + lp.xSpan && pos.y < lp.y + lp.ySpan) {
                return getChildAt(i);
            }
        }
        return null;
    }

    @Nullable
    public final LayoutParams coordinateToLayoutParams(int mX, int mY, int xSpan, int ySpan) {
        Point pos = new Point();
        touchPosToCoordinate(pos, mX, mY, xSpan, ySpan, true);
        return !pos.equals(-1, -1) ? new LayoutParams(WRAP_CONTENT, WRAP_CONTENT, pos.x, pos.y, xSpan, ySpan) : null;
    }

    public void touchPosToCoordinate(@NotNull Point coordinate, int mX, int mY, int xSpan, int ySpan, boolean checkAvailability) {
        touchPosToCoordinate(coordinate, mX, mY, xSpan, ySpan, checkAvailability, false);
    }


    public final void touchPosToCoordinate(@NotNull Point coordinate, int mX, int mY, int xSpan, int ySpan, boolean checkAvailability, boolean checkBoundary) {
        if (cells == null) {
            coordinate.set(-1, -1);
            return;
        }


        mX -= (xSpan - 1) * cellWidth / 2f;
        mY -= (ySpan - 1) * cellHeight / 2f;

        int x = 0;
        while (x < cellSpanH) {
            int y = 0;
            while (y < cellSpanV) {
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
                        int dp2 = Tool.toPx(6);
                        offsetCell.inset(dp2, dp2);
                        if (mY >= offsetCell.top && mY <= offsetCell.bottom && mX >= offsetCell.left && mX <= offsetCell.right) {
                            coordinate.set(-1, -1);
                            return;
                        }
                    }
                    coordinate.set(x, y);
                    return;
                }
                y++;
            }
            x++;
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = ((r - l) - getPaddingLeft()) - getPaddingRight();
        int height = ((b - t) - getPaddingTop()) - getPaddingBottom();
        if (this.cellSpanH == 0) {
            cellSpanH = 1;
        }
        if (cellSpanV == 0) {
            cellSpanV = 1;
        }
        cellWidth = width / cellSpanH;
        cellHeight = height / cellSpanV;
        initCellInfo(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(), height - getPaddingBottom());
        int count = getChildCount();
        if (cells != null) {
            int i = 0;
            while (i < count) {
                View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    child.measure(MeasureSpec.makeMeasureSpec(lp.getXSpan() * cellWidth, View.MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(lp.getYSpan() * cellHeight, View.MeasureSpec.EXACTLY));
                    Rect[][] rectArr = cells;
                    Rect upRect = rectArr[lp.getX()][lp.getY()];
                    Rect downRect = tempRect;
                    if ((lp.getX() + lp.getXSpan()) - 1 < cellSpanH && (lp.getY() + lp.getYSpan()) - 1 < cellSpanV) {
                        Rect[][] rectArr2 = cells;
                        downRect = rectArr2[(lp.getX() + lp.getXSpan()) - 1][(lp.getY() + lp.getYSpan()) - 1];
                    }
                    if (lp.getXSpan() == 1 && lp.getYSpan() == 1) {
                        child.layout(upRect.left, upRect.top, upRect.right, upRect.bottom);
                    } else if (lp.getXSpan() > 1 && lp.getYSpan() > 1) {
                        child.layout(upRect.left, upRect.top, downRect.right, downRect.bottom);
                    } else if (lp.getXSpan() > 1) {
                        child.layout(upRect.left, upRect.top, downRect.right, upRect.bottom);
                    } else if (lp.getYSpan() > 1) {
                        child.layout(upRect.left, upRect.top, upRect.right, downRect.bottom);
                    }
                }
                i++;
            }
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
}
