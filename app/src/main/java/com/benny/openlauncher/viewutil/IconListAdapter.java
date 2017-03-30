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

    private Context c;
    private List<String> labels;
    private List<Integer> icons;

    public IconListAdapter(Context c, List labels, List icons) {
        this.c = c;
        this.labels = labels;
        this.icons = icons;
    }

    public int getCount() {
        return labels.size();
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
            LayoutInflater inflater = LayoutInflater.from(c);
            view = inflater.inflate(R.layout.item_minbar, parent, false);
        } else {
            view = convertView;
        }

        iv = (ImageView) view.findViewById(R.id.iv);
        tv = (TextView) view.findViewById(R.id.tv);

        //tv.setText(labels.get(position));
        iv.setImageResource(icons.get(position));
        return view;
    }
}