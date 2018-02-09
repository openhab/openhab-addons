/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.handler;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

import static java.lang.Math.max;

/**
 * Internal LED state.
 *
 * @author Stefan Endrullis
 */
public class InternalLedState {

    /** Values for the colors red, green, blue from 0 to 1. */
    double r, g, b;
    /** White values from 0 to 1. */
    double w, w2;

    public InternalLedState() {
        this(0, 0, 0, 0, 0);
    }

    public InternalLedState(double r, double g, double b, double w, double w2) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.w = w;
        this.w2 = w2;
    }

    public static InternalLedState fromRGBW(int r, int g, int b, int w, int w2) {
        return new InternalLedState(conv(r), conv(g), conv(b), conv(w), conv(w2));
    }

    public InternalLedState withColor(HSBType color) {
        return new InternalLedState(
            color.getRed().doubleValue()   / 100,
            color.getGreen().doubleValue() / 100,
            color.getBlue().doubleValue()  / 100,
            w,
            w2
        );
    }

    public InternalLedState withBrightness(double brightness) {
        return new InternalLedState(
            r * brightness,
            g * brightness,
            b * brightness,
            w,
            w2
        );
    }

    public InternalLedState withWhite(double w) {
        return new InternalLedState(
            r,
            g,
            b,
            w,
            w2
        );
    }

    public InternalLedState withWhite2(double w2) {
        return new InternalLedState(
            r,
            g,
            b,
            w,
            w2
        );
    }

    public PercentType toHSBType() {
        return HSBType.fromRGB(conv(r), conv(g), conv(b));
    }

    /**
     * Fades from this color to the that color according to the given progress value from 0 (this color)
     * to 1 (that color).
     *
     * @param that that color
     * @param progress value between 0 (this color) and 1 (that color)
     * @return faded color
     */
    public InternalLedState fade(InternalLedState that, double progress) {
        double invProgress = 1 - progress;

        return new InternalLedState(
            this.r * invProgress + that.r * progress,
            this.g * invProgress + that.g * progress,
            this.b * invProgress + that.b * progress,
            this.w * invProgress + that.w * progress,
            this.w2 * invProgress + that.w2 * progress
        );
    }

    /**
     * Returns the brightness or the RGB color.
     *
     * @return value between 0 and 1
     */
    public double getBrightness() {
        return max(r, max(g, b));
    }

    /**
     * Returns the white value.
     *
     * @return value between 0 and 1
     */
    public double getWhite() {
        return w;
    }

    /**
     * Returns the white2 value.
     *
     * @return value between 0 and 1
     */
    public double getWhite2() {
        return w2;
    }

    private static double conv(int v) {
        return (double) v / 255;
    }

    private static int conv(double v) {
        return (int) (v * 255 + 0.5);
    }

    /**
     * Returns red value.
     *
     * @return value between 0 and 255
     */
    public int getR() {
        return conv(r);
    }

    /**
     * Returns green value.
     *
     * @return value between 0 and 255
     */
    public int getG() {
        return conv(g);
    }

    /**
     * Returns blue value.
     *
     * @return value between 0 and 255
     */
    public int getB() {
        return conv(b);
    }

    /**
     * Returns white value.
     *
     * @return value between 0 and 255
     */
    public int getW() {
        return conv(w);
    }

    /**
     * Returns white2 value.
     *
     * @return value between 0 and 255
     */
    public int getW2() {
        return conv(w2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InternalLedState that = (InternalLedState) o;

        return Double.compare(that.r, r) == 0
            && Double.compare(that.g, g) == 0
            && Double.compare(that.b, b) == 0
            && Double.compare(that.w, w) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(r);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(g);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(b);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(w);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "InternalLedState{" +
            "r=" + r +
            ", g=" + g +
            ", b=" + b +
            ", w=" + w +
            '}';
    }

}
