package com.benny.openlauncher.weather;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.Tool;

import java.util.ArrayList;
import java.util.List;

public class WeatherLocationSelectionAdapter extends RecyclerView.Adapter<WeatherLocationView.WeatherLocationViewHolder> {
    private Context _context;
    private List<WeatherLocation> _locations;
    private Dialog _dialog = null;

    public WeatherLocationSelectionAdapter(@NonNull Context context) {
        _context = context;
        _locations = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return _locations.size();
    }
    /**
     * Used to Return the full object directly from adapter.
     *
     * @param position
     * @return
     */
    public WeatherLocation getObject(int position) {
        return _locations.get(position);
    }

    public void appendData(List<WeatherLocation> list) {
        for (WeatherLocation loc : list) {
            if (!_locations.contains(loc)) {
                _locations.add(loc);
            }
        }
    }

    public void appendData(WeatherLocation loc) {
        _locations.add(loc);
    }

    public void clear() {
        _locations.clear();
    }

    @Override
    public void onBindViewHolder(WeatherLocationView.WeatherLocationViewHolder holder, int position) {
        AppSettings settings = AppSettings.get();
        final WeatherLocation loc = _locations.get(position);
        holder._location.setText(loc.toString());

        holder._parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loc.getName().equals(HomeActivity._launcher.getString(R.string.weather_service_use_network_location))) {
                    // Use Location Services.
                    settings.setUseLocationServices(true);
                    HomeActivity._launcher.initWeatherIfRequired();
                } else if (loc.getName().equals(HomeActivity._launcher.getString(R.string.weather_service_add_location))) {
                    // Pop up the add Location Dialog.
                    WeatherService.getWeatherService().findCityToAdd();
                } else {
                    WeatherLocation loc = WeatherLocation.parse(holder._location.getText().toString());
                    settings.setWeatherCity(loc);
                    settings.addWeatherLocations(loc);
                    WeatherService.getWeatherService().getWeatherForLocation(loc);
                }

                if (_dialog != null) {
                    _dialog.dismiss();
                }
                int toastMessageId = settings.getWeatherForecastByHour() ? R.string.weather_service_hourly : R.string.weather_service_daily;
                Tool.toast(HomeActivity._launcher, String.format(HomeActivity._launcher.getString(toastMessageId), loc.getName()));
            }
        });
    }

    @Override
    public WeatherLocationView.WeatherLocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.weather_location, viewGroup, false);
        WeatherLocationView.WeatherLocationViewHolder listHolder = new WeatherLocationView.WeatherLocationViewHolder(mainGroup);
        return listHolder;
    }

    public void remove(int pos) {
        WeatherLocation loc = _locations.get(pos);
        _locations.remove(pos);
        AppSettings.get().removeWeatherLocations(loc);
    }

    public void setDialog(Dialog dialog) {
        _dialog = dialog;
    }
}

