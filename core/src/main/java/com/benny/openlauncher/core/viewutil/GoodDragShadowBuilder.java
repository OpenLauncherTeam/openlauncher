package com.benny.openlauncher.core.viewutil;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

import com.benny.openlauncher.core.activity.CoreHome;

public class GoodDragShadowBuilder extends View.DragShadowBuilder {
    int x;
    int y;

    public GoodDragShadowBuilder(View view) {
        super(view);
        this.x = CoreHome.Companion.getTouchX();
        this.y = CoreHome.Companion.getTouchY();
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        shadowSize.set(getView().getWidth(), getView().getHeight());
        if (x < 0 || y < 0)
            shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2);
        else
            shadowTouchPoint.set(x, y);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        getView().draw(canvas);
    }
}
