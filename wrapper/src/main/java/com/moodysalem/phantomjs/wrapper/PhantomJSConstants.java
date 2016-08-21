package com.moodysalem.phantomjs.wrapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

class PhantomJSConstants {

    static final String DEFAULT_RENDER_SCRIPT = "defaultrender.js";

    private static final String TEMP_FOLDER_NAME = "java-phantomjs";
    static final String ZIP_EXTENSION = ".zip";

    static final String PHANTOM_BINARIES_RESOURCEPATH = "com/moodysalem/phantomjs/wrapper/phantomjs-2.1.1-%s";
    static final String PHANTOM_BINARIES_PACKAGENAME = "phantomjs-2.1.1-%s";
    static final String PHANTOM_BINARIES_BIN = "bin/phantomjs%s";
    static final String PHANTOM_BINARIES_WINDOWS = "windows";
    static final String PHANTOM_BINARIES_MAC = "macosx";
    static final String PHANTOM_BINARIES_UNIX = "linux-x86_64";

    static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir", "/tmp"))
            .resolve(TEMP_FOLDER_NAME);

    private static final String JVM_UUID = UUID.randomUUID().toString();

    static final Path TEMP_SCRIPT_DIR = TEMP_DIR.resolve("scripts-" + JVM_UUID);
    static final Path TEMP_SOURCE_DIR = TEMP_DIR.resolve("source-" + JVM_UUID);
    static final Path TEMP_RENDER_DIR = TEMP_DIR.resolve("output-" + JVM_UUID);

    static final String HEADER_PREFIX = "header-";
    static final String FOOTER_PREFIX = "footer-";
    static final String TARGET_PREFIX = "target-";
    static final String SCRIPT_PREFIX = "script-";
    static final String SOURCE_PREFIX = "source-";

    static final String HEADERFUNCTION_FILE = "headerFunctionFile";
    static final String FOOTERFUNCTION_FILE = "footerFunctionFile";

    static final String SOURCEPATH_TEMPLATENAME = "sourcePath";
    static final String RENDERPATH_TEMPLATENAME = "renderPath-";

    static final String JSWAIT_TEMPLATENAME = "jsWait";
    static final String JSINTERVAL_TEMPLATENAME = "jsInterval";

    static final String SHUTDOWN_HOOK_THREAD_NAME = "PhantomJSSetupShutDownHook";
}
