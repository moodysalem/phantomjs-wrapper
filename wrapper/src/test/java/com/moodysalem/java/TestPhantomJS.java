package com.moodysalem.java;

import static org.testng.Assert.assertTrue;

import com.moodysalem.phantomjs.wrapper.PhantomJS;
import com.moodysalem.phantomjs.wrapper.RenderException;
import com.moodysalem.phantomjs.wrapper.RenderOptionsException;
import com.moodysalem.phantomjs.wrapper.beans.BannerInfo;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSExecutionResponse;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJsOptions;
import com.moodysalem.phantomjs.wrapper.beans.RenderOptions;
import com.moodysalem.phantomjs.wrapper.enums.SizeUnit;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class TestPhantomJS {

  private static final Logger LOG = LoggerFactory.getLogger(TestPhantomJS.class);

  @Test
  public void testRunScript() throws IOException, InterruptedException {
    final PhantomJSExecutionResponse phantomJSExecutionResponse = PhantomJS
        .exec(TestPhantomJS.class.getResourceAsStream("test-script.js"));

    assertTrue(phantomJSExecutionResponse.getExitCode() == 3);
    assertTrue(phantomJSExecutionResponse.getStdErr().isEmpty());
    assertTrue(!phantomJSExecutionResponse.getStdOut().isEmpty());
  }


  @Test
  public void testOptions() throws IOException {
    assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"),
        PhantomJsOptions.DEFAULT.withHelp(true)).getExitCode() == 0);
    assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"),
        PhantomJsOptions.DEFAULT.withVersion(true)).getExitCode() == 0);
  }


  @Test(expectedExceptions = {NullPointerException.class})
  public void testNullPointerMissingRenderOptions()
      throws IOException, RenderException, RenderOptionsException {
    PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test-js-execution-timeout.html"), null);
  }

  @Test(expectedExceptions = {NullPointerException.class})
  public void testNullPointerMissingHtml()
      throws IOException, RenderException, RenderOptionsException {
    PhantomJS
        .render(null, new RenderOptions());
  }


  @Test(expectedExceptions = {RenderOptionsException.class})
  public void testRenderOptionsRenderFormatIsNull()
      throws IOException, RenderException, RenderOptionsException {
    final RenderOptions renderOptions = new RenderOptions().renderFormat(null);

    PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test.html"), renderOptions);
  }

  @Test(expectedExceptions = {RenderOptionsException.class})
  public void testRenderOptionsPaperSizeIsNull()
      throws IOException, RenderException, RenderOptionsException {
    final RenderOptions renderOptions = new RenderOptions().paperSize(null);

    PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test.html"), renderOptions);
  }

  @Test(expectedExceptions = {RenderOptionsException.class})
  public void testRenderOptionsViewportDimensionsIsNull()
      throws IOException, RenderException, RenderOptionsException {
    final RenderOptions renderOptions = new RenderOptions().viewportDimensions(null);

    PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test.html"), renderOptions);
  }


  @Test(expectedExceptions = {RenderOptionsException.class})
  public void testRenderOptionsMarginIsNull()
      throws IOException, RenderException, RenderOptionsException {
    final RenderOptions renderOptions = new RenderOptions().margin(null);

    PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test.html"), renderOptions);
  }

  @Test(expectedExceptions = {RenderOptionsException.class})
  public void testRenderOptionsHeaderInfoIsNull()
      throws IOException, RenderException, RenderOptionsException {
    final RenderOptions renderOptions = new RenderOptions().headerInfo(null);

    PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test.html"), renderOptions);
  }

  @Test(expectedExceptions = {RenderOptionsException.class})
  public void testRenderOptionsFooterInfoIsNull()
      throws IOException, RenderException, RenderOptionsException {
    final RenderOptions renderOptions = new RenderOptions().footerInfo(null);

    PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test.html"), renderOptions);
  }


  @Test
  public void testRender() throws IOException, RenderException, RenderOptionsException {
    try (InputStream is = PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test-js-execution-timeout.html"),
            new RenderOptions())) {
      final PDDocument doc = PDDocument.load(is);
      doc.close();
      assertTrue(doc.getNumberOfPages() == 1);
    }
  }


  @Test
  public void testWithExternalCss() throws IOException, RenderException, RenderOptionsException {
    try (InputStream is = PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test-with-resources.html"),
            new RenderOptions())) {
      final PDDocument doc = PDDocument.load(is);
      doc.close();
      assertTrue(doc.getNumberOfPages() == 1);
    }
  }


  @Test
  public void testRenderJsExecutionTimeoutSuccessful()
      throws IOException, RenderException, RenderOptionsException {
    try (InputStream is = PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test-js-execution-timeout.html"),
            new RenderOptions())) {
      final PDDocument doc = PDDocument.load(is);
      doc.close();
      assertTrue(doc.getNumberOfPages() == 1);
    }
  }


  @Test(expectedExceptions = RenderException.class)
  public void testRenderJsExecutionTimeout()
      throws IOException, RenderException, RenderOptionsException {
    try (InputStream is = PhantomJS
        .render(TestPhantomJS.class.getResourceAsStream("test-js-execution-timeout.html"),
            new RenderOptions().jsExecutionTimeout(100L).jsInterval(50L))) {
      final PDDocument doc = PDDocument.load(is);
      doc.close();
      assertTrue(doc.getNumberOfPages() == 1);
    }
  }


  @Test
  public void testFooterAndHeader() throws IOException, RenderException, RenderOptionsException {
    final BannerInfo header = new BannerInfo(1, SizeUnit.in,
        "function (pageNum, numPages) { return \"<h5>Header: <span style='float:right'>\" + ['zero','one','two'][pageNum] + \" / \" + ['zero','one','two'][numPages] + \"</span></h5>\"; }");
    final BannerInfo footer = new BannerInfo(1, SizeUnit.in,
        "function (pageNum, numPages) { return \"<h5>Footer: <span style='float:right'>\" + pageNum + \" / \" + numPages + \"</span></h5>\"; }");

    try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"),
        new RenderOptions().headerInfo(header).footerInfo(footer))) {
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
      throw new InterruptedException(
          "Load test has encountered a thread InterruptedException. Please refer to log file for details.");
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
        try (final InputStream pdf = PhantomJS
            .render(TestPhantomJS.class.getResourceAsStream("test.html"), new RenderOptions())) {
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
