package com.benny.openlauncher.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.interfaces.AppUpdateListener;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.DragHandler;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.CircleDrawable;
import com.benny.openlauncher.viewutil.IconLabelItem;
import com.benny.openlauncher.weather.MultiClickListener;
import com.benny.openlauncher.weather.WeatherLocation;
import com.benny.openlauncher.weather.WeatherLocationAdapter;
import com.benny.openlauncher.weather.WeatherResult;
import com.benny.openlauncher.weather.WeatherService;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchBar extends FrameLayout {
    private static Logger LOG = LoggerFactory.getLogger("SearchBar");

    private static final long ANIM_TIME = 200;
    public TextView _searchClock;
    public AppCompatImageView _switchButton;
    public AppCompatImageView _searchButton;
    public ArrayList<AppCompatButton> _weatherIcons = new ArrayList<>();
    protected Integer _weatherIconSize;
    public AppCompatEditText _searchInput;
    public RecyclerView _searchRecycler;
    private CircleDrawable _icon;
    private CardView _searchCardContainer;
    private FastItemAdapter<IconLabelItem> _adapter = new FastItemAdapter<>();
    private CallBack _callback;
    private boolean _expanded;
    private int _searchClockTextSize = 28;
    private float _searchClockSubTextFactor = 0.5f;
    private int bottomInset;

    private int _iconMarginOutside;
    private int _iconMarginTop;
    private int _searchTextMarginTop;
    private int _iconSize;
    private int _iconPadding;

    private HashMap<Integer, DateTimeFormatter> _clockModes = new HashMap<>(4);
    private Integer _clockFormatterIndex = -1;
    private DateTimeFormatter _clockFormatter = null;

    // Weather AutoComplete support
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private WeatherLocationAdapter _locationAdapter;
    private Handler handler;

    public SearchBar(@NonNull Context context) {
        super(context);
        init();
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCallback(CallBack callback) {
        _callback = callback;
    }

    public boolean collapse() {
        if (!_expanded) {
            return false;
        }
        _searchButton.callOnClick();
        return !_expanded;
    }

    private void init() {
        int dp1 = Tool.dp2px(1);
        _iconMarginOutside = dp1 * 16;
        _iconMarginTop = dp1 * 14;
        _searchTextMarginTop = dp1 * 4;
        _iconSize = dp1 * 24;
        _iconPadding = dp1 * 6;

        // These have to match the Preferences Array, but without item 0 as that is a custom option which can be changed:
        //   <item>@string/custom</item>
        //   <item>February 17\nSaturday, 2018</item>
        //   <item>February 17\n15:48</item>
        //   <item>February 17, 2018\n15:48</item>
        //   <item>15:48\nFebruary 17, 2018</item>
        _clockModes.put(1, DateTimeFormatter.ofPattern("MMMM dd\nEEEE, yyyy", Locale.getDefault()));
        _clockModes.put(2, DateTimeFormatter.ofPattern("MMMM dd\nHH:mm", Locale.getDefault()));
        _clockModes.put(3, DateTimeFormatter.ofPattern("MMMM dd, yyyy\nHH:mm", Locale.getDefault()));
        _clockModes.put(4, DateTimeFormatter.ofPattern("HH:mm\nMMMM dd, yyyy", Locale.getDefault()));

        _searchClock = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.view_search_clock, this, false);
        _searchClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, _searchClockTextSize);
        LayoutParams clockParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clockParams.setMargins(_iconMarginOutside, dp1 * 4, 0, dp1 * 4);
        clockParams.gravity = Gravity.START;

        _switchButton = new AppCompatImageView(getContext());
        _switchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Setup.appSettings().setSearchUseGrid(!Setup.appSettings().getSearchUseGrid());
                updateSwitchIcon();
                updateRecyclerViewLayoutManager();
            }
        });
        _switchButton.setVisibility(View.GONE);
        _switchButton.setPadding(0, _iconPadding, 0, _iconPadding);
        updateSwitchIcon();

        LayoutParams switchButtonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switchButtonParams.setMargins(_iconMarginOutside / 2, 0, 0, 0);
        switchButtonParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;

        if (isInEditMode()) return;

        _icon = new CircleDrawable(getContext(), getResources().getDrawable(R.drawable.ic_search), Color.WHITE, Color.BLACK, 100);
        _searchButton = new AppCompatImageView(getContext());
        _searchButton.setImageDrawable(_icon);
        _searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_expanded && _searchInput.getText().length() > 0) {
                    _searchInput.getText().clear();
                    return;
                }
                _expanded = !_expanded;
                if (_expanded) {
                    expandInternal();
                } else {
                    collapseInternal();
                }
            }
        });

        LayoutParams buttonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, _iconMarginTop, _iconMarginOutside, 0);
        buttonParams.gravity = Gravity.END;

        _searchCardContainer = new CardView(getContext());
        _searchCardContainer.setCardBackgroundColor(Color.TRANSPARENT);
        _searchCardContainer.setVisibility(View.GONE);
        _searchCardContainer.setRadius(0);
        _searchCardContainer.setCardElevation(0);
        _searchCardContainer.setContentPadding(dp1 * 4, dp1 * 4, dp1 * 4, dp1 * 4);

        _searchInput = new AppCompatEditText(getContext());
        _searchInput.setBackground(null);
        _searchInput.setHint(R.string.search_hint);
        _searchInput.setHintTextColor(Color.WHITE);
        _searchInput.setTextColor(Color.WHITE);
        _searchInput.setSingleLine();
        _searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                _adapter.filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        _searchInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event != null) && (event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    _callback.onInternetSearch(_searchInput.getText().toString());
                    _searchInput.getText().clear();
                    return true;
                }
                return false;
            }
        });
        LayoutParams inputCardParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputCardParams.setMargins(0, _searchTextMarginTop, 0, 0);

        LayoutParams inputParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(_iconMarginOutside + _iconSize, 0, 0, 0);

        _searchCardContainer.addView(_switchButton, switchButtonParams);
        _searchCardContainer.addView(_searchInput, inputParams);

        initRecyclerView();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                SearchBar.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                createWeatherButtons();
            }
        });

        Setup.appLoader().addUpdateListener(new AppUpdateListener() {
            @Override
            public boolean onAppUpdated(List<App> apps) {
                _adapter.clear();
                if (Setup.appSettings().getSearchBarShouldShowHiddenApps()) {
                    apps = Setup.appLoader().getAllApps(getContext(), true);
                }
                List<IconLabelItem> items = new ArrayList<>();
                for (int i = 0; i < apps.size(); i++) {
                    final App app = apps.get(i);
                    items.add(new IconLabelItem(app.getIcon(), app.getLabel())
                            .withIconSize(50)
                            .withTextColor(Color.WHITE)
                            .withIsAppLauncher(true)
                            .withIconPadding(8)
                            .withOnClickAnimate(false)
                            .withTextGravity(Setup.appSettings().getSearchUseGrid() ? Gravity.CENTER : Gravity.CENTER_VERTICAL)
                            .withIconGravity(Setup.appSettings().getSearchUseGrid() ? Gravity.TOP : Gravity.START)
                            .withOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Tool.startApp(v.getContext(), app, null);
                                }
                            })
                            .withOnLongClickListener(DragHandler.getLongClick(Item.newAppItem(app), DragAction.Action.SEARCH, null)));
                }
                _adapter.set(items);

                return false;
            }
        });
        _adapter.getItemFilter().withFilterPredicate(new IItemAdapter.Predicate<IconLabelItem>() {
            @Override
            public boolean filter(IconLabelItem item, CharSequence constraint) {
                if (constraint.length() == 0) {
                    return true;
                }

                String s = constraint.toString().toLowerCase();
                if (Setup.appSettings().getSearchBarStartsWith()) {
                    if (item._label.toLowerCase().startsWith(s)) {
                        return true;
                    }
                } else {
                    if (item._label.toLowerCase().contains(s)) {
                        return true;
                    }
                }

                return false;
            }
        });

        final LayoutParams recyclerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addView(_searchClock, clockParams);
        addView(_searchRecycler, recyclerParams);
        addView(_searchCardContainer, inputCardParams);
        addView(_searchButton, buttonParams);

        _searchInput.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                _searchInput.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int marginTop = Tool.dp2px(60);
                recyclerParams.setMargins(0, marginTop, 0, 0);
                _searchRecycler.setLayoutParams(recyclerParams);
                _searchRecycler.setPadding(0, 0, 0, (int) (bottomInset * 1.5));
            }
        });
    }

    private void collapseInternal() {
        if (_callback != null) {
            _callback.onCollapse();
        }

        _icon.setIcon(getResources().getDrawable(R.drawable.ic_search));

        for (AppCompatButton icon : _weatherIcons) {
            Tool.visibleViews(0, icon);
        }

        Tool.visibleViews(ANIM_TIME, _searchClock);
        Tool.goneViews(ANIM_TIME, _searchCardContainer, _searchRecycler, _switchButton);

        _searchInput.getText().clear();
    }

    private void expandInternal() {
        if (_callback != null) {
            _callback.onExpand();
        }

        _icon.setIcon(getResources().getDrawable(R.drawable.ic_clear));

        for (AppCompatButton icon : _weatherIcons) {
            Tool.goneViews(0, icon);
        }

        Tool.visibleViews(ANIM_TIME, _searchCardContainer, _searchRecycler, _switchButton);
        Tool.goneViews(ANIM_TIME, _searchClock);
    }

    private void updateSwitchIcon() {
        _switchButton.setImageResource(Setup.appSettings().getSearchUseGrid() ? R.drawable.ic_view_grid_white : R.drawable.ic_view_list_white);
    }

    private void updateRecyclerViewLayoutManager() {
        int gridSize = Setup.appSettings().getSearchUseGrid() ? 4 : 1;
        if (gridSize == 1) {
            _searchRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            updateList(Gravity.START, Gravity.CENTER_VERTICAL);
        } else {
            _searchRecycler.setLayoutManager(new GridLayoutManager(getContext(), gridSize, GridLayoutManager.VERTICAL, false));
            updateList(Gravity.TOP, Gravity.CENTER);
        }
        _searchRecycler.getLayoutManager().setAutoMeasureEnabled(false);
    }

    private void updateList(int iconGravity, int textGravity) {
        List<IconLabelItem> apps = _adapter.getAdapterItems();
        for (IconLabelItem app : apps) {
            app.setIconGravity(iconGravity);
            app.setTextGravity(textGravity);
        }
    }

    protected void initRecyclerView() {
        _searchRecycler = new RecyclerView(getContext());
        _searchRecycler.setItemAnimator(null);
        _searchRecycler.setVisibility(View.GONE);
        _searchRecycler.setAdapter(_adapter);
        _searchRecycler.setClipToPadding(false);
        _searchRecycler.setHasFixedSize(true);
        updateRecyclerViewLayoutManager();
    }

    public AppCompatImageView getSearchButton() {
        return _searchButton;
    }

    public void updateClock() {
        AppSettings appSettings = AppSettings.get();
        if (_searchClock != null) {
            _searchClock.setTextColor(appSettings.getDesktopDateTextColor());
        }

        ZonedDateTime now = ZonedDateTime.now();
        _clockFormatter = getSearchBarClockFormat(Setup.appSettings().getDesktopDateMode());

        String text = now.format(_clockFormatter);
        String[] lines = text.split("\n");
        Spannable span = new SpannableString(text);
        span.setSpan(new RelativeSizeSpan(_searchClockSubTextFactor), lines[0].length() + 1, lines[0].length() + 1 + lines[1].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        _searchClock.setText(span);
    }

    private void findCityToAdd() {
        HomeActivity launcher = HomeActivity.Companion.getLauncher();
        final AlertDialog.Builder alert = new AlertDialog.Builder(launcher);
        alert.setTitle(launcher.getString(R.string.weather_service_add_location));

        _locationAdapter = new WeatherLocationAdapter(launcher, android.R.layout.simple_dropdown_item_1line);

        final AppCompatAutoCompleteTextView input = new AppCompatAutoCompleteTextView(launcher);
        input.setMaxLines(1);
        input.setThreshold(3);
        input.setSingleLine(true);
        input.setAdapter(_locationAdapter);

        input.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        WeatherLocation loc = _locationAdapter.getObject(position);
                        input.setText(loc.toString());
                    }
                });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(input.getText())) {
                        launcher._weatherService.getLocationsByName(input.getText().toString(),
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        launcher._weatherService.getLocationsFromResponse(response);
                                        _locationAdapter.setData(new ArrayList<>(WeatherLocation._locations.values()));
                                        _locationAdapter.notifyDataSetChanged();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        LOG.error("Error getting Location list from Weather Service: {}", error.networkResponse);
                                    }
                                });
                    }
                }
                return false;
            }
        });

        alert.setView(input, 32, 0, 32, 0);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppSettings settings = AppSettings.get();

                WeatherLocation loc = WeatherLocation.parse(input.getText().toString());
                settings.setWeatherCity(loc);
                settings.addWeatherLocations(loc);

                int toastMessageId = settings.getWeatherForecastByHour() ? R.string.weather_service_hourly : R.string.weather_service_daily;
                Tool.toast(getContext(), String.format(getContext().getString(toastMessageId), loc.getName()));

                launcher._weatherService.getWeatherForLocation();
                dialog.dismiss();
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            // Use Location Services.
            AppSettings.get().setWeatherCity(null);
        } else if (item.getItemId() == 99) {
            // Pop up the add Location Dialog.
            findCityToAdd();
            return true;
        } else {
            AppSettings.get().setWeatherCity(WeatherLocation.getByName(item.toString()));
        }

        HomeActivity.Companion.getLauncher().initWeatherIfRequired();
        return true;

    }

    protected void createWeatherButtons() {
        int[] coords = new int[2];
        _searchButton.getLocationOnScreen(coords);

        int leftPosition = _searchClock.getMeasuredWidth();
        int rightPosition = coords[0] - _iconMarginOutside;

        int totalWidth = rightPosition - leftPosition;

        _weatherIconSize = (int) (_searchClock.getMeasuredHeight() * 0.5f);

        int numberOfIcons = totalWidth / (_weatherIconSize + _iconMarginOutside);

        for (int i = 0; i < numberOfIcons; i++) {
            AppCompatButton btn =new AppCompatButton(getContext());
            _weatherIcons.add(btn);

            LayoutParams iconParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            iconParams.setMargins(leftPosition, 0, 0, 0);

            btn.setLayoutParams(iconParams);
            leftPosition += _weatherIconSize + _iconMarginOutside;
        }

        setButtonCallbacks();
    }

    protected void setButtonCallbacks() {
        for (AppCompatButton icon : _weatherIcons) {
            HomeActivity.Companion.getLauncher().registerForContextMenu(icon);

            icon.setBackgroundColor(Color.TRANSPARENT);
            icon.setTextColor(AppSettings.get().getDesktopDateTextColor());

            icon.setOnClickListener(new MultiClickListener() {
                @Override
                public void onSingleClick(View v) {
                    super.onSingleClick(v);
                    AppSettings settings = AppSettings.get();
                    boolean hourly = settings.getWeatherForecastByHour();

                    settings.setWeatherForecastByHour(!hourly);

                    int toastMessageId = !hourly ? R.string.weather_service_hourly : R.string.weather_service_daily;

                    try {
                        WeatherLocation loc = settings.getWeatherCity();
                        String locationName;

                        if (loc == null) {
                            locationName = getContext().getString(R.string.weather_service_current_location);
                        } else {
                            locationName =  loc.getName();
                        }
                        Tool.toast(getContext(), String.format(getContext().getString(toastMessageId), locationName));
                    } catch (Exception e) {
                        LOG.error("Failed to get location: {}", e);
                    }
                    HomeActivity.Companion.getLauncher().initWeatherIfRequired();
                }

                public void onDoubleClick(View v) {
                    super.onDoubleClick(v);
                    HomeActivity._weatherService.openWeatherApp();
                }
            });

            icon.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.setHeaderTitle(getContext().getString(R.string.weather_service_choose_location));
                    //MenuCompat.setGroupDividerEnabled(menu, true);

                    menu.add(0, 0, 0, R.string.weather_service_use_network_location);

                    int i = 1;
                    // Stored Locations first
                    ArrayList<WeatherLocation> storedLocations = AppSettings.get().getWeatherLocations();
                    for (WeatherLocation loc : storedLocations) {
                        menu.add(1, i, i, loc.toString());
                        i++;
                    }

                    // Then locations associated with the current location (when using Location Services)
                    if (AppSettings.get().getWeatherCity() == null) {
                        for (String name : WeatherLocation._locations.keySet()) {
                            menu.add(2, i, i, WeatherLocation.getByName(name).getName());
                            i++;
                        }
                    }

                    menu.add(3, 99, 99, R.string.weather_service_add_location);
                }
            });
        }
    }

    public void updateWeather(WeatherResult weather) {
        LOG.debug("updateWeather() -> {}", weather);

        for (int i = 0; i < _weatherIcons.size(); i++) {
            Pair<Double, Drawable> forecast = weather.getForecast(i, _weatherIconSize);

            // May happen if we get an error from the Weather Service
            if (forecast == null) {
                break;
            }

            AppCompatButton icon = _weatherIcons.get(i);
            icon.setCompoundDrawablesWithIntrinsicBounds(null, forecast.second, null, null);
            icon.setText(Double.toString(forecast.first));

            if (icon.getParent() == null) {
                addView(icon);
            }
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            bottomInset = insets.getSystemWindowInsetBottom();
            setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
            return insets;
        }
        return insets;
    }

    public DateTimeFormatter getSearchBarClockFormat(Integer id) {
        if (_clockFormatterIndex != id && id > 0) {
            if (_clockModes.containsKey(id)) {
                return _clockModes.get(id);
            }
        }

        return Setup.appSettings().getUserDateFormat();
    }

    public interface CallBack {
        void onInternetSearch(String string);

        void onExpand();

        void onCollapse();
    }
}
