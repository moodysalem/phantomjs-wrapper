package com.moodysalem.phantomjs.wrapper;

class BannerInfo {
    public static final BannerInfo EMPTY = new BannerInfo(0, SizeUnit.px, "function (pageNum, numPages) { return ''; }");

    private final float height;
    private final SizeUnit heightUnit;
    private final String generatorFunction;

    /**
     * Generates html from the page number and number of pages
     */
    public interface Generator {
        String getBanner(int pageNum, int numPages);
    }

    public BannerInfo(float height, SizeUnit heightUnit, String generatorFunction) {
        if (heightUnit == null || generatorFunction == null) {
            throw new NullPointerException();
        }
        this.generatorFunction = generatorFunction;
        this.height = height;
        this.heightUnit = heightUnit;
    }

    public String getHeight() {
        return Float.toString(height) + heightUnit.name();
    }

    public String getGeneratorFunction() {
        return generatorFunction;
    }
}
