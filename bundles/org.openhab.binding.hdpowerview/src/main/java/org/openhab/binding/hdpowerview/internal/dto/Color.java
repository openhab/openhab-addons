/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Color and brightness information for HD PowerView repeater
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Color {
    public int brightness;
    public int red;
    public int green;
    public int blue;

    public Color(int brightness, int srgb) {
        this(brightness, new java.awt.Color(srgb));
    }

    public Color(int brightness, java.awt.Color color) {
        this(brightness, color.getRed(), color.getGreen(), color.getBlue());
    }

    public Color(int brightness, int red, int green, int blue) {
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d/%d%%", red, green, blue, brightness);
    }
}
