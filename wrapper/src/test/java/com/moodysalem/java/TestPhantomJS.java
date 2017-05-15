package com.moodysalem.java;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.moodysalem.phantomjs.wrapper.PhantomJS;
import com.moodysalem.phantomjs.wrapper.RenderException;
import com.moodysalem.phantomjs.wrapper.beans.BannerInfo;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSExecutionResponse;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSOptions;
import com.moodysalem.phantomjs.wrapper.beans.RenderOptions;
import com.moodysalem.phantomjs.wrapper.enums.SizeUnit;

public class TestPhantomJS {

    private static final Logger LOG = LoggerFactory.getLogger(TestPhantomJS.class);


    @Test
    public void testRunScript() throws IOException, InterruptedException {
        final PhantomJSExecutionResponse phantomJSExecutionResponse = PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"));

        assertTrue(phantomJSExecutionResponse.getExitCode() == 3);
        assertTrue(phantomJSExecutionResponse.getStdErr().isEmpty());
        assertTrue(!phantomJSExecutionResponse.getStdOut().isEmpty());
    }


    @Test
    public void testOptions() throws IOException {
        assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"), PhantomJSOptions.DEFAULT.withHelp(true)).getExitCode() == 0);
        assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"), PhantomJSOptions.DEFAULT.withVersion(true)).getExitCode() == 0);
    }


    @Test(expectedExceptions = { NullPointerException.class })
    public void testNullPointerExWithEmpty() throws IOException, RenderException {
        PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.EMPTY);
    }


    @Test
    public void testRender() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT)) {
            final PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }


    @Test
    public void testWithExternalCss() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-with-resources.html"), RenderOptions.DEFAULT)) {
            final PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }


    @Test
    public void testRenderJsWait() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT)) {
            final PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }


    @Test(expectedExceptions = RenderException.class)
    public void testRenderJsWaitTimeout() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT.withJavaScriptExecutionDetails(100L, 50L))) {
            final PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }


    @Test
    public void testFooterAndHeader() throws IOException, RenderException {
        final BannerInfo header = new BannerInfo(1, SizeUnit.in, "function (pageNum, numPages) { return \"<h5>Header: <span style='float:right'>\" + ['zero','one','two'][pageNum] + \" / \" + ['zero','one','two'][numPages] + \"</span></h5>\"; }");
        final BannerInfo footer = new BannerInfo(1, SizeUnit.in, "function (pageNum, numPages) { return \"<h5>Footer: <span style='float:right'>\" + pageNum + \" / \" + numPages + \"</span></h5>\"; }");

        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"), RenderOptions.DEFAULT.withHeaderInfo(header).withFooterInfo(footer))) {
            final PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 2);
        }
    }


    @Test
    public void concurrencyTest() throws InterruptedException {
        final List<Thread> threads = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            threads.add(newRenderThread());
        }

        threads.forEach(Thread::start);

        // wait 10 seconds and none of the threads should be interrupted
        Thread.sleep(10000);

        if (threads.stream().anyMatch(Thread::isInterrupted)) {
            throw new InterruptedException("Load test has encountered a thread InterruptedException. Please refer to log file for details.");
        }
    }


    /**
     * Returns a thread that renders test.html
     *
     * @return the thread
     */
    private Thread newRenderThread() {
        return new Thread() {

            @Override
            public void run() {
                try (final InputStream pdf = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"), RenderOptions.DEFAULT)) {
                    assert IOUtils.toByteArray(pdf).length > 0;
                    LOG.info("Thread finished rendering: {}", getName());
                } catch (final Exception e) {
                    interrupt();
                    e.printStackTrace();
                }
            }
        };
    }
}
