package com.benny.openlauncher.widget;

import android.content.Context;
import android.content.res.*;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.R;

import java.util.ArrayList;
import java.util.List;

import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.bennyv5.smoothviewpager.SmoothPagerAdapter;
import com.bennyv5.smoothviewpager.SmoothViewPager;

public class PagedAppDrawer extends SmoothViewPager {
    private List<AppManager.App> apps;

    private Home home;

    private static int vertCellCount, horiCellCount;

    private PagerIndicator appDrawerIndicator;

    private int pageCount = 0;

    public PagedAppDrawer(Context c, AttributeSet attr) {
        super(c, attr);
        init(c);
    }

    public PagedAppDrawer(Context c) {
        super(c);
        init(c);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (apps == null) {
            super.onConfigurationChanged(newConfig);
            return;
        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeValue();
            calculatePage();
            setAdapter(new Adapter());
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitValue();
            calculatePage();
            setAdapter(new Adapter());
        }
        super.onConfigurationChanged(newConfig);
    }

    private void setPortraitValue() {
        horiCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridx;
        vertCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridy;
    }

    private void setLandscapeValue() {
        horiCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridxL;
        vertCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridyL;
    }

    private void calculatePage() {
        pageCount = 0;
        int appsSize = apps.size();
        while ((appsSize = appsSize - (vertCellCount * horiCellCount)) >= (vertCellCount * horiCellCount) || (appsSize > -(vertCellCount * horiCellCount))) {
            pageCount++;
        }
    }

    private void init(Context c) {
        if (isInEditMode()) return;

        boolean mPortrait = c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (mPortrait) {
            setPortraitValue();
        } else {
            setLandscapeValue();
        }

        if (AppManager.getInstance(getContext()).getApps().size() != 0){
            PagedAppDrawer.this.apps = AppManager.getInstance(getContext()).getApps();
            calculatePage();
            setAdapter(new Adapter());
        }
        AppManager.getInstance(c).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                PagedAppDrawer.this.apps = apps;
                calculatePage();
                setAdapter(new Adapter());
                appDrawerIndicator.setViewPager(PagedAppDrawer.this);
            }
        });
    }

    public void withHome(Home home, PagerIndicator appDrawerIndicator) {
        this.home = home;
        this.appDrawerIndicator = appDrawerIndicator;
        if (getAdapter() != null)
            appDrawerIndicator.setViewPager(PagedAppDrawer.this);
    }

    public class Adapter extends SmoothPagerAdapter {

        List<ViewGroup> views = new ArrayList<>();

        private View getItemView(int page, int x, int y) {
            int pagePos = y * horiCellCount + x;
            final int pos = vertCellCount * horiCellCount * page + pagePos;

            if (pos >= apps.size())
                return null;

            final AppManager.App app = apps.get(pos);

            return new AppItemView.Builder(getContext())
                    .setAppItem(app)
                    .withOnClickLaunchApp(app)
                    .withOnTouchGetPosition()
                    .withOnLongClickDrag(app, DragAction.Action.ACTION_APP_DRAWER, new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            home.closeAppDrawer();
                            return false;
                        }
                    }).getView();
        }

        public Adapter() {
            for (int i = 0; i < getCount(); i++) {
                ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_appdrawer_page, null);
                CellContainer cc = (CellContainer) layout.findViewById(R.id.cc);
                cc.setGridSize(horiCellCount, vertCellCount);

                for (int x = 0; x < horiCellCount; x++) {
                    for (int y = 0; y < vertCellCount; y++) {
                        View view = getItemView(i, x, y);
                        if (view != null) {
                            CellContainer.LayoutParams lp = new CellContainer.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, x, y, 1, 1);
                            view.setLayoutParams(lp);
                            cc.addViewToGrid(view);
                        }
                    }
                }

                views.add(layout);
            }
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public boolean isViewFromObject(View p1, Object p2) {
            return p1 == p2;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            int index = views.indexOf(object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int pos) {
            ViewGroup layout = views.get(pos);
            container.addView(layout);
            return layout;
        }
    }
}
