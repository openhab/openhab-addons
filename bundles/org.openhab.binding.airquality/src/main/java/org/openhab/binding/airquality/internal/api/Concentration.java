package org.openhab.binding.airquality.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class Concentration {

    private final double min;
    private final double span;
    private final Index index;

    public Concentration(double min, double max, Index index) {
        this.min = min;
        this.span = max - min;
        this.index = index;
    }

    public double getMin() {
        return min;
    }

    public double getSpan() {
        return span;
    }

    public Index getIndex() {
        return index;
    }
}
