package com.moodysalem.phantomjs.wrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

import com.moodysalem.phantomjs.wrapper.beans.BannerInfo;
import com.moodysalem.phantomjs.wrapper.beans.Margin;
import com.moodysalem.phantomjs.wrapper.beans.OperatingSystem;
import com.moodysalem.phantomjs.wrapper.beans.PaperSize;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSOptions;
import com.moodysalem.phantomjs.wrapper.beans.RenderOptions;
import com.moodysalem.phantomjs.wrapper.beans.ViewportDimensions;
import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;

public class PhantomJS{
	
	protected final static Logger logger = Logger.getLogger(PhantomJS.class.getName());
	
	// we use this for generating file names that do not conflict
    private static final AtomicLong RENDER_NUMBER = new AtomicLong(0);

    private static synchronized long getAndIncrementNumber() {
        return RENDER_NUMBER.incrementAndGet();
    }
	
    // get a reference to the executable binary and store it in PHANTOM_JS_BINARY
    static {
        String resourcePath = PhantomJSSetup.getZipPath();
        
        logger.info("Initializing PhantomJS with resource path: " + resourcePath); //TODO
        if (resourcePath != null) {
        	PhantomJSSetup.unzipPhantomJSbin(PhantomJSSetup.TEMP_DIR, resourcePath);
        }
    }
    

    /**
     * Another way to call PhantomJS#render using the RenderOptions to specify all the common options
     *
     * @param html    to render
     * @param options for rendering
     * @return same as PhantomJS#render
     * @throws IOException
     * @throws RenderException
     */
    public static InputStream render(InputStream html, RenderOptions options) throws IOException, RenderException {
        return render(options.getOptions(), html, options.getPaperSize(), options.getDimensions(),
            options.getMargin(), options.getHeaderInfo(), options.getFooterInfo(), options.getRenderFormat(),
            options.getJsWait(), options.getJsInterval());
    }

