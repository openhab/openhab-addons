/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wiz.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wiz.internal.utils.WizColorConverter;
import org.openhab.core.library.types.HSBType;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents Color Request Param
 *
 * Outgoing JSON should look like this:
 *
 * {"id": 24, "method": "setPilot", "params": {"r": 0, "g": 230, "b": 80, "w":
 * 130, "c": 0, "dimming": 12}}
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class ColorRequestParam extends DimmingRequestParam {
    @Expose
    private int r; // red 0-255
    @Expose
    private int g; // green 0-255
    @Expose
    private int b; // blue 0-255
    @Expose
    private int w; // warm white LED's 0-255
    @Expose
    private int c; // cool white LED's 0-255
    @Expose(serialize = false, deserialize = false)
    private WizColorConverter colorConverter = new WizColorConverter();

    public ColorRequestParam(int r, int g, int b, int w, int c, int dimming) {
        super(dimming);
        this.r = r;
        this.g = g;
        this.b = b;
        this.w = w;
        this.c = c;
    }

    public ColorRequestParam(HSBType hsb) {
        super(hsb.getBrightness().intValue());
        int rgbw[] = colorConverter.hsbToRgbw(hsb);
        this.r = rgbw[0];
        this.g = rgbw[1];
        this.b = rgbw[2];
        this.w = rgbw[3];
        this.c = 0;
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
