import com.moodysalem.phantomjs.wrapper.*;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestPhantomJS {
    @Test
    public void testRunScript() throws IOException, InterruptedException {
        PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"));
    }

    @Test
    public void testRender() throws IOException {
        PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"),
                PaperSize.Letter,
                ViewportDimensions.VIEW_1280_1024,
                Margin.ZERO,
                RenderFormat.PDF);
    }
}
