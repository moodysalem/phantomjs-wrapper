package com.moodysalem.phantomjs.wrapper;

class BannerInfo {
    public static final BannerInfo EMPTY = new BannerInfo(0, SizeUnit.px, (pn, np) -> "");

    private final float height;
    private final SizeUnit heightUnit;
    private final Generator generator;

    /**
     * Generates html from the page number and number of pages
     */
    public interface Generator {
        String getBanner(int pageNum, int numPages);
    }

    public BannerInfo(float height, SizeUnit heightUnit, Generator generator) {
        if (heightUnit == null || generator == null) {
            throw new NullPointerException();
        }
        this.generator = generator;
        this.height = height;
        this.heightUnit = heightUnit;
    }

    public String getHeight() {
        return Float.toString(height) + heightUnit.name();
    }
}
