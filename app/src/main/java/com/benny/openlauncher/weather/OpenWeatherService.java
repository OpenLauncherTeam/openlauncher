package com.benny.openlauncher.weather;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

import im.delight.android.location.SimpleLocation;

/*
 * Thanks to https://github.com/martykan/forecastie for the basics for this class. It's a great app;
 * download it!
 */
public class OpenWeatherService implements WeatherService {
    private static Logger LOG = LoggerFactory.getLogger("WeatherService");

    private URL createURL() throws MalformedURLException {
        AppSettings settings = AppSettings.get();
        final String apiKey = "3e29e62e2ddf6dd3d2ebd28aed069215";
        final String city = settings.getWeatherCity();

        StringBuilder urlBuilder = new StringBuilder("https://api.openweathermap.org/data/2.5/");
        urlBuilder.append("forecast?");

        if (city.equals("")) {
            SimpleLocation loc = HomeActivity._location;
            urlBuilder.append("lat=").append(loc.getLatitude()).append("&lon=").append(loc.getLongitude());
        } else {
            /*
            final String cityId = sp.getString("cityId", Constants.DEFAULT_CITY_ID);
            urlBuilder.append("id=").append(URLEncoder.encode(cityId, "UTF-8"));*/
            urlBuilder.append("q=").append(city);
        }

        urlBuilder.append("&lang=").append(settings.getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&units=metric");
        urlBuilder.append("&appid=").append(apiKey);

        return new URL(urlBuilder.toString());
    }

    private WeatherResult createWeatherResult(JSONArray results) throws JSONException {
        LOG.debug("createWeatherResult: {}", results);
        WeatherResult currentWeather = new WeatherResult();

        for (int i = 0; i < 3; i++) {
            JSONObject listItem = results.getJSONObject(i);
            JSONObject main = listItem.getJSONObject("main");
            JSONObject weather = listItem.getJSONArray("weather").getJSONObject(0);
            currentWeather.add(main.getDouble("temp"), weather.getString("icon"));
        }

        return currentWeather;
    }

    /*
     * This should be called in a Background Thread.
     */
    public WeatherResult getWeatherForLocation() {
        try {
            final URL url = createURL();
            LOG.debug("URL: {}", url);

            final String response = WeatherService.getDataFromWeatherService(url);
            JSONObject obj = new JSONObject(response);

            return createWeatherResult(obj.getJSONArray("list"));

        } catch (Exception e) {
            LOG.error("Exception calling OpenWeatherService: {}", e);
        }

        return null;
    }
}
