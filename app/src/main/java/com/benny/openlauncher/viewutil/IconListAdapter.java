package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;

import java.util.List;

public class IconListAdapter extends BaseAdapter {

    private Context _context;
    private List<String> _labels;
    private List<Integer> _icons;

    public IconListAdapter(Context context, List labels, List icons) {
        this._context = context;
        this._labels = labels;
        this._icons = icons;
    }

    public int getCount() {
        return _labels.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView tv;
        ImageView iv;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(_context);
            view = inflater.inflate(R.layout.item_minibar, parent, false);
        } else {
            view = convertView;
        }

        iv = view.findViewById(R.id.iv);
        tv = view.findViewById(R.id.tv);

        //tv.setText(labels.get(position));
        iv.setImageResource(_icons.get(position));
        return view;
    }
}