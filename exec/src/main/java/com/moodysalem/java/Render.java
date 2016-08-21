package com.moodysalem.java;

import com.moodysalem.phantomjs.wrapper.PhantomJS;
import com.moodysalem.phantomjs.wrapper.RenderException;
import com.moodysalem.phantomjs.wrapper.beans.BannerInfo;
import com.moodysalem.phantomjs.wrapper.beans.Margin;
import com.moodysalem.phantomjs.wrapper.beans.PaperSize;
import com.moodysalem.phantomjs.wrapper.beans.ViewportDimensions;
import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;

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
     * Render the first argument to a PDF located at the second argument
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try (
                final InputStream html = Files.newInputStream(Paths.get(args[0]));
                final InputStream pdf = PhantomJS.render(null, html, PaperSize.Letter, ViewportDimensions.VIEW_1280_1024,
                        Margin.ZERO, BannerInfo.EMPTY, BannerInfo.EMPTY, RenderFormat.PDF, 10000L, 100L);
        ) {

            Path dest = Paths.get(args[1]);
            Files.deleteIfExists(dest);
            Files.copy(pdf, dest);

        } catch (IOException | RenderException e) {
            e.printStackTrace();
        }
    }
}
