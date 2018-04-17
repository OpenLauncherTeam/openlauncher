package com.benny.openlauncher.widget;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;

import com.benny.openlauncher.activity.CoreHome;
import com.benny.openlauncher.util.Tool;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Calendar;

/**
 * Created by BennyKok on 9/9/2017.
 */

public class CalendarDropDownView extends CardView implements View.OnClickListener {

    public MaterialCalendarView calendarView;
    private boolean stateOpened = false;

    public CalendarDropDownView(Context context) {
        super(context);
        init();
    }

    public CalendarDropDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        calendarView = new MaterialCalendarView(getContext());
        addView(calendarView);
        int twoDp = Tool.INSTANCE.dp2px(2, getContext());
        setContentPadding(0, twoDp * 5, 0, 0);
        calendarView.setTileHeightDp(40);

        Tool.INSTANCE.invisibleViews(calendarView);
        setScaleY(0);
    }

    public void animateShow() {
        if (stateOpened) return;
        Tool.INSTANCE.invisibleViews(CoreHome.Companion.getLauncher().getSearchBar());
        CoreHome.Companion.getLauncher().dimBackground();
        CoreHome.Companion.getLauncher().clearRoomForPopUp();
        CoreHome.Companion.getLauncher().getBackground().setOnClickListener(this);
        calendarView.setSelectedDate(Calendar.getInstance());
        stateOpened = true;
        animate().scaleY(1).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                Tool.INSTANCE.visibleViews(200, calendarView);
            }
        });
    }

    public void animateHide() {
        if (!stateOpened) return;
        Tool.INSTANCE.visibleViews(CoreHome.Companion.getLauncher().getSearchBar());
        CoreHome.Companion.getLauncher().unDimBackground();
        CoreHome.Companion.getLauncher().unClearRoomForPopUp();
        CoreHome.Companion.getLauncher().getBackground().setOnClickListener(null);
        stateOpened = false;
        Tool.INSTANCE.invisibleViews(200, calendarView);
        animate().scaleY(0).setStartDelay(200).setDuration(200);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            ((MarginLayoutParams) getLayoutParams()).topMargin = insets.getSystemWindowInsetTop();
        }
        return insets;
    }

    @Override
    public void onClick(View view) {
        animateHide();
    }
}
