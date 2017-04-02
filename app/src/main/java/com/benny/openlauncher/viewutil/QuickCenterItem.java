package com.benny.openlauncher.viewutil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

//Concept idea of adding a place that enable user to take notes, suggest apps and contacts;
@Deprecated
public class QuickCenterItem {

    public static class NoteItem extends AbstractItem<NoteItem, NoteItem.ViewHolder> {
        public String date;
        public String description;
        private FastItemAdapter<NoteItem> adapter;

        public NoteItem(String date, String description, FastItemAdapter<QuickCenterItem.NoteItem> adapter) {
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

            viewHolder.date.setText(Html.fromHtml("<b><big>Note</big></b><br><small>" + date + "</small>"));
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (view.getContext() instanceof Home) {
                        //((Home) view.getContext()).launcher.notes.remove(viewHolder.getAdapterPosition());
                        adapter.remove(viewHolder.getAdapterPosition());
                    }
                    return true;
                }
            });
            viewHolder.description.setText(Html.fromHtml("<big>" + description + "</big>"));
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

    public static class NoteContent {
        public String content;
        public String date;

        public NoteContent(String date, String content) {
            this.content = content;
            this.date = date;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof NoteContent && this.content.equals(((NoteContent) obj).content) && this.date.equals(((NoteContent) obj).date);
        }
    }

    public static class ContactItem extends AbstractItem<ContactItem, ContactItem.ViewHolder> {
        private ContactContent info;

        public ContactItem(ContactContent info) {
            this.info = info;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.view_contact;
        }

        @Override
        public void bindView(final ViewHolder viewHolder, List payloads) {
            super.bindView(viewHolder, payloads);
            Tool.print(info.icon == null);
            if (info.icon != null)
                viewHolder.imageView.setImageDrawable(new RoundDrawable(info.icon));
            else {
                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color1 = generator.getRandomColor();
                TextDrawable.IBuilder builder = TextDrawable.builder().round();
                String name = info.name == null || info.name.isEmpty() ? info.number : info.name;
                TextDrawable ic1 = builder.build(name.substring(0, 1), color1);
                viewHolder.imageView.setImageDrawable(ic1);
            }

            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Home.launcher != null) {
                        if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                            Home.launcher.startActivity(info.data);
                        else {
                            Tool.toast(view.getContext(), "Unable to call the person without Manifest.permission.CALL_PHONE granted");
                            ActivityCompat.requestPermissions(Home.launcher, new String[]{Manifest.permission.CALL_PHONE}, Home.REQUEST_PERMISSION_CALL);
                        }
                    }
                }
            });
        }

        private final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();

        class ItemFactory implements ViewHolderFactory<ViewHolder> {
            public ViewHolder create(View v) {
                return new ViewHolder(v);
            }
        }

        @Override
        public ViewHolderFactory<? extends ViewHolder> getFactory() {
            return FACTORY;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ViewHolder(View view) {
                super(view);
                this.imageView = (ImageView) view;
            }
        }
    }

    public static class ContactContent {
        public String name;
        public Intent data;
        public Bitmap icon;
        public String number;

        public ContactContent(String name, String number, Intent data, Bitmap icon) {
            this.name = name;
            this.data = data;
            this.icon = icon;
            this.number = number;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ContactContent && this.data.equals(((ContactContent) obj).data);
        }
    }

    public static class SearchHeader extends AbstractItem<SearchHeader, SearchHeader.ViewHolder> {

        public SearchHeader() {
        }

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
