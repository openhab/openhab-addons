/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.api;

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

    public HueState(boolean on, short bri) {
        super();
        this.on = on;
        this.bri = bri;
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
