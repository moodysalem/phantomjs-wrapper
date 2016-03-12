package com.moodysalem.phantomjs.wrapper;

/**
 * An exception bubbled up from the render script
 */
public class RenderException extends Exception {
    public RenderException(String s) {
        super(s);
    }
}
