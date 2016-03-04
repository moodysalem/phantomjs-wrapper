package com.moodysalem.phantomjs.wrapper;

public class HeaderFooterInfo {
    private final float headerHeight;
    private final SizeUnit headerHeightUnit;
    private final float footerHeight;
    private final SizeUnit footerHeightUnit;

    public static final HeaderFooterInfo NONE = new HeaderFooterInfo(0, SizeUnit.px, 0, SizeUnit.px);

    public HeaderFooterInfo(float headerHeight, SizeUnit headerHeightUnit, float footerHeight, SizeUnit footerHeightUnit) {
        if (headerHeightUnit == null || footerHeightUnit == null) {
            throw new NullPointerException();
        }
        this.headerHeight = headerHeight;
        this.headerHeightUnit = headerHeightUnit;
        this.footerHeight = footerHeight;
        this.footerHeightUnit = footerHeightUnit;
    }

    public String getHeaderHeight() {
        return Float.toString(headerHeight) + headerHeightUnit.name();
    }

    public String getFooterHeight() {
        return Float.toString(footerHeight) + footerHeightUnit.name();
    }
}
