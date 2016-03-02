import com.moodysalem.wrapper.PhantomJS;
import org.testng.annotations.Test;

public class TestPhantomJS {
    @Test
    public void testRunScript() {

        PhantomJS.exec("https://google.com");

    }
}
