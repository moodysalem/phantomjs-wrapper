package com.moodysalem.phantomjs.wrapper;

import com.moodysalem.phantomjs.wrapper.beans.*;
import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;
import org.apache.commons.exec.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.moodysalem.phantomjs.wrapper.CommandLineArgument.wrapCommandLineArgumentName;

public class PhantomJS {
    private final static Logger logger = Logger.getLogger(PhantomJS.class.getName());

    private static synchronized String getRenderId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Another way to call PhantomJS#render using the RenderOptions to specify all the common options
     *
     * @param html    to render
     * @param options for rendering
     * @return same as PhantomJS#render
     * @throws IOException     if anything goes wrong executing the program
     * @throws RenderException if the render script fails for any reason
     */
    public static InputStream render(final InputStream html, final RenderOptions options) throws IOException, RenderException {
        return render(
            options.getOptions(), html,
            options.getPaperSize(), options.getDimensions(),
            options.getMargin(), options.getHeaderInfo(), options.getFooterInfo(), options.getRenderFormat(),
            options.getJsWait(), options.getJsInterval()
        );
    }

    /**
     * Render the html in the input stream with the following properties using a script included with the wrapper
     *
     * @param options      any phantomjs options to pass to the script
     * @param html         to render
     * @param paperSize    size of the paper (for printed output formats)
     * @param dimensions   dimensions of the viewport
     * @param margin       of the paper
     * @param headerInfo   how the header is generated
     * @param footerInfo   how the footer is generated
     * @param renderFormat the format to render
     * @param jsWait       the maximum amount of time to wait for JS to finish execution in milliseconds
     * @param jsInterval   the interval
     * @return a stream of the rendered output
     * @throws IOException     if any file operations fail
     * @throws RenderException if the render script fails for any reason
     */
    public static InputStream render(
        final PhantomJSOptions options, final InputStream html,
        final PaperSize paperSize, final ViewportDimensions dimensions,
        final Margin margin, final BannerInfo headerInfo, final BannerInfo footerInfo,
        final RenderFormat renderFormat, final Long jsWait, final Long jsInterval
    ) throws IOException, RenderException {
        if (html == null || renderFormat == null || paperSize == null || dimensions == null || margin == null ||
            headerInfo == null || footerInfo == null || jsWait == null || jsInterval == null) {
            throw new NullPointerException("All parameters are required");
        }

        if (jsWait < 0 || jsInterval < 0 || (jsWait > 0 && jsInterval > jsWait) || (jsInterval == 0 && jsWait > 0)) {
            throw new IllegalArgumentException("Invalid jsWait or jsInterval values provided");
        }

        // The render script
        final InputStream renderScript = PhantomJS.class.getResourceAsStream(PhantomJSConstants.DEFAULT_RENDER_SCRIPT);

        // create the parent directories
        Files.createDirectories(PhantomJSConstants.TEMP_SOURCE_DIR);
        Files.createDirectories(PhantomJSConstants.TEMP_RENDER_DIR);

        final String renderId = getRenderId();

        // create a source file for the source html input stream
        final Path sourcePath = PhantomJSConstants.TEMP_SOURCE_DIR.resolve(PhantomJSConstants.SOURCE_PREFIX + renderId);
        Files.copy(html, sourcePath);

        // create a source file for the header function
        final Path headerFunctionPath = PhantomJSConstants.TEMP_SOURCE_DIR.resolve(PhantomJSConstants.HEADER_PREFIX + renderId);
        Files.copy(new ByteArrayInputStream(headerInfo.getGeneratorFunction().getBytes()), headerFunctionPath);

        // create a source file for the footer function
        final Path footerFunctionPath = PhantomJSConstants.TEMP_SOURCE_DIR.resolve(PhantomJSConstants.FOOTER_PREFIX + renderId);
        Files.copy(new ByteArrayInputStream(footerInfo.getGeneratorFunction().getBytes()), footerFunctionPath);

        // the output file
        final Path renderPath = PhantomJSConstants.TEMP_RENDER_DIR.resolve(String.format(PhantomJSConstants.TARGET_PREFIX + "%s.%s", renderId, renderFormat.name().toLowerCase()));

        final PhantomJSExecutionResponse phantomJSExecutionResponse = exec(
            renderScript,
            options,
            new CommandLineArgument(paperSize.getWidth()), new CommandLineArgument(paperSize.getHeight()),
            new CommandLineArgument(dimensions.getWidth()), new CommandLineArgument(dimensions.getHeight()),
            new CommandLineArgument(margin.getTop()), new CommandLineArgument(margin.getRight()),
            new CommandLineArgument(margin.getBottom()), new CommandLineArgument(margin.getLeft()),
            new CommandLineArgument(headerInfo.getHeight()), new CommandLineArgument(wrapCommandLineArgumentName(PhantomJSConstants.HEADERFUNCTION_FILE), PhantomJSConstants.HEADERFUNCTION_FILE, headerFunctionPath.toFile()),
            new CommandLineArgument(footerInfo.getHeight()), new CommandLineArgument(wrapCommandLineArgumentName(PhantomJSConstants.FOOTERFUNCTION_FILE), PhantomJSConstants.FOOTERFUNCTION_FILE, footerFunctionPath.toFile()),
            new CommandLineArgument(OperatingSystem.get().name()),
            new CommandLineArgument(wrapCommandLineArgumentName(PhantomJSConstants.SOURCEPATH_TEMPLATENAME), PhantomJSConstants.SOURCEPATH_TEMPLATENAME, sourcePath.toFile()),
            new CommandLineArgument(wrapCommandLineArgumentName(PhantomJSConstants.RENDERPATH_TEMPLATENAME), PhantomJSConstants.RENDERPATH_TEMPLATENAME, renderPath.toFile()),
            new CommandLineArgument(wrapCommandLineArgumentName(PhantomJSConstants.JSWAIT_TEMPLATENAME), PhantomJSConstants.JSWAIT_TEMPLATENAME, jsWait),
            new CommandLineArgument(wrapCommandLineArgumentName(PhantomJSConstants.JSINTERVAL_TEMPLATENAME), PhantomJSConstants.JSINTERVAL_TEMPLATENAME, jsInterval)
        );

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


    public static PhantomJSExecutionResponse exec(InputStream script, CommandLineArgument... arguments) throws IOException {
        return exec(script, null, arguments);
    }

    /**
     * Execute a script with options and a list of arguments
     *
     * @param script    path of script to execute
     * @param options   options to execute
     * @param arguments list of arguments
     * @return the exit code of the script
     * @throws IOException if cmd execution fails
     */
    public static PhantomJSExecutionResponse exec(InputStream script, PhantomJSOptions options, CommandLineArgument... arguments) throws IOException {
        if (!PhantomJSSetup.isInitialized()) {
            throw new IllegalStateException("Unable to find and execute PhantomJS binaries");
        }

        if (script == null) {
            throw new IllegalArgumentException("Script is a required argument");
        }

        // the path where the script will be copied to
        final String renderId = getRenderId();
        final Path scriptPath = PhantomJSConstants.TEMP_SCRIPT_DIR.resolve(PhantomJSConstants.SCRIPT_PREFIX + renderId);

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

        logger.log(Level.INFO, String.format("Running command: %s", cmd.toString()));

        final LoggerOutputStream stdOutLogger = new LoggerOutputStream(logger, Level.INFO);
        final LoggerOutputStream stdErrLogger = new LoggerOutputStream(logger, Level.SEVERE);

        final DefaultExecutor de = new DefaultExecutor();
        de.setStreamHandler(new PumpStreamHandler(stdOutLogger, stdErrLogger));

        int code;
        try {
            code = de.execute(cmd);
        } catch (ExecuteException exe) {
            code = exe.getExitValue();
        }

        // remove the script after running it
        Files.deleteIfExists(scriptPath);

        logger.info("Execution Completed");

        return new PhantomJSExecutionResponse(code, stdOutLogger.getMessageContents(), stdErrLogger.getMessageContents());
    }

    /**
     * LoggerOutputStream private nested class override
     */
    private static class LoggerOutputStream extends LogOutputStream {
        private Logger logger;
        private Level level;
        private StringBuffer messageContents;

        LoggerOutputStream(Logger logger, Level level) {
            super();
            this.logger = logger;
            this.level = level;
            this.messageContents = new StringBuffer();
        }

        @Override
        protected void processLine(String s, int i) {
            logger.log(level, String.format("PhantomJS script logged: %s", s));
            messageContents.append(s).append(System.lineSeparator());
        }

        String getMessageContents() {
            return messageContents.toString();
        }
    }
}