package com.vostrik.elena.photowork.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Elena on 28.04.2018.
 */

public interface StreamReaderWriter {
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;
}
