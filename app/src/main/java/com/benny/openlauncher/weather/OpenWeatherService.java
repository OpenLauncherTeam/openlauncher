package com.benny.openlauncher.weather;

import android.graphics.drawable.Drawable;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.TreeMap;

/*
 * Thanks to https://github.com/martykan/forecastie for the basics for this class. It's a great app;
 * download it!
 */
public class OpenWeatherService extends WeatherService {
    private static Logger LOG = LoggerFactory.getLogger("WeatherService");

    private static String API_BASE = "https://api.openweathermap.org/data/2.5/";

    private HashMap<String, Drawable> _iconCache = new HashMap<>();

    private String createURL() {
        AppSettings settings = AppSettings.get();
        final String apiKey = "3e29e62e2ddf6dd3d2ebd28aed069215";
        final String postcode = "3000";

        StringBuilder urlBuilder = new StringBuilder(API_BASE);
        urlBuilder.append("forecast?q=").append(postcode);
        urlBuilder.append("&lang=").append(settings.getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&units=metric");
        urlBuilder.append("&appid=").append(apiKey);

        return urlBuilder.toString();
    }

    private WeatherResult createWeatherResult(JSONArray results) throws JSONException {
        LOG.debug("createWeatherResult: {}", results);
        WeatherResult currentWeather = new WeatherResult();

        for (int i = 0; i < 3; i++) {
            JSONObject listItem = results.getJSONObject(i);
            JSONObject main = listItem.getJSONObject("main");
            JSONObject weather = listItem.getJSONArray("weather").getJSONObject(0);
            currentWeather.add(main.getDouble("temp"), getWeatherIcon(weather.getString("icon")));
        }

        return currentWeather;
    }

    public void getLocationsByName(String name) {
        JsonObjectRequest request = new JsonObjectRequest(createURL(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray suburbs = response.getJSONArray("data");

                    WeatherLocation.clear();
                    for (int i = 0; i < suburbs.length(); i++) {
                        JSONObject suburb = suburbs.getJSONObject(i);
                        WeatherLocation wLoc = new WeatherLocation(
                                suburb.getString("name"),
                                suburb.getString("postcode"),
                                suburb.getString("geohash").substring(0, 6));

                        WeatherLocation.put(wLoc);
                    }
                } catch (JSONException e) {
                    LOG.error("Failed to get locations from the BOM Weather Service: {}", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LOG.error("Failed to get locations from the BOM Weather Service: {}", error);
            }
        });
        addToRequestQueue(request);
    }

    public void getLocationsFromResponse(JSONObject response) {
    }

    public void getLocationsByName(String name, Response.Listener<JSONObject> listener, Response.ErrorListener err) {
    }

    public String getName() {
        return "openweather";
    }

    /*
     * This should be called in a Background Thread.
     */
    public void getWeatherForLocation(WeatherLocation location) {
        String url = createURL();

        LOG.debug("getWeatherForLocation: {}", url);
        JsonObjectRequest request = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    WeatherResult result = createWeatherResult(response.getJSONArray("data"));
                    _searchBar.updateWeather(result);
                } catch (JSONException e) {
                    LOG.error("Exception calling BOM WeatherService: {}", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LOG.error("Error on HTTP request to the BOM Weather Service: {}", error);
            }
        });

        addToRequestQueue(request);
    }

    public Integer getWeatherIcon(String icon) {
        if (icon.equals("01d") || icon.equals("01n")) {
            return R.drawable.sunny;
        }

        if (icon.equals("02d") || icon.equals("02n") || icon.equals("04d") || icon.equals("04n")) {
            return R.drawable.cloudy;
        }

        if (icon.equals("03d") || icon.equals("03n")) {
            return R.drawable.clouds;
        }

        if (icon.equals("50d") || icon.equals("50n")) {
            return R.drawable.hazy;
        }

        if (icon.equals("10d") || icon.equals("10n")) {
            return R.drawable.rain;
        }

        if (icon.equals("13d") || icon.equals("13n")) {
            return R.drawable.snowflake;
        }

        if (icon.equals("11d") || icon.equals("11n")) {
            return R.drawable.storm;
        }

        LOG.error("Can't parse weather condition: {}", icon);
        return -1;
    }


    public void openWeatherApp() {
        openWeatherApp("cz.martykan.forecastie");
    }
}
