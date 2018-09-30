package com.benny.openlauncher.widget;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.Tool;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Calendar;

/**
 * Created by BennyKok on 9/9/2017.
 */

public class CalendarView extends CardView implements View.OnClickListener {

    public MaterialCalendarView _calendarView;
    private boolean _stateOpened = false;

    public CalendarView(Context context) {
        super(context);
        init();
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        _calendarView = new MaterialCalendarView(getContext());
        addView(_calendarView);
        int twoDp = Tool.dp2px(2, getContext());
        setContentPadding(0, twoDp * 5, 0, 0);
        _calendarView.setTileHeightDp(40);

        Tool.invisibleViews(_calendarView);
        setScaleY(0);
    }

    public void animateShow() {
        if (_stateOpened) return;
        Tool.invisibleViews(HomeActivity.Companion.getLauncher().getSearchBar());
        HomeActivity.Companion.getLauncher().dimBackground();
        HomeActivity.Companion.getLauncher().clearRoomForPopUp();
        HomeActivity.Companion.getLauncher().getBackground().setOnClickListener(this);
        _calendarView.setSelectedDate(Calendar.getInstance());
        _stateOpened = true;
        animate().scaleY(1).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                Tool.visibleViews(200, _calendarView);
            }
        });
    }

    public void animateHide() {
        if (!_stateOpened) return;
        Tool.visibleViews(HomeActivity.Companion.getLauncher().getSearchBar());
        HomeActivity.Companion.getLauncher().unDimBackground();
        HomeActivity.Companion.getLauncher().unClearRoomForPopUp();
        HomeActivity.Companion.getLauncher().getBackground().setOnClickListener(null);
        _stateOpened = false;
        Tool.invisibleViews(200, _calendarView);
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
