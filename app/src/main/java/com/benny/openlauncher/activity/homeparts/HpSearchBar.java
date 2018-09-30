package com.benny.openlauncher.activity.homeparts;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.CalendarView;
import com.benny.openlauncher.widget.SearchBar;

public class HpSearchBar implements SearchBar.CallBack, View.OnClickListener {
    private HomeActivity _homeActivity;
    private SearchBar _searchBar;
    private CalendarView _calendarView;

    public HpSearchBar(HomeActivity homeActivity, SearchBar searchBar, CalendarView calendarView) {
        _homeActivity = homeActivity;
        _searchBar = searchBar;
        _calendarView = calendarView;
    }


    public void initSearchBar() {
        _searchBar.setCallback(this);
        _searchBar._searchClock.setOnClickListener(this);
        _homeActivity.updateSearchClock();
    }

    @Override
    public void onInternetSearch(String string) {
        Intent intent = new Intent();

        if (Tool.isIntentActionAvailable(_homeActivity.getApplicationContext(), Intent.ACTION_WEB_SEARCH) && !Setup.appSettings().getSearchBarForceBrowser()) {
            intent.setAction(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, string);
        } else {
            String baseUri = Setup.appSettings().getSearchBarBaseURI();
            String searchUri = baseUri.contains("{query}") ? baseUri.replace("{query}", string) : (baseUri + string);

            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(searchUri));
        }

        try {
            _homeActivity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onExpand() {
        _homeActivity.clearRoomForPopUp();
        _homeActivity.dimBackground();

        _searchBar._searchInput.setFocusable(true);
        _searchBar._searchInput.setFocusableInTouchMode(true);
        _searchBar._searchInput.post(new Runnable() {
            @Override
            public void run() {
                _searchBar._searchInput.requestFocus();
            }
        });

        Tool.showKeyboard(_homeActivity, _searchBar._searchInput);
    }

    @Override
    public void onCollapse() {
        _homeActivity.getDesktop().postDelayed(new Runnable() {
            @Override
            public void run() {
                _homeActivity.unClearRoomForPopUp();
            }
        }, 100);

        _homeActivity.unDimBackground();
        _searchBar._searchInput.clearFocus();
        Tool.hideKeyboard(_homeActivity, _searchBar._searchInput);
    }

    @Override
    public void onClick(View v) {
        _calendarView.animateShow();
    }
}
