package com.benny.openlauncher.weather;

import android.location.Location;

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

/*
 * Thanks to https://github.com/tonyallan/weather-au for the basics for this class.
 */

public class BOMWeatherService extends WeatherService {
    private static Logger LOG = LoggerFactory.getLogger("WeatherService");

    private static String API_BASE = "https://api.weather.bom.gov.au/v1/locations";
    private static String API_FORECAST_RAIN = API_BASE + "forecast/rain";
    private static String API_WARNAPI_FORECAST_WARNINGS = API_BASE + "warnings";
    private static String API_FORECAST_DAILY = "/forecasts/daily";
    private static String API_FORECAST_3HOURLY = "/forecasts/3-hourly";
    private static String API_OBSERVATIONS = "/observations";
    private static String API_SEARCH = "?search=";
    private static String ACKNOWLEDGEMENT = "Data courtesy of the Australian Bureau of Meteorology (https://api.weather.bom.gov.au)";

    private String createURL(WeatherLocation location) {
        if (location == null) {
            return "";
        }

        StringBuilder url = new StringBuilder();
        url.append(API_BASE)
                .append("/")
                .append(location.getId());

        if (AppSettings.get().getWeatherForecastByHour()) {
            url.append(API_FORECAST_3HOURLY);
        } else {
            url.append((API_FORECAST_DAILY));
        }

        return url.toString();
    }

    private WeatherResult createWeatherResult(JSONArray results) throws JSONException {
        LOG.debug("createWeatherResult: {}", results);
        WeatherResult currentWeather = new WeatherResult();

        int numberOfIcons = _searchBar._weatherIcons.size();
        if (AppSettings.get().getWeatherForecastByHour()) {
            for (int i = 0; i < numberOfIcons; i++) {
                JSONObject obj = results.getJSONObject(i);
                currentWeather.add(obj.getDouble("temp"), getWeatherIcon(obj.getString("icon_descriptor")));
            }
        } else {
            for (int i = 0; i < numberOfIcons; i++) {
                JSONObject obj = results.getJSONObject(i);
                currentWeather.add(obj.getDouble("temp_max"), getWeatherIcon(obj.getString("icon_descriptor")));
            }
        }

        return currentWeather;
    }

    public void getLocationsByName(String name, Response.Listener<JSONObject> listener, Response.ErrorListener err) {
        // https://api.weather.bom.gov.au/v1/locations?search=3130
        JsonObjectRequest request = new JsonObjectRequest(API_BASE + API_SEARCH + name, null, listener, err);
        addToRequestQueue(request);
    }


    public void getLocationsFromResponse(JSONObject response) {
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

    public String getName() {
        return "au_bom";
    }

    public void getWeatherForLocation(Location location) {
        if (location != null) {
            String postcode = getPostCodeByCoordinates(location);

            if (!"".equals(postcode)) {
                getWeatherForLocation(postcode);
            }
        }
    }

    public void getWeatherForLocation(WeatherLocation location) {
        String url = createURL(location);

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

    public boolean isCountrySupported(String countryCode) {
        if ("AU".equals(countryCode)) {
            return true;
        }

        return false;
    }

    public void openWeatherApp() {
        openWeatherApp("au.gov.bom.metview");
    }

}
