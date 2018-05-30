package com.vostrik.elena.photowork.util.impl;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.vostrik.elena.photowork.util.StreamReaderWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * Created by Elena on 28.04.2018.
 */

public class ContextStreamReaderWriter implements StreamReaderWriter {

    private static final String TAG = "CtxtStrReaderWriter";

    Context context;
    String dataFileName;

    public ContextStreamReaderWriter(Context context, String dataFileName) {
        this.context = context;
        this.dataFileName = dataFileName;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // otherwise use internal cache dir
        final String dateFilePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        Log.d(TAG, "getFileDataPath " + dateFilePath + File.separator + dataFileName);
        File file = new File(dateFilePath + File.separator + dataFileName);
        if(!file.exists()) {
            Log.d(TAG, "create dataFile");
            file.createNewFile();
        }
        return new FileInputStream(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        //return context.openFileOutput(dataFileName, MODE_PRIVATE);
        final String dateFilePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        Log.d(TAG, "getFileDataPath " + dateFilePath + File.separator + dataFileName);
        File file = new File(dateFilePath + File.separator + dataFileName);
        if(!file.exists()) {
            Log.d(TAG, "create dataFile");
            file.createNewFile();
        }
        return new FileOutputStream(file);
    }
}
