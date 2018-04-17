package com.benny.openlauncher.model

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.benny.openlauncher.R
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Created by BennyKok on 11/14/2017.
 */

class PopupIconLabelItem(val labelRes: Int, val iconRes: Int) : AbstractItem<PopupIconLabelItem, PopupIconLabelItem.ViewHolder>() {

    override fun getViewHolder(v: View?): ViewHolder = ViewHolder(v!!)

    override fun getType(): Int = R.id.id_adapter_popup_icon_label_item

    override fun getLayoutRes(): Int = R.layout.item_popup_icon_label

    override fun bindView(holder: ViewHolder?, payloads: MutableList<Any>?) {
        super.bindView(holder, payloads)

        holder?.labelView?.setText(labelRes)
        holder?.iconView?.setImageResource(iconRes)
    }

    override fun unbindView(holder: ViewHolder?) {
        super.unbindView(holder)

        holder?.labelView?.text = null
        holder?.iconView?.setImageDrawable(null)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView as CardView
        var labelView = itemView.findViewById<TextView>(R.id.item_popup_label)
        var iconView = itemView.findViewById<ImageView>(R.id.item_popup_icon)
    }

}