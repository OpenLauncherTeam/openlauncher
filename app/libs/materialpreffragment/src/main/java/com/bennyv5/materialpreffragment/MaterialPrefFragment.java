package com.bennyv5.materialpreffragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaterialPrefFragment extends Fragment implements OnClickListener {
    private int accentColor = Color.BLUE;

    private int cardColor = Color.WHITE;

    private int textColor = Color.DKGRAY;

    private int textColorSec = Color.GRAY;

    private List<Pref> prefs = new ArrayList<>();

    private SharedPreferences sharedPrefs;

    public OnPrefClickedListener listener;

    public OnPrefChangedListener listener2;

    private final int TAG_ID = 638390376;

    private final int pad = 18;

    private boolean useSystemPref = true;

    private BaseSettingsActivity activity;

    public ColorPref currentColorPref;

    public MaterialPrefFragment() {
    }

    public static MaterialPrefFragment newInstance(Builder b) {
        MaterialPrefFragment fragment = new MaterialPrefFragment();
        fragment.activity = b.activity;
        fragment.useSystemPref = b.useSystemPref;
        fragment.textColorSec = b.textColorSec;
        fragment.cardColor = b.cardColor;
        fragment.textColor = b.textColor;
        fragment.accentColor = b.accentColor;
        fragment.prefs = b.prefs;
        fragment.listener = b.listener;
        fragment.listener2 = b.listener2;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        sharedPrefs = context.getSharedPreferences("materialpref", Context.MODE_PRIVATE);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView contentView = new ScrollView(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        layout.setDividerDrawable(getContext().getResources().getDrawable(android.R.drawable.divider_horizontal_dark));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 0, 0, Tool.pixelToDp(getContext(), 6));

        for (Pref pref : prefs) {
            layout.addView(pref.onCreateView(getContext(), this, sharedPrefs));
        }

        layout.addView(new GroupTitle("").onCreateView(getContext(), this, sharedPrefs));

        contentView.addView(layout);
        return contentView;
    }

    public static SharedPreferences getSharedPrefs(Context c) {
        return c.getSharedPreferences("materialpref", Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View p1) {
        Object tag = p1.getTag(TAG_ID);
        if (tag != null && listener != null && tag instanceof String) {
            listener.onPrefClicked((String) tag);
        }
    }

    public static class GroupTitle implements Pref {
        String title;

        public GroupTitle(String title) {
            this.title = title;
        }

        @Override
        public View onCreateView(Context c, MaterialPrefFragment fragment, SharedPreferences sharedPrefs) {
            TextView b = new TextView(c);
            setStyle(b, fragment);

            b.setText(title);
            b.setPadding(Tool.pixelToDp(b.getContext(), fragment.pad), Tool.pixelToDp(b.getContext(), 12), Tool.pixelToDp(b.getContext(), fragment.pad), Tool.pixelToDp(b.getContext(), 12));
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setBackgroundColor(Color.TRANSPARENT);
            b.setTextColor(fragment.accentColor);
            //b.setTextColor(c.getResources().getColor(R.color.colorAccent));
            return b;
        }
    }

    public static class TBPref implements Pref {
        String id, title, summary;

        boolean defaultValue;

        public TBPref(String id, String title, String summary, boolean defaultValue) {
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.defaultValue = defaultValue;
        }

        @Override
        public View onCreateView(Context c, final MaterialPrefFragment fragment, final SharedPreferences sharedPrefs) {
            Switch s = new Switch(c);
            setStyle(s, fragment);

            s.setTag(fragment.TAG_ID, id);
            s.setOnClickListener(fragment);
            s.setText(warpText(summary, title, fragment));
            s.setChecked(sharedPrefs.getBoolean(id, defaultValue));
            s.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton p1, boolean p2) {
                    if (fragment.useSystemPref)
                        sharedPrefs.edit().putBoolean(id, p2).apply();

                    if (fragment.listener2 != null) {
                        fragment.listener2.onPrefChanged(id, p2);
                    }
                }
            });
            return warpCardView(s, fragment);
        }
    }

    public static class NUMPref implements Pref {
        String id, title, summary;

        ArrayList<NUMPrefItem> items;

        public NUMPref(String id, String title, String summary, NUMPrefItem... items) {
            this.id = id;
            this.title = title;
            this.summary = summary;

            this.items = new ArrayList<>(Arrays.asList(items));
        }

        public NUMPref(String id, String title, String summary, int defaultValue, int start, int end) {
            this.id = id;
            this.title = title;
            this.summary = summary;

            this.items = new ArrayList<>(Arrays.asList(new NUMPrefItem(id, null, defaultValue, start, end)));
        }

        public static class NUMPrefItem {
            String id, title;

            int defaultValue, start, end;

            int currentState;

            SeekBar valueBar;
            TextView valueTitle;
            TextView itemTitle;

            public NUMPrefItem(String id, String title, int defaultValue, int start, int end) {
                this.id = id;
                this.title = title;
                this.defaultValue = defaultValue;
                this.currentState = defaultValue;
                this.start = start;
                this.end = end;
            }

            public View getItemView(ViewGroup parent, final MaterialPrefFragment fragment, SharedPreferences sharedPrefs) {
                View layout = LayoutInflater.from(fragment.getContext()).inflate(R.layout.item_pref_num_picker, parent, false);
                valueBar = (SeekBar) layout.findViewById(R.id.valueBar);
                valueTitle = (TextView) layout.findViewById(R.id.valueTitle);
                itemTitle = (TextView) layout.findViewById(R.id.itemTitle);

                itemTitle.setTextColor(fragment.textColor);
                if (title != null)
                    itemTitle.setText(title);
                else {
                    itemTitle.setVisibility(View.GONE);
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) valueTitle.getLayoutParams();
                    layoutParams.topMargin = Tool.pixelToDp(fragment.getContext(), 8);
                    layoutParams.leftMargin = Tool.pixelToDp(fragment.getContext(), 24);
                }
                valueTitle.setTextColor(fragment.textColor);
                valueBar.setMax(end);

                if (fragment.useSystemPref)
                    currentState = sharedPrefs.getInt(id, defaultValue);

                valueBar.setProgress(currentState - start);
                valueTitle.setText(String.valueOf(valueBar.getProgress() + start));
                valueTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                valueBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
                        valueTitle.setText(String.valueOf(p2 + start));
                        if (!fragment.useSystemPref)
                            currentState = p2 + start;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar p1) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar p1) {
                    }
                });
                return layout;
            }

            public void save(boolean systemPref, SharedPreferences sharedPrefs, OnPrefChangedListener listener) {
                if (systemPref)
                    sharedPrefs.edit().putInt(id, valueBar.getProgress() + start).apply();
                if (listener != null) {
                    listener.onPrefChanged(id, valueBar.getProgress() + start);
                }
            }
        }

        @Override
        public View onCreateView(final Context c, final MaterialPrefFragment fragment, final SharedPreferences sharedPrefs) {
            Button b = new Button(c);
            setStyle(b, fragment);

            b.setTag(fragment.TAG_ID, id);
            b.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View p1) {
                    LinearLayout layout = new LinearLayout(fragment.getContext());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    if (items != null)
                        for (int i = 0; i < items.size(); i++) {
                            layout.addView(items.get(i).getItemView(layout, fragment, sharedPrefs));
                        }
                    new MaterialDialog.Builder(c)
                            .title(title)
                            .customView(layout, false)
                            .positiveText(R.string.ok)
                            .negativeText(R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    for (int i = 0; i < items.size(); i++)
                                        items.get(i).save(fragment.useSystemPref, sharedPrefs, fragment.listener2);
                                }
                            })
                            .show();
                }
            });
            b.setText(warpText(summary, title, fragment));
            return warpCardView(b, fragment);
        }
    }

    public static class ColorPref implements Pref {
        String id, title, summary;
        int selected;

        public ColorPref(String id, String title, String summary, int selected) {
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.selected = selected;
        }

        @Override
        public View onCreateView(Context c, final MaterialPrefFragment fragment, SharedPreferences sharedPrefs) {
            Button b = new Button(c);
            setStyle(b, fragment);

            b.setTag(fragment.TAG_ID, id);
            b.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.currentColorPref = ColorPref.this;
                    new ColorChooserDialog.Builder(fragment.activity, R.string.choose_color)
                            .titleSub(R.string.choose_color)
                            .doneButton(R.string.done)
                            .cancelButton(R.string.cancel)
                            .backButton(R.string.back)
                            .preselect(selected)
                            .dynamicButtonColor(false)
                            .show();
                }
            });
            b.setText(warpText(summary, title, fragment));
            return warpCardView(b, fragment);
        }
    }

    public static class ButtonPref implements Pref {
        String id, title, summary;
        Drawable icon = null;

        public ButtonPref(String id, String title, String summary) {
            this.id = id;
            this.title = title;
            this.summary = summary;
        }

        public ButtonPref(String id, Drawable icon, String title, String summary) {
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.icon = icon;
        }

        @Override
        public View onCreateView(Context c, MaterialPrefFragment fragment, SharedPreferences sharedPrefs) {
            Button b = new Button(c);
            setStyle(b, fragment);

            b.setTag(fragment.TAG_ID, id);
            if (icon != null) {
                b.setCompoundDrawablePadding(Tool.dp2px(20, c));
                b.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
            b.setOnClickListener(fragment);
            b.setText(warpText(summary, title, fragment));
            return warpCardView(b, fragment);
        }
    }

    public interface Pref {
        View onCreateView(Context c, MaterialPrefFragment fragment, SharedPreferences sharedPrefs);
    }

    private static View warpCardView(View v, MaterialPrefFragment fragment) {
        CardView cv = new CardView(v.getContext());
        cv.setLayoutParams(Tool.matchParentWidthLayoutParams());
        cv.setCardBackgroundColor(fragment.cardColor);
        cv.setCardElevation(4);
        cv.setRadius(0);
        cv.addView(v);
        return cv;
    }

    private static Spanned warpText(String summary, String title, MaterialPrefFragment fragment) {
        return summary == null ? new SpannableString(title) : Html.fromHtml(title + "<br>" + "<small>" + Tool.warpColorTag(summary, fragment.textColorSec) + "</small>");
    }

    private static void setStyle(TextView textView, MaterialPrefFragment fragment) {
        textView.setTextColor(fragment.textColor);
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        textView.setAllCaps(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setStateListAnimator(null);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        textView.setTypeface(Typeface.DEFAULT);
        textView.setPadding(Tool.pixelToDp(textView.getContext(), fragment.pad), Tool.pixelToDp(textView.getContext(), fragment.pad), Tool.pixelToDp(textView.getContext(), fragment.pad), Tool.pixelToDp(textView.getContext(), fragment.pad));
        if (fragment.cardColor == Color.WHITE)
            textView.setBackgroundResource(R.drawable.selector_dark);
        else
            textView.setBackgroundResource(R.drawable.selector);
        textView.setLayoutParams(Tool.matchParentWidthLayoutParams());
    }

    public static class Builder {
        private List<Pref> prefs = new ArrayList<>();

        private OnPrefClickedListener listener;

        private OnPrefChangedListener listener2;

        private BaseSettingsActivity activity;

        private int accentColor;

        private int cardColor;

        private int textColor;

        private int textColorSec;

        private boolean useSystemPref;

        public Builder(BaseSettingsActivity activity, int textColor, int textColorSec, int cardColor, int accentColor, boolean useSystemPref) {
            this.activity = activity;
            this.textColorSec = textColorSec;
            this.textColor = textColor;
            this.accentColor = accentColor;
            this.useSystemPref = useSystemPref;
            this.cardColor = cardColor;
        }

        public Builder() {
            this.useSystemPref = true;
        }

        public Builder add(Pref pref) {
            prefs.add(pref);
            return this;
        }

        public Builder setOnPrefClickedListener(OnPrefClickedListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setOnPrefChangedListener(OnPrefChangedListener listener) {
            this.listener2 = listener;
            return this;
        }
    }

    public interface OnPrefChangedListener {
        void onPrefChanged(String id, Object p2);
    }

    public interface OnPrefClickedListener {
        void onPrefClicked(String id);
    }
}
