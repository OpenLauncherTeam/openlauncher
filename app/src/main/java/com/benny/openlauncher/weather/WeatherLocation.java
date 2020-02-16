package com.benny.openlauncher.weather;

import android.text.TextUtils;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;

import org.json.JSONObject;

import java.util.Comparator;
import java.util.TreeMap;

public class WeatherLocation {
    public static String JSON_TAG_NAME = "name";
    public static String JSON_TAG_POSTCODE= "postcode";
    public static String JSON_TAG_COUNTRYCODE= "country";
    public static String JSON_TAG_SERVICE_ID = "serviceid";

    public static WeatherLocation useCurrentLocation = new WeatherLocation(R.string.weather_service_use_network_location);
    public static WeatherLocation findLocation = new WeatherLocation(R.string.weather_service_add_location);

    private String _name = "";
    private String _postcode = "";
    private String _countryCode = "";
    private String _weatherServiceId = "";

    public static TreeMap<String, WeatherLocation> _locations = new TreeMap<>();

    public WeatherLocation(int resId) {
        _name = HomeActivity._launcher.getString(resId);
    }

    public WeatherLocation(String name, String postcode, String countryCode, String weatherServiceId) {
        _name = name;
        _postcode = postcode;
        _countryCode = countryCode;
        _weatherServiceId = weatherServiceId;
    }

    public static void clear() {
        _locations.clear();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof WeatherLocation) {
            WeatherLocation loc = (WeatherLocation) obj;

            result = _name.equals(loc._name);

            if (!TextUtils.isEmpty(_postcode)) {
                result &= _postcode.equals(loc._postcode);
            }

            if (!TextUtils.isEmpty(_weatherServiceId)) {
                result &= _weatherServiceId.equals(loc._weatherServiceId);
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (_name != null ? _name.hashCode() : 0) + (_postcode != null ? _postcode.hashCode() : 0) + (_weatherServiceId != null ? _weatherServiceId.hashCode() : 0);
        return hash;
    }

    public static WeatherLocation fromJson(JSONObject json) {
        try {
            return new WeatherLocation(json.getString(JSON_TAG_NAME), json.getString(JSON_TAG_POSTCODE), json.getString(JSON_TAG_COUNTRYCODE), json.getString(JSON_TAG_SERVICE_ID));
        } catch (Exception e) {
            WeatherService.LOG.error("Invalid json; can't create WeatherLocation: {}", json);
        }

        return null;
    }

    public String getId() {
        return _weatherServiceId;
    }

    public String getName() {
        return _name;
    }

    public String getCountryCode() {
        return _countryCode;
    }

    public String getPostcode() {
        return _postcode;
    }

    public void setId(String weatherServiceId) {
        _weatherServiceId = weatherServiceId;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put(JSON_TAG_NAME, _name);
            json.put(JSON_TAG_POSTCODE, _postcode);
            json.put(JSON_TAG_COUNTRYCODE, _countryCode);
            json.put(JSON_TAG_SERVICE_ID, _weatherServiceId);
        } catch (Exception e) {
            WeatherService.LOG.error("Can't serialise WeatherLocation to Json: {}", this);
        }

        return json;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(_name);
        if (!TextUtils.isEmpty(_postcode)) {
            builder.append("|").append(_postcode);
        }
        if (!TextUtils.isEmpty(_countryCode)) {
            builder.append("|").append(_countryCode);
        }
        if (!TextUtils.isEmpty(_weatherServiceId)) {
            builder.append("|").append(_weatherServiceId);
        }

        return builder.toString();
    }

    public static WeatherLocation getByPostcode(String postcode) {
        for (WeatherLocation loc : _locations.values()) {
            if (loc.getPostcode().equals(postcode)) {
                return loc;
            }
        }

        return null;
    }

    public static WeatherLocation parse(String location) {
        if ("".equals(location)) {
            // Expected behaviour if we are using Location Services.
            return null;
        }

        WeatherLocation loc = WeatherService.getWeatherService().parse(location);

        if (loc != null && _locations.containsKey(loc._name)) {
            _locations.put(loc._name, loc);
        }

        // We don't display WeatherLocations that might be part of another service.
        if (loc == null && location.indexOf("|") == -1) {
            loc = new WeatherLocation(location, "", "", "");
        }

        return loc;
    }

    public static void put(WeatherLocation location) {
        _locations.put(location._name + "-" + location._countryCode, location);
    }

    public static class WeatherLocationComparator implements Comparator<WeatherLocation>
    {
        public int compare(WeatherLocation left, WeatherLocation right) {
            String leftName = left._name + left._countryCode;
            String rightName = right._name + right._countryCode;

            return leftName.compareTo(rightName);
        }
    }
}
