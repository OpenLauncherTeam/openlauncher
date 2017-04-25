package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.IconLabelItem;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BennyKok on 4/25/2017.
 */

public class DesktopOptionView extends FrameLayout {

    private AppCompatTextView setAsHomeButton;
    private RecyclerView actionRecyclerView;

    private FastItemAdapter<IconLabelItem> actionAdapter = new FastItemAdapter<>();
    private DesktopOptionViewListener desktopOptionViewListener;

    public DesktopOptionView(@NonNull Context context) {
        super(context);
        init();
    }

    public DesktopOptionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DesktopOptionView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setDesktopOptionViewListener(DesktopOptionViewListener desktopOptionViewListener) {
        this.desktopOptionViewListener = desktopOptionViewListener;
    }

    public void setStarButtonColored(boolean colored) {
        if (colored) {
            setAsHomeButton.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_star_yellow_36dp),null,null,null);
        } else {
            setAsHomeButton.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_star_border_black_36dp),null,null,null);
        }
    }

    private void init() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");

        setAsHomeButton = new AppCompatTextView(getContext());
        setAsHomeButton.setText("Home");
        setAsHomeButton.setTypeface(typeface);
        setAsHomeButton.setGravity(Gravity.CENTER);
        setAsHomeButton.setTextColor(Color.WHITE);
        setAsHomeButton.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_star_yellow_36dp),null,null,null);
        setAsHomeButton.setCompoundDrawablePadding(Tool.dp2px(4,getContext()));
        setAsHomeButton.setBackgroundColor(Color.TRANSPARENT);
        LayoutParams setAsHomeButtonLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setAsHomeButtonLP.gravity = Gravity.CENTER_HORIZONTAL;
        addView(setAsHomeButton, setAsHomeButtonLP);

        setAsHomeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (desktopOptionViewListener != null) {
                    desktopOptionViewListener.onSetPageAsHome();
                    setStarButtonColored(true);
                }
            }
        });

        actionRecyclerView = new RecyclerView(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        actionRecyclerView.setLayoutManager(linearLayoutManager);
        actionRecyclerView.setAdapter(actionAdapter);
        LayoutParams actionRecyclerViewLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionRecyclerViewLP.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        addView(actionRecyclerView, actionRecyclerViewLP);

        List<IconLabelItem> items = new ArrayList<>();
        items.add(new IconLabelItem(getContext(), R.drawable.ic_clear_black_24dp, R.string.remove, null, Gravity.TOP,Color.WHITE,Gravity.CENTER,0, typeface));
        items.add(new IconLabelItem(getContext(), R.drawable.ic_dashboard_black_24dp, R.string.add_widget, null, Gravity.TOP,Color.WHITE,Gravity.CENTER,0,typeface));
        items.add(new IconLabelItem(getContext(), R.drawable.ic_launch_black_24dp, R.string.action, null, Gravity.TOP,Color.WHITE,Gravity.CENTER,0,typeface));
        items.add(new IconLabelItem(getContext(), R.drawable.ic_settings_launcher_36dp, R.string.settings, null, Gravity.TOP,Color.WHITE,Gravity.CENTER,0,typeface));
        actionAdapter.set(items);

        actionAdapter.withOnClickListener(new FastAdapter.OnClickListener<IconLabelItem>() {
            @Override
            public boolean onClick(View v, IAdapter<IconLabelItem> adapter, IconLabelItem item, int position) {
                if (desktopOptionViewListener != null) {
                    switch (position) {
                        case 0:
                            desktopOptionViewListener.onRemovePage();
                            break;
                        case 1:
                            desktopOptionViewListener.onPickWidget();
                            break;
                        case 2:
                            desktopOptionViewListener.onPickDesktopAction();
                            break;
                        case 3:
                            desktopOptionViewListener.onLaunchSettings();
                            break;
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public interface DesktopOptionViewListener {
        void onRemovePage();

        void onSetPageAsHome();

        void onLaunchSettings();

        void onPickDesktopAction();

        void onPickWidget();
    }
}
