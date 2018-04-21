package com.benny.openlauncher.activity.homeparts;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.CalendarDropDownView;
import com.benny.openlauncher.widget.SearchBar;

public class HpSearchBar implements SearchBar.CallBack, View.OnClickListener {
    private Home _home;
    private SearchBar _searchBar;
    private CalendarDropDownView _calendarDropDownView;

    public HpSearchBar(Home home, SearchBar searchBar, CalendarDropDownView calendarDropDownView) {
        _home = home;
        _searchBar = searchBar;
        _calendarDropDownView = calendarDropDownView;
    }


    public void initSearchBar() {
        _searchBar.setCallback(this);
        _searchBar._searchClock.setOnClickListener(this);
        _home.updateSearchClock();
    }

    @Override
    public void onInternetSearch(String string) {
        Intent intent = new Intent();

        if (Tool.isIntentActionAvailable(_home.getApplicationContext(), Intent.ACTION_WEB_SEARCH) && !Setup.appSettings().getSearchBarForceBrowser()) {
            intent.setAction(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, string);
        } else {
            String baseUri = Setup.appSettings().getSearchBarBaseURI();
            String searchUri = baseUri.contains("{query}") ? baseUri.replace("{query}", string) : (baseUri + string);

            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(searchUri));
        }

        try {
            _home.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onExpand() {
        _home.clearRoomForPopUp();
        _home.dimBackground();

        _searchBar._searchInput.setFocusable(true);
        _searchBar._searchInput.setFocusableInTouchMode(true);
        _searchBar._searchInput.post(new Runnable() {
            @Override
            public void run() {
                _searchBar._searchInput.requestFocus();
            }
        });

        Tool.showKeyboard(_home, _searchBar._searchInput);
    }

    @Override
    public void onCollapse() {
        _home.getDesktop().postDelayed(new Runnable() {
            @Override
            public void run() {
                _home.unClearRoomForPopUp();
            }
        }, 100);

        _home.unDimBackground();
        _searchBar._searchInput.clearFocus();
        Tool.hideKeyboard(_home, _searchBar._searchInput);
    }

    @Override
    public void onClick(View v) {
        _calendarDropDownView.animateShow();
    }
}
