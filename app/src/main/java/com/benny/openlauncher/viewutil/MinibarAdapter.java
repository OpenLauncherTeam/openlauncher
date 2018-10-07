package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.LauncherAction;

import java.util.List;

public class MinibarAdapter extends BaseAdapter {

    private Context context;
    private List<LauncherAction.ActionDisplayItem> items;

    public MinibarAdapter(Context context, List<LauncherAction.ActionDisplayItem> items) {
        this.context = context;
        this.items = items;
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_minibar, parent, false);

        ImageView icon = view.findViewById(R.id.iv);
        TextView label = view.findViewById(R.id.tv);

        icon.setImageResource(items.get(position)._icon);
        label.setText(items.get(position)._label);
        return view;
    }
}