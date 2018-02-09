/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.api;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * Hue API state object
 *
 * @author Dan Cunningham
 *
 */
public class HueState {
    public boolean on;
    public short bri = -1;
    public int hue = 0;
    public short sat = 0;
    public double[] xy = { 0, 0 };
    public int ct = 500;
    public String alert = "none";
    public String effect = "none";
    public String colormode = "ct";
    public boolean reachable = true;

    public HueState() {
        super();
    }

    public HueState(short bri) {
        super();
        this.on = bri > 0;
        this.bri = bri > 0 ? bri : -1;
    }

    public HueState(int h, short s, short b) {
        super();
        this.on = b > 0;
        this.hue = h;
        this.sat = s;
        this.bri = b;
    }

    public HueState(HSBType hsb) {
        this.on = hsb.getBrightness().intValue() > 0;
        this.hue = hsb.getHue().intValue();
        this.sat = hsb.getSaturation().shortValue();
        this.bri = hsb.intValue() > 0 ? (short) ((hsb.intValue() * 255) / 100) : -1;
    }

    /**
     * Converts this HueState to a HSBType
     *
     * @return
     *         HSBType
     */
    public HSBType toHSBType() {
        int brightness = 0;
        if (this.on || this.bri > 0) {
            // if on but brightness is less then 1, set HSB brightness to 100, otherwise convert Hue brightness
            brightness = this.bri < 1 ? 100 : (int) (this.bri / 255.0 * 100);
        }
        return new HSBType(new DecimalType(this.hue), new PercentType(this.sat), new PercentType(brightness));
    }

    @Override
    public String toString() {
        String xyString = "{";
        for (double d : xy) {
            xyString += d + " ";
        }
        xyString += "}";
        return "[on: " + on + " bri: " + bri + " hue: " + hue + " sat: " + sat + " xy: " + xyString + " ct: " + ct
                + " alert: " + alert + " effect: " + effect + " colormode: " + colormode + " reachable: " + reachable;
    }
}
