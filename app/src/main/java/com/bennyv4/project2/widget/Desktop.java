package com.bennyv4.project2.widget;

import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import com.bennyv4.project2.*;
import com.bennyv4.project2.util.*;
import java.util.*;
import android.widget.*;
import com.bennyv5.smoothviewpager.SmoothPagerAdapter;
import com.bennyv5.smoothviewpager.SmoothViewPager;

public class Desktop extends SmoothViewPager implements OnDragListener
{
	public int pageCount;

	public List<CellContainer> pages = new ArrayList<>();

	public Desktop(Context c, AttributeSet attr){
		super(c, attr);
		init(c);
	}

	public Desktop(Context c){
		super(c);
		init(c);
	}

	private void init(Context c){
        pageCount = LauncherSettings.getInstance(c).normalSettings.desktopPageCount;
		setAdapter(new Adapter());
		setOnDragListener(this);
	}

	@Override
	public boolean onDrag(View p1, DragEvent p2){
		switch(p2.getAction()){
			case DragEvent.ACTION_DRAG_STARTED:
				switch((DragAction)p2.getLocalState()){
					case ACTION_APP:
						pages.get(getCurrentItem()).setHideGrid(false);
						return true;
				}
				return false;
			case DragEvent.ACTION_DRAG_ENTERED:
				return true;

			case DragEvent.ACTION_DRAG_EXITED:
				return true;

			case DragEvent.ACTION_DROP:
				Intent intent = p2.getClipData().getItemAt(0).getIntent();
				intent.setExtrasClassLoader(Item.class.getClassLoader());
				Item item = intent.getParcelableExtra("mDragData");
				if(item.type == Desktop.Item.Type.APP)
					addAppToCurrentPage(item, (int)p2.getX(), (int)p2.getY());
				return true;
			case DragEvent.ACTION_DRAG_ENDED:
				pages.get(getCurrentItem()).setHideGrid(true);
				return true;
		}
		return false;
	}

    public void addAppToPagePosition(final Item item,int page){
        ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
        TextView tv = (TextView) item_layout.findViewById(R.id.tv);
        ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);
        iv.getLayoutParams().width = (int)Tools.convertDpToPixel(AppDrawer.iconSize,getContext());
        iv.getLayoutParams().height = (int)Tools.convertDpToPixel(AppDrawer.iconSize,getContext());

        final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.packageName[0], item.className[0]);
        if (app == null){
            LauncherSettings.getInstance(getContext()).desktopData.get(page).remove(item);
            return;
        }

        tv.setText(app.appName);
        tv.setTextColor(Color.WHITE);
        iv.setImageDrawable(app.icon);
        item_layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent();
                i.putExtra("mDragData", Desktop.Item.newAppItem(app));
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new DragShadowBuilder(view), DragAction.ACTION_APP, 0);

                //Remove the item from settings
                LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
                //end

                pages.get(getCurrentItem()).removeView(view);
                return true;
            }
        });
        item_layout.setOnClickListener(new OnClickListener() {
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
        pages.get(page).addViewToGrid(item_layout,item.x,item.y);
    }

	public void addAppToCurrentPage(final Item item, int x, int y){
		CellContainer.LayoutParams positionToLayoutPrams = pages.get(getCurrentItem()).positionToLayoutPrams(x, y);
		if(positionToLayoutPrams != null){

            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1)
                LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(),new ArrayList<Item>());
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
            //end

			ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
			TextView tv = (TextView) item_layout.findViewById(R.id.tv);
			ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);
			iv.getLayoutParams().width = (int)Tools.convertDpToPixel(AppDrawer.iconSize,getContext());
			iv.getLayoutParams().height = (int)Tools.convertDpToPixel(AppDrawer.iconSize,getContext());

			final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.packageName[0], item.className[0]);
            if (app == null)
                return;

			tv.setText(app.appName);
			tv.setTextColor(Color.WHITE);
			iv.setImageDrawable(app.icon);
			item_layout.setLayoutParams(positionToLayoutPrams);
			item_layout.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					Intent i = new Intent();
					i.putExtra("mDragData", Desktop.Item.newAppItem(app));
					ClipData data = ClipData.newIntent("mDragIntent", i);
					view.startDrag(data, new DragShadowBuilder(view), DragAction.ACTION_APP, 0);

                    //Remove the item from settings
                    LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
                    //end

					pages.get(getCurrentItem()).removeView(view);
					return true;
				}
			});
			item_layout.setOnClickListener(new OnClickListener() {
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
			pages.get(getCurrentItem()).addView(item_layout);
		}
		else{
			Toast.makeText(getContext(), "Occupied", Toast.LENGTH_SHORT).show();
		}
	}

	public class Adapter extends SmoothPagerAdapter {
		public Adapter(){
			for(int i = 0 ; i < getCount() ; i++){
				CellContainer layout = new CellContainer(getContext());
				int pad = (int)Tools.convertDpToPixel(10, getContext());
				layout.setPadding(pad, pad, pad, pad);
				pages.add(layout);
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
		public Object instantiateItem(ViewGroup container, int pos){
			ViewGroup layout = pages.get(pos);

			container.addView(layout);

			return layout;
		}
	}

	public static class Item implements Parcelable {
		public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>(){

			@Override
			public Item createFromParcel(Parcel in){
				return new Item(in);
			}

			@Override
			public Item[] newArray(int size){
				return new Item[size];
			}
		};

		public Type type;

		public String packageName[];

		public String className[];

		public int x , y;

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Item
					&& ((Item)obj).type == this.type
					&& Arrays.equals(((Item)obj).packageName,this.packageName)
					&& Arrays.equals(((Item)obj).className,this.className)
					&& ((Item)obj).x == this.x
					&& ((Item)obj).y == this.y
					;
		}

		public Item(){}

		public static Item newAppItem(AppManager.App app){
			Desktop.Item item = new Item();
			item.type = Type.APP;
			item.packageName = new String[]{app.packageName};
			item.className = new String[]{app.className};
			return item;
		}

		public Item(Parcel in){
			type = Type.valueOf(in.readString());
			switch(type){
				case GROUP:
				case APP:
					packageName = in.createStringArray();
					className = in.createStringArray();
					break;
			}
        }

		@Override
		public int describeContents(){
			return 0;
		}

		@Override
        public void writeToParcel(Parcel out, int flags){
			out.writeString(type.toString());
			switch(type){
				case GROUP:
				case APP:
					out.writeStringArray(packageName);
					out.writeStringArray(className);
					break;
			}
        }

		public enum Type
		{
			APP,
			WIDGET,
			SHORTCUT,
			GROUP;
		}
	}

}
