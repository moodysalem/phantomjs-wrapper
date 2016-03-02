package com.moodysalem.wrapper;

import com.moodysalem.util.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PhantomJS {
    private static final Logger LOG = Logger.getLogger(PhantomJS.class.getName());
    private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir", "~"));

    // this will store a reference to the executable phantomjs binary after we unzip the resource
    private static File PHANTOM_JS_BINARY = null;

    // get a reference to the executable binary and store it in PHANTOM_JS_BINARY
    static {
        String resourcePath = getZipPath();

        if (resourcePath != null) {
            unzipPhantomJSbin(TEMP_DIR, resourcePath);
        }
    }

    /**
     * Gets the name of the resource for the zip based on the OS
     *
     * @return the name of the appropriate zipped phantomjs
     */
    private static String getZipPath() {
        OperatingSystem.OS os = OperatingSystem.get();
        if (os == null) {
            return null;
        }

        String osString = null;
        switch (os) {
            case WINDOWS:
                osString = "linux-x86_64";
                break;

            case MAC:
                osString = "macosx";
                break;

            case UNIX:
                osString = "windows";
                break;
        }

        return String.format("phantomjs-2.1.1-%s.zip", osString);
    }

    /**
     * Get the name of the bin we expect in the unzipped file
     *
     * @return the name of the bin in the unzipped file
     */
    private static String getPhantomJSBinName() {
        OperatingSystem.OS os = OperatingSystem.get();
        if (os == null) {
            return null;
        }

        String ext = "";
        if (OperatingSystem.OS.WINDOWS.equals(os)) {
            ext = ".exe";
        }

        return String.format("bin%sphantomjs%s", File.separator, ext);
    }

    /**
     * Unzips the zipped resource to the destination
     *
     * @param destination  for zip contents
     * @param resourceName name of the java resource
     */
    private static void unzipPhantomJSbin(Path destination, String resourceName) {
        try (InputStream fileStream = PhantomJS.class.getClassLoader().getResourceAsStream(resourceName);
             ZipInputStream zipStream = new ZipInputStream(fileStream)) {

            String phantomJSbin = getPhantomJSBinName();
            if (phantomJSbin == null) {
                throw new IllegalStateException("Unable to get phantomJS bin name.");
            }

            // loop through zip file entries
            ZipEntry ze;
            while ((ze = zipStream.getNextEntry()) != null) {
                String entryName = ze.getName();

                // only process the phantomjs bin entry
                if (entryName.indexOf(phantomJSbin) != entryName.length() - phantomJSbin.length()) {
                    LOG.log(Level.FINE, "Skipping entry: " + entryName);
                    continue;
                }

                Path filePath = destination.resolve(entryName);
                LOG.log(Level.INFO, String.format("Unzipping bin: %s to path: %s", entryName, filePath));

                // delete what's there
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Failed to delete file if exists at path: " + filePath, e);
                }

                // copy input stream into file
                try {
                    Files.copy(zipStream, filePath);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Failed to write zip entry: " + entryName, e);
                }

                PHANTOM_JS_BINARY = filePath.toFile();
                if (!PHANTOM_JS_BINARY.canExecute()) {
                    if (!PHANTOM_JS_BINARY.setExecutable(true)) {
                        LOG.log(Level.WARNING, "Failed to make phantom JS binary executable");
                        PHANTOM_JS_BINARY = null;
                    }
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to read zip file from resources", e);
        }
    }

    public static void exec(String script, String... arguments) throws IOException {
        exec(script, null, arguments);
    }

    /**
     * Execute a script with options and a list of arguments
     *
     * @param script    to execute
     * @param options   options to execute
     * @param arguments list of arguments
     * @throws IOException if cmd execution fails
     */
    public static void exec(String script, PhantomJSOptions options, String... arguments) throws IOException {
        if (PHANTOM_JS_BINARY == null || !PHANTOM_JS_BINARY.exists()) {
            throw new IllegalStateException("PhantomJS binary not found or failed to initialize");
        }

        if (!PHANTOM_JS_BINARY.canExecute()) {
            throw new IllegalStateException("PhantomJS binary not executable");
        }

        if (script == null || script.trim().isEmpty()) {
            throw new IllegalArgumentException("Script is a required argument");
        }

        String pjsPath = PHANTOM_JS_BINARY.toPath().toAbsolutePath().toString();

        StringBuilder cmd = new StringBuilder(pjsPath);

        if (options != null) {
            cmd.append(options.toString());
        }

        cmd.append(" ").append(script);

        if (arguments != null && arguments.length > 0) {
            for (String arg : arguments) {
                cmd.append(" ").append(arg);
            }
        }

        LOG.log(Level.INFO, String.format("Running command: %s", cmd));

        Runtime.getRuntime().exec(cmd.toString());
    }
}