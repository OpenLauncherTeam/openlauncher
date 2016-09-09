package com.bennyv4.project2.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.*;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bennyv4.project2.activity.Home;
import com.bennyv4.project2.R;

import java.util.ArrayList;
import java.util.List;

import com.bennyv4.project2.util.AppManager;
import com.bennyv4.project2.util.DragAction;
import com.bennyv4.project2.util.GoodDragShadowBuilder;
import com.bennyv4.project2.util.LauncherSettings;
import com.bennyv4.project2.util.Tools;
import com.bennyv5.smoothviewpager.SmoothPagerAdapter;
import com.bennyv5.smoothviewpager.SmoothViewPager;

public class AppDrawer extends SmoothViewPager
{
	private List<AppManager.App> apps;

	private Home home;

	private static int vertCellCount, horiCellCount;

	private boolean mPortrait;

	private int textHeight = 22;

	private PagerIndicator appDrawerIndicator;

	private int pageCount = 0;

	public AppDrawer(Context c, AttributeSet attr){
		super(c, attr);
		init(c);
	}

	public AppDrawer(Context c){
		super(c);
		init(c);
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig){
		if (apps == null){
			super.onConfigurationChanged(newConfig);
			return;
		}
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			mPortrait = false;
            setLandscapeValue();
			setAdapter(new Adapter());
		}
		else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			mPortrait = true;
            setPortraitValue();
			setAdapter(new Adapter());
		}
		super.onConfigurationChanged(newConfig);
	}

    private void setPortraitValue(){
        horiCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridx;
        vertCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridy;
    }

    private void setLandscapeValue(){
        horiCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridxL;
        vertCellCount = LauncherSettings.getInstance(getContext()).generalSettings.drawerGridyL;
    }

    private void calculatePage(){
        pageCount = 0;
        int appsSize = apps.size();
        while((appsSize = appsSize - (vertCellCount * horiCellCount)) >= (vertCellCount * horiCellCount) || (appsSize > -(vertCellCount * horiCellCount))){
            pageCount ++;
        }
    }

	private void init(Context c){
		mPortrait = c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

		if(mPortrait){
            setPortraitValue();
		} else {
			setLandscapeValue();
		}

		AppManager.getInstance(c).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
			@Override
			public void onAppUpdated(List<AppManager.App> apps) {
				AppDrawer.this.apps = apps;
                calculatePage();
				setAdapter(new Adapter());
				appDrawerIndicator.setViewPager(AppDrawer.this);
			}
		});
	}

	public void withHome(Home home, PagerIndicator appDrawerIndicator){
		this.home = home;
		this.appDrawerIndicator = appDrawerIndicator;
	}

	public class Adapter extends SmoothPagerAdapter
	{
		List<ViewGroup> views = new ArrayList<>();

        private View getItemView(int page,int x,int y){
            int pagePos = y * horiCellCount + x;
            final int pos = vertCellCount * horiCellCount * page + pagePos;

            if (pos >= apps.size())
                return null;

            final AppManager.App app = apps.get(pos);

            FrameLayout itemView = new FrameLayout(getContext());
            LinearLayout innerView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_app,itemView,false);
            itemView.addView(innerView);

            ImageView iv = (ImageView) itemView.findViewById(R.id.iv);
            TextView tv = (TextView) itemView.findViewById(R.id.tv);

            iv.getLayoutParams().width = Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());
            iv.getLayoutParams().height = Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());

            tv.getLayoutParams().height = Tools.convertDpToPixel(textHeight, getContext());

            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Tools.createScaleInScaleOutAnim(view, new Runnable() {
                        @Override
                        public void run() {
                            Tools.startApp(getContext(),app);
                        }
                    });
                }
            });
            itemView.setOnTouchListener(Tools.getItemOnTouchListener());
            itemView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent i = new Intent();
                    i.putExtra("mDragData", Desktop.Item.newAppItem(app));
                    ClipData data = ClipData.newIntent("mDragIntent", i);
                    view.startDrag(data, new GoodDragShadowBuilder(view),new DragAction(DragAction.Action.ACTION_APP_DRAWER,0), 0);
                    home.closeAppDrawer();
                    return true;
                }
            });
            iv.setImageDrawable(app.icon);
            tv.setText(app.appName);

            return itemView;
        }

		public Adapter(){
			for(int i = 0 ; i < getCount() ; i++){
				ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_appdrawer_page, null);
				CellContainer cc = (CellContainer) layout.findViewById(R.id.cc);
                cc.setGridSize(horiCellCount,vertCellCount);

                for (int x = 0 ; x < horiCellCount ; x ++){
                    for (int y = 0 ; y < vertCellCount ; y ++){
                        View view = getItemView(i,x,y);
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
		public int getCount(){
			return pageCount;
		}

		@Override
		public boolean isViewFromObject(View p1, Object p2){
			return p1 == p2;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object){
			container.removeView((View)object);
		}

		@Override
		public int getItemPosition(Object object){
			int index = views.indexOf(object);
			if(index == -1)
				return POSITION_NONE;
			else
				return index;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int pos){
			ViewGroup layout = views.get(pos);
			container.addView(layout);
			return layout;
		}
	}
}
