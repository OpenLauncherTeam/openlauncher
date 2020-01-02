package com.benny.openlauncher.weather;

import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.IconPackHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class WeatherResult {
    private HashMap<Integer, HashMap<Integer, Drawable>> _iconCache = new HashMap<>();

    public ArrayList<Pair<Double, Integer>> _weatherResults = new ArrayList();

    public void add(Double temperature, int resourceId) {
        _weatherResults.add(new Pair(round(temperature, 1), resourceId));
    }

    public Pair<Double, Drawable> getForecast(int i, int size) {
        if (_weatherResults != null && _weatherResults.size() > i) {
            Pair<Double, Integer> result = _weatherResults.get(i);
            return new Pair<>(result.first, getWeatherIcon(result.second, size));
        }

        return null;
    }

    /*
     * Complicated, but width and height could change if the screen is rotated.
     */
    public Drawable getWeatherIcon(int resourceId, int size) {
        HashMap<Integer, Drawable> heightMap;

        if (_iconCache.containsKey(resourceId)) {
            heightMap = _iconCache.get(resourceId);
        } else {
            heightMap = new HashMap<>();
            _iconCache.put(resourceId, heightMap);
        }

        if (heightMap.containsKey(size)) {
            return heightMap.get(size);
        } else {
            try {
                Drawable icon = HomeActivity.Companion.getLauncher().getResources().getDrawable(resourceId);
                icon = IconPackHelper.resizeDrawable(icon, size, size);

                heightMap.put(size, icon);

                return icon;
            } catch (Exception e) {
                WeatherService.LOG.error("Exception when loading Drawable from Resource: {}", e);
            }

            return null;
        }
    }

    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public Integer size() {
        return _weatherResults.size();
    }
}
