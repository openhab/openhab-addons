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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link RefreshMessage} handles the Refresh Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class RefreshMessage extends DreamScreenMessage {
    static final byte COMMAND_UPPER = 0x01;
    static final byte COMMAND_LOWER = 0x0A;

    protected RefreshMessage(final byte[] data, final int off) {
        super(data, off);
    }

    public RefreshMessage() {
        super((byte) 0xFF, COMMAND_UPPER, COMMAND_LOWER, new byte[0]);
    }

    static boolean matches(final byte[] data, final int off) {
        return matches(data, off, COMMAND_UPPER, COMMAND_LOWER);
    }

    public byte getGroup() {
        return this.payload.get(32);
    }

    public String getName() {
        return new String(this.payload.array(), 0, 16, StandardCharsets.UTF_8).trim();
    }

    public byte getMode() {
        return this.payload.get(33);
    }

    public byte getScene() {
        return this.payload.get(62);
    }

    public byte getRed() {
        return this.payload.get(40);
    }

    public byte getGreen() {
        return this.payload.get(41);
    }

    public byte getBlue() {
        return this.payload.get(42);
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
}
