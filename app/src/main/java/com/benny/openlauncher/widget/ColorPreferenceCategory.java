package com.benny.openlauncher.widget;

import android.content.Context;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.benny.openlauncher.util.AppSettings;

public class ColorPreferenceCategory extends PreferenceCategory {
    public ColorPreferenceCategory(Context context) {
        super(context);
    }

    public ColorPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        titleView.setTextColor(AppSettings.get().getPrimaryColor());
    }
}