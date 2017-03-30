package com.bennyv5.materialpreffragment;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.ViewGroup;

public class Tool {
    public static int pixelToDp(Context ctx, int in) {
        int out = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, in, ctx.getResources().getDisplayMetrics());
        return out;
    }

    public static String warpColorTag(String in, int color) {
        return "<font color=" + String.format("#%06X", 0xFFFFFF & color) + ">" + in + "</font>";
    }

    public static ViewGroup.LayoutParams matchParentLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static ViewGroup.LayoutParams matchParentWidthLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static int dp2px(float dp, Context context) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
