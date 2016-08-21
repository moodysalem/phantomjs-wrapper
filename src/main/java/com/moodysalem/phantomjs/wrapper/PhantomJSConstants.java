package com.moodysalem.phantomjs.wrapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class PhantomJSConstants {
	
	protected static final String DEFAULT_RENDER_SCRIPT = "defaultrender.js";
	
	protected static final String TEMP_FOLDER_NAME = "java-phantomjs";
	protected static final String ZIP_EXTENSION = ".zip";
	
	protected static final String PHANTOM_BINARIES_RESOURCEPATH = "com/moodysalem/phantomjs/wrapper/phantomjs-2.1.1-%s";
	protected static final String PHANTOM_BINARIES_PACKAGENAME = "phantomjs-2.1.1-%s";
	protected static final String PHANTOM_BINARIES_BIN = "bin/phantomjs%s";
	protected static final String PHANTOM_BINARIES_WINDOWS = "windows";
	protected static final String PHANTOM_BINARIES_MAC = "macosx";
	protected static final String PHANTOM_BINARIES_UNIX = "linux-x86_64";	
	
	protected static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir", "/tmp"))
			.resolve(TEMP_FOLDER_NAME);
	
	protected static final String JVM_UUID = UUID.randomUUID().toString();
	
	protected static final Path TEMP_SCRIPT_DIR = TEMP_DIR.resolve("scripts-" + JVM_UUID);
	protected static final Path TEMP_SOURCE_DIR = TEMP_DIR.resolve("source-" + JVM_UUID);
	protected static final Path TEMP_RENDER_DIR = TEMP_DIR.resolve("output-" + JVM_UUID);
	
	protected static final String HEADER_PREFIX = "header-";
	protected static final String FOOTER_PREFIX = "footer-";
	protected static final String TARGET_PREFIX = "target-";
	protected static final String SCRIPT_PREFIX = "script-";
	protected static final String SOURCE_PREFIX = "source-";

	protected static final String HEADERFUNCTION_FILE = "headerFunctionFile";
	protected static final String FOOTERFUNCTION_FILE = "footerFunctionFile";
	
	protected static final String SOURCEPATH_TEMPLATENAME = "sourcePath";
	protected static final String RENDERPATH_TEMPLATENAME = "renderPath-";
	
	protected static final String JSWAIT_TEMPLATENAME = "jsWait";
	protected static final String JSINTERVAL_TEMPLATENAME = "jsInterval";
	
	protected static final String SHUTDOWN_HOOK_THREAD_NAME = "PhantomJSSetupShutDownHook";
}
