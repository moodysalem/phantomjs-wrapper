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

	protected final static Logger logger = Logger.getLogger(PhantomJSSetup.class.getName());

	// this will store a reference to the executable phantomjs binary after we unzip the resource
	protected final static File PHANTOM_JS_BINARY = initializeBinaries();

	// get a reference to the executable binary and store it in PHANTOM_JS_BINARY
	private static File initializeBinaries() throws IllegalStateException {
		final String resourcePath = getZipPath(PhantomJSConstants.PHANTOM_BINARIES_RESOURCEPATH);

		logger.info("Initializing PhantomJS with resource path: " + resourcePath);

		//As long as we have a resource path, and that the binaries have not already been initialized, initialize them
		if (null != resourcePath && null == PHANTOM_JS_BINARY) {
            initializeShutDownHook(); 
			return unzipPhantomJSbin(PhantomJSConstants.TEMP_DIR, resourcePath);
		}
		else{
			throw new IllegalStateException("Instantiation mechanism was unable to determine platform type for PhantomJS extraction.");
		}
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

		return String.format(PhantomJSConstants.PHANTOM_BINARIES_BIN, ext);
	}

	/**
	 * Unzips the zipped resource to the destination
	 *
	 * @param destination  for zip contents
	 * @param resourceName name of the java resource
	 */
	private static File unzipPhantomJSbin(Path destination, String resourceName) throws IllegalStateException {

		Path absoluteResource = Paths.get(
				destination.toString()
				.concat(File.separator
						.concat(getZipPath(PhantomJSConstants.PHANTOM_BINARIES_PACKAGENAME)
								.replace(PhantomJSConstants.ZIP_EXTENSION, "")
								.concat(File.separator)
								.concat(getPhantomJSBinName()))));

		logger.finer("Verifying existence of PhantomJS executable at: " + absoluteResource.toString());

		if (!Files.exists(absoluteResource)) {
			
			File binary = null;
			
			try (InputStream fileStream = PhantomJSSetup.class.getClassLoader().getResourceAsStream(resourceName);
					ZipInputStream zipStream = new ZipInputStream(fileStream)) {

				logger.info("Unzipping PhantomJS to resource path: " + destination);

				String phantomJSbin = getPhantomJSBinName();
				if (phantomJSbin == null) {
					throw new IllegalStateException("Unable to get PhantomJS bin name.");
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
						throw new IllegalStateException("Failed to delete file if exists at path: " + filePath, e);
					}

					// create the parent directory
					try {
						Files.createDirectories(filePath.getParent());
					} catch (IOException e) {
						throw new IllegalStateException("Failed to create file path to file: " + filePath, e);
					}

					// copy input stream into file
					try {
						Files.copy(zipStream, filePath);
					} catch (IOException e) {
						throw new IllegalStateException("Failed to write zip entry: " + entryName, e);
					}

					binary = filePath.toFile();
					
					if (!binary.canExecute()) {
						if (!binary.setExecutable(true)) {
							throw new IllegalStateException("PhantomJSSetup failed to make PhantomJS binary executable");
						}
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException("Failed to read zip file from resources", e);
			} 
			
			return binary;
		}
		else {
			logger.fine("PhantomJS exists under resource path: " + destination);
			return absoluteResource.toFile();
		}
	}

	/**
	 * Gets the name of the resource for the zip based on the OS
	 * @param resourceName
	 *
	 * @return the name of the appropriate zipped phantomjs
	 */
	protected static String getZipPath(String resourceName) {
		OperatingSystem.OS os = OperatingSystem.get();
		if (os == null) {
			return null;
		}

		String osString = "";

		switch (os) {
		case WINDOWS:
			osString = PhantomJSConstants.PHANTOM_BINARIES_WINDOWS;
			break;

		case MAC:
			osString = PhantomJSConstants.PHANTOM_BINARIES_MAC;
			break;

		case UNIX:
			osString = PhantomJSConstants.PHANTOM_BINARIES_UNIX;
			break;
		}

		return String.format(resourceName
				.concat(PhantomJSConstants.ZIP_EXTENSION), osString);
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

	/**
	 * Shutdown hook in charge of cleaning of JVM specific folders during JVM shutdown.
	 * This hook needs to be added during initialization of the class.
	 */
	private static void initializeShutDownHook() {

		Runtime runtime = Runtime.getRuntime();

		Thread shutdownThread = new Thread(PhantomJSConstants.SHUTDOWN_HOOK_THREAD_NAME){
			public void run(){
				try {
					Files.delete(PhantomJSConstants.TEMP_SOURCE_DIR);
					Files.delete(PhantomJSConstants.TEMP_SCRIPT_DIR);
					Files.delete(PhantomJSConstants.TEMP_RENDER_DIR);

				} catch (Exception e) {
					logger.warning("PhantomJSSetup was unable to clean up temporary directories under: " + PhantomJSConstants.TEMP_DIR + ". Caused by: " + e.getMessage());
				}
			}
		};
		
		runtime.addShutdownHook(shutdownThread);
	}


}
