package com.benny.openlauncher.util;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;

import com.benny.openlauncher.model.App;

import java.util.List;

public class IconPackHelper {
    public static void applyIconPack(AppManager appManager, final int iconSize, String iconPackName, List<App> apps) {
        Resources iconPackResources = null;
        int intResourceIcon = 0;
        int intResourceBack = 0;
        int intResourceMask = 0;
        int intResourceUpon = 0;
        float scale = 1;

        Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
        p.setAntiAlias(true);

        Paint origP = new Paint(Paint.FILTER_BITMAP_FLAG);
        origP.setAntiAlias(true);

        Paint maskP = new Paint(Paint.FILTER_BITMAP_FLAG);
        maskP.setAntiAlias(true);
        maskP.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        if (!iconPackName.equals("")) {
            try {
                iconPackResources = appManager.getPackageManager().getResourcesForApplication(iconPackName);
            } catch (Exception e) {
                System.out.println(e);
            }
            if (iconPackResources != null) {
                if (getResource(iconPackResources, iconPackName, "iconback", null) != null)
                    intResourceBack = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconback", null), "drawable", iconPackName);
                if (getResource(iconPackResources, iconPackName, "iconmask", null) != null)
                    intResourceMask = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconmask", null), "drawable", iconPackName);
                if (getResource(iconPackResources, iconPackName, "iconupon", null) != null)
                    intResourceUpon = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconupon", null), "drawable", iconPackName);
                if (getResource(iconPackResources, iconPackName, "scale", null) != null)
                    scale = Float.parseFloat(getResource(iconPackResources, iconPackName, "scale", null));
            }
        }

        BitmapFactory.Options uniformOptions = new BitmapFactory.Options();
        uniformOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        uniformOptions.inScaled = false;
        uniformOptions.inDither = false;

        Bitmap back = null;
        Bitmap mask = null;
        Bitmap upon = null;
        Canvas canvasOrig;
        Canvas canvas;
        Bitmap scaledBitmap;
        Bitmap scaledOrig;
        Bitmap orig;

        if (iconPackName.compareTo("") != 0 && iconPackResources != null) {
            try {
                if (intResourceBack != 0)
                    back = BitmapFactory.decodeResource(iconPackResources, intResourceBack, uniformOptions);
                if (intResourceMask != 0)
                    mask = BitmapFactory.decodeResource(iconPackResources, intResourceMask, uniformOptions);
                if (intResourceUpon != 0)
                    upon = BitmapFactory.decodeResource(iconPackResources, intResourceUpon, uniformOptions);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        for (int i = 0; i < apps.size(); i++) {
            if (iconPackResources != null) {
                String iconResource = getResource(iconPackResources, iconPackName, null, apps.get(i).getComponentName());
                if (iconResource != null) {
                    intResourceIcon = iconPackResources.getIdentifier(iconResource, "drawable", iconPackName);
                } else {
                    intResourceIcon = 0;
                }

                if (intResourceIcon != 0) {
                    // has single drawable for app
                    apps.get(i).setIcon(new BitmapDrawable(BitmapFactory.decodeResource(iconPackResources, intResourceIcon, uniformOptions)));
                } else {
                    try {
                        orig = Bitmap.createBitmap(apps.get(i).getIcon().getIntrinsicWidth(), apps.get(i).getIcon().getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    } catch (Exception e) {
                        continue;
                    }
                    apps.get(i).getIcon().setBounds(0, 0, apps.get(i).getIcon().getIntrinsicWidth(), apps.get(i).getIcon().getIntrinsicHeight());
                    apps.get(i).getIcon().draw(new Canvas(orig));

                    scaledOrig = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                    scaledBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(scaledBitmap);

                    if (back != null)
                        canvas.drawBitmap(back, getResizedMatrix(back, iconSize, iconSize), p);

                    canvasOrig = new Canvas(scaledOrig);
                    orig = getResizedBitmap(orig, (int) (iconSize * scale), (int) (iconSize * scale));
                    canvasOrig.drawBitmap(orig, scaledOrig.getWidth() - (orig.getWidth() / 2) - scaledOrig.getWidth() / 2, scaledOrig.getWidth() - (orig.getWidth() / 2) - scaledOrig.getWidth() / 2, origP);

                    if (mask != null)
                        canvasOrig.drawBitmap(mask, getResizedMatrix(mask, iconSize, iconSize), maskP);

                    canvas.drawBitmap(getResizedBitmap(scaledOrig, iconSize, iconSize), 0, 0, p);

                    if (upon != null)
                        canvas.drawBitmap(upon, getResizedMatrix(upon, iconSize, iconSize), p);

                    apps.get(i).setIcon(new BitmapDrawable(appManager.getContext().getResources(), scaledBitmap));
                }
            }
        }
    }

    private static String getResource(Resources resources, String packageName, String resourceName, String componentName) {
        XmlResourceParser xrp;
        String resource = null;
        try {
            int resourceValue = resources.getIdentifier("appfilter", "xml", packageName);
            if (resourceValue != 0) {
                xrp = resources.getXml(resourceValue);
                while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                    if (xrp.getEventType() == 2) {
                        try {
                            String string = xrp.getName();
                            if (componentName != null) {
                                if (xrp.getAttributeValue(0).compareTo(componentName) == 0) {
                                    resource = xrp.getAttributeValue(1);
                                }
                            } else if (string.equals(resourceName)) {
                                resource = xrp.getAttributeValue(0);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                    xrp.next();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return resource;
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    private static Matrix getResizedMatrix(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return matrix;
    }
}
