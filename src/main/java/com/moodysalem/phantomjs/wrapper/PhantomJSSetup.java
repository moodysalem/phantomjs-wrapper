package com.moodysalem.phantomjs.wrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.moodysalem.phantomjs.wrapper.beans.OperatingSystem;

public class PhantomJSSetup{
	
	//TODO Phantom Max Reuse count
	
	protected final static Logger logger = Logger.getLogger(PhantomJSSetup.class.getName());
	
	// this will store a reference to the executable phantomjs binary after we unzip the resource
    protected static File PHANTOM_JS_BINARY = null;
	
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

        return String.format("bin/phantomjs%s", ext);
    }

	/**
     * Unzips the zipped resource to the destination
     *
     * @param destination  for zip contents
     * @param resourceName name of the java resource
     */
    protected static void unzipPhantomJSbin(Path destination, String resourceName) {
        try (InputStream fileStream = PhantomJS.class.getClassLoader().getResourceAsStream(resourceName);
             ZipInputStream zipStream = new ZipInputStream(fileStream)) {

        	logger.info("Unzipping PhantomJS to resource path: " + destination);
        	
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
                    logger.log(Level.FINE, "Skipping entry: " + entryName);
                    continue;
                }

                Path filePath = destination.resolve(entryName);
                logger.log(Level.INFO, String.format("Unzipping bin: %s to path: %s", entryName, filePath));

                // delete what's there
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Failed to delete file if exists at path: " + filePath, e);
                }

                // create the parent directory
                try {
                    Files.createDirectories(filePath.getParent());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Failed to create file path to file: " + filePath, e);
                }

                // copy input stream into file
                try {
                    Files.copy(zipStream, filePath);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Failed to write zip entry: " + entryName, e);
                }

                PHANTOM_JS_BINARY = filePath.toFile();
                if (!PHANTOM_JS_BINARY.canExecute()) {
                    if (!PHANTOM_JS_BINARY.setExecutable(true)) {
                        logger.log(Level.WARNING, "Failed to make phantom JS binary executable");
                        PHANTOM_JS_BINARY = null;
                    }
                }

                break;
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read zip file from resources", e);
        }
    }
	
	/**
     * Gets the name of the resource for the zip based on the OS
     *
     * @return the name of the appropriate zipped phantomjs
     */
    protected static String getZipPath() {
        OperatingSystem.OS os = OperatingSystem.get();
        if (os == null) {
            return null;
        }

        String osString = "";
        
        switch (os) {
            case WINDOWS:
                osString = "windows";
                break;

            case MAC:
                osString = "macosx";
                break;

            case UNIX:
                osString = "linux-x86_64";
                break;
        }

        return String.format("com/moodysalem/phantomjs/wrapper/phantomjs-2.1.1-%s.zip", osString);
    }
	
	/**
     * Get the sha256 message digest
     *
     * @return sha256 message digest if it is supported
     */
    protected static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "SHA-256 algorithm not supported", e);
            return null;
        }
    }

	protected static final Path TEMP_DIR = Paths.get("/").resolve("java-phantomjs") //System.getProperty("java.io.tmpdir", "~")
	.resolve(Long.toString(System.currentTimeMillis()));
	protected static final Path TEMP_SCRIPT_DIR = TEMP_DIR.resolve("scripts");
	protected static final Path TEMP_SOURCE_DIR = TEMP_DIR.resolve("source");
	protected static final Path TEMP_RENDER_DIR = TEMP_DIR.resolve("output");
}
