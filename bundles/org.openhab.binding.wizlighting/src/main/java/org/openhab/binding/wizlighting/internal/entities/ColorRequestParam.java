/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

/**
 * This POJO represents Color Request Param
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
// TODO: Decide if this implements param or extends dimming request
public class ColorRequestParam implements Param {
    private int r; // red 0-255
    private int g; // green 0-255
    private int b; // blue 0-255
    private int w; // warm white LED's 0-255
    private int c; // cool white LED's 0-255

    public ColorRequestParam(int r, int g, int b, int w, int c) {
        this.r = r;
        this.g = g;
        this.b = b;
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
