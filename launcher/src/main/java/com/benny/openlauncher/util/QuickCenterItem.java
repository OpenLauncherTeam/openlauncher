package com.benny.openlauncher.util;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.LauncherSettings;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.HeaderAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

//Concept idea of adding a place that enable user to take notes, suggest apps and contacts;
public class QuickCenterItem{

    public static class NoteItem extends AbstractItem<NoteItem, NoteItem.ViewHolder> {
        public String name;
        public String description;

        public NoteItem(String name,String description){
            this.name = name;
            this.description = description;
        }

        @Override
        public int getType() {
            return R.id.item_note;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.item_note;
        }

        @Override
        public void bindView(ViewHolder viewHolder, List payloads) {
            super.bindView(viewHolder, payloads);

            //viewHolder.name.setText(name);
            viewHolder.description.setText(description);
        }

        private final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();

        protected class ItemFactory implements ViewHolderFactory<ViewHolder> {
            public ViewHolder create(View v) {
                return new ViewHolder(v);
            }
        }

        @Override
        public ViewHolderFactory<? extends ViewHolder> getFactory() {
            return FACTORY;
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            protected TextView name;
            protected TextView description;

            public ViewHolder(View view) {
                super(view);
                this.name = (TextView) view.findViewById(R.id.tv);
                this.description = (TextView) view.findViewById(R.id.tv2);
            }
        }
    }

    public static class SearchHeader extends AbstractItem<SearchHeader, SearchHeader.ViewHolder> {

        public SearchHeader(){}

        @Override
        public int getType() {
            return R.id.item_header_search;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.item_header_search;
        }

        @Override
        public void bindView(ViewHolder viewHolder, List payloads) {
            super.bindView(viewHolder, payloads);
        }

        private final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();

        protected class ItemFactory implements ViewHolderFactory<ViewHolder> {
            public ViewHolder create(View v) {
                return new ViewHolder(v);
            }
        }

        @Override
        public ViewHolderFactory<? extends ViewHolder> getFactory() {
            return FACTORY;
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            protected EditText et;

            public ViewHolder(View view) {
                super(view);
                this.et = (EditText) view.findViewById(R.id.et);
            }
        }
    }


}
