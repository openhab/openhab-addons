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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link SerialNumberMessage} handles the Serial Number Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class SerialNumberMessage extends DreamScreenMessage {
    private static final byte COMMAND_UPPER = 0x01;
    private static final byte COMMAND_LOWER = 0x03;

    protected SerialNumberMessage(final byte[] data, final int off) {
        super(data, off);
    }

    static boolean matches(final byte[] data, final int off) {
        return matches(data, off, COMMAND_UPPER, COMMAND_LOWER);
    }

    public int getSerialNumber() {
        return this.payload.getInt(0);
    }

    @Override
    public DatagramPacket writePacket(InetAddress address, int port) {
        return broadcastReadPacket(address, port);
    }

    @Override
    public String toString() {
        return "Serial Number " + getSerialNumber();
    }
}
