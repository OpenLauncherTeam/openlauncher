package com.benny.openlauncher.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.LauncherAction;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MinibarEditActivity extends ColorActivity implements ItemTouchCallback {
    @BindView(R.id.toolbar)
    Toolbar _toolbar;
    @BindView(R.id.enableSwitch)
    SwitchCompat _enableSwitch;
    @BindView(R.id.recyclerView)
    RecyclerView _recyclerView;
    private FastItemAdapter<Item> _adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_minibar_edit);
        ButterKnife.bind(this);
        setSupportActionBar(_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.minibar);

        _adapter = new FastItemAdapter<>();

        SimpleDragCallback touchCallback = new SimpleDragCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(_recyclerView);

        _recyclerView.setLayoutManager(new LinearLayoutManager(this));
        _recyclerView.setAdapter(_adapter);

        final ArrayList<LauncherAction.ActionDisplayItem> minibarArrangement = AppSettings.get().getMinibarArrangement();
        for (LauncherAction.ActionDisplayItem item : LauncherAction.actionDisplayItems) {
            _adapter.add(new Item(item, minibarArrangement.contains(item)));
        }

        boolean minibarEnable = AppSettings.get().getMinibarEnable();
        _enableSwitch.setChecked(minibarEnable);
        _enableSwitch.setText(minibarEnable ? R.string.on : R.string.off);
        _enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setText(isChecked ? R.string.on : R.string.off);
                AppSettings.get().setMinibarEnable(isChecked);
                if (HomeActivity.Companion.getLauncher() != null) {
                    HomeActivity.Companion.getLauncher().closeAppDrawer();
                    HomeActivity.Companion.getLauncher().getDrawerLayout().setDrawerLockMode(isChecked ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        });

        setResult(RESULT_OK);
    }

    @Override
    protected void onPause() {
        ArrayList<String> minibarArrangement = new ArrayList<>();
        for (Item item : _adapter.getAdapterItems()) {
            if (item.enable) minibarArrangement.add(item.item._action.toString());
        }
        AppSettings.get().setMinibarArrangement(minibarArrangement);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (HomeActivity.Companion.getLauncher() != null) {
            HomeActivity.Companion.getLauncher().initMinibar();
        }
        super.onStop();
    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        Collections.swap(_adapter.getAdapterItems(), oldPosition, newPosition);
        _adapter.notifyAdapterDataSetChanged();
        return false;
    }

    @Override
    public void itemTouchDropped(int i, int i1) {
    }

    public static class Item extends AbstractItem<Item, Item.ViewHolder> {
        public final LauncherAction.ActionDisplayItem item;
        public boolean enable;

        public Item(LauncherAction.ActionDisplayItem item, boolean enable) {
            this.item = item;
            this.enable = enable;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.item_edit_minibar;
        }

        @Override
        public ViewHolder getViewHolder(View v) {
            return new ViewHolder(v);
        }

        @Override
        public void bindView(ViewHolder holder, List payloads) {
            holder._label.setText(item._label);
            holder._description.setText(item._description);
            holder._icon.setImageResource(item._icon);
            holder._cb.setChecked(enable);
            holder._cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    enable = b;
                }
            });
            super.bindView(holder, payloads);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView _label;
            TextView _description;
            ImageView _icon;
            CheckBox _cb;

            public ViewHolder(View itemView) {
                super(itemView);
                _label = itemView.findViewById(R.id.tv);
                _description = itemView.findViewById(R.id.tv2);
                _icon = itemView.findViewById(R.id.iv);
                _cb = itemView.findViewById(R.id.cb);
            }
        }
    }
}