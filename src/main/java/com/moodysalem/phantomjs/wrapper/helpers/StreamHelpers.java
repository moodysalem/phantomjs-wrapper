package com.moodysalem.phantomjs.wrapper.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamHelpers {

	/**
     * Read an input stream into a byte array
     *
     * @param is to read
     * @return byte array of inputstream
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) > 0) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        }
    }
}
