package com.benny.openlauncher.weather;

import android.graphics.drawable.Drawable;
import android.location.Location;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/*
 * Thanks to https://github.com/martykan/forecastie for the basics for this class. It's a great app;
 * download it!
 */
public class OpenWeatherService extends WeatherService {
    private static String API_BASE = "https://api.openweathermap.org/data/2.5/";

    private HashMap<String, Drawable> _iconCache = new HashMap<>();

    private void appendURLDetails(StringBuilder urlBuilder) {
        AppSettings settings = AppSettings.get();

        String units = settings.isMetricUnit() ? "metric" : "imperial";

        urlBuilder.append("&lang=").append(settings.getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&units=").append(units);
        urlBuilder.append("&appid=").append(settings.getWeatherAPIKey());
    }

    private String createURL(String search) {
        StringBuilder urlBuilder = new StringBuilder(API_BASE);
        urlBuilder.append("find?q=").append(search);
        appendURLDetails(urlBuilder);

        return urlBuilder.toString();
    }

    private String createURL(Location location) {
        StringBuilder urlBuilder = new StringBuilder(API_BASE);
        urlBuilder.append("forecast?lat=").append(location.getLatitude());
        urlBuilder.append("&lon=").append(location.getLongitude());
        appendURLDetails(urlBuilder);

        return urlBuilder.toString();
    }

    private String createURL(WeatherLocation location) {
        StringBuilder urlBuilder = new StringBuilder(API_BASE);
        urlBuilder.append("forecast?id=").append(location.getId());
        appendURLDetails(urlBuilder);

        return urlBuilder.toString();
    }

    private WeatherResult createWeatherResult(JSONArray results) throws JSONException {
        LOG.debug("createWeatherResult: {}", results);
        WeatherResult currentWeather = new WeatherResult();

        boolean hourly = AppSettings.get().getWeatherForecastByHour();
        String day = "";
        int i = 0;
        int numberOfIcons = _weatherIcons.size();

        while (currentWeather.size() < numberOfIcons) {
            JSONObject listItem = results.getJSONObject(i);
            JSONObject main = listItem.getJSONObject("main");
            JSONObject weather = listItem.getJSONArray("weather").getJSONObject(0);
            String datetime = listItem.getString("dt_txt");

            LOG.debug("createWeatherResult for {}, {} -> {}", i, day, listItem.getString("dt_txt"));

            if (hourly) {
                currentWeather.add(main.getDouble("temp"), getWeatherIcon(weather.getString("icon")));

                i++;
                continue;
            } else {
                if (i == 0) {
                    currentWeather.add(main.getDouble("temp"), getWeatherIcon(weather.getString("icon")));
                    day = datetime.substring(0, 10);
                } else {
                    String currentDay = datetime.substring(0, 10);
                    if (datetime.endsWith("12:00:00") && !day.equals(currentDay)) {
                        currentWeather.add(main.getDouble("temp"), getWeatherIcon(weather.getString("icon")));
                        day = currentDay;
                    }
                }

                i++;
                continue;
            }
        }

        return currentWeather;
    }

    public int getIntervalResourceId() {
        if (AppSettings.get().getWeatherForecastByHour()) {
            return R.string.weather_service_3_hours;
        } else {
            return R.string.weather_service_1_day;
        }
    }

    public void getLocationsFromResponse(JSONObject response) {
        try {
            WeatherLocation.clear();

            JSONArray locations = response.getJSONArray("list");
            for (int i = 0; i < locations.length(); i++) {
                JSONObject city = locations.getJSONObject(i);

                WeatherLocation wLoc = new WeatherLocation(
                        city.getString("name"),
                        "",
                        city.getJSONObject("sys").getString("country"),
                        city.getString("id"));
                WeatherLocation.put(wLoc);
            }
        } catch (JSONException e) {
            LOG.error("Failed to get locations from the OpenWeather Service: {}", e);
        }
    }

    public void getLocationsByName(String name, Response.Listener<JSONObject> listener, Response.ErrorListener err) {
        JsonObjectRequest request = new JsonObjectRequest(createURL(name), null, listener, err);
        addToRequestQueue(request);
    }

    public String getName() {
        return "openweather";
    }


    public void getWeatherForLocation(Location location) {
        String url = createURL(location);

        getWeatherForUrl(url);
    }

    public void getWeatherForLocation(WeatherLocation location) {
        String url = createURL(location);

        getWeatherForUrl(url);
    }

    public void getWeatherForUrl(String url) {
        LOG.debug("getWeatherForLocation: {}", url);
        JsonObjectRequest request = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    WeatherResult result = createWeatherResult(response.getJSONArray("list"));
                    updateWeather(result);
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
            return R.drawable.weather_sunny;
        }

        if (icon.equals("02d") || icon.equals("02n") || icon.equals("04d") || icon.equals("04n")) {
            return R.drawable.weather_cloudy;
        }

        if (icon.equals("03d") || icon.equals("03n")) {
            return R.drawable.weather_clouds;
        }

        if (icon.equals("50d") || icon.equals("50n")) {
            return R.drawable.weather_hazy;
        }

        if (icon.equals("10d") || icon.equals("10n")) {
            return R.drawable.weather_rain;
        }

        if (icon.equals("13d") || icon.equals("13n")) {
            return R.drawable.weather_snowflake;
        }

        if (icon.equals("11d") || icon.equals("11n")) {
            return R.drawable.weather_storm;
        }

        LOG.error("Can't parse weather condition: {}", icon);
        return -1;
    }

    public boolean isCountrySupported(String countryCode) {
        return true;
    }

    public void openWeatherApp() {
        openWeatherApp("cz.martykan.forecastie");
    }

    public WeatherLocation parse(String location) {
        String[] parts = location.split("\\|");

        if (parts.length == 3) {
            return new WeatherLocation(parts[0], "", parts[1], parts[2]);
        }

        WeatherService.LOG.error("Stored Weather City does not conform to expected format: {}", location);
        return null;
    }
}
