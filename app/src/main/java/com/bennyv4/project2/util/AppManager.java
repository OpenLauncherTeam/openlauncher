package com.bennyv4.project2.util;

import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import java.text.Collator;
import java.util.*;

public class AppManager
{
	private static AppManager ref;

	private Context c;
	private PackageManager pm;
	private List<App> apps = new ArrayList<>();
	public List<AppUpdatedListener> updateListeners = new ArrayList<>();
	public List<AppDeletedListener> deleteListeners = new ArrayList<>();
	private AsyncTask task;

	public static AppManager getInstance(Context c){
		return ref == null ? (ref = new AppManager(c)) : ref;
	}

	public AppManager(Context c){
		this.c = c;
		this.pm = c.getPackageManager();
	}
	
	public App findApp(String packageName , String className){
		for(App app : apps){
			if (app.className.equals(className)&&app.packageName.equals(packageName)){
				return app;
			}
		}
		return null;
	}

	public void init(){
        getAllApps();
	}

	public void addAppUpdatedListener(AppUpdatedListener listener){
		updateListeners.add(listener);
	}

	public void addAppDeletedListener(AppDeletedListener listener){
		deleteListeners.add(listener);
	}

	private void getAllApps(){
		if(task == null || task.getStatus() == AsyncTask.Status.FINISHED)
			task = new AsyncGetApps().execute();
		else if(task.getStatus() == AsyncTask.Status.RUNNING){
			task.cancel(false);
			task = new AsyncGetApps().execute();
		}
	}

	public void onReceive(Context p1, Intent p2){
		getAllApps();
	}

	private class AsyncGetApps extends AsyncTask
	{
		private List<App> tempapps;

		@Override
		protected void onPreExecute(){
			tempapps = apps;
			super.onPreExecute();
		}

		@Override
		protected void onCancelled(){
			tempapps = null;
			super.onCancelled();
		}

		@Override
		protected Object doInBackground(Object[] p1){
			apps.clear();
			Intent intent = new Intent(Intent.ACTION_MAIN,null);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			List<ResolveInfo> activitiesInfo = pm.queryIntentActivities(intent,0);
            Collections.sort(activitiesInfo, new Comparator<ResolveInfo>()
            {
                @Override
                public int compare(ResolveInfo p1, ResolveInfo p2)
                {
                    return Collator.getInstance().compare(p1.loadLabel(pm).toString(),p2.loadLabel(pm).toString());
                }
            });
			for(ResolveInfo info : activitiesInfo){
				apps.add(new App(info, pm));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result){
			for(AppUpdatedListener listener : updateListeners){
				listener.onAppUpdated(apps);
			}

			if(tempapps.size() > apps.size()){
				App temp = null;
				for(int i = 0 ; i < tempapps.size() ; i++){
					if (!apps.contains(tempapps.get(i))){
						temp = tempapps.get(i);
						break;
					}
				}
				for(AppDeletedListener listener : deleteListeners){
					listener.onAppDeleted(temp);
				}
			}
			super.onPostExecute(result);
		}
	}

	public static class App
	{
		public String appName,packageName,className;
		public Drawable icon;
		public ResolveInfo info;

		public App(ResolveInfo info, PackageManager pm){
			this.info = info;

			icon = info.loadIcon(pm);
			appName = info.loadLabel(pm).toString();
			packageName = info.activityInfo.packageName;
			className = info.activityInfo.name;
		}

		@Override
		public boolean equals(Object o){
			if(o instanceof App){
				AppManager.App temp = (App)o;
				return this.packageName.equals(temp.packageName);
			}
			else{
				return false;
			}
		}
	}

	public interface AppUpdatedListener
	{
		public void onAppUpdated(List<App> apps);
	}

	public interface AppDeletedListener
	{
		public void onAppDeleted(App app);
	}
}