    /**
     * Render the html in the input stream with the following properties using a script included with the wrapper
     *
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
    public static InputStream render(PhantomJSOptions options, InputStream html, PaperSize paperSize, ViewportDimensions dimensions,
                                     Margin margin, BannerInfo headerInfo, BannerInfo footerInfo,
                                     RenderFormat renderFormat, Long jsWait, Long jsInterval) throws IOException, RenderException {
       
    	if (html == null || renderFormat == null || paperSize == null || dimensions == null || margin == null ||
            headerInfo == null || footerInfo == null || jsWait == null || jsInterval == null) {
            throw new NullPointerException("All parameters are required");
        }

        if (jsWait < 0 || jsInterval < 0 || (jsWait > 0 && jsInterval > jsWait) || (jsInterval == 0 && jsWait > 0)) {
            throw new IllegalArgumentException();
        }

        // The render script
        InputStream renderScript = PhantomJS.class.getResourceAsStream("render.js");

        // create the parent directories
        Files.createDirectories(PhantomJSSetup.TEMP_SOURCE_DIR);
        Files.createDirectories(PhantomJSSetup.TEMP_RENDER_DIR);

        final long renderNumber = getAndIncrementNumber();

        // create a source file for the source html input stream
        Path sourcePath = PhantomJSSetup.TEMP_SOURCE_DIR.resolve("source-" + renderNumber);
        Files.copy(html, sourcePath);

        // create a source file for the header function
        Path headerFunctionPath = PhantomJSSetup.TEMP_SOURCE_DIR.resolve("header-" + renderNumber);
        Files.copy(new ByteArrayInputStream(headerInfo.getGeneratorFunction().getBytes()), headerFunctionPath);

        // create a source file for the footer function
        Path footerFunctionPath = PhantomJSSetup.TEMP_SOURCE_DIR.resolve("footer-" + renderNumber);
        Files.copy(new ByteArrayInputStream(footerInfo.getGeneratorFunction().getBytes()), footerFunctionPath);

        // the output file
        Path renderPath = PhantomJSSetup.TEMP_RENDER_DIR.resolve(String.format("target-%s.%s", renderNumber, renderFormat.name().toLowerCase()));

        int renderExitCode = exec(renderScript,
            options,
            new CommandLineArgument(paperSize.getWidth()), new CommandLineArgument(paperSize.getHeight()),
            new CommandLineArgument(dimensions.getWidth()), new CommandLineArgument(dimensions.getHeight()),
            new CommandLineArgument(margin.getTop()), new CommandLineArgument(margin.getRight()),
            new CommandLineArgument(margin.getBottom()), new CommandLineArgument(margin.getLeft()),
            new CommandLineArgument(headerInfo.getHeight()), new CommandLineArgument("${headerFunctionFile}", "headerFunctionFile", headerFunctionPath.toFile()),
            new CommandLineArgument(footerInfo.getHeight()), new CommandLineArgument("${footerFunctionFile}", "footerFunctionFile", footerFunctionPath.toFile()),
            new CommandLineArgument(OperatingSystem.get().name()),
            new CommandLineArgument("${sourcePath}", "sourcePath", sourcePath.toFile()),
            new CommandLineArgument("${renderPath}", "renderPath", renderPath.toFile()),
            new CommandLineArgument("${jsWait}", "jsWait", jsWait),
            new CommandLineArgument("${jsInterval}", "jsInterval", jsInterval)
        );

        Files.deleteIfExists(sourcePath);
        Files.deleteIfExists(headerFunctionPath);
        Files.deleteIfExists(footerFunctionPath);

        if (renderExitCode == 0) {
            return new DeleteOnCloseFileInputStream(renderPath.toFile());
        }

        String error = "Render script failed for an unknown reason.";
        switch (renderExitCode) {
            case 1:
                error = "Failed to read source html file from input stream";
                break;
            case 2:
                error = "Failed to set zoom on document body";
                break;
            case 3:
                error = "Failed to render pdf to output";
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
        }

        throw new RenderException(error);
    }


    public static int exec(InputStream script, CommandLineArgument... arguments) throws IOException {
        return exec(script, null, arguments);
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
            logger.log(level, String.format("PhantomJS script logged: %s", s));
        }
    }

    private static final LoggerOutputStream STDOUT_LOGGER = new LoggerOutputStream(logger, Level.INFO);
    private static final LoggerOutputStream STDERR_LOGGER = new LoggerOutputStream(logger, Level.SEVERE);

    /**
     * Execute a script with options and a list of arguments
     *
     * @param script    path of script to execute
     * @param options   options to execute
     * @param arguments list of arguments
     * @return the exit code of the script
     * @throws IOException if cmd execution fails
     */
    public static int exec(InputStream script, PhantomJSOptions options, CommandLineArgument... arguments) throws IOException {
        if (PhantomJSSetup.PHANTOM_JS_BINARY == null) {
            throw new IllegalStateException("PhantomJS binary not found");
        }

        if (!PhantomJSSetup.PHANTOM_JS_BINARY.canExecute()) {
            throw new IllegalStateException("Unable to execute phantomJS binaries");
        }

        if (!PhantomJSSetup.PHANTOM_JS_BINARY.canExecute()) {
            throw new IllegalStateException("PhantomJS binary not executable");
        }

        if (script == null) {
            throw new IllegalArgumentException("Script is a required argument");
        }

        // the path where the script will be copied to
        long incrementingNumber = getAndIncrementNumber();
        Path scriptPath = PhantomJSSetup.TEMP_SCRIPT_DIR.resolve("script-" + incrementingNumber);

        // create the parent directory
        Files.createDirectories(PhantomJSSetup.TEMP_SCRIPT_DIR);

        // copy the script to the path
        Files.copy(script, scriptPath);

        // start building the phantomjs binary call
        CommandLine cmd = new CommandLine(PhantomJSSetup.PHANTOM_JS_BINARY);
        Map<String, Object> args = new HashMap<>();
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
            for (CommandLineArgument arg : arguments) {
                arg.apply(cmd, args);
            }
        }

        logger.log(Level.INFO, String.format("Running command: %s", cmd.toString()));
        DefaultExecutor de = new DefaultExecutor();
        de.setStreamHandler(new PumpStreamHandler(STDOUT_LOGGER, STDERR_LOGGER));
        int code;
        try {
            code = de.execute(cmd);
        } catch (ExecuteException exe) {
            code = exe.getExitValue();
        }


        // remove the script after running it
        Files.deleteIfExists(scriptPath);

        return code;
    }
}