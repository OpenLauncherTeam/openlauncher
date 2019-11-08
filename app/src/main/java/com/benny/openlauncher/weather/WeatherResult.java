package com.benny.openlauncher.weather;

import android.support.v4.util.Pair;

import java.util.ArrayList;

public class WeatherResult {
    ArrayList<Pair<Double, String>> _weatherResults = new ArrayList();

    public void add(Double temperature, String icon) {
        _weatherResults.add(new Pair(temperature, icon));
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Pair<Double, String> forecast : _weatherResults) {
            builder.append(forecast.first + " - " + forecast.second + ", ");
        }

        return builder.toString();
    }
}
