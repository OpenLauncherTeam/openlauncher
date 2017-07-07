package com.benny.openlauncher.core.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.core.interfaces.IconDrawer;
import com.benny.openlauncher.core.interfaces.IconProvider;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.viewutil.GroupIconDrawable;

public class SimpleIconProvider implements IconProvider {

    protected Drawable drawable;
    protected int drawableResource;

    public SimpleIconProvider(Drawable drawable) {
        this.drawable = drawable;
        this.drawableResource = -1;
    }

    public SimpleIconProvider(int drawableResource) {
        this.drawable = null;
        this.drawableResource = drawableResource;
    }

    private Drawable getDrawable() {
        if (drawable != null) {
            return drawable;
        } else if (drawableResource > 0) {
            return Setup.appContext().getResources().getDrawable(drawableResource);
        }
        return null;
    }

    @Override
    public void displayIcon(ImageView iv, int forceSize) {
        Drawable d = getDrawable();
        d = scaleDrawable(d, forceSize);
        iv.setImageDrawable(d);
    }

    @Override
    public void displayCompoundIcon(TextView tv, int gravity, int forceSize) {
        Drawable d = getDrawable();
        d = scaleDrawable(d, forceSize);

        if (gravity == Gravity.LEFT || gravity == Gravity.START) {
            tv.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        } else if (gravity == Gravity.RIGHT || gravity == Gravity.END) {
            tv.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
        } else if (gravity == Gravity.TOP) {
            tv.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        } else if (gravity == Gravity.BOTTOM) {
            tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d);
        }
    }

    @Override
    public void cancelLoad(ImageView iv) {
        // nothing to load...
    }

    @Override
    public void cancelLoad(TextView tv) {
        // nothing to load...
    }

    @Override
    public void cancelLoadDrawable() {
        // nothing to load...
    }

    @Override
    public void loadDrawable(IconDrawer iconDrawer, int forceSize) {
        // we simply load the drawable in a synchronise way
        iconDrawer.onIconAvailable(getDrawable());
    }

    @Override
    public Drawable getDrawableSynchronously(int forceSize) {
        Drawable d = getDrawable();
        d = scaleDrawable(d, forceSize);
        return d;
    }

    @Override
    public boolean isGroupIconDrawable() {
        return drawable != null && drawable instanceof GroupIconDrawable;
    }

    private Drawable scaleDrawable(Drawable drawable, int forceSize) {
        if (drawable != null && forceSize != Definitions.NO_SCALE) {
            forceSize = Tool.dp2px(forceSize, Setup.appContext());
            drawable = new BitmapDrawable(Setup.appContext().getResources(), Bitmap.createScaledBitmap(Tool.drawableToBitmap(drawable), forceSize, forceSize, true));
        }
        return drawable;
    }
}