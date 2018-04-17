package com.benny.openlauncher.core.model

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import com.benny.openlauncher.core.activity.CoreHome
import com.benny.openlauncher.core.interfaces.AbstractApp
import com.benny.openlauncher.core.interfaces.LabelProvider
import com.benny.openlauncher.core.manager.Setup
import com.benny.openlauncher.core.util.BaseIconProvider
import com.benny.openlauncher.core.util.Tool
import java.util.*

data class Item(
        var type: Type? = null,
        var iconProvider: BaseIconProvider? = null,
        var x: Int = 0,
        var y: Int = 0,
        //Needed for folder to optimize the folder open position
        var locationInLauncher: Int = 0,
        // intent for shortcuts and apps
        var intent: Intent? = null,
        // list of items for groups
        var items: MutableList<Item> = ArrayList(),
        // int value for launcher action
        var actionValue: Int = 0,
        // widget specific values
        var widgetValue: Int = 0,
        var spanX: Int = 1,
        var spanY: Int = 1,
        // all items need these values
        private var idValue: Int = 0,
        private var name: String = ""
) : LabelProvider, Parcelable {

    val id: Int?
        get() = idValue

    val groupItems: MutableList<Item>
        get() = items

    init {
        if (id == 0) {
            val random = Random()
            idValue = random.nextInt()
        }
    }

    constructor(parcel: Parcel) : this() {
        idValue = parcel.readInt()
        type = Type.valueOf(parcel.readString())
        name = parcel.readString()
        x = parcel.readInt()
        y = parcel.readInt()
        when (type) {
            Item.Type.APP, Item.Type.SHORTCUT -> intent = Tool.getIntentFromString(parcel.readString())
            Item.Type.GROUP -> {
                val labels = ArrayList<String>()
                parcel.readStringList(labels)
                items = ArrayList()
                for (s in labels) {
                    items.add(CoreHome.db.getItem(Integer.parseInt(s)))
                }
            }
            Item.Type.ACTION -> actionValue = parcel.readInt()
            Item.Type.WIDGET -> {
                widgetValue = parcel.readInt()
                spanX = parcel.readInt()
                spanY = parcel.readInt()
            }
        }
        locationInLauncher = parcel.readInt()

        if (Setup.appSettings().enableImageCaching()) {
            iconProvider = Setup.imageLoader().createIconProvider(Tool.getIcon(CoreHome.launcher!!, Integer.toString(idValue)))
        } else {
            when (type) {
                Item.Type.APP, Item.Type.SHORTCUT -> {
                    val app = Setup.appLoader().findItemApp(this)
                    iconProvider = app?.iconProvider
                }
                else -> {
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        val itemObject = other as Item?
        return other != null && this.idValue == itemObject!!.idValue
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(idValue)
        out.writeString(type.toString())
        out.writeString(name)
        out.writeInt(x)
        out.writeInt(y)
        when (type) {
            Item.Type.APP, Item.Type.SHORTCUT -> out.writeString(Tool.getIntentAsString(this.intent))
            Item.Type.GROUP -> {
                val labels = ArrayList<String>()
                for (i in items) {
                    labels.add(Integer.toString(i.idValue))
                }
                out.writeStringList(labels)
            }
            Item.Type.ACTION -> out.writeInt(actionValue)
            Item.Type.WIDGET -> {
                out.writeInt(widgetValue)
                out.writeInt(spanX)
                out.writeInt(spanY)
            }
        }
        out.writeInt(locationInLauncher)
    }

    fun reset() {
        val random = Random()
        idValue = random.nextInt()
    }

    fun setItemId(id: Int) {
        idValue = id
    }

    override fun getLabel(): String = name

    fun setLabel(label: String) {
        this.name = label
    }

    override fun hashCode(): Int = idValue

    enum class Type {
        APP,
        SHORTCUT,
        GROUP,
        ACTION,
        WIDGET
    }

    companion object {

        val LOCATION_DESKTOP = 0
        val LOCATION_DOCK = 1

        @JvmStatic
        val CREATOR = object : Parcelable.Creator<Item> {
            override fun createFromParcel(parcel: Parcel): Item = Item(parcel)
            override fun newArray(size: Int): Array<Item?> = arrayOfNulls(size)
        }

        @JvmStatic
        fun newAppItem(app: AbstractApp): Item {
            val item = Item()
            item.type = Type.APP
            item.name = app.label
            item.iconProvider = app.iconProvider
            item.intent = toIntent(app)
            return item
        }

        @JvmStatic
        fun newShortcutItem(intent: Intent, icon: Drawable, name: String): Item {
            val item = Item()
            item.type = Type.SHORTCUT
            item.name = name
            item.iconProvider = Setup.imageLoader().createIconProvider(icon)
            item.spanX = 1
            item.spanY = 1
            item.intent = intent
            return item
        }

        @JvmStatic
        fun newGroupItem(): Item {
            val item = Item()
            item.type = Type.GROUP
            item.name = ""
            item.spanX = 1
            item.spanY = 1
            item.items = ArrayList()
            return item
        }

        @JvmStatic
        fun newActionItem(action: Int): Item {
            val item = Item()
            item.type = Type.ACTION
            item.spanX = 1
            item.spanY = 1
            item.actionValue = action
            return item
        }

        @JvmStatic
        fun newWidgetItem(widgetValue: Int): Item {
            val item = Item()
            item.type = Type.WIDGET
            item.widgetValue = widgetValue
            item.spanX = 1
            item.spanY = 1
            return item
        }

        @JvmStatic
        private fun toIntent(app: AbstractApp): Intent {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setClassName(app.packageName, app.className)
            return intent
        }
    }
}
