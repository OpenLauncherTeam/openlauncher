package com.benny.openlauncher.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.benny.openlauncher.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupHelper {
    public static void backupConfig(Context context, String file) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo p = packageManager.getPackageInfo(context.getPackageName(), 0);
            String dataDir = p.applicationInfo.dataDir;

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ZipOutputStream zos = new ZipOutputStream(bos);

            addFileToZip(zos, dataDir + "/databases/home.db", "home.db");
            addFileToZip(zos, dataDir + "/shared_prefs/app.xml", "app.xml");
            Toast.makeText(context, R.string.toast_backup_success, Toast.LENGTH_SHORT).show();
            zos.flush();
            zos.close();
        } catch (Exception e) {
            Toast.makeText(context, R.string.toast_backup_error, Toast.LENGTH_SHORT).show();
        }
    }

    public static void restoreConfig(Context context, String file) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo p = packageManager.getPackageInfo(context.getPackageName(), 0);
            String dataDir = p.applicationInfo.dataDir;

            extractFileFromZip(file, dataDir + "/databases/home.db", "home.db");
            extractFileFromZip(file, dataDir + "/shared_prefs/app.xml", "app.xml");
            Toast.makeText(context, R.string.toast_backup_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, R.string.toast_backup_error, Toast.LENGTH_SHORT).show();
        }
    }

    public static void addFileToZip(ZipOutputStream outZip, String file, String name) throws Exception {
        byte data[] = new byte[Definitions.BUFFER_SIZE];
        FileInputStream fi = new FileInputStream(file);
        BufferedInputStream inputStream = new BufferedInputStream(fi, Definitions.BUFFER_SIZE);
        ZipEntry entry = new ZipEntry(name);
        outZip.putNextEntry(entry);
        int count;
        while((count = inputStream.read(data, 0, Definitions.BUFFER_SIZE)) != -1) {
            outZip.write(data, 0, count);
        }
        inputStream.close();
    }

    public static boolean extractFileFromZip(String filePath, String file, String name) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filePath)));
        byte data[] = new byte[Definitions.BUFFER_SIZE];
        boolean found = false;

        ZipEntry ze;
        while((ze = inZip.getNextEntry()) != null) {
            if(ze.getName().equals(name)) {
                found = true;
                // delete old file first
                File oldFile = new File(file);
                if(oldFile.exists()) {
                    if(!oldFile.delete()) {
                        throw new Exception("Could not delete " + file);
                    }
                }

                FileOutputStream outFile = new FileOutputStream(file);
                int count = 0;
                while((count = inZip.read(data)) != -1) {
                    outFile.write(data, 0, count);
                }

                outFile.close();
                inZip.closeEntry();
            }
        }
        return found;
    }
}
