package com.moodysalem.phantomjs.wrapper;

public class Margin {
    private final float top;
    private final SizeUnit topUnit;
    private final float bottom;
    private final SizeUnit bottomUnit;
    private final float left;
    private final SizeUnit leftUnit;
    private final float right;
    private final SizeUnit rightUnit;

    public static final Margin ZERO = new Margin();

    public Margin() {
        this(0, SizeUnit.px);
    }

    public Margin(float margin, SizeUnit unit) {
        this(margin, unit, margin, unit, margin, unit, margin, unit);
    }

    public Margin(float top, SizeUnit topUnit, float bottom, SizeUnit bottomUnit,
                  float left, SizeUnit leftUnit, float right, SizeUnit rightUnit) {
        if (topUnit == null || bottomUnit == null || leftUnit == null || rightUnit == null) {
            throw new NullPointerException();
        }
        this.top = top;
        this.topUnit = topUnit;
        this.bottom = bottom;
        this.bottomUnit = bottomUnit;
        this.right = right;
        this.rightUnit = rightUnit;
        this.left = left;
        this.leftUnit = leftUnit;
    }

    public String getTop() {
        return Float.toString(top) + topUnit.name();
    }

    public String getBottom() {
        return Float.toString(bottom) + bottomUnit.name();
    }

    public String getLeft() {
        return Float.toString(left) + leftUnit.name();
    }

    public String getRight() {
        return Float.toString(right) + rightUnit.name();
    }
}
