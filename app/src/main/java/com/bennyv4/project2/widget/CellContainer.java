package com.bennyv4.project2.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.bennyv4.project2.R;

public class CellContainer extends ViewGroup
{
	private boolean[][] occupied;

	private Rect[][] cells;

	public int cellWidth,

	cellHeight,

	cellSpanVert = 4,

	cellSpanHori = 4;

	private Paint mPaint;

	private boolean hideGrid = true;

	public CellContainer(Context c){
		super(c);
		init();
	}

	public CellContainer(Context c, AttributeSet attr){
		super(c, attr);
		init();
	}

	public void setHideGrid(boolean hideGrid){
		this.hideGrid = hideGrid;
		invalidate();
	}

	public void init(){
		occupied = new boolean[cellSpanHori][cellSpanVert];

		for(int i = 0 ; i < cellSpanHori ; i++){
			for(int j = 0 ; j < cellSpanVert ; j++){
				occupied[i][j] = false;
			}
		}
		
		setWillNotDraw(false);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2f);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setColor(Color.WHITE);
		mPaint.setAlpha(0);
	}

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		
		float s = 7f;
		for(int x = 0 ; x < cellSpanHori ; x++){
			for(int y = 0 ; y < cellSpanVert ; y++){
				Rect cell = cells[x][y];
				
				canvas.save();
				canvas.rotate(45,cell.left,cell.top);
				canvas.drawRect(cell.left - s, cell.top - s, cell.left + s, cell.top + s, mPaint);
				canvas.restore();
				
				canvas.save();
				canvas.rotate(45,cell.left,cell.bottom);
				canvas.drawRect(cell.left - s, cell.bottom - s, cell.left + s, cell.bottom + s, mPaint);
				canvas.restore();
				
				canvas.save();
				canvas.rotate(45,cell.right,cell.top);
				canvas.drawRect(cell.right - s, cell.top - s , cell.right + s, cell.top + s, mPaint);
				canvas.restore();
				
				canvas.save();
				canvas.rotate(45,cell.right,cell.bottom);
				canvas.drawRect(cell.right - s, cell.bottom - s, cell.right + s, cell.bottom + s, mPaint);
				canvas.restore();
			}
		}
		

		if(hideGrid && mPaint.getAlpha() != 0){
			mPaint.setAlpha(Math.max(mPaint.getAlpha() - 20, 0));
			invalidate();
		}
		else if((!hideGrid) && mPaint.getAlpha() != 255){
			mPaint.setAlpha(Math.min(mPaint.getAlpha() + 20, 255));
			invalidate();
		}
	}

	@Override
	public void addView(View child){
		LayoutParams lp = (CellContainer.LayoutParams) child.getLayoutParams();
        setOccupied(true,lp);
		super.addView(child);
	}

    @Override
    public void removeView(View view) {
        LayoutParams lp = (CellContainer.LayoutParams) view.getLayoutParams();
        setOccupied(false,lp);
        super.removeView(view);
    }

    private void setOccupied(boolean b,LayoutParams lp){
        for (int x = lp.x ; x < lp.x + lp.xSpan ; x++){
            for (int y = lp.y ; y < lp.y + lp.ySpan ; y++){
                occupied[x][y] = b;
            }
        }
    }

	public void addViewToGrid(View view, int x, int y,int xSpan,int ySpan){
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,x,y,xSpan,ySpan);
		view.setLayoutParams(lp);
		addView(view);
	}

	public void addViewToGrid(View view){
		addView(view);
	}

	public LayoutParams positionToLayoutPrams(int mX, int mY, int xSpan, int ySpan){
		for(int x = 0 ; x < cellSpanHori ; x++){
			for(int y = 0 ; y < cellSpanVert ; y++){
				Rect cell = cells[x][y];
				if(mY >= cell.top && mY <= cell.bottom && mX >= cell.left && mX <= cell.right){
					if(occupied[x][y]){
						return null;
					}

                    int dx = x;
                    int dy = y;
                    if (xSpan > 1) {
                        dx = x + xSpan;
                        if (dx >= cellSpanHori - 1) {
                            dx = cellSpanHori - 1;
                            xSpan = dx - x+1;
                        }
                    }
                    if (ySpan > 1) {
                        dy = y + ySpan;
                        if (dy >= cellSpanVert - 1) {
                            dy = cellSpanVert - 1;
                            ySpan = dy - y+1;
                        }
                    }

                    for (int x2 = x ; x2 < dx ; x2++){
                        for (int y2 = y ; y < dy ; y2++){
                            if(occupied[x2][y2]){
                                return null;
                            }
                        }
                    }

					return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, x, y,xSpan,ySpan);
				}
			}
		}
		return null;
	}


	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){
		int width = r - l - getPaddingLeft() - getPaddingRight();
        int height = b - t - getPaddingTop() - getPaddingBottom();

		cellWidth = width / cellSpanHori;
		cellHeight = height / cellSpanVert;

		initCellInfo(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(), height - getPaddingBottom());

        final int count = getChildCount();

        for(int i = 0; i < count; i++){
            View child = getChildAt(i);

            if(child.getVisibility() == GONE)
                continue;

			LayoutParams lp = (CellContainer.LayoutParams) child.getLayoutParams();

			int childWidth = lp.xSpan * cellWidth;
			int childHeight = lp.ySpan * cellHeight;
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));

			Rect upRect = cells[lp.x][lp.y];
            Rect downRect = new Rect();
            if (lp.x+lp.xSpan-1 < cellSpanHori && lp.y+lp.ySpan-1 < cellSpanVert)
                downRect = cells[lp.x+lp.xSpan-1][lp.y+lp.ySpan-1];

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

	private void initCellInfo(int l, int t, int r, int b){
		cells = new Rect[cellSpanHori][cellSpanVert];

		int curLeft = l;
		int curTop = t;
		int curRight = l + cellWidth;
		int curBottom = t + cellHeight;

		for(int i = 0 ; i < cellSpanHori ; i++){
			if(i != 0){
				curLeft += cellWidth;
				curRight += cellWidth;
			}
			for(int j = 0 ; j < cellSpanVert ; j++){
				if(j != 0){
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

	public static class LayoutParams extends ViewGroup.LayoutParams
	{
		public int x,

		y,

		xSpan = 1,

		ySpan = 1;

		public LayoutParams(int w, int h, int x, int y){
			super(w, h);

			this.x = x;
			this.y = y;
		}

        public LayoutParams(int w, int h, int x, int y,int xSpan,int ySpan){
            super(w, h);

            this.x = x;
            this.y = y;

            this.xSpan = xSpan;
            this.ySpan = ySpan;
        }

		public LayoutParams(int w, int h){
			super(w, h);
		}
	}
}
