package com.moodysalem.phantomjs.wrapper;

import static com.moodysalem.phantomjs.wrapper.CommandLineArgument.wrapCommandLineArgumentName;

import com.moodysalem.phantomjs.wrapper.beans.BannerInfo;
import com.moodysalem.phantomjs.wrapper.beans.Margin;
import com.moodysalem.phantomjs.wrapper.beans.OperatingSystem;
import com.moodysalem.phantomjs.wrapper.beans.PaperSize;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSExecutionResponse;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJsOptions;
import com.moodysalem.phantomjs.wrapper.beans.RenderOptions;
import com.moodysalem.phantomjs.wrapper.beans.ViewportDimensions;
import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhantomJS {

  private final static Logger logger = LoggerFactory.getLogger(PhantomJS.class);


  private static synchronized String getRenderId() {
    return UUID.randomUUID().toString();
  }


  /**
   * Render the html in the input stream with the following properties using a
   * script included with the wrapper.
   *
   * @param options any phantomjs options to pass to the script
   * @param html to render
   * @param paperSize size of the paper (for printed output formats)
   * @param dimensions viewportDimensions of the viewport
   * @param margin of the paper
   * @param headerInfo how the header is generated
   * @param footerInfo how the footer is generated
   * @param renderFormat the format to render
   * @param jsExecutionTimeout the maximum amount of time to wait for JS to finish execution in
   * milliseconds
   * @param jsInterval the interval
   * @return a stream of the rendered output
   * @throws IOException if any file operations fail
   * @throws RenderException if the render script fails for any reason
   * @deprecated This method will be deleted with the next version. Please use {@link
   * PhantomJS#render(InputStream, RenderOptions)} instead.
   */
  @Deprecated
  public static InputStream render(final PhantomJsOptions options, final InputStream html,
      final PaperSize paperSize, final ViewportDimensions dimensions, final Margin margin,
      final BannerInfo headerInfo, final BannerInfo footerInfo,
      final RenderFormat renderFormat, final Long jsExecutionTimeout, final Long jsInterval)
      throws IOException, RenderException, RenderOptionsException {

    final RenderOptions renderOptions = new RenderOptions().phantomJsOptions(options)
        .paperSize(paperSize).margin(margin).viewportDimensions(dimensions).headerInfo(headerInfo)
        .footerInfo(footerInfo).renderFormat(renderFormat).jsExecutionTimeout(jsExecutionTimeout)
        .jsInterval(jsInterval);

    return render(html, renderOptions);
  }


  /**
   * Render the html in the input stream with the given options.
   *
   * @param html to render
   * @param options for rendering
   * @return a stream of the rendered output
   * @throws IOException if anything goes wrong executing the program
   * @throws RenderException if the render script fails for any reason
   */
  public static InputStream render(final InputStream html, final RenderOptions options)
      throws IOException, RenderException, RenderOptionsException {

    if (html == null || options == null) {
      throw new NullPointerException("All parameters are required.");
    }

    final RenderFormat renderFormat = options.getRenderFormat();
    final PaperSize paperSize = options.getPaperSize();
    final ViewportDimensions viewportDimensions = options.getViewportDimensions();
    final Margin margin = options.getMargin();
    final BannerInfo headerInfo = options.getHeaderInfo();
    final BannerInfo footerInfo = options.getFooterInfo();

    if (renderFormat == null || paperSize == null || viewportDimensions == null || margin == null
        || headerInfo == null || footerInfo == null) {
      throw new RenderOptionsException("RenderOptions must not contain null for properties.");
    }

    final long jsExecutionTimeout = options.getJsExecutionTimeout();
    final long jsInterval = options.getJsInterval();

    if (jsExecutionTimeout < 0 || jsInterval < 0 || (jsExecutionTimeout > 0
        && jsInterval > jsExecutionTimeout) || (jsInterval == 0
        && jsExecutionTimeout > 0)) {
      throw new IllegalArgumentException(
          "Invalid jsExecutionTimeout or jsInterval values provided");
    }

    // The render script
    final InputStream renderScript = PhantomJS.class
        .getResourceAsStream(PhantomJSConstants.DEFAULT_RENDER_SCRIPT);

    // create the parent directories
    Files.createDirectories(PhantomJSConstants.TEMP_SOURCE_DIR);
    Files.createDirectories(PhantomJSConstants.TEMP_RENDER_DIR);

    final String renderId = getRenderId();

    // create a source file for the source html input stream
    final Path sourcePath = PhantomJSConstants.TEMP_SOURCE_DIR
        .resolve(PhantomJSConstants.SOURCE_PREFIX + renderId);
    Files.copy(html, sourcePath);

    // create a source file for the header function
    final Path headerFunctionPath = PhantomJSConstants.TEMP_SOURCE_DIR
        .resolve(PhantomJSConstants.HEADER_PREFIX + renderId);
    Files.copy(new ByteArrayInputStream(headerInfo.getGeneratorFunction().getBytes()),
        headerFunctionPath);

    // create a source file for the footer function
    final Path footerFunctionPath = PhantomJSConstants.TEMP_SOURCE_DIR
        .resolve(PhantomJSConstants.FOOTER_PREFIX + renderId);
    Files.copy(new ByteArrayInputStream(footerInfo.getGeneratorFunction().getBytes()),
        footerFunctionPath);

    // the output file
    final Path renderPath = PhantomJSConstants.TEMP_RENDER_DIR.resolve(String
        .format(PhantomJSConstants.TARGET_PREFIX + "%s.%s", renderId,
            renderFormat.name().toLowerCase()));

    final CommandLineArgument[] commandLineArguments = new CommandLineArgument[17];
    commandLineArguments[0] = new CommandLineArgument(paperSize.getWidth());
    commandLineArguments[1] = new CommandLineArgument(paperSize.getHeight());
    commandLineArguments[2] = new CommandLineArgument(viewportDimensions.getWidth());
    commandLineArguments[3] = new CommandLineArgument(viewportDimensions.getHeight());
    commandLineArguments[4] = new CommandLineArgument(margin.getTop());
    commandLineArguments[5] = new CommandLineArgument(margin.getRight());
    commandLineArguments[6] = new CommandLineArgument(margin.getBottom());
    commandLineArguments[7] = new CommandLineArgument(margin.getLeft());
    commandLineArguments[8] = new CommandLineArgument(headerInfo.getHeight());
    commandLineArguments[9] =
        new CommandLineArgument(wrapCommandLineArgumentName(PhantomJSConstants.HEADERFUNCTION_FILE),
            PhantomJSConstants.HEADERFUNCTION_FILE, headerFunctionPath.toFile());
    commandLineArguments[10] = new CommandLineArgument(footerInfo.getHeight());
    commandLineArguments[11] =
        new CommandLineArgument(wrapCommandLineArgumentName(PhantomJSConstants.FOOTERFUNCTION_FILE),
            PhantomJSConstants.FOOTERFUNCTION_FILE, footerFunctionPath.toFile());
    commandLineArguments[12] = new CommandLineArgument(OperatingSystem.get().name());
    commandLineArguments[13] = new CommandLineArgument(
        wrapCommandLineArgumentName(PhantomJSConstants.SOURCEPATH_TEMPLATENAME),
        PhantomJSConstants.SOURCEPATH_TEMPLATENAME, sourcePath.toFile());
    commandLineArguments[14] = new CommandLineArgument(
        wrapCommandLineArgumentName(PhantomJSConstants.RENDERPATH_TEMPLATENAME),
        PhantomJSConstants.RENDERPATH_TEMPLATENAME, renderPath.toFile());
    commandLineArguments[15] =
        new CommandLineArgument(
            wrapCommandLineArgumentName(PhantomJSConstants.JS_EXECUTION_TIMEOUT_TEMPLATENAME),
            PhantomJSConstants.JS_EXECUTION_TIMEOUT_TEMPLATENAME, jsExecutionTimeout);
    commandLineArguments[16] = new CommandLineArgument(
        wrapCommandLineArgumentName(PhantomJSConstants.JSINTERVAL_TEMPLATENAME),
        PhantomJSConstants.JSINTERVAL_TEMPLATENAME, jsInterval);

    final PhantomJSExecutionResponse phantomJSExecutionResponse = exec(renderScript,
        options.getPhantomJsOptions(), commandLineArguments);

    final int renderExitCode = phantomJSExecutionResponse.getExitCode();

    Files.deleteIfExists(sourcePath);
    Files.deleteIfExists(headerFunctionPath);
    Files.deleteIfExists(footerFunctionPath);

    if (renderExitCode == 0) {
      return new DeleteOnCloseFileInputStream(renderPath.toFile());
    }

    final String error;

    switch (renderExitCode) {
      case 1:
        error = "Failed to read source HTML file from input stream";
        break;
      case 2:
        error = "Failed to set zoom on document body";
        break;
      case 3:
        error = "Failed to render PDF to output";
        break;
      case 4:
        error = "Failed to read header function";
        break;
      case 5:
        error = "Failed to read footer function";
        break;
      case 6:
        error = "JS execution did not finish within the wait time";
        break;
      default:
        error = "Render script failed for an unknown reason.";
        break;
    }

    throw new RenderException(error);
  }


  public static PhantomJSExecutionResponse exec(final InputStream script,
      final CommandLineArgument... arguments) throws IOException {
    return exec(script, null, arguments);
  }


  /**
   * Execute a script with options and a list of arguments
   *
   * @param script path of script to execute
   * @param options options to execute
   * @param arguments list of arguments
   * @return the exit code of the script
   * @throws IOException if cmd execution fails
   */
  public static PhantomJSExecutionResponse exec(final InputStream script,
      final PhantomJsOptions options, final CommandLineArgument... arguments) throws IOException {
    if (!PhantomJSSetup.isInitialized()) {
      throw new IllegalStateException("Unable to find and execute PhantomJS binaries");
    }

    if (script == null) {
      throw new IllegalArgumentException("Script is a required argument");
    }

    // the path where the script will be copied to
    final String renderId = getRenderId();
    final Path scriptPath = PhantomJSConstants.TEMP_SCRIPT_DIR
        .resolve(PhantomJSConstants.SCRIPT_PREFIX + renderId);

    // create the parent directory
    Files.createDirectories(PhantomJSConstants.TEMP_SCRIPT_DIR);

    // copy the script to the path
    Files.copy(script, scriptPath);

    // start building the phantomjs binary call
    final CommandLine cmd = new CommandLine(PhantomJSSetup.getPhantomJsBinary());
    final Map<String, Object> args = new HashMap<>();
    cmd.setSubstitutionMap(args);

    // add options to the phantomjs call
    if (options != null) {
      options.apply(cmd, args);
    }

    // then script
    args.put("_script_path", scriptPath.toFile());
    cmd.addArgument("${_script_path}");

    // then any additional arguments
    if (arguments != null) {
      for (final CommandLineArgument arg : arguments) {
        if (arg != null) {
          arg.apply(cmd, args);
        }
      }
    }

    logger.info("Running command: [{}]", cmd);

    final InfoLoggerOutputStream stdOutLogger = new InfoLoggerOutputStream();
    final ErrorLoggerOutputStream stdErrLogger = new ErrorLoggerOutputStream();

    final DefaultExecutor de = new DefaultExecutor();
    de.setStreamHandler(new PumpStreamHandler(stdOutLogger, stdErrLogger));

    int code;
    try {
      code = de.execute(cmd);
    } catch (final ExecuteException exe) {
      code = exe.getExitValue();
    }

    // remove the script after running it
    Files.deleteIfExists(scriptPath);

    logger.info("Execution Completed");

    return new PhantomJSExecutionResponse(code, stdOutLogger.getMessageContents(),
        stdErrLogger.getMessageContents());
  }

  /**
   * Abstract logging class.
   */
  private static abstract class LoggerOutputStream extends LogOutputStream {

    protected final StringBuffer messageContents;


    LoggerOutputStream() {
      messageContents = new StringBuffer();
    }


    String getMessageContents() {
      return messageContents.toString();
    }
  }

  /**
   * Info level logger.
   */
  private static class InfoLoggerOutputStream extends LoggerOutputStream {

    @Override
    protected void processLine(final String s, final int i) {
      logger.info("PhantomJS script logged: {}", s);
      messageContents.append(s).append(System.lineSeparator());
    }
  }

  /**
   * Error level logger.
   */
  private static class ErrorLoggerOutputStream extends LoggerOutputStream {

    @Override
    protected void processLine(final String s, final int i) {
      logger.error("PhantomJS script logged: {}", s);
      messageContents.append(s).append(System.lineSeparator());
    }
  }
}