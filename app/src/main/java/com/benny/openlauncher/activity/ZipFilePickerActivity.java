package com.benny.openlauncher.activity;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.benny.openlauncher.fragment.ZipFilePickerFragment;
import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;

import java.io.File;

@SuppressLint("Registered")
public class ZipFilePickerActivity extends AbstractFilePickerActivity<File> {
    public ZipFilePickerActivity() {
        super();
    }

    @Override
    public AbstractFilePickerFragment<File> getFragment(
            @Nullable final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir, final boolean allowExistingFile,
            final boolean singleClick) {
        AbstractFilePickerFragment<File> fragment = new ZipFilePickerFragment();
        // startPath is allowed to be null. In that case, default folder should be SD-card and not "/"
        fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick);
        return fragment;
    }
}
