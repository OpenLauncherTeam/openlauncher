package com.benny.openlauncher.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.benny.openlauncher.R;
import com.benny.openlauncher.model.AppInfo;
import com.benny.openlauncher.util.App;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.Definitions;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class HideAppsFragment extends Fragment {
    private static final String TAG = "RequestActivity";
    private static final boolean DEBUG = true;

    @SuppressWarnings("unchecked")
    private ArrayList<String> _listActivities = new ArrayList();
    @SuppressWarnings("unchecked")
    private ArrayList<AppInfo> _listActivitiesFinal = new ArrayList();
    @SuppressLint("StaticFieldLeak")
    private ViewSwitcher _switcherLoad;
    private AsyncWorkerList _taskList = new AsyncWorkerList();
    private Typeface _tf;


    @SuppressWarnings("unused")
    private ViewSwitcher _viewSwitcher;
    private ListView _grid;
    private AppAdapter _appInfoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.request, container, false);
        _switcherLoad = rootView.findViewById(R.id.viewSwitcherLoadingMain);

        FloatingActionButton fab = rootView.findViewById(R.id.fab_rq);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmSelection();
            }
        });

        if (_taskList.getStatus() == AsyncTask.Status.PENDING) {
            // My AsyncTask has not started yet
            _taskList.execute();
        }

        if (_taskList.getStatus() == AsyncTask.Status.FINISHED) {
            // My AsyncTask is done and onPostExecute was called
            new AsyncWorkerList().execute();
        }

        return rootView;
    }

    public class AsyncWorkerList extends AsyncTask<String, Integer, String> {

        private AsyncWorkerList() {
        }

        @Override
        protected void onPreExecute() {
            List<String> hiddenList = AppSettings.get().getHiddenAppsList();
            _listActivities.addAll(hiddenList);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                // Compare them to installed apps
                prepareData();
                return null;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            populateView();
            //Switch from loading screen to the main view
            _switcherLoad.showNext();

            super.onPostExecute(result);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (DEBUG) Log.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);
    }

    private void confirmSelection() {
        Thread actionSend_Thread = new Thread() {

            @Override
            public void run() {
                int selected = 0;
                ArrayList<String> hiddenList = new ArrayList<>();

                // Get all selected apps
                for (int i = 0; i < _listActivitiesFinal.size(); i++) {
                    if ((_listActivitiesFinal.get(i)).isSelected()) {
                        hiddenList.add((_listActivitiesFinal.get(i)).getCode());
                        selected++;
                    }
                }
                AppSettings.get().setHiddenAppsList(hiddenList);
                getActivity().finish();
            }
        };
        if (!actionSend_Thread.isAlive()) {
            //Prevents the thread to be executed twice (or more) times.
            actionSend_Thread.start();
        }
    }

    private void prepareData() {
        List<App> apps = AppManager.getInstance(getContext()).getNonFilteredApps();

        for (App app : apps) {
            AppInfo tempAppInfo = new AppInfo(
                    app.getPackageName() + "/" + app.getClassName(),
                    app.getLabel(),
                    app.getIconProvider().getDrawableSynchronously(Definitions.NO_SCALE),
                    _listActivities.contains(app.getPackageName() + "/" + app.getClassName())
            );
            _listActivitiesFinal.add(tempAppInfo);
        }
    }

    @SuppressWarnings("unchecked")
    private void populateView() {
        _grid = getActivity().findViewById(R.id.app_grid);

        assert _grid != null;
        _grid.setFastScrollEnabled(true);
        _grid.setFastScrollAlwaysVisible(false);

        _appInfoAdapter = new AppAdapter(getActivity(), _listActivitiesFinal);

        _grid.setAdapter(_appInfoAdapter);
        _grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> AdapterView, View view, int position, long row) {
                AppInfo appInfo = (AppInfo) AdapterView.getItemAtPosition(position);
                CheckBox checker = view.findViewById(R.id.CBappSelect);
                ViewSwitcher icon = view.findViewById(R.id.viewSwitcherChecked);

                checker.toggle();
                appInfo.setSelected(checker.isChecked());

                if (appInfo.isSelected()) {
                    if (DEBUG) Log.v(TAG, "Selected App: " + appInfo.getName());
                    if (icon.getDisplayedChild() == 0) {
                        icon.showNext();
                    }
                } else {
                    if (DEBUG) Log.v(TAG, "Deselected App: " + appInfo.getName());
                    if (icon.getDisplayedChild() == 1) {
                        icon.showPrevious();
                    }
                }
            }
        });
    }

    private class AppAdapter extends ArrayAdapter<AppInfo> {
        @SuppressWarnings("unchecked")

        private AppAdapter(Context context, ArrayList<AppInfo> adapterArrayList) {
            super(context, R.layout.request_item_list, adapterArrayList);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.request_item_list, parent, false);
                holder = new ViewHolder();
                holder._apkIcon = convertView.findViewById(R.id.IVappIcon);
                holder._apkName = convertView.findViewById(R.id.TVappName);
                holder._apkPackage = convertView.findViewById(R.id.TVappPackage);
                holder._checker = convertView.findViewById(R.id.CBappSelect);
                holder._switcherChecked = convertView.findViewById(R.id.viewSwitcherChecked);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo appInfo = getItem(position);

            holder._apkPackage.setText(appInfo.getCode());
            holder._apkPackage.setTypeface(_tf);

            holder._apkName.setText(appInfo.getName());

            holder._apkIcon.setImageDrawable(appInfo.getImage());

            holder._switcherChecked.setInAnimation(null);
            holder._switcherChecked.setOutAnimation(null);

            holder._checker.setChecked(appInfo.isSelected());
            if (appInfo.isSelected()) {
                if (holder._switcherChecked.getDisplayedChild() == 0) {
                    holder._switcherChecked.showNext();
                }
            } else {
                if (holder._switcherChecked.getDisplayedChild() == 1) {
                    holder._switcherChecked.showPrevious();
                }
            }
            return convertView;
        }
    }

    private class ViewHolder {
        TextView _apkName;
        TextView _apkPackage;
        ImageView _apkIcon;
        CheckBox _checker;
        ViewSwitcher _switcherChecked;
    }

}
