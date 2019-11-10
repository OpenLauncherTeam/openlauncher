package com.benny.openlauncher.weather;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import im.delight.android.location.SimpleLocation;

public interface WeatherService {
    public static Logger LOG = LoggerFactory.getLogger("WeatherService");

    public String getName();

    public WeatherResult getWeatherForLocation();

    public static String getPostCodeByCoordinates(SimpleLocation location) throws IOException {
        Geocoder mGeocoder = new Geocoder(HomeActivity._launcher, Locale.getDefault());
        if (mGeocoder != null) {
            List<Address> addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);

            if (addresses != null) {
                for (Address address : addresses) {
                    if (address.getPostalCode() != null) {
                        return address.getPostalCode();
                    }
                }
            }
        }

        return null;
    }

    public static String getDataFromWeatherService(URL url) throws IOException {
        String response = "";

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        if (urlConnection.getResponseCode() == 200) {
            InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            response += stringBuilder.toString();

            r.close();
            urlConnection.disconnect();

        } else if (urlConnection.getResponseCode() == 429) {
            // Too many requests
            LOG.error("Too many requests to the WeatherService");
            return null;
        } else {
            // Bad response from server
            LOG.error("Bad response from WeatherService: {} -> {}", urlConnection.getResponseCode(), urlConnection.getResponseMessage());
            return null;
        }

        return response;
    }

    public static Drawable getDrawableFromWeatherService(URL url) throws IOException {
        Drawable response = null;

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        if (urlConnection.getResponseCode() == 200) {
            response = Drawable.createFromStream(urlConnection.getInputStream(), url.toString());

            urlConnection.disconnect();

        } else if (urlConnection.getResponseCode() == 429) {
            // Too many requests
            LOG.error("Too many requests to the WeatherService");
            return null;
        } else {
            // Bad response from server
            LOG.error("Bad response from WeatherService: {} -> {}", urlConnection.getResponseCode(), urlConnection.getResponseMessage());
            return null;
        }

        return response;
    }
}
