package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.IApp;
import com.benny.openlauncher.core.interfaces.IAppItemView;
import com.benny.openlauncher.core.interfaces.IAppUpdateListener;
import com.benny.openlauncher.core.manager.StaticSetup;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.viewutil.SmoothPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AppDrawerPaged extends SmoothViewPager {
    private List<IApp> apps;

    public List<ViewGroup> pages = new ArrayList<>();

    private Home home;

    private static int rowCellCount, columnCellCount;

    private PagerIndicator appDrawerIndicator;

    private int pageCount = 0;

    public AppDrawerPaged(Context c, AttributeSet attr) {
        super(c, attr);
        init(c);
    }

    public AppDrawerPaged(Context c) {
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
        columnCellCount = StaticSetup.get().getAppSettings().getDrawerColumnCount();
        rowCellCount = StaticSetup.get().getAppSettings().getDrawerRowCount();
    }

    private void setLandscapeValue() {
        columnCellCount = StaticSetup.get().getAppSettings().getDrawerRowCount();
        rowCellCount = StaticSetup.get().getAppSettings().getDrawerColumnCount();
    }

    private void calculatePage() {
        pageCount = 0;
        int appsSize = apps.size();
        while ((appsSize = appsSize - (rowCellCount * columnCellCount)) >= (rowCellCount * columnCellCount) || (appsSize > -(rowCellCount * columnCellCount))) {
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

        List<IApp> allApps = StaticSetup.get().getAllApps(c);
        if (allApps.size() != 0) {
            AppDrawerPaged.this.apps = allApps;
            calculatePage();
            setAdapter(new Adapter());
            if (appDrawerIndicator != null)
                appDrawerIndicator.setViewPager(AppDrawerPaged.this);
        }
        StaticSetup.get().addAppUpdatedListener(c, new IAppUpdateListener<IApp>() {
            @Override
            public void onAppUpdated(List<IApp> apps) {
                AppDrawerPaged.this.apps = apps;
                calculatePage();
                setAdapter(new Adapter());
                if (appDrawerIndicator != null)
                    appDrawerIndicator.setViewPager(AppDrawerPaged.this);
            }
        });
    }

    public void withHome(Home home, PagerIndicator appDrawerIndicator) {
        this.home = home;
        this.appDrawerIndicator = appDrawerIndicator;
        if (getAdapter() != null)
            appDrawerIndicator.setViewPager(AppDrawerPaged.this);
    }

    public void resetAdapter() {
        setAdapter(null);
        setAdapter(new Adapter());
    }

    public class Adapter extends SmoothPagerAdapter {

        private View getItemView(int page, int x, int y) {
            int pagePos = y * columnCellCount + x;
            final int pos = rowCellCount * columnCellCount * page + pagePos;

            if (pos >= apps.size())
                return null;

            final IApp app = apps.get(pos);

            return StaticSetup.get()
                    .createAppItemView(
                        getContext(),
                        home,
                        app,
                        new IAppItemView.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return StaticSetup.get().getAppSettings().getDesktopStyle() != Desktop.DesktopMode.SHOW_ALL_APPS;
                            }

                            @Override
                            public void afterDrag(View view) {
                                home.closeAppDrawer();
                            }
                        });
        }

        public Adapter() {
            pages.clear();
            for (int i = 0; i < getCount(); i++) {
                ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.view_app_drawer_paged_inner, null);
                if (!StaticSetup.get().getAppSettings().isDrawerShowCardView()) {
                    ((CardView) layout.getChildAt(0)).setCardBackgroundColor(Color.TRANSPARENT);
                    ((CardView) layout.getChildAt(0)).setCardElevation(0);
                } else {
                    ((CardView) layout.getChildAt(0)).setCardBackgroundColor(StaticSetup.get().getAppSettings().getDrawerCardColor());
                    ((CardView) layout.getChildAt(0)).setCardElevation(Tool.dp2px(4, getContext()));
                }
                CellContainer cc = (CellContainer) layout.findViewById(R.id.group);
                cc.setGridSize(columnCellCount, rowCellCount);

                for (int x = 0; x < columnCellCount; x++) {
                    for (int y = 0; y < rowCellCount; y++) {
                        View view = getItemView(i, x, y);
                        if (view != null) {
                            CellContainer.LayoutParams lp = new CellContainer.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, x, y, 1, 1);
                            view.setLayoutParams(lp);
                            cc.addViewToGrid(view);
                        }
                    }
                }
                pages.add(layout);
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
            int index = pages.indexOf(object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int pos) {
            ViewGroup layout = pages.get(pos);
            container.addView(layout);
            return layout;
        }
    }
}
