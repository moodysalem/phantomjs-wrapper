import com.moodysalem.phantomjs.wrapper.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.AssertJUnit.assertTrue;

public class TestPhantomJS {
    @Test
    public void testRunScript() throws IOException, InterruptedException {
        PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"));
    }

    @Test
    public void testRender() throws IOException, RenderException {
        InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"),
                PaperSize.Letter,
                ViewportDimensions.VIEW_1280_1024,
                Margin.ZERO,
                null, null,
                RenderFormat.PDF);

        PDDocument doc = PDDocument.load(is);

        assertTrue(doc.getNumberOfPages() == 1);
    }
}
