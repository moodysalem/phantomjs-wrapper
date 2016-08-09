
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.testng.annotations.Test;

import com.moodysalem.phantomjs.wrapper.PhantomJS;
import com.moodysalem.phantomjs.wrapper.RenderException;
import com.moodysalem.phantomjs.wrapper.beans.BannerInfo;
import com.moodysalem.phantomjs.wrapper.beans.Margin;
import com.moodysalem.phantomjs.wrapper.beans.PaperSize;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSExecutionResponse;
import com.moodysalem.phantomjs.wrapper.beans.PhantomJSOptions;
import com.moodysalem.phantomjs.wrapper.beans.RenderOptions;
import com.moodysalem.phantomjs.wrapper.beans.ViewportDimensions;
import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;
import com.moodysalem.phantomjs.wrapper.enums.SizeUnit;

public class TestPhantomJS {

    @Test
    public void testRunScript() throws IOException, InterruptedException {
    	PhantomJSExecutionResponse phantomJSExecutionResponse = 
    			PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"));
    	
        assertTrue(phantomJSExecutionResponse.getExitCode() == 3);
        assertTrue(phantomJSExecutionResponse.getStdErr().isEmpty());
        assertTrue(!phantomJSExecutionResponse.getStdOut().isEmpty());
    }

    @Test
    public void testOptions() throws IOException {
        assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"), PhantomJSOptions.DEFAULT.withHelp(true)).getExitCode() == 0);
        assertTrue(PhantomJS.exec(TestPhantomJS.class.getResourceAsStream("test-script.js"), PhantomJSOptions.DEFAULT.withVersion(true)).getExitCode() == 0);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNullPointerExWithEmpty() throws IOException, RenderException {
        PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.EMPTY);
    }

    @Test
    public void testRender() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT)) {
            PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }

    @Test
    public void testWithExternalCss() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-with-resources.html"), RenderOptions.DEFAULT)) {
            PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }

    @Test
    public void testRenderJsWait() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT)) {
            PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }

    @Test(expectedExceptions = RenderException.class)
    public void testRenderJsWaitTimeout() throws IOException, RenderException {
        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test-js-waiting.html"), RenderOptions.DEFAULT.withJavaScriptExecutionDetails(100L, 50L))) {
            PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 1);
        }
    }


    @Test
    public void testFooterAndHeader() throws IOException, RenderException {
        BannerInfo header = new BannerInfo(1, SizeUnit.in, "function (pageNum, numPages) { return \"<h5>Header: <span style='float:right'>\" + ['zero','one','two'][pageNum] + \" / \" + ['zero','one','two'][numPages] + \"</span></h5>\"; }");
        BannerInfo footer = new BannerInfo(1, SizeUnit.in, "function (pageNum, numPages) { return \"<h5>Footer: <span style='float:right'>\" + pageNum + \" / \" + numPages + \"</span></h5>\"; }");

        try (InputStream is = PhantomJS.render(TestPhantomJS.class.getResourceAsStream("test.html"), RenderOptions.DEFAULT.withHeaderInfo(header).withFooterInfo(footer))) {
            PDDocument doc = PDDocument.load(is);
            doc.close();
            assertTrue(doc.getNumberOfPages() == 2);
        }
    }
    
    @Test
    public void loadTest() throws InterruptedException{
    	
    	String htmlPath = "src/test/resources/test.html";
    	String pdfPath = "target/test-%s.pdf";
    	
    	Thread thread1 = newLoadThread("thread1", htmlPath, pdfPath);
    	Thread thread2 = newLoadThread("thread2", htmlPath, pdfPath);
    	Thread thread3 = newLoadThread("thread3", htmlPath, pdfPath);
    	Thread thread4 = newLoadThread("thread4", htmlPath, pdfPath);
    	Thread thread5 = newLoadThread("thread5", htmlPath, pdfPath);
    	
    	thread1.start();
    	thread2.start();
    	thread3.start();
    	thread4.start();
    	thread5.start();
    	
    	Thread.sleep(10000);

		if(thread1.isInterrupted() ||
				thread2.isInterrupted() ||
				thread3.isInterrupted() ||
				thread4.isInterrupted() ||
				thread5.isInterrupted()){
			throw new InterruptedException("Load test has encountered a thread InterruptedException. Please refer to log file for details.");
		}
    	
    }
    
    private Thread newLoadThread(String threadName, String htmlPath, String pdfPath){
    	
    	return new Thread(threadName){
    		public void run(){
    			try (
    					InputStream html = Files.newInputStream(Paths.get(htmlPath));
    					InputStream pdf = PhantomJS.render(null, html, PaperSize.Letter, ViewportDimensions.VIEW_1280_1024, 
    							Margin.ZERO, BannerInfo.EMPTY, BannerInfo.EMPTY, RenderFormat.PDF, 10000L, 100L);
    				){

    				Path dest = Paths.get(String.format(pdfPath, threadName));
    				Files.deleteIfExists(dest);
    				Files.copy(pdf, dest);

    			} catch (IOException | RenderException e) {
					this.interrupt();
					e.printStackTrace();
				}
    		}
    	};
    }
}
