package com.benny.openlauncher.weather;

import android.graphics.drawable.Drawable;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.IconPackHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

import im.delight.android.location.SimpleLocation;

/*
 * Thanks to https://github.com/tonyallan/weather-au for the basics for this class.
 */

public class BOMWeatherService implements WeatherService {
    private static Logger LOG = LoggerFactory.getLogger("WeatherService");

    private static String API_BASE = "https://api.weather.bom.gov.au/v1/locations";
    private static String API_FORECAST_RAIN = API_BASE + "forecast/rain";
    private static String API_WARNAPI_FORECAST_WARNINGS = API_BASE + "warnings";
    private static String API_FORECAST_DAILY = "/forecasts/daily";
    private static String API_FORECAST_3HOURLY = "/forecasts/3-hourly";
    private static String API_OBSERVATIONS = "/observations";
    private static String API_SEARCH = "?search=";
    private static String ACKNOWLEDGEMENT = "Data courtesy of the Australian Bureau of Meteorology (https://api.weather.bom.gov.au)";

    private URL createURL() throws IOException, JSONException {
        AppSettings settings = AppSettings.get();

        SimpleLocation loc = HomeActivity._location;
        String postcode = settings.getWeatherCity();
        if (postcode.equals("")) {
            postcode = WeatherService.getPostCodeByCoordinates(loc);
        }

        String geohash = getGeoHash(postcode);

        // https://api.weather.bom.gov.au/v1/locations/r1r143/forecasts/3-hourly
        StringBuilder url = new StringBuilder();
        url.append(API_BASE).append("/").append(geohash);

        if (settings.getWeatherForecastByHour()) {
            url.append(API_FORECAST_3HOURLY);
        } else {
            url.append((API_FORECAST_DAILY));
        }

        return new URL(url.toString());
    }

    private WeatherResult createWeatherResult(JSONArray results) throws JSONException {
        LOG.debug("createWeatherResult: {}", results);
        WeatherResult currentWeather = new WeatherResult();

        if (AppSettings.get().getWeatherForecastByHour()) {
            for (int i = 0; i < 3; i++) {
                JSONObject obj = results.getJSONObject(i);
                currentWeather.add(obj.getDouble("temp"), getWeatherIcon(obj.getString("icon_descriptor")));
            }
        } else {
            for (int i = 0; i < 3; i++) {
                JSONObject obj = results.getJSONObject(i);
                currentWeather.add(obj.getDouble("temp_max"), getWeatherIcon(obj.getString("icon_descriptor")));
            }
        }

        return currentWeather;
    }


    public String getGeoHash(String postcode) throws IOException, JSONException {
        // https://api.weather.bom.gov.au/v1/locations?search=3130
        URL url = new URL(API_BASE + API_SEARCH + postcode);

        String response = WeatherService.getDataFromWeatherService(url);
        // data:[{geohash, id, name, postcode, state},]

        JSONObject json = new JSONObject(response);
        JSONArray suburbs = json.getJSONArray("data");

        LOG.debug("JSONarray: {}", suburbs);

        return suburbs.getJSONObject(0).getString("geohash").substring(0,6);
    }

    public String getName() {
        return "au_bom";
    }

    public WeatherResult getWeatherForLocation() {
        try {
            final URL url = createURL();
            LOG.debug("getWeatherForLocation: {}", url);

            final String response = WeatherService.getDataFromWeatherService(url);
            JSONObject obj = new JSONObject(response);

            return createWeatherResult(obj.getJSONArray("data"));

        } catch (Exception e) {
            LOG.error("Exception calling BOM WeatherService: {}", e);
        }

        return null;
    }

    public int getWeatherIcon(String icon) {
        if (icon.equals("sunny") || icon.equals("clear")) {
            return R.drawable.sunny;
        }

        if (icon.equals("mostly_sunny") || icon.equals("party_cloudy")) {
            return R.drawable.cloudy;
        }

        if (icon.equals("cloudy")) {
            return R.drawable.clouds;
        }

        if (icon.equals("hazy") || icon.equals("fog")) {
            return R.drawable.hazy;
        }

        if (icon.equals("light_rain") || icon.equals("rain") || icon.equals("shower") || icon.equals("light_shower") || icon.equals("heavy_shower")) {
            return R.drawable.rain;
        }

        if (icon.equals("frost") || icon.equals("snow")) {
            return R.drawable.snowflake;
        }

        if (icon.equals("storm")) {
            return R.drawable.storm;
        }

        if (icon.equals("windy")) {
            return R.drawable.wind;
        }

        LOG.error("Can't parse weather condition: {}", icon);
        return -1;
    }

}
