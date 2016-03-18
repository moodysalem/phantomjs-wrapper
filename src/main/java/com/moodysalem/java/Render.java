package com.moodysalem.java;

import com.moodysalem.phantomjs.wrapper.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A command line tool for accessing the render method of the phantomJS binary
 */
public class Render {

    /**
     * Render the html in argument 1 to argument 2
     */
    public static void main(String[] args) {
        try (InputStream html = Files.newInputStream(Paths.get(args[0]))) {
            try (InputStream pdf = PhantomJS.render(html, PaperSize.Letter, ViewportDimensions.VIEW_1280_1024, Margin.ZERO,
                BannerInfo.EMPTY, BannerInfo.EMPTY, RenderFormat.PDF, 10000L, 100L)) {
                Path dest = Paths.get(args[1]);
                Files.deleteIfExists(dest);
                Files.copy(pdf, dest);
            } catch (RenderException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
