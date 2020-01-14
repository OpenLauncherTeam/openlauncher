package com.benny.openlauncher.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.SearchBar;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public abstract class WeatherService implements LocationListener {
    public static Logger LOG = LoggerFactory.getLogger("WeatherService");

    // Weather Service singleton
    public static WeatherService _weatherService = null;

    // Location Services, iff required.
    private LocationManager _locationManager = null;

    // Volley interactions.
    protected RequestQueue _requestQueue;

    // Weather UI elements
    public ArrayList<AppCompatButton> _weatherIcons = new ArrayList<>();
    protected Integer _weatherIconSize;

    // Cache the constructed Drawable.
    protected HashMap<Integer, Drawable> timingIntervals = new HashMap<>();

    protected WeatherResult _latestResult = null;
    protected long _nextQueryTime = 0l;

    // Weather AutoComplete support
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private WeatherLocationAdapter _locationAdapter;
    private Handler handler;

    // Functions for each Weather Service to implement
    public abstract String getName();
    public abstract int getIntervalResourceId();
    public abstract void getLocationsByName(String name, Response.Listener<JSONObject> listener, Response.ErrorListener err);
    public abstract void getLocationsFromResponse(JSONObject response);
    public abstract void getWeatherForLocation(Location location);
    public abstract void getWeatherForLocation(WeatherLocation location);
    public abstract boolean isCountrySupported(String countryCode);
    public abstract void openWeatherApp();

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public Drawable getIntervalDrawable(int size) {
        int resourceId = getIntervalResourceId();

        Drawable intervalDrawable = timingIntervals.get(resourceId);

        if (intervalDrawable == null) {
            String timing = HomeActivity._launcher.getResources().getString(resourceId);

            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(AppSettings.get().getDesktopDateTextColor());

            float height = size * .4f;
            textPaint.setTextSize((int) (height * .7));
            float width = textPaint.measureText(timing);

            Canvas icon = new Canvas();
            Bitmap iconBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);

            icon.setBitmap(iconBitmap);
            icon.drawText(timing, 0, height / 2 - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);

            intervalDrawable = new BitmapDrawable(HomeActivity._launcher.getResources(), iconBitmap);
            timingIntervals.put(resourceId, intervalDrawable);
        }

        return intervalDrawable;
    }

    public void getWeatherForLocation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > _nextQueryTime || _latestResult == null) {
            // Only query once per hour.
            _nextQueryTime = currentTime + (60 * 60 * 1000l);

            if (AppSettings.get().isLocationServicesSet()) {
                try {
                    if (_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        Location currentLocation = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        getWeatherForLocation(currentLocation);
                    }
                } catch (SecurityException e) {
                    LOG.error("User has not allowed Location Services: {}", e);
                }
            } else {
                WeatherLocation loc = AppSettings.get().getWeatherCity();
                if (loc != null) {
                    getWeatherForLocation(loc);
                } else {
                    findCityToAdd();
                }
            }
        }
        else {
            // Sometimes we need to redisplay the icons on Resume.
            updateWeather(_latestResult);
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

    public void openWeatherApp(String packageName) {
        Intent intent = HomeActivity._launcher.getPackageManager().getLaunchIntentForPackage(packageName);

        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        HomeActivity._launcher.startActivity(intent);
    }

    public void resetQueryTime() {
        _nextQueryTime = 0l;
    }

    public void updateWeather() {
        // Should only need to do this once.
        if (_weatherIcons.size() == 0) {
            HomeActivity._launcher.getSearchBar().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    HomeActivity._launcher.getSearchBar().getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    createWeatherButtons();
                }
            });
        }

        // We only ask for a Location if we haven't specified a specific locality.
        if (AppSettings.get().isLocationServicesSet()) {
            HomeActivity activity = HomeActivity.Companion.getLauncher();
            if (_locationManager == null) {
                // Weather Service initialisation, if required. Coarse location only.

                if (activity.checkLocationPermissions()) {
                    try {
                        LOG.debug("Creating LocationManager to track current location");
                        _locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

                        // Register the listener with the Location Manager to receive location updates, 1 hour or 25 klms apart.
                        _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60 * 60 * 1000l, 25 * 1000l, this);
                    } catch (SecurityException e) {
                        LOG.error("Can't turn on Location Services: {}", e);
                    }
                }
            }
        } else if (AppSettings.get().getWeatherCity() == null) {
            findCityToAdd();
            return;
        }
        getWeatherForLocation();
    }

    @SuppressLint("RestrictedApi")
    private void findCityToAdd() {
        HomeActivity launcher = HomeActivity.Companion.getLauncher();
        final AlertDialog.Builder alert = new AlertDialog.Builder(launcher);
        alert.setTitle(launcher.getString(R.string.weather_service_add_location));

        _locationAdapter = new WeatherLocationAdapter(launcher, android.R.layout.simple_dropdown_item_1line);

        final AppCompatAutoCompleteTextView input = new AppCompatAutoCompleteTextView(launcher);
        input.setMaxLines(1);
        input.setThreshold(3);
        input.setSingleLine(true);
        input.setAdapter(_locationAdapter);

        input.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        WeatherLocation loc = _locationAdapter.getObject(position);
                        input.setText(loc.toString());
                    }
                });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(input.getText())) {
                        _weatherService.getLocationsByName(input.getText().toString(),
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        _weatherService.getLocationsFromResponse(response);
                                        _locationAdapter.setData(new ArrayList<>(WeatherLocation._locations.values()));
                                        _locationAdapter.notifyDataSetChanged();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        LOG.error("Error getting Location list from Weather Service: {}", error.networkResponse);
                                    }
                                });
                    }
                }
                return false;
            }
        });

        alert.setView(input, 32, 0, 32, 0);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppSettings settings = AppSettings.get();

                WeatherLocation loc = WeatherLocation.parse(input.getText().toString());
                settings.setWeatherCity(loc);
                settings.addWeatherLocations(loc);

                int toastMessageId = settings.getWeatherForecastByHour() ? R.string.weather_service_hourly : R.string.weather_service_daily;
                Tool.toast(launcher, String.format(launcher.getString(toastMessageId), loc.getName()));

                getWeatherForLocation();
                dialog.dismiss();
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            // Use Location Services.
            AppSettings.get().setUseLocationServices(true);
            HomeActivity._launcher.initWeatherIfRequired();
        } else if (item.getItemId() == 99) {
            // Pop up the add Location Dialog.
            findCityToAdd();
            return true;
        } else {
            AppSettings.get().setWeatherCity(WeatherLocation.parse(item.toString()));
        }

        displayWeatherChangedToast();
        return true;

    }

    protected void createWeatherButtons() {
        SearchBar searchBar = HomeActivity._launcher.getSearchBar();

        int[] coords = new int[2];
        searchBar._searchButton.getLocationOnScreen(coords);

        int leftPosition = searchBar._searchClock.getMeasuredWidth();
        int rightPosition = coords[0] - searchBar._iconMarginOutside;

        int totalWidth = rightPosition - leftPosition;

        _weatherIconSize = (int) (searchBar._searchClock.getMeasuredHeight() * 0.5f);

        int numberOfIcons = totalWidth / (_weatherIconSize + searchBar._iconMarginOutside);
        for (int i = 0; i < numberOfIcons; i++) {
            AppCompatButton btn = new AppCompatButton(searchBar.getContext());
            _weatherIcons.add(btn);

            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            iconParams.setMargins(leftPosition, 0, 0, 0);

            btn.setLayoutParams(iconParams);
            leftPosition += _weatherIconSize + searchBar._iconMarginOutside;
        }


        setButtonCallbacks();
    }

    protected void setButtonCallbacks() {
        for (AppCompatButton icon : _weatherIcons) {
            HomeActivity.Companion.getLauncher().registerForContextMenu(icon);

            icon.setBackgroundColor(Color.TRANSPARENT);
            icon.setTextColor(AppSettings.get().getDesktopDateTextColor());

            icon.setOnClickListener(new MultiClickListener() {
                @Override
                public void onSingleClick(View v) {
                    super.onSingleClick(v);
                    AppSettings settings = AppSettings.get();
                    settings.setWeatherForecastByHour(!settings.getWeatherForecastByHour());

                    displayWeatherChangedToast();
                }

                public void onDoubleClick(View v) {
                    super.onDoubleClick(v);
                    _weatherService.openWeatherApp();
                }
            });

            icon.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.setHeaderTitle(HomeActivity._launcher.getString(R.string.weather_service_choose_location));
                    //MenuCompat.setGroupDividerEnabled(menu, true);

                    menu.add(0, 0, 0, R.string.weather_service_use_network_location);

                    int i = 1;
                    // Stored Locations first
                    ArrayList<WeatherLocation> storedLocations = AppSettings.get().getWeatherLocations();
                    for (WeatherLocation loc : storedLocations) {
                        menu.add(1, i, i, loc.toString());
                        i++;
                    }

                    // Then locations associated with the current location (when using Location Services)
                    if (AppSettings.get().isLocationServicesSet()) {
                        for (String name : WeatherLocation._locations.keySet()) {
                            menu.add(2, i, i, WeatherLocation.getByName(name).getName());
                            i++;
                        }
                    }

                    menu.add(3, 99, 99, R.string.weather_service_add_location);
                }
            });
        }
    }

    public void updateWeather(WeatherResult weather) {
        LOG.debug("updateWeather() -> {}", weather);
        _latestResult = weather;

        Drawable interval = null;
        for (int i = 0; i < _weatherIcons.size(); i++) {
            Pair<Double, Drawable> forecast = weather.getForecast(i, _weatherIconSize);

            // May happen if we get an error from the Weather Service
            if (forecast == null) {
                break;
            }

            if (i == 0) {
                interval = _weatherService.getIntervalDrawable(_weatherIconSize);
            } else {
                interval = null;
            }

            AppCompatButton icon = _weatherIcons.get(i);
            icon.setCompoundDrawablesWithIntrinsicBounds(interval, forecast.second, null, null);
            icon.setText(Double.toString(forecast.first));

            // This is weird, when returning from some full screen apps, the SearchBar gets recreated;
            // this works around this occurence.
            if (HomeActivity._launcher.getSearchBar().indexOfChild(icon) == -1) {
                if (icon.getParent() != null) {
                    ((ViewGroup) icon.getParent()).removeView(icon);
                }
                HomeActivity._launcher.getSearchBar().addView(icon);
            }
        }
    }

    protected void displayWeatherChangedToast() {
        AppSettings settings = AppSettings.get();

        int toastMessageId = settings.getWeatherForecastByHour() ? R.string.weather_service_hourly : R.string.weather_service_daily;

        try {
            String locationName = "";

            if (AppSettings.get().isLocationServicesSet()) {
                locationName = HomeActivity._launcher.getString(R.string.weather_service_current_location);
            } else {
                WeatherLocation loc = settings.getWeatherCity();

                if (loc != null) {
                    locationName = loc.getName();
                }
            }
            Tool.toast(HomeActivity._launcher, String.format(HomeActivity._launcher.getString(toastMessageId), locationName));
        } catch (Exception e) {
            LOG.error("Failed to get location: {}", e);
        }

        _weatherService.resetQueryTime();
        HomeActivity.Companion.getLauncher().initWeatherIfRequired();
    }

    public void toggleWeatherButtons(boolean visible) {
        for (AppCompatButton icon : _weatherIcons) {
            if (visible) {
                Tool.visibleViews(0, icon);
            } else {
                Tool.goneViews(0, icon);
            }
        }
    }

    public static WeatherService getWeatherService() {
        final String weatherServiceRequested = AppSettings.get().getWeatherService();

        if (_weatherService == null || !_weatherService.getName().equals(weatherServiceRequested)) {
            if (weatherServiceRequested.equals("au_bom")) {
                _weatherService = new BOMWeatherService();
            } else if (weatherServiceRequested.equals("openweather")) {
                _weatherService = new OpenWeatherService();
            }
        }

        return _weatherService;
    }

}
