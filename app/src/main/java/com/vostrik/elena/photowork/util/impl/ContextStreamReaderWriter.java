package com.vostrik.elena.photowork.util.impl;

import android.content.Context;

import com.vostrik.elena.photowork.util.StreamReaderWriter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Elena on 28.04.2018.
 */

public class ContextStreamReaderWriter implements StreamReaderWriter {
    Context context;
    String dataFileName;

    public ContextStreamReaderWriter(Context context, String dataFileName) {
        this.context = context;
        this.dataFileName = dataFileName;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return context.openFileInput(dataFileName);
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return context.openFileOutput(dataFileName, Context.MODE_PRIVATE);
    }
}
