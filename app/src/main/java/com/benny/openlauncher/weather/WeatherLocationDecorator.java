package com.benny.openlauncher.weather;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.benny.openlauncher.R;

import java.util.ArrayList;

public class WeatherLocationDecorator extends RecyclerView.ItemDecoration {
    private Drawable _divider;
    ArrayList<WeatherLocation> _dividers = new ArrayList<>();

    public WeatherLocationDecorator(Resources resources) {
        _divider = resources.getDrawable(R.drawable.weather_divider);
    }

    public void addDivider(WeatherLocation loc) {
        _dividers.add(loc);
    }

    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            if (child instanceof LinearLayout) {
                TextView locationView = (TextView) child.findViewById(R.id.location);

                String name = locationView.getText().toString();
                WeatherLocation loc = WeatherLocation.parse(name);

                if (_dividers.contains(loc)) {
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + _divider.getIntrinsicHeight();

                    _divider.setBounds(left, top, right, bottom);
                    _divider.draw(c);
                }
            }
        }
    }

    public void removeDivider(WeatherLocation loc) {
        _dividers.remove(loc);
    }
}

