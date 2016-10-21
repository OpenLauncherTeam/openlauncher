package com.benny.openlauncher.util;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

//Concept idea of adding a place that enable user to take notes, suggest apps and contacts;
public class QuickCenterItem{

    public static class NoteItem extends AbstractItem<NoteItem, NoteItem.ViewHolder> {
        public String date;
        public String description;
        private FastItemAdapter<QuickCenterItem.NoteItem> adapter;

        public NoteItem(String date, String description, FastItemAdapter<QuickCenterItem.NoteItem> adapter){
            this.date = date;
            this.description = description;
            this.adapter = adapter;
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
        public void bindView(final ViewHolder viewHolder, List payloads) {
            super.bindView(viewHolder, payloads);

            viewHolder.date.setText(Html.fromHtml("<b><big>Note</big></b><br><small>"+date+"</small>"));
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    adapter.remove(viewHolder.getAdapterPosition());
                    return true;
                }
            });
            viewHolder.description.setText(Html.fromHtml("<big>"+description+"</big>"));
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
            protected TextView date;
            protected TextView description;

            public ViewHolder(View view) {
                super(view);
                this.date = (TextView) view.findViewById(R.id.tv);
                this.description = (TextView) view.findViewById(R.id.tv2);
            }
        }
    }

    public static class NoteContent{
        public String content;
        public String date;

        public NoteContent(String date,String content){
            this.content = content;
            this.date = date;
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
