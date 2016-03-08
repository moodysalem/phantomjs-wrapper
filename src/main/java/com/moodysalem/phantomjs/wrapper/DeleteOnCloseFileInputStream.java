package com.moodysalem.phantomjs.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class DeleteOnCloseFileInputStream extends FileInputStream {
    private File file;

    public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (file != null) {
                Files.deleteIfExists(file.toPath());
            }
        }
    }
}
