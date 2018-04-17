package com.benny.openlauncher.widget

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.AttrRes
import android.support.v7.widget.*
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.TextView
import com.benny.openlauncher.R
import com.benny.openlauncher.drawable.LauncherCircleDrawable
import com.benny.openlauncher.interfaces.AbstractApp
import com.benny.openlauncher.interfaces.AppUpdateListener
import com.benny.openlauncher.interfaces.SettingsManager
import com.benny.openlauncher.manager.Setup
import com.benny.openlauncher.model.IconLabelItem
import com.benny.openlauncher.model.Item
import com.benny.openlauncher.util.DragAction
import com.benny.openlauncher.util.Tool
import com.mikepenz.fastadapter.IItemAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import java.text.SimpleDateFormat
import java.util.*

class SearchBar : FrameLayout {

    val searchClock: TextView
    val switchButton: AppCompatImageView
    val searchButton: AppCompatImageView
    val searchInput: AppCompatEditText
    val searchRecycler: RecyclerView

    private val icon: LauncherCircleDrawable
    private val icon2: LauncherCircleDrawable

    private val searchCardContainer: FrameLayout
    private val adapter = FastItemAdapter<IconLabelItem>()
    private var callback: CallBack? = null
    private var expanded: Boolean = false
    private var searchInternetEnabled = true
    private var mode = Mode.DateAll
    private var searchClockTextSize = 28
    private var searchClockSubTextFactor = 0.5f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setSearchInternetEnabled(enabled: Boolean): SearchBar {
        searchInternetEnabled = enabled
        return this
    }

    fun setSearchClockTextSize(size: Int): SearchBar {
        searchClockTextSize = size
        if (searchClock != null) {
            searchClock!!.setTextSize(TypedValue.COMPLEX_UNIT_DIP, searchClockTextSize.toFloat())
        }
        return this
    }

    fun setSearchClockSubTextFactor(factor: Float): SearchBar {
        searchClockSubTextFactor = factor
        return this
    }

    fun setMode(mode: Mode): SearchBar {
        this.mode = mode
        return this
    }

    fun setCallback(callback: CallBack) {
        this.callback = callback
    }

    fun collapse(): Boolean {
        if (!expanded) {
            return false
        }
        searchButton.callOnClick()
        return !expanded
    }

