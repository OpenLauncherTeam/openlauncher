package com.bennyv5.materialpreffragment;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.color.ColorChooserDialog;

/**
 * Created by BennyKok on 12/4/2016.
 */

public class BaseSettingsActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    private MaterialPrefFragment fragment;

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        fragment.currentColorPref.selected = selectedColor;
        fragment.listener2.onPrefChanged(fragment.currentColorPref.id, selectedColor);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }

    public void setSettingsFragment(MaterialPrefFragment fragment) {
        this.fragment = fragment;
    }
}
