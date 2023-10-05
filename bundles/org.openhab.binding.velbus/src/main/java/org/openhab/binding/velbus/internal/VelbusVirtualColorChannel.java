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
package org.openhab.binding.velbus.internal;

import java.util.InvalidPropertiesFormatException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusVirtualColorChannel} represents a class with properties that manage DALI virtual color channel.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusVirtualColorChannel extends VelbusColorChannel {
    private final byte NOT_CONFIGURED = (byte) 0x00;

    private byte redChannel;
    private byte greenChannel;
    private byte blueChannel;
    private byte whiteChannel;

    public VelbusVirtualColorChannel() {
        redChannel = NOT_CONFIGURED;
        greenChannel = NOT_CONFIGURED;
        blueChannel = NOT_CONFIGURED;
        whiteChannel = NOT_CONFIGURED;
    }

    public VelbusVirtualColorChannel(int r, int g, int b, int w) {
        redChannel = getByteValue(r);
        greenChannel = getByteValue(g);
        blueChannel = getByteValue(b);
        whiteChannel = getByteValue(w);
    }

    public VelbusVirtualColorChannel(String r, String g, String b, String w) throws NumberFormatException {
        redChannel = getByteValue(r);
        greenChannel = getByteValue(g);
        blueChannel = getByteValue(b);
        whiteChannel = getByteValue(w);
    }

    public VelbusVirtualColorChannel(String rgbw) throws InvalidPropertiesFormatException, NumberFormatException {
        String virtualChannels[] = rgbw.split("\\s*,\\s*");
        if (virtualChannels.length == 4) {
            redChannel = getByteValue(virtualChannels[0]);
            greenChannel = getByteValue(virtualChannels[1]);
            blueChannel = getByteValue(virtualChannels[2]);
            whiteChannel = getByteValue(virtualChannels[3]);
        } else if (virtualChannels.length == 3) {
            redChannel = getByteValue(virtualChannels[0]);
            greenChannel = getByteValue(virtualChannels[1]);
            blueChannel = getByteValue(virtualChannels[2]);
        } else {
            throw new InvalidPropertiesFormatException("Wrong format");
        }
    }

    private byte getByteValue(String channel) {
        return Integer.valueOf(channel.substring(2)).byteValue();
    }

    private byte getByteValue(int channel) {
        return Integer.valueOf(channel).byteValue();
    }

    public byte getRedChannel() {
        return redChannel;
    }

    public byte getGreenChannel() {
        return greenChannel;
    }

    public byte getBlueChannel() {
        return blueChannel;
    }

    public byte getWhiteChannel() {
        return whiteChannel;
    }

    public boolean isVirtualColorChannel(int channel) {
        byte c = getByteValue(channel);

        return c == redChannel || c == greenChannel || c == blueChannel || c == whiteChannel;
    }

    public boolean isColorChannel(int channel) {
        byte c = getByteValue(channel);

        return c == redChannel || c == greenChannel || c == blueChannel;
    }

    public boolean isWhiteChannel(int channel) {
        byte c = getByteValue(channel);

        return c == whiteChannel;
    }

    public boolean isRGBConfigured() {
        if (redChannel != NOT_CONFIGURED && greenChannel != NOT_CONFIGURED && blueChannel != NOT_CONFIGURED) {
            return true;
        }
        return false;
    }

    public boolean isWhiteConfigured() {
        if (whiteChannel != NOT_CONFIGURED) {
            return true;
        }
        return false;
    }

    public int getRedColor() {
        return super.getColor()[0];
    }

    public int getGreenColor() {
        return super.getColor()[1];
    }

    public int getBlueColor() {
        return super.getColor()[2];
    }

    public byte getRedColorVelbus() {
        return super.getColorVelbus()[0];
    }

    public byte getGreenColorVelbus() {
        return super.getColorVelbus()[1];
    }

    public byte getBlueColorVelbus() {
        return super.getColorVelbus()[2];
    }

    public void setColor(byte color, int channel) {
        byte c = getByteValue(channel);
        if (c == redChannel) {
            setRedColor(color);
        } else if (c == greenChannel) {
            setGreenColor(color);
        } else if (c == blueChannel) {
            setBlueColor(color);
        } else if (c == whiteChannel) {
            setWhite(color);
        }
    }
}
