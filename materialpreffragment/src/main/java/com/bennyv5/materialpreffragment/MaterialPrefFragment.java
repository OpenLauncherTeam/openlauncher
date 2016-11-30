package com.bennyv5.materialpreffragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class MaterialPrefFragment extends Fragment implements OnClickListener
{
    private int accentColor = Color.BLUE;

    private int cardColor = Color.WHITE;

    private int textColor = Color.DKGRAY;

    private int textColorSec = Color.GRAY;

    private List<Pref> prefs = new ArrayList<>();

    private SharedPreferences sharedPrefs;

    private OnPrefClickedListener listener;

    private OnPrefChangedListener listener2;

    private final int TAG_ID = 638390376;

    private final int pad = 18;

    private boolean useSystemPref = true;

    public MaterialPrefFragment(){}

    public static MaterialPrefFragment newInstance(Builder b){
        MaterialPrefFragment fragment = new MaterialPrefFragment();
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
    public void onAttach(Context context){
        sharedPrefs = context.getSharedPreferences("materialpref",Context.MODE_PRIVATE);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        ScrollView contentView = new ScrollView(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        layout.setDividerDrawable(getContext().getResources().getDrawable(android.R.drawable.divider_horizontal_dark));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0,0,0,CommonUtility.pixelToDp(getContext(),6));

        for (Pref pref : prefs){
            layout.addView(pref.onCreateView(getContext(),this,sharedPrefs));
        }

        layout.addView(new GroupTitle("").onCreateView(getContext(),this,sharedPrefs));

        contentView.addView(layout);
        return contentView;
    }

    public static SharedPreferences getSharedPrefs(Context c){
        return c.getSharedPreferences("materialpref",Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View p1){
        Object tag = p1.getTag(TAG_ID);
        if (tag != null && listener != null && tag instanceof String){
            listener.onPrefClicked((String)tag);
        }
    }

    public static class GroupTitle implements Pref
    {
        String title;

        public GroupTitle(String title){
            this.title = title;
        }

        @Override
        public View onCreateView(Context c,MaterialPrefFragment fragment,SharedPreferences sharedPrefs){
            TextView b = new TextView(c);
            setStyle(b,fragment);

            b.setText(title);
            b.setPadding(CommonUtility.pixelToDp(b.getContext(),fragment.pad),CommonUtility.pixelToDp(b.getContext(),12),CommonUtility.pixelToDp(b.getContext(),fragment.pad),CommonUtility.pixelToDp(b.getContext(),12));
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setBackgroundColor(Color.TRANSPARENT);
            b.setTextColor(fragment.accentColor);
            //b.setTextColor(c.getResources().getColor(R.color.colorAccent));
            return b;
        }
    }

    public static class TBPref implements Pref
    {
        String id,title,summary;

        boolean defaultValue;

        public TBPref(String id,String title,String summary,boolean defaultValue){
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.defaultValue = defaultValue;
        }

        @Override
        public View onCreateView(Context c,final MaterialPrefFragment fragment,final SharedPreferences sharedPrefs){
            Switch s = new Switch(c);
            setStyle(s,fragment);

            s.setTag(fragment.TAG_ID,id);
            s.setOnClickListener(fragment);
            s.setText(warpText(summary,title,fragment));
            s.setChecked(sharedPrefs.getBoolean(id,defaultValue));
            s.setOnCheckedChangeListener(new OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton p1,boolean p2){
                    if (fragment.useSystemPref)
                        sharedPrefs.edit().putBoolean(id,p2).apply();

                    if (fragment.listener2 != null) {
                        fragment.listener2.onPrefChanged(id,p2);
                    }
                }
            });
            return warpCardView(s,fragment);
        }
    }

    public static class NUMPref implements Pref
    {
        String id,title,summary;

        int defaultValue,start,end;

        int currentState;

        public NUMPref(String id,String title,String summary,int defaultValue,int start,int end){
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.defaultValue = defaultValue;

            currentState = defaultValue;

            this.start = start;
            this.end = end;
        }

        @Override
        public View onCreateView(final Context c,final MaterialPrefFragment fragment,final SharedPreferences sharedPrefs){
            Button b = new Button(c);
            setStyle(b,fragment);

            b.setTag(fragment.TAG_ID,id);
            b.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View p1){
                    final SeekBar picker = new SeekBar(p1.getContext());
                    final TextView tv = new TextView(p1.getContext());
                    tv.setTextColor(fragment.textColor);
                    picker.setMax(end);

                    if (fragment.useSystemPref)
                        currentState = sharedPrefs.getInt(id,defaultValue);

                    picker.setProgress(currentState-start);
                    picker.setPadding(CommonUtility.pixelToDp(p1.getContext(),60),picker.getPaddingTop(),picker.getPaddingRight(),0);
                    tv.setText(String.valueOf(picker.getProgress()+start));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                    tv.setPadding(CommonUtility.pixelToDp(p1.getContext(),20),0,0,0);
                    picker.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
                        @Override
                        public void onProgressChanged(SeekBar p1,int p2,boolean p3){
                            tv.setText(String.valueOf(p2+start));
                            if (!fragment.useSystemPref)
                            currentState = p2+start;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar p1){}

                        @Override
                        public void onStopTrackingTouch(SeekBar p1){}
                    });

                    FrameLayout layout = new FrameLayout(p1.getContext());
                    layout.setPadding(CommonUtility.pixelToDp(p1.getContext(),10),CommonUtility.pixelToDp(p1.getContext(),10),CommonUtility.pixelToDp(p1.getContext(),10),CommonUtility.pixelToDp(p1.getContext(),10));
                    layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    l.gravity = Gravity.END;
                    layout.addView(picker,l);

                    FrameLayout.LayoutParams l2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    l2.gravity = Gravity.CENTER_VERTICAL;
                    layout.addView(tv,l2);

                    new MaterialDialog.Builder(c)
                            .title(title)
                            .customView(layout,false)
                            .positiveText("ok")
                            .negativeText("cancel")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if (fragment.useSystemPref)
                                        sharedPrefs.edit().putInt(id,picker.getProgress()+start).apply();

                                    if (fragment.listener2 != null) {
                                        fragment.listener2.onPrefChanged(id,picker.getProgress()+start);
                                    }
                                }
                            })
                            .show();
                }
            });
            b.setText(warpText(summary,title,fragment));
            return warpCardView(b,fragment);
        }
    }

    public static class ButtonPref implements Pref
    {
        String id,title,summary;

        public ButtonPref(String id,String title,String summary){
            this.id = id;
            this.title = title;
            this.summary = summary;
        }

        @Override
        public View onCreateView(Context c,MaterialPrefFragment fragment,SharedPreferences sharedPrefs){
            Button b = new Button(c);
            setStyle(b,fragment);

            b.setTag(fragment.TAG_ID,id);
            b.setOnClickListener(fragment);
            b.setText(warpText(summary,title,fragment));
            return warpCardView(b,fragment);
        }
    }

    public interface Pref
    {
        View onCreateView(Context c,MaterialPrefFragment fragment,SharedPreferences sharedPrefs);
    }

    private static View warpCardView(View v,MaterialPrefFragment fragment){
        CardView cv = new CardView(v.getContext());
        cv.setLayoutParams(CommonUtility.matchParentWidthLayoutParams());
        cv.setCardBackgroundColor(fragment.cardColor);
        cv.setCardElevation(4);
        cv.setRadius(0);
        cv.addView(v);
        return cv;
    }

    private static Spanned warpText(String summary, String title,MaterialPrefFragment fragment){
        return summary == null ? new SpannableString(title) : Html.fromHtml(title + "<br>" + "<small>" + CommonUtility.warpColorTag(summary,fragment.textColorSec) + "</small>");
    }

    private static void setStyle(TextView textView,MaterialPrefFragment fragment){
        textView.setTextColor(fragment.textColor);
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        textView.setAllCaps(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setStateListAnimator(null);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        textView.setTypeface(Typeface.DEFAULT);
        textView.setPadding(CommonUtility.pixelToDp(textView.getContext(),fragment.pad),CommonUtility.pixelToDp(textView.getContext(),fragment.pad),CommonUtility.pixelToDp(textView.getContext(),fragment.pad),CommonUtility.pixelToDp(textView.getContext(),fragment.pad));
        if (fragment.cardColor == Color.WHITE)
            textView.setBackgroundResource(R.drawable.selector_dark);
        else
            textView.setBackgroundResource(R.drawable.selector);
        textView.setLayoutParams(CommonUtility.matchParentWidthLayoutParams());
    }

    public static class Builder
    {
        private List<Pref> prefs = new ArrayList<>();

        private OnPrefClickedListener listener;

        private OnPrefChangedListener listener2;

        private int accentColor;

        private int cardColor;

        private int textColor;

        private int textColorSec;

        private boolean useSystemPref;

        public Builder(int textColor,int textColorSec,int cardColor,int accentColor,boolean useSystemPref){
            this.textColorSec = textColorSec;
            this.textColor = textColor;
            this.accentColor = accentColor;
            this.useSystemPref = useSystemPref;
            this.cardColor = cardColor;
        }

        public Builder(){this.useSystemPref = true;}

        public Builder add(Pref pref){
            prefs.add(pref);
            return this;
        }

        public Builder setOnPrefClickedListener(OnPrefClickedListener listener){
            this.listener = listener;
            return this;
        }

        public Builder setOnPrefChangedListener(OnPrefChangedListener listener){
            this.listener2 = listener;
            return this;
        }
    }

    public interface OnPrefChangedListener{
        void onPrefChanged(String id,Object p2);
    }

    public interface OnPrefClickedListener
    {
        void onPrefClicked(String id);
    }
}
