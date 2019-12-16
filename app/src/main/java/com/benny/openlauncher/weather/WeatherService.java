package com.benny.openlauncher.weather;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.widget.SearchBar;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

public abstract class WeatherService {
    public static Logger LOG = LoggerFactory.getLogger("WeatherService");

    // Location Services, iff required.
    private LocationManager _locationManager = null;

    // Volley interactions.
    protected RequestQueue _requestQueue;

    // Main UI element we interact with.
    protected SearchBar _searchBar;

    // Functions for each Weather Service to implement
    public abstract String getName();
    public abstract void getLocationsByName(String name, Response.Listener<JSONObject> listener, Response.ErrorListener err);
    public abstract void getLocationsFromResponse(JSONObject response);
    public abstract void getWeatherForLocation(Location location);
    public abstract void getWeatherForLocation(WeatherLocation location);
    public abstract boolean isCountrySupported(String countryCode);
    public abstract void openWeatherApp();

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void getWeatherForLocation() {
        WeatherLocation loc = AppSettings.get().getWeatherCity();

        if (loc != null) {
            getWeatherForLocation(loc);
        } else {
            try {
                if (_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    Location currentLocation = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    getWeatherForLocation(currentLocation);
                }
            } catch (SecurityException e) {
                LOG.error("User has not allowed Location Services: {}", e);
            }
        }
    }

    public void getWeatherForLocation(String postcode) {
        WeatherLocation loc = WeatherLocation.getByPostcode(postcode);
        if (loc != null) {
            getWeatherForLocation(loc);
        } else {
            // Now chain the Location search and the Weather retrieval together.
            getLocationsByName(postcode, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    getLocationsFromResponse(response);
                    getWeatherForLocation(WeatherLocation._locations.firstEntry().getValue());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    LOG.error("Failed to get locations from the BOM Weather Service: {}", error);
                }
            });
        }
    }

    public String getPostCodeByCoordinates(Location loc) {
        LOG.debug("getPostCodeByCoordinates() -> {}", loc);
        try {
            Geocoder geocoder = new Geocoder(HomeActivity.Companion.getLauncher(), Locale.getDefault());
            if (geocoder != null) {
                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 5);

                LOG.debug("looking for postcode from GPS coordinates: {}, {} -> {}", loc.getLatitude(), loc.getLongitude(), addresses);
                if (addresses != null) {
                    for (Address address : addresses) {
                        if (!isCountrySupported(address.getCountryCode())) {
                            break;
                        }

                        if (address.getPostalCode() != null) {
                            return address.getPostalCode();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Can't find postcode from Geocoder: {}", e);
        }

        return "";
    }

    public RequestQueue getRequestQueue() {
        if (_requestQueue == null) {
            _requestQueue = Volley.newRequestQueue(HomeActivity.Companion.getLauncher());
        }
        return _requestQueue;
    }

    public void openWeatherApp(String packageName) {
        Intent intent = _searchBar.getContext().getPackageManager().getLaunchIntentForPackage(packageName);

        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _searchBar.getContext().startActivity(intent);
    }

    public void updateWeather(SearchBar searchBar) {
        this._searchBar = searchBar;
        // We only ask for a Location if we haven't specified a specific locality.
        if (AppSettings.get().getWeatherCity() == null) {
            HomeActivity activity = HomeActivity.Companion.getLauncher();
            if (_locationManager == null) {
                // Weather Service initialisation, if required. Coarse location only.

                if (activity.checkLocationPermissions()) {
                    try {
                        _locationManager = (LocationManager) HomeActivity._launcher.getSystemService(Context.LOCATION_SERVICE);

                        // Define a listener that responds to location updates
                        LocationListener locationListener = new LocationListener() {
                            public void onLocationChanged(Location location) {
                                // Called when a new location is found by the network location provider.
                                LOG.debug("onLocationChanged() received: {}, {}", location.getLatitude(), location.getLongitude());
                                getWeatherForLocation(location);
                            }

                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }

                            public void onProviderEnabled(String provider) {
                            }

                            public void onProviderDisabled(String provider) {
                            }
                        };

                        // Register the listener with the Location Manager to receive location updates, 1 hour or 25 klms apart.
                        _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60*60*1000l, 25*1000l, locationListener);
                    } catch (SecurityException e) {
                        LOG.error("Can't turn on Location Services: {}", e);
                    }
                }
            } else {
                getWeatherForLocation();
            }
        } else {
            getWeatherForLocation();
        }
    }
}
