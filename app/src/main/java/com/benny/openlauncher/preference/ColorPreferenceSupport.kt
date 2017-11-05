/*
 * Copyright (C) 2017 Jared Rummler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.benny.openlauncher.preference

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.view.ContextThemeWrapper

import com.jaredrummler.android.colorpicker.R
import com.jaredrummler.android.colorpicker.ColorPanelView
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.jaredrummler.android.colorpicker.ColorShape

/**
 * A Preference to select a color
 */
class ColorPreferenceSupport : Preference, ColorPickerDialogListener {
    private var onShowDialogListener: OnShowDialogListener? = null
    private var color = Color.BLACK
    private var showDialog: Boolean = false
    @ColorPickerDialog.DialogType
    private var dialogType: Int = 0
    private var colorShape: Int = 0
    private var allowPresets: Boolean = false
    private var allowCustom: Boolean = false
    private var showAlphaSlider: Boolean = false
    private var showColorShades: Boolean = false
    private var previewSize: Int = 0
    /**
     * Get the colors that will be shown in the [ColorPickerDialog].
     *
     * @return An array of color ints
     */
    /**
     * Set the colors shown in the [ColorPickerDialog].
     *
     * @param presets An array of color ints
     */
    var presets: IntArray? = null
    private var dialogTitle: Int = 0

    /**
     * The tag used for the [ColorPickerDialog].
     *
     * @return The tag
     */
    val fragmentTag: String
        get() = "color_" + key

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        isPersistent = true
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorPreference)
        showDialog = a.getBoolean(R.styleable.ColorPreference_cpv_showDialog, true)

        dialogType = a.getInt(R.styleable.ColorPreference_cpv_dialogType, ColorPickerDialog.TYPE_PRESETS)
        colorShape = a.getInt(R.styleable.ColorPreference_cpv_colorShape, ColorShape.CIRCLE)
        allowPresets = a.getBoolean(R.styleable.ColorPreference_cpv_allowPresets, true)
        allowCustom = a.getBoolean(R.styleable.ColorPreference_cpv_allowCustom, true)
        showAlphaSlider = a.getBoolean(R.styleable.ColorPreference_cpv_showAlphaSlider, false)
        showColorShades = a.getBoolean(R.styleable.ColorPreference_cpv_showColorShades, true)
        previewSize = a.getInt(R.styleable.ColorPreference_cpv_previewSize, SIZE_NORMAL)
        val presetsResId = a.getResourceId(R.styleable.ColorPreference_cpv_colorPresets, 0)
        dialogTitle = a.getResourceId(R.styleable.ColorPreference_cpv_dialogTitle, R.string.cpv_default_title)
        if (presetsResId != 0) {
            presets = context.resources.getIntArray(presetsResId)
        } else {
            presets = ColorPickerDialog.MATERIAL_COLORS
        }
        if (colorShape == ColorShape.CIRCLE) {
            widgetLayoutResource = if (previewSize == SIZE_LARGE) R.layout.cpv_preference_circle_large else R.layout.cpv_preference_circle
        } else {
            widgetLayoutResource = if (previewSize == SIZE_LARGE) R.layout.cpv_preference_square_large else R.layout.cpv_preference_square
        }
        a.recycle()
    }

    override fun onClick() {
        super.onClick()
        if (onShowDialogListener != null) {
            onShowDialogListener!!.onShowColorPickerDialog(title as String, color)
        } else if (showDialog) {
            val dialog = ColorPickerDialog.newBuilder()
                    .setDialogType(dialogType)
                    .setDialogTitle(dialogTitle)
                    .setColorShape(colorShape)
                    .setPresets(presets!!)
                    .setAllowPresets(allowPresets)
                    .setAllowCustom(allowCustom)
                    .setShowAlphaSlider(showAlphaSlider)
                    .setShowColorShades(showColorShades)
                    .setColor(color)
                    .create()
            dialog.setColorPickerDialogListener(this@ColorPreferenceSupport)
            val activity = getPrefActivity(this)
            dialog.show(activity!!.fragmentManager, fragmentTag)
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)

        if (showDialog) {
            val activity = getPrefActivity(this)
            val fragment = activity?.fragmentManager?.findFragmentByTag(fragmentTag) as ColorPickerDialog?
            fragment?.setColorPickerDialogListener(this)
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val preview = holder.findViewById(R.id.cpv_preference_preview_color_panel) as ColorPanelView
        if (preview != null) {
            preview.color = color
        }
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            color = getPersistedInt(-0x1000000)
        } else {
            color = (defaultValue as Int?)!!
            persistInt(color)
        }
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInteger(index, Color.BLACK)
    }

    override fun onColorSelected(dialogId: Int, @ColorInt color: Int) {
        saveValue(color)
    }

    override fun onDialogDismissed(dialogId: Int) {
        // no-op
    }

    /**
     * Set the new color
     *
     * @param color The newly selected color
     */
    fun saveValue(@ColorInt color: Int) {
        this.color = color
        persistInt(this.color)
        notifyChanged()
        callChangeListener(color)
    }

    /**
     * The listener used for showing the [ColorPickerDialog].
     * Call [.saveValue] after the user chooses a color.
     * If this is set then it is up to you to show the dialog.
     *
     * @param listener The listener to show the dialog
     */
    fun setOnShowDialogListener(listener: OnShowDialogListener) {
        onShowDialogListener = listener
    }

    interface OnShowDialogListener {

        fun onShowColorPickerDialog(title: String, currentColor: Int)
    }

    companion object {

        private val SIZE_NORMAL = 0
        private val SIZE_LARGE = 1

        fun getPrefActivity(pref: Preference): Activity? {
            val c = pref.context
            if (c is ContextThemeWrapper) {
                if (c.baseContext is Activity)
                    return c.baseContext as Activity
            } else if (c is Activity)
                return c
            return null
        }
    }

}
