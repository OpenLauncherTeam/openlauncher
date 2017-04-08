package com.benny.openlauncher.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.bennyv5.smoothviewpager.SmoothPagerAdapter;
import com.bennyv5.smoothviewpager.SmoothViewPager;

import java.util.ArrayList;
import java.util.List;

public class AppDrawer_Paged extends SmoothViewPager {
    private List<AppManager.App> apps;

    public List<ViewGroup> pages = new ArrayList<>();

    private Home home;

    private static int vCellCount, hCellCount;

    private PagerIndicator appDrawerIndicator;

    private int pageCount = 0;

    public AppDrawer_Paged(Context c, AttributeSet attr) {
        super(c, attr);
        init(c);
    }

    public AppDrawer_Paged(Context c) {
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
        hCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridX;
        vCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridY;
    }

    private void setLandscapeValue() {
        hCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridX_L;
        vCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridY_L;
    }

    private void calculatePage() {
        pageCount = 0;
        int appsSize = apps.size();
        while ((appsSize = appsSize - (vCellCount * hCellCount)) >= (vCellCount * hCellCount) || (appsSize > -(vCellCount * hCellCount))) {
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

        if (AppManager.getInstance(getContext()).getApps().size() != 0) {
            AppDrawer_Paged.this.apps = AppManager.getInstance(getContext()).getApps();
            calculatePage();
            setAdapter(new Adapter());
            if (appDrawerIndicator != null)
                appDrawerIndicator.setViewPager(AppDrawer_Paged.this);
        }
        AppManager.getInstance(c).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                AppDrawer_Paged.this.apps = apps;
                calculatePage();
                setAdapter(new Adapter());
                if (appDrawerIndicator != null)
                    appDrawerIndicator.setViewPager(AppDrawer_Paged.this);
            }
        });
    }

    public void withHome(Home home, PagerIndicator appDrawerIndicator) {
        this.home = home;
        this.appDrawerIndicator = appDrawerIndicator;
        if (getAdapter() != null)
            appDrawerIndicator.setViewPager(AppDrawer_Paged.this);
    }

    public void resetAdapter() {
        setAdapter(null);
        setAdapter(new Adapter());
    }

    public class Adapter extends SmoothPagerAdapter {

        private View getItemView(int page, int x, int y) {
            int pagePos = y * hCellCount + x;
            final int pos = vCellCount * hCellCount * page + pagePos;

            if (pos >= apps.size())
                return null;

            final AppManager.App app = apps.get(pos);

            return new AppItemView.Builder(getContext())
                    .setAppItem(app)
                    .withOnClickLaunchApp(app)
                    .setTextColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerLabelColor)
                    .withOnTouchGetPosition()
                    .withOnLongPressDrag(app, DragAction.Action.ACTION_APP_DRAWER, new AppItemView.Builder.LongPressCallBack() {
                        @Override
                        public boolean readyForDrag(View view) {
                            return LauncherSettings.getInstance(view.getContext()).generalSettings.desktopMode != Desktop.DesktopMode.ShowAllApps;
                        }

                        @Override
                        public void afterDrag(View view) {
                            home.closeAppDrawer();

                        }
                    }).getView();
        }

        public Adapter() {
            pages.clear();
            for (int i = 0; i < getCount(); i++) {
                ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_appdrawer_page, null);
                if (!LauncherSettings.getInstance(getContext()).generalSettings.drawerUseCard) {
                    ((CardView) layout.getChildAt(0)).setCardBackgroundColor(Color.TRANSPARENT);
                    ((CardView) layout.getChildAt(0)).setCardElevation(0);
                } else {
                    ((CardView) layout.getChildAt(0)).setCardBackgroundColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerCardColor);
                    ((CardView) layout.getChildAt(0)).setCardElevation(Tool.dp2px(4, getContext()));
                }
                CellContainer cc = (CellContainer) layout.findViewById(R.id.cc);
                cc.setGridSize(hCellCount, vCellCount);

                for (int x = 0; x < hCellCount; x++) {
                    for (int y = 0; y < vCellCount; y++) {
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
