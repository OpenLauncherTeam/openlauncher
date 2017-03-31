package com.benny.openlauncher.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
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

import butterknife.ButterKnife;
import butterknife.BindView;

import static com.benny.openlauncher.activity.Home.launcher;

public class MinibarEditActivity extends AppCompatActivity implements ItemTouchCallback {
    @BindView(R.id.tb)
    Toolbar tb;
    @BindView(R.id.enableSwitch)
    SwitchCompat enableSwitch;
    @BindView(R.id.rv)
    RecyclerView rv;
    private FastItemAdapter<Item> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Tool.setTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_minbaredit);
        ButterKnife.bind(this);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.minibar);

        adapter = new FastItemAdapter<>();

        SimpleDragCallback touchCallback = new SimpleDragCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(rv);

        rv.setLayoutManager(new LinearLayoutManager(this));

        rv.setAdapter(adapter);

        int i = 0;
        final ArrayList<String> minBarArrangement = LauncherSettings.getInstance(this).generalSettings.miniBarArrangement;
        for (String act : minBarArrangement) {
            LauncherAction.ActionItem item = LauncherAction.getActionItemFromString(act.substring(1));
            adapter.add(new Item(i, item, act.charAt(0) == '0'));
            i++;
        }

        boolean minBarEnable = LauncherSettings.getInstance(MinibarEditActivity.this).generalSettings.minBarEnable;
        enableSwitch.setChecked(minBarEnable);
        enableSwitch.setText(minBarEnable ? R.string.on : R.string.off);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setText(isChecked ? R.string.on : R.string.off);
                LauncherSettings.getInstance(MinibarEditActivity.this).generalSettings.minBarEnable = isChecked;
                if (Home.launcher != null) {
                    Home.launcher.drawerLayout.closeDrawers();
                    Home.launcher.drawerLayout.setDrawerLockMode(isChecked ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        });

        setResult(RESULT_OK);
    }

    @Override
    protected void onPause() {
        LauncherSettings.getInstance(this).generalSettings.miniBarArrangement.clear();
        for (Item item : adapter.getAdapterItems()) {
            if (item.enable) {
                LauncherSettings.getInstance(this).generalSettings.miniBarArrangement.add("0" + item.item.label.toString());
            } else
                LauncherSettings.getInstance(this).generalSettings.miniBarArrangement.add("1" + item.item.label.toString());
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (launcher != null)
            launcher.initMinBar();
        super.onStop();
    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        Collections.swap(adapter.getAdapterItems(), oldPosition, newPosition);
        adapter.notifyAdapterDataSetChanged();
        return false;
    }

    public static class Item extends AbstractItem<Item, Item.ViewHolder> {
        public final long id;
        public final LauncherAction.ActionItem item;
        public boolean enable;
        public boolean edited;

        public Item(long id, LauncherAction.ActionItem item, boolean enable) {
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
            holder.tv.setText(item.label.toString());
            holder.tv2.setText(item.des);
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
                tv = (TextView) itemView.findViewById(R.id.tv);
                tv2 = (TextView) itemView.findViewById(R.id.tv2);
                iv = (ImageView) itemView.findViewById(R.id.iv);
                cb = (CheckBox) itemView.findViewById(R.id.cb);
            }
        }
    }
}