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

    static int OFF_GROUP = 32;
    static int OFF_MODE = 33;
    static int OFF_BRIGHTNESS = 34;
    static int OFF_COLOR = 40;
    static int OFF_SATURATION = 43;
    static int OFF_AMBIENT_SCENE = 62;
    // Throwback from original stuff, might not be needed
    static int OFF_INPUT_ID = 73;
    static int OFF_INPUT_N1 = 75;
    static int OFF_INPUT_N2 = 91;
    static int OFF_INPUT_N3 = 107;

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
        return this.payload.get(OFF_GROUP);
    }

    public String getName() {
        return new String(this.payload.array(), 0, 16, StandardCharsets.UTF_8).trim();
    }

    public byte getMode() {
        return this.payload.get(OFF_MODE);
    }

    public byte getScene() {
        return this.payload.get(OFF_AMBIENT_SCENE);
    }

    public byte getRed() {
        return this.payload.get(OFF_COLOR);
    }

    public byte getGreen() {
        return this.payload.get(OFF_COLOR + 1);
    }

    public byte getBlue() {
        return this.payload.get(OFF_COLOR + 2);
    }

    public byte getBrightness() {
        return this.payload.get(OFF_BRIGHTNESS);
    }

    public HSBType getSaturation() {
        int r = this.payload.get(OFF_SATURATION);
        int g = this.payload.get(OFF_SATURATION + 1);
        int b = this.payload.get(OFF_SATURATION + 2);
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
                OFF_GROUP = 32;
                OFF_MODE = 33;
                OFF_BRIGHTNESS = 34;
                OFF_COLOR = 40;
                OFF_SATURATION = 43;
                OFF_AMBIENT_SCENE = 62;
                OFF_INPUT_ID = 73;
                OFF_INPUT_N1 = 75;
                OFF_INPUT_N2 = 91;
                OFF_INPUT_N3 = 107;
                break;
            case PRODUCT_ID_SIDEKICK:
            case PRODUCT_ID_CONNECT:
                OFF_GROUP = 32;
                OFF_MODE = 33;
                OFF_BRIGHTNESS = 34;
                OFF_COLOR = 35;
                OFF_SATURATION = 38;
                OFF_AMBIENT_SCENE = 60;
                OFF_INPUT_ID = -1;
                OFF_INPUT_N1 = -1;
                OFF_INPUT_N2 = -1;
                OFF_INPUT_N3 = -1;
                break;
        }
    }
}
