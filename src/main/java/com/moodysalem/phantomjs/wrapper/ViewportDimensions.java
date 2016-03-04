package com.moodysalem.phantomjs.wrapper;

public class ViewportDimensions {
    private final int width;
    private final int height;

    public static final ViewportDimensions VIEW_1280_1024 = new ViewportDimensions(1280, 1024);

    public ViewportDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String getWidth() {
        return Integer.toString(width);
    }

    public String getHeight() {
        return Integer.toString(height);
    }
}