    init {
        val dp1 = Tool.dp2px(1, context)
        val iconMarginOutside = dp1 * 16
        val iconMarginTop = dp1 * 13
        val searchTextHorizontalMargin = dp1 * 8
        val searchTextMarginTop = dp1 * 4
        val iconSize = dp1 * 24
        val iconPadding = dp1 * 6 // LauncherCircleDrawable uses 6dp as well!!

        searchClock = LayoutInflater.from(context).inflate(R.layout.view_search_clock, this, false) as TextView
        searchClock!!.setTextSize(TypedValue.COMPLEX_UNIT_DIP, searchClockTextSize.toFloat())
        val clockParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        clockParams.setMargins(iconMarginOutside, 0, 0, 0)
        clockParams.gravity = Gravity.START

        // && Setup.appSettings().isSearchGridListSwitchEnabled()

        icon2 = LauncherCircleDrawable(context, resources.getDrawable(if (Setup.appSettings().isSearchUseGrid) R.drawable.ic_apps_white_48dp else R.drawable.ic_view_list_white_24dp), Color.BLACK)
        switchButton = AppCompatImageView(context)
        switchButton.setImageDrawable(icon2)
        switchButton.setOnClickListener {
            Setup.appSettings().isSearchUseGrid = !Setup.appSettings().isSearchUseGrid
            updateSwitchIcon()
            updateRecyclerViewLayoutManager()
        }

        val switchButtonParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        switchButtonParams.setMargins(0, 0, iconSize + iconMarginOutside * 2, 0)
        switchButtonParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL

        searchButton = AppCompatImageView(context)
        searchInput = AppCompatEditText(context)

        icon = LauncherCircleDrawable(context, resources.getDrawable(R.drawable.ic_search_light_24dp), Color.BLACK)
        searchButton.setImageDrawable(icon)
        searchButton.setOnClickListener(OnClickListener {
            if (expanded && searchInput.text.isNotEmpty()) {
                searchInput.text.clear()
                return@OnClickListener
            }
            expanded = !expanded
            if (expanded) {
                expandInternal()
            } else {
                collapseInternal()
            }
        })
        val buttonParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        buttonParams.setMargins(0, iconMarginTop, iconMarginOutside, 0)
        buttonParams.gravity = Gravity.END

        searchCardContainer = FrameLayout(context)
        //searchCardContainer.setOverlayColor(resources.getColor(R.color.colorAccent))
        searchCardContainer.visibility = View.GONE
        searchCardContainer.setPadding(dp1 * 4, dp1 * 4, dp1 * 4, dp1 * 4)

//        val root = CoreHome.launcher!!.window.decorView.findViewById<ViewGroup>(android.R.id.content)
//        searchCardContainer.setupWith(root)
//                .windowBackground(root.background)
//                .blurAlgorithm(RenderScriptBlur(context))
//                .blurRadius(20f)

        searchInput.background = null
        searchInput.setHint(R.string.search_hint)
        searchInput.setHintTextColor(Color.WHITE)
        searchInput.setTextColor(Color.WHITE)
        searchInput.setSingleLine()
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (searchInput.text.isEmpty()) {
                    clearFilter()
                } else
                    adapter.filter(s)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        searchInput.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if ((event != null) && (event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    callback!!.onInternetSearch(searchInput.text.toString())
                    searchInput.text.clear()
                    return true
                }
                return false
            }
        })
        val inputCardParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //(switchButton != null ? iconMarginOutside + iconSize : 0) + searchTextHorizontalMargin
        //iconMarginOutside + iconSize + searchTextHorizontalMargin
        inputCardParams.setMargins(0, searchTextMarginTop, 0, 0)

        val inputParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        inputParams.setMargins(iconMarginOutside / 2, 0, 0, 0)

        searchCardContainer.addView(searchInput, inputParams)
        searchCardContainer.addView(switchButton, switchButtonParams)

        searchRecycler = RecyclerView(context)
        searchRecycler.overScrollMode = View.OVER_SCROLL_NEVER
        searchRecycler.itemAnimator = null
        searchRecycler.visibility = View.GONE
        searchRecycler.adapter = adapter
        searchRecycler.clipToPadding = false
        searchRecycler.setHasFixedSize(true)

        Setup.appLoader().addUpdateListener(AppUpdateListener { apps ->
            var apps = apps
            adapter.clear()
            if (Setup.appSettings().searchBarShouldShowHiddenApps) {
                apps = Setup.appLoader().getAllApps(context, true)
            }
            val items = ArrayList<IconLabelItem>()
            if (searchInternetEnabled) {
                items.add(IconLabelItem(context, R.drawable.ic_search_light_24dp, R.string.search_online)
                        .withIconGravity(if (Setup.appSettings().isSearchUseGrid) Gravity.TOP else Gravity.LEFT)
                        .withTextGravity(if (Setup.appSettings().isSearchUseGrid) Gravity.CENTER_HORIZONTAL else Gravity.RIGHT)
                        .withOnClickListener {
                            callback!!.onInternetSearch(searchInput.text.toString())
                            searchInput.text.clear()
                        }
                        .withTextColor(Color.WHITE)
                        .withDrawablePadding(context, 19)
                        .withBold(true)
                        .withMatchParent(true))
            }
            for (i in apps.indices) {
                val app = apps[i]
                items.add(IconLabelItem(context, app.iconProvider, app.label, 36)
                        .withIconGravity(if (Setup.appSettings().isSearchUseGrid) Gravity.TOP else Gravity.LEFT)
                        .withTextGravity(if (Setup.appSettings().isSearchUseGrid) Gravity.CENTER_HORIZONTAL else Gravity.CENTER_VERTICAL)
                        .withOnClickListener { v -> startApp(v.context, app, v) }
                        .withOnTouchListener(AppItemView.Builder.getOnTouchGetPosition(null, null))
                        .withOnLongClickListener(true, AppItemView.Builder.getLongClickDragAppListener(Item.newAppItem(app), DragAction.Action.SEARCH_RESULT, object : AppItemView.LongPressCallBack {
                            override fun readyForDrag(view: View): Boolean {
                                expanded = !expanded
                                collapseInternal()
                                return true
                            }

                            override fun afterDrag(view: View) {}
                        }))
                        .withTextColor(Color.WHITE)
                        .withMatchParent(true)
                        .withDrawablePadding(context, 8)
                        .withMaxTextLines(Setup.appSettings().searchLabelLines))
            }
            adapter.set(items)
            adapter.withOnLongClickListener(com.mikepenz.fastadapter.listeners.OnLongClickListener { v, adapter, item, position ->
                if (!searchInternetEnabled || position != 0) {
                    item.onLongClickListener.onLongClick(v)
                    return@OnLongClickListener true
                }
                false
            })
            clearFilter()
            false
        })
        adapter.itemFilter.withFilterPredicate(IItemAdapter.Predicate { item, constraint ->
            updateItemGravity(if (item.label == context.getString(R.string.search_online)) 0 else 1, item)
            if (item.label == context.getString(R.string.search_online))
                return@Predicate true
            val s = constraint!!.toString()
            if (s.isBlank())
                return@Predicate true
            else
                return@Predicate item.label.toLowerCase().contains(s.toLowerCase())
        })
        updateRecyclerViewLayoutManager()

        val recyclerParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        addView(searchClock, clockParams)
        addView(searchRecycler, recyclerParams)
        addView(searchCardContainer, inputCardParams)
        addView(searchButton, buttonParams)

        searchInput.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                searchInput.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val marginTop = Tool.dp2px(56, context) + searchInput.height
                val marginBottom = Desktop.bottomInset
                recyclerParams.setMargins(0, marginTop, 0, marginBottom)
                recyclerParams.height = (parent as View).height - marginTop - marginBottom / 2
                searchRecycler.layoutParams = recyclerParams
                searchRecycler.setPadding(0, 0, 0, (marginBottom * 1.5f).toInt())
            }
        })
    }

    private fun clearFilter() {
        adapter.filter(" ")
    }

    private fun collapseInternal() {
        if (callback != null) {
            callback!!.onCollapse()
        }
        icon!!.setIcon(resources.getDrawable(R.drawable.ic_search_light_24dp))
        Tool.visibleViews(ANIM_TIME, searchClock!!)
        Tool.goneViews(ANIM_TIME, searchCardContainer, searchRecycler, switchButton)
        searchInput.text.clear()
    }

    private fun expandInternal() {
        if (callback != null) {
            callback!!.onExpand()
        }
        if (Setup.appSettings().isResetSearchBarOnOpen) {
            val lm = searchRecycler.layoutManager
            if (lm is LinearLayoutManager) {
                (searchRecycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
            } else if (lm is GridLayoutManager) {
                (searchRecycler.layoutManager as GridLayoutManager).scrollToPositionWithOffset(0, 0)
            }
        }
        icon!!.setIcon(resources.getDrawable(R.drawable.ic_clear_white_24dp))
        Tool.visibleViews(ANIM_TIME, searchCardContainer, searchRecycler, switchButton)
        Tool.goneViews(ANIM_TIME, searchClock!!)
    }

    private fun updateSwitchIcon() {
        icon2.setIcon(resources.getDrawable(if (Setup.appSettings().isSearchUseGrid) R.drawable.ic_apps_white_48dp else R.drawable.ic_view_list_white_24dp))
    }

    private fun updateRecyclerViewLayoutManager() {
        for ((i, item) in adapter.adapterItems.withIndex()) {
            updateItemGravity(i, item)
        }
        adapter.notifyAdapterDataSetChanged()

        val gridSize = if (Setup.appSettings().isSearchUseGrid) Setup.appSettings().searchGridSize else 1
        if (gridSize == 1) {
            searchRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        } else {
            searchRecycler.layoutManager = GridLayoutManager(context, gridSize, GridLayoutManager.VERTICAL, false)
        }
        searchRecycler.layoutManager.isAutoMeasureEnabled = false
    }

    private fun updateItemGravity(position: Int, item: IconLabelItem) {
        if (position == 0) {
            item.withTextGravity(if (Setup.appSettings().isSearchUseGrid) Gravity.CENTER_HORIZONTAL else Gravity.RIGHT)
        } else {
            item.withTextGravity(if (Setup.appSettings().isSearchUseGrid) Gravity.CENTER_HORIZONTAL else Gravity.CENTER_VERTICAL)
        }
        item.withIconGravity(if (Setup.appSettings().isSearchUseGrid) Gravity.TOP else Gravity.LEFT)
    }

    protected fun startApp(context: Context, app: AbstractApp, view: View) {
        Tool.startApp(context, app, view)
    }

    fun updateClock() {
        val settingsManager: SettingsManager =  Setup.appSettings()
        if (!settingsManager.isSearchBarTimeEnabled) {
            searchClock!!.text = ""
            return
        }
        searchClock!!.setTextColor(settingsManager.desktopDateTextColor)
        val calendar = Calendar.getInstance(Locale.getDefault())


        var sdf: SimpleDateFormat? = mode.sdf
        val mode = settingsManager.desktopDateMode
        if (mode >= 0 && mode < Mode.count()){
            sdf = Mode.getById(mode).sdf
            if (mode == 0){
                sdf = settingsManager.userDateFormat
            }
        }

        if (sdf == null) {
            sdf = Setup.appSettings().userDateFormat
        }
        val text = sdf!!.format(calendar.time)
        val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val span = SpannableString(text)
        span.setSpan(RelativeSizeSpan(searchClockSubTextFactor), lines[0].length + 1, lines[0].length + 1 + lines[1].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        searchClock!!.text = span
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            setPadding(paddingLeft, insets.systemWindowInsetTop + Tool.dp2px(10, context), paddingRight, paddingBottom)
        }
        return insets
    }

    enum class Mode private constructor(id: Int, internal var sdf: SimpleDateFormat?) {
        DateAll(1, SimpleDateFormat("MMMM dd'\n'EEEE',' yyyy", Locale.getDefault())),
        DateNoYearAndTime(2, SimpleDateFormat("MMMM dd'\n'HH':'mm", Locale.getDefault())),
        DateAllAndTime(3, SimpleDateFormat("MMMM dd',' yyyy'\n'HH':'mm", Locale.getDefault())),
        TimeAndDateAll(4, SimpleDateFormat("HH':'mm'\n'MMMM dd',' yyyy", Locale.getDefault())),
        Custom(0, null);

        var id: Int = 0
            internal set

        init {
            this.id = id
        }

        companion object {

            fun getById(id: Int): Mode {
                for (i in 0 until values().size) {
                    if (values()[i].id == id)
                        return values()[i]
                }
                throw RuntimeException("ID not found!")
            }

            fun getByIndex(index: Int): Mode {
                return values()[index]
            }

            fun getIndex(id: Int): Int {
                for (i in 0 until values().size) {
                    if (values()[i].id == id) {
                        return i
                    }
                }
                throw RuntimeException("ID not found!")
            }
            fun count(): Int {
                return values().size;
            }
        }
    }

    interface CallBack {
        fun onInternetSearch(string: String)

        fun onExpand()

        fun onCollapse()
    }

    companion object {

        private val ANIM_TIME: Long = 200
    }
}
