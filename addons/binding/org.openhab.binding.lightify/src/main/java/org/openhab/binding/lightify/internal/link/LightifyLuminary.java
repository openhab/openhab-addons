/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal.link;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public abstract class LightifyLuminary {

    private final LightifyLink lightifyLink;
    private final String name;

    private boolean status;
    private short temperature;
    private byte luminance;
    private byte[] rgb = new byte[3];

    LightifyLuminary(LightifyLink lightifyLink, String name) {
        this.lightifyLink = lightifyLink;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSwitch(boolean activate, Consumer<LightifyLuminary> consumer) {
        lightifyLink.performSwitch(this, activate, consumer);
    }

    public void setLuminance(byte luminance, short millis, Consumer<LightifyLuminary> consumer) {
        lightifyLink.performLuminance(this, luminance, millis, consumer);
    }

    public void setRGB(byte r, byte g, byte b, short millis, Consumer<LightifyLuminary> consumer) {
        lightifyLink.performRGB(this, r, g, b, millis, consumer);
    }

    public void setTemperature(short temperature, short millis, Consumer<LightifyLuminary> consumer) {
        lightifyLink.performTemperature(this, temperature, millis, consumer);
    }

    public boolean isPowered() {
        return status;
    }

    public short getTemperature() {
        return temperature;
    }

    public byte getLuminance() {
        return luminance;
    }

    public byte[] getRGB() {
        return Arrays.copyOf(rgb, rgb.length);
    }

    public abstract byte[] address();

    @Override
    public String toString() {
        return "LightifyLuminary{" + "name='" + name + '\'' + ", status=" + status
                + ", temperature=" + temperature + ", luminance=" + luminance + ", rgb=" + Arrays.toString(rgb) + '}';
    }

    void updateTemperature(short temperature) {
        this.temperature = temperature;
    }

    void updateLuminance(byte luminance) {
        this.luminance = luminance;
    }

    void updateRGB(byte r, byte g, byte b) {
        this.rgb[0] = r;
        this.rgb[1] = g;
        this.rgb[2] = b;
    }

    void updatePowered(boolean status) {
        this.status = status;
    }

    abstract byte typeFlag();
}
