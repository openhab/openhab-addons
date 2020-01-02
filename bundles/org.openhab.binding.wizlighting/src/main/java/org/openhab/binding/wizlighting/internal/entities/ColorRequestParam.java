/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.HSBType;

/**
 * This POJO represents Color Request Param
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class ColorRequestParam extends DimmingRequestParam {
    private int b; // blue 0-255
    private int g; // green 0-255
    private int r; // red 0-255
    private int w; // warm white LED's 0-255
    private int c; // cool white LED's 0-255

    public ColorRequestParam(HSBType color) {
        super(color.getBrightness().intValue());

        this.b = (int) Math.round(color.getBlue().intValue() * 2.55);
        this.g = (int) Math.round(color.getGreen().intValue() * 2.55);
        this.r = (int) Math.round(color.getRed().intValue() * 2.55);

        // strange logic here
        // The WiZ app turns on the warm LED's when the requested saturation is high
        if (color.getSaturation().intValue() > 50) {
            this.w = 255;
        } else {
            this.w = 0;
        }

        if (this.r > 0 && this.b > 0 && this.g > 0) {
            this.w = 0;
        }
        this.c = 0;
    }

    public ColorRequestParam(int b, int g, int r, int w, int c) {
        super(100);
        this.b = b;
        this.g = g;
        this.r = r;
        this.w = w;
        this.c = c;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }
}
