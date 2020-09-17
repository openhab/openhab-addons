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
package org.openhab.binding.dreamscreen.internal.message;

import static org.openhab.binding.dreamscreen.internal.DreamScreenBindingConstants.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.HSBType;

/**
 * {@link RefreshMessage} handles the Refresh Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class RefreshMessage extends DreamScreenMessage {
    static final byte COMMAND_UPPER = 0x01;
    static final byte COMMAND_LOWER = 0x0A;

    // Set integers so we know which bytes to pull based on device

    private int groupInt = 32;
    private int modeInt = 33;
    private int brightnessInt = 34;
    private int colorInt = 40;
    private int saturationInt = 43;
    private int ambientSceneInt = 62;
    // Throwback from original stuff, might not be needed
    private int inputInt = 73;
    private int n1Int = 75;
    private int n2Int = 91;
    private int n3Int = 107;

    public RefreshMessage() {
        super((byte) 0xFF, COMMAND_UPPER, COMMAND_LOWER, new byte[0]);
    }

    public RefreshMessage(final byte[] data) {
        super(data);
        refreshType();
    }

    static boolean matches(final byte[] data) {
        return matches(data, COMMAND_UPPER, COMMAND_LOWER);
    }

    public byte getGroup() {
        return this.payload.get(groupInt);
    }

    public String getName() {
        return new String(this.payload.array(), 0, 16, StandardCharsets.UTF_8).trim();
    }

    public byte getMode() {
        return this.payload.get(modeInt);
    }

    public byte getScene() {
        return this.payload.get(ambientSceneInt);
    }

    public byte getRed() {
        return this.payload.get(colorInt);
    }

    public byte getGreen() {
        return this.payload.get(colorInt + 1);
    }

    public byte getBlue() {
        return this.payload.get(colorInt + 2);
    }

    public byte getBrightness() {
        return this.payload.get(brightnessInt);
    }

    public HSBType getSaturation() {
        int r = this.payload.get(saturationInt);
        int g = this.payload.get(saturationInt + 1);
        int b = this.payload.get(saturationInt + 2);
        return HSBType.fromRGB(r & 0xFF, g & 0xFF, b & 0xFF);
    }

    public byte getProductId() {
        return this.payload.get(this.payloadLen - 1);
    }

    @Override
    public DatagramPacket writePacket(InetAddress address, int port) {
        return broadcastReadPacket(address, port);
    }

    @Override
    public String toString() {
        return "Refresh";
    }

    private void refreshType() {
        switch (super.deviceType) {
            case PRODUCT_ID_HD:
            case PRODUCT_ID_4K:
            case PRODUCT_ID_SOLO:
                groupInt = 32;
                modeInt = 33;
                brightnessInt = 34;
                colorInt = 40;
                saturationInt = 43;
                ambientSceneInt = 62;
                inputInt = 73;
                n1Int = 75;
                n2Int = 91;
                n3Int = 107;
                break;
            case PRODUCT_ID_SIDEKICK:
            case PRODUCT_ID_CONNECT:
                groupInt = 32;
                modeInt = 33;
                brightnessInt = 34;
                colorInt = 35;
                saturationInt = 38;
                ambientSceneInt = 60;
                inputInt = -1;
                n1Int = -1;
                n2Int = -1;
                n3Int = -1;
                break;
        }
    }
}
