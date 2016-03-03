import com.moodysalem.wrapper.PhantomJS;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestPhantomJS {
    @Test
    public void testRunScript() throws IOException, InterruptedException {
        PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"), "test-google.pdf");
    }
}
