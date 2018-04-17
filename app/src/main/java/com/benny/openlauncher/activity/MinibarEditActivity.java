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

public class MinibarEditActivity extends ThemeActivity implements ItemTouchCallback {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.enableSwitch)
    SwitchCompat enableSwitch;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private FastItemAdapter<Item> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_minibar_edit);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.minibar);

        adapter = new FastItemAdapter<>();

        SimpleDragCallback touchCallback = new SimpleDragCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        int i = 0;
        final ArrayList<String> minibarArrangement = AppSettings.get().getMinibarArrangement();
        for (String act : minibarArrangement) {
            LauncherAction.ActionDisplayItem item = LauncherAction.getActionItemFromString(act.substring(1));
            adapter.add(new Item(i, item, act.charAt(0) == '0'));
            i++;
        }

        boolean minBarEnable = AppSettings.get().getMinibarEnable();
        enableSwitch.setChecked(minBarEnable);
        enableSwitch.setText(minBarEnable ? R.string.on : R.string.off);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setText(isChecked ? R.string.on : R.string.off);
                AppSettings.get().setMinibarEnable(isChecked);
                if (Home.Companion.getLauncher() != null) {
                    Home.Companion.getLauncher().closeAppDrawer();
                    Home.Companion.getLauncher().getDrawerLayout().setDrawerLockMode(isChecked ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        });

        setResult(RESULT_OK);
    }

    @Override
    protected void onPause() {
        ArrayList<String> minibarArrangement = new ArrayList<>();
        for (Item item : adapter.getAdapterItems()) {
            if (item.enable) {
                minibarArrangement.add("0" + item.item.label.toString());
            } else
                minibarArrangement.add("1" + item.item.label.toString());
        }
        AppSettings.get().setMinibarArrangement(minibarArrangement);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (Home.Companion.getLauncher() != null) {
            Home.Companion.getLauncher().initMinibar();
        }
        super.onStop();
    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        Collections.swap(adapter.getAdapterItems(), oldPosition, newPosition);
        adapter.notifyAdapterDataSetChanged();
        return false;
    }

    @Override
    public void itemTouchDropped(int i, int i1) {
    }

    public static class Item extends AbstractItem<Item, Item.ViewHolder> {
        public final long id;
        public final LauncherAction.ActionDisplayItem item;
        public boolean enable;
        public boolean edited;

        public Item(long id, LauncherAction.ActionDisplayItem item, boolean enable) {
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
            return R.layout.item_minibar_edit;
        }

        @Override
        public ViewHolder getViewHolder(View v) {
            return new ViewHolder(v);
        }

        @Override
        public void bindView(ViewHolder holder, List payloads) {
            holder.tv.setText(item.label.toString());
            holder.tv2.setText(item.description);
            holder.iv.setImageResource(item.icon);
            holder.cb.setChecked(enable);
            holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    edited = true;
                    enable = b;
                }
            });
            super.bindView(holder, payloads);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            TextView tv2;
            ImageView iv;
            CheckBox cb;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tv);
                tv2 = itemView.findViewById(R.id.tv2);
                iv = itemView.findViewById(R.id.iv);
                cb = itemView.findViewById(R.id.cb);
            }
        }
    }
}