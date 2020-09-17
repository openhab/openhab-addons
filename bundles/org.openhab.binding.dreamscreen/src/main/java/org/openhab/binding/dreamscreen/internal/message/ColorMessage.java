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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ColorMessage} handles the Color Number Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class ColorMessage extends DreamScreenMessage {
    static final byte COMMAND_UPPER = 0x03;
    static final byte COMMAND_LOWER = 0x05;

    protected ColorMessage(final byte[] data) {
        super(data);
    }

    public ColorMessage(byte group, byte red, byte green, byte blue) {
        super(group, COMMAND_UPPER, COMMAND_LOWER, new byte[] { red, green, blue });
    }

    static boolean matches(final byte[] data) {
        return matches(data, COMMAND_UPPER, COMMAND_LOWER);
    }

    public byte getRed() {
        return this.payload.get(0);
    }

    public byte getGreen() {
        return this.payload.get(1);
    }

    public byte getBlue() {
        return this.payload.get(2);
    }

    @Override
    public String toString() {
        return String.format("Color %02X:%02X:%02X", getRed(), getGreen(), getBlue());
    }
}
