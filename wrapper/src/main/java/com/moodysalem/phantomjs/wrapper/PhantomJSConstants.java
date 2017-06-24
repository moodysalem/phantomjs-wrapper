package com.moodysalem.phantomjs.wrapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

class PhantomJSConstants {

  static final String DEFAULT_RENDER_SCRIPT = "defaultrender.js";

  static final String ZIP_EXTENSION = ".zip";

  static final String
      PHANTOM_BINARIES_RESOURCEPATH = "com/moodysalem/phantomjs/wrapper/phantomjs-2.1.1-%s",
      PHANTOM_BINARIES_PACKAGENAME = "phantomjs-2.1.1-%s",
      PHANTOM_BINARIES_BIN = "bin/phantomjs%s",
      PHANTOM_BINARIES_WINDOWS = "windows",
      PHANTOM_BINARIES_MAC = "macosx",
      PHANTOM_BINARIES_UNIX = "linux-x86_64";


  private static final String JVM_UUID = UUID.randomUUID().toString();

  static final Path TEMP_DIR =
      Paths.get(System.getProperty("java.io.tmpdir", "/tmp")).resolve("java-phantomjs");

  static final Path
      TEMP_SCRIPT_DIR = TEMP_DIR.resolve("scripts-" + JVM_UUID),
      TEMP_SOURCE_DIR = TEMP_DIR.resolve("source-" + JVM_UUID),
      TEMP_RENDER_DIR = TEMP_DIR.resolve("output-" + JVM_UUID);

  static final String
      HEADER_PREFIX = "header-",
      FOOTER_PREFIX = "footer-",
      TARGET_PREFIX = "target-",
      SCRIPT_PREFIX = "script-",
      SOURCE_PREFIX = "source-";

  static final String
      HEADERFUNCTION_FILE = "headerFunctionFile",
      FOOTERFUNCTION_FILE = "footerFunctionFile";

  static final String
      SOURCEPATH_TEMPLATENAME = "sourcePath",
      RENDERPATH_TEMPLATENAME = "renderPath-";

  static final String
      JS_EXECUTION_TIMEOUT_TEMPLATENAME = "jsExecutionTimeout",
      JSINTERVAL_TEMPLATENAME = "jsInterval";

  static final String
      SHUTDOWN_HOOK_THREAD_NAME = "PhantomJSSetupShutDownHook";
}
