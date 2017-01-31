package com.benny.openlauncher.hideApps;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.benny.openlauncher.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Fragment_hideApps extends Fragment {

    @SuppressWarnings("unchecked")
    private final ArrayList<String> list_activities = new ArrayList();
    @SuppressWarnings("unchecked")
    private static ArrayList<AppInfo> list_activities_final = new ArrayList();
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private ViewSwitcher switcherLoad;
    private final Fragment_hideApps.AsyncWorkerList taskList = new AsyncWorkerList();

    private String SAVE_LOC;
    private static final String appfilter_path = "empty_appfilter.xml"; //TODO Define path to appfilter.xml in assets folder.

    private static final String TAG = "RequestActivity";
    private static final boolean DEBUG = true; //TODO Set to false for PlayStore Release

    @SuppressWarnings("unused")
    private ViewSwitcher viewSwitcher;
    private ListView grid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.request, container, false);
        switcherLoad = (ViewSwitcher)rootView.findViewById(R.id.viewSwitcherLoadingMain);
        context = getActivity();

        try {

            PackageManager m = getActivity().getPackageManager();
            String s = getActivity().getPackageName();
            PackageInfo p = m.getPackageInfo(s, 0);
            s = p.applicationInfo.dataDir;
            SAVE_LOC = s;

        } catch (Exception e){
            Toast.makeText(getActivity(), R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
        }

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_rq);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                actionSend();
            }
        });

        if(taskList.getStatus() == AsyncTask.Status.PENDING){
            // My AsyncTask has not started yet
            taskList.execute();
        }

        if(taskList.getStatus() == AsyncTask.Status.FINISHED){
            // My AsyncTask is done and onPostExecute was called
            new AsyncWorkerList().execute();
        }

        return rootView;
    }

    public class AsyncWorkerList extends AsyncTask<String, Integer, String>{

        private AsyncWorkerList(){}

        @Override
        protected String doInBackground(String... arg0) {
            try {
                //Get already styled apps
                parseXML();
                // Compare them to installed apps
                prepareData();

                return null;
            }
            catch (Throwable e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            // Display the unstyled app
            populateView(list_activities_final);
            //Switch from loading screen to the main view
            switcherLoad.showNext();

            super.onPostExecute(result);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        if(DEBUG) Log.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);
    }

    private void actionSend() {

        Thread actionSend_Thread = new Thread() {

            @Override
            public void run() {

                ArrayList arrayList = list_activities_final;
                StringBuilder stringBuilderXML = new StringBuilder();
                int amount = 0;

                // Get all selected apps
                for (int i = 0; i < arrayList.size(); i++) {
                    if (((AppInfo)arrayList.get(i)).isSelected()) {
                        String iconName = (((AppInfo)arrayList.get(i)).getCode().split("/")[0].replace(".", "_") + "_" +((AppInfo)arrayList.get(i)).getCode().split("/")[1]).replace(".", "_");
                        if(DEBUG)Log.i(TAG, "iconName: " + iconName);
                        stringBuilderXML.append("<!-- ").append(((AppInfo) arrayList.get(i)).getName()).append(" -->\n<item component=\"ComponentInfo{").append(((AppInfo) arrayList.get(i)).getCode()).append("}\" drawable=\"").append(iconName).append("\"/>").append("\n");
                        amount++;
                    }
                }
                if (amount == 0){//When there's no app selected show a toast and return.
                    Snackbar snackbar = Snackbar
                            .make(grid, R.string.request_toast, Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        FileWriter fstream = new FileWriter(SAVE_LOC + "/appfilter.txt");
                                        BufferedWriter out = new BufferedWriter(fstream);
                                        out.write("");
                                        out.close();
                                        getActivity().finish();
                                    } catch (Exception e){
                                        Toast.makeText(getActivity(), R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    snackbar.show();

                } else {
                    // write zip and start email intent.
                    try {
                        FileWriter fstream = new FileWriter(SAVE_LOC + "/appfilter.txt");
                        BufferedWriter out = new BufferedWriter(fstream);
                        out.write(stringBuilderXML.toString());
                        out.close();
                        getActivity().finish();
                    } catch (Exception e){
                        Toast.makeText(getActivity(), R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        };
        if(!actionSend_Thread.isAlive()) {
            //Prevents the thread to be executed twice (or more) times.
            actionSend_Thread.start();
        }
    }

    // Read the appfilter.xml from assets and get all activities
    private void parseXML() {

        try{
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myparser = xmlFactoryObject.newPullParser();

            AssetManager am = context.getAssets();
            InputStream inputStream = am.open(appfilter_path);
            myparser.setInput(inputStream, null);

            int activity = myparser.getEventType();
            while (activity != XmlPullParser.END_DOCUMENT) {
                String name=myparser.getName();
                switch (activity){
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.END_TAG:
                        if(name.equals("item")) {
                            try	{
                                String tmp_act = myparser.getAttributeValue(null,"component").split("/")[1];
                                String t_activity= tmp_act.substring(0, tmp_act.length()-1);

                                String tmp_pack = myparser.getAttributeValue(null,"component").split("/")[0];
                                String t_package= tmp_pack.substring(14, tmp_pack.length());

                                list_activities.add(t_package + "/" + t_activity);

                                if(DEBUG)Log.v(TAG,"Added Styled App: \"" +t_package + "/" + t_activity+"\"");
                            }
                            catch(ArrayIndexOutOfBoundsException ignored){}
                        }
                        break;
                }
                activity = myparser.next();
            }
        } catch(IOException exIO){
            Toast.makeText(getActivity(), "Make sure you copied appfilter.xml in assets folder!", Toast.LENGTH_SHORT).show();
        } //Show toast when there's no appfilter.xml in assets
        catch(XmlPullParserException ignored){
        }
    }

    @SuppressWarnings("unchecked")
    private void prepareData() { // Sort the apps

        ArrayList<AppInfo> arrayList = new ArrayList();
        PackageManager pm = getActivity().getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");
        List list = pm.queryIntentActivities(intent, 0);
        Iterator localIterator = list.iterator();
        if(DEBUG)Log.v(TAG,"list.size(): "+list.size());

        for (int i = 0; i < list.size(); i++) {
            ResolveInfo resolveInfo = (ResolveInfo)localIterator.next();

            // This is the main part where the already styled apps are sorted out.
            if ((list_activities.indexOf(resolveInfo.activityInfo.packageName + "/" + resolveInfo.activityInfo.name) == -1)) {

                AppInfo tempAppInfo = new AppInfo(
                        resolveInfo.activityInfo.packageName + "/" + resolveInfo.activityInfo.name, //Get package/activity
                        resolveInfo.loadLabel(pm).toString(), //Get the app name
                        getHighResIcon(pm, resolveInfo) //Loads xxxhdpi icon, returns normal if it on fail
                        //Unselect icon per default
                );
                arrayList.add(tempAppInfo);

                // This is just for debugging
                if(DEBUG)Log.i(TAG,"Added app: " + resolveInfo.loadLabel(pm));
            } else {
                // This is just for debugging
                if(DEBUG)Log.v(TAG,"Removed app: " + resolveInfo.loadLabel(pm));
            }
        }

        Collections.sort(arrayList, new Comparator<AppInfo>() { //Custom comparator to ensure correct sorting for characters like and apps starting with a small letter like iNex
            @Override
            public int compare(AppInfo object1, AppInfo object2) {
                Locale locale = Locale.getDefault();
                Collator collator = Collator.getInstance(locale);
                collator.setStrength(Collator.TERTIARY);

                if(DEBUG)Log.v(TAG,"Comparing \""+object1.getName()+"\" to \"" + object2.getName()+"\"");

                return collator.compare(object1.getName(), object2.getName());
            }
        });

        list_activities_final = arrayList;
    }


    private Drawable getHighResIcon(PackageManager pm, ResolveInfo resolveInfo){

        Resources resources;
        Drawable icon;

        try {
            ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName , resolveInfo.activityInfo.name);

            resources = pm.getResourcesForActivity(componentName);//Get resources for the activity

            int iconId = resolveInfo.getIconResource();//Get the resource Id for the activity icon

            if(iconId != 0) {
                icon = resources.getDrawableForDensity(iconId, 640); //Loads the icon at xxhdpi resolution or lower.
                return icon;
            }
            return resolveInfo.loadIcon(pm);

        } catch (PackageManager.NameNotFoundException e) {
            return resolveInfo.loadIcon(pm);//If it fails return the normal icon
        } catch (Resources.NotFoundException e) {
            return resolveInfo.loadIcon(pm);
        }
    }

    @SuppressWarnings("deprecation")
    private int getDisplaySize(String which){
        Display display =((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if(which.equals("height")){
            return display.getHeight();
        }
        if(which.equals("width"))
        {
            return display.getWidth();
        }
        if(DEBUG)Log.v(TAG, "Normally unreachable. Line. What happened??");
        return 1000;
    }



    @SuppressWarnings("unchecked")
    private void populateView(ArrayList arrayListFinal){
        ArrayList<AppInfo> local_arrayList;
        local_arrayList = arrayListFinal;

        grid = (ListView) getActivity().findViewById(R.id.appgrid);

        assert grid != null;
        grid.setFastScrollEnabled(true);
        grid.setFastScrollAlwaysVisible(false);

        if(DEBUG)Log.v(TAG,"height: "+getDisplaySize("height")+"; width: "+getDisplaySize("width"));

        AppAdapter appInfoAdapter;
        appInfoAdapter = new AppAdapter(getActivity(), local_arrayList);


        grid.setAdapter(appInfoAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> AdapterView, View view, int position, long row)
            {
                AppInfo appInfo = (AppInfo)AdapterView.getItemAtPosition(position);
                CheckBox checker = (CheckBox)view.findViewById(R.id.CBappSelect);
                ViewSwitcher icon = (ViewSwitcher)view.findViewById(R.id.viewSwitcherChecked);

                checker.toggle();
                appInfo.setSelected(checker.isChecked());


                if(appInfo.isSelected()) {
                    if(DEBUG)Log.v(TAG,"Selected App: "+appInfo.getName());
                    if(icon.getDisplayedChild() == 0){
                        icon.showNext();
                    }
                } else {
                    if(DEBUG)Log.v(TAG,"Deselected App: "+appInfo.getName());
                    if(icon.getDisplayedChild() == 1){
                        icon.showPrevious();
                    }
                }
            }
        });
    }

    private class AppAdapter extends ArrayAdapter<AppInfo> {
        @SuppressWarnings("unchecked")
        private final ArrayList<AppInfo> appList = new ArrayList();

        private AppAdapter(Context context, ArrayList<AppInfo> adapterArrayList) {
            super(context, R.layout.request_item_list, adapterArrayList);
            appList.addAll(adapterArrayList);
        }
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {


            ViewHolder holder;
            if (convertView == null) {
                convertView = ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.request_item_list, parent, false);
                holder = new ViewHolder();
                holder.apkIcon = (ImageView) convertView.findViewById(R.id.IVappIcon);
                holder.apkName = (TextView) convertView.findViewById(R.id.TVappName);
                holder.apkPackage = (TextView) convertView.findViewById(R.id.TVappPackage);
                holder.checker = (CheckBox) convertView.findViewById(R.id.CBappSelect);
                holder.switcherChecked = (ViewSwitcher)convertView.findViewById(R.id.viewSwitcherChecked);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo appInfo = appList.get(position);

            holder.apkPackage.setText(String.valueOf(appInfo.getCode().split("/")[0]+"/"+appInfo.getCode().split("/")[1]));
            holder.apkName.setText(appInfo.getName());

            holder.apkIcon.setImageDrawable(appInfo.getImage());

            holder.switcherChecked.setInAnimation(null);
            holder.switcherChecked.setOutAnimation(null);

            holder.checker.setChecked(appInfo.isSelected());
            if(appInfo.isSelected()) {
                if(holder.switcherChecked.getDisplayedChild() == 0){
                    holder.switcherChecked.showNext();
                }
            } else {
                if(holder.switcherChecked.getDisplayedChild() == 1){
                    holder.switcherChecked.showPrevious();
                }
            }
            return convertView;
        }
    }

    private class ViewHolder {
        TextView apkName;
        TextView apkPackage;
        ImageView apkIcon;
        CheckBox checker;
        ViewSwitcher switcherChecked;
    }

    //Zip Stuff. Better leave that Alone ^^

    private static void deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
    }
}