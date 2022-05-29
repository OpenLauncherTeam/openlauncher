package com.benny.openlauncher.fragment;

import android.support.annotation.NonNull;

import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

public class ZipFilePickerFragment extends FilePickerFragment {
    // File extension to filter on, including the initial dot.
    private static final String EXTENSION = ".zip";

    /**
     * @param file
     * @return The file extension. If file has no extension, it returns null.
     */
    private String getExtension(@NonNull File file) {
        String path = file.getPath();
        int i = path.lastIndexOf(".");
        if (i < 0) {
            return null;
        } else {
            return path.substring(i);
        }
    }

    @Override
    protected boolean isItemVisible(final File file) {
        // simplified behavior   (see below full code)
        // return isDir(file) || (mode == MODE_FILE || mode == MODE_FILE_AND_DIR);
        if (!isDir(file) && (mode == MODE_FILE || mode == MODE_FILE_AND_DIR)) {
            String ext = getExtension(file);
            return ext != null && EXTENSION.equalsIgnoreCase(ext);
        }
        return isDir(file);
    }

}
