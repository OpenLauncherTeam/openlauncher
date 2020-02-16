package com.benny.openlauncher.weather;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class WeatherLocationFilterAdapter extends ArrayAdapter<WeatherLocation> implements Filterable {
    private List<WeatherLocation> _locations;

    public WeatherLocationFilterAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        _locations = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return _locations.size();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter dataFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = _locations;
                    filterResults.count = _locations.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && (results.count > 0)) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return dataFilter;
    }

    @Nullable
    @Override
    public WeatherLocation getItem(int position) {
        return _locations.get(position);
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

    public void setData(List<WeatherLocation> list) {
        _locations.clear();
        _locations.addAll(list);
    }
}

