package com.benny.openlauncher.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.benny.openlauncher.R;
import com.benny.openlauncher.fragment.SettingsMasterFragment;
import com.benny.openlauncher.util.BackupHelper;
import com.benny.openlauncher.util.Definitions;
import com.nononsenseapps.filepicker.Utils;

import net.gsantner.opoc.util.ContextUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends ThemeActivity {
    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    private SettingsMasterFragment prefFrag;

    public void onCreate(Bundle b) {
        // must be applied before setContentView
        super.onCreate(b);
        ContextUtils contextUtils = new ContextUtils(this);
        contextUtils.setAppLanguage(_appSettings.getLanguage());

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.pref_title__settings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setBackgroundColor(_appSettings.getPrimaryColor());

        prefFrag = new SettingsMasterFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, prefFrag).commit();

        // if system exit is called the app will open settings activity again
        // this pushes the user back out to the home activity
        if (_appSettings.getAppRestartRequired()) {
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            switch (requestCode) {
                case Definitions.INTENT_BACKUP:
                    BackupHelper.backupConfig(this, new File(Utils.getFileForUri(files.get(0)).getAbsolutePath() + "/openlauncher.zip").toString());
                    break;
                case Definitions.INTENT_RESTORE:
                    BackupHelper.restoreConfig(this, Utils.getFileForUri(files.get(0)).toString());
                    System.exit(0);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (prefFrag != null && prefFrag.canGoBack()) {
            prefFrag.goBack();
            return;
        }
        super.onBackPressed();
    }
}
