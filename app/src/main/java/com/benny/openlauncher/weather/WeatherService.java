package com.benny.openlauncher.weather;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.widget.SearchBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public abstract class WeatherService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    public static Logger LOG = LoggerFactory.getLogger("WeatherService");

    // Location Services, iff required.
    private GoogleApiClient _location = null;
    private LocationRequest _locationRequest;

    // Volley interactions.
    protected RequestQueue _requestQueue;

    // Main UI element we interact with.
    protected SearchBar _searchBar;

    // Functions for each Weather Service to implement
    public abstract String getName();
    public abstract void getLocationsByName(String name, Response.Listener<JSONObject> listener, Response.ErrorListener err);
    public abstract void getLocationsFromResponse(JSONObject response);
    public abstract void getWeatherForLocation(WeatherLocation location);
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
                Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(_location);
                if (currentLocation != null) {
                    String postcode = getPostCodeByCoordinates(currentLocation);

                    if (!"".equals(postcode)) {
                        getWeatherForLocation(postcode);
                    }
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

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        LOG.debug("onConnected: {}", arg0);
        startLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        LOG.error("Location Services connection failed: {}", result);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        _location.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        String postcode = getPostCodeByCoordinates(location);
        LOG.debug("Location changed: {}", postcode);

        if (!"".equals(postcode)) {
            getWeatherForLocation(postcode);
        }
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

    protected void startLocationUpdates() {
        LOG.debug("startLocationUpdates()");
        try {
            // Create the location request
            _locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                    .setInterval(10000)
                    .setFastestInterval(5000);

            LocationServices.FusedLocationApi.requestLocationUpdates(_location, _locationRequest, this);
        } catch (SecurityException e) {
            LOG.error("User has not allowed Location Services: {}", e);
        }
    }

    public void updateWeather(SearchBar searchBar) {
        this._searchBar = searchBar;
        // We only ask for a Location if we haven't specified a specific locality.
        if (AppSettings.get().getWeatherCity() == null) {
            HomeActivity activity = HomeActivity.Companion.getLauncher();
            if (_location == null) {
                // Weather Service initialisation, if required. Coarse location only.

                if (activity.checkLocationPermissions()) {
                    _location = new GoogleApiClient.Builder(_searchBar.getContext())
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API).build();
                    _location.connect();
                }
            }
        }

        getWeatherForLocation();
    }
}
