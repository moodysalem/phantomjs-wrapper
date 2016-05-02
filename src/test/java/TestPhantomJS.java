import com.moodysalem.phantomjs.wrapper.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.AssertJUnit.assertTrue;

public class TestPhantomJS {

    @Test
    public void testRunScript() throws IOException, InterruptedException {
        assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js")) == 3);
    }

    @Test
    public void testOptions() throws IOException {
        assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"), PhantomJSOptions.DEFAULT.withHelp(true)) == 0);
        assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"), PhantomJSOptions.DEFAULT.withVersion(true)) == 0);
    }

    @Test(expectedExceptions = {NullPointerException.class})
    public void testNullPointerExWithEmpty() throws IOException, RenderException {
        PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.EMPTY);
    }

    @Test
    public void testRender() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT)) {
            PDDocument doc = PDDocument.load(is);
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }

    @Test
    public void testWithExternalCss() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-with-resources.html"), RenderOptions.DEFAULT)) {
            PDDocument doc = PDDocument.load(is);
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }

    @Test
    public void testRenderJsWait() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT)) {
            PDDocument doc = PDDocument.load(is);
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }

    @Test(expectedExceptions = RenderException.class)
    public void testRenderJsWaitTimeout() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT.withJavaScriptExecutionDetails(100L, 50L))) {
            PDDocument doc = PDDocument.load(is);
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }


    @Test
    public void testFooterAndHeader() throws IOException, RenderException {
        BannerInfo header = new BannerInfo(1, SizeUnit.in, "function (pageNum, numPages) { return \"<h5>Header: <span style='float:right'>\" + ['zero','one','two'][pageNum] + \" / \" + ['zero','one','two'][numPages] + \"</span></h5>\"; }");
        BannerInfo footer = new BannerInfo(1, SizeUnit.in, "function (pageNum, numPages) { return \"<h5>Footer: <span style='float:right'>\" + pageNum + \" / \" + numPages + \"</span></h5>\"; }");

        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"), RenderOptions.DEFAULT.withHeaderInfo(header).withFooterInfo(footer))) {
            PDDocument doc = PDDocument.load(is);
            assertTrue(doc.getNumberOfPages() == 2);
        }
    }
}
