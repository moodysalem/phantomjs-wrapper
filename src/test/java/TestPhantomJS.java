import com.moodysalem.phantomjs.wrapper.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.AssertJUnit.assertTrue;

public class TestPhantomJS {
    @Test
    public void testRunScript() throws IOException, InterruptedException {
        assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js")) == 0);
    }

    @Test
    public void testRender() throws IOException, RenderException {
        InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"),
                PaperSize.Letter,
                null, null, null, null,
                RenderFormat.PDF);

        PDDocument doc = PDDocument.load(is);
        assertTrue(doc.getNumberOfPages() == 1);

//        PDFTextStripper ps = new PDFTextStripper();
//        assertTrue(ps.getText(doc).contains("Hello, world!"));
    }

    @Test
    public void testFooterAndHeader() throws IOException, RenderException {
        BannerInfo header = new BannerInfo(1, SizeUnit.in, "function (pageNum, numPages) { return \"<h5>Header: <span style='float:right'>\" + ['zero','one','two'][pageNum] + \" / \" + ['zero','one','two'][numPages] + \"</span></h5>\"; }");
        BannerInfo footer = new BannerInfo(1, SizeUnit.in, "function (pageNum, numPages) { return \"<h5>Footer: <span style='float:right'>\" + pageNum + \" / \" + numPages + \"</span></h5>\"; }");
        InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"),
                PaperSize.Letter, ViewportDimensions.VIEW_1280_1024, Margin.ZERO,
                header, footer,
                RenderFormat.PDF);

        PDDocument doc = PDDocument.load(is);
        assertTrue(doc.getNumberOfPages() == 2);

//        PDFTextStripper ps = new PDFTextStripper();
//        String text = ps.getText(doc);
//        assertTrue(text.contains("Hello, world!"));
//        // in the footer we have numbers
//        assertTrue(text.contains("1 / 2"));
//        assertTrue(text.contains("2 / 2"));
//        // in the header we have text
//        assertTrue(text.contains("one / two"));
//        assertTrue(text.contains("two / two"));
    }
}
