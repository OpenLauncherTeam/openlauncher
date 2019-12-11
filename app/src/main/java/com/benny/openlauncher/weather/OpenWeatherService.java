package com.benny.openlauncher.weather;

import android.graphics.drawable.Drawable;
import android.location.Location;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.benny.openlauncher.R;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.AppSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/*
 * Thanks to https://github.com/martykan/forecastie for the basics for this class. It's a great app;
 * download it!
 */
public class OpenWeatherService extends WeatherService {
    private static Logger LOG = LoggerFactory.getLogger("WeatherService");

    private static String API_BASE = "https://api.openweathermap.org/data/2.5/";

    private HashMap<String, Drawable> _iconCache = new HashMap<>();

    private String createURL(String search) {
        AppSettings settings = AppSettings.get();
        final String apiKey = "3e29e62e2ddf6dd3d2ebd28aed069215";

        StringBuilder urlBuilder = new StringBuilder(API_BASE);
        urlBuilder.append("forecast?q=").append(search);
        urlBuilder.append("&lang=").append(settings.getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&units=metric");
        urlBuilder.append("&appid=").append(apiKey);

        return urlBuilder.toString();
    }

    private String createURL(Location location) {
        AppSettings settings = AppSettings.get();
        final String apiKey = "3e29e62e2ddf6dd3d2ebd28aed069215";

        StringBuilder urlBuilder = new StringBuilder(API_BASE);
        urlBuilder.append("forecast?lat=").append(location.getLatitude());
        urlBuilder.append("&lon=").append(location.getLongitude());
        urlBuilder.append("&lang=").append(settings.getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&units=metric");
        urlBuilder.append("&appid=").append(apiKey);

        return urlBuilder.toString();
    }

    private String createURL(WeatherLocation location) {
        AppSettings settings = AppSettings.get();
        final String apiKey = "3e29e62e2ddf6dd3d2ebd28aed069215";

        StringBuilder urlBuilder = new StringBuilder(API_BASE);
        urlBuilder.append("forecast?id=").append(location.getId());
        urlBuilder.append("&lang=").append(settings.getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&units=metric");
        urlBuilder.append("&appid=").append(apiKey);

        return urlBuilder.toString();
    }

    private WeatherResult createWeatherResult(JSONArray results) throws JSONException {
        LOG.debug("createWeatherResult: {}", results);
        WeatherResult currentWeather = new WeatherResult();

        boolean hourly = AppSettings.get().getWeatherForecastByHour();
        String day = "";
        int i = 0;
        int numberOfIcons = _searchBar._weatherIcons.size();

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

    /*
    {"city":{"id":1851632,"name":"Shuzenji",
             "coord":{"lon":138.933334,"lat":34.966671},
             "country":"JP",
             "timezone": 32400
             "cod":"200",
             "message":0.0045,
             "cnt":38,
     "list":[{
        "dt":1406106000,
        "main":{
            "temp":298.77,
            "temp_min":298.77,
            "temp_max":298.774,
            "pressure":1005.93,
            "sea_level":1018.18,
            "grnd_level":1005.93,
            "humidity":87,
            "temp_kf":0.26},
        "weather":[{"id":804,"main":"Clouds","description":"overcast clouds","icon":"04d"}],
        "clouds":{"all":88},
        "wind":{"speed":5.71,"deg":229.501},
        "sys":{"pod":"d"},
        "dt_txt":"2014-07-23 09:00:00"}
        ]}
     */
    public void getLocationsFromResponse(JSONObject response) {
        try {
            JSONObject city = response.getJSONObject("city");

            WeatherLocation.clear();
            WeatherLocation wLoc = new WeatherLocation(city.getString("name"), "", city.getString("id"));
            WeatherLocation.put(wLoc);
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

    public boolean isCountrySupported(String countryCode) {
        return true;
    }

    public void openWeatherApp() {
        openWeatherApp("cz.martykan.forecastie");
    }
}
