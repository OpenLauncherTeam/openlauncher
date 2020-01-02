package com.benny.openlauncher.weather;

import org.json.JSONObject;

import java.util.Comparator;
import java.util.TreeMap;

public class WeatherLocation {
    public static String JSON_TAG_NAME = "name";
    public static String JSON_TAG_POSTCODE= "postcode";
    public static String JSON_TAG_COUNTRYCODE= "country";
    public static String JSON_TAG_SERVICE_ID = "serviceid";

    private String _name = "";
    private String _postcode = "";
    private String _countryCode = "";
    private String _weatherServiceId = "";

    public static TreeMap<String, WeatherLocation> _locations = new TreeMap<>();

    public WeatherLocation(String name, String postcode, String countryCode, String weatherServiceId) {
        _name = name;
        _postcode = postcode;
        _countryCode = countryCode;
        _weatherServiceId = weatherServiceId;
    }

    public static void clear() {
        _locations.clear();
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
        builder.append(_name)
                .append("|")
                .append(_postcode)
                .append("|")
                .append(_countryCode)
                .append("|")
                .append(_weatherServiceId);

        return builder.toString();
    }

    public static WeatherLocation getByName(String name) {
        return _locations.get(name);
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

        String[] parts = location.split("\\|");
        if (parts.length != 4) {
            WeatherService.LOG.error("Stored Weather City does not conform to expected format: {}", location);
            return null;
        }

        WeatherLocation loc = getByName(parts[0]);

        if (loc == null) {
            loc = new WeatherLocation(parts[0], parts[1], parts[2], parts[3]);

            _locations.put(loc._name, loc);
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
