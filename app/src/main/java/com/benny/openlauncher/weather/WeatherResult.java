package com.benny.openlauncher.weather;

import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.IconPackHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class WeatherResult {
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Drawable>>> _iconCache = new HashMap<>();

    public ArrayList<Pair<Double, Integer>> _weatherResults = new ArrayList();

    public void add(Double temperature, int resourceId) {
        _weatherResults.add(new Pair(round(temperature, 1), resourceId));
    }

    public Pair<Double, Drawable> getForecast(int i, int requiredWidth, int requiredHeight) {
        if (_weatherResults != null && _weatherResults.size() >= i) {
            Pair<Double, Integer> result = _weatherResults.get(i);
            return new Pair<Double, Drawable>(result.first, getWeatherIcon(result.second, requiredWidth, requiredHeight));
        }

        return null;
    }

    /*
     * Complicated, but width and height could change if the screen is rotated.
     */
    public Drawable getWeatherIcon(int resourceId, int requiredWidth, int requiredHeight) {
        HashMap<Integer, HashMap<Integer, Drawable>> widthMap;
        HashMap<Integer, Drawable> heightMap;

        if (_iconCache.containsKey(resourceId)) {
            widthMap = _iconCache.get(resourceId);
        } else {
            widthMap = new HashMap<>();
            _iconCache.put(resourceId, widthMap);
        }

        if (widthMap.containsKey(requiredWidth)) {
            heightMap = widthMap.get(requiredWidth);
        } else {
            heightMap = new HashMap<>();
            widthMap.put(requiredWidth, heightMap);
        }

        if (heightMap.containsKey(requiredHeight)) {
            return heightMap.get(requiredHeight);
        } else {
            Drawable icon = HomeActivity.Companion.getLauncher().getResources().getDrawable(resourceId);
            icon = IconPackHelper.resizeDrawable(icon, requiredWidth, requiredHeight);

            heightMap.put(requiredHeight, icon);

            return icon;
        }
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
