package com.bennyv4.project2.widget;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.bennyv4.project2.util.*;
import java.util.*;
import com.bennyv4.project2.*;
import com.bennyv5.smoothviewpager.SmoothPagerAdapter;
import com.bennyv5.smoothviewpager.SmoothViewPager;
import com.viewpagerindicator.PageIndicator;

public class AppDrawer extends SmoothViewPager implements AppManager.AppUpdatedListener
{
	private List<AppManager.App> apps;

	private Home home;

	private static int pageHeight = 5 , pageWidth = 4;

	private boolean mPortrait,mPrePortrait;

	private int realPageHeight,realPageWidth,

	itemOffset = 20,

	textHeight = 22;

	public static int iconSize = 58;

	private PageIndicator appDrawerIndicator;

	@Override
	public void onAppUpdated(List<AppManager.App> apps){
		this.apps = apps;
		setAdapter(new Adapter());
		appDrawerIndicator.setViewPager(this);
	}

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
		mPrePortrait = mPortrait;
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			mPortrait = false;
			pageWidth = 5;
			pageHeight = 3;
			realPageHeight = (int)Tools.convertDpToPixel((itemOffset + iconSize + textHeight) * pageHeight, getContext());
			realPageWidth =  (int)Tools.convertDpToPixel((itemOffset + iconSize) * pageWidth, getContext());
			setAdapter(new Adapter());
		}
		else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			mPortrait = true;
			pageWidth = 4;
			pageHeight = 5;
			realPageHeight = (int)Tools.convertDpToPixel((itemOffset + iconSize + textHeight) * pageHeight, getContext());
			realPageWidth =  (int)Tools.convertDpToPixel((itemOffset + iconSize) * pageWidth, getContext());
			setAdapter(new Adapter());
		}
		super.onConfigurationChanged(newConfig);
	}

	private void init(Context c){
		mPortrait = c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		mPrePortrait = mPortrait;

		if(mPortrait){
			pageWidth = 4;
			pageHeight = 5;
		} else {
			pageWidth = 5;
			pageHeight = 3;
		}
		realPageHeight = (int)Tools.convertDpToPixel((itemOffset + iconSize + textHeight) * pageHeight, getContext());
		realPageWidth =  (int)Tools.convertDpToPixel((itemOffset + iconSize) * pageWidth, getContext());

		AppManager.getInstance(c).addAppUpdatedListener(this);
		AppManager.getInstance(c).init();
	}


	public void withHome(Home home, PageIndicator appDrawerIndicator){
		this.home = home;
		this.appDrawerIndicator = appDrawerIndicator;
	}

	public class Adapter extends SmoothPagerAdapter
	{
		List<ViewGroup> views = new ArrayList<>();

		public Adapter(){
			for(int i = 0 ; i < getCount() ; i++){
				ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_appdrawer_page, null);
				RecyclerView rv = (RecyclerView) layout.findViewById(R.id.rv);
				rv.setLayoutManager(new GridLayoutManager(getContext(), pageWidth));
				rv.setAdapter(new AppItemAdapter(i));
				rv.setOverScrollMode(OVER_SCROLL_NEVER);
				rv.addItemDecoration(new MyDecor());
				rv.getLayoutParams().height = realPageHeight;
				rv.getLayoutParams().width = realPageWidth;

				views.add(layout);
			}
		}

		@Override
		public int getCount(){
			int page = 0;
			int appsSize = apps.size();
			while((appsSize = appsSize - (pageHeight * pageWidth)) >= (pageHeight * pageWidth) || (appsSize > -(pageHeight*pageWidth))){
				page ++;                                                                           
			}
			return page;
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

			RecyclerView rv = (RecyclerView) layout.findViewById(R.id.rv);

			if(mPortrait != mPrePortrait){
				rv.getAdapter().notifyDataSetChanged();
				((GridLayoutManager)rv.getLayoutManager()).setSpanCount(pageWidth);
				rv.getLayoutParams().height = realPageHeight;
				rv.getLayoutParams().width = realPageWidth;
			}

			container.addView(layout);

			return layout;
		}

		private class MyDecor extends RecyclerView.ItemDecoration
		{
			@Override
			public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
				outRect.top = itemOffset;
				outRect.bottom = itemOffset;
				outRect.left = itemOffset;
				outRect.right = itemOffset;
			}
		}

		public class AppItem extends RecyclerView.ViewHolder implements OnClickListener,OnLongClickListener
		{
			@Override
			public boolean onLongClick(View p1){
				Intent i = new Intent();
				i.putExtra("mDragData", Desktop.Item.newAppItem(app));
				ClipData data = ClipData.newIntent("mDragIntent", i);
				p1.startDrag(data, new DragShadowBuilder(p1), DragAction.ACTION_APP, 0);
				home.closeAppDrawer();
				return true;
			}

			@Override
			public void onClick(View view){
				Tools.createScaleInScaleOutAnim(view, new Runnable() {
                    @Override
                    public void run() {
                        Tools.startApp(getContext(),app);
                    }
                });
			}

			ImageView iv;
			TextView tv;
			AppManager.App app;

			public AppItem(View v){
				super(v);
				iv = (ImageView) itemView.findViewById(R.id.iv);
				tv = (TextView) itemView.findViewById(R.id.tv);

				iv.getLayoutParams().width = (int)Tools.convertDpToPixel(iconSize, getContext());
				iv.getLayoutParams().height = (int)Tools.convertDpToPixel(iconSize, getContext());

				tv.getLayoutParams().height = (int)Tools.convertDpToPixel(textHeight, getContext());
			}                                                               

			public void setup(int page, int pos){
				AppManager.App temp = apps.get(pageHeight * pageWidth * page + pos);
				app = temp;

				itemView.setOnClickListener(this);
				itemView.setOnLongClickListener(this);
				iv.setImageDrawable(temp.icon);
				tv.setText(temp.appName);
			}
		}

		public class AppItemAdapter extends RecyclerView.Adapter<AppItem>
		{
			public int pos;

			public AppItemAdapter(int pos){
				this.pos = pos;
			}

			@Override
			public AppItem onCreateViewHolder(ViewGroup p1, int p2){
				return new AppItem(LayoutInflater.from(getContext()).inflate(R.layout.item_app, p1, false));
			}

			@Override
			public void onBindViewHolder(AppItem p1, int p2){
				p1.setup(pos, p2);
			}

			@Override
			public int getItemCount(){
				int page = 0;
				int appsSize = apps.size();
				while((appsSize = appsSize - (pageHeight * pageWidth)) >= (pageHeight * pageWidth) || (appsSize > -(pageHeight*pageWidth))){
					page ++;                                                                               
				}
				if(pos == page - 1)
					return  apps.size() - pos * pageHeight * pageWidth;
				else
					return pageHeight * pageWidth;
			}
		}

	}
}
