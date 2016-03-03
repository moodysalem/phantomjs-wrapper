package com.moodysalem.phantomjs.wrapper;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PhantomJS {
    private static final Logger LOG = Logger.getLogger(PhantomJS.class.getName());
    private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir", "~")).resolve("java-phantomjs")
            .resolve(Long.toString(System.currentTimeMillis()));
    private static final Path TEMP_SCRIPT_DIR = TEMP_DIR.resolve("scripts");
    private static final Path TEMP_SOURCE_DIR = TEMP_DIR.resolve("source");
    private static final Path TEMP_RENDER_DIR = TEMP_DIR.resolve("output");
    private static final MessageDigest md;

    // this will store a reference to the executable phantomjs binary after we unzip the resource
    private static File PHANTOM_JS_BINARY = null;

    // get a reference to the executable binary and store it in PHANTOM_JS_BINARY
    static {
        md = getMessageDigest();

        String resourcePath = getZipPath();

        if (resourcePath != null) {
            unzipPhantomJSbin(TEMP_DIR, resourcePath);
        }
    }

    /**
     * Get the sha256 message digest
     *
     * @return sha256 message digest if it is supported
     */
    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOG.log(Level.SEVERE, "SHA-256 algorithm not supported", e);
            return null;
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

                // create the parent directory
                try {
                    Files.createDirectories(filePath.getParent());
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Failed to create file path to file: " + filePath, e);
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

    // used for bytesToHex
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();


    /**
     * Converts a byte array to its hexadecimal representation
     *
     * @param bytes to convert
     * @return hex rep
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * The supported formats for PhantomJS
     */
    public enum Format {
        PDF, PNG, JPEG, BMP, PPM, GIF
    }

    /**
     * The supported width and height units for paper size
     */
    public enum PageSizeUnit {
        in, cm, px
    }

    private static final AtomicLong RENDER_NUMBER = new AtomicLong(0);

    /**
     * Renders html to a document with the specified format
     *
     * @return an input stream for the rendered file
     */
    public static InputStream render(InputStream html,
                                     // these are relevant for printed formats, e.g. pdf
                                     float width, PageSizeUnit widthUnit, float height, PageSizeUnit heightUnit,
                                     // these are especially relevant for responsive websites
                                     int viewportWidth, int viewportHeight,
                                     // the output format
                                     Format format) throws IOException {
        // this is the render script
        InputStream renderScript = PhantomJS.class.getResourceAsStream("render.js");

        if (format == null) {
            throw new IllegalArgumentException("Format is required");
        }

        // printed formats use this
        if (Format.PDF.equals(format)) {
            if (width < 0) {
                throw new IllegalArgumentException("Width must be greater than 0");
            }
            if (height < 0) {
                throw new IllegalArgumentException("Height must be greater than 0");
            }
            if (widthUnit == null) {
                throw new IllegalArgumentException("Width unit is required");
            }
            if (heightUnit == null) {
                throw new IllegalArgumentException("Height unit is required");
            }
        }

        if (viewportHeight < 0 || viewportWidth < 0) {
            throw new IllegalArgumentException("Viewport height and width must be greater than 0");
        }

        // create the parent directories
        Files.createDirectories(TEMP_SOURCE_DIR);
        Files.createDirectories(TEMP_RENDER_DIR);

        final long renderNumber;
        synchronized (RENDER_NUMBER) {
            renderNumber = RENDER_NUMBER.addAndGet(1);
        }

        // create a source file for the source html input stream
        Path sourcePath = TEMP_SOURCE_DIR.resolve("source-" + renderNumber);
        Files.copy(html, sourcePath);

        // the output file
        Path renderPath = TEMP_RENDER_DIR.resolve(String.format("target-%s.%s", renderNumber, format.name().toLowerCase()));

        exec(renderScript,
                width + widthUnit.name(), height + heightUnit.name(),
                // the path of
                Integer.toString(viewportWidth), Integer.toString(viewportHeight),
                // the path of source and target
                sourcePath.toAbsolutePath().toString(),
                renderPath.toAbsolutePath().toString()
        );

        return new FileInputStream(renderPath.toFile());
    }


    public static void exec(InputStream script, String... arguments) throws IOException {
        exec(script, null, arguments);
    }

    private static class LoggerOutputStream extends LogOutputStream {
        private Logger logger;
        private Level level;

        public LoggerOutputStream(Logger logger, Level level) {
            super();
            this.logger = logger;
            this.level = level;
        }

        @Override
        protected void processLine(String s, int i) {
            logger.log(level, s);
        }
    }

    /**
     * Execute a script with options and a list of arguments
     *
     * @param script    path of script to execute
     * @param options   options to execute
     * @param arguments list of arguments
     * @throws IOException if cmd execution fails
     */
    public static void exec(InputStream script, PhantomJSOptions options, String... arguments) throws IOException {
        if (PHANTOM_JS_BINARY == null || !PHANTOM_JS_BINARY.exists()) {
            throw new IllegalStateException("PhantomJS binary not found or failed to initialize");
        }

        if (!PHANTOM_JS_BINARY.canExecute()) {
            throw new IllegalStateException("PhantomJS binary not executable");
        }

        if (script == null) {
            throw new IllegalArgumentException("Script is a required argument");
        }

        // load the script into memory
        byte[] is = toByteArray(script);
        // calculate a hash of the script to use as a filename
        String fname = bytesToHex(md.digest(is)).substring(0, 10);
        Path scriptPath = TEMP_SCRIPT_DIR.resolve(fname);

        // create the parent directory
        Files.createDirectories(TEMP_SCRIPT_DIR);

        // copy the byte array to the file
        try (FileOutputStream fos = new FileOutputStream(scriptPath.toFile())) {
            fos.write(is);
        }

        CommandLine cmd = new CommandLine(PHANTOM_JS_BINARY);
        Map<String, Object> args = new HashMap<>();
        cmd.setSubstitutionMap(args);

        args.put("script", scriptPath.toFile());
        cmd.addArgument("${script}");

        if (options != null) {
            options.apply(cmd, args);
        }

        if (arguments != null && arguments.length > 0) {
            for (String arg : arguments) {
                cmd.addArgument(arg);
            }
        }

        LOG.log(Level.INFO, String.format("Running command: %s", cmd.toString()));
        DefaultExecutor de = new DefaultExecutor();
        de.setStreamHandler(new PumpStreamHandler(new LoggerOutputStream(LOG, Level.INFO), new LoggerOutputStream(LOG, Level.SEVERE)));
        de.execute(cmd);
    }

    /**
     * Read an input stream into a byte array
     *
     * @param is to read
     * @return byte array of inputstream
     * @throws IOException
     */
    private static byte[] toByteArray(InputStream is) throws IOException {
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