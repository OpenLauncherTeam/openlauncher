package com.benny.openlauncher.preference

// MIT Licensed, taken from
// https://github.com/Alobar/AndroidPreferenceTest/blob/master/alobar-preference/src/main/java/alobar/preference/NumberPickerPreference.java

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.preference.Preference
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.afollestad.materialdialogs.MaterialDialog
import com.benny.openlauncher.R

class NumberPickerPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = android.R.attr.dialogPreferenceStyle) : Preference(context, attrs, defStyleAttr) {
    private val minValue: Int
    private val maxValue: Int
    private val wrapSelectorWheel: Boolean

    private lateinit var picker: NumberPicker

    var value: Int = 0
        set(value) {
            field = value
            persistInt(this.value)
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference)

        minValue = a.getInteger(R.styleable.NumberPickerPreference_minValue, DEFAULT_MIN_VALUE)
        maxValue = a.getInteger(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAX_VALUE)
        wrapSelectorWheel = a.getBoolean(R.styleable.NumberPickerPreference_wrapSelectorWheel, DEFAULT_WRAP_SELECTOR_WHEEL)
        a.recycle()
    }

    override fun onClick() {
        super.onClick()

        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER

        picker = NumberPicker(context)
        picker.layoutParams = layoutParams

        val dialogView = FrameLayout(context)
        dialogView.addView(picker)

        picker.minValue = minValue
        picker.maxValue = maxValue
        picker.wrapSelectorWheel = wrapSelectorWheel
        picker.value = value

        MaterialDialog.Builder(context)
                .title(title)
                .customView(dialogView, false)
                .positiveText(R.string.ok)
                .onPositive { _, _ ->
                    picker.clearFocus()
                    val newValue = picker.value
                    if (callChangeListener(newValue))
                        value = newValue
                }
                .negativeText(R.string.cancel)
                .show()
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any = a!!.getInt(index, minValue)

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value = if (restorePersistedValue) getPersistedInt(minValue) else defaultValue as Int
    }

    companion object {
        private val DEFAULT_MAX_VALUE = 100
        private val DEFAULT_MIN_VALUE = 0
        private val DEFAULT_WRAP_SELECTOR_WHEEL = true
    }
}