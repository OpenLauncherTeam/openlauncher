package com.benny.openlauncher.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.benny.openlauncher.activity.Home.launcher;

public class MinibarEditFragment extends Fragment implements ItemTouchCallback {
    @BindView(R.id.enableSwitch)
    SwitchCompat enableSwitch;
    @BindView(R.id.rv)
    RecyclerView rv;
    private FastItemAdapter<AppItem> adapter;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Tool.setTheme((Activity) context);
        super.onCreate(savedInstanceState);

        ButterKnife.bind((Activity) context);

        adapter = new FastItemAdapter<>();

        SimpleDragCallback touchCallback = new SimpleDragCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(rv);

        rv.setLayoutManager(new LinearLayoutManager(context));

        rv.setAdapter(adapter);

        int i = 0;
        final ArrayList<String> minBarArrangement = LauncherSettings.getInstance(context).generalSettings.miniBarArrangement;
        for (String act : minBarArrangement) {
            LauncherAction.ActionItem item = LauncherAction.getActionItemFromString(act.substring(1));
            adapter.add(new AppItem(i, item, act.charAt(0) == '0'));
            i++;
        }

        boolean minBarEnable = LauncherSettings.getInstance(context).generalSettings.minBarEnable;
        enableSwitch.setChecked(minBarEnable);
        enableSwitch.setText(minBarEnable ? R.string.on : R.string.off);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setText(isChecked ? R.string.on : R.string.off);
                LauncherSettings.getInstance(context).generalSettings.minBarEnable = isChecked;
                if (Home.launcher != null) {
                    Home.launcher.drawerLayout.closeDrawers();
                    Home.launcher.drawerLayout.setDrawerLockMode(isChecked ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_minbaredit, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public void onPause() {
        LauncherSettings.getInstance(context).generalSettings.miniBarArrangement.clear();
        for (AppItem item : adapter.getAdapterItems()) {
            if (item.enable) {
                LauncherSettings.getInstance(context).generalSettings.miniBarArrangement.add("0" + item.item.label.toString());
            } else {
                LauncherSettings.getInstance(context).generalSettings.miniBarArrangement.add("1" + item.item.label.toString());
            }
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (launcher != null) {
            launcher.initMinBar();
        }
        super.onStop();
    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        Collections.swap(adapter.getAdapterItems(), oldPosition, newPosition);
        adapter.notifyAdapterDataSetChanged();
        return false;
    }

    public static class AppItem extends AbstractItem<AppItem, AppItem.ViewHolder> {
        public final long id;
        public final LauncherAction.ActionItem item;
        public boolean enable;
        public boolean edited;

        public AppItem(long id, LauncherAction.ActionItem item, boolean enable) {
            this.id = id;
            this.item = item;
            this.enable = enable;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.item_minbaredit;
        }

        private static final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();

        static class ItemFactory implements ViewHolderFactory<ViewHolder> {
            public ViewHolder create(View v) {
                return new ViewHolder(v);
            }
        }

        @Override
        public ViewHolderFactory<? extends ViewHolder> getFactory() {
            return FACTORY;
        }

        @Override
        public void bindView(ViewHolder holder, List payloads) {
            holder.label.setText(item.label.toString());
            holder.description.setText(item.des);
            holder.icon.setImageResource(item.icon);
            holder.checkbox.setChecked(enable);
            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    edited = true;
                    enable = b;
                }
            });
            super.bindView(holder, payloads);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView label;
            TextView description;
            ImageView icon;
            CheckBox checkbox;

            public ViewHolder(View itemView) {
                super(itemView);
                label = (TextView) itemView.findViewById(R.id.tv);
                description = (TextView) itemView.findViewById(R.id.tv2);
                icon = (ImageView) itemView.findViewById(R.id.iv);
                checkbox = (CheckBox) itemView.findViewById(R.id.cb);
            }
        }
    }
}