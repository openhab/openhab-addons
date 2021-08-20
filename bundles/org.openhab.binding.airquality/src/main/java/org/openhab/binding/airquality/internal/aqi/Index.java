package org.openhab.binding.airquality.internal.aqi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public enum Index {
    ZERO(0, 50, Category.GOOD),
    FIFTY(51, 100, Category.MODERATE),
    ONE_HUNDRED(101, 150, Category.UNHEALTHY_FSG),
    ONE_HUNDRED_FIFTY(151, 200, Category.UNHEALTHY),
    TWO_HUNDRED(201, 300, Category.VERY_UNHEALTHY),
    THREE_HUNDRED(301, 400, Category.HAZARDOUS),
    FOUR_HUNDRED(401, 500, Category.HAZARDOUS);

    private double min;
    private double max;
    private Category category;

    Index(double min, double max, Category category) {
        this.min = min;
        this.max = max;
        this.category = category;
    }

    public double getMin() {
        return min;
    }

    public double getSpan() {
        return max - min;
    }

    public boolean contains(double idx) {
        return min <= idx && idx <= max;
    }

    public static @Nullable Index find(double idx) {
        for (Index item : Index.values()) {
            if (item.contains(idx)) {
                return item;
            }
        }
        return null;
    }

    public Category getCategory() {
        return category;
    }
}
