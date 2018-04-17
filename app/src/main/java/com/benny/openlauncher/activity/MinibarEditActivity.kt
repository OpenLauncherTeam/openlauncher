package com.benny.openlauncher.activity

import android.app.Activity
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.benny.openlauncher.R
import com.benny.openlauncher.util.AppSettings
import com.benny.openlauncher.util.LauncherAction
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_minibar_edit.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class MinibarEditActivity : ThemeActivity(), ItemTouchCallback {

    private var adapter: FastItemAdapter<Item>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_minibar_edit)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(AppSettings.get().primaryColor)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        setTitle(R.string.minibar)

        adapter = FastItemAdapter()

        val touchCallback = SimpleDragCallback(this)
        val touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        var i = 0
        val minibarArrangement = AppSettings.get().minibarArrangement
        for (act in minibarArrangement) {
            val item = LauncherAction.getActionItemFromString(act.substring(1))
            adapter!!.add(Item(i.toLong(), item, act[0] == '0'))
            i++
        }

        val minBarEnable = AppSettings.get().minibarEnable
        enableSwitch!!.isChecked = minBarEnable
        enableSwitch!!.setText(if (minBarEnable) R.string.on else R.string.off)
        enableSwitch!!.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.setText(if (isChecked) R.string.on else R.string.off)
            AppSettings.get().minibarEnable = isChecked
            if (CoreHome.launcher != null) {
                CoreHome.launcher?.drawer_layout?.closeDrawers()
                CoreHome.launcher?.drawer_layout?.setDrawerLockMode(if (isChecked) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        setResult(Activity.RESULT_OK)
    }

    override fun onPause() {
        val minibarArrangement = ArrayList<String>()
        for (item in adapter!!.adapterItems) {
            if (item.enable) {
                minibarArrangement.add("0" + item.item.label.toString())
            } else
                minibarArrangement.add("1" + item.item.label.toString())
        }
        AppSettings.get().minibarArrangement = minibarArrangement
        super.onPause()
    }

    override fun onStop() {
        Home.launcher?.initMinibar()
        super.onStop()
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        Collections.swap(adapter!!.adapterItems, oldPosition, newPosition)
        adapter!!.notifyAdapterDataSetChanged()
        return false
    }

    override fun itemTouchDropped(i: Int, i1: Int) {}

    class Item(val id: Long, val item: LauncherAction.ActionDisplayItem, var enable: Boolean) : AbstractItem<Item, Item.ViewHolder>() {
        private var edited: Boolean = false

        override fun getType(): Int = 0

        override fun getLayoutRes(): Int = R.layout.item_minibar_edit

        override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

        override fun bindView(holder: ViewHolder, payloads: List<*>?) {
            holder.tv.text = item.label.toString()
            holder.tv2.text = item.description
            holder.iv.setImageResource(item.icon)
            holder.cb.isChecked = enable
            holder.cb.setOnCheckedChangeListener { _, b ->
                edited = true
                enable = b
            }
            super.bindView(holder, payloads)
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tv: TextView = itemView.findViewById(R.id.tv)
            var tv2: TextView = itemView.findViewById(R.id.tv2)
            var iv: ImageView = itemView.findViewById(R.id.iv)
            var cb: CheckBox = itemView.findViewById(R.id.cb)
        }
    }
}