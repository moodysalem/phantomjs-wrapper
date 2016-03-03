import com.moodysalem.phantomjs.wrapper.PhantomJS;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestPhantomJS {
    @Test
    public void testRunScript() throws IOException, InterruptedException {
        PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"), "target/test-google.pdf");
    }

    @Test
    public void testRender() throws IOException {
        PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"),
                8.5f, PhantomJS.PageSizeUnit.in, 11, PhantomJS.PageSizeUnit.in,
                1280, 768,
                PhantomJS.Format.PDF);
    }
}
