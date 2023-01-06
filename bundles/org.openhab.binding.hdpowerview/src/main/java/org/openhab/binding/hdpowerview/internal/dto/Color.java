/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.library.types.HSBType;

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

    public Color(int brightness, HSBType hsbType) {
        this.brightness = brightness;
        int rgb = hsbType.getRGB();
        java.awt.Color color = new java.awt.Color(rgb);
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }

    public Color(int brightness, java.awt.Color color) {
        this.brightness = brightness;
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
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
