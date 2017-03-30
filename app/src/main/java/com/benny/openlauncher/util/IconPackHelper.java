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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.List;

/**
 * Created by BennyKok on 10/25/2016.
 */

public class IconPackHelper {
    public static void themePacs(AppManager appManager, final int iconSize, String resPacName, List<AppManager.App> apps) {
        //theming vars-----------------------------------------------
        Resources themeRes = null;
        String iconResource;
        int intres;
        int intresiconback = 0;
        int intresiconfront = 0;
        int intresiconmask = 0;
        float scaleFactor;

        Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
        p.setAntiAlias(true);

        Paint origP = new Paint(Paint.FILTER_BITMAP_FLAG);
        origP.setAntiAlias(true);

        Paint maskp = new Paint(Paint.FILTER_BITMAP_FLAG);
        maskp.setAntiAlias(true);
        maskp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        if (resPacName.compareTo("") != 0) {
            try {
                themeRes = appManager.getPackageManager().getResourcesForApplication(resPacName);
            } catch (Exception ignored) {
            }
            if (themeRes != null) {
                String[] backAndMaskAndFront = getIconBackAndMaskResourceName(themeRes, resPacName);
                if (backAndMaskAndFront[0] != null)
                    intresiconback = themeRes.getIdentifier(backAndMaskAndFront[0], "drawable", resPacName);
                if (backAndMaskAndFront[1] != null)
                    intresiconmask = themeRes.getIdentifier(backAndMaskAndFront[1], "drawable", resPacName);
                if (backAndMaskAndFront[2] != null)
                    intresiconfront = themeRes.getIdentifier(backAndMaskAndFront[2], "drawable", resPacName);
            }
        }

        BitmapFactory.Options uniformOptions = new BitmapFactory.Options();
        uniformOptions.inScaled = false;
        uniformOptions.inDither = false;
        uniformOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Canvas origCanv;
        Canvas canvas;
        scaleFactor = getScaleFactor(themeRes, resPacName);
        Bitmap back = null;
        Bitmap mask = null;
        Bitmap front = null;
        Bitmap scaledBitmap;
        Bitmap scaledOrig;
        Bitmap orig;

        if (resPacName.compareTo("") != 0 && themeRes != null) {
            try {
                if (intresiconback != 0)
                    back = BitmapFactory.decodeResource(themeRes, intresiconback, uniformOptions);
            } catch (Exception ignored) {
            }
            try {
                if (intresiconmask != 0)
                    mask = BitmapFactory.decodeResource(themeRes, intresiconmask, uniformOptions);
            } catch (Exception ignored) {
            }
            try {
                if (intresiconfront != 0)
                    front = BitmapFactory.decodeResource(themeRes, intresiconfront, uniformOptions);
            } catch (Exception ignored) {
            }
        }
        //theming vars-----------------------------------------------
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        for (int I = 0; I < apps.size(); I++) {
            if (themeRes != null) {
                iconResource = null;
                intres = 0;
                iconResource = getResourceName(themeRes, resPacName, "ComponentInfo{" + apps.get(I).packageName + "/" + apps.get(I).className + "}");
                if (iconResource != null) {
                    intres = themeRes.getIdentifier(iconResource, "drawable", resPacName);
                }

                if (intres != 0) {//has single drawable for app
                    apps.get(I).icon = new BitmapDrawable(BitmapFactory.decodeResource(themeRes, intres, uniformOptions));
                } else {
                    try {
                        orig = Bitmap.createBitmap(apps.get(I).icon.getIntrinsicWidth(), apps.get(I).icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    } catch (Exception e) {
                        continue;
                    }
                    apps.get(I).icon.setBounds(0, 0, apps.get(I).icon.getIntrinsicWidth(), apps.get(I).icon.getIntrinsicHeight());
                    apps.get(I).icon.draw(new Canvas(orig));

                    scaledOrig = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                    scaledBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(scaledBitmap);
                    if (back != null) {
                        canvas.drawBitmap(back, getResizedMatrix(back, iconSize, iconSize), p);
                    }

                    origCanv = new Canvas(scaledOrig);
                    orig = getResizedBitmap(orig, ((int) (iconSize * scaleFactor)), ((int) (iconSize * scaleFactor)));
                    origCanv.drawBitmap(orig, scaledOrig.getWidth() - (orig.getWidth() / 2) - scaledOrig.getWidth() / 2, scaledOrig.getWidth() - (orig.getWidth() / 2) - scaledOrig.getWidth() / 2, origP);

                    if (mask != null) {
                        origCanv.drawBitmap(mask, getResizedMatrix(mask, iconSize, iconSize), maskp);
                    }

                    if (back != null) {
                        canvas.drawBitmap(getResizedBitmap(scaledOrig, iconSize, iconSize), 0, 0, p);
                    } else
                        canvas.drawBitmap(getResizedBitmap(scaledOrig, iconSize, iconSize), 0, 0, p);

                    if (front != null)
                        canvas.drawBitmap(front, getResizedMatrix(front, iconSize, iconSize), p);

                    apps.get(I).icon = new BitmapDrawable(appManager.getContext().getResources(), scaledBitmap);
                }
            }
        }


        front = null;
        back = null;
        mask = null;
        scaledOrig = null;
        orig = null;
        scaledBitmap = null;
        canvas = null;
        origCanv = null;
        p = null;
        maskp = null;
        resPacName = null;
        iconResource = null;
        intres = 0;
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

    private static float getScaleFactor(Resources res, String string) {
        float scaleFactor = 1.0f;
        XmlResourceParser xrp = null;
        XmlPullParser xpp = null;
        try {
            int n;
            if ((n = res.getIdentifier("appfilter", "xml", string)) != 0) {
                xrp = res.getXml(n);
                System.out.println(n);
            } else {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setValidating(false);
                xpp = factory.newPullParser();
                InputStream raw = res.getAssets().open("appfilter.xml");
                xpp.setInput(raw, null);
            }

            if (n != 0) {
                while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT && scaleFactor == 1.0f) {
                    if (xrp.getEventType() == 2) {
                        try {
                            String s = xrp.getName();
                            if (s.equals("scale")) {
                                scaleFactor = Float.parseFloat(xrp.getAttributeValue(0));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    xrp.next();
                }
            } else {
                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && scaleFactor == 1.0f) {
                    if (xpp.getEventType() == 2) {
                        try {
                            String s = xpp.getName();
                            if (s.equals("scale")) {
                                scaleFactor = Float.parseFloat(xpp.getAttributeValue(0));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    xpp.next();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return scaleFactor;
    }


    private static String getResourceName(Resources res, String string, String componentInfo) {
        String resource = null;
        XmlResourceParser xrp = null;
        XmlPullParser xpp = null;
        try {
            int n;
            if ((n = res.getIdentifier("appfilter", "xml", string)) != 0) {
                xrp = res.getXml(n);
            } else {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setValidating(false);
                xpp = factory.newPullParser();
                InputStream raw = res.getAssets().open("appfilter.xml");
                xpp.setInput(raw, null);
            }

            if (n != 0) {
                while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT && resource == null) {
                    if (xrp.getEventType() == 2) {
                        try {
                            String s = xrp.getName();
                            if (s.equals("item")) {
                                if (xrp.getAttributeValue(0).compareTo(componentInfo) == 0) {
                                    resource = xrp.getAttributeValue(1);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    xrp.next();
                }
            } else {
                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && resource == null) {
                    if (xpp.getEventType() == 2) {
                        try {
                            String s = xpp.getName();
                            if (s.equals("item")) {
                                if (xpp.getAttributeValue(0).compareTo(componentInfo) == 0) {
                                    resource = xpp.getAttributeValue(1);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    xpp.next();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return resource;
    }


    private static String[] getIconBackAndMaskResourceName(Resources res, String packageName) {
        String[] resource = new String[3];
        XmlResourceParser xrp = null;
        XmlPullParser xpp = null;
        try {
            int n;
            if ((n = res.getIdentifier("appfilter", "xml", packageName)) != 0) {
                xrp = res.getXml(n);
            } else {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setValidating(false);
                xpp = factory.newPullParser();
                InputStream raw = res.getAssets().open("appfilter.xml");
                xpp.setInput(raw, null);
            }

            if (n != 0) {
                while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT && (resource[0] == null || resource[1] == null || resource[2] == null)) {
                    if (xrp.getEventType() == 2) {
                        try {
                            String s = xrp.getName();
                            if (s.equals("iconback")) {
                                resource[0] = xrp.getAttributeValue(0);
                            }
                            if (s.equals("iconmask")) {
                                resource[1] = xrp.getAttributeValue(0);
                            }
                            if (s.equals("iconupon")) {
                                resource[2] = xrp.getAttributeValue(0);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    xrp.next();
                }
            } else {
                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && (resource[0] == null || resource[1] == null || resource[2] == null)) {
                    if (xpp.getEventType() == 2) {
                        try {
                            String s = xpp.getName();
                            if (s.equals("iconback")) {
                                resource[0] = xpp.getAttributeValue(0);
                            }
                            if (s.equals("iconmask")) {
                                resource[1] = xpp.getAttributeValue(0);
                            }
                            if (s.equals("iconupon")) {
                                resource[2] = xpp.getAttributeValue(0);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    xpp.next();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return resource;
    }
}
